package com.assetvault.dto;

import com.assetvault.model.enums.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;

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
