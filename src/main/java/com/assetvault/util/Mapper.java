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

public final class Mapper {
    private Mapper() {
    }

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
