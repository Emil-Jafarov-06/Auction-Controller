package com.project.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.verification-end-point}")
    private String verificationEndpoint;

    public void sendVerificationEmail(String email, String verificationToken) {

        String verificationUrl = verificationEndpoint + "?token=" + verificationToken;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setSubject("Verification your email address!");
        message.setTo(email);
        message.setText("""
                Hello.
                There has been an application request for Auction-System from this email address.
                If it was you, please verify:
                %s
                """.formatted(verificationUrl));

        mailSender.send(message);

    }

}
