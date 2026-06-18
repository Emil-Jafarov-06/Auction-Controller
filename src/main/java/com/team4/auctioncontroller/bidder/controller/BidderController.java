package com.team4.auctioncontroller.bidder.controller;

import com.team4.auctioncontroller.bidder.model.dto.BidderRegisterRequest;
import com.team4.auctioncontroller.bidder.model.dto.BidderResponse;
import com.team4.auctioncontroller.bidder.service.BidderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bidders")
public class BidderController {

    private final BidderService bidderService;

    @PostMapping
    public ResponseEntity<BidderResponse> registerBidder(@Valid @RequestBody BidderRegisterRequest bidderRegisterRequest) {
        BidderResponse bidderResponse = bidderService.registerBidder(bidderRegisterRequest);
        return new ResponseEntity<>(bidderResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BidderResponse> getBidderById(@PathVariable @Positive Long id) {
        BidderResponse bidderResponse = bidderService.getById(id);
        return new ResponseEntity<>(bidderResponse, HttpStatus.OK);
    }

}
