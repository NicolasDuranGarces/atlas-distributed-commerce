package com.atlas.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("Should create user with builder")
    void createUser_WithBuilder() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id)
                .email("test@example.com")
                .password("password")
                .firstName("John")
                .lastName("Doe")
                .role(Role.USER)
                .enabled(true)
                .build();

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should return full name")
    void getFullName() {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        assertThat(user.getFullName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Default values should be set correctly")
    void defaultValues() {
        User user = User.builder()
                .email("test@example.com")
                .password("password")
                .build();

        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isAccountLocked()).isFalse();
        assertThat(user.getFailedLoginAttempts()).isZero();
    }
}
