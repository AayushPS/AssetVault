package com.assetvault.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(
        description = "Total purchase cost of active assets",
        example = "{\"totalActiveAssetValue\":12345000.00}"
)
public record AssetValueResponse(
        @Schema(description = "Total active asset value", example = "12345000.00")
        BigDecimal totalActiveAssetValue
) {
}
