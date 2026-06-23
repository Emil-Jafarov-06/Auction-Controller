package com.project.msauction.auction.service;

import com.project.msauction.auction.mapper.AuctionMapper;
import com.project.msauction.auction.model.Auction;
import com.project.msauction.auction.model.dto.*;
import com.project.msauction.client.BiddingClientForAuction;
import com.project.msauction.client.dto.BidInfoResponse;
import com.project.msauction.enums.AuctionStatus;
import com.project.msauction.exception.BadRequestException;
import com.project.msauction.exception.NotFoundException;
import com.project.msauction.exception.NotSavedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionMapper auctionMapper;
    private final BiddingClientForAuction biddingClient;

    public AuctionResponse createAuction(AuctionCreateRequest request) {
        if(request.getStartAt().isBefore(LocalDateTime.now())
                || !request.getStartAt().isBefore(request.getEndAt()))
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

        Auction savedAuction = auctionMapper.insertAuction(auction);
        if(savedAuction == null) {
            throw new NotSavedException("Auction could not be saved!");
        }

        return toResponse(savedAuction);
    }

    public AuctionResponse getById(Long id) {
        Auction auction = Optional.ofNullable(auctionMapper.findById(id))
                .orElseThrow(() -> new NotFoundException("Auction not found!"));

        return toResponse(auction);
    }

    public PageResponse<AuctionResponse> getAll(AuctionFilter filter, int pageSize, int pageNumber) {
        if(pageSize <= 0){
            throw new BadRequestException("Page size must be greater than zero!");
        }
        if (pageNumber <= 0) {
            throw new BadRequestException("Page number must be greater than zero!");
        }
        List<Auction> auctions = auctionMapper.findFiltered(filter, pageSize, (pageNumber-1) * pageSize);
        long totalCount = auctionMapper.countFiltered(filter);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        List<AuctionResponse> auctionResponses =
                auctions.stream()
                        .map(this::toResponse)
                        .toList();

        return new PageResponse<>(auctionResponses, pageNumber, pageSize, totalCount, totalPages);
    }

    public void activateAuction(Long id) {
        Auction auction = Optional.ofNullable(auctionMapper.findById(id))
                .orElseThrow(() -> new NotFoundException("Auction not found!"));
        if(auction.getStatus() != AuctionStatus.DRAFT){
            throw new BadRequestException("Auction to activate is not DRAFT!");
        }
        if(!auction.getEndAt().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("End time must be in future to activate auction!");
        }
        int changed = auctionMapper.updateStatus(id, AuctionStatus.ACTIVE);
        if(changed <= 0) {
            throw new BadRequestException("Auction could not be activated!");
        }
    }

    @Transactional
    public void finishAuction(Long auctionId) {
        Auction auction = Optional.ofNullable(auctionMapper.findById(auctionId))
                .orElseThrow(() -> new NotFoundException("Auction not found!"));
        if(auction.getStatus() != AuctionStatus.ACTIVE){
            throw new BadRequestException("Auction to finish is not ACTIVE!");
        }

        BidInfoResponse highestBidInfo = biddingClient.findHighestBidInfo(auctionId);

        int statusUpdate, winnerUpdate;
        if(!highestBidInfo.hasBid()) {
            statusUpdate =
                    auctionMapper.updateStatus(auctionId, AuctionStatus.NO_BIDDER);
            if(statusUpdate <= 0){
                throw new BadRequestException("Auction status could not be updated!");
            }
            return;
        }

        statusUpdate =
                auctionMapper.updateStatus(auctionId, AuctionStatus.COMPLETED);
        winnerUpdate =
                auctionMapper.updateWinner(auctionId, highestBidInfo.bidderId());
        if(statusUpdate <= 0 || winnerUpdate <= 0) {
            throw new BadRequestException("Auction status could not be updated!");
        }
    }

    public void cancelAuction(Long id) {
        Auction auction = Optional.ofNullable(auctionMapper.findById(id))
                .orElseThrow(() -> new NotFoundException("Auction not found!"));
        if(auction.getStatus() != AuctionStatus.DRAFT &&
                auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new BadRequestException("Auction to cancel must be ACTIVE or DRAFT!");
        }

        int statusChange =
                auctionMapper.updateStatus(auction.getId(), AuctionStatus.CANCELLED);
        if(statusChange <= 0) {
            throw new BadRequestException("Auction could not be cancelled!");
        }
    }

    public AuctionInfoResponse getInfoResponse(Long id) {
        Auction auction = Optional.ofNullable(auctionMapper.findById(id))
                .orElseThrow(() -> new NotFoundException("Auction not found!"));

        return AuctionInfoResponse.builder()
                .id(auction.getId())
                .startPrice(auction.getStartPrice())
                .status(String.valueOf(auction.getStatus())).build();

    }

    public int finishExpiredAuctions() {
        var expiredActiveAuctionIds = auctionMapper.findExpiredActiveAuctions();
        if(expiredActiveAuctionIds.isEmpty()) {
            return 0;
        }

        List<BidInfoResponse> responses = biddingClient.getBiddingForGivenIds(expiredActiveAuctionIds);
        List<ExpiredAuctionsUpdate> updates = responses.stream()
                .map(r -> ExpiredAuctionsUpdate.builder()
                        .id(r.auctionId())
                        .status(r.hasBid() ? AuctionStatus.COMPLETED : AuctionStatus.NO_BIDDER)
                        .bidderId(r.bidderId())
                        .build()).toList();

        if(updates.isEmpty()) {
            return 0;
        }
        return auctionMapper.finishExpiredAuctions(updates);
    }

    public int startAuctions() {
        return auctionMapper.startAuctions();
    }

    public AuctionResponse toResponse(Auction auction) {
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


