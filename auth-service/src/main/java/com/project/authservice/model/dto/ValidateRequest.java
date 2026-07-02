package com.project.authservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateRequest {

    @NotBlank
    private String accessToken;

    @NotBlank
    private String method;

    @NotBlank
    private String path;

}
