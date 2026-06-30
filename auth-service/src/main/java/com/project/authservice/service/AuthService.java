package com.project.authservice.service;

import com.project.authservice.enums.Role;
import com.project.authservice.exception.BadRequestException;
import com.project.authservice.exception.ConflictException;
import com.project.authservice.exception.NotFoundException;
import com.project.authservice.model.dto.AuthResponse;
import com.project.authservice.model.dto.LoginRequest;
import com.project.authservice.model.dto.RefreshRequest;
import com.project.authservice.model.dto.RegisterRequest;
import com.project.authservice.model.entity.EmailVerificationToken;
import com.project.authservice.model.entity.User;
import com.project.authservice.repository.UserRepository;
import com.project.authservice.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        String fullName = registerRequest.getFullName().trim();
        String email = registerRequest.getEmail().trim().toLowerCase();
        String pin = registerRequest.getPin().trim().toLowerCase();
        String password = registerRequest.getPassword().trim();
        if (userRepository.existsByEmailAndDeletedFalse(email)) {
            throw new ConflictException("This email is in use!");
        }
        if (userRepository.existsByPinAndDeletedFalse(pin)) {
            throw new ConflictException("Pin is already in use!");
        }

        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .pin(pin)
                .role(Role.BIDDER)
                .password(passwordEncoder.encode(password.trim()))
                .registeredAt(LocalDateTime.now())
                .emailVerified(false)
                .deleted(false)
                .build();

        User savedUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .userId(savedUser.getId())
                .createDate(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .isUsed(false).build();

        verificationTokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken.getToken());
    }

    public AuthResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, loginRequest.getPassword())
        );

        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new BadRequestException("Invalid email or password!"));

        if (user.getEmailVerified() == false){
            throw new BadRequestException("Email should be verified before login!");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return new AuthResponse(
                jwtService.generateAccessToken(userDetails),
                jwtService.generateRefreshToken(userDetails)
        );
    }

    public AuthResponse refreshToken(RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token!");
        }

        String email = jwtService.extractEmail(refreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new BadRequestException("Invalid refresh token!");
        }

        String accessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthResponse(accessToken, newRefreshToken);
    }

    @Transactional
    public void verifyUser(String token) {
        EmailVerificationToken verificationToken = Optional.ofNullable(verificationTokenRepository.getByToken(token))
                .orElseThrow(() -> new NotFoundException("Invalid token!"));

        if(verificationToken.isUsed()){
            throw new BadRequestException("Token has already been used!");
        }

        if(verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token is expired!");
        }

        User user = userRepository.findByIdAndDeletedFalse(verificationToken.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found!"));

        user.setEmailVerified(true);
        verificationToken.setUsed(true);
        userRepository.save(user);
        verificationTokenRepository.save(verificationToken);
    }
}
