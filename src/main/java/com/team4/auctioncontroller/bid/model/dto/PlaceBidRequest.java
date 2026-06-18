package com.team4.auctioncontroller.bid.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceBidRequest {

    @NotNull
    @Positive
    private Long auctionId;

    @NotNull
    @Positive
    private Long bidderId;

    @NotNull
    @Positive
    private BigDecimal amount;

}
