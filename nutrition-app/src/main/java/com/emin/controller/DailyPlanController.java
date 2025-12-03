package com.emin.controller;

import com.emin.dto.DtoDailyPlan;
import com.emin.dto.DtoDailyPlanWrapper;
import com.emin.services.DailyPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/api/users/{userId}/daily-plans")
public class DailyPlanController {

    @Autowired
    private DailyPlanService dailyPlanService;

    // POST /rest/api/users/{userId}/daily-plans
    
    @PostMapping
    public ResponseEntity<DtoDailyPlanWrapper> createDailyPlans(
            @PathVariable String userId,
            @RequestBody DtoDailyPlanWrapper wrapper) {
        
        List<DtoDailyPlan> savedPlans = dailyPlanService.createDailyPlans(userId, wrapper.getDailyPlans());
        return new ResponseEntity<>(new DtoDailyPlanWrapper(savedPlans), HttpStatus.CREATED);
    }

    // GET /rest/api/users/{userId}/daily-plans
    
    @GetMapping
    public ResponseEntity<DtoDailyPlanWrapper> getAllDailyPlansForUser(@PathVariable String userId) {
        List<DtoDailyPlan> planList = dailyPlanService.getDailyPlans(userId);
        return ResponseEntity.ok(new DtoDailyPlanWrapper(planList));
    }

    // PUT /rest/api/users/{userId}/daily-plans/check-all
    // Marks all daily plans as checked for the specified user

    @PutMapping("/check-all")
    public ResponseEntity<DtoDailyPlanWrapper> checkAllDailyPlans(@PathVariable String userId) {
        List<DtoDailyPlan> updatedPlans = dailyPlanService.markAllPlansAsChecked(userId);
        return ResponseEntity.ok(new DtoDailyPlanWrapper(updatedPlans));
    }

    // DELETE /rest/api/users/{userId}/daily-plans

    @DeleteMapping
    public ResponseEntity<Void> deleteAllDailyPlans(@PathVariable String userId) {
        dailyPlanService.deleteAllPlans(userId);
        return ResponseEntity.noContent().build();
    }
}