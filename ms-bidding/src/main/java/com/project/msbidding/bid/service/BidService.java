package com.project.msbidding.bid.service;

import com.project.msbidding.bid.model.dto.BidInfoResponse;
import com.project.msbidding.bid.model.dto.BidResponse;
import com.project.msbidding.bid.model.dto.PlaceBidRequest;
import com.project.msbidding.bid.model.entity.Bid;
import com.project.msbidding.bid.repository.BidRepository;
import com.project.msbidding.bidder.model.entity.Bidder;
import com.project.msbidding.bidder.repository.BidderRepository;
import com.project.msbidding.client.AuctionClientForBidding;
import com.project.msbidding.client.dto.AuctionInfoResponse;
import com.project.msbidding.exception.BadRequestException;
import com.project.msbidding.exception.NotFoundException;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BidService {

    private final AuctionClientForBidding auctionClient;
    private final BidderRepository bidderRepository;
    private final BidRepository bidRepository;

    public BidResponse placeBid(PlaceBidRequest bidRequest) {
        Long auctionId = bidRequest.getAuctionId();
        Long bidderId = bidRequest.getBidderId();

        AuctionInfoResponse auction = auctionClient.getAuctionInfoResponse(auctionId);
        Bidder bidder = Optional.ofNullable(bidderRepository.findByIdAndDeletedFalse(bidderId))
                .orElseThrow(() -> new NotFoundException("Bidder not found"));

        if(!auction.status().equals("ACTIVE")){
            throw new BadRequestException("Auction is not active.");
        }

        Bid highestBid = bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId);
        if (highestBid == null) {
            if (bidRequest.getAmount().compareTo(auction.startPrice()) <= 0) {
                throw new BadRequestException("First bid amount must be greater than start price!");
            }
        } else {
            if (bidRequest.getAmount().compareTo(highestBid.getAmount()) <= 0) {
                throw new BadRequestException("A new bid amount must be greater than highest bid!");
            }
            if (highestBid.getBidderId().equals(bidderId)) {
                throw new BadRequestException("A bidder cannot place two consecutive bids on the same auction.");
            }
        }

        Bid bid = Bid.builder()
                .auctionId(bidRequest.getAuctionId())
                .bidderId(bidRequest.getBidderId())
                .amount(bidRequest.getAmount())
                .build();

        Bid savedBid = bidRepository.save(bid);
        return toResponse(savedBid);
    }

    public List<BidResponse> getAllBidsForAuction(@Positive Long id) {
        List<Bid> bids = bidRepository.findBidsByAuctionIdOrderByAmountDesc(id);
        if(bids.isEmpty()){
            return new ArrayList<>();
        }
        return bids.stream()
                .map(this::toResponse)
                .toList();
    }

    public BidResponse getHighestBidForAuction(@Positive Long auctionId) {
        Bid highestBid = bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId);
        if(highestBid == null){
            throw new NotFoundException("No bids were placed for this auction!");
        }
        return toResponse(highestBid);
    }

    public BidInfoResponse getBiddingInfo(Long id) {
        Bid bid = bidRepository.findTopByAuctionIdOrderByAmountDesc(id);
        if(bid == null){
            return BidInfoResponse.builder()
                    .hasBid(false)
                    .bidderId(null).build();
        } else {
            return BidInfoResponse.builder()
                    .hasBid(true)
                    .bidderId(bid.getBidderId()).build();
        }
    }

    public BidResponse toResponse(Bid bid) {
        return BidResponse.builder()
                .id(bid.getId())
                .bidderId(bid.getBidderId())
                .auctionId(bid.getAuctionId())
                .amount(bid.getAmount())
                .placedAt(bid.getPlacedAt()).build();
    }

}
