package com.assetvault.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload describing dashboard summary data.
 *
 * @param totalAssets the total assets value
 * @param assignedAssets the assigned assets value
 * @param availableAssets the available assets value
 * @param underMaintenanceAssets the under maintenance assets value
 * @param retiredAssets the retired assets value
 */
@Schema(
        description = "Dashboard summary counts",
        example = "{\"totalAssets\":120,\"assignedAssets\":80,\"availableAssets\":25,\"underMaintenanceAssets\":8,\"retiredAssets\":7}"
)
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
