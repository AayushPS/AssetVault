package com.Gemini.AssetVault.Util;

import com.Gemini.AssetVault.Dto.AssetAssignmentResponse;
import com.Gemini.AssetVault.Dto.AssetResponse;
import com.Gemini.AssetVault.Dto.EmployeeResponse;
import com.Gemini.AssetVault.Dto.MaintenanceRecordResponse;
import com.Gemini.AssetVault.Dto.SoftwareAssignmentResponse;
import com.Gemini.AssetVault.Dto.SoftwareLicenseResponse;
import com.Gemini.AssetVault.Model.Asset;
import com.Gemini.AssetVault.Model.AssetAssignment;
import com.Gemini.AssetVault.Model.Employee;
import com.Gemini.AssetVault.Model.MaintenanceRecord;
import com.Gemini.AssetVault.Model.SoftwareAssignment;
import com.Gemini.AssetVault.Model.SoftwareLicense;

import java.util.List;

public final class AssetVaultMapper {
    private AssetVaultMapper() {
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
