package com.assetvault.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response payload describing license alerts data.
 */
@Schema(
        description = "License alerts for expiring or exhausted licenses",
        example = "{\"expiringSoon\":[{\"id\":1,\"softwareName\":\"IntelliJ IDEA\",\"licenseKey\":\"LIC-IDEA-2026-001\",\"licenseType\":\"FLOATING\",\"vendor\":\"JetBrains\",\"totalSeats\":25,\"usedSeats\":7,\"remainingSeats\":18,\"expiryDate\":\"2027-04-01\",\"active\":true}],\"seatsExhausted\":[{\"id\":2,\"softwareName\":\"Adobe Creative Cloud\",\"licenseKey\":\"LIC-ADOBE-2026-001\",\"licenseType\":\"FLOATING\",\"vendor\":\"Adobe\",\"totalSeats\":5,\"usedSeats\":5,\"remainingSeats\":0,\"expiryDate\":\"2027-03-01\",\"active\":true}]}"
)
public record LicenseAlertsResponse(
        @Schema(
                description = "Licenses expiring soon",
                example = "[{\"id\":1,\"softwareName\":\"IntelliJ IDEA\",\"remainingSeats\":18,\"expiryDate\":\"2027-04-01\"}]"
        )
        List<SoftwareLicenseResponse> expiringSoon,

        @Schema(
                description = "Licenses with no remaining seats",
                example = "[{\"id\":2,\"softwareName\":\"Adobe Creative Cloud\",\"remainingSeats\":0,\"expiryDate\":\"2027-03-01\"}]"
        )
        List<SoftwareLicenseResponse> seatsExhausted
) {
}
