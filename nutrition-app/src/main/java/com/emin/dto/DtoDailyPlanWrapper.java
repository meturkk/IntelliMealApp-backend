package com.emin.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtoDailyPlanWrapper {
    
    @JsonProperty("daily_plans")
    private List<DtoDailyPlan> dailyPlans;
}
