package com.assetvault.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        description = "Request payload for creating or updating an employee",
        example = "{\"name\":\"Aarav Mehta\",\"email\":\"aarav.mehta@example.com\",\"department\":\"Engineering\",\"designation\":\"Senior Engineer\",\"employeeCode\":\"EMP-00001\"}"
)
public record EmployeeRequest(
        @Schema(description = "Employee full name", example = "Aarav Mehta")
        @NotBlank
        @Size(max = 30)
        String name,

        @Schema(description = "Employee email address", example = "aarav.mehta@example.com")
        @NotBlank
        @Email
        @Size(max = 30)
        String email,

        @Schema(description = "Department name", example = "Engineering")
        @NotBlank
        @Size(max = 15)
        String department,

        @Schema(description = "Employee designation", example = "Senior Engineer")
        @NotBlank
        @Size(max = 30)
        String designation,

        @Schema(description = "Optional employee code. If omitted, AssetVault generates one.", example = "EMP-00001")
        @Size(max = 30)
        String employeeCode
) {
}
