package com.project.msbidding.client.dto;

import java.math.BigDecimal;

public record AuctionInfoResponse(Long id, BigDecimal startPrice, String status) {}