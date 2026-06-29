package com.project.authservice.repository;

import com.project.authservice.model.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    EmailVerificationToken getByToken(String token);
}
