package com.emin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emin.entities.DailyPlan;

public interface DailyPlanRepository extends JpaRepository<DailyPlan, String> {
    List<DailyPlan> findByUserId(String userId);
    void deleteByUserId(String userId);
    DailyPlan findByUserIdAndDay(String userId, Integer day);
    void deleteByUserIdAndDay(String userId, Integer day);
    List<DailyPlan> findByUserIdOrderByDayAsc(String userId);
}
