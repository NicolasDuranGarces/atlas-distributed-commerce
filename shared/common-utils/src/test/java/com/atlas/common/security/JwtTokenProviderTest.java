package com.atlas.common.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // Set a valid secret key (256+ bits for HS256)
        String secret = "atlas-super-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm";
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshExpiration", 604800000L); // 7 days
    }

    @Test
    @DisplayName("Should generate valid access token")
    void generateToken_Success() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String role = "USER";

        String token = jwtTokenProvider.generateToken(userId, email, role);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should generate valid refresh token")
    void generateRefreshToken_Success() {
        UUID userId = UUID.randomUUID();

        String token = jwtTokenProvider.generateRefreshToken(userId);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should extract user ID from token")
    void getUserIdFromToken_Success() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId, "test@example.com", "USER");

        UUID extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should extract email from token")
    void getEmailFromToken_Success() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String token = jwtTokenProvider.generateToken(userId, email, "USER");

        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("Should extract role from token")
    void getRoleFromToken_Success() {
        UUID userId = UUID.randomUUID();
        String role = "ADMIN";
        String token = jwtTokenProvider.generateToken(userId, "test@example.com", role);

        String extractedRole = jwtTokenProvider.getRoleFromToken(token);

        assertThat(extractedRole).isEqualTo(role);
    }

    @Test
    @DisplayName("Should validate correct token")
    void validateToken_ValidToken_ReturnsTrue() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId, "test@example.com", "USER");

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should invalidate malformed token")
    void validateToken_MalformedToken_ReturnsFalse() {
        boolean isValid = jwtTokenProvider.validateToken("invalid-token");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should invalidate null token")
    void validateToken_NullToken_ReturnsFalse() {
        boolean isValid = jwtTokenProvider.validateToken(null);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should invalidate empty token")
    void validateToken_EmptyToken_ReturnsFalse() {
        boolean isValid = jwtTokenProvider.validateToken("");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should check if token is not expired")
    void isTokenExpired_ValidToken_ReturnsFalse() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId, "test@example.com", "USER");

        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        assertThat(isExpired).isFalse();
    }
}
