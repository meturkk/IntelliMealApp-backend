package com.emin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalDietRequest {

    private Integer age;
    private String gender;
    private Integer height;
    private Integer weight;

    @JsonProperty("activity_level")
    private String activityLevel;

    private String goal;

    @JsonProperty("diet_preference")
    private String dietPreference;

    @JsonProperty("health_condition")
    private String healthCondition;

    private List<String> allergens;

    private Integer days;
}
