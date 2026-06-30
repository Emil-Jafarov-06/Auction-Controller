package com.project.authservice.model.dto;

import com.project.authservice.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenResponse {

    private Boolean valid;
    private Long id;
    private String email;
    private Role role;

}
