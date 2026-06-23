package com.project.msbidding.bid.model.dto;

import lombok.Builder;

@Builder
public record BidInfoResponse(boolean hasBid, Long bidderId) {}
