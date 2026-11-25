package com.emin.nutrition_app.support;

import com.emin.repository.DailyPlanRepository;
import com.emin.repository.UserRepositorty;
import com.emin.services.ExternalDietService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Shared setup for API tests: full Spring context, MockMvc, H2 database,
 * and mocks for the two external dependencies (mail server and the RAG diet service).
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class ApiTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepositorty userRepository;

    /** No real e-mails are sent during tests. */
    @MockitoBean
    protected JavaMailSender mailSender;

    /** The external RAG diet service is never called during tests. */
    @MockitoBean
    protected ExternalDietService externalDietService;

    @Autowired
    protected DailyPlanRepository dailyPlanRepository;

    @BeforeEach
    void cleanDatabase() {
        dailyPlanRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@test.com";
    }

    protected String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    protected Map<String, Object> registerBody(String email, String password) {
        return Map.of(
                "name", "Emin",
                "surname", "Test",
                "email", email,
                "password", password);
    }

    /** Registers a user and returns the response body (token + user). */
    protected JsonNode register(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/rest/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerBody(email, password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
