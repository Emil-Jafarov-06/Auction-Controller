package com.team4.auctioncontroller.bid.service;

import com.team4.auctioncontroller.auction.model.Auction;
import com.team4.auctioncontroller.auction.repository.AuctionRepository;
import com.team4.auctioncontroller.bid.model.dto.BidResponse;
import com.team4.auctioncontroller.bid.model.dto.PlaceBidRequest;
import com.team4.auctioncontroller.bid.model.entity.Bid;
import com.team4.auctioncontroller.bid.repository.BidRepository;
import com.team4.auctioncontroller.bidder.model.entity.Bidder;
import com.team4.auctioncontroller.bidder.repository.BidderRepository;
import com.team4.auctioncontroller.enums.AuctionStatus;
import com.team4.auctioncontroller.exception.BadRequestException;
import com.team4.auctioncontroller.exception.NotFoundException;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BidService {

    private final AuctionRepository auctionRepository;
    private final BidderRepository bidderRepository;
    private final BidRepository bidRepository;

    public BidResponse placeBid(PlaceBidRequest bidRequest) {
        Long auctionId = bidRequest.getAuctionId();
        Long bidderId = bidRequest.getBidderId();

        Auction auction = Optional.ofNullable(auctionRepository.findById(auctionId))
                .orElseThrow(() -> new NotFoundException("Auction not found"));
        Bidder bidder = Optional.ofNullable(bidderRepository.findByIdAndDeletedFalse(bidderId))
                .orElseThrow(() -> new NotFoundException("Bidder not found"));

        if(auction.getStatus() != AuctionStatus.ACTIVE){
            throw new BadRequestException("Auction is not active.");
        }

        Bid highestBid = bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId);
        if (highestBid == null) {
            if (bidRequest.getAmount().compareTo(auction.getStartPrice()) <= 0) {
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
        List<BidResponse> bidResponses = bids.stream()
                .map(this::toResponse)
                .toList();
        return bidResponses;
    }

    public BidResponse getHighestBidForAuction(@Positive Long auctionId) {
        Bid highestBid = bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId);
        if(highestBid == null){
            throw new NotFoundException("No bids were placed for this auction!");
        }
        return toResponse(highestBid);
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
