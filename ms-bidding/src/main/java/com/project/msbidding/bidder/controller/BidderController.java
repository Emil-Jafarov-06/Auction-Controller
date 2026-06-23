package com.project.msbidding.bidder.controller;

import com.project.msbidding.bidder.model.dto.BidderRegisterRequest;
import com.project.msbidding.bidder.model.dto.BidderResponse;
import com.project.msbidding.bidder.service.BidderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Bidder", description = "Endpoints for registering bidders and reading bidder information")
public class BidderController {

    private final BidderService bidderService;

    @Operation(
            summary = "Register bidder",
            description = "Registers a new bidder."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Bidder registered successfully",
                    content = @Content(schema = @Schema(implementation = BidderResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid bidder request", content = @Content)
    })
    @PostMapping
    public ResponseEntity<BidderResponse> registerBidder(
            @Valid @RequestBody BidderRegisterRequest bidderRegisterRequest
    ) {
        BidderResponse bidderResponse = bidderService.registerBidder(bidderRegisterRequest);
        return new ResponseEntity<>(bidderResponse, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get bidder by ID",
            description = "Returns bidder information by bidder ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bidder found",
                    content = @Content(schema = @Schema(implementation = BidderResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Bidder not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<BidderResponse> getBidderById(
            @Parameter(description = "Bidder ID", example = "1")
            @PathVariable @Positive Long id
    ) {
        BidderResponse bidderResponse = bidderService.getById(id);
        return new ResponseEntity<>(bidderResponse, HttpStatus.OK);
    }
}