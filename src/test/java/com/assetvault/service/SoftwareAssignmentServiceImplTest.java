package com.assetvault.service;

import com.assetvault.exception.AssignmentNotFoundException;
import com.assetvault.exception.InactiveEmployeeException;
import com.assetvault.exception.LicenseAlreadyAssignedException;
import com.assetvault.exception.LicenseExpiredException;
import com.assetvault.exception.NoLicenseSeatsAvailableException;
import com.assetvault.model.Employee;
import com.assetvault.model.SoftwareAssignment;
import com.assetvault.model.SoftwareLicense;
import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.model.enums.LicenseType;
import com.assetvault.repository.EmployeeRepository;
import com.assetvault.repository.SoftwareAssignmentRepository;
import com.assetvault.repository.SoftwareLicenseRepository;
import com.assetvault.service.impl.SoftwareAssignmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SoftwareAssignmentServiceImplTest {
    @Mock
    private SoftwareLicenseRepository softwareLicenseRepository;

    @Mock
    private SoftwareAssignmentRepository softwareAssignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private SoftwareAssignmentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SoftwareAssignmentServiceImpl(
                softwareLicenseRepository,
                softwareAssignmentRepository,
                employeeRepository
        );
    }

    @Test
    void assignRejectsExpiredLicense() {
        when(softwareLicenseRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(license(1, LocalDate.now().minusDays(1))));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee()));

        assertThatThrownBy(() -> service.assign(1L, 1L))
                .isInstanceOf(LicenseExpiredException.class);
    }

    @Test
    void assignRejectsInactiveEmployee() {
        when(softwareLicenseRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(license(1, LocalDate.now().plusDays(30))));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(false)));

        assertThatThrownBy(() -> service.assign(1L, 1L))
                .isInstanceOf(InactiveEmployeeException.class);
    }

    @Test
    void assignRejectsInactiveLicense() {
        SoftwareLicense license = license(1, LocalDate.now().plusDays(30));
        license.setIsActive(false);
        when(softwareLicenseRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(license));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee()));

        assertThatThrownBy(() -> service.assign(1L, 1L))
                .isInstanceOf(NoLicenseSeatsAvailableException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void assignRejectsEmployeeAlreadyHoldingLicense() {
        when(softwareLicenseRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(license(2, LocalDate.now().plusDays(30))));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee()));
        when(softwareAssignmentRepository.existsByEmployeeIdAndSoftwareLicenseIdAndStatus(
                1L,
                1L,
                AssignmentStatus.ACTIVE
        )).thenReturn(true);

        assertThatThrownBy(() -> service.assign(1L, 1L))
                .isInstanceOf(LicenseAlreadyAssignedException.class);
    }

    @Test
    void assignRejectsWhenSeatsAreExhausted() {
        when(softwareLicenseRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(license(1, LocalDate.now().plusDays(30))));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee()));
        when(softwareAssignmentRepository.existsByEmployeeIdAndSoftwareLicenseIdAndStatus(
                1L,
                1L,
                AssignmentStatus.ACTIVE
        )).thenReturn(false);
        when(softwareAssignmentRepository.countBySoftwareLicenseIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(1L);

        assertThatThrownBy(() -> service.assign(1L, 1L))
                .isInstanceOf(NoLicenseSeatsAvailableException.class);
    }

    @Test
    void assignConsumesASeat() {
        SoftwareLicense license = license(2, LocalDate.now().plusDays(30));
        Employee employee = employee();
        when(softwareLicenseRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(license));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(softwareAssignmentRepository.existsByEmployeeIdAndSoftwareLicenseIdAndStatus(
                1L,
                1L,
                AssignmentStatus.ACTIVE
        )).thenReturn(false);
        when(softwareAssignmentRepository.countBySoftwareLicenseIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(0L);
        when(softwareAssignmentRepository.save(any(SoftwareAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(softwareLicenseRepository.save(any(SoftwareLicense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(softwareAssignmentRepository.findAllBySoftwareLicenseIdAndStatusOrderByAssignedDateDescIdDesc(
                1L,
                AssignmentStatus.ACTIVE
        )).thenReturn(List.of());

        service.assign(1L, 1L);

        assertThat(license.getUsedSeats()).isEqualTo(1);
        ArgumentCaptor<SoftwareAssignment> captor = ArgumentCaptor.forClass(SoftwareAssignment.class);
        verify(softwareAssignmentRepository).save(captor.capture());
        SoftwareAssignment savedAssignment = captor.getValue();
        assertThat(savedAssignment.getSoftwareLicense()).isSameAs(license);
        assertThat(savedAssignment.getEmployee()).isSameAs(employee);
        assertThat(savedAssignment.getSeatIndex()).isEqualTo(1);
        assertThat(savedAssignment.getAssignedDate()).isEqualTo(LocalDate.now());
        assertThat(savedAssignment.getStatus()).isEqualTo(AssignmentStatus.ACTIVE);
        assertThat(savedAssignment.getAssignedBy()).isEqualTo("SYSTEM");
        assertThat(savedAssignment.getRemarks()).isEqualTo("License seat assigned");
    }

    @Test
    void revokeReturnsSeatToPool() {
        SoftwareLicense license = license(2, LocalDate.now().plusDays(30));
        license.setUsedSeats(1);
        SoftwareAssignment assignment = assignment(license);
        when(softwareLicenseRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(license));
        when(softwareAssignmentRepository.findByEmployeeIdAndSoftwareLicenseIdAndStatus(
                1L,
                1L,
                AssignmentStatus.ACTIVE
        )).thenReturn(Optional.of(assignment));
        when(softwareAssignmentRepository.save(any(SoftwareAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(softwareAssignmentRepository.countBySoftwareLicenseIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(0L);
        when(softwareLicenseRepository.save(any(SoftwareLicense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(softwareAssignmentRepository.findAllBySoftwareLicenseIdAndStatusOrderByAssignedDateDescIdDesc(
                1L,
                AssignmentStatus.ACTIVE
        )).thenReturn(List.of());

        service.revoke(1L, 1L);

        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.RETURNED);
        assertThat(license.getUsedSeats()).isZero();
    }

    @Test
    void revokeRejectsWhenEmployeeDoesNotHoldActiveSeat() {
        when(softwareLicenseRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(license(2, LocalDate.now().plusDays(30))));
        when(softwareAssignmentRepository.findByEmployeeIdAndSoftwareLicenseIdAndStatus(
                1L,
                1L,
                AssignmentStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.revoke(1L, 1L))
                .isInstanceOf(AssignmentNotFoundException.class)
                .hasMessageContaining("Active software assignment");
    }

    @Test
    void revokeActiveAssignmentsReturnsAllActiveSeats() {
        SoftwareAssignment first = assignment(license(2, LocalDate.now().plusDays(30)));
        SoftwareAssignment second = assignment(license(2, LocalDate.now().plusDays(30)));
        when(softwareAssignmentRepository.findAllBySoftwareLicenseIdAndStatus(1L, AssignmentStatus.ACTIVE))
                .thenReturn(List.of(first, second));

        assertThat(service.revokeActiveAssignmentsForLicense(1L, "Bulk return")).isEqualTo(2);

        assertThat(first.getStatus()).isEqualTo(AssignmentStatus.RETURNED);
        assertThat(second.getStatus()).isEqualTo(AssignmentStatus.RETURNED);
        verify(softwareAssignmentRepository).saveAll(List.of(first, second));
    }

    @Test
    void deleteAssignmentsForLicenseDelegatesToRepository() {
        when(softwareAssignmentRepository.deleteBySoftwareLicenseId(1L)).thenReturn(3L);

        assertThat(service.deleteAssignmentsForLicense(1L)).isEqualTo(3L);
    }

    private SoftwareAssignment assignment(SoftwareLicense license) {
        return SoftwareAssignment.builder()
                .softwareLicense(license)
                .employee(employee())
                .seatIndex(1)
                .assignedDate(LocalDate.now())
                .status(AssignmentStatus.ACTIVE)
                .assignedBy("SYSTEM")
                .build();
    }

    private SoftwareLicense license(int totalSeats, LocalDate expiryDate) {
        return SoftwareLicense.builder()
                .id(1L)
                .softwareName("IntelliJ IDEA")
                .licenceKey("LIC-001")
                .licenseType(LicenseType.FLOATING)
                .vendor("JetBrains")
                .totalSeats(totalSeats)
                .usedSeats(0)
                .purchaseDate(LocalDate.now())
                .expiryDate(expiryDate)
                .isActive(true)
                .build();
    }

    private Employee employee() {
        return employee(true);
    }

    private Employee employee(boolean active) {
        return Employee.builder()
                .id(1L)
                .name("Aarav")
                .email("aarav@example.com")
                .department("Engineering")
                .designation("Engineer")
                .employeeCode("EMP-00001")
                .isActive(active)
                .build();
    }
}
