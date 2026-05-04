package com.Gemini.AssetVault.Dto;

import com.Gemini.AssetVault.Model.Enum.MaintenanceStatus;
import com.Gemini.AssetVault.Model.Enum.MaintenanceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request payload for creating or updating maintenance records")
public record MaintenanceRecordRequest(
        @Schema(description = "Asset id under maintenance", example = "1")
        @NotNull
        Long assetId,

        @Schema(description = "Maintenance type", example = "REPAIR")
        @NotNull
        MaintenanceType maintenanceType,

        @Schema(description = "Work description", example = "Keyboard replacement")
        String description,

        @Schema(description = "Maintenance cost", example = "3500.00")
        @DecimalMin(value = "0.00")
        BigDecimal maintenanceCost,

        @Schema(description = "Vendor handling maintenance", example = "Apple Service")
        @Size(max = 100)
        String vendor,

        @Schema(description = "Scheduled date", example = "2026-05-10")
        @NotNull
        LocalDate scheduledDate,

        @Schema(description = "Completed date", example = "2026-05-12")
        LocalDate completedDate,

        @Schema(description = "Maintenance status. Defaults to SCHEDULED on create.", example = "SCHEDULED")
        MaintenanceStatus status
) {
}
