package com.project.msbidding.bid.repository;

import com.project.msbidding.bid.model.entity.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    Bid findTopByAuctionIdOrderByPlacedAtDesc(Long auctionId);

    @Query(value = """
        select * from (
            select b.*,
                   row_number() over (
                       partition by b.auction_id
                       order by b.amount desc, b.placed_at desc
                   ) as rn
            from bidding_schema.bid b
            where b.auction_id in (:auctionIds)
        ) ranked
        where ranked.rn = 1
        """, nativeQuery = true)
    List<Bid> findHighestBidsForAuctions(@Param("auctionIds") List<Long> auctionIds);

    Page<Bid> findBidsByAuctionIdOrderByPlacedAtDesc(Long auctionId, Pageable pageable);

}
