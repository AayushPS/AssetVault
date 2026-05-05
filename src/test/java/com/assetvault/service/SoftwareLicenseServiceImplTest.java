package com.assetvault.service;

import com.assetvault.dto.SoftwareLicenseRequest;
import com.assetvault.exception.DuplicateLicenseException;
import com.assetvault.model.SoftwareLicense;
import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.model.enums.LicenseType;
import com.assetvault.repository.SoftwareAssignmentRepository;
import com.assetvault.repository.SoftwareLicenseRepository;
import com.assetvault.service.impl.SoftwareLicenseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private SoftwareAssignmentService softwareAssignmentService;

    private SoftwareLicenseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SoftwareLicenseServiceImpl(
                softwareLicenseRepository,
                softwareAssignmentRepository,
                softwareAssignmentService
        );
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
        when(softwareLicenseRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(deactivateLicense));
        when(softwareLicenseRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(deleteLicense));
        when(softwareLicenseRepository.existsByLicenceKeyAndIdNot("LIC-001", 1L)).thenReturn(false);
        when(softwareLicenseRepository.save(any(SoftwareLicense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(softwareAssignmentRepository.findAllBySoftwareLicenseIdAndStatusOrderByAssignedDateDescIdDesc(
                1L,
                AssignmentStatus.ACTIVE
        )).thenReturn(List.of());
        when(softwareAssignmentService.revokeActiveAssignmentsForLicense(2L, "License seat returned automatically before license deactivation"))
                .thenReturn(2);
        when(softwareAssignmentService.revokeActiveAssignmentsForLicense(3L, "License seat returned automatically before license deletion"))
                .thenReturn(1);
        when(softwareAssignmentService.deleteAssignmentsForLicense(3L)).thenReturn(3L);

        assertThat(service.update(1L, request(2, 1)).usedSeats()).isEqualTo(1);
        assertThat(service.deactivate(2L).active()).isFalse();
        service.delete(3L);

        assertThat(deactivateLicense.getUsedSeats()).isZero();
        verify(softwareLicenseRepository).delete(deleteLicense);
        verify(softwareAssignmentService).deleteAssignmentsForLicense(3L);
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
}
