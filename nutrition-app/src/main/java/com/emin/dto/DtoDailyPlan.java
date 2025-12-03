package com.emin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoDailyPlan {

    @JsonProperty("day")
    private Integer day;

    @JsonProperty("checked")
    private Boolean checked;
    
    @JsonProperty("meals")
    private List<DtoMealRecord> meals;
    
    @JsonProperty("day_summary")
    private DtoDaySummary daySummary;
}