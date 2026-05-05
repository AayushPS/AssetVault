package com.assetvault.service.impl;

import com.assetvault.dto.AssetResponse;
import com.assetvault.dto.AssetValueResponse;
import com.assetvault.dto.DashboardSummaryResponse;
import com.assetvault.dto.DepartmentAssetSummaryResponse;
import com.assetvault.dto.EmployeeResponse;
import com.assetvault.dto.LicenseAlertsResponse;
import com.assetvault.dto.MaintenanceSummaryResponse;
import com.assetvault.dto.SoftwareLicenseResponse;
import com.assetvault.dto.TypeBreakdownResponse;
import com.assetvault.model.enums.AssetStatus;
import com.assetvault.model.enums.AssetType;
import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.model.enums.MaintenanceStatus;
import com.assetvault.model.SoftwareAssignment;
import com.assetvault.model.SoftwareLicense;
import com.assetvault.repository.AssetRepository;
import com.assetvault.repository.MaintenanceRecordRepository;
import com.assetvault.repository.SoftwareAssignmentRepository;
import com.assetvault.repository.SoftwareLicenseRepository;
import com.assetvault.service.DashboardService;
import com.assetvault.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<TypeBreakdownResponse> getTypeBreakdown(Pageable pageable) {
        return assetRepository.countGroupedByType(pageable)
                .map(row -> new TypeBreakdownResponse((AssetType) row[0], ((Number) row[1]).longValue()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentAssetSummaryResponse> getDepartmentAssets(Pageable pageable) {
        return assetRepository.getDepartmentAssetSummary(pageable)
                .map(row -> new DepartmentAssetSummaryResponse(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        (BigDecimal) row[2]
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getWarrantyAlerts(Pageable pageable) {
        LocalDate today = LocalDate.now();
        return assetRepository.findAssetsWithWarrantyExpiring(today, today.plusDays(30), pageable)
                .map(Mapper::toAssetResponse);
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
                .map(Mapper::toEmployeeResponse)
                .toList();
        return Mapper.toSoftwareLicenseResponse(license, assignedEmployees);
    }
}
