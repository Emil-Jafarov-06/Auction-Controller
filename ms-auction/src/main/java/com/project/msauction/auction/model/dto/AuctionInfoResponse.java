package com.project.msauction.auction.model.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AuctionInfoResponse(Long id, BigDecimal startPrice, String status) {}
