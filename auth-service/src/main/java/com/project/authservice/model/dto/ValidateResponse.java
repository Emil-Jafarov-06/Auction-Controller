package com.project.authservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateResponse {

    private Boolean authenticated;
    private Boolean authorized;
    private Long id;
    private String email;
    private String role;

}
