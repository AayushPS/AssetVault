package com.assetvault.dto;

import com.assetvault.model.enums.AssignmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response payload describing asset assignment data.
 *
 * @param id the database identifier
 * @param assetId the asset identifier
 * @param assetCode the generated asset code
 * @param assetName the asset name value
 * @param employeeId the employee identifier
 * @param employeeName the employee name value
 * @param assignedDate the assigned date value
 * @param returnedDate the returned date value
 * @param status the requested status value
 * @param assignedBy the assigned by value
 * @param remarks the reason captured for the state change
 * @param createdAt the created at value
 */
@Schema(
        description = "Asset assignment details returned by the API",
        example = "{\"id\":1,\"assetId\":1,\"assetCode\":\"LPT-00001\",\"assetName\":\"MacBook Pro 14\",\"employeeId\":1,\"employeeName\":\"Aarav Mehta\",\"assignedDate\":\"2026-04-21\",\"status\":\"ACTIVE\",\"assignedBy\":\"IT Admin\",\"remarks\":\"Issued during onboarding\",\"createdAt\":\"2026-04-21T09:45:00\"}"
)
public record AssetAssignmentResponse(
        @Schema(description = "Assignment id", example = "1")
        Long id,

        @Schema(description = "Assigned asset id", example = "1")
        Long assetId,

        @Schema(description = "Assigned asset code", example = "LPT-00001")
        String assetCode,

        @Schema(description = "Assigned asset name", example = "MacBook Pro 14")
        String assetName,

        @Schema(description = "Employee id", example = "1")
        Long employeeId,

        @Schema(description = "Employee name", example = "Aarav Mehta")
        String employeeName,

        @Schema(description = "Assignment date", example = "2026-04-21")
        LocalDate assignedDate,

        @Schema(description = "Return date", example = "2026-05-02")
        LocalDate returnedDate,

        @Schema(description = "Assignment status", example = "ACTIVE")
        AssignmentStatus status,

        @Schema(description = "Admin who performed assignment", example = "IT Admin")
        String assignedBy,

        @Schema(description = "Remarks", example = "Issued during onboarding")
        String remarks,

        @Schema(description = "Creation timestamp", example = "2026-04-21T09:45:00")
        LocalDateTime createdAt
) {
}
