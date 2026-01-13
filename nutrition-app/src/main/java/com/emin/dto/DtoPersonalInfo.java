package com.emin.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtoPersonalInfo {
    
    private String id;
    private String userId;
    private LocalDateTime date;
    private Integer age;
    private Double weight;
    private Double targetWeight;
    private Integer height;
    private String gender;
    private String activityLevel;
    private String dietaryPreference;
    private String goal;
    private String healthCondition;
    private String allergens;
    private Integer neckSize;
    private Integer waistSize;
    private Integer hipSize;
    private Integer chestSize;
    private Integer armSize;
    private Integer legSize;
}
