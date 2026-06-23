package com.project.msbidding.bid.controller;

import com.project.msbidding.bid.model.dto.BidInfoResponse;
import com.project.msbidding.bid.service.BidService;
import com.project.msbidding.client.dto.AuctionInfoResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/biddings")
public class InternalBiddingController {

    private final BidService bidService;

    @GetMapping("/{id}/bid-info")
    public BidInfoResponse getBiddingInfoResponse(@PathVariable("id") Long auctionId) {
        return bidService.getBiddingInfo(auctionId);
    }

    @PostMapping("/batch")
    public List<BidInfoResponse> getBiddingInformationForAuctions(@RequestBody List<Long> auctionIds){
        return bidService.getBiddingInformationForAuctions(auctionIds);
    }

}
