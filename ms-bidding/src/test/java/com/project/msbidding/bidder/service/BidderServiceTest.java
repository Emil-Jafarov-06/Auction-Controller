package com.project.msbidding.bidder.service;

import com.project.msbidding.bidder.model.dto.BidderRegisterRequest;
import com.project.msbidding.bidder.model.dto.BidderResponse;
import com.project.msbidding.bidder.model.entity.Bidder;
import com.project.msbidding.bidder.repository.BidderRepository;
import com.project.msbidding.exception.ConflictException;
import com.project.msbidding.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
                .fullName("  Emil Jafarov  ")
                .email("  EMIL@MAIL.COM  ")
                .pin(" 1234567 ")
                .build();

        Bidder savedBidder = Bidder.builder()
                .id(1L)
                .fullName("emil jafarov")
                .email("emil@mail.com")
                .pin("1234567")
                .registeredAt(LocalDateTime.now())
                .deleted(false)
                .build();

        when(bidderRepository.existsBidderByEmail("emil@mail.com")).thenReturn(false);
        when(bidderRepository.existsBidderByPin("1234567")).thenReturn(false);
        when(bidderRepository.save(any(Bidder.class))).thenReturn(savedBidder);

        BidderResponse response = bidderService.registerBidder(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("emil jafarov", response.getFullName());
        assertEquals("emil@mail.com", response.getEmail());
        assertEquals(savedBidder.getRegisteredAt(), response.getRegisteredAt());

        ArgumentCaptor<Bidder> bidderCaptor = ArgumentCaptor.forClass(Bidder.class);
        verify(bidderRepository).save(bidderCaptor.capture());

        Bidder bidderToSave = bidderCaptor.getValue();

        assertEquals("emil jafarov", bidderToSave.getFullName());
        assertEquals("emil@mail.com", bidderToSave.getEmail());
        assertEquals("1234567", bidderToSave.getPin());
        assertFalse(bidderToSave.getDeleted());
    }

    @Test
    void registerBidder_whenEmailAlreadyExists_throwsConflictException() {
        BidderRegisterRequest request = BidderRegisterRequest.builder()
                .fullName("Emil Jafarov")
                .email("emil@mail.com")
                .pin("1234567")
                .build();

        when(bidderRepository.existsBidderByEmail("emil@mail.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> bidderService.registerBidder(request));

        verify(bidderRepository).existsBidderByEmail("emil@mail.com");
        verify(bidderRepository, never()).existsBidderByPin(anyString());
        verify(bidderRepository, never()).save(any(Bidder.class));
    }

    @Test
    void registerBidder_whenPinAlreadyExists_throwsConflictException() {
        BidderRegisterRequest request = BidderRegisterRequest.builder()
                .fullName("Emil Jafarov")
                .email("emil@mail.com")
                .pin("1234567")
                .build();

        when(bidderRepository.existsBidderByEmail("emil@mail.com")).thenReturn(false);
        when(bidderRepository.existsBidderByPin("1234567")).thenReturn(true);

        assertThrows(ConflictException.class, () -> bidderService.registerBidder(request));

        verify(bidderRepository).existsBidderByEmail("emil@mail.com");
        verify(bidderRepository).existsBidderByPin("1234567");
        verify(bidderRepository, never()).save(any(Bidder.class));
    }

    @Test
    void getById_success() {
        Bidder bidder = Bidder.builder()
                .id(1L)
                .fullName("emil jafarov")
                .email("emil@mail.com")
                .pin("1234567")
                .registeredAt(LocalDateTime.now())
                .deleted(false)
                .build();

        when(bidderRepository.findByIdAndDeletedFalse(1L)).thenReturn(bidder);

        BidderResponse response = bidderService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("emil jafarov", response.getFullName());
        assertEquals("emil@mail.com", response.getEmail());
        assertEquals(bidder.getRegisteredAt(), response.getRegisteredAt());

        verify(bidderRepository).findByIdAndDeletedFalse(1L);
    }

    @Test
    void getById_whenBidderDoesNotExist_throwsNotFoundException() {
        when(bidderRepository.findByIdAndDeletedFalse(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> bidderService.getById(1L));

        verify(bidderRepository).findByIdAndDeletedFalse(1L);
    }

    @Test
    void toResponse_success() {
        Bidder bidder = Bidder.builder()
                .id(1L)
                .fullName("emil jafarov")
                .email("emil@mail.com")
                .pin("1234567")
                .registeredAt(LocalDateTime.now())
                .deleted(false)
                .build();

        BidderResponse response = BidderService.toResponse(bidder);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("emil jafarov", response.getFullName());
        assertEquals("emil@mail.com", response.getEmail());
        assertEquals(bidder.getRegisteredAt(), response.getRegisteredAt());
    }
}