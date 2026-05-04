package com.Gemini.AssetVault.Service;

import com.Gemini.AssetVault.Dto.SoftwareLicenseRequest;
import com.Gemini.AssetVault.Exception.AssignmentNotFoundException;
import com.Gemini.AssetVault.Exception.DuplicateLicenseException;
import com.Gemini.AssetVault.Exception.InactiveEmployeeException;
import com.Gemini.AssetVault.Exception.LicenseAlreadyAssignedException;
import com.Gemini.AssetVault.Exception.LicenseExpiredException;
import com.Gemini.AssetVault.Exception.NoLicenseSeatsAvailableException;
import com.Gemini.AssetVault.Model.Employee;
import com.Gemini.AssetVault.Model.Enum.AssignmentStatus;
import com.Gemini.AssetVault.Model.Enum.LicenseType;
import com.Gemini.AssetVault.Model.SoftwareAssignment;
import com.Gemini.AssetVault.Model.SoftwareLicense;
import com.Gemini.AssetVault.Repository.EmployeeRepository;
import com.Gemini.AssetVault.Repository.SoftwareAssignmentRepository;
import com.Gemini.AssetVault.Repository.SoftwareLicenseRepository;
import com.Gemini.AssetVault.Service.Impl.SoftwareLicenseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SoftwareLicenseServiceImplTest {
    @Mock
    private SoftwareLicenseRepository softwareLicenseRepository;

    @Mock
    private SoftwareAssignmentRepository softwareAssignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private SoftwareLicenseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SoftwareLicenseServiceImpl(
                softwareLicenseRepository,
                softwareAssignmentRepository,
                employeeRepository
        );
    }

    @Test
    void assignRejectsExpiredLicense() {
        when(softwareLicenseRepository.findById(1L)).thenReturn(Optional.of(license(1, LocalDate.now().minusDays(1))));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee()));

        assertThatThrownBy(() -> service.assign(1L, 1L))
                .isInstanceOf(LicenseExpiredException.class);
    }

    @Test
    void createRejectsDuplicateLicenseKey() {
        when(softwareLicenseRepository.existsByLicenseKey("LIC-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request(1, 0)))
                .isInstanceOf(DuplicateLicenseException.class);
    }

    @Test
    void createRejectsUsedSeatsGreaterThanTotalSeats() {
        when(softwareLicenseRepository.existsByLicenseKey("LIC-001")).thenReturn(false);

        assertThatThrownBy(() -> service.create(request(1, 2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Used seats");
    }

    @Test
    void createDefaultsOptionalSeatsAndActiveFlag() {
        when(softwareLicenseRepository.existsByLicenseKey("LIC-001")).thenReturn(false);
        when(softwareLicenseRepository.save(any(SoftwareLicense.class))).thenAnswer(invocation -> {
            SoftwareLicense saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(softwareAssignmentRepository.findAllBySoftwareLicenseIdAndStatusOrderByAssignedDateDescIdDesc(
                1L,
                AssignmentStatus.ACTIVE
        )).thenReturn(List.of());

        var response = service.create(request(5, null, null));

        assertThat(response.usedSeats()).isZero();
        assertThat(response.active()).isTrue();
        assertThat(response.remainingSeats()).isEqualTo(5);
    }

    @Test
    void assignRejectsInactiveEmployee() {
        when(softwareLicenseRepository.findById(1L)).thenReturn(Optional.of(license(1, LocalDate.now().plusDays(30))));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(false)));

        assertThatThrownBy(() -> service.assign(1L, 1L))
                .isInstanceOf(InactiveEmployeeException.class);
    }

    @Test
    void assignRejectsInactiveLicense() {
        SoftwareLicense license = license(1, LocalDate.now().plusDays(30));
        license.setIsActive(false);
        when(softwareLicenseRepository.findById(1L)).thenReturn(Optional.of(license));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee()));

        assertThatThrownBy(() -> service.assign(1L, 1L))
                .isInstanceOf(NoLicenseSeatsAvailableException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void assignRejectsEmployeeAlreadyHoldingLicense() {
        when(softwareLicenseRepository.findById(1L)).thenReturn(Optional.of(license(2, LocalDate.now().plusDays(30))));
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
        when(softwareLicenseRepository.findById(1L)).thenReturn(Optional.of(license(1, LocalDate.now().plusDays(30))));
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
        when(softwareLicenseRepository.findById(1L)).thenReturn(Optional.of(license));
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
        SoftwareAssignment assignment = SoftwareAssignment.builder()
                .softwareLicense(license)
                .employee(employee())
                .seatIndex(1)
                .assignedDate(LocalDate.now())
                .status(AssignmentStatus.ACTIVE)
                .assignedBy("SYSTEM")
                .build();
        when(softwareLicenseRepository.findById(1L)).thenReturn(Optional.of(license));
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
        when(softwareLicenseRepository.findById(1L)).thenReturn(Optional.of(license(2, LocalDate.now().plusDays(30))));
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
    void readMethodsCoverRepositoryQueries() {
        SoftwareLicense license = license(2, LocalDate.now().plusDays(30));
        var pageable = PageRequest.of(0, 10);
        when(softwareLicenseRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(license)));
        when(softwareLicenseRepository.findById(1L)).thenReturn(Optional.of(license));
        when(softwareLicenseRepository.findLicensesExpiringSoon(any(), any(), any())).thenReturn(new PageImpl<>(List.of(license)));
        when(softwareLicenseRepository.findExpiredLicenses(any(), any())).thenReturn(new PageImpl<>(List.of(license)));
        when(softwareLicenseRepository.findLicensesWithNoRemainingSeats(pageable)).thenReturn(new PageImpl<>(List.of(license)));
        when(softwareLicenseRepository.searchBySoftwareName("idea", pageable)).thenReturn(new PageImpl<>(List.of(license)));
        when(softwareAssignmentRepository.findAllBySoftwareLicenseIdAndStatusOrderByAssignedDateDescIdDesc(
                1L,
                AssignmentStatus.ACTIVE
        )).thenReturn(List.of());

        assertThat(service.getAll(pageable)).hasSize(1);
        assertThat(service.getById(1L).licenseKey()).isEqualTo("LIC-001");
        assertThat(service.getExpiringSoon(30, pageable)).hasSize(1);
        assertThat(service.getExpired(pageable)).hasSize(1);
        assertThat(service.getLowSeats(pageable)).hasSize(1);
        assertThat(service.search("idea", pageable)).hasSize(1);
    }

    @Test
    void updateDeactivateAndDeleteCoverMutationPaths() {
        SoftwareLicense updateLicense = license(2, LocalDate.now().plusDays(30));
        SoftwareLicense deactivateLicense = license(2, LocalDate.now().plusDays(30));
        SoftwareLicense deleteLicense = license(2, LocalDate.now().plusDays(30));
        when(softwareLicenseRepository.findById(1L)).thenReturn(Optional.of(updateLicense));
        when(softwareLicenseRepository.findById(2L)).thenReturn(Optional.of(deactivateLicense));
        when(softwareLicenseRepository.findById(3L)).thenReturn(Optional.of(deleteLicense));
        when(softwareLicenseRepository.existsByLicenceKeyAndIdNot("LIC-001", 1L)).thenReturn(false);
        when(softwareLicenseRepository.save(any(SoftwareLicense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(softwareAssignmentRepository.findAllBySoftwareLicenseIdAndStatusOrderByAssignedDateDescIdDesc(
                1L,
                AssignmentStatus.ACTIVE
        )).thenReturn(List.of());

        assertThat(service.update(1L, request(2, 1)).usedSeats()).isEqualTo(1);
        assertThat(service.deactivate(2L).active()).isFalse();
        service.delete(3L);

        verify(softwareLicenseRepository).delete(deleteLicense);
    }

    @Test
    void updateRejectsDuplicateLicenseKeyAndInvalidSeatCounts() {
        when(softwareLicenseRepository.findById(1L)).thenReturn(Optional.of(license(2, LocalDate.now().plusDays(30))));
        when(softwareLicenseRepository.findById(2L)).thenReturn(Optional.of(license(2, LocalDate.now().plusDays(30))));
        when(softwareLicenseRepository.existsByLicenceKeyAndIdNot("LIC-001", 1L)).thenReturn(true);
        when(softwareLicenseRepository.existsByLicenceKeyAndIdNot("LIC-001", 2L)).thenReturn(false);

        assertThatThrownBy(() -> service.update(1L, request(2, 1)))
                .isInstanceOf(DuplicateLicenseException.class);
        assertThatThrownBy(() -> service.update(2L, request(1, 2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Used seats");
    }

    private SoftwareLicenseRequest request(int totalSeats, int usedSeats) {
        return request(totalSeats, usedSeats, true);
    }

    private SoftwareLicenseRequest request(int totalSeats, Integer usedSeats, Boolean active) {
        return new SoftwareLicenseRequest(
                "IntelliJ IDEA",
                "LIC-001",
                LicenseType.FLOATING,
                "JetBrains",
                totalSeats,
                usedSeats,
                LocalDate.now(),
                null,
                LocalDate.now().plusDays(30),
                active
        );
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
