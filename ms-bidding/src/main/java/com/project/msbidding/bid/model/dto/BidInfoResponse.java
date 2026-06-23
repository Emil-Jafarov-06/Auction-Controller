package com.project.msbidding.bid.model.dto;

import lombok.Builder;

@Builder
public record BidInfoResponse(Long auctionId, boolean hasBid, Long bidderId) {}
