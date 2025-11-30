package com.emin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emin.entities.User;

public interface UserRepositorty extends JpaRepository<User, String>{
    
}
