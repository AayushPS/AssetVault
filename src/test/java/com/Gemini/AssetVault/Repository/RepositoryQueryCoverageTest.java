package com.Gemini.AssetVault.Repository;

import com.Gemini.AssetVault.Model.Asset;
import com.Gemini.AssetVault.Model.AssetAssignment;
import com.Gemini.AssetVault.Model.Employee;
import com.Gemini.AssetVault.Model.Enum.AssetStatus;
import com.Gemini.AssetVault.Model.Enum.AssetType;
import com.Gemini.AssetVault.Model.Enum.AssignmentStatus;
import com.Gemini.AssetVault.Model.Enum.LicenseType;
import com.Gemini.AssetVault.Model.Enum.MaintenanceStatus;
import com.Gemini.AssetVault.Model.Enum.MaintenanceType;
import com.Gemini.AssetVault.Model.MaintenanceRecord;
import com.Gemini.AssetVault.Model.SoftwareAssignment;
import com.Gemini.AssetVault.Model.SoftwareLicense;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(RepositoryQueryCoverageTest.JpaAuditingConfig.class)
class RepositoryQueryCoverageTest {
    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AssetAssignmentRepository assetAssignmentRepository;

    @Autowired
    private MaintenanceRecordRepository maintenanceRecordRepository;

    @Autowired
    private SoftwareLicenseRepository softwareLicenseRepository;

    @Autowired
    private SoftwareAssignmentRepository softwareAssignmentRepository;

