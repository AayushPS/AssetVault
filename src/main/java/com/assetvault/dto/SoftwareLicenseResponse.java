package com.assetvault.dto;

import com.assetvault.model.enums.LicenseType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(
        description = "Software license details returned by the API",
        example = "{\"id\":1,\"softwareName\":\"IntelliJ IDEA\",\"licenseKey\":\"LIC-IDEA-2026-001\",\"licenseType\":\"FLOATING\",\"vendor\":\"JetBrains\",\"totalSeats\":25,\"usedSeats\":7,\"remainingSeats\":18,\"purchaseDate\":\"2026-04-01\",\"purchaseCost\":250000.00,\"expiryDate\":\"2027-04-01\",\"active\":true,\"assignedEmployees\":[{\"id\":1,\"name\":\"Aarav Mehta\",\"email\":\"aarav.mehta@example.com\",\"department\":\"Engineering\",\"designation\":\"Senior Engineer\",\"employeeCode\":\"EMP-00001\",\"active\":true}],\"createdAt\":\"2026-04-21T09:45:00\",\"updatedAt\":\"2026-04-22T10:30:00\"}"
)
public record SoftwareLicenseResponse(
        @Schema(description = "License database id", example = "1")
        Long id,

        @Schema(description = "Software name", example = "IntelliJ IDEA")
        String softwareName,

        @Schema(description = "License key", example = "LIC-IDEA-2026-001")
        String licenseKey,

        @Schema(description = "License type", example = "FLOATING")
        LicenseType licenseType,

        @Schema(description = "Vendor name", example = "JetBrains")
        String vendor,

        @Schema(description = "Total seats", example = "25")
        Integer totalSeats,

        @Schema(description = "Used seats", example = "7")
        Integer usedSeats,

        @Schema(description = "Remaining seats", example = "18")
        Integer remainingSeats,

        @Schema(description = "Purchase date", example = "2026-04-01")
        LocalDate purchaseDate,

        @Schema(description = "Purchase cost", example = "250000.00")
        BigDecimal purchaseCost,

        @Schema(description = "Expiry date", example = "2027-04-01")
        LocalDate expiryDate,

        @Schema(description = "Whether the license is active", example = "true")
        Boolean active,

        @Schema(
                description = "Employees with active seats",
                example = "[{\"id\":1,\"name\":\"Aarav Mehta\",\"email\":\"aarav.mehta@example.com\",\"department\":\"Engineering\",\"designation\":\"Senior Engineer\",\"employeeCode\":\"EMP-00001\",\"active\":true}]"
        )
        List<EmployeeResponse> assignedEmployees,

        @Schema(description = "Creation timestamp", example = "2026-04-21T09:45:00")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp", example = "2026-04-22T10:30:00")
        LocalDateTime updatedAt
) {
}
