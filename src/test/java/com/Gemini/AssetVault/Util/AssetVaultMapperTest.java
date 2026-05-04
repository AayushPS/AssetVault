package com.Gemini.AssetVault.Util;

import com.Gemini.AssetVault.Dto.EmployeeResponse;
import com.Gemini.AssetVault.Model.Employee;
import com.Gemini.AssetVault.Model.Enum.AssignmentStatus;
import com.Gemini.AssetVault.Model.Enum.LicenseType;
import com.Gemini.AssetVault.Model.SoftwareAssignment;
import com.Gemini.AssetVault.Model.SoftwareLicense;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AssetVaultMapperTest {
    @Test
    void mapsSoftwareAssignmentDetailsAcrossLicenseEmployeeAndAssignment() {
        LocalDate assignedDate = LocalDate.of(2026, 4, 21);
        LocalDate returnedDate = LocalDate.of(2026, 5, 1);
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 21, 9, 30);
        SoftwareAssignment assignment = SoftwareAssignment.builder()
                .id(12L)
                .softwareLicense(license(7L, 10, 3))
                .employee(employee())
                .seatIndex(4)
                .assignedDate(assignedDate)
                .returnedDate(returnedDate)
                .status(AssignmentStatus.RETURNED)
                .assignedBy("IT Admin")
                .remarks("Seat rotated")
                .createdAt(createdAt)
                .build();

        var response = AssetVaultMapper.toSoftwareAssignmentResponse(assignment);

        assertThat(response.id()).isEqualTo(12L);
        assertThat(response.licenseId()).isEqualTo(7L);
        assertThat(response.softwareName()).isEqualTo("IntelliJ IDEA");
        assertThat(response.employeeId()).isEqualTo(1L);
        assertThat(response.employeeName()).isEqualTo("Aarav");
        assertThat(response.seatIndex()).isEqualTo(4);
        assertThat(response.assignedDate()).isEqualTo(assignedDate);
        assertThat(response.returnedDate()).isEqualTo(returnedDate);
        assertThat(response.status()).isEqualTo(AssignmentStatus.RETURNED);
        assertThat(response.assignedBy()).isEqualTo("IT Admin");
        assertThat(response.remarks()).isEqualTo("Seat rotated");
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void licenseResponseNeverReportsNegativeRemainingSeats() {
        EmployeeResponse assignedEmployee = AssetVaultMapper.toEmployeeResponse(employee());

        var response = AssetVaultMapper.toSoftwareLicenseResponse(
                license(7L, 2, 5),
                List.of(assignedEmployee)
        );

        assertThat(response.totalSeats()).isEqualTo(2);
        assertThat(response.usedSeats()).isEqualTo(5);
        assertThat(response.remainingSeats()).isZero();
        assertThat(response.assignedEmployees()).containsExactly(assignedEmployee);
    }

    @Test
    void licenseResponseTreatsNullSeatCountsAsZeroForRemainingCalculation() {
        SoftwareLicense license = license(7L, null, null);

        var response = AssetVaultMapper.toSoftwareLicenseResponse(license, List.of());

        assertThat(response.totalSeats()).isNull();
        assertThat(response.usedSeats()).isNull();
        assertThat(response.remainingSeats()).isZero();
    }

    private SoftwareLicense license(Long id, Integer totalSeats, Integer usedSeats) {
        return SoftwareLicense.builder()
                .id(id)
                .softwareName("IntelliJ IDEA")
                .licenceKey("LIC-001")
                .licenseType(LicenseType.FLOATING)
                .vendor("JetBrains")
                .totalSeats(totalSeats)
                .usedSeats(usedSeats)
                .purchaseDate(LocalDate.of(2026, 4, 1))
                .expiryDate(LocalDate.of(2027, 4, 1))
                .isActive(true)
                .build();
    }

    private Employee employee() {
        return Employee.builder()
                .id(1L)
                .name("Aarav")
                .email("aarav@example.com")
                .department("Engineering")
                .designation("Engineer")
                .employeeCode("EMP-00001")
                .isActive(true)
                .createdAt(LocalDateTime.of(2026, 4, 21, 9, 0))
                .build();
    }
}
