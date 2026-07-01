package com.project.msbidding.bid.controller;

import com.project.msbidding.bid.model.dto.BidResponse;
import com.project.msbidding.bid.model.dto.PlaceBidRequest;
import com.project.msbidding.bid.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
@Tag(name = "Bid", description = "Endpoints for placing bids and reading auction bids")
public class BidController {

    private final BidService bidService;

    @Operation(
            summary = "Place bid",
            description = "Places a bid for an active auction. The bidder id is passed through the Bidder-Id request header."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Bid placed successfully",
                    content = @Content(schema = @Schema(implementation = BidResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid bid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Auction or bidder not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<BidResponse> placeBid(
            @Valid @RequestBody PlaceBidRequest bidRequest,

            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") @NotNull @Positive Long bidderId
    ) {
        BidResponse bidResponse = bidService.placeBid(bidRequest, bidderId);
        return new ResponseEntity<>(bidResponse, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get bids for auction",
            description = "Returns all bids placed for a specific auction."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bids returned successfully"),
            @ApiResponse(responseCode = "404", description = "Auction not found", content = @Content)
    })
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<List<BidResponse>> getBidsForAuction(
            @Parameter(description = "Auction ID", example = "1")
            @PathVariable @Positive Long auctionId
    ) {
        List<BidResponse> bidResponses = bidService.getAllBidsForAuction(auctionId);
        return new ResponseEntity<>(bidResponses, HttpStatus.OK);
    }

    @Operation(
            summary = "Get highest bid for auction",
            description = "Returns the highest bid for a specific auction."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Highest bid returned successfully",
                    content = @Content(schema = @Schema(implementation = BidResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Auction or bid not found", content = @Content)
    })
    @GetMapping("/auction/{auctionId}/highest")
    public ResponseEntity<BidResponse> getHighestBidForAuction(
            @Parameter(description = "Auction ID", example = "1")
            @PathVariable @Positive Long auctionId
    ) {
        BidResponse bidResponse = bidService.getHighestBidForAuction(auctionId);
        return new ResponseEntity<>(bidResponse, HttpStatus.OK);
    }
}