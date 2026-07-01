package com.project.msbidding.bid.service;

import com.project.msbidding.bid.model.dto.BidInfoResponse;
import com.project.msbidding.bid.model.dto.BidResponse;
import com.project.msbidding.bid.model.dto.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    void getPaginatedBidsForAuction_whenBidsExist_returnsPageResponse() {
        List<Bid> bids = List.of(
                bid(1L, 10L, 5L, BigDecimal.valueOf(200)),
                bid(2L, 10L, 6L, BigDecimal.valueOf(250))
        );

        Page<Bid> bidPage = new PageImpl<>(bids);

        when(bidRepository.findBidsByAuctionIdOrderByPlacedAtDesc(eq(10L), any(Pageable.class)))
                .thenReturn(bidPage);

        PageResponse<BidResponse> response = bidService.getPaginatedBidsForAuction(10L, 2, 1);

        assertNotNull(response);
        assertEquals(2, response.content().size());

        assertEquals(1L, response.content().get(0).getId());
        assertEquals(10L, response.content().get(0).getAuctionId());
        assertEquals(5L, response.content().get(0).getUserId());
        assertEquals(BigDecimal.valueOf(200), response.content().get(0).getAmount());

        assertEquals(2L, response.content().get(1).getId());
        assertEquals(10L, response.content().get(1).getAuctionId());
        assertEquals(6L, response.content().get(1).getUserId());
        assertEquals(BigDecimal.valueOf(250), response.content().get(1).getAmount());

        assertEquals(1, response.pageNumber());
        assertEquals(2, response.pageSize());
        assertEquals(2, response.totalCount());
        assertEquals(1, response.totalPages());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(bidRepository).findBidsByAuctionIdOrderByPlacedAtDesc(
                eq(10L),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(2, pageable.getPageSize());
        assertTrue(pageable.getSort().getOrderFor("placedAt").isDescending());
    }

    @Test
    void getPaginatedBidsForAuction_whenNoBids_returnsEmptyPageResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Bid> bidPage = new PageImpl<>(List.of(), pageable, 0);

        when(bidRepository.findBidsByAuctionIdOrderByPlacedAtDesc(eq(10L), any(Pageable.class)))
                .thenReturn(bidPage);

        PageResponse<BidResponse> response = bidService.getPaginatedBidsForAuction(10L, 10, 1);

        assertNotNull(response);
        assertTrue(response.content().isEmpty());

        assertEquals(1, response.pageNumber());
        assertEquals(10, response.pageSize());
        assertEquals(0, response.totalCount());
        assertEquals(0, response.totalPages());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(bidRepository).findBidsByAuctionIdOrderByPlacedAtDesc(
                eq(10L),
                pageableCaptor.capture()
        );

        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(10, capturedPageable.getPageSize());
    }

    @Test
    void getPaginatedBidsForAuction_whenPageSizeIsZero_throwsBadRequestException() {
        assertThrows(
                BadRequestException.class,
                () -> bidService.getPaginatedBidsForAuction(10L, 0, 1)
        );

        verify(bidRepository, never())
                .findBidsByAuctionIdOrderByPlacedAtDesc(anyLong(), any(Pageable.class));
    }

    @Test
    void getPaginatedBidsForAuction_whenPageSizeIsNegative_throwsBadRequestException() {
        assertThrows(
                BadRequestException.class,
                () -> bidService.getPaginatedBidsForAuction(10L, -5, 1)
        );

        verify(bidRepository, never())
                .findBidsByAuctionIdOrderByPlacedAtDesc(anyLong(), any(Pageable.class));
    }

    @Test
    void getPaginatedBidsForAuction_whenPageNumberIsZero_throwsBadRequestException() {
        assertThrows(
                BadRequestException.class,
                () -> bidService.getPaginatedBidsForAuction(10L, 10, 0)
        );

        verify(bidRepository, never())
                .findBidsByAuctionIdOrderByPlacedAtDesc(anyLong(), any(Pageable.class));
    }

    @Test
    void getPaginatedBidsForAuction_whenPageNumberIsNegative_throwsBadRequestException() {
        assertThrows(
                BadRequestException.class,
                () -> bidService.getPaginatedBidsForAuction(10L, 10, -1)
        );

        verify(bidRepository, never())
                .findBidsByAuctionIdOrderByPlacedAtDesc(anyLong(), any(Pageable.class));
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