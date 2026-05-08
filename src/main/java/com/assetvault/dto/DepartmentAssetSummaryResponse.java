package com.assetvault.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Response payload describing department asset summary data.
 *
 * @param department the department name
 * @param assetCount the asset count value
 * @param totalValue the total value value
 */
@Schema(
        description = "Asset count and value grouped by department",
        example = "{\"department\":\"Engineering\",\"assetCount\":25,\"totalValue\":3500000.00}"
)
public record DepartmentAssetSummaryResponse(
        @Schema(description = "Department name", example = "Engineering")
        String department,

        @Schema(description = "Active assigned asset count", example = "25")
        long assetCount,

        @Schema(description = "Total purchase value", example = "3500000.00")
        BigDecimal totalValue
) {
}
