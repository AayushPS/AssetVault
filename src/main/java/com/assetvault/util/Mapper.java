package com.assetvault.util;

import com.assetvault.dto.AssetAssignmentResponse;
import com.assetvault.dto.AssetResponse;
import com.assetvault.dto.EmployeeResponse;
import com.assetvault.dto.MaintenanceRecordResponse;
import com.assetvault.dto.SoftwareAssignmentResponse;
import com.assetvault.dto.SoftwareLicenseResponse;
import com.assetvault.model.Asset;
import com.assetvault.model.AssetAssignment;
import com.assetvault.model.Employee;
import com.assetvault.model.MaintenanceRecord;
import com.assetvault.model.SoftwareAssignment;
import com.assetvault.model.SoftwareLicense;

import java.util.List;

/**
 * Utility methods for mapper.
 */
public final class Mapper {
    /**
     * Prevents instantiation of this utility class.
     */
    private Mapper() {
    }

    /**
     * Maps the supplied domain object to its response representation.
     *
     * @param asset the asset entity to map or validate
     * @return the mapped response payload
     */
    public static AssetResponse toAssetResponse(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getAssetCode(),
                asset.getName(),
                asset.getBrand(),
                asset.getModel(),
                asset.getType(),
                asset.getStatus(),
                asset.getPurchaseDate(),
                asset.getPurchaseCost(),
                asset.getWarrantyExpiryDate(),
                asset.getSerialNumber(),
                asset.getLocation(),
                asset.getNotes(),
                asset.getCreatedAt(),
                asset.getUpdatedAt()
        );
    }

    /**
     * Maps the supplied domain object to its response representation.
     *
     * @param employee the employee entity to map or validate
     * @return the mapped response payload
     */
    public static EmployeeResponse toEmployeeResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getDesignation(),
                employee.getEmployeeCode(),
                employee.isActive(),
                employee.getCreatedAt()
        );
    }

    /**
     * Maps the supplied domain object to its response representation.
     *
     * @param assignment the assignment entity to map or update
     * @return the mapped response payload
     */
    public static AssetAssignmentResponse toAssetAssignmentResponse(AssetAssignment assignment) {
        return new AssetAssignmentResponse(
                assignment.getId(),
                assignment.getAsset().getId(),
                assignment.getAsset().getAssetCode(),
                assignment.getAsset().getName(),
                assignment.getEmployee().getId(),
                assignment.getEmployee().getName(),
                assignment.getAssignedDate(),
                assignment.getReturnedDate(),
                assignment.getStatus(),
                assignment.getAssignedBy(),
                assignment.getRemarks(),
                assignment.getCreatedAt()
        );
    }

    /**
     * Maps the supplied domain object to its response representation.
     *
     * @param record the maintenance record to map or update
     * @return the mapped response payload
     */
    public static MaintenanceRecordResponse toMaintenanceResponse(MaintenanceRecord record) {
        return new MaintenanceRecordResponse(
                record.getId(),
                record.getAsset().getId(),
                record.getAsset().getAssetCode(),
                record.getAsset().getName(),
                record.getMaintenanceType(),
                record.getDescription(),
                record.getMaintenanceCost(),
                record.getVendor(),
                record.getScheduledDate(),
                record.getCompletedDate(),
                record.getStatus(),
                record.getCreatedAt()
        );
    }

    /**
     * Maps the supplied domain object to its response representation.
     *
     * @param license the software license entity to map or update
     * @param assignedEmployees the assigned employees value
     * @return the mapped response payload
     */
    public static SoftwareLicenseResponse toSoftwareLicenseResponse(
            SoftwareLicense license,
            List<EmployeeResponse> assignedEmployees
    ) {
        int usedSeats = license.getUsedSeats() == null ? 0 : license.getUsedSeats();
        int totalSeats = license.getTotalSeats() == null ? 0 : license.getTotalSeats();
        return new SoftwareLicenseResponse(
                license.getId(),
                license.getSoftwareName(),
                license.getLicenceKey(),
                license.getLicenseType(),
                license.getVendor(),
                license.getTotalSeats(),
                license.getUsedSeats(),
                Math.max(totalSeats - usedSeats, 0),
                license.getPurchaseDate(),
                license.getPurchaseCost(),
                license.getExpiryDate(),
                license.getIsActive(),
                assignedEmployees,
                license.getCreatedAt(),
                license.getUpdatedAt()
        );
    }

    /**
     * Maps the supplied domain object to its response representation.
     *
     * @param assignment the assignment entity to map or update
     * @return the mapped response payload
     */
    public static SoftwareAssignmentResponse toSoftwareAssignmentResponse(SoftwareAssignment assignment) {
        return new SoftwareAssignmentResponse(
                assignment.getId(),
                assignment.getSoftwareLicense().getId(),
                assignment.getSoftwareLicense().getSoftwareName(),
                assignment.getEmployee().getId(),
                assignment.getEmployee().getName(),
                assignment.getSeatIndex(),
                assignment.getAssignedDate(),
                assignment.getReturnedDate(),
                assignment.getStatus(),
                assignment.getAssignedBy(),
                assignment.getRemarks(),
                assignment.getCreatedAt()
        );
    }
}
