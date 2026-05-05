package com.assetvault.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(
        description = "Request payload for assigning an asset",
        example = "{\"assetId\":1,\"employeeId\":1,\"assignedDate\":\"2026-04-21\",\"assignedBy\":\"IT Admin\",\"remarks\":\"Issued during onboarding\"}"
)
public record AssetAssignmentRequest(
        @Schema(description = "Asset id to assign", example = "1")
        @NotNull
        Long assetId,

        @Schema(description = "Employee id receiving the asset", example = "1")
        @NotNull
        Long employeeId,

        @Schema(description = "Assignment date. Defaults to today when omitted.", example = "2026-04-21")
        LocalDate assignedDate,

        @Schema(description = "Admin who assigned the asset", example = "IT Admin")
        @NotNull
        @Size(max = 30)
        String assignedBy,

        @Schema(description = "Assignment remarks", example = "Issued during onboarding")
        String remarks
) {
}
