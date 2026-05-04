package com.Gemini.AssetVault.Exception;

import com.Gemini.AssetVault.Dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void unexpectedExceptionsHideInternalDetailsBehindStableMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/assets");

        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(
                new IllegalStateException("database password was bad"),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().message()).isEqualTo("Unexpected server error");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/assets");
        assertThat(response.getBody().fieldErrors()).isNull();
    }
}
