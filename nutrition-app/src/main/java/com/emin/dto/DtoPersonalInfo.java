package com.emin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtoPersonalInfo {
    
    private String id;
    private Integer age;
    private Double weight;
    private Double height;
    private String gender;
    private String activityLevel;
    private String dietaryPreference;
    private String goal;
    private String healthCondition;
    private Integer neckSize;
    private Integer waistSize;
    private Integer hipSize;
    private Integer chestSize;
    private Integer armSize;
    private Integer legSize;

}
