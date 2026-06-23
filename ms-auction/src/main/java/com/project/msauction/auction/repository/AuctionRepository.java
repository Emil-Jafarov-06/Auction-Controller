package com.project.msauction.auction.repository;


import com.project.msauction.auction.mapper.AuctionMapper;
import com.project.msauction.auction.model.Auction;
import com.project.msauction.enums.AuctionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AuctionRepository {

  //TODO niye birbasha AuctionMapper istifade etmirik?

    private final AuctionMapper auctionMapper;

    public Auction findById(Long id) {
        return auctionMapper.findById(id);
    }

    public List<Auction> findAll() {
        return auctionMapper.findAll();
    }

    public List<Auction> findAllByStatus(AuctionStatus status) {
        return auctionMapper.findAllByStatus(status);
    }

    public Auction save(Auction auction) {
        return auctionMapper.insertAuction(auction);
    }

    public int updateStatus(Long id, AuctionStatus status) {
        return auctionMapper.updateStatus(id, status);
    }

    public int updateWinner(Long id, Long winnerId) {
        return auctionMapper.updateWinner(id, winnerId);
    }

    public int finishExpiredAuctions() {
        return auctionMapper.finishExpiredAuctions();
    }

    public int startAuctions() {
        return auctionMapper.startAuctions();
    }

}
