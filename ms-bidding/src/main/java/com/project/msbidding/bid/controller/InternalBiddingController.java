package com.project.msbidding.bid.controller;

import com.project.msbidding.bid.model.dto.BidInfoResponse;
import com.project.msbidding.bid.service.BidService;
import com.project.msbidding.client.dto.AuctionInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/biddings")
public class InternalBiddingController {

    private final BidService bidService;

    @GetMapping("/{id}/bid-info")
    public BidInfoResponse getBiddingInfoResponse(@PathVariable Long id) {
        return bidService.getBiddingInfo(id);
    }

}
