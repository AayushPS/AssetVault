package com.assetvault.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(
        description = "Employee details returned by the API",
        example = "{\"id\":1,\"name\":\"Aarav Mehta\",\"email\":\"aarav.mehta@example.com\",\"department\":\"Engineering\",\"designation\":\"Senior Engineer\",\"employeeCode\":\"EMP-00001\",\"active\":true,\"createdAt\":\"2026-04-21T09:45:00\"}"
)
public record EmployeeResponse(
        @Schema(description = "Employee database id", example = "1")
        Long id,

        @Schema(description = "Employee full name", example = "Aarav Mehta")
        String name,

        @Schema(description = "Employee email address", example = "aarav.mehta@example.com")
        String email,

        @Schema(description = "Department name", example = "Engineering")
        String department,

        @Schema(description = "Employee designation", example = "Senior Engineer")
        String designation,

        @Schema(description = "Employee code", example = "EMP-00001")
        String employeeCode,

        @Schema(description = "Whether employee can receive assignments", example = "true")
        boolean active,

        @Schema(description = "Creation timestamp", example = "2026-04-21T09:45:00")
        LocalDateTime createdAt
) {
}
