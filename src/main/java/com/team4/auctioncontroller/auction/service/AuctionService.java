package com.team4.auctioncontroller.auction.service;

import com.team4.auctioncontroller.auction.model.Auction;
import com.team4.auctioncontroller.auction.model.dto.AuctionCreateRequest;
import com.team4.auctioncontroller.auction.model.dto.AuctionResponse;
import com.team4.auctioncontroller.auction.repository.AuctionRepository;
import com.team4.auctioncontroller.bid.model.entity.Bid;
import com.team4.auctioncontroller.bid.repository.BidRepository;
import com.team4.auctioncontroller.bidder.model.dto.BidderResponse;
import com.team4.auctioncontroller.bidder.model.entity.Bidder;
import com.team4.auctioncontroller.bidder.repository.BidderRepository;
import com.team4.auctioncontroller.bidder.service.BidderService;
import com.team4.auctioncontroller.enums.AuctionStatus;
import com.team4.auctioncontroller.exception.NotFoundException;
import com.team4.auctioncontroller.exception.BadRequestException;
import com.team4.auctioncontroller.exception.NotSavedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final BidderRepository bidderRepository;

    public AuctionResponse createAuction(AuctionCreateRequest request) {

        if(request.getStartAt().isBefore(LocalDateTime.now())
                || request.getEndAt().isBefore(LocalDateTime.now())
                || request.getStartAt().isAfter(request.getEndAt()))
        {
            throw new BadRequestException("Start and end times must be configured correctly!");
        }

        Auction auction = Auction.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startPrice(request.getStartPrice())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .status(AuctionStatus.DRAFT)
                .deleted(false)
                .build();

        Auction savedAuction = auctionRepository.save(auction);
        if(savedAuction == null) {
            throw new NotSavedException("Auction could not be saved!");
        }

        return toResponse(savedAuction);
    }

    public AuctionResponse getById(Long id) {
        Auction auction = Optional.ofNullable(auctionRepository.findById(id))
                .orElseThrow(() -> new NotFoundException("Auction not found!"));

        return toResponse(auction);
    }

    public List<AuctionResponse> getAll(AuctionStatus status) {
        List<Auction> auctions;
        if(Objects.isNull(status)) {
            auctions = auctionRepository.findAll();
        } else {
            auctions = auctionRepository.findAllByStatus(status);
        }

        if(auctions.isEmpty()) {
            return new ArrayList<>();
        }
        return auctions.stream()
                .map(this::toResponse)
                .toList();
    }

    public void activateAuction(Long id) {
        Auction auction = Optional.ofNullable(auctionRepository.findById(id))
                .orElseThrow(() -> new NotFoundException("Auction not found!"));
        if(auction.getStatus() != AuctionStatus.DRAFT){
            throw new BadRequestException("Auction to activate is not DRAFT!");
        }
        if(!auction.getEndAt().isAfter(LocalDateTime.now())) {
            throw  new BadRequestException("End time must be in future to activate auction!");
        }
        int changed = auctionRepository.updateStatus(id, AuctionStatus.ACTIVE);
        if(changed <= 0) {
            throw new BadRequestException("Auction could not be activated!");
        }
    }

    @Transactional
    public BidderResponse finishAuction(Long auctionId) {
        Auction auction = Optional.ofNullable(auctionRepository.findById(auctionId))
                .orElseThrow(() -> new NotFoundException("Auction not found!"));
        if(auction.getStatus() != AuctionStatus.ACTIVE){
            throw new BadRequestException("Auction to finish is not ACTIVE!");
        }

        Bid highestBid = Optional.ofNullable(bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId))
                .orElseThrow(() -> new BadRequestException("Cannot complete auction because it has no bids."));

        int statusUpdate =
                auctionRepository.updateStatus(auctionId, AuctionStatus.COMPLETED);
        int winnerUpdate =
                auctionRepository.updateWinner(auctionId, highestBid.getBidderId());
        if(statusUpdate <= 0 || winnerUpdate <= 0) {
            throw new BadRequestException("Auction could not be completed!");
        }

        Bidder bidder = bidderRepository.findByIdAndDeletedFalse(highestBid.getBidderId());
        return BidderService.toResponse(bidder);
    }

    public void cancelAuction(Long id) {
        Auction auction = Optional.ofNullable(auctionRepository.findById(id))
                .orElseThrow(() -> new NotFoundException("Auction not found!"));
        if(auction.getStatus() != AuctionStatus.DRAFT &&
                auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new BadRequestException("Auction to cancel must be ACTIVE or DRAFT!");
        }

        int statusChange =
                auctionRepository.updateStatus(auction.getId(), AuctionStatus.CANCELLED);
        if(statusChange <= 0) {
            throw new BadRequestException("Auction could not be cancelled!");
        }
    }

    public int finishExpiredAuctions() {
        return auctionRepository.finishExpiredAuctions();
    }

    private AuctionResponse toResponse(Auction auction) {
        return AuctionResponse.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .description(auction.getDescription())
                .startPrice(auction.getStartPrice())
                .startAt(auction.getStartAt())
                .endAt(auction.getEndAt())
                .status(auction.getStatus())
                .winnerId(auction.getWinnerId())
                .createdAt(auction.getCreatedAt())
                .modifiedAt(auction.getModifiedAt())
                .build();
    }
}
