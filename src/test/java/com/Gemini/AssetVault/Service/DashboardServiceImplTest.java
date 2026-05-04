package com.Gemini.AssetVault.Service;

import com.Gemini.AssetVault.Model.Asset;
import com.Gemini.AssetVault.Model.Enum.AssetStatus;
import com.Gemini.AssetVault.Model.Enum.AssetType;
import com.Gemini.AssetVault.Model.Enum.AssignmentStatus;
import com.Gemini.AssetVault.Model.Enum.LicenseType;
import com.Gemini.AssetVault.Model.Enum.MaintenanceStatus;
import com.Gemini.AssetVault.Model.SoftwareLicense;
import com.Gemini.AssetVault.Repository.AssetRepository;
import com.Gemini.AssetVault.Repository.MaintenanceRecordRepository;
import com.Gemini.AssetVault.Repository.SoftwareAssignmentRepository;
import com.Gemini.AssetVault.Repository.SoftwareLicenseRepository;
import com.Gemini.AssetVault.Service.Impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {
    @Mock
    private AssetRepository assetRepository;

    @Mock
    private SoftwareLicenseRepository softwareLicenseRepository;

    @Mock
    private SoftwareAssignmentRepository softwareAssignmentRepository;

    @Mock
    private MaintenanceRecordRepository maintenanceRecordRepository;

    private DashboardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DashboardServiceImpl(
                assetRepository,
                softwareLicenseRepository,
                softwareAssignmentRepository,
                maintenanceRecordRepository
        );
    }

    @Test
    void summaryUsesLifecycleCountsFromRepository() {
        when(assetRepository.count()).thenReturn(10L);
        when(assetRepository.countByStatus(AssetStatus.ASSIGNED)).thenReturn(4L);
        when(assetRepository.countByStatus(AssetStatus.AVAILABLE)).thenReturn(5L);
        when(assetRepository.countByStatus(AssetStatus.UNDER_MAINTENANCE)).thenReturn(1L);
        when(assetRepository.countByStatus(AssetStatus.RETIRED)).thenReturn(0L);

        var summary = service.getSummary();

        assertThat(summary.totalAssets()).isEqualTo(10);
        assertThat(summary.assignedAssets()).isEqualTo(4);
        assertThat(summary.availableAssets()).isEqualTo(5);
        assertThat(summary.underMaintenanceAssets()).isEqualTo(1);
    }

    @Test
    void dashboardAggregatesAlertsBreakdownsAndValue() {
        Asset asset = asset();
        SoftwareLicense license = license();
        when(assetRepository.countGroupedByType()).thenReturn(List.<Object[]>of(new Object[]{AssetType.LAPTOP, 2L}));
        when(assetRepository.getDepartmentAssetSummary()).thenReturn(List.<Object[]>of(new Object[]{"Engineering", 2L, BigDecimal.valueOf(2000)}));
        when(assetRepository.findAssetsWithWarrantyExpiring(LocalDate.now(), LocalDate.now().plusDays(30))).thenReturn(List.of(asset));
        when(softwareLicenseRepository.findLicensesExpiringSoon(LocalDate.now(), LocalDate.now().plusDays(30))).thenReturn(List.of(license));
        when(softwareLicenseRepository.findLicensesWithNoRemainingSeats()).thenReturn(List.of(license));
        when(softwareAssignmentRepository.findAllBySoftwareLicenseIdAndStatusOrderByAssignedDateDescIdDesc(
                1L,
                AssignmentStatus.ACTIVE
        )).thenReturn(List.of());
        when(maintenanceRecordRepository.countByStatus(MaintenanceStatus.SCHEDULED)).thenReturn(3L);
        when(maintenanceRecordRepository.countByStatus(MaintenanceStatus.IN_PROGRESS)).thenReturn(1L);
        when(maintenanceRecordRepository.countByStatus(MaintenanceStatus.COMPLETED)).thenReturn(9L);
        when(assetRepository.getTotalActiveAssetValue()).thenReturn(Optional.of(BigDecimal.valueOf(2000)));

        assertThat(service.getTypeBreakdown()).extracting("type").containsExactly(AssetType.LAPTOP);
        assertThat(service.getDepartmentAssets()).extracting("department").containsExactly("Engineering");
        assertThat(service.getWarrantyAlerts()).extracting("assetCode").containsExactly("LPT-00001");
        assertThat(service.getLicenseAlerts().expiringSoon()).hasSize(1);
        assertThat(service.getLicenseAlerts().seatsExhausted()).hasSize(1);
        assertThat(service.getMaintenanceSummary().scheduled()).isEqualTo(3);
        assertThat(service.getAssetValue().totalActiveAssetValue()).isEqualByComparingTo("2000");
    }

    @Test
    void assetValueDefaultsToZeroWhenRepositoryReturnsEmpty() {
        when(assetRepository.getTotalActiveAssetValue()).thenReturn(Optional.empty());

        assertThat(service.getAssetValue().totalActiveAssetValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private Asset asset() {
        return Asset.builder()
                .id(1L)
                .assetCode("LPT-00001")
                .name("MacBook Pro")
                .brand("Apple")
                .model("M3")
                .type(AssetType.LAPTOP)
                .status(AssetStatus.AVAILABLE)
                .purchaseDate(LocalDate.now())
                .purchaseCost(BigDecimal.valueOf(1000))
                .warrantyExpiryDate(LocalDate.now().plusDays(20))
                .serialNumber("SER-001")
                .build();
    }

    private SoftwareLicense license() {
        return SoftwareLicense.builder()
                .id(1L)
                .softwareName("IntelliJ IDEA")
                .licenceKey("LIC-001")
                .licenseType(LicenseType.FLOATING)
                .vendor("JetBrains")
                .totalSeats(1)
                .usedSeats(1)
                .purchaseDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusDays(20))
                .isActive(true)
                .build();
    }
}
