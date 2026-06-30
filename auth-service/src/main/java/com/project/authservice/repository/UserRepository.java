package com.project.authservice.repository;

import com.project.authservice.enums.Role;
import com.project.authservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByPinAndDeletedFalse(String pin);

    Optional<User> findByEmailAndDeletedFalse(String email);

    Optional<User> findByIdAndDeletedFalse(Long id);

    boolean existsByRoleAndDeletedFalse(Role role);
}
