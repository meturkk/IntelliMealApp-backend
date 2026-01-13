package com.emin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emin.entities.MealRecord;

public interface MealRecordRepository extends JpaRepository<MealRecord, String> {
    
}
