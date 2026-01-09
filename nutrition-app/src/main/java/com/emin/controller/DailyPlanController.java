package com.emin.controller;

import com.emin.dto.DtoDailyPlan;
import com.emin.dto.DtoDailyPlanWrapper;
import com.emin.dto.ExternalDietRequest;
import com.emin.dto.DtoPersonalInfo;
import com.emin.services.ExternalDietService;
import com.emin.services.PersonalInfoService;
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

    @Autowired
    private ExternalDietService externalDietService;

    @Autowired
    private PersonalInfoService personalInfoService;

    @Autowired
    private com.emin.services.UserService userService;

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

    // POST /rest/api/users/{userId}/daily-plans/generate
    @PostMapping("/generate")
    public ResponseEntity<DtoDailyPlanWrapper> generateAndSaveDailyPlans(
            @PathVariable String userId,
            @RequestBody ExternalDietRequest request) {
        try {
            java.util.List<DtoDailyPlan> saved = externalDietService.generateAndSave(userId, request);
            userService.markAsReceivedMealPlan(userId);
            return new ResponseEntity<>(new DtoDailyPlanWrapper(saved), HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // POST /rest/api/users/{userId}/daily-plans/generate-from-personal
    @PostMapping("/generate-from-personal")
    public ResponseEntity<DtoDailyPlanWrapper> generateFromPersonalInfo(@PathVariable String userId) {
        try {
            // remove existing plans for this user before generating new ones
            dailyPlanService.deleteAllPlans(userId);

            java.util.List<DtoPersonalInfo> infos = personalInfoService.getAllByUserId(userId);
            if (infos == null || infos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            DtoPersonalInfo latest = infos.stream()
                .filter(i -> i.getDate() != null)
                .max(java.util.Comparator.comparing(DtoPersonalInfo::getDate))
                .orElse(infos.get(0));

            ExternalDietRequest req = new ExternalDietRequest();
            req.setAge(latest.getAge());
            req.setGender(latest.getGender());
            req.setHeight(latest.getHeight());
            if (latest.getWeight() != null) {
                req.setWeight((int) Math.round(latest.getWeight()));
            }
            req.setActivityLevel(latest.getActivityLevel());
            req.setDietPreference(latest.getDietaryPreference());
            req.setGoal(latest.getGoal());
            req.setHealthCondition(latest.getHealthCondition());
            
            String allergensStr = latest.getAllergens();
            if (allergensStr != null && !allergensStr.trim().isEmpty()) {
                req.setAllergens(java.util.Arrays.asList(allergensStr.split("\\s*,\\s*")));
            } else {
                req.setAllergens(java.util.Collections.emptyList());
            }
            
            req.setDays(15);

            java.util.List<DtoDailyPlan> saved = externalDietService.generateAndSave(userId, req);
            userService.markAsReceivedMealPlan(userId);
            return new ResponseEntity<>(new DtoDailyPlanWrapper(saved), HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // PUT /rest/api/users/{userId}/daily-plans/{day}
    // Regenerate a single day's plan by calling external API with days=1 and replacing stored day
    @PutMapping("/{day}")
    public ResponseEntity<DtoDailyPlan> regenerateSingleDay(@PathVariable String userId, @PathVariable Integer day) {
        try {
            java.util.List<DtoPersonalInfo> infos = personalInfoService.getAllByUserId(userId);
            if (infos == null || infos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            DtoPersonalInfo latest = infos.stream()
                .filter(i -> i.getDate() != null)
                .max(java.util.Comparator.comparing(DtoPersonalInfo::getDate))
                .orElse(infos.get(0));

            ExternalDietRequest req = new ExternalDietRequest();
            req.setAge(latest.getAge());
            req.setGender(latest.getGender());
            req.setHeight(latest.getHeight());
            if (latest.getWeight() != null) req.setWeight((int) Math.round(latest.getWeight()));
            req.setActivityLevel(latest.getActivityLevel());
            req.setDietPreference(latest.getDietaryPreference());
            req.setGoal(latest.getGoal());
            req.setHealthCondition(latest.getHealthCondition());

            String allergensStr = latest.getAllergens();
            if (allergensStr != null && !allergensStr.trim().isEmpty()) {
                req.setAllergens(java.util.Arrays.asList(allergensStr.split("\\s*,\\s*")));
            } else {
                req.setAllergens(java.util.Collections.emptyList());
            }

            req.setDays(1); 

            java.util.List<DtoDailyPlan> generated = externalDietService.generate(req);
            if (generated == null || generated.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            DtoDailyPlan planForDay = generated.get(0);
            planForDay.setDay(day);

            DtoDailyPlan saved = dailyPlanService.replaceDailyPlanForDay(userId, day, planForDay);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // PUT /rest/api/users/{userId}/daily-plans/{day}/update
    // Manually update a daily plan (especially meals) for a specific day
    @PutMapping("/{day}/update")
    public ResponseEntity<DtoDailyPlan> updateDailyPlan(
            @PathVariable String userId,
            @PathVariable Integer day,
            @RequestBody DtoDailyPlan dtoDailyPlan) {
        
        DtoDailyPlan updatedPlan = dailyPlanService.replaceDailyPlanForDay(userId, day, dtoDailyPlan);
        return ResponseEntity.ok(updatedPlan);
    }
    
    // PUT /rest/api/users/{userId}/daily-plans/{day}/meals/{mealType}
    // Update a specific meal (e.g., breakfast) for a specific day
    @PutMapping("/{day}/meals/{mealType}")
    public ResponseEntity<DtoDailyPlan> updateMealForDate(
            @PathVariable String userId,
            @PathVariable Integer day,
            @PathVariable String mealType,
            @RequestBody com.emin.dto.DtoMealRecord mealRecord) {
        try {
            DtoDailyPlan updatedPlan = dailyPlanService.updateMeal(userId, day, mealType, mealRecord);
            return ResponseEntity.ok(updatedPlan);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); 
        }
    }
}