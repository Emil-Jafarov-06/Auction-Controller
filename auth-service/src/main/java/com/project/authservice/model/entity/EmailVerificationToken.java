package com.project.authservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "email_verification_token", schema = "user_schema")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "is_used", nullable = false)
    private boolean isUsed;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @PrePersist
    public void prePersist() {
        if (createDate == null) {
            createDate = LocalDateTime.now();
        }
        if (expiryDate == null) {
            expiryDate = LocalDateTime.now().plusMinutes(15);
        }
    }

}
