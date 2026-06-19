package com.team4.auctioncontroller.bidder.service;

import com.team4.auctioncontroller.bidder.model.dto.BidderRegisterRequest;
import com.team4.auctioncontroller.bidder.model.dto.BidderResponse;
import com.team4.auctioncontroller.bidder.model.entity.Bidder;
import com.team4.auctioncontroller.bidder.repository.BidderRepository;
import com.team4.auctioncontroller.exception.ConflictException;
import com.team4.auctioncontroller.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidderServiceTest {

    @Mock
    private BidderRepository bidderRepository;

    @InjectMocks
    private BidderService bidderService;

    @Test
    void registerBidder_success() {
        BidderRegisterRequest request = BidderRegisterRequest.builder()
                .fullName("John Doe")
                .email("John@gmail.com")
                .pin("1234567")
                .build();

        Bidder savedBidder = Bidder.builder()
                .id(1L)
                .fullName("john doe")
                .email("john@gmail.com")
                .pin("1234567")
                .deleted(false)
                .registeredAt(LocalDateTime.now())
                .build();

        when(bidderRepository.existsBidderByEmail("john@gmail.com")).thenReturn(false);
        when(bidderRepository.existsBidderByPin("1234567")).thenReturn(false);
        when(bidderRepository.save(any(Bidder.class))).thenReturn(savedBidder);

        BidderResponse response = bidderService.registerBidder(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("john doe", response.getFullName());
        assertEquals("john@gmail.com", response.getEmail());

        verify(bidderRepository).existsBidderByEmail("john@gmail.com");
        verify(bidderRepository).existsBidderByPin("1234567");
        verify(bidderRepository).save(any(Bidder.class));
    }

    @Test
    void registerBidder_fail_whenEmailAlreadyExists() {
        BidderRegisterRequest request = BidderRegisterRequest.builder()
                .fullName("John Doe")
                .email("John@gmail.com")
                .pin("1234567")
                .build();

        when(bidderRepository.existsBidderByEmail("john@gmail.com")).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () -> bidderService.registerBidder(request));
        assertEquals("This email is in use!", exception.getMessage());

        verify(bidderRepository).existsBidderByEmail("john@gmail.com");
        verify(bidderRepository, never()).existsBidderByPin(anyString());
        verify(bidderRepository, never()).save(any(Bidder.class));
    }

    @Test
    void registerBidder_fail_whenPinAlreadyExists() {
        BidderRegisterRequest request = BidderRegisterRequest.builder()
                .fullName("John Doe")
                .email("John@gmail.com")
                .pin("1234567")
                .build();

        when(bidderRepository.existsBidderByEmail("john@gmail.com")).thenReturn(false);
        when(bidderRepository.existsBidderByPin("1234567")).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> bidderService.registerBidder(request)
        );
        assertEquals("This pin is in use!", exception.getMessage());

        verify(bidderRepository).existsBidderByEmail("john@gmail.com");
        verify(bidderRepository).existsBidderByPin("1234567");
        verify(bidderRepository, never()).save(any(Bidder.class));
    }

    @Test
    void getById_success() {
        Long bidderId = 1L;

        Bidder bidder = Bidder.builder()
                .id(bidderId)
                .fullName("john doe")
                .email("john@gmail.com")
                .pin("1234567")
                .deleted(false)
                .registeredAt(LocalDateTime.now())
                .build();

        when(bidderRepository.findByIdAndDeletedFalse(bidderId)).thenReturn(bidder);

        BidderResponse response = bidderService.getById(bidderId);

        assertNotNull(response);
        assertEquals(bidderId, response.getId());
        assertEquals("john doe", response.getFullName());
        assertEquals("john@gmail.com", response.getEmail());

        verify(bidderRepository).findByIdAndDeletedFalse(bidderId);
    }

    @Test
    void getById_fail_whenBidderNotFound() {
        Long bidderId = 99L;

        when(bidderRepository.findByIdAndDeletedFalse(bidderId)).thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bidderService.getById(bidderId)
        );

        assertEquals("Bidder not found!", exception.getMessage());

        verify(bidderRepository).findByIdAndDeletedFalse(bidderId);
    }
}
