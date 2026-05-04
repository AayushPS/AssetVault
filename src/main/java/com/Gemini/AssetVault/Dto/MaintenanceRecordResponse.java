package com.Gemini.AssetVault.Dto;

import com.Gemini.AssetVault.Model.Enum.MaintenanceStatus;
import com.Gemini.AssetVault.Model.Enum.MaintenanceType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Maintenance record details returned by the API")
public record MaintenanceRecordResponse(
        @Schema(description = "Maintenance record id", example = "1")
        Integer id,

        @Schema(description = "Asset id", example = "1")
        Long assetId,

        @Schema(description = "Asset code", example = "LPT-00001")
        String assetCode,

        @Schema(description = "Asset name", example = "MacBook Pro 14")
        String assetName,

        @Schema(description = "Maintenance type", example = "REPAIR")
        MaintenanceType maintenanceType,

        @Schema(description = "Description", example = "Keyboard replacement")
        String description,

        @Schema(description = "Cost", example = "3500.00")
        BigDecimal maintenanceCost,

        @Schema(description = "Vendor", example = "Apple Service")
        String vendor,

        @Schema(description = "Scheduled date", example = "2026-05-10")
        LocalDate scheduledDate,

        @Schema(description = "Completed date", example = "2026-05-12")
        LocalDate completedDate,

        @Schema(description = "Maintenance status", example = "COMPLETED")
        MaintenanceStatus status,

        @Schema(description = "Creation timestamp", example = "2026-04-21T09:45:00")
        LocalDateTime createdAt
) {
}
