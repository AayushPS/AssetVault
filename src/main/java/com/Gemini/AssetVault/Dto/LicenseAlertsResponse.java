package com.Gemini.AssetVault.Dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "License alerts for expiring or exhausted licenses")
public record LicenseAlertsResponse(
        @Schema(description = "Licenses expiring soon")
        List<SoftwareLicenseResponse> expiringSoon,

        @Schema(description = "Licenses with no remaining seats")
        List<SoftwareLicenseResponse> seatsExhausted
) {
}
