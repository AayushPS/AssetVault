package com.assetvault.dto;

import com.assetvault.model.enums.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload describing type breakdown data.
 *
 * @param type the requested type value
 * @param count the count value
 */
@Schema(
        description = "Asset count grouped by type",
        example = "{\"type\":\"LAPTOP\",\"count\":42}"
)
public record TypeBreakdownResponse(
        @Schema(description = "Asset type", example = "LAPTOP")
        AssetType type,

        @Schema(description = "Count for this type", example = "42")
        long count
) {
}
