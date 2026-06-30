package com.project.msbidding.bid.service;

import com.project.msbidding.bid.model.dto.BidInfoResponse;
import com.project.msbidding.bid.model.dto.BidResponse;
import com.project.msbidding.bid.model.dto.PlaceBidRequest;
import com.project.msbidding.bid.model.entity.Bid;
import com.project.msbidding.bid.repository.AuctionBidStateRepository;
import com.project.msbidding.bid.repository.BidRepository;
import com.project.msbidding.client.AuctionClientForBidding;
import com.project.msbidding.client.dto.AuctionInfoResponse;
import com.project.msbidding.exception.BadRequestException;
import com.project.msbidding.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    private AuctionClientForBidding auctionClient;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private AuctionBidStateRepository auctionBidStateRepository;

    @InjectMocks
    private BidService bidService;

    @Test
    void placeBid_whenBidIsValid_savesBidAndReturnsResponse() {
        PlaceBidRequest request = placeBidRequest(1L, BigDecimal.valueOf(150));
        AuctionInfoResponse auction = auctionInfo(1L, BigDecimal.valueOf(100), "ACTIVE");
        Long userId = 5L;
        Bid savedBid = bid(10L, 1L, 5L, BigDecimal.valueOf(150));

        when(auctionClient.getAuctionInfoResponse(1L)).thenReturn(auction);
        when(auctionBidStateRepository.tryAcceptBid(1L, 5L, BigDecimal.valueOf(150))).thenReturn(1);
        when(bidRepository.save(any(Bid.class))).thenReturn(savedBid);

        BidResponse response = bidService.placeBid(request, 5L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getAuctionId());
        assertEquals(5L, response.getUserId());
        assertEquals(BigDecimal.valueOf(150), response.getAmount());

        ArgumentCaptor<Bid> bidCaptor = ArgumentCaptor.forClass(Bid.class);
        verify(bidRepository).save(bidCaptor.capture());

        Bid bidToSave = bidCaptor.getValue();
        assertEquals(1L, bidToSave.getAuctionId());
        assertEquals(5L, bidToSave.getUserId());
        assertEquals(BigDecimal.valueOf(150), bidToSave.getAmount());

        verify(auctionBidStateRepository).tryAcceptBid(1L, 5L, BigDecimal.valueOf(150));
    }

    @Test
    void placeBid_whenAuctionIsNotActive_throwsBadRequestException() {
        PlaceBidRequest request = placeBidRequest(1L, BigDecimal.valueOf(150));
        AuctionInfoResponse auction = auctionInfo(1L, BigDecimal.valueOf(100), "DRAFT");
        Long userId = 5L;

        when(auctionClient.getAuctionInfoResponse(1L)).thenReturn(auction);

        assertThrows(BadRequestException.class, () -> bidService.placeBid(request, 5L));

        verify(auctionBidStateRepository, never()).tryAcceptBid(anyLong(), anyLong(), any());
        verify(bidRepository, never()).save(any(Bid.class));
    }

    @Test
    void placeBid_whenAmountIsNotGreaterThanStartPrice_throwsBadRequestException() {
        PlaceBidRequest request = placeBidRequest(1L, BigDecimal.valueOf(100));
        AuctionInfoResponse auction = auctionInfo(1L, BigDecimal.valueOf(100), "ACTIVE");
        Long userId = 5L;

        when(auctionClient.getAuctionInfoResponse(1L)).thenReturn(auction);

        assertThrows(BadRequestException.class, () -> bidService.placeBid(request, 5L));

        verify(auctionBidStateRepository, never()).tryAcceptBid(anyLong(), anyLong(), any());
        verify(bidRepository, never()).save(any(Bid.class));
    }

    @Test
    void placeBid_whenAuctionBidStateRejectsBid_throwsBadRequestException() {
        PlaceBidRequest request = placeBidRequest(1L, BigDecimal.valueOf(150));
        AuctionInfoResponse auction = auctionInfo(1L, BigDecimal.valueOf(100), "ACTIVE");
        Long userId = 5L;

        when(auctionClient.getAuctionInfoResponse(1L)).thenReturn(auction);
        when(auctionBidStateRepository.tryAcceptBid(1L, 5L, BigDecimal.valueOf(150))).thenReturn(0);

        assertThrows(BadRequestException.class, () -> bidService.placeBid(request, 5L));

        verify(auctionBidStateRepository).tryAcceptBid(1L, 5L, BigDecimal.valueOf(150));
        verify(bidRepository, never()).save(any(Bid.class));
    }

    @Test
    void getAllBidsForAuction_whenBidsExist_returnsResponses() {
        List<Bid> bids = List.of(
                bid(1L, 10L, 5L, BigDecimal.valueOf(200)),
                bid(2L, 10L, 6L, BigDecimal.valueOf(250))
        );

        when(bidRepository.findBidsByAuctionIdOrderByPlacedAtDesc(10L)).thenReturn(bids);

        List<BidResponse> responses = bidService.getAllBidsForAuction(10L);

        assertEquals(2, responses.size());
        assertEquals(10L, responses.get(0).getAuctionId());
        assertEquals(10L, responses.get(1).getAuctionId());
    }

    @Test
    void getAllBidsForAuction_whenNoBids_returnsEmptyList() {
        when(bidRepository.findBidsByAuctionIdOrderByPlacedAtDesc(10L)).thenReturn(List.of());

        List<BidResponse> responses = bidService.getAllBidsForAuction(10L);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getHighestBidForAuction_whenBidExists_returnsResponse() {
        Bid highestBid = bid(1L, 10L, 5L, BigDecimal.valueOf(300));

        when(bidRepository.findTopByAuctionIdOrderByPlacedAtDesc(10L)).thenReturn(highestBid);

        BidResponse response = bidService.getHighestBidForAuction(10L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(10L, response.getAuctionId());
        assertEquals(5L, response.getUserId());
        assertEquals(BigDecimal.valueOf(300), response.getAmount());
    }

    @Test
    void getHighestBidForAuction_whenNoBidExists_throwsNotFoundException() {
        when(bidRepository.findTopByAuctionIdOrderByPlacedAtDesc(10L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> bidService.getHighestBidForAuction(10L));
    }

    @Test
    void getBiddingInfo_whenBidExists_returnsHasBidTrue() {
        Bid bid = bid(1L, 10L, 5L, BigDecimal.valueOf(300));

        when(bidRepository.findTopByAuctionIdOrderByPlacedAtDesc(10L)).thenReturn(bid);

        BidInfoResponse response = bidService.getBiddingInfo(10L);

        assertNotNull(response);
        assertEquals(10L, response.auctionId());
        assertTrue(response.hasBid());
        assertEquals(5L, response.bidderId());
    }

    @Test
    void getBiddingInfo_whenNoBidExists_returnsHasBidFalse() {
        when(bidRepository.findTopByAuctionIdOrderByPlacedAtDesc(10L)).thenReturn(null);

        BidInfoResponse response = bidService.getBiddingInfo(10L);

        assertNotNull(response);
        assertEquals(10L, response.auctionId());
        assertFalse(response.hasBid());
        assertNull(response.bidderId());
    }

    @Test
    void getBiddingInformationForAuctions_whenInputIsNull_returnsEmptyList() {
        List<BidInfoResponse> responses = bidService.getBiddingInformationForAuctions(null);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(bidRepository, never()).findHighestBidsForAuctions(anyList());
    }

    @Test
    void getBiddingInformationForAuctions_whenInputIsEmpty_returnsEmptyList() {
        List<BidInfoResponse> responses = bidService.getBiddingInformationForAuctions(List.of());

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(bidRepository, never()).findHighestBidsForAuctions(anyList());
    }

    @Test
    void getBiddingInformationForAuctions_returnsResponseForEveryRequestedAuction() {
        List<Long> auctionIds = List.of(1L, 2L, 3L);

        List<Bid> highestBids = List.of(
                bid(100L, 1L, 10L, BigDecimal.valueOf(500)),
                bid(101L, 3L, 30L, BigDecimal.valueOf(700))
        );

        when(bidRepository.findHighestBidsForAuctions(auctionIds)).thenReturn(highestBids);

        List<BidInfoResponse> responses = bidService.getBiddingInformationForAuctions(auctionIds);

        assertEquals(3, responses.size());

        assertEquals(1L, responses.get(0).auctionId());
        assertTrue(responses.get(0).hasBid());
        assertEquals(10L, responses.get(0).bidderId());

        assertEquals(2L, responses.get(1).auctionId());
        assertFalse(responses.get(1).hasBid());
        assertNull(responses.get(1).bidderId());

        assertEquals(3L, responses.get(2).auctionId());
        assertTrue(responses.get(2).hasBid());
        assertEquals(30L, responses.get(2).bidderId());
    }

    @Test
    void toResponse_success() {
        Bid bid = bid(1L, 10L, 5L, BigDecimal.valueOf(300));

        BidResponse response = bidService.toResponse(bid);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(10L, response.getAuctionId());
        assertEquals(5L, response.getUserId());
        assertEquals(BigDecimal.valueOf(300), response.getAmount());
        assertEquals(bid.getPlacedAt(), response.getPlacedAt());
    }

    private PlaceBidRequest placeBidRequest(Long auctionId, BigDecimal amount) {
        return PlaceBidRequest.builder()
                .auctionId(auctionId)
                .amount(amount)
                .build();
    }

    private AuctionInfoResponse auctionInfo(Long id, BigDecimal startPrice, String status) {
        return new AuctionInfoResponse(id, startPrice, status);
    }

    private Bid bid(Long id, Long auctionId, Long bidderId, BigDecimal amount) {
        return Bid.builder()
                .id(id)
                .auctionId(auctionId)
                .userId(bidderId)
                .amount(amount)
                .placedAt(LocalDateTime.now())
                .build();
    }
}