package com.emin.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.emin.entities.User;

public interface UserRepositorty extends JpaRepository<User, String>{
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationCode(String verificationCode);
}
