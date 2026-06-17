package com.team4.auctioncontroller.auction.model;

import com.team4.auctioncontroller.enums.AuctionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Auction {

    private Long id;
    private String title;
    private String description;
    private BigDecimal startPrice;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private AuctionStatus status;
    private Long winnerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Boolean deleted;

}
