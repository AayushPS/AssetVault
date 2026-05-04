package com.Gemini.AssetVault.Dto;

import com.Gemini.AssetVault.Model.Enum.AssetStatus;
import com.Gemini.AssetVault.Model.Enum.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Asset details returned by the API")
public record AssetResponse(
        @Schema(description = "Asset database id", example = "1")
        Long id,

        @Schema(description = "Generated unique asset code", example = "LPT-00001")
        String assetCode,

        @Schema(description = "Display name", example = "MacBook Pro 14")
        String name,

        @Schema(description = "Brand", example = "Apple")
        String brand,

        @Schema(description = "Model", example = "M3 Pro")
        String model,

        @Schema(description = "Asset type", example = "LAPTOP")
        AssetType type,

        @Schema(description = "Current lifecycle status", example = "ASSIGNED")
        AssetStatus status,

        @Schema(description = "Purchase date", example = "2026-04-01")
        LocalDate purchaseDate,

        @Schema(description = "Purchase cost", example = "149999.00")
        BigDecimal purchaseCost,

        @Schema(description = "Warranty expiry date", example = "2029-04-01")
        LocalDate warrantyExpiryDate,

        @Schema(description = "Unique manufacturer serial number", example = "C02ZK1ABCD")
        String serialNumber,

        @Schema(description = "Physical location", example = "Bengaluru HQ")
        String location,

        @Schema(description = "Internal notes", example = "Procured for engineering refresh")
        String notes,

        @Schema(description = "Creation timestamp", example = "2026-04-21T09:45:00")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp", example = "2026-04-22T10:30:00")
        LocalDateTime updatedAt
) {
}
