package com.emin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emin.entities.PersonalInfo;

public interface PersonalInfoRepository extends JpaRepository<PersonalInfo, String> {
    List<PersonalInfo> findByUserId(String userId);
}
