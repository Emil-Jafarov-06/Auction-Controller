package com.project.msauction.auction.service;

import com.project.msauction.auction.mapper.AuctionMapper;
import com.project.msauction.auction.model.Auction;
import com.project.msauction.auction.model.dto.*;
import com.project.msauction.client.BiddingClientForAuction;
import com.project.msauction.client.dto.BidInfoResponse;
import com.project.msauction.enums.AuctionStatus;
import com.project.msauction.exception.BadRequestException;
import com.project.msauction.exception.NotFoundException;
import com.project.msauction.exception.NotSavedException;
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
class AuctionServiceTest {

    @Mock
    private AuctionMapper auctionMapper;

    @Mock
    private BiddingClientForAuction biddingClient;

    @InjectMocks
    private AuctionService auctionService;

    @Test
    void createAuction_success() {
        AuctionCreateRequest request = validCreateRequest();

        Auction savedAuction = auction(1L, AuctionStatus.DRAFT);
        when(auctionMapper.insertAuction(any(Auction.class))).thenReturn(savedAuction);

        AuctionResponse response = auctionService.createAuction(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(AuctionStatus.DRAFT, response.getStatus());

        ArgumentCaptor<Auction> auctionCaptor = ArgumentCaptor.forClass(Auction.class);
        verify(auctionMapper).insertAuction(auctionCaptor.capture());

        Auction insertedAuction = auctionCaptor.getValue();
        assertEquals(request.getTitle(), insertedAuction.getTitle());
        assertEquals(request.getDescription(), insertedAuction.getDescription());
        assertEquals(request.getStartPrice(), insertedAuction.getStartPrice());
        assertEquals(AuctionStatus.DRAFT, insertedAuction.getStatus());
        assertFalse(insertedAuction.getDeleted());
    }

    @Test
    void createAuction_whenStartTimeIsPast_throwsBadRequestException() {
        AuctionCreateRequest request = AuctionCreateRequest.builder()
                .title("Test auction")
                .description("Description")
                .startPrice(BigDecimal.valueOf(100))
                .startAt(LocalDateTime.now().minusMinutes(5))
                .endAt(LocalDateTime.now().plusHours(1))
                .build();

        assertThrows(BadRequestException.class, () -> auctionService.createAuction(request));

        verify(auctionMapper, never()).insertAuction(any(Auction.class));
    }

    @Test
    void createAuction_whenStartTimeIsAfterEndTime_throwsBadRequestException() {
        AuctionCreateRequest request = AuctionCreateRequest.builder()
                .title("Test auction")
                .description("Description")
                .startPrice(BigDecimal.valueOf(100))
                .startAt(LocalDateTime.now().plusHours(2))
                .endAt(LocalDateTime.now().plusHours(1))
                .build();

        assertThrows(BadRequestException.class, () -> auctionService.createAuction(request));

        verify(auctionMapper, never()).insertAuction(any(Auction.class));
    }

    @Test
    void createAuction_whenMapperReturnsNull_throwsNotSavedException() {
        AuctionCreateRequest request = validCreateRequest();

        when(auctionMapper.insertAuction(any(Auction.class))).thenReturn(null);

        assertThrows(NotSavedException.class, () -> auctionService.createAuction(request));
    }

    @Test
    void getById_success() {
        Auction auction = auction(1L, AuctionStatus.DRAFT);
        when(auctionMapper.findById(1L)).thenReturn(auction);

        AuctionResponse response = auctionService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(AuctionStatus.DRAFT, response.getStatus());

        verify(auctionMapper).findById(1L);
    }

    @Test
    void getById_whenAuctionDoesNotExist_throwsNotFoundException() {
        when(auctionMapper.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> auctionService.getById(1L));

        verify(auctionMapper).findById(1L);
    }

    @Test
    void getAll_success() {
        AuctionFilter filter = new AuctionFilter();
        filter.setStatus(AuctionStatus.ACTIVE);

        List<Auction> auctions = List.of(
                auction(1L, AuctionStatus.ACTIVE),
                auction(2L, AuctionStatus.ACTIVE)
        );

        when(auctionMapper.findFiltered(filter, 10, 0)).thenReturn(auctions);
        when(auctionMapper.countFiltered(filter)).thenReturn(2L);

        PageResponse<AuctionResponse> response = auctionService.getAll(filter, 10, 1);

        assertNotNull(response);

        verify(auctionMapper).findFiltered(filter, 10, 0);
        verify(auctionMapper).countFiltered(filter);
    }

    @Test
    void getAll_whenPageSizeIsInvalid_throwsBadRequestException() {
        AuctionFilter filter = new AuctionFilter();

        assertThrows(BadRequestException.class, () -> auctionService.getAll(filter, 0, 1));

        verify(auctionMapper, never()).findFiltered(any(), anyInt(), anyInt());
        verify(auctionMapper, never()).countFiltered(any());
    }

    @Test
    void getAll_whenPageNumberIsInvalid_throwsBadRequestException() {
        AuctionFilter filter = new AuctionFilter();

        assertThrows(BadRequestException.class, () -> auctionService.getAll(filter, 10, 0));

        verify(auctionMapper, never()).findFiltered(any(), anyInt(), anyInt());
        verify(auctionMapper, never()).countFiltered(any());
    }

    @Test
    void activateAuction_success() {
        Auction auction = auction(1L, AuctionStatus.DRAFT);
        auction.setEndAt(LocalDateTime.now().plusHours(2));

        when(auctionMapper.findById(1L)).thenReturn(auction);
        when(auctionMapper.updateStatus(1L, AuctionStatus.ACTIVE)).thenReturn(1);

        auctionService.activateAuction(1L);

        verify(auctionMapper).findById(1L);
        verify(auctionMapper).updateStatus(1L, AuctionStatus.ACTIVE);
    }

    @Test
    void activateAuction_whenAuctionNotFound_throwsNotFoundException() {
        when(auctionMapper.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> auctionService.activateAuction(1L));

        verify(auctionMapper, never()).updateStatus(anyLong(), any());
    }

    @Test
    void activateAuction_whenAuctionIsNotDraft_throwsBadRequestException() {
        Auction auction = auction(1L, AuctionStatus.ACTIVE);
        when(auctionMapper.findById(1L)).thenReturn(auction);

        assertThrows(BadRequestException.class, () -> auctionService.activateAuction(1L));

        verify(auctionMapper, never()).updateStatus(anyLong(), any());
    }

    @Test
    void activateAuction_whenEndTimeIsPast_throwsBadRequestException() {
        Auction auction = auction(1L, AuctionStatus.DRAFT);
        auction.setEndAt(LocalDateTime.now().minusMinutes(1));

        when(auctionMapper.findById(1L)).thenReturn(auction);

        assertThrows(BadRequestException.class, () -> auctionService.activateAuction(1L));

        verify(auctionMapper, never()).updateStatus(anyLong(), any());
    }

    @Test
    void activateAuction_whenUpdateFails_throwsBadRequestException() {
        Auction auction = auction(1L, AuctionStatus.DRAFT);
        auction.setEndAt(LocalDateTime.now().plusHours(2));

        when(auctionMapper.findById(1L)).thenReturn(auction);
        when(auctionMapper.updateStatus(1L, AuctionStatus.ACTIVE)).thenReturn(0);

        assertThrows(BadRequestException.class, () -> auctionService.activateAuction(1L));
    }

    @Test
    void finishAuction_whenNoBid_updatesStatusToNoBidder() {
        Auction auction = auction(1L, AuctionStatus.ACTIVE);

        when(auctionMapper.findById(1L)).thenReturn(auction);
        when(biddingClient.findHighestBidInfo(1L))
                .thenReturn(new BidInfoResponse(1L, false, null));
        when(auctionMapper.updateStatus(1L, AuctionStatus.NO_BIDDER)).thenReturn(1);

        auctionService.finishAuction(1L);

        verify(auctionMapper).updateStatus(1L, AuctionStatus.NO_BIDDER);
        verify(auctionMapper, never()).updateWinner(anyLong(), anyLong());
    }

    @Test
    void finishAuction_whenHasBid_updatesCompletedAndWinner() {
        Auction auction = auction(1L, AuctionStatus.ACTIVE);

        when(auctionMapper.findById(1L)).thenReturn(auction);
        when(biddingClient.findHighestBidInfo(1L))
                .thenReturn(new BidInfoResponse(1L, true, 5L));
        when(auctionMapper.updateStatus(1L, AuctionStatus.COMPLETED)).thenReturn(1);
        when(auctionMapper.updateWinner(1L, 5L)).thenReturn(1);

        auctionService.finishAuction(1L);

        verify(auctionMapper).updateStatus(1L, AuctionStatus.COMPLETED);
        verify(auctionMapper).updateWinner(1L, 5L);
    }

    @Test
    void finishAuction_whenAuctionNotFound_throwsNotFoundException() {
        when(auctionMapper.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> auctionService.finishAuction(1L));

        verify(biddingClient, never()).findHighestBidInfo(anyLong());
    }

    @Test
    void finishAuction_whenAuctionIsNotActive_throwsBadRequestException() {
        Auction auction = auction(1L, AuctionStatus.DRAFT);
        when(auctionMapper.findById(1L)).thenReturn(auction);

        assertThrows(BadRequestException.class, () -> auctionService.finishAuction(1L));

        verify(biddingClient, never()).findHighestBidInfo(anyLong());
    }

    @Test
    void finishAuction_whenNoBidStatusUpdateFails_throwsBadRequestException() {
        Auction auction = auction(1L, AuctionStatus.ACTIVE);

        when(auctionMapper.findById(1L)).thenReturn(auction);
        when(biddingClient.findHighestBidInfo(1L))
                .thenReturn(new BidInfoResponse(1L, false, null));
        when(auctionMapper.updateStatus(1L, AuctionStatus.NO_BIDDER)).thenReturn(0);

        assertThrows(BadRequestException.class, () -> auctionService.finishAuction(1L));
    }

    @Test
    void finishAuction_whenWinnerUpdateFails_throwsBadRequestException() {
        Auction auction = auction(1L, AuctionStatus.ACTIVE);

        when(auctionMapper.findById(1L)).thenReturn(auction);
        when(biddingClient.findHighestBidInfo(1L))
                .thenReturn(new BidInfoResponse(1L, true, 5L));
        when(auctionMapper.updateStatus(1L, AuctionStatus.COMPLETED)).thenReturn(1);
        when(auctionMapper.updateWinner(1L, 5L)).thenReturn(0);

        assertThrows(BadRequestException.class, () -> auctionService.finishAuction(1L));
    }

    @Test
    void cancelAuction_whenDraft_success() {
        Auction auction = auction(1L, AuctionStatus.DRAFT);

        when(auctionMapper.findById(1L)).thenReturn(auction);
        when(auctionMapper.updateStatus(1L, AuctionStatus.CANCELLED)).thenReturn(1);

        auctionService.cancelAuction(1L);

        verify(auctionMapper).updateStatus(1L, AuctionStatus.CANCELLED);
    }

    @Test
    void cancelAuction_whenActive_success() {
        Auction auction = auction(1L, AuctionStatus.ACTIVE);

        when(auctionMapper.findById(1L)).thenReturn(auction);
        when(auctionMapper.updateStatus(1L, AuctionStatus.CANCELLED)).thenReturn(1);

        auctionService.cancelAuction(1L);

        verify(auctionMapper).updateStatus(1L, AuctionStatus.CANCELLED);
    }

    @Test
    void cancelAuction_whenAuctionNotFound_throwsNotFoundException() {
        when(auctionMapper.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> auctionService.cancelAuction(1L));

        verify(auctionMapper, never()).updateStatus(anyLong(), any());
    }

    @Test
    void cancelAuction_whenStatusIsInvalid_throwsBadRequestException() {
        Auction auction = auction(1L, AuctionStatus.COMPLETED);
        when(auctionMapper.findById(1L)).thenReturn(auction);

        assertThrows(BadRequestException.class, () -> auctionService.cancelAuction(1L));

        verify(auctionMapper, never()).updateStatus(anyLong(), any());
    }

    @Test
    void cancelAuction_whenUpdateFails_throwsBadRequestException() {
        Auction auction = auction(1L, AuctionStatus.ACTIVE);

        when(auctionMapper.findById(1L)).thenReturn(auction);
        when(auctionMapper.updateStatus(1L, AuctionStatus.CANCELLED)).thenReturn(0);

        assertThrows(BadRequestException.class, () -> auctionService.cancelAuction(1L));
    }

    @Test
    void getInfoResponse_success() {
        Auction auction = auction(1L, AuctionStatus.ACTIVE);
        when(auctionMapper.findById(1L)).thenReturn(auction);

        AuctionInfoResponse response = auctionService.getInfoResponse(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(auction.getStartPrice(), response.startPrice());
        assertEquals("ACTIVE", response.status());
    }

    @Test
    void getInfoResponse_whenAuctionNotFound_throwsNotFoundException() {
        when(auctionMapper.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> auctionService.getInfoResponse(1L));
    }

    @Test
    void finishExpiredAuctions_whenNoExpiredAuctions_returnsZero() {
        when(auctionMapper.findExpiredActiveAuctions()).thenReturn(List.of());

        int result = auctionService.finishExpiredAuctions();

        assertEquals(0, result);
        verify(biddingClient, never()).getBiddingForGivenIds(anyList());
        verify(auctionMapper, never()).finishExpiredAuctions(anyList());
    }

    @Test
    void finishExpiredAuctions_success() {
        List<Long> expiredIds = List.of(1L, 2L);

        List<BidInfoResponse> bidResponses = List.of(
                new BidInfoResponse(1L, true, 10L),
                new BidInfoResponse(2L, false, null)
        );

        when(auctionMapper.findExpiredActiveAuctions()).thenReturn(expiredIds);
        when(biddingClient.getBiddingForGivenIds(expiredIds)).thenReturn(bidResponses);
        when(auctionMapper.finishExpiredAuctions(anyList())).thenReturn(2);

        int result = auctionService.finishExpiredAuctions();

        assertEquals(2, result);

        ArgumentCaptor<List<ExpiredAuctionsUpdate>> captor = ArgumentCaptor.forClass(List.class);
        verify(auctionMapper).finishExpiredAuctions(captor.capture());

        List<ExpiredAuctionsUpdate> updates = captor.getValue();

        assertEquals(2, updates.size());

        assertEquals(1L, updates.get(0).id());
        assertEquals(AuctionStatus.COMPLETED, updates.get(0).status());
        assertEquals(10L, updates.get(0).bidderId());

        assertEquals(2L, updates.get(1).id());
        assertEquals(AuctionStatus.NO_BIDDER, updates.get(1).status());
        assertNull(updates.get(1).bidderId());
    }

    @Test
    void finishExpiredAuctions_whenBiddingReturnsEmptyList_returnsZero() {
        List<Long> expiredIds = List.of(1L, 2L);

        when(auctionMapper.findExpiredActiveAuctions()).thenReturn(expiredIds);
        when(biddingClient.getBiddingForGivenIds(expiredIds)).thenReturn(List.of());

        int result = auctionService.finishExpiredAuctions();

        assertEquals(0, result);
        verify(auctionMapper, never()).finishExpiredAuctions(anyList());
    }

    @Test
    void startAuctions_success() {
        when(auctionMapper.startAuctions()).thenReturn(3);

        int result = auctionService.startAuctions();

        assertEquals(3, result);
        verify(auctionMapper).startAuctions();
    }

    private AuctionCreateRequest validCreateRequest() {
        return AuctionCreateRequest.builder()
                .title("Test auction")
                .description("Test description")
                .startPrice(BigDecimal.valueOf(100))
                .startAt(LocalDateTime.now().plusHours(1))
                .endAt(LocalDateTime.now().plusHours(3))
                .build();
    }

    private Auction auction(Long id, AuctionStatus status) {
        return Auction.builder()
                .id(id)
                .title("Auction " + id)
                .description("Description " + id)
                .startPrice(BigDecimal.valueOf(100))
                .startAt(LocalDateTime.now().plusHours(1))
                .endAt(LocalDateTime.now().plusHours(3))
                .status(status)
                .winnerId(null)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .deleted(false)
                .build();
    }
}