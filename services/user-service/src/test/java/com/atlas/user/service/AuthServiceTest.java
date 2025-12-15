package com.atlas.user.service;

import com.atlas.common.exception.AuthenticationException;
import com.atlas.common.exception.BusinessException;
import com.atlas.common.security.JwtTokenProvider;
import com.atlas.user.dto.*;
import com.atlas.user.entity.Role;
import com.atlas.user.entity.User;
import com.atlas.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(Role.USER)
                .enabled(true)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Doe");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password123!");
    }

    @Test
    @DisplayName("Should register new user successfully")
    void register_Success() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(jwtTokenProvider.generateToken(any(UUID.class), anyString(), anyString()))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any(UUID.class)))
                .thenReturn("refresh-token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);

        // When
        AuthResponse response = authService.register(registerRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser().getEmail()).isEqualTo("new@example.com");

        verify(userRepository).save(any(User.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), anyMap());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void register_EmailExists_ThrowsException() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void login_Success() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(UUID.class), anyString(), anyString()))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any(UUID.class)))
                .thenReturn("refresh-token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);

        // When
        AuthResponse response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");

        verify(userRepository).updateLastLogin(any(UUID.class), any());
        verify(userRepository).resetFailedAttempts(any(UUID.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void login_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Should throw exception when account is locked")
    void login_AccountLocked_ThrowsException() {
        // Given
        testUser.setAccountLocked(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // When/Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Account is locked");
    }

    @Test
    @DisplayName("Should throw exception when account is disabled")
    void login_AccountDisabled_ThrowsException() {
        // Given
        testUser.setEnabled(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // When/Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Account is disabled");
    }

    @Test
    @DisplayName("Should increment failed attempts on wrong password")
    void login_WrongPassword_IncrementsFailedAttempts() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthenticationException.class);

        verify(userRepository).incrementFailedAttempts(testUser.getId());
    }

    @Test
    @DisplayName("Should lock account after max failed attempts")
    void login_MaxFailedAttempts_LocksAccount() {
        // Given
        testUser.setFailedLoginAttempts(4); // One more will trigger lock
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthenticationException.class);

        verify(userRepository).lockAccount(testUser.getId());
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void refreshToken_Success() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        when(jwtTokenProvider.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("valid-refresh-token"))
                .thenReturn(testUser.getId());
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(any(UUID.class), anyString(), anyString()))
                .thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(any(UUID.class)))
                .thenReturn("new-refresh-token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);

        // When
        AuthResponse response = authService.refreshToken(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    @DisplayName("Should throw exception for invalid refresh token")
    void refreshToken_InvalidToken_ThrowsException() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-token");

        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    @DisplayName("Should get current user successfully")
    void getCurrentUser_Success() {
        // Given
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // When
        UserResponse response = authService.getCurrentUser(testUser.getId());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
    }
}
