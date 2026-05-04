package com.Gemini.AssetVault.Dto;

import com.Gemini.AssetVault.Model.Enum.AssetStatus;
import com.Gemini.AssetVault.Model.Enum.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request payload for creating or updating an asset")
public record AssetRequest(
        @Schema(description = "Display name of the asset", example = "MacBook Pro 14")
        @NotBlank
        @Size(max = 100)
        String name,

        @Schema(description = "Asset brand", example = "Apple")
        @NotBlank
        @Size(max = 50)
        String brand,

        @Schema(description = "Asset model", example = "M3 Pro")
        @NotBlank
        @Size(max = 50)
        String model,

        @Schema(description = "Asset type", example = "LAPTOP")
        @NotNull
        AssetType type,

        @Schema(description = "Asset lifecycle status. Defaults to AVAILABLE on create.", example = "AVAILABLE")
        AssetStatus status,

        @Schema(description = "Purchase date", example = "2026-04-01")
        @NotNull
        @PastOrPresent
        LocalDate purchaseDate,

        @Schema(description = "Purchase cost", example = "149999.00")
        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal purchaseCost,

        @Schema(description = "Warranty expiry date", example = "2029-04-01")
        LocalDate warrantyExpiryDate,

        @Schema(description = "Unique manufacturer serial number", example = "C02ZK1ABCD")
        @NotBlank
        @Size(max = 100)
        String serialNumber,

        @Schema(description = "Current physical location", example = "Bengaluru HQ")
        @Size(max = 100)
        String location,

        @Schema(description = "Internal notes", example = "Procured for engineering refresh")
        String notes
) {
}
