package com.project.msbidding.bid.model.dto;

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
public class BidResponse {

    private Long id;
    private Long auctionId;
    private Long userId;
    private BigDecimal amount;
    private LocalDateTime placedAt;

}
