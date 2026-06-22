package com.team4.auctioncontroller.auction.controller;

import com.team4.auctioncontroller.auction.model.dto.AuctionCreateRequest;
import com.team4.auctioncontroller.auction.model.dto.AuctionResponse;
import com.team4.auctioncontroller.auction.service.AuctionService;
import com.team4.auctioncontroller.enums.AuctionStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Auction", description = "Endpoints for creating, reading, activating, finishing, and cancelling auctions")
public class AuctionController {

    private final AuctionService auctionService;

    @Operation(
            summary = "Create auction",
            description = "Creates a new auction with title, description, start price, start time, and end time."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Auction created successfully",
                    content = @Content(schema = @Schema(implementation = AuctionResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid auction request", content = @Content)
    })
    @PostMapping
    public ResponseEntity<AuctionResponse> createAuction(
            @Valid @RequestBody AuctionCreateRequest auctionCreateRequest
    ) {
        AuctionResponse auctionResponse = auctionService.createAuction(auctionCreateRequest);
        return new ResponseEntity<>(auctionResponse, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get auction by ID",
            description = "Returns one auction by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Auction found",
                    content = @Content(schema = @Schema(implementation = AuctionResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Auction not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponse> findById(
            @Parameter(description = "Auction ID", example = "1")
            @PathVariable @Positive Long id
    ) {
        AuctionResponse auctionResponse = auctionService.getById(id);
        return new ResponseEntity<>(auctionResponse, HttpStatus.OK);
    }

    @Operation(
            summary = "Get all auctions",
            description = "Returns all auctions. You can optionally filter by auction status."
    )
    @ApiResponse(responseCode = "200", description = "Auction list returned successfully")
    @GetMapping

    //TODO findAll methodu ucun filterasiya imkanlarini genishlendir. Statusdan elave startAt, endAt , startPrice fieldlerine gore filterasiya imkani olsun (AuctionFilter dto)
    public ResponseEntity<List<AuctionResponse>> findAll(
            @Parameter(description = "Optional auction status filter", example = "ACTIVE")
            @RequestParam(required = false) AuctionStatus status
    ) {
        List<AuctionResponse> auctionResponseList = auctionService.getAll(status);
        return new ResponseEntity<>(auctionResponseList, HttpStatus.OK);
    }

    @Operation(
            summary = "Activate auction",
            description = "Manually activates a DRAFT auction. If activated manually before start time, startAt is changed to current timestamp."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Auction activated successfully"),
            @ApiResponse(responseCode = "400", description = "Auction cannot be activated", content = @Content),
            @ApiResponse(responseCode = "404", description = "Auction not found", content = @Content)
    })
    @PatchMapping("/{id}/activate")
    public ResponseEntity<String> activateAuction(
            @Parameter(description = "Auction ID", example = "1")
            @PathVariable @Positive Long id
    ) {
        auctionService.activateAuction(id);
        return new ResponseEntity<>("Auction successfully activated!", HttpStatus.OK);
    }

    @Operation(
            summary = "Finish auction",
            description = "Manually finishes an ACTIVE auction. If there is a highest bid, status becomes COMPLETED. Otherwise, status becomes NO_BIDDER."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Auction finished successfully",
                    content = @Content(schema = @Schema(implementation = AuctionResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Auction cannot be finished", content = @Content),
            @ApiResponse(responseCode = "404", description = "Auction not found", content = @Content)
    })
    @PatchMapping("/{id}/finish")
    public ResponseEntity<AuctionResponse> finishAuction(
            @Parameter(description = "Auction ID", example = "1")
            @PathVariable @Positive Long id
    ) {
        AuctionResponse auctionResponse = auctionService.finishAuction(id);
        return new ResponseEntity<>(auctionResponse, HttpStatus.OK);
    }

    @Operation(
            summary = "Cancel auction",
            description = "Cancels an auction if it is currently DRAFT or ACTIVE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Auction cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Auction cannot be cancelled", content = @Content),
            @ApiResponse(responseCode = "404", description = "Auction not found", content = @Content)
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancelAuction(
            @Parameter(description = "Auction ID", example = "1")
            @PathVariable @Positive Long id
    ) {
        auctionService.cancelAuction(id);
        return new ResponseEntity<>("Auction successfully cancelled!", HttpStatus.OK);
    }
}