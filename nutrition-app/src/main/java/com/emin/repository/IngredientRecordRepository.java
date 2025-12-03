package com.emin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emin.entities.IngredientRecord;

public interface IngredientRecordRepository extends JpaRepository<IngredientRecord, String> {
    
}
