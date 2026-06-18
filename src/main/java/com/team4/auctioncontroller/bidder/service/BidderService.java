package com.team4.auctioncontroller.bidder.service;

import com.team4.auctioncontroller.bidder.model.dto.BidderRegisterRequest;
import com.team4.auctioncontroller.bidder.model.dto.BidderResponse;
import com.team4.auctioncontroller.bidder.model.entity.Bidder;
import com.team4.auctioncontroller.bidder.repository.BidderRepository;
import com.team4.auctioncontroller.exception.ConflictException;
import com.team4.auctioncontroller.exception.NotFoundException;
import com.team4.auctioncontroller.exception.NotSavedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BidderService {

    private final BidderRepository bidderRepository;

    public BidderResponse registerBidder(BidderRegisterRequest bidderRegisterRequest) {
        String fullName = bidderRegisterRequest.getFullName().trim().toLowerCase();
        String email = bidderRegisterRequest.getEmail().trim().toLowerCase();
        String pin = bidderRegisterRequest.getPin().trim();
        if(bidderRepository.existsBidderByEmail(email)){
            throw new ConflictException("This email is in use!");
        }
        if(bidderRepository.existsBidderByPin(pin)){
            throw new ConflictException("This pin is in use!");
        }

        Bidder bidder = Bidder.builder()
                .fullName(fullName)
                .email(email)
                .pin(pin)
                .deleted(false).build();

        Bidder savedBidder = bidderRepository.save(bidder);
        return toResponse(savedBidder);
    }

    public BidderResponse getById(Long id) {
        Bidder bidder = Optional.ofNullable(bidderRepository.findByIdAndDeletedFalse(id))
                .orElseThrow(() -> new NotFoundException("Bidder not found!"));

        return toResponse(bidder);
    }

    public static BidderResponse toResponse(Bidder bidder) {
        return BidderResponse.builder()
                .id(bidder.getId())
                .fullName(bidder.getFullName())
                .email(bidder.getEmail())
                .registeredAt(bidder.getRegisteredAt()).build();
    }
}
