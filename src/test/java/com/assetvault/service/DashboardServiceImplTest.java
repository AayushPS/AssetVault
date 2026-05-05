package com.assetvault.service;

import com.assetvault.model.Asset;
import com.assetvault.model.enums.AssetStatus;
import com.assetvault.model.enums.AssetType;
import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.model.enums.LicenseType;
import com.assetvault.model.enums.MaintenanceStatus;
import com.assetvault.model.SoftwareLicense;
import com.assetvault.repository.AssetRepository;
import com.assetvault.repository.MaintenanceRecordRepository;
import com.assetvault.repository.SoftwareAssignmentRepository;
import com.assetvault.repository.SoftwareLicenseRepository;
import com.assetvault.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
        var pageable = PageRequest.of(0, 10);
        when(assetRepository.countGroupedByType(pageable))
                .thenReturn(new PageImpl<>(List.<Object[]>of(new Object[]{AssetType.LAPTOP, 2L})));
        when(assetRepository.getDepartmentAssetSummary(pageable))
                .thenReturn(new PageImpl<>(List.<Object[]>of(new Object[]{"Engineering", 2L, BigDecimal.valueOf(2000)})));
        when(assetRepository.findAssetsWithWarrantyExpiring(LocalDate.now(), LocalDate.now().plusDays(30), pageable))
                .thenReturn(new PageImpl<>(List.of(asset)));
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

        assertThat(service.getTypeBreakdown(pageable)).extracting("type").containsExactly(AssetType.LAPTOP);
        assertThat(service.getDepartmentAssets(pageable)).extracting("department").containsExactly("Engineering");
        assertThat(service.getWarrantyAlerts(pageable)).extracting("assetCode").containsExactly("LPT-00001");
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
