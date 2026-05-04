package com.Gemini.AssetVault.Dto;

import com.Gemini.AssetVault.Model.Enum.LicenseType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request payload for creating or updating a software license")
public record SoftwareLicenseRequest(
        @Schema(description = "Software name", example = "IntelliJ IDEA")
        @NotBlank
        @Size(max = 100)
        String softwareName,

        @Schema(description = "Unique license key", example = "LIC-IDEA-2026-001")
        @NotBlank
        @Size(max = 100)
        String licenseKey,

        @Schema(description = "License type", example = "FLOATING")
        @NotNull
        LicenseType licenseType,

        @Schema(description = "Vendor name", example = "JetBrains")
        @NotBlank
        @Size(max = 100)
        String vendor,

        @Schema(description = "Total available seats", example = "25")
        @NotNull
        @Min(1)
        Integer totalSeats,

        @Schema(description = "Used seats. Defaults to zero on create.", example = "0")
        @Min(0)
        Integer usedSeats,

        @Schema(description = "Purchase date", example = "2026-04-01")
        @NotNull
        @PastOrPresent
        LocalDate purchaseDate,

        @Schema(description = "Purchase cost", example = "250000.00")
        @DecimalMin(value = "0.00")
        BigDecimal purchaseCost,

        @Schema(description = "Expiry date", example = "2027-04-01")
        LocalDate expiryDate,

        @Schema(description = "Whether the license can be assigned", example = "true")
        Boolean active
) {
}
