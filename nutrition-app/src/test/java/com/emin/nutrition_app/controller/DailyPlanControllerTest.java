package com.emin.nutrition_app.controller;

import com.emin.dto.DtoDailyPlan;
import com.emin.dto.DtoDaySummary;
import com.emin.dto.DtoMealRecord;
import com.emin.nutrition_app.support.ApiTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Daily plan API: create, list, update meals, generate")
class DailyPlanControllerTest extends ApiTestBase {

    private String userId;
    private String token;

    @BeforeEach
    void createUser() throws Exception {
        JsonNode auth = register(uniqueEmail(), "password123");
        userId = auth.at("/user/id").asText();
        token = auth.get("token").asText();
    }

    private String plansUrl() {
        return "/rest/api/users/" + userId + "/daily-plans";
    }

    private static DtoMealRecord meal(String type, String name, int calories, double protein) {
        return new DtoMealRecord(type, name, List.of("ingredient"), calories, protein, "note");
    }

    private static DtoDailyPlan plan(int day, DtoMealRecord... meals) {
        int calories = 0;
        double protein = 0;
        for (DtoMealRecord m : meals) {
            calories += m.getTotalCalories();
            protein += m.getTotalProteinG();
        }
        return new DtoDailyPlan(day, null, false, List.of(meals), new DtoDaySummary(calories, protein));
    }

    private void createPlans(DtoDailyPlan... plans) throws Exception {
        mockMvc.perform(post(plansUrl())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("daily_plans", List.of(plans)))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Created plans are returned in day order with dates starting today")
    void createAndList_returnsPlansOrderedByDay() throws Exception {
        createPlans(
                plan(2, meal("breakfast", "Yulaf", 300, 10.0)),
                plan(1, meal("breakfast", "Omlet", 250, 15.0)));

        mockMvc.perform(get(plansUrl()).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.daily_plans.length()").value(2))
                .andExpect(jsonPath("$.daily_plans[0].day").value(1))
                .andExpect(jsonPath("$.daily_plans[0].date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.daily_plans[1].day").value(2))
                .andExpect(jsonPath("$.daily_plans[1].date").value(LocalDate.now().plusDays(1).toString()))
                .andExpect(jsonPath("$.daily_plans[0].checked").value(false));
    }

    @Test
    @DisplayName("Updating one meal recalculates the day's calorie and protein totals")
    void updateMeal_recalculatesDailyTotals() throws Exception {
        createPlans(plan(1,
                meal("breakfast", "Omlet", 250, 15.0),
                meal("lunch", "Salata", 400, 20.0)));

        mockMvc.perform(put(plansUrl() + "/1/meals/breakfast")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(meal("breakfast", "Menemen", 350, 18.5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.day_summary.total_calories").value(750))
                .andExpect(jsonPath("$.day_summary.total_protein_g").value(38.5));
    }

    @Test
    @DisplayName("Meal type matching is case-insensitive (BREAKFAST updates breakfast)")
    void updateMeal_isCaseInsensitive() throws Exception {
        createPlans(plan(1, meal("breakfast", "Omlet", 250, 15.0)));

        mockMvc.perform(put(plansUrl() + "/1/meals/BREAKFAST")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(meal("breakfast", "Menemen", 350, 18.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meals.length()").value(1))
                .andExpect(jsonPath("$.meals[0].meal_name").value("Menemen"));
    }

    @Test
    @DisplayName("Updating a meal type that does not exist adds it to the day")
    void updateMeal_newMealType_isAdded() throws Exception {
        createPlans(plan(1, meal("breakfast", "Omlet", 250, 15.0)));

        mockMvc.perform(put(plansUrl() + "/1/meals/dinner")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(meal("dinner", "Balık", 500, 35.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meals.length()").value(2))
                .andExpect(jsonPath("$.day_summary.total_calories").value(750));
    }

    @Test
    @DisplayName("Updating a meal on a day with no plan returns 404")
    void updateMeal_unknownDay_returns404() throws Exception {
        mockMvc.perform(put(plansUrl() + "/99/meals/breakfast")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(meal("breakfast", "Omlet", 250, 15.0))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("check-all marks every plan as completed")
    void checkAll_marksAllPlansChecked() throws Exception {
        createPlans(plan(1, meal("breakfast", "Omlet", 250, 15.0)),
                plan(2, meal("breakfast", "Yulaf", 300, 10.0)));

        mockMvc.perform(put(plansUrl() + "/check-all").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.daily_plans[0].checked").value(true))
                .andExpect(jsonPath("$.daily_plans[1].checked").value(true));
    }

    @Test
    @DisplayName("Deleting all plans leaves an empty list")
    void deleteAll_removesPlans() throws Exception {
        createPlans(plan(1, meal("breakfast", "Omlet", 250, 15.0)));

        mockMvc.perform(delete(plansUrl()).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(plansUrl()).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.daily_plans.length()").value(0));
    }

    @Test
    @DisplayName("Generate: plan returned by the (mocked) RAG service is returned with 201")
    void generate_withMockedRagService_returnsCreated() throws Exception {
        DtoDailyPlan generated = plan(1, meal("breakfast", "Omlet", 250, 15.0));
        when(externalDietService.generateAndSave(eq(userId), any())).thenReturn(List.of(generated));

        mockMvc.perform(post(plansUrl() + "/generate")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("age", 25, "gender", "male", "height", 180, "weight", 75,
                                "goal", "lose_weight", "days", 1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.daily_plans[0].meals[0].meal_name").value("Omlet"));

        mockMvc.perform(get("/rest/api/users/" + userId).header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.isReceived").value(1));
    }

    @Test
    @DisplayName("Generate: when the RAG service fails the API returns 500")
    void generate_whenRagServiceFails_returns500() throws Exception {
        when(externalDietService.generateAndSave(eq(userId), any()))
                .thenThrow(new RuntimeException("RAG service unavailable"));

        mockMvc.perform(post(plansUrl() + "/generate")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("age", 25, "days", 1))))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Generate from personal info without any personal info returns 400")
    void generateFromPersonal_withoutPersonalInfo_returns400() throws Exception {
        mockMvc.perform(post(plansUrl() + "/generate-from-personal").header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());
    }
}
