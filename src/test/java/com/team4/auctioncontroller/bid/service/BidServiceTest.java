package com.team4.auctioncontroller.bid.service;

import com.team4.auctioncontroller.auction.model.Auction;
import com.team4.auctioncontroller.auction.repository.AuctionRepository;
import com.team4.auctioncontroller.bid.model.dto.BidResponse;
import com.team4.auctioncontroller.bid.model.dto.PlaceBidRequest;
import com.team4.auctioncontroller.bid.model.entity.Bid;
import com.team4.auctioncontroller.bid.repository.BidRepository;
import com.team4.auctioncontroller.bidder.model.entity.Bidder;
import com.team4.auctioncontroller.bidder.repository.BidderRepository;
import com.team4.auctioncontroller.enums.AuctionStatus;
import com.team4.auctioncontroller.exception.BadRequestException;
import com.team4.auctioncontroller.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private BidderRepository bidderRepository;

    @Mock
    private BidRepository bidRepository;

    @InjectMocks
    private BidService bidService;

    // placeBid
    @Test
    void placeBid_success() {
        Long auctionId = 1L;
        Long bidderId = 10L;

        PlaceBidRequest request = PlaceBidRequest.builder()
                .auctionId(auctionId)
                .bidderId(bidderId)
                .amount(new BigDecimal("150"))
                .build();

        Auction auction = Auction.builder()
                .id(auctionId)
                .startPrice(new BigDecimal("100"))
                .status(AuctionStatus.ACTIVE)
                .deleted(false)
                .build();

        Bidder bidder = Bidder.builder()
                .id(bidderId)
                .fullName("john doe")
                .email("john@gmail.com")
                .pin("1234567")
                .deleted(false)
                .build();

        Bid savedBid = Bid.builder()
                .id(1L)
                .auctionId(auctionId)
                .bidderId(bidderId)
                .amount(new BigDecimal("150"))
                .placedAt(LocalDateTime.now())
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);
        when(bidderRepository.findByIdAndDeletedFalse(bidderId)).thenReturn(bidder);
        when(bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId)).thenReturn(null);
        when(bidRepository.save(any(Bid.class))).thenReturn(savedBid);

        BidResponse response = bidService.placeBid(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(auctionId, response.getAuctionId());
        assertEquals(bidderId, response.getBidderId());
        assertEquals(new BigDecimal("150"), response.getAmount());

        verify(auctionRepository).findById(auctionId);
        verify(bidderRepository).findByIdAndDeletedFalse(bidderId);
        verify(bidRepository).findTopByAuctionIdOrderByAmountDesc(auctionId);
        verify(bidRepository).save(any(Bid.class));
    }

    @Test
    void placeBid_fail_whenAuctionNotFound() {
        Long auctionId = 99L;
        Long bidderId = 10L;

        PlaceBidRequest request = PlaceBidRequest.builder()
                .auctionId(auctionId)
                .bidderId(bidderId)
                .amount(new BigDecimal("150"))
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bidService.placeBid(request)
        );

        assertEquals("Auction not found", exception.getMessage());

        verify(auctionRepository).findById(auctionId);
        verifyNoInteractions(bidderRepository);
        verifyNoInteractions(bidRepository);
    }

    @Test
    void placeBid_fail_whenBidderNotFound() {
        Long auctionId = 1L;
        Long bidderId = 99L;

        PlaceBidRequest request = PlaceBidRequest.builder()
                .auctionId(auctionId)
                .bidderId(bidderId)
                .amount(new BigDecimal("150"))
                .build();

        Auction auction = Auction.builder()
                .id(auctionId)
                .startPrice(new BigDecimal("100"))
                .status(AuctionStatus.ACTIVE)
                .deleted(false)
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);
        when(bidderRepository.findByIdAndDeletedFalse(bidderId)).thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bidService.placeBid(request)
        );

        assertEquals("Bidder not found", exception.getMessage());

        verify(auctionRepository).findById(auctionId);
        verify(bidderRepository).findByIdAndDeletedFalse(bidderId);
        verifyNoInteractions(bidRepository);
    }

    @Test
    void placeBid_fail_whenAuctionStatusIsNotActive() {
        Long auctionId = 1L;
        Long bidderId = 10L;

        PlaceBidRequest request = PlaceBidRequest.builder()
                .auctionId(auctionId)
                .bidderId(bidderId)
                .amount(new BigDecimal("150"))
                .build();

        Auction auction = Auction.builder()
                .id(auctionId)
                .startPrice(new BigDecimal("100"))
                .status(AuctionStatus.DRAFT)
                .deleted(false)
                .build();

        Bidder bidder = Bidder.builder()
                .id(bidderId)
                .deleted(false)
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);
        when(bidderRepository.findByIdAndDeletedFalse(bidderId)).thenReturn(bidder);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bidService.placeBid(request)
        );

        assertEquals("Auction is not active.", exception.getMessage());

        verify(auctionRepository).findById(auctionId);
        verify(bidderRepository).findByIdAndDeletedFalse(bidderId);
        verifyNoInteractions(bidRepository);
    }

    @Test
    void placeBid_fail_whenFirstBidIsNotGreaterThanStartPrice() {
        Long auctionId = 1L;
        Long bidderId = 10L;

        PlaceBidRequest request = PlaceBidRequest.builder()
                .auctionId(auctionId)
                .bidderId(bidderId)
                .amount(new BigDecimal("100"))
                .build();

        Auction auction = Auction.builder()
                .id(auctionId)
                .startPrice(new BigDecimal("100"))
                .status(AuctionStatus.ACTIVE)
                .deleted(false)
                .build();

        Bidder bidder = Bidder.builder()
                .id(bidderId)
                .deleted(false)
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);
        when(bidderRepository.findByIdAndDeletedFalse(bidderId)).thenReturn(bidder);
        when(bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId)).thenReturn(null);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bidService.placeBid(request)
        );

        assertEquals("First bid amount must be greater than start price!", exception.getMessage());

        verify(auctionRepository).findById(auctionId);
        verify(bidderRepository).findByIdAndDeletedFalse(bidderId);
        verify(bidRepository).findTopByAuctionIdOrderByAmountDesc(auctionId);
        verify(bidRepository, never()).save(any(Bid.class));
    }

    @Test
    void placeBid_fail_whenBidIsNotGreaterThanHighestBid() {
        Long auctionId = 1L;
        Long bidderId = 10L;

        PlaceBidRequest request = PlaceBidRequest.builder()
                .auctionId(auctionId)
                .bidderId(bidderId)
                .amount(new BigDecimal("200"))
                .build();

        Auction auction = Auction.builder()
                .id(auctionId)
                .startPrice(new BigDecimal("100"))
                .status(AuctionStatus.ACTIVE)
                .deleted(false)
                .build();

        Bidder bidder = Bidder.builder()
                .id(bidderId)
                .deleted(false)
                .build();

        Bid highestBid = Bid.builder()
                .id(1L)
                .auctionId(auctionId)
                .bidderId(20L)
                .amount(new BigDecimal("200"))
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);
        when(bidderRepository.findByIdAndDeletedFalse(bidderId)).thenReturn(bidder);
        when(bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId)).thenReturn(highestBid);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bidService.placeBid(request)
        );

        assertEquals("A new bid amount must be greater than highest bid!", exception.getMessage());

        verify(auctionRepository).findById(auctionId);
        verify(bidderRepository).findByIdAndDeletedFalse(bidderId);
        verify(bidRepository).findTopByAuctionIdOrderByAmountDesc(auctionId);
        verify(bidRepository, never()).save(any(Bid.class));
    }

    @Test
    void placeBid_fail_whenSameBidderPlacesTwoConsecutiveBids() {
        Long auctionId = 1L;
        Long bidderId = 10L;

        PlaceBidRequest request = PlaceBidRequest.builder()
                .auctionId(auctionId)
                .bidderId(bidderId)
                .amount(new BigDecimal("250"))
                .build();

        Auction auction = Auction.builder()
                .id(auctionId)
                .startPrice(new BigDecimal("100"))
                .status(AuctionStatus.ACTIVE)
                .deleted(false)
                .build();

        Bidder bidder = Bidder.builder()
                .id(bidderId)
                .deleted(false)
                .build();

        Bid highestBid = Bid.builder()
                .id(1L)
                .auctionId(auctionId)
                .bidderId(bidderId)
                .amount(new BigDecimal("200"))
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);
        when(bidderRepository.findByIdAndDeletedFalse(bidderId)).thenReturn(bidder);
        when(bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId)).thenReturn(highestBid);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bidService.placeBid(request)
        );

        assertEquals("A bidder cannot place two consecutive bids on the same auction.", exception.getMessage());

        verify(auctionRepository).findById(auctionId);
        verify(bidderRepository).findByIdAndDeletedFalse(bidderId);
        verify(bidRepository).findTopByAuctionIdOrderByAmountDesc(auctionId);
        verify(bidRepository, never()).save(any(Bid.class));
    }

    // getAllBidsForAuction
    @Test
    void getAllBidsForAuction_success() {
        Long auctionId = 1L;

        LocalDateTime placedAt = LocalDateTime.now();

        Bid bid1 = Bid.builder()
                .id(1L)
                .auctionId(auctionId)
                .bidderId(10L)
                .amount(new BigDecimal("300"))
                .placedAt(placedAt)
                .build();

        Bid bid2 = Bid.builder()
                .id(2L)
                .auctionId(auctionId)
                .bidderId(20L)
                .amount(new BigDecimal("200"))
                .placedAt(placedAt)
                .build();

        when(bidRepository.findBidsByAuctionIdOrderByAmountDesc(auctionId))
                .thenReturn(List.of(bid1, bid2));

        List<BidResponse> responses = bidService.getAllBidsForAuction(auctionId);

        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals(1L, responses.get(0).getId());
        assertEquals(auctionId, responses.get(0).getAuctionId());
        assertEquals(10L, responses.get(0).getBidderId());
        assertEquals(new BigDecimal("300"), responses.get(0).getAmount());

        assertEquals(2L, responses.get(1).getId());
        assertEquals(auctionId, responses.get(1).getAuctionId());
        assertEquals(20L, responses.get(1).getBidderId());
        assertEquals(new BigDecimal("200"), responses.get(1).getAmount());

        verify(bidRepository).findBidsByAuctionIdOrderByAmountDesc(auctionId);
    }

    // getHighestBidForAuction
    @Test
    void getHighestBidForAuction_success() {
        Long auctionId = 1L;

        LocalDateTime placedAt = LocalDateTime.now();

        Bid highestBid = Bid.builder()
                .id(1L)
                .auctionId(auctionId)
                .bidderId(10L)
                .amount(new BigDecimal("300"))
                .placedAt(placedAt)
                .build();

        when(bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId))
                .thenReturn(highestBid);

        BidResponse response = bidService.getHighestBidForAuction(auctionId);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(auctionId, response.getAuctionId());
        assertEquals(10L, response.getBidderId());
        assertEquals(new BigDecimal("300"), response.getAmount());
        assertEquals(placedAt, response.getPlacedAt());

        verify(bidRepository).findTopByAuctionIdOrderByAmountDesc(auctionId);
    }

    @Test
    void getHighestBidForAuction_fail_whenNoBidFound() {
        Long auctionId = 1L;

        when(bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId))
                .thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bidService.getHighestBidForAuction(auctionId)
        );

        assertEquals("No bids were placed for this auction!", exception.getMessage());

        verify(bidRepository).findTopByAuctionIdOrderByAmountDesc(auctionId);
    }
}