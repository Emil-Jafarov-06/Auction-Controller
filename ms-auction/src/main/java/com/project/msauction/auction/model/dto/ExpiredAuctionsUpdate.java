package com.project.msauction.auction.model.dto;

import com.project.msauction.enums.AuctionStatus;
import lombok.Builder;

@Builder
public record ExpiredAuctionsUpdate(Long id, AuctionStatus status, Long bidderId) {}
