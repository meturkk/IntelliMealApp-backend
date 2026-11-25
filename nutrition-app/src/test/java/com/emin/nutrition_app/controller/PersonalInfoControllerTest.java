package com.emin.nutrition_app.controller;

import com.emin.nutrition_app.support.ApiTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Personal info API: CRUD")
class PersonalInfoControllerTest extends ApiTestBase {

    private String userId;
    private String token;

    @BeforeEach
    void createUser() throws Exception {
        JsonNode auth = register(uniqueEmail(), "password123");
        userId = auth.at("/user/id").asText();
        token = auth.get("token").asText();
    }

    private String infoUrl() {
        return "/rest/api/users/" + userId + "/personal-info";
    }

    private Map<String, Object> info(double weight) {
        Map<String, Object> body = new HashMap<>();
        body.put("age", 25);
        body.put("gender", "male");
        body.put("height", 180);
        body.put("weight", weight);
        body.put("goal", "lose_weight");
        body.put("allergens", "peanut, milk");
        return body;
    }

    private String createInfo(double weight) throws Exception {
        MvcResult result = mockMvc.perform(post(infoUrl())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(info(weight))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    @DisplayName("Created record is linked to the user and gets a server-side date")
    void create_linksToUserAndSetsDate() throws Exception {
        mockMvc.perform(post(infoUrl())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(info(80.5))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.weight").value(80.5))
                .andExpect(jsonPath("$.date").isNotEmpty());
    }

    @Test
    @DisplayName("Measurement history: all records are listed")
    void list_returnsAllRecords() throws Exception {
        createInfo(82.0);
        createInfo(80.0);

        mockMvc.perform(get(infoUrl()).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("A single record can be read, updated and deleted")
    void readUpdateDelete() throws Exception {
        String infoId = createInfo(82.0);

        mockMvc.perform(get(infoUrl() + "/" + infoId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(82.0));

        mockMvc.perform(put(infoUrl() + "/" + infoId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(info(79.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(79.0));

        mockMvc.perform(delete(infoUrl() + "/" + infoId).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(infoUrl()).header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Reading a record that does not exist returns 404")
    void read_unknownRecord_returns404() throws Exception {
        mockMvc.perform(get(infoUrl() + "/does-not-exist").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Creating a record for a user that does not exist returns 404")
    void create_unknownUser_returns404() throws Exception {
        mockMvc.perform(post("/rest/api/users/does-not-exist/personal-info")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(info(80.0))))
                .andExpect(status().isNotFound());
    }
}
