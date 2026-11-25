package com.emin.nutrition_app.controller;

import com.emin.entities.User;
import com.emin.nutrition_app.support.ApiTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Auth API: register, authenticate, verify")
class AuthControllerTest extends ApiTestBase {

    // ---------- Register ----------

    @Test
    @DisplayName("Valid registration returns a token, hides the password and sends a verification e-mail")
    void register_validRequest_returnsTokenWithoutPassword() throws Exception {
        String email = uniqueEmail();

        mockMvc.perform(post("/rest/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerBody(email, "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.verified").value(false))
                .andExpect(jsonPath("$.user.role").value("USER"));

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Password is stored as a BCrypt hash, never in plain text")
    void register_storesHashedPassword() throws Exception {
        String email = uniqueEmail();
        register(email, "password123");

        User saved = userRepository.findByEmail(email).orElseThrow();
        assertThat(saved.getPassword()).isNotEqualTo("password123").startsWith("$2");
    }

    @Test
    @DisplayName("Verification code is 6 digits and expires in 15 minutes")
    void register_createsSixDigitCodeValidFor15Minutes() throws Exception {
        String email = uniqueEmail();
        LocalDateTime before = LocalDateTime.now();
        register(email, "password123");

        User saved = userRepository.findByEmail(email).orElseThrow();
        assertThat(saved.getVerificationCode()).matches("\\d{6}");
        assertThat(saved.getVerificationExpiry())
                .isAfter(before.plusMinutes(14))
                .isBefore(LocalDateTime.now().plusMinutes(16));
    }

    @ParameterizedTest(name = "password length {0} chars -> rejected")
    @ValueSource(ints = {0, 1, 7})
    @DisplayName("Boundary value: passwords shorter than 8 characters are rejected")
    void register_shortPassword_returns400(int length) throws Exception {
        mockMvc.perform(post("/rest/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerBody(uniqueEmail(), "a".repeat(length)))))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "password length {0} chars -> accepted")
    @ValueSource(ints = {8, 9, 64})
    @DisplayName("Boundary value: passwords of 8 or more characters are accepted")
    void register_passwordAtOrAboveMinimum_isAccepted(int length) throws Exception {
        mockMvc.perform(post("/rest/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerBody(uniqueEmail(), "a".repeat(length)))))
                .andExpect(status().isOk());
    }

    @ParameterizedTest(name = "e-mail \"{0}\" -> rejected")
    @ValueSource(strings = {"", "not-an-email", "a@", "@test.com"})
    @DisplayName("Invalid e-mail formats are rejected")
    void register_invalidEmail_returns400(String email) throws Exception {
        mockMvc.perform(post("/rest/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerBody(email, "password123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Missing name is rejected")
    void register_missingName_returns400() throws Exception {
        Map<String, Object> body = new HashMap<>(registerBody(uniqueEmail(), "password123"));
        body.remove("name");

        mockMvc.perform(post("/rest/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isBadRequest());
    }

    // ---------- Authenticate ----------

    @Test
    @DisplayName("Correct credentials return a token")
    void authenticate_correctCredentials_returnsToken() throws Exception {
        String email = uniqueEmail();
        register(email, "password123");

        mockMvc.perform(post("/rest/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    @DisplayName("Wrong password is rejected")
    void authenticate_wrongPassword_isRejected() throws Exception {
        String email = uniqueEmail();
        register(email, "password123");

        mockMvc.perform(post("/rest/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "wrong-password"))))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Unknown user is rejected")
    void authenticate_unknownUser_isRejected() throws Exception {
        mockMvc.perform(post("/rest/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", uniqueEmail(), "password", "password123"))))
                .andExpect(status().is4xxClientError());
    }

    // ---------- Verify ----------

    @Test
    @DisplayName("Correct verification code verifies the account and clears the code")
    void verify_correctCode_verifiesUser() throws Exception {
        String email = uniqueEmail();
        register(email, "password123");
        String code = userRepository.findByEmail(email).orElseThrow().getVerificationCode();

        mockMvc.perform(post("/rest/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "code", code))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());

        User saved = userRepository.findByEmail(email).orElseThrow();
        assertThat(saved.getVerified()).isTrue();
        assertThat(saved.getVerificationCode()).isNull();
    }

    @Test
    @DisplayName("Wrong verification code is rejected and the account stays unverified")
    void verify_wrongCode_isRejected() throws Exception {
        String email = uniqueEmail();
        register(email, "password123");
        String code = userRepository.findByEmail(email).orElseThrow().getVerificationCode();
        String wrongCode = code.equals("000000") ? "111111" : "000000";

        mockMvc.perform(post("/rest/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "code", wrongCode))))
                .andExpect(status().isNotFound());

        assertThat(userRepository.findByEmail(email).orElseThrow().getVerified()).isFalse();
    }

    @Test
    @DisplayName("A verification code cannot be used twice")
    void verify_sameCodeTwice_secondAttemptRejected() throws Exception {
        String email = uniqueEmail();
        register(email, "password123");
        String code = userRepository.findByEmail(email).orElseThrow().getVerificationCode();
        String body = json(Map.of("email", email, "code", code));

        mockMvc.perform(post("/rest/api/auth/verify").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mockMvc.perform(post("/rest/api/auth/verify").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Expired verification code is rejected with 400")
    void verify_expiredCode_isRejected() throws Exception {
        String email = uniqueEmail();
        register(email, "password123");
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setVerificationExpiry(LocalDateTime.now().minusSeconds(1));
        userRepository.save(user);

        mockMvc.perform(post("/rest/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "code", user.getVerificationCode()))))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findByEmail(email).orElseThrow().getVerified()).isFalse();
    }

    // ---------- Protected endpoints ----------

    @Test
    @DisplayName("Protected endpoint without a token is rejected")
    void protectedEndpoint_withoutToken_isRejected() throws Exception {
        mockMvc.perform(get("/rest/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Protected endpoint with a tampered token is rejected")
    void protectedEndpoint_withTamperedToken_isRejected() throws Exception {
        JsonNode auth = register(uniqueEmail(), "password123");
        String token = auth.get("token").asText();
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

        mockMvc.perform(get("/rest/api/users").header("Authorization", bearer(tampered)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Protected endpoint with a valid token is allowed")
    void protectedEndpoint_withValidToken_isAllowed() throws Exception {
        JsonNode auth = register(uniqueEmail(), "password123");

        mockMvc.perform(get("/rest/api/users/" + auth.at("/user/id").asText())
                        .header("Authorization", bearer(auth.get("token").asText())))
                .andExpect(status().isOk());
    }
}
