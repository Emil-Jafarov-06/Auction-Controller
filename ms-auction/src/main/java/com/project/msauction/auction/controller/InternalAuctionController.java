package com.project.msauction.auction.controller;

import com.project.msauction.auction.model.dto.AuctionInfoResponse;
import com.project.msauction.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/auctions")
public class InternalAuctionController {

    private final AuctionService auctionService;

    @GetMapping("/{id}/auction-info")
    public AuctionInfoResponse getAuctionInfoResponse(@PathVariable Long id){
        return auctionService.getInfoResponse(id);
    }

}
