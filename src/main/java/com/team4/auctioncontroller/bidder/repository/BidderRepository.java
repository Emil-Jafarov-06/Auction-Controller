package com.team4.auctioncontroller.bidder.repository;

import com.team4.auctioncontroller.bidder.model.entity.Bidder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BidderRepository extends JpaRepository<Bidder, Long> {



}