    @Test
    void employeeQueriesReturnOnlyActiveAssetsAndLicenses() {
        Employee employee = employeeRepository.save(employee("EMP-00001", "aarav@example.com", "Engineering", true));
        Asset activeAsset = assetRepository.save(asset("LPT-00001", "SER-001", AssetType.LAPTOP, AssetStatus.ASSIGNED, 1000));
        Asset returnedAsset = assetRepository.save(asset("MON-00001", "SER-002", AssetType.MONITOR, AssetStatus.AVAILABLE, 500));
        SoftwareLicense license = softwareLicenseRepository.save(license("IntelliJ IDEA", "LIC-001", 2, 1, LocalDate.now().plusDays(30)));
        assetAssignmentRepository.save(assignment(activeAsset, employee, AssignmentStatus.ACTIVE, LocalDate.now()));
        assetAssignmentRepository.save(assignment(returnedAsset, employee, AssignmentStatus.RETURNED, LocalDate.now().minusDays(5)));
        softwareAssignmentRepository.save(softwareAssignment(license, employee, AssignmentStatus.ACTIVE));

        assertThat(employeeRepository.findActiveAssetsForEmployee(employee.getId(), PageRequest.of(0, 10)).getContent())
                .extracting(Asset::getAssetCode)
                .containsExactly("LPT-00001");
        assertThat(employeeRepository.findLicensesAssignedToEmployee(employee.getId(), PageRequest.of(0, 10)).getContent())
                .extracting(SoftwareLicense::getSoftwareName)
                .containsExactly("IntelliJ IDEA");
        assertThat(employeeRepository.searchByName("aar", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(employeeRepository.findByDepartment("engineering", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(employeeRepository.findByIsActive(true, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
    }

    @Test
    void assignmentAndMaintenanceQueriesFilterStatusTypeAndDateRanges() {
        Employee employee = employeeRepository.save(employee("EMP-00001", "aarav@example.com", "Engineering", true));
        Asset asset = assetRepository.save(asset("LPT-00001", "SER-001", AssetType.LAPTOP, AssetStatus.ASSIGNED, 1000));
        assetAssignmentRepository.save(assignment(asset, employee, AssignmentStatus.ACTIVE, LocalDate.of(2026, 4, 10)));
        assetAssignmentRepository.save(assignment(asset, employee, AssignmentStatus.RETURNED, LocalDate.of(2026, 3, 10)));
        maintenanceRecordRepository.save(maintenance(asset, MaintenanceType.REPAIR, MaintenanceStatus.SCHEDULED, LocalDate.of(2026, 5, 10)));
        maintenanceRecordRepository.save(maintenance(asset, MaintenanceType.INSPECTION, MaintenanceStatus.COMPLETED, LocalDate.of(2026, 3, 10)));

        assertThat(assetAssignmentRepository.findByStatus(AssignmentStatus.ACTIVE, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(assetAssignmentRepository.findByAssetId(asset.getId(), PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(assetAssignmentRepository.findByEmployeeId(employee.getId(), PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(assetAssignmentRepository.findByAssignedDateBetween(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                PageRequest.of(0, 10)
        ).getTotalElements()).isEqualTo(1);
        assertThat(assetAssignmentRepository.existsByAssetIdAndStatus(asset.getId(), AssignmentStatus.ACTIVE)).isTrue();

        assertThat(maintenanceRecordRepository.findByAssetId(asset.getId(), PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(maintenanceRecordRepository.findByStatus(MaintenanceStatus.SCHEDULED, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(maintenanceRecordRepository.findByMaintenanceType(MaintenanceType.REPAIR, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(maintenanceRecordRepository.findScheduledMaintenance(LocalDate.of(2026, 5, 1), PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(maintenanceRecordRepository.findByScheduledDateBetween(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                PageRequest.of(0, 10)
        ).getTotalElements()).isEqualTo(1);
    }

    @Test
    void softwareLicenseQueriesFindExpiringExpiredAndExhaustedLicenses() {
        SoftwareLicense expiring = softwareLicenseRepository.save(license("IntelliJ IDEA", "LIC-001", 2, 1, LocalDate.now().plusDays(10)));
        SoftwareLicense expired = softwareLicenseRepository.save(license("Photoshop", "LIC-002", 2, 0, LocalDate.now().minusDays(1)));
        SoftwareLicense exhausted = softwareLicenseRepository.save(license("Figma", "LIC-003", 1, 1, LocalDate.now().plusDays(90)));
        Employee employee = employeeRepository.save(employee("EMP-00001", "aarav@example.com", "Design", true));
        softwareAssignmentRepository.save(softwareAssignment(expiring, employee, AssignmentStatus.ACTIVE));

        assertThat(softwareLicenseRepository.searchBySoftwareName("idea", PageRequest.of(0, 10)).getContent())
                .extracting(SoftwareLicense::getLicenceKey)
                .containsExactly(expiring.getLicenceKey());
        assertThat(softwareLicenseRepository.findLicensesExpiringSoon(LocalDate.now(), LocalDate.now().plusDays(30), PageRequest.of(0, 10)).getContent())
                .extracting(SoftwareLicense::getLicenceKey)
                .containsExactly(expiring.getLicenceKey());
        assertThat(softwareLicenseRepository.findExpiredLicenses(LocalDate.now(), PageRequest.of(0, 10)).getContent())
                .extracting(SoftwareLicense::getLicenceKey)
                .containsExactly(expired.getLicenceKey());
        assertThat(softwareLicenseRepository.findLicensesWithNoRemainingSeats(PageRequest.of(0, 10)).getContent())
                .extracting(SoftwareLicense::getLicenceKey)
                .contains(exhausted.getLicenceKey());
        assertThat(softwareAssignmentRepository.findBySoftwareLicenseId(expiring.getId(), PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(softwareAssignmentRepository.countBySoftwareLicenseIdAndStatus(expiring.getId(), AssignmentStatus.ACTIVE)).isEqualTo(1);
        assertThat(softwareAssignmentRepository.findByEmployeeIdAndSoftwareLicenseIdAndStatus(
                employee.getId(),
                expiring.getId(),
                AssignmentStatus.ACTIVE
        )).isPresent();
    }

    @Test
    void assetQueriesCoverDepartmentSummariesLowValueAndActiveValue() {
        Employee employee = employeeRepository.save(employee("EMP-00001", "aarav@example.com", "Engineering", true));
        Asset active = assetRepository.save(asset("LPT-00001", "SER-001", AssetType.LAPTOP, AssetStatus.ASSIGNED, 1000));
        Asset lowValue = assetRepository.save(asset("MON-00001", "SER-002", AssetType.MONITOR, AssetStatus.AVAILABLE, 300));
        assetRepository.save(asset("PRN-00001", "SER-003", AssetType.PRINTER, AssetStatus.LOST, 700));
        assetAssignmentRepository.save(assignment(active, employee, AssignmentStatus.ACTIVE, LocalDate.now()));

        assertThat(assetRepository.findAssetsByDepartment("engineering", PageRequest.of(0, 10)).getContent())
                .extracting(Asset::getAssetCode)
                .containsExactly(active.getAssetCode());
        assertThat(assetRepository.findByPurchaseCostLessThanEqual(BigDecimal.valueOf(500), PageRequest.of(0, 10)).getContent())
                .extracting(Asset::getAssetCode)
                .containsExactly(lowValue.getAssetCode());
        assertThat(assetRepository.countGroupedByType()).hasSize(3);
        assertThat(assetRepository.getDepartmentAssetSummary()).hasSize(1);
        assertThat(assetRepository.getTotalActiveAssetValue()).contains(BigDecimal.valueOf(1300).setScale(2));
    }

    private Employee employee(String code, String email, String department, boolean active) {
        return Employee.builder()
                .name("Aarav")
                .email(email)
                .department(department)
                .designation("Engineer")
                .employeeCode(code)
                .isActive(active)
                .build();
    }

    private Asset asset(String code, String serial, AssetType type, AssetStatus status, int cost) {
        return Asset.builder()
                .assetCode(code)
                .name("MacBook Pro")
                .brand("Apple")
                .model("M3")
                .type(type)
                .status(status)
                .purchaseDate(LocalDate.now())
                .purchaseCost(BigDecimal.valueOf(cost))
                .warrantyExpiryDate(LocalDate.now().plusDays(20))
                .serialNumber(serial)
                .location("HQ")
                .build();
    }

    private AssetAssignment assignment(Asset asset, Employee employee, AssignmentStatus status, LocalDate assignedDate) {
        return AssetAssignment.builder()
                .asset(asset)
                .employee(employee)
                .assignedDate(assignedDate)
                .status(status)
                .assignedBy("IT Admin")
                .remarks("Repository test")
                .build();
    }

    private MaintenanceRecord maintenance(
            Asset asset,
            MaintenanceType type,
            MaintenanceStatus status,
            LocalDate scheduledDate
    ) {
        return MaintenanceRecord.builder()
                .asset(asset)
                .maintenanceType(type)
                .description("Routine work")
                .maintenanceCost(BigDecimal.valueOf(100))
                .vendor("Vendor")
                .scheduledDate(scheduledDate)
                .status(status)
                .build();
    }

    private SoftwareLicense license(String name, String key, int totalSeats, int usedSeats, LocalDate expiryDate) {
        return SoftwareLicense.builder()
                .softwareName(name)
                .licenceKey(key)
                .licenseType(LicenseType.FLOATING)
                .vendor("Vendor")
                .totalSeats(totalSeats)
                .usedSeats(usedSeats)
                .purchaseDate(LocalDate.now())
                .expiryDate(expiryDate)
                .isActive(true)
                .build();
    }

    private SoftwareAssignment softwareAssignment(
            SoftwareLicense license,
            Employee employee,
            AssignmentStatus status
    ) {
        return SoftwareAssignment.builder()
                .softwareLicense(license)
                .employee(employee)
                .seatIndex(1)
                .assignedDate(LocalDate.now())
                .status(status)
                .assignedBy("SYSTEM")
                .remarks("Repository test")
                .build();
    }

    @TestConfiguration
    @EnableJpaAuditing
    static class JpaAuditingConfig {
    }
}
