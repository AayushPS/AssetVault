package com.Gemini.AssetVault.Dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dashboard summary counts")
public record DashboardSummaryResponse(
        @Schema(description = "Total assets", example = "120")
        long totalAssets,

        @Schema(description = "Assigned assets", example = "80")
        long assignedAssets,

        @Schema(description = "Available assets", example = "25")
        long availableAssets,

        @Schema(description = "Assets under maintenance", example = "8")
        long underMaintenanceAssets,

        @Schema(description = "Retired assets", example = "7")
        long retiredAssets
) {
}
