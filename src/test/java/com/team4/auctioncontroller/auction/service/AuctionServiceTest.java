package com.team4.auctioncontroller.auction.service;

import com.team4.auctioncontroller.auction.model.Auction;
import com.team4.auctioncontroller.auction.model.dto.AuctionCreateRequest;
import com.team4.auctioncontroller.auction.model.dto.AuctionResponse;
import com.team4.auctioncontroller.auction.repository.AuctionRepository;
import com.team4.auctioncontroller.bid.model.entity.Bid;
import com.team4.auctioncontroller.bid.repository.BidRepository;
import com.team4.auctioncontroller.bidder.model.dto.BidderResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @InjectMocks
    private AuctionService auctionService;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private BidderRepository bidderRepository;

    // createAuction
    @Test
    void createAuction_success() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.plusDays(1);
        LocalDateTime end = now.plusDays(2);
        LocalDateTime createdAt = now.plusDays(1);
        LocalDateTime modifiedAt = now.plusDays(1);

        AuctionCreateRequest request = AuctionCreateRequest.builder()
                .title("auction")
                .description("just auction")
                .startPrice(new BigDecimal("100"))
                .startAt(start)
                .endAt(end).build();

        Auction savedAuction = Auction.builder()
                .id(1L)
                .title("auction")
                .description("just auction")
                .startPrice(new BigDecimal(100))
                .startAt(start)
                .endAt(end)
                .status(AuctionStatus.DRAFT)
                .winnerId(null)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .deleted(false).build();

        when(auctionRepository.save(any(Auction.class))).thenReturn(savedAuction);

        AuctionResponse response = auctionService.createAuction(request);

        assertNotNull(response);
        assertEquals(response, auctionService.toResponse(savedAuction));

        verify(auctionRepository).save(any(Auction.class));
    }

    @Test
    public void createAuction_fail_whenStartTimeIsBeforeCurrentTime(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(1);
        LocalDateTime end = now.plusDays(2);

        AuctionCreateRequest request = AuctionCreateRequest.builder()
                .title("auction")
                .description("just auction")
                .startPrice(new BigDecimal("100"))
                .startAt(start)
                .endAt(end).build();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> auctionService.createAuction(request));
        assertEquals("Start and end times must be configured correctly!", exception.getMessage());

        verify(auctionRepository, never()).save(any(Auction.class));
    }

    @Test
    public void createAuction_fail_whenStartTimeIsAfterEndTime(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.plusDays(2);
        LocalDateTime end = now.plusDays(1);

        AuctionCreateRequest request = AuctionCreateRequest.builder()
                .title("auction")
                .description("just auction")
                .startPrice(new BigDecimal("100"))
                .startAt(start)
                .endAt(end).build();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> auctionService.createAuction(request));
        assertEquals("Start and end times must be configured correctly!", exception.getMessage());

        verify(auctionRepository, never()).save(any(Auction.class));
    }

    // getByID
    @Test
    public void getById_success() {
        Long auctionId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.plusDays(1);
        LocalDateTime end = now.plusDays(2);
        LocalDateTime createdAt = now.plusDays(1);
        LocalDateTime modifiedAt = now.plusDays(1);

        Auction auction = Auction.builder()
                .id(auctionId)
                .title("auction")
                .description("just auction")
                .startPrice(new BigDecimal(100))
                .startAt(start)
                .endAt(end)
                .status(AuctionStatus.DRAFT)
                .winnerId(null)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .deleted(false).build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);
        AuctionResponse response = auctionService.getById(auctionId);

        assertNotNull(response);
        assertEquals(response, auctionService.toResponse(auction));

        verify(auctionRepository).findById(auctionId);
    }

    @Test
    public void getById_fail_whenNotFound() {
        Long auctionId = 1L;

        when(auctionRepository.findById(auctionId)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> auctionService.getById(auctionId));

        assertEquals("Auction not found!", exception.getMessage());

        verify(auctionRepository).findById(auctionId);
    }

    // getAll
    @Test
    public void getAll_success_whenStatusIsNull() {
        LocalDateTime startAt = LocalDateTime.now().plusDays(1);
        LocalDateTime endAt = LocalDateTime.now().plusDays(2);

        Auction auction1 = Auction.builder()
                .id(1L)
                .title("auction 1")
                .description("first auction")
                .startPrice(new BigDecimal("100"))
                .startAt(startAt)
                .endAt(endAt)
                .status(AuctionStatus.DRAFT)
                .winnerId(null)
                .createdAt(startAt)
                .modifiedAt(startAt)
                .deleted(false)
                .build();

        Auction auction2 = Auction.builder()
                .id(2L)
                .title("auction 2")
                .description("second auction")
                .startPrice(new BigDecimal("200"))
                .startAt(startAt)
                .endAt(endAt)
                .status(AuctionStatus.ACTIVE)
                .winnerId(null)
                .createdAt(startAt)
                .modifiedAt(startAt)
                .deleted(false)
                .build();

        when(auctionRepository.findAll()).thenReturn(List.of(auction1, auction2));

        List<AuctionResponse> responses = auctionService.getAll(null);

        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals(1L, responses.get(0).getId());
        assertEquals("auction 1", responses.get(0).getTitle());
        assertEquals(AuctionStatus.DRAFT, responses.get(0).getStatus());

        assertEquals(2L, responses.get(1).getId());
        assertEquals("auction 2", responses.get(1).getTitle());
        assertEquals(AuctionStatus.ACTIVE, responses.get(1).getStatus());

        verify(auctionRepository).findAll();
        verify(auctionRepository, never()).findAllByStatus(any(AuctionStatus.class));
    }

    @Test
    public void getAll_success_whenStatusIsNotNull() {
        LocalDateTime startAt = LocalDateTime.now().plusDays(1);
        LocalDateTime endAt = LocalDateTime.now().plusDays(2);

        Auction auction1 = Auction.builder()
                .id(1L)
                .title("auction 1")
                .description("first auction")
                .startPrice(new BigDecimal("100"))
                .startAt(startAt)
                .endAt(endAt)
                .status(AuctionStatus.DRAFT)
                .winnerId(null)
                .createdAt(startAt)
                .modifiedAt(startAt)
                .deleted(false)
                .build();

        Auction auction2 = Auction.builder()
                .id(2L)
                .title("auction 2")
                .description("second auction")
                .startPrice(new BigDecimal("200"))
                .startAt(startAt)
                .endAt(endAt)
                .status(AuctionStatus.ACTIVE)
                .winnerId(null)
                .createdAt(startAt)
                .modifiedAt(startAt)
                .deleted(false)
                .build();

        when(auctionRepository.findAllByStatus(AuctionStatus.ACTIVE))
                .thenReturn(List.of(auction2));

        List<AuctionResponse> responses = auctionService.getAll(AuctionStatus.ACTIVE);

        assertNotNull(responses);
        assertEquals(1, responses.size());

        assertEquals(2L, responses.get(0).getId());
        assertEquals("auction 2", responses.get(0).getTitle());
        assertEquals("second auction", responses.get(0).getDescription());
        assertEquals(new BigDecimal("200"), responses.get(0).getStartPrice());
        assertEquals(AuctionStatus.ACTIVE, responses.get(0).getStatus());

        verify(auctionRepository).findAllByStatus(AuctionStatus.ACTIVE);
        verify(auctionRepository, never()).findAll();
    }

    // activateAuction
    @Test
    void activateAuction_success() {
        Long auctionId = 1L;

        Auction auction = Auction.builder()
                .id(auctionId)
                .title("auction")
                .description("just auction")
                .startPrice(new BigDecimal("100"))
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(2))
                .status(AuctionStatus.DRAFT)
                .deleted(false)
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);
        when(auctionRepository.updateStatus(auctionId, AuctionStatus.ACTIVE)).thenReturn(1);

        assertDoesNotThrow(() -> auctionService.activateAuction(auctionId));

        verify(auctionRepository).findById(auctionId);
        verify(auctionRepository).updateStatus(auctionId, AuctionStatus.ACTIVE);
    }

    @Test
    void activateAuction_fail_whenIdIsNotFound() {
        Long auctionId = 99L;

        when(auctionRepository.findById(auctionId)).thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> auctionService.activateAuction(auctionId)
        );

        assertEquals("Auction not found!", exception.getMessage());

        verify(auctionRepository).findById(auctionId);
        verify(auctionRepository, never()).updateStatus(anyLong(), any(AuctionStatus.class));
    }

    @Test
    void activateAuction_fail_whenStatusIsNotDraft() {
        Long auctionId = 1L;

        Auction auction = Auction.builder()
                .id(auctionId)
                .title("auction")
                .description("just auction")
                .startPrice(new BigDecimal("100"))
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(2))
                .status(AuctionStatus.ACTIVE)
                .deleted(false)
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> auctionService.activateAuction(auctionId)
        );

        assertEquals("Auction to activate is not DRAFT!", exception.getMessage());

        verify(auctionRepository).findById(auctionId);
        verify(auctionRepository, never()).updateStatus(anyLong(), any(AuctionStatus.class));
    }

    @Test
    void activateAuction_fail_whenEndTimeHasPassed() {
        Long auctionId = 1L;

        Auction auction = Auction.builder()
                .id(auctionId)
                .title("auction")
                .description("just auction")
                .startPrice(new BigDecimal("100"))
                .startAt(LocalDateTime.now().minusDays(2))
                .endAt(LocalDateTime.now().minusDays(1))
                .status(AuctionStatus.DRAFT)
                .deleted(false)
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> auctionService.activateAuction(auctionId)
        );

        assertEquals("End time must be in future to activate auction!", exception.getMessage());

        verify(auctionRepository).findById(auctionId);
        verify(auctionRepository, never()).updateStatus(anyLong(), any(AuctionStatus.class));
    }

    @Test
    void activateAuction_fail_whenChangedIsNotPositive() {
        Long auctionId = 1L;

        Auction auction = Auction.builder()
                .id(auctionId)
                .title("auction")
                .description("just auction")
                .startPrice(new BigDecimal("100"))
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(2))
                .status(AuctionStatus.DRAFT)
                .deleted(false)
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);
        when(auctionRepository.updateStatus(auctionId, AuctionStatus.ACTIVE)).thenReturn(0);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> auctionService.activateAuction(auctionId)
        );

        assertEquals("Auction could not be activated!", exception.getMessage());

        verify(auctionRepository).findById(auctionId);
        verify(auctionRepository).updateStatus(auctionId, AuctionStatus.ACTIVE);
    }

    // finishAuction
    @Test
    void finishAuction_success() {
        Long auctionId = 1L;
        Long bidderId = 10L;

        Auction auction = Auction.builder()
                .id(auctionId)
                .title("auction")
                .description("description")
                .startPrice(new BigDecimal("100"))
                .startAt(LocalDateTime.now().minusDays(2))
                .endAt(LocalDateTime.now().minusDays(1))
                .status(AuctionStatus.ACTIVE)
                .deleted(false)
                .build();

        Bid highestBid = Bid.builder()
                .id(1L)
                .auctionId(auctionId)
                .bidderId(bidderId)
                .amount(new BigDecimal("300"))
                .build();

        Bidder bidder = Bidder.builder()
                .id(bidderId)
                .fullName("john doe")
                .email("john@gmail.com")
                .pin("1234567")
                .deleted(false)
                .registeredAt(LocalDateTime.now())
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);
        when(bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId)).thenReturn(highestBid);
        when(auctionRepository.updateStatus(auctionId, AuctionStatus.COMPLETED)).thenReturn(1);
        when(auctionRepository.updateWinner(auctionId, bidderId)).thenReturn(1);
        when(bidderRepository.findByIdAndDeletedFalse(bidderId)).thenReturn(bidder);

        BidderResponse response = auctionService.finishAuction(auctionId);

        assertNotNull(response);
        assertEquals(bidderId, response.getId());
        assertEquals("john doe", response.getFullName());
        assertEquals("john@gmail.com", response.getEmail());

        verify(auctionRepository).findById(auctionId);
        verify(bidRepository).findTopByAuctionIdOrderByAmountDesc(auctionId);
        verify(auctionRepository).updateStatus(auctionId, AuctionStatus.COMPLETED);
        verify(auctionRepository).updateWinner(auctionId, bidderId);
        verify(bidderRepository).findByIdAndDeletedFalse(bidderId);
    }

    @Test
    void finishAuction_fail_whenAuctionNotFound() {
        Long auctionId = 99L;

        when(auctionRepository.findById(auctionId)).thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> auctionService.finishAuction(auctionId)
        );

        assertEquals("Auction not found!", exception.getMessage());

        verify(auctionRepository).findById(auctionId);
        verifyNoInteractions(bidRepository);
        verifyNoInteractions(bidderRepository);
        verify(auctionRepository, never()).updateStatus(anyLong(), any(AuctionStatus.class));
        verify(auctionRepository, never()).updateWinner(anyLong(), anyLong());
    }

    @Test
    void finishAuction_fail_whenAuctionIsNotActive() {
        Long auctionId = 1L;

        Auction auction = Auction.builder()
                .id(auctionId)
                .status(AuctionStatus.DRAFT)
                .deleted(false)
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> auctionService.finishAuction(auctionId)
        );

        assertEquals("Auction to finish is not ACTIVE!", exception.getMessage());

        verify(auctionRepository).findById(auctionId);
        verifyNoInteractions(bidRepository);
        verifyNoInteractions(bidderRepository);
        verify(auctionRepository, never()).updateStatus(anyLong(), any(AuctionStatus.class));
        verify(auctionRepository, never()).updateWinner(anyLong(), anyLong());
    }

    @Test
    void finishAuction_fail_whenAuctionHasNoBids() {
        Long auctionId = 1L;

        Auction auction = Auction.builder()
                .id(auctionId)
                .status(AuctionStatus.ACTIVE)
                .deleted(false)
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);
        when(bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId)).thenReturn(null);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> auctionService.finishAuction(auctionId)
        );

        assertEquals("Cannot complete auction because it has no bids.", exception.getMessage());

        verify(auctionRepository).findById(auctionId);
        verify(bidRepository).findTopByAuctionIdOrderByAmountDesc(auctionId);
        verify(auctionRepository, never()).updateStatus(anyLong(), any(AuctionStatus.class));
        verify(auctionRepository, never()).updateWinner(anyLong(), anyLong());
        verifyNoInteractions(bidderRepository);
    }

    @Test
    void finishAuction_fail_whenStatusUpdateIsNotPositive() {
        Long auctionId = 1L;
        Long bidderId = 10L;

        Auction auction = Auction.builder()
                .id(auctionId)
                .status(AuctionStatus.ACTIVE)
                .deleted(false)
                .build();

        Bid highestBid = Bid.builder()
                .id(1L)
                .auctionId(auctionId)
                .bidderId(bidderId)
                .amount(new BigDecimal("300"))
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);
        when(bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId)).thenReturn(highestBid);
        when(auctionRepository.updateStatus(auctionId, AuctionStatus.COMPLETED)).thenReturn(0);
        when(auctionRepository.updateWinner(auctionId, bidderId)).thenReturn(1);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> auctionService.finishAuction(auctionId)
        );

        assertEquals("Auction could not be completed!", exception.getMessage());

        verify(auctionRepository).updateStatus(auctionId, AuctionStatus.COMPLETED);
        verify(auctionRepository).updateWinner(auctionId, bidderId);
        verifyNoInteractions(bidderRepository);
    }

    @Test
    void finishAuction_fail_whenWinnerUpdateIsNotPositive() {
        Long auctionId = 1L;
        Long bidderId = 10L;

        Auction auction = Auction.builder()
                .id(auctionId)
                .status(AuctionStatus.ACTIVE)
                .deleted(false)
                .build();

        Bid highestBid = Bid.builder()
                .id(1L)
                .auctionId(auctionId)
                .bidderId(bidderId)
                .amount(new BigDecimal("300"))
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);
        when(bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId)).thenReturn(highestBid);
        when(auctionRepository.updateStatus(auctionId, AuctionStatus.COMPLETED)).thenReturn(1);
        when(auctionRepository.updateWinner(auctionId, bidderId)).thenReturn(0);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> auctionService.finishAuction(auctionId)
        );

        assertEquals("Auction could not be completed!", exception.getMessage());

        verify(auctionRepository).updateStatus(auctionId, AuctionStatus.COMPLETED);
        verify(auctionRepository).updateWinner(auctionId, bidderId);
        verifyNoInteractions(bidderRepository);
    }

    // cancelAuction
    @Test
    void cancelAuction_success() {
        Long auctionId = 1L;

        Auction auction = Auction.builder()
                .id(auctionId)
                .status(AuctionStatus.ACTIVE)
                .deleted(false)
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);
        when(auctionRepository.updateStatus(auctionId, AuctionStatus.CANCELLED)).thenReturn(1);

        assertDoesNotThrow(() -> auctionService.cancelAuction(auctionId));

        verify(auctionRepository).findById(auctionId);
        verify(auctionRepository).updateStatus(auctionId, AuctionStatus.CANCELLED);
    }

    @Test
    void cancelAuction_fail_whenStatusIsNotDraftOrActive() {
        Long auctionId = 1L;

        Auction auction = Auction.builder()
                .id(auctionId)
                .status(AuctionStatus.COMPLETED)
                .deleted(false)
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> auctionService.cancelAuction(auctionId)
        );

        assertEquals("Auction to cancel must be ACTIVE or DRAFT!", exception.getMessage());

        verify(auctionRepository).findById(auctionId);
        verify(auctionRepository, never()).updateStatus(anyLong(), any(AuctionStatus.class));
    }

    @Test
    void cancelAuction_fail_whenStatusChangeIsNotPositive() {
        Long auctionId = 1L;

        Auction auction = Auction.builder()
                .id(auctionId)
                .status(AuctionStatus.DRAFT)
                .deleted(false)
                .build();

        when(auctionRepository.findById(auctionId)).thenReturn(auction);
        when(auctionRepository.updateStatus(auctionId, AuctionStatus.CANCELLED)).thenReturn(0);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> auctionService.cancelAuction(auctionId)
        );

        assertEquals("Auction could not be cancelled!", exception.getMessage());

        verify(auctionRepository).findById(auctionId);
        verify(auctionRepository).updateStatus(auctionId, AuctionStatus.CANCELLED);
    }

}
