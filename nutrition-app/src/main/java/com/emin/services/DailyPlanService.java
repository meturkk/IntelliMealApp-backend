package com.emin.services;

import com.emin.dto.*;
import com.emin.entities.*;
import com.emin.repository.DailyPlanRepository;
import com.emin.repository.UserRepositorty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
public class DailyPlanService {

    @Autowired
    private DailyPlanRepository dailyPlanRepository;
    @Autowired
    private UserRepositorty userRepository;
    @Autowired
    private UserService userService;

    @Transactional
    public List<DtoDailyPlan> createDailyPlans(String userId, List<DtoDailyPlan> dtoList) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<DailyPlan> plans = dtoList.stream()
                .map(dto -> convertToEntity(dto, user))
                .collect(Collectors.toList());

        List<DailyPlan> savedPlans = dailyPlanRepository.saveAll(plans);

        return savedPlans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private DailyPlan convertToEntity(DtoDailyPlan dto, User user) {
        DailyPlan entity = new DailyPlan();
        entity.setDay(dto.getDay());
        entity.setUser(user);
        entity.setChecked(dto.getChecked() != null ? dto.getChecked() : false);
        
        // Calculate date: today + (day - 1) days
        if (dto.getDay() != null) {
            LocalDate planDate = LocalDate.now().plusDays(dto.getDay() - 1);
            entity.setDate(planDate);
        }
        
        if (dto.getDaySummary() != null) {
            entity.setTotalCalories(dto.getDaySummary().getTotalCalories());
            entity.setTotalProteinG(dto.getDaySummary().getTotalProteinG());
        }

        if (dto.getMeals() != null) {
            List<MealRecord> meals = dto.getMeals().stream()
                    .map(mDto -> convertToMealEntity(mDto, entity))
                    .collect(Collectors.toList());
            entity.setMealRecords(meals);
        }
        return entity;
    }

    private MealRecord convertToMealEntity(DtoMealRecord dto, DailyPlan plan) {
        MealRecord entity = new MealRecord();
        entity.setId(UUID.randomUUID().toString());
        entity.setDailyPlan(plan);
        entity.setMealType(dto.getMealType());
        entity.setMealName(dto.getMealName());
        entity.setTotalCalories(dto.getTotalCalories());
        entity.setTotalProteinG(dto.getTotalProteinG());
        entity.setHealthBenefitNote(dto.getHealthBenefitNote());
        entity.setIngredients(dto.getIngredients());
        return entity;
    }

    private DtoDailyPlan convertToDto(DailyPlan entity) {
        DtoDailyPlan dto = new DtoDailyPlan();
        dto.setDay(entity.getDay());
        dto.setDate(entity.getDate());
        dto.setChecked(entity.getChecked());
        
        DtoDaySummary summary = new DtoDaySummary();
        summary.setTotalCalories(entity.getTotalCalories());
        summary.setTotalProteinG(entity.getTotalProteinG());
        dto.setDaySummary(summary);

        if (entity.getMealRecords() != null) {
            List<DtoMealRecord> meals = entity.getMealRecords().stream()
                    .map(this::convertToMealDto)
                    .collect(Collectors.toList());
            dto.setMeals(meals);
        }
        return dto;
    }

    private DtoMealRecord convertToMealDto(MealRecord entity) {
        DtoMealRecord dto = new DtoMealRecord();
        dto.setMealType(entity.getMealType());
        dto.setMealName(entity.getMealName());
        dto.setTotalCalories(entity.getTotalCalories());
        dto.setTotalProteinG(entity.getTotalProteinG());
        dto.setHealthBenefitNote(entity.getHealthBenefitNote());
        dto.setIngredients(entity.getIngredients());
        return dto;
    }
    
    @Transactional(readOnly = true)
    public List<DtoDailyPlan> getDailyPlans(String userId) {
        List<DailyPlan> plans = dailyPlanRepository.findByUserIdOrderByDayAsc(userId);
        return plans.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional
    public List<DtoDailyPlan> markAllPlansAsChecked(String userId) {
        List<DailyPlan> plans = dailyPlanRepository.findByUserId(userId);
        plans.forEach(plan -> plan.setChecked(true));
        List<DailyPlan> savedPlans = dailyPlanRepository.saveAll(plans);
        
        // Update user's isReceived to 2
        userService.markAsCompletedMealPlan(userId);
        
        return savedPlans.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional
    public void deleteAllPlans(String userId) {
        dailyPlanRepository.deleteByUserId(userId);
    }

    @Transactional
    public DtoDailyPlan replaceDailyPlanForDay(String userId, Integer day, DtoDailyPlan dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DailyPlan existing = dailyPlanRepository.findByUserIdAndDay(userId, day);
        if (existing != null) {
            dailyPlanRepository.delete(existing);
        }

        dto.setDay(day);

        DailyPlan entity = convertToEntity(dto, user);
        DailyPlan saved = dailyPlanRepository.save(entity);

        return convertToDto(saved);
    }

    @Transactional
    public DtoDailyPlan updateMeal(String userId, Integer day, String mealType, DtoMealRecord mealDto) {
        DailyPlan plan = dailyPlanRepository.findByUserIdAndDay(userId, day);
        if (plan == null) {
            throw new RuntimeException("Plan not found for user " + userId + " on day " + day);
        }

        List<MealRecord> meals = plan.getMealRecords();
        if (meals == null) {
            meals = new java.util.ArrayList<>();
            plan.setMealRecords(meals);
        }
        
        MealRecord targetMeal = meals.stream()
                .filter(m -> m.getMealType().equalsIgnoreCase(mealType))
                .findFirst()
                .orElse(null);

        if (targetMeal == null) {
            targetMeal = new MealRecord();
            targetMeal.setId(UUID.randomUUID().toString());
            targetMeal.setDailyPlan(plan);
            targetMeal.setMealType(mealType);
            meals.add(targetMeal);
        }

        // Update fields
        targetMeal.setMealName(mealDto.getMealName());
        targetMeal.setTotalCalories(mealDto.getTotalCalories());
        targetMeal.setTotalProteinG(mealDto.getTotalProteinG());
        targetMeal.setHealthBenefitNote(mealDto.getHealthBenefitNote());
        targetMeal.setIngredients(mealDto.getIngredients());
        
        // Recalculate daily totals
        int totalCals = meals.stream().mapToInt(m -> m.getTotalCalories() != null ? m.getTotalCalories() : 0).sum();
        double totalProtein = meals.stream().mapToDouble(m -> m.getTotalProteinG() != null ? m.getTotalProteinG() : 0.0).sum();
        
        plan.setTotalCalories(totalCals);
        plan.setTotalProteinG(totalProtein);

        DailyPlan saved = dailyPlanRepository.save(plan);
        return convertToDto(saved);
    }
}