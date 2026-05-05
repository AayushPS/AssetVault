package com.assetvault.repository;

import com.assetvault.model.Asset;
import com.assetvault.model.AssetAssignment;
import com.assetvault.model.Employee;
import com.assetvault.model.enums.AssetStatus;
import com.assetvault.model.enums.AssetType;
import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.model.enums.LicenseType;
import com.assetvault.model.enums.MaintenanceStatus;
import com.assetvault.model.enums.MaintenanceType;
import com.assetvault.model.MaintenanceRecord;
import com.assetvault.model.SoftwareAssignment;
import com.assetvault.model.SoftwareLicense;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
        MaintenanceRecord scheduled = maintenanceRecordRepository.save(
                maintenance(asset, MaintenanceType.REPAIR, MaintenanceStatus.SCHEDULED, LocalDate.of(2026, 5, 10))
        );
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
        List<MaintenanceStatus> activeMaintenanceStatuses = List.of(
                MaintenanceStatus.SCHEDULED,
                MaintenanceStatus.IN_PROGRESS
        );
        assertThat(maintenanceRecordRepository.existsByAssetIdAndStatusIn(
                asset.getId(),
                activeMaintenanceStatuses
        )).isTrue();
        assertThat(maintenanceRecordRepository.existsByAssetIdAndStatusInAndIdNot(
                asset.getId(),
                activeMaintenanceStatuses,
                scheduled.getId()
        )).isFalse();
    }

    @Test
    void softwareLicenseQueriesFindExpiringExpiredAndExhaustedLicenses() {
        SoftwareLicense expiring = softwareLicenseRepository.save(license("IntelliJ IDEA", "LIC-001", 2, 1, LocalDate.now().plusDays(10)));
        SoftwareLicense expired = softwareLicenseRepository.save(license("Photoshop", "LIC-002", 2, 0, LocalDate.now().minusDays(1)));
        SoftwareLicense exhausted = softwareLicenseRepository.save(license("Figma", "LIC-003", 1, 1, LocalDate.now().plusDays(90)));
        SoftwareLicense exhaustedIndividual = license("Sketch", "LIC-004", 1, 1, LocalDate.now().plusDays(90));
        exhaustedIndividual.setLicenseType(LicenseType.INDIVIDUAL);
        softwareLicenseRepository.save(exhaustedIndividual);
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
                .contains(exhausted.getLicenceKey())
                .doesNotContain(exhaustedIndividual.getLicenceKey());
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

    @Test
    void assetRepositoryCoversUniqueLookupsCountsListVariantsAndPagedSummaries() {
        Employee employee = employeeRepository.save(employee("EMP-00001", "aarav@example.com", "Engineering", true));
        Asset laptop = assetRepository.save(asset("LPT-00001", "SER-001", AssetType.LAPTOP, AssetStatus.ASSIGNED, 1000));
        Asset desktop = assetRepository.save(asset("DSK-00001", "SER-002", AssetType.DESKTOP, AssetStatus.RETIRED, 1500));
        Asset expiredWarranty = asset("MON-00001", "SER-003", AssetType.MONITOR, AssetStatus.AVAILABLE, 300);
        expiredWarranty.setWarrantyExpiryDate(LocalDate.now().minusDays(3));
        assetRepository.save(expiredWarranty);
        assetAssignmentRepository.save(assignment(laptop, employee, AssignmentStatus.ACTIVE, LocalDate.now()));

        assertThat(assetRepository.findByAssetCode("LPT-00001")).contains(laptop);
        assertThat(assetRepository.existsByAssetCode("LPT-00001")).isTrue();
        assertThat(assetRepository.existsBySerialNumber("SER-001")).isTrue();
        assertThat(assetRepository.existsBySerialNumberAndIdNot("SER-001", desktop.getId())).isTrue();
        assertThat(assetRepository.findAllByType(AssetType.LAPTOP)).containsExactly(laptop);
        assertThat(assetRepository.findAllByType(AssetType.LAPTOP, PageRequest.of(0, 10)).getContent())
                .containsExactly(laptop);
        assertThat(assetRepository.findAllByStatus(AssetStatus.ASSIGNED)).containsExactly(laptop);
        assertThat(assetRepository.findAllByStatus(AssetStatus.ASSIGNED, PageRequest.of(0, 10)).getContent())
                .containsExactly(laptop);
        assertThat(assetRepository.findAssetsByDepartment("engineering"))
                .extracting(Asset::getAssetCode)
                .containsExactly("LPT-00001");
        assertThat(assetRepository.findAssetsWithWarrantyExpiring(
                LocalDate.now(),
                LocalDate.now().plusDays(30)
        )).extracting(Asset::getAssetCode).containsExactlyInAnyOrder("LPT-00001", "DSK-00001");
        assertThat(assetRepository.findAssetsWithExpiredWarranty(LocalDate.now()))
                .extracting(Asset::getAssetCode)
                .containsExactly("MON-00001");
        assertThat(assetRepository.searchByKeyword("apple"))
                .extracting(Asset::getAssetCode)
                .containsExactlyInAnyOrder("LPT-00001", "DSK-00001", "MON-00001");
        assertThat(assetRepository.countByStatus(AssetStatus.ASSIGNED)).isEqualTo(1);
        assertThat(assetRepository.countByType(AssetType.LAPTOP)).isEqualTo(1);
        assertThat(assetRepository.countGroupedByType(PageRequest.of(0, 10)).getTotalElements()).isEqualTo(3);
        assertThat(assetRepository.getDepartmentAssetSummary(PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
    }

    @Test
    void employeeRepositoryCoversUniqueLookupsExistsChecksAndListVariants() {
        Employee active = employeeRepository.save(employee("EMP-00001", "aarav@example.com", "Engineering", true));
        Employee inactive = employee("EMP-00002", "mira@example.com", "Design", false);
        inactive.setName("Mira");
        inactive = employeeRepository.save(inactive);

        assertThat(employeeRepository.findByEmployeeCode("EMP-00001")).contains(active);
        assertThat(employeeRepository.findByEmail("AARAV@example.com")).contains(active);
        assertThat(employeeRepository.existsByEmail("AARAV@example.com")).isTrue();
        assertThat(employeeRepository.existsByEmailIgnoreCaseAndIdNot("aarav@example.com", inactive.getId())).isTrue();
        assertThat(employeeRepository.existsByEmployeeCode("EMP-00001")).isTrue();
        assertThat(employeeRepository.existsByEmployeeCodeAndIdNot("EMP-00001", inactive.getId())).isTrue();
        assertThat(employeeRepository.searchByName("aar"))
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP-00001");
        assertThat(employeeRepository.findByDepartment("engineering"))
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP-00001");
        assertThat(employeeRepository.findAllByIsActive(true))
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP-00001");
        assertThat(employeeRepository.findByIsActive(false, PageRequest.of(0, 10)).getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP-00002");
    }

    @Test
    void assetAssignmentRepositoryCoversOrderedListsExistenceLookupsAndCounts() {
        Employee employee = employeeRepository.save(employee("EMP-00001", "aarav@example.com", "Engineering", true));
        Asset asset = assetRepository.save(asset("LPT-00001", "SER-001", AssetType.LAPTOP, AssetStatus.ASSIGNED, 1000));
        Asset otherAsset = assetRepository.save(asset("MON-00001", "SER-002", AssetType.MONITOR, AssetStatus.AVAILABLE, 300));
        AssetAssignment older = assetAssignmentRepository.save(
                assignment(asset, employee, AssignmentStatus.RETURNED, LocalDate.of(2026, 3, 1))
        );
        AssetAssignment newer = assetAssignmentRepository.save(
                assignment(asset, employee, AssignmentStatus.ACTIVE, LocalDate.of(2026, 4, 1))
        );
        assetAssignmentRepository.save(
                assignment(otherAsset, employee, AssignmentStatus.TRANSFERRED, LocalDate.of(2026, 2, 1))
        );

        assertThat(assetAssignmentRepository.findAllByStatus(AssignmentStatus.ACTIVE)).containsExactly(newer);
        assertThat(assetAssignmentRepository.findAllByAssetIdOrderByAssignedDateDescIdDesc(asset.getId()))
                .containsExactly(newer, older);
        assertThat(assetAssignmentRepository.findAllByEmployeeIdOrderByAssignedDateDescIdDesc(employee.getId()))
                .extracting(AssetAssignment::getAssignedDate)
                .containsExactly(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 2, 1));
        assertThat(assetAssignmentRepository.findAllByAssignedDateBetweenOrderByAssignedDateDescIdDesc(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 4, 30)
        )).containsExactly(newer, older);
        assertThat(assetAssignmentRepository.existsByEmployeeIdAndStatus(employee.getId(), AssignmentStatus.ACTIVE))
                .isTrue();
        assertThat(assetAssignmentRepository.findByAssetIdAndStatus(asset.getId(), AssignmentStatus.ACTIVE))
                .contains(newer);
        assertThat(assetAssignmentRepository.findByEmployeeIdAndAssetIdAndStatus(
                employee.getId(),
                asset.getId(),
                AssignmentStatus.ACTIVE
        )).contains(newer);
        assertThat(assetAssignmentRepository.countByStatus(AssignmentStatus.ACTIVE)).isEqualTo(1);
    }

    @Test
    void maintenanceRepositoryCoversOrderedListsCountsAndActiveStatusLookup() {
        Asset asset = assetRepository.save(asset("LPT-00001", "SER-001", AssetType.LAPTOP, AssetStatus.UNDER_MAINTENANCE, 1000));
        MaintenanceRecord scheduled = maintenanceRecordRepository.save(
                maintenance(asset, MaintenanceType.REPAIR, MaintenanceStatus.SCHEDULED, LocalDate.of(2026, 5, 10))
        );
        MaintenanceRecord inProgress = maintenanceRecordRepository.save(
                maintenance(asset, MaintenanceType.SERVICE, MaintenanceStatus.IN_PROGRESS, LocalDate.of(2026, 5, 20))
        );
        maintenanceRecordRepository.save(
                maintenance(asset, MaintenanceType.INSPECTION, MaintenanceStatus.COMPLETED, LocalDate.of(2026, 4, 1))
        );

        assertThat(maintenanceRecordRepository.findAllByAssetIdOrderByScheduledDateDescIdDesc(asset.getId()))
                .extracting(MaintenanceRecord::getScheduledDate)
                .containsExactly(LocalDate.of(2026, 5, 20), LocalDate.of(2026, 5, 10), LocalDate.of(2026, 4, 1));
        assertThat(maintenanceRecordRepository.findAllByAssetIdAndStatusOrderByScheduledDateDescIdDesc(
                asset.getId(),
                MaintenanceStatus.IN_PROGRESS
        )).containsExactly(inProgress);
        assertThat(maintenanceRecordRepository.findAllByStatus(MaintenanceStatus.SCHEDULED)).containsExactly(scheduled);
        assertThat(maintenanceRecordRepository.findAllByMaintenanceType(MaintenanceType.SERVICE)).containsExactly(inProgress);
        assertThat(maintenanceRecordRepository.findScheduledMaintenance(LocalDate.of(2026, 5, 1)))
                .containsExactly(scheduled);
        assertThat(maintenanceRecordRepository.findAllByScheduledDateBetweenOrderByScheduledDateDescIdDesc(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31)
        )).containsExactly(inProgress, scheduled);
        assertThat(maintenanceRecordRepository.countByStatus(MaintenanceStatus.IN_PROGRESS)).isEqualTo(1);
    }

    @Test
    void softwareRepositoriesCoverLockLookupListsExistenceCountsAndBulkDelete() {
        Employee employee = employeeRepository.save(employee("EMP-00001", "aarav@example.com", "Engineering", true));
        SoftwareLicense active = softwareLicenseRepository.save(license("IntelliJ IDEA", "LIC-001", 2, 1, LocalDate.now().plusDays(30)));
        SoftwareLicense inactive = license("Photoshop", "LIC-002", 1, 0, LocalDate.now().plusDays(30));
        inactive.setIsActive(false);
        softwareLicenseRepository.save(inactive);
        SoftwareAssignment activeAssignment = softwareAssignmentRepository.save(
                softwareAssignment(active, employee, AssignmentStatus.ACTIVE, LocalDate.of(2026, 5, 1))
        );
        SoftwareAssignment returnedAssignment = softwareAssignmentRepository.save(
                softwareAssignment(active, employee, AssignmentStatus.RETURNED, LocalDate.of(2026, 4, 1))
        );

        assertThat(softwareLicenseRepository.findByLicenseKey("LIC-001")).contains(active);
        assertThat(softwareLicenseRepository.findByIdForUpdate(active.getId())).contains(active);
        assertThat(softwareLicenseRepository.existsByLicenseKey("LIC-001")).isTrue();
        assertThat(softwareLicenseRepository.existsByLicenceKeyAndIdNot("LIC-001", inactive.getId())).isTrue();
        assertThat(softwareLicenseRepository.findByIsActive(true, PageRequest.of(0, 10)).getContent())
                .extracting(SoftwareLicense::getLicenceKey)
                .containsExactly("LIC-001");
        assertThat(softwareLicenseRepository.findAllByIsActive(false))
                .extracting(SoftwareLicense::getLicenceKey)
                .containsExactly("LIC-002");
        assertThat(softwareLicenseRepository.searchBySoftwareName("idea"))
                .extracting(SoftwareLicense::getLicenceKey)
                .containsExactly("LIC-001");
        assertThat(softwareLicenseRepository.findLicensesExpiringSoon(
                LocalDate.now(),
                LocalDate.now().plusDays(60)
        )).extracting(SoftwareLicense::getLicenceKey).containsExactly("LIC-001", "LIC-002");
        assertThat(softwareLicenseRepository.findExpiredLicenses(LocalDate.now())).isEmpty();

        assertThat(softwareAssignmentRepository.findByStatus(AssignmentStatus.ACTIVE, PageRequest.of(0, 10)).getContent())
                .containsExactly(activeAssignment);
        assertThat(softwareAssignmentRepository.findAllByStatus(AssignmentStatus.RETURNED))
                .containsExactly(returnedAssignment);
        assertThat(softwareAssignmentRepository.findAllBySoftwareLicenseIdOrderByAssignedDateDescIdDesc(active.getId()))
                .containsExactly(activeAssignment, returnedAssignment);
        assertThat(softwareAssignmentRepository.findAllBySoftwareLicenseIdAndStatusOrderByAssignedDateDescIdDesc(
                active.getId(),
                AssignmentStatus.ACTIVE
        )).containsExactly(activeAssignment);
        assertThat(softwareAssignmentRepository.findAllBySoftwareLicenseIdAndStatus(
                active.getId(),
                AssignmentStatus.ACTIVE
        )).containsExactly(activeAssignment);
        assertThat(softwareAssignmentRepository.findByEmployeeId(employee.getId(), PageRequest.of(0, 10)).getContent())
                .containsExactly(activeAssignment, returnedAssignment);
        assertThat(softwareAssignmentRepository.findAllByEmployeeIdOrderByAssignedDateDescIdDesc(employee.getId()))
                .containsExactly(activeAssignment, returnedAssignment);
        assertThat(softwareAssignmentRepository.existsByEmployeeIdAndSoftwareLicenseIdAndStatus(
                employee.getId(),
                active.getId(),
                AssignmentStatus.ACTIVE
        )).isTrue();
        assertThat(softwareAssignmentRepository.deleteBySoftwareLicenseId(active.getId())).isEqualTo(2);
        assertThat(softwareAssignmentRepository.findBySoftwareLicenseId(active.getId(), PageRequest.of(0, 10)).getTotalElements())
                .isZero();
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
        return softwareAssignment(license, employee, status, LocalDate.now());
    }

    private SoftwareAssignment softwareAssignment(
            SoftwareLicense license,
            Employee employee,
            AssignmentStatus status,
            LocalDate assignedDate
    ) {
        return SoftwareAssignment.builder()
                .softwareLicense(license)
                .employee(employee)
                .seatIndex(1)
                .assignedDate(assignedDate)
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
