package com.team4.auctioncontroller.auction.repository;

import com.team4.auctioncontroller.auction.mapper.AuctionMapper;
import com.team4.auctioncontroller.auction.model.Auction;
import com.team4.auctioncontroller.enums.AuctionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AuctionRepository {

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

}
