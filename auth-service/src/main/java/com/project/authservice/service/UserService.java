package com.project.authservice.service;

import com.project.authservice.exception.NotFoundException;
import com.project.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.project.authservice.model.entity.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;

    public User getUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new NotFoundException("User not found!"));
    }
}
