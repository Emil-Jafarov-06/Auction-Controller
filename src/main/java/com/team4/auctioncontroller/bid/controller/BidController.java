package com.team4.auctioncontroller.bid.controller;

import com.team4.auctioncontroller.bid.model.dto.BidResponse;
import com.team4.auctioncontroller.bid.model.dto.PlaceBidRequest;
import com.team4.auctioncontroller.bid.service.BidService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bids")
public class BidController {

    private final BidService bidService;

    @PostMapping
    public ResponseEntity<BidResponse> placeBid(@RequestBody @Valid PlaceBidRequest bidRequest) {
        BidResponse bidResponse = bidService.placeBid(bidRequest);
        return new ResponseEntity<>(bidResponse, HttpStatus.CREATED);
    }

    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<List<BidResponse>> getBidsForAuction(@PathVariable @Positive Long auctionId){
        List<BidResponse> bidResponses = bidService.getAllBidsForAuction(auctionId);
        return new ResponseEntity<>(bidResponses, HttpStatus.OK);
    }

    @GetMapping("/auction/{auctionId}/highest")
    public ResponseEntity<BidResponse> getHighestBidForAuction(@PathVariable @Positive Long auctionId){
        BidResponse bidResponse = bidService.getHighestBidForAuction(auctionId);
        return new ResponseEntity<>(bidResponse, HttpStatus.OK);
    }

}
