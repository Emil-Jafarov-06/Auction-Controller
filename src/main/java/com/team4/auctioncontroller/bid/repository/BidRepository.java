package com.team4.auctioncontroller.bid.repository;

import com.team4.auctioncontroller.bid.model.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    Bid findTopByAuctionIdOrderByAmountDesc(Long auctionId);

    List<Bid> findBidsByAuctionIdOrderByAmountDesc(Long auctionId);
}
