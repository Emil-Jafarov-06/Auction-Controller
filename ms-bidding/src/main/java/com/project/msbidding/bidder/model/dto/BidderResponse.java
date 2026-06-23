package com.project.msbidding.bidder.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidderResponse {

    private Long id;
    private String fullName;
    private String email;
    private LocalDateTime registeredAt;

}
