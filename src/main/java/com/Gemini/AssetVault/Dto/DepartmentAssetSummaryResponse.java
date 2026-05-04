package com.Gemini.AssetVault.Dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Asset count and value grouped by department")
public record DepartmentAssetSummaryResponse(
        @Schema(description = "Department name", example = "Engineering")
        String department,

        @Schema(description = "Active assigned asset count", example = "25")
        long assetCount,

        @Schema(description = "Total purchase value", example = "3500000.00")
        BigDecimal totalValue
) {
}
