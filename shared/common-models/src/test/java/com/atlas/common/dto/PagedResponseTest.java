package com.atlas.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PagedResponseTest {

    @Test
    @DisplayName("Should create paged response with static factory")
    void of_Success() {
        List<String> content = List.of("item1", "item2", "item3");
        PagedResponse<String> response = PagedResponse.of(content, 0, 10, 25);

        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getPage()).isZero();
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(25);
        assertThat(response.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should calculate hasNext and hasPrevious for first page")
    void firstPage() {
        PagedResponse<String> firstPage = PagedResponse.of(List.of("item"), 0, 10, 25);

        assertThat(firstPage.isHasNext()).isTrue();
        assertThat(firstPage.isHasPrevious()).isFalse();
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(firstPage.isLast()).isFalse();
    }

    @Test
    @DisplayName("Should calculate hasNext and hasPrevious for middle page")
    void middlePage() {
        PagedResponse<String> middlePage = PagedResponse.of(List.of("item"), 1, 10, 25);

        assertThat(middlePage.isHasNext()).isTrue();
        assertThat(middlePage.isHasPrevious()).isTrue();
        assertThat(middlePage.isFirst()).isFalse();
        assertThat(middlePage.isLast()).isFalse();
    }

    @Test
    @DisplayName("Should calculate hasNext and hasPrevious for last page")
    void lastPage() {
        PagedResponse<String> lastPage = PagedResponse.of(List.of("item"), 2, 10, 25);

        assertThat(lastPage.isHasNext()).isFalse();
        assertThat(lastPage.isHasPrevious()).isTrue();
        assertThat(lastPage.isFirst()).isFalse();
        assertThat(lastPage.isLast()).isTrue();
    }

    @Test
    @DisplayName("Should handle empty response")
    void emptyResponse() {
        PagedResponse<String> emptyResponse = PagedResponse.empty(0, 10);

        assertThat(emptyResponse.getContent()).isEmpty();
        assertThat(emptyResponse.getTotalElements()).isZero();
        assertThat(emptyResponse.getTotalPages()).isZero();
        assertThat(emptyResponse.isFirst()).isTrue();
        assertThat(emptyResponse.isLast()).isTrue();
    }
}
