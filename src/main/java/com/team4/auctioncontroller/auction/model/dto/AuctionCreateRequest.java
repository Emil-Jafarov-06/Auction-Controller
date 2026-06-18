package com.team4.auctioncontroller.auction.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuctionCreateRequest {

    @NotBlank
    @Length(min = 2, max = 255)
    private String title;

    private String description;

    @Positive
    @NotNull
    private BigDecimal startPrice;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

}
