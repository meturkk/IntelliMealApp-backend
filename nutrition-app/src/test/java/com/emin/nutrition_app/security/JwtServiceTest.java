package com.emin.nutrition_app.security;

import com.emin.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService unit tests (no Spring context)")
class JwtServiceTest {

    private static final String KEY_A = "dGVzdC1vbmx5LWp3dC1rZXktZm9yLWludGVsbGltZWFsLXRlc3RzLTEyMzQ1Njc4OTA=";
    private static final String KEY_B = "YW5vdGhlci10ZXN0LWtleS1mb3ItaW50ZWxsaW1lYWwtdGVzdHMtMDk4NzY1NDMyMQ==";

    private final JwtService jwtService = new JwtService(KEY_A);
    private final UserDetails emin = User.withUsername("emin@test.com").password("x").authorities(List.of()).build();
    private final UserDetails other = User.withUsername("other@test.com").password("x").authorities(List.of()).build();

    @Test
    @DisplayName("Token subject is the user's e-mail")
    void generatedToken_containsUsername() {
        String token = jwtService.generateToken(emin);
        assertThat(jwtService.extractUsername(token)).isEqualTo("emin@test.com");
    }

    @Test
    @DisplayName("Token is valid for its own user")
    void token_isValidForSameUser() {
        String token = jwtService.generateToken(emin);
        assertThat(jwtService.isTokenValid(token, emin)).isTrue();
    }

    @Test
    @DisplayName("Token is not valid for a different user")
    void token_isNotValidForOtherUser() {
        String token = jwtService.generateToken(emin);
        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    @DisplayName("Token signed with a different key is rejected")
    void tokenSignedWithOtherKey_isRejected() {
        String foreignToken = new JwtService(KEY_B).generateToken(emin);
        assertThatThrownBy(() -> jwtService.extractUsername(foreignToken))
                .isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
    }

    @Test
    @DisplayName("Tampered token is rejected")
    void tamperedToken_isRejected() {
        String token = jwtService.generateToken(emin);
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + "x." + parts[2];
        assertThatThrownBy(() -> jwtService.extractUsername(tampered))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Missing JWT secret fails fast at startup")
    void missingSecret_failsFast(String secret) {
        assertThatThrownBy(() -> new JwtService(secret))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }
}
