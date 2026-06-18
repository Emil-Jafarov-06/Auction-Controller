package com.team4.auctioncontroller.auction.controller;

import com.team4.auctioncontroller.auction.model.dto.AuctionCreateRequest;
import com.team4.auctioncontroller.auction.model.dto.AuctionResponse;
import com.team4.auctioncontroller.auction.service.AuctionService;
import com.team4.auctioncontroller.bidder.model.dto.BidderResponse;
import com.team4.auctioncontroller.enums.AuctionStatus;
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
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;

    @PostMapping()
    public ResponseEntity<?> createAuction(@RequestBody @Valid AuctionCreateRequest auctionCreateRequest) {
        AuctionResponse auctionResponse = auctionService.createAuction(auctionCreateRequest);
        return new ResponseEntity<>(auctionResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable @Positive Long id) {
        AuctionResponse auctionResponse = auctionService.getById(id);
        return new ResponseEntity<>(auctionResponse, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<?> findAll(@RequestParam(required = false) AuctionStatus status) {
        List<AuctionResponse> auctionResponseList = auctionService.getAll(status);
        return new ResponseEntity<>(auctionResponseList, HttpStatus.OK);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<String> activateAuction(@PathVariable @Positive Long id) {
        auctionService.activateAuction(id);
        return new ResponseEntity<>("Auction successfully activated!", HttpStatus.OK);
    }

    @PatchMapping("{id}/finish")
    public ResponseEntity<BidderResponse> finishAuction(@PathVariable @Positive Long id) {
        BidderResponse winner = auctionService.finishAuction(id);
        return new ResponseEntity<>(winner, HttpStatus.OK);
    }

    @PatchMapping("{id}/cancel")
    public ResponseEntity<String> cancelAuction(@PathVariable @Positive Long id) {
        auctionService.cancelAuction(id);
        return new ResponseEntity<>("Auction successfully cancelled!", HttpStatus.OK);
    }

}
