package com.emin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoMealRecord {

    @JsonProperty("meal_type")
    private String mealType;

    @JsonProperty("meal_name")
    private String mealName;

    @JsonProperty("ingredients")
    private List<String> ingredients;

    @JsonProperty("total_calories")
    private Integer totalCalories;

    @JsonProperty("total_protein_g")
    private Double totalProteinG;

    @JsonProperty("health_benefit_note")
    private String healthBenefitNote;
}