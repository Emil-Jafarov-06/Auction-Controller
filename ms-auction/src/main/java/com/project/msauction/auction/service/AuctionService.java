package com.project.msauction.auction.service;

import com.project.msauction.auction.model.Auction;
import com.project.msauction.auction.model.dto.AuctionCreateRequest;
import com.project.msauction.auction.model.dto.AuctionInfoResponse;
import com.project.msauction.auction.model.dto.AuctionResponse;
import com.project.msauction.auction.repository.AuctionRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
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

    // -
    public List<AuctionResponse> getAll(AuctionStatus status) {
        List<Auction> auctions;
        if(Objects.isNull(status)) {
          //TODO niye birbasha AuctionMapper istifade etmirik?
          //TODO  findAll ve findAllByStatus methodlarini bir methodda birleshdir ve input kimi AuctionFilter dto yarat. Mybatisde
          //value-su bosh olmayan fieldleri yoxla ve onlar ucun filterasiya tetbiq et

          //TODO pagination (sort, limit) implement et

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
            throw new BadRequestException("End time must be in future to activate auction!");
        }
        int changed = auctionRepository.updateStatus(id, AuctionStatus.ACTIVE);
        if(changed <= 0) {
            throw new BadRequestException("Auction could not be activated!");
        }
    }

    // -
    @Transactional
    //TODO finishAuction apinin AuctionResponse (detalli response data) qaytarmaqina ehtiyac yoxdur. Void ve 200 status code OK-dur
    public AuctionResponse finishAuction(Long auctionId) {
        Auction auction = Optional.ofNullable(auctionRepository.findById(auctionId))
                .orElseThrow(() -> new NotFoundException("Auction not found!"));
        if(auction.getStatus() != AuctionStatus.ACTIVE){
            throw new BadRequestException("Auction to finish is not ACTIVE!");
        }

        BidInfoResponse highestBid = biddingClient.findHighestBidInfo(auctionId);

        int statusUpdate, winnerUpdate;
        if(Objects.isNull(highestBid)) {
            statusUpdate =
                    auctionRepository.updateStatus(auctionId, AuctionStatus.NO_BIDDER);
            if(statusUpdate <= 0){
                throw new BadRequestException("Auction status could not be updated!");
            }

            Auction updatedAuction = auctionRepository.findById(auctionId);
            return toResponse(updatedAuction);
        }

        statusUpdate =
                auctionRepository.updateStatus(auctionId, AuctionStatus.COMPLETED);
        winnerUpdate =
                auctionRepository.updateWinner(auctionId, highestBid.bidderId());
        if(statusUpdate <= 0 || winnerUpdate <= 0) {
            throw new BadRequestException("Auction status could not be updated!");
        }

        Auction updatedAuction = auctionRepository.findById(auctionId);
        return toResponse(updatedAuction);
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

    public AuctionInfoResponse getInfoResponse(Long id) {
        Auction auction = Optional.ofNullable(auctionRepository.findById(id))
                .orElseThrow(() -> new NotFoundException("Auction not found!"));

        return AuctionInfoResponse.builder()
                .id(auction.getId())
                .startPrice(auction.getStartPrice())
                .status(String.valueOf(auction.getStatus())).build();

    }

    // -
    public int finishExpiredAuctions() {
        return auctionRepository.finishExpiredAuctions();
    }

    // -
    public int startAuctions() {
        return auctionRepository.startAuctions();
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


