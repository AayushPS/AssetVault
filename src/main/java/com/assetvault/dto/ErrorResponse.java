package com.assetvault.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(
        description = "Standard API error response",
        example = "{\"timestamp\":\"2026-04-21T09:45:00\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Asset LPT-00001 is currently ASSIGNED\",\"path\":\"/api/v1/assignments\"}"
)
public record ErrorResponse(
        @Schema(description = "Error timestamp", example = "2026-04-21T09:45:00")
        LocalDateTime timestamp,

        @Schema(description = "HTTP status code", example = "409")
        int status,

        @Schema(description = "HTTP status reason", example = "Conflict")
        String error,

        @Schema(description = "Human-readable error message", example = "Asset LPT-00001 is currently ASSIGNED")
        String message,

        @Schema(description = "Request path", example = "/api/v1/assignments")
        String path,

        @Schema(description = "Field validation errors", example = "{\"name\":\"must not be blank\"}")
        Map<String, String> fieldErrors
) {
}
