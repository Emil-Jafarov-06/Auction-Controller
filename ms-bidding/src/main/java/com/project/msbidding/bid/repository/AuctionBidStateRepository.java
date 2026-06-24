package com.project.msbidding.bid.repository;

import com.project.msbidding.bid.model.entity.AuctionBidState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface AuctionBidStateRepository extends JpaRepository<AuctionBidState, Long> {

    @Modifying
    @Query(value = """
    insert into bidding_schema.auction_bid_state(auction_id, highest_bid_amount, last_bidder_id, updated_at)
    values (:auctionId, :amount, :bidderId, CURRENT_TIMESTAMP)
    on conflict (auction_id)
    do update
    set
            highest_bid_amount = excluded.highest_bid_amount,
            last_bidder_id = excluded.last_bidder_id,
            updated_at = CURRENT_TIMESTAMP
    where excluded.highest_bid_amount > auction_bid_state.highest_bid_amount
           and excluded.last_bidder_id is distinct from auction_bid_state.last_bidder_id
    """, nativeQuery = true)
    int tryAcceptBid(@Param("auctionId") Long auctionId, @Param("bidderId") Long bidderId, @Param("amount") BigDecimal amount);

}
