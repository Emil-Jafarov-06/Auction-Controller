package com.project.msbidding.bid.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auction_bid_state", schema = "bidding_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionBidState {

    @Id
    @Column(name = "auction_id")
    private Long auctionId;

    @Column(name = "highest_bid_amount", nullable = false)
    private BigDecimal highestBidAmount;

    @Column(name = "last_bidder_id", nullable = false)
    private Long lastBidderId;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
