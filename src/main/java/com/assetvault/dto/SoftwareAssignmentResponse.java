package com.assetvault.dto;

import com.assetvault.model.enums.AssignmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(
        description = "Software license assignment details",
        example = "{\"id\":1,\"licenseId\":1,\"softwareName\":\"IntelliJ IDEA\",\"employeeId\":1,\"employeeName\":\"Aarav Mehta\",\"seatIndex\":3,\"assignedDate\":\"2026-04-21\",\"status\":\"ACTIVE\",\"assignedBy\":\"IT Admin\",\"remarks\":\"Seat assigned for backend work\",\"createdAt\":\"2026-04-21T09:45:00\"}"
)
public record SoftwareAssignmentResponse(
        @Schema(description = "Assignment id", example = "1")
        Long id,

        @Schema(description = "License id", example = "1")
        Long licenseId,

        @Schema(description = "Software name", example = "IntelliJ IDEA")
        String softwareName,

        @Schema(description = "Employee id", example = "1")
        Long employeeId,

        @Schema(description = "Employee name", example = "Aarav Mehta")
        String employeeName,

        @Schema(description = "Seat index", example = "3")
        Integer seatIndex,

        @Schema(description = "Assignment date", example = "2026-04-21")
        LocalDate assignedDate,

        @Schema(description = "Returned/revoked date", example = "2026-05-02")
        LocalDate returnedDate,

        @Schema(description = "Assignment status", example = "ACTIVE")
        AssignmentStatus status,

        @Schema(description = "Admin who assigned the seat", example = "IT Admin")
        String assignedBy,

        @Schema(description = "Remarks", example = "Seat assigned for backend work")
        String remarks,

        @Schema(description = "Creation timestamp", example = "2026-04-21T09:45:00")
        LocalDateTime createdAt
) {
}
