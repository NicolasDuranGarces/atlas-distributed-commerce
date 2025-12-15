package com.atlas.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ApiResponseTest {

    @Test
    @DisplayName("Should create success response with data")
    void success_WithData() {
        String data = "test data";
        ApiResponse<String> response = ApiResponse.success(data);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("test data");
        assertThat(response.getMessage()).isNull();
        assertThat(response.getError()).isNull();
    }

    @Test
    @DisplayName("Should create success response with data and message")
    void success_WithDataAndMessage() {
        String data = "test data";
        ApiResponse<String> response = ApiResponse.success(data, "Operation successful");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("test data");
        assertThat(response.getMessage()).isEqualTo("Operation successful");
    }

    @Test
    @DisplayName("Should create error response with code")
    void error_WithMessageAndCode() {
        ApiResponse<Object> response = ApiResponse.error("Not found", "NOT_FOUND");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Not found");
        assertThat(response.getError()).isNotNull();
        assertThat(response.getError().getCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("Should create error response with details")
    void error_WithDetails() {
        ApiResponse<Object> response = ApiResponse.error("Error", "CODE", "Additional details");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError().getDetails()).isEqualTo("Additional details");
    }

    @Test
    @DisplayName("Timestamp should be set automatically")
    void timestamp_IsSetAutomatically() {
        ApiResponse<String> response = ApiResponse.success("data");

        assertThat(response.getTimestamp()).isNotNull();
    }
}
