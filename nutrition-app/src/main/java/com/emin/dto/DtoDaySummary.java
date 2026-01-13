package com.emin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtoDaySummary {
    
    @JsonProperty("total_calories")
    private Integer totalCalories;
    
    @JsonProperty("total_protein_g")
    private Double totalProteinG;
}
