package com.atlas.user.controller;

import com.atlas.user.dto.*;
import com.atlas.user.entity.Role;
import com.atlas.user.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();

        userResponse = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.USER)
                .build();

        authResponse = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(userResponse)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password123!");
    }

    @Test
    @DisplayName("POST /api/auth/register - Success")
    @WithMockUser
    void register_Success() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Success")
    @WithMockUser
    void login_Success() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Success")
    @WithMockUser
    void refreshToken_Success() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("GET /api/auth/me - Success")
    @WithMockUser
    void getCurrentUser_Success() throws Exception {
        when(authService.getCurrentUser(any(UUID.class))).thenReturn(userResponse);

        mockMvc.perform(get("/api/auth/me")
                        .header("X-User-Id", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Validation Error")
    @WithMockUser
    void register_ValidationError() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("short");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
