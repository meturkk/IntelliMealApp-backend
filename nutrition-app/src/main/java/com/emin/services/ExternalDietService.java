package com.emin.services;

import com.emin.dto.DtoDailyPlan;
import com.emin.dto.ExternalDietRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ExternalDietService {

    private static final String DIET_API_URL = "https://diet-ai-rag.onrender.com/generate-diet-plan";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DailyPlanService dailyPlanService;

    public List<DtoDailyPlan> generateAndSave(String userId, ExternalDietRequest request) throws Exception {
        List<DtoDailyPlan> plans = generate(request);
        return dailyPlanService.createDailyPlans(userId, plans);
    }

    public List<DtoDailyPlan> generate(ExternalDietRequest request) throws Exception {
        String response = restTemplate.postForObject(DIET_API_URL, request, String.class);
        JsonNode root = objectMapper.readTree(response);

        JsonNode arrayNode = null;
        if (root.isArray()) {
            arrayNode = root;
        } else if (root.has("daily_plans")) {
            arrayNode = root.get("daily_plans");
        } else if (root.has("dailyPlans")) {
            arrayNode = root.get("dailyPlans");
        } else if (root.has("plan")) {
            arrayNode = root.get("plan");
        } else if (root.has("data")) {
            JsonNode data = root.get("data");
            if (data.isArray()) arrayNode = data;
        }

        if (arrayNode == null || !arrayNode.isArray()) {
            throw new RuntimeException("Unexpected response format from diet API");
        }

        return objectMapper.convertValue(arrayNode, new TypeReference<List<DtoDailyPlan>>(){});
    }
}
