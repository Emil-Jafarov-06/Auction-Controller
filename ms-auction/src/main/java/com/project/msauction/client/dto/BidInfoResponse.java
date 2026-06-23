package com.project.msauction.client.dto;

public record BidInfoResponse(Long auctionId, boolean hasBid, Long bidderId) {}
