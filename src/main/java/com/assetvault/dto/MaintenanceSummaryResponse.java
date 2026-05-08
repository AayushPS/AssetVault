package com.assetvault.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload describing maintenance summary data.
 *
 * @param scheduled the scheduled value
 * @param inProgress the in progress value
 * @param completed the completed value
 */
@Schema(
        description = "Maintenance count summary",
        example = "{\"scheduled\":4,\"inProgress\":2,\"completed\":31}"
)
public record MaintenanceSummaryResponse(
        @Schema(description = "Scheduled maintenance count", example = "4")
        long scheduled,

        @Schema(description = "In-progress maintenance count", example = "2")
        long inProgress,

        @Schema(description = "Completed maintenance count", example = "31")
        long completed
) {
}
