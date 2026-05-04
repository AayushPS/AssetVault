package com.Gemini.AssetVault.Service.Impl;

import com.Gemini.AssetVault.Dto.AssetResponse;
import com.Gemini.AssetVault.Dto.AssetValueResponse;
import com.Gemini.AssetVault.Dto.DashboardSummaryResponse;
import com.Gemini.AssetVault.Dto.DepartmentAssetSummaryResponse;
import com.Gemini.AssetVault.Dto.EmployeeResponse;
import com.Gemini.AssetVault.Dto.LicenseAlertsResponse;
import com.Gemini.AssetVault.Dto.MaintenanceSummaryResponse;
import com.Gemini.AssetVault.Dto.SoftwareLicenseResponse;
import com.Gemini.AssetVault.Dto.TypeBreakdownResponse;
import com.Gemini.AssetVault.Model.Enum.AssetStatus;
import com.Gemini.AssetVault.Model.Enum.AssetType;
import com.Gemini.AssetVault.Model.Enum.AssignmentStatus;
import com.Gemini.AssetVault.Model.Enum.MaintenanceStatus;
import com.Gemini.AssetVault.Model.SoftwareAssignment;
import com.Gemini.AssetVault.Model.SoftwareLicense;
import com.Gemini.AssetVault.Repository.AssetRepository;
import com.Gemini.AssetVault.Repository.MaintenanceRecordRepository;
import com.Gemini.AssetVault.Repository.SoftwareAssignmentRepository;
import com.Gemini.AssetVault.Repository.SoftwareLicenseRepository;
import com.Gemini.AssetVault.Service.DashboardService;
import com.Gemini.AssetVault.Util.AssetVaultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final AssetRepository assetRepository;
    private final SoftwareLicenseRepository softwareLicenseRepository;
    private final SoftwareAssignmentRepository softwareAssignmentRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        return new DashboardSummaryResponse(
                assetRepository.count(),
                assetRepository.countByStatus(AssetStatus.ASSIGNED),
                assetRepository.countByStatus(AssetStatus.AVAILABLE),
                assetRepository.countByStatus(AssetStatus.UNDER_MAINTENANCE),
                assetRepository.countByStatus(AssetStatus.RETIRED)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TypeBreakdownResponse> getTypeBreakdown() {
        return assetRepository.countGroupedByType().stream()
                .map(row -> new TypeBreakdownResponse((AssetType) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentAssetSummaryResponse> getDepartmentAssets() {
        return assetRepository.getDepartmentAssetSummary().stream()
                .map(row -> new DepartmentAssetSummaryResponse(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        (BigDecimal) row[2]
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetResponse> getWarrantyAlerts() {
        LocalDate today = LocalDate.now();
        return assetRepository.findAssetsWithWarrantyExpiring(today, today.plusDays(30)).stream()
                .map(AssetVaultMapper::toAssetResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LicenseAlertsResponse getLicenseAlerts() {
        LocalDate today = LocalDate.now();
        List<SoftwareLicenseResponse> expiringSoon = softwareLicenseRepository
                .findLicensesExpiringSoon(today, today.plusDays(30))
                .stream()
                .map(this::toLicenseResponse)
                .toList();
        List<SoftwareLicenseResponse> exhausted = softwareLicenseRepository
                .findLicensesWithNoRemainingSeats()
                .stream()
                .map(this::toLicenseResponse)
                .toList();
        return new LicenseAlertsResponse(expiringSoon, exhausted);
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceSummaryResponse getMaintenanceSummary() {
        return new MaintenanceSummaryResponse(
                maintenanceRecordRepository.countByStatus(MaintenanceStatus.SCHEDULED),
                maintenanceRecordRepository.countByStatus(MaintenanceStatus.IN_PROGRESS),
                maintenanceRecordRepository.countByStatus(MaintenanceStatus.COMPLETED)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AssetValueResponse getAssetValue() {
        return new AssetValueResponse(assetRepository.getTotalActiveAssetValue().orElse(BigDecimal.ZERO));
    }

    private SoftwareLicenseResponse toLicenseResponse(SoftwareLicense license) {
        List<EmployeeResponse> assignedEmployees = softwareAssignmentRepository
                .findAllBySoftwareLicenseIdAndStatusOrderByAssignedDateDescIdDesc(
                        license.getId(),
                        AssignmentStatus.ACTIVE
                )
                .stream()
                .map(SoftwareAssignment::getEmployee)
                .map(AssetVaultMapper::toEmployeeResponse)
                .toList();
        return AssetVaultMapper.toSoftwareLicenseResponse(license, assignedEmployees);
    }
}
