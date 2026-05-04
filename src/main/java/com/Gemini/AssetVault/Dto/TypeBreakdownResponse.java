package com.Gemini.AssetVault.Dto;

import com.Gemini.AssetVault.Model.Enum.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Asset count grouped by type")
public record TypeBreakdownResponse(
        @Schema(description = "Asset type", example = "LAPTOP")
        AssetType type,

        @Schema(description = "Count for this type", example = "42")
        long count
) {
}
