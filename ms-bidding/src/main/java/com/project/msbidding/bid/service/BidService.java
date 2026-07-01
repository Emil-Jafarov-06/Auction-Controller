package com.project.msbidding.bid.service;

import com.project.msbidding.bid.model.dto.BidInfoResponse;
import com.project.msbidding.bid.model.dto.BidResponse;
import com.project.msbidding.bid.model.dto.PageResponse;
import com.project.msbidding.bid.model.dto.PlaceBidRequest;
import com.project.msbidding.bid.model.entity.Bid;
import com.project.msbidding.bid.repository.AuctionBidStateRepository;
import com.project.msbidding.bid.repository.BidRepository;
import com.project.msbidding.client.AuctionClientForBidding;
import com.project.msbidding.client.dto.AuctionInfoResponse;
import com.project.msbidding.exception.BadRequestException;
import com.project.msbidding.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {

    private final AuctionClientForBidding auctionClient;
    private final BidRepository bidRepository;
    private final AuctionBidStateRepository auctionBidStateRepository;

    @Transactional
    public BidResponse placeBid(PlaceBidRequest bidRequest, Long userId) {
        Long auctionId = bidRequest.getAuctionId();

        AuctionInfoResponse auction = auctionClient.getAuctionInfoResponse(auctionId);

        if(!auction.status().equals("ACTIVE")){
            throw new BadRequestException("Auction is not active.");
        }

        if (bidRequest.getAmount().compareTo(auction.startPrice()) <= 0) {
            throw new BadRequestException("First bid amount must be greater than start price!");
        }

        int updates = auctionBidStateRepository
                .tryAcceptBid(auctionId, userId, bidRequest.getAmount());

        if (updates <= 0){
            log.warn("Bid rejected: auctionId : {}, userId : {}, amount : {}",
                    auctionId,
                    userId,
                    bidRequest.getAmount());

            throw new BadRequestException("Rejected! Amount must be greater than current highest bid and a user cannot bid consecutively.");
        }

        Bid bid = Bid.builder()
                .auctionId(bidRequest.getAuctionId())
                .userId(userId)
                .amount(bidRequest.getAmount())
                .build();

        Bid savedBid = bidRepository.save(bid);

        log.info("Bid placed successfully: bidId = {}, auctionId = {}, userId = {}, amount = {}",
                savedBid.getId(),
                savedBid.getAuctionId(),
                savedBid.getUserId(),
                savedBid.getAmount());

        return toResponse(savedBid);
    }

    public PageResponse<BidResponse> getPaginatedBidsForAuction(Long id, int pageSize, int pageNumber) {
        if (pageSize <= 0) {
            throw new BadRequestException("Page size must be greater than 0!");
        }
        if (pageNumber <= 0) {
            throw new BadRequestException("Page number must be greater than 0!");
        }

        Page<Bid> bidPage = bidRepository.
                findBidsByAuctionIdOrderByPlacedAtDesc(
                        id,
                        PageRequest.of(pageNumber-1, pageSize, Sort.by(Sort.Direction.DESC, "placedAt"))
                );

        List<BidResponse> bidResponses = bidPage.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(bidResponses,
                pageNumber,
                pageSize,
                bidPage.getTotalElements(),
                bidPage.getTotalPages()
        );
    }

    public BidResponse getHighestBidForAuction(Long auctionId) {
        Bid highestBid = bidRepository.findTopByAuctionIdOrderByPlacedAtDesc(auctionId);
        if(highestBid == null){
            throw new NotFoundException("No bids were placed for this auction!");
        }
        return toResponse(highestBid);
    }

    public BidInfoResponse getBiddingInfo(Long auctionId) {
        Bid bid = bidRepository.findTopByAuctionIdOrderByPlacedAtDesc(auctionId);

        if(bid == null){
            log.info("Bidding info resolved: auctionId = {}, hasBid = {}",
                    auctionId,
                    false);

            return BidInfoResponse.builder()
                    .auctionId(auctionId)
                    .hasBid(false)
                    .bidderId(null).build();
        } else {
            log.info("Bidding info resolved: auctionId = {}, hasBid = {}, winnerId = {}",
                    auctionId,
                    true,
                    bid.getUserId());

            return BidInfoResponse.builder()
                    .auctionId(auctionId)
                    .hasBid(true)
                    .bidderId(bid.getUserId()).build();
        }
    }

    public List<BidInfoResponse> getBiddingInformationForAuctions(List<Long> auctionIds) {
        if(Objects.isNull(auctionIds) || auctionIds.isEmpty()){
            return new ArrayList<>();
        }

        log.info("Highest bid information requested for {} auctions",
                auctionIds.size());

        List<Bid> bids = bidRepository.findHighestBidsForAuctions(auctionIds);

        log.info("Highest bid information fetched for {} auctions out of {} auctions.",
                bids.size(),
                auctionIds.size());

        Map<Long, Bid> bidsByAuctionId = bids.stream()
                        .collect(Collectors.toMap(Bid::getAuctionId, b -> b));

        return auctionIds.stream()
                .map((auctionId) -> {
                    Bid bid = bidsByAuctionId.get(auctionId);

                    if(bid == null){
                        return BidInfoResponse.builder()
                                .auctionId(auctionId)
                                .hasBid(false)
                                .bidderId(null).build();
                    } else  {
                        return BidInfoResponse.builder()
                                .auctionId(auctionId)
                                .hasBid(true)
                                .bidderId(bid.getUserId()).build();
                    }
                }).toList();
    }

    public BidResponse toResponse(Bid bid) {
        return BidResponse.builder()
                .id(bid.getId())
                .userId(bid.getUserId())
                .auctionId(bid.getAuctionId())
                .amount(bid.getAmount())
                .placedAt(bid.getPlacedAt()).build();
    }
}
