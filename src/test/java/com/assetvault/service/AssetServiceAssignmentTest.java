package com.assetvault.service;

import com.assetvault.dto.AssetAssignmentRequest;
import com.assetvault.exception.AssetAlreadyAssignedException;
import com.assetvault.exception.AssetNotAvailableException;
import com.assetvault.exception.AssetRetiredException;
import com.assetvault.exception.AssignmentNotFoundException;
import com.assetvault.exception.InactiveEmployeeException;
import com.assetvault.model.Asset;
import com.assetvault.model.AssetAssignment;
import com.assetvault.model.Employee;
import com.assetvault.model.enums.AssetStatus;
import com.assetvault.model.enums.AssetType;
import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.repository.AssetAssignmentRepository;
import com.assetvault.repository.AssetRepository;
import com.assetvault.repository.EmployeeRepository;
import com.assetvault.service.impl.AssetAssignmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetServiceAssignmentTest {
    @Mock
    private AssetAssignmentRepository assignmentRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private AssetAssignmentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AssetAssignmentServiceImpl(assignmentRepository, assetRepository, employeeRepository);
    }

    @Test
    void assignMovesAvailableAssetToAssigned() {
        Asset asset = asset(AssetStatus.AVAILABLE);
        Employee employee = employee(true);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(assignmentRepository.existsByAssetIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(false);
        when(assignmentRepository.save(any(AssetAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.assign(new AssetAssignmentRequest(1L, 1L, LocalDate.now(), "IT Admin", "Onboarding"));

        assertThat(asset.getStatus()).isEqualTo(AssetStatus.ASSIGNED);
        verify(assetRepository).save(asset);
    }

    @Test
    void assignDefaultsMissingDateAndPersistsAssignmentDetails() {
        Asset asset = asset(AssetStatus.AVAILABLE);
        Employee employee = employee(true);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(assignmentRepository.existsByAssetIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(false);
        when(assignmentRepository.save(any(AssetAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.assign(new AssetAssignmentRequest(1L, 1L, null, "IT Admin", "Onboarding"));

        ArgumentCaptor<AssetAssignment> captor = ArgumentCaptor.forClass(AssetAssignment.class);
        verify(assignmentRepository).save(captor.capture());
        AssetAssignment saved = captor.getValue();
        assertThat(saved.getAssignedDate()).isEqualTo(LocalDate.now());
        assertThat(saved.getStatus()).isEqualTo(AssignmentStatus.ACTIVE);
        assertThat(saved.getAssignedBy()).isEqualTo("IT Admin");
        assertThat(saved.getRemarks()).isEqualTo("Onboarding");
        assertThat(saved.getAsset()).isSameAs(asset);
        assertThat(saved.getEmployee()).isSameAs(employee);
    }

    @Test
    void assignRejectsUnavailableAsset() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset(AssetStatus.UNDER_MAINTENANCE)));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(true)));

        assertThatThrownBy(() -> service.assign(
                new AssetAssignmentRequest(1L, 1L, LocalDate.now(), "IT Admin", null)
        )).isInstanceOf(AssetNotAvailableException.class);
    }

    @Test
    void assignRejectsRetiredAsset() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset(AssetStatus.RETIRED)));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(true)));

        assertThatThrownBy(() -> service.assign(
                new AssetAssignmentRequest(1L, 1L, LocalDate.now(), "IT Admin", null)
        )).isInstanceOf(AssetRetiredException.class);
    }

    @Test
    void assignRejectsAlreadyAssignedAsset() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset(AssetStatus.ASSIGNED)));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(true)));
        when(assignmentRepository.existsByAssetIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> service.assign(
                new AssetAssignmentRequest(1L, 1L, LocalDate.now(), "IT Admin", null)
        )).isInstanceOf(AssetAlreadyAssignedException.class);
    }

    @Test
    void assignRejectsInactiveEmployee() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset(AssetStatus.AVAILABLE)));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(false)));

        assertThatThrownBy(() -> service.assign(
                new AssetAssignmentRequest(1L, 1L, LocalDate.now(), "IT Admin", null)
        )).isInstanceOf(InactiveEmployeeException.class);
    }

    @Test
    void returnAssetFreesAsset() {
        Asset asset = asset(AssetStatus.ASSIGNED);
        Employee employee = employee(true);
        AssetAssignment assignment = AssetAssignment.builder()
                .asset(asset)
                .employee(employee)
                .assignedDate(LocalDate.now())
                .status(AssignmentStatus.ACTIVE)
                .assignedBy("IT Admin")
                .build();
        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(AssetAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.returnAsset(10L);

        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.RETURNED);
        assertThat(asset.getStatus()).isEqualTo(AssetStatus.AVAILABLE);
    }

    @Test
    void returnAssetRejectsNonActiveAssignmentsWithoutMutatingState() {
        Asset asset = asset(AssetStatus.ASSIGNED);
        AssetAssignment assignment = AssetAssignment.builder()
                .asset(asset)
                .employee(employee(true))
                .assignedDate(LocalDate.now())
                .status(AssignmentStatus.RETURNED)
                .assignedBy("IT Admin")
                .build();
        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> service.returnAsset(10L))
                .isInstanceOf(AssignmentNotFoundException.class);

        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.RETURNED);
        assertThat(assignment.getReturnedDate()).isNull();
        assertThat(asset.getStatus()).isEqualTo(AssetStatus.ASSIGNED);
        verify(assignmentRepository, never()).save(any());
        verify(assetRepository, never()).save(any());
    }

    @Test
    void transferRejectsSameEmployee() {
        AssetAssignment assignment = AssetAssignment.builder()
                .asset(asset(AssetStatus.ASSIGNED))
                .employee(employee(true))
                .assignedDate(LocalDate.now())
                .status(AssignmentStatus.ACTIVE)
                .assignedBy("IT Admin")
                .build();
        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(true)));

        assertThatThrownBy(() -> service.transfer(10L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("different");
    }

    @Test
    void dateRangeRejectsInvertedDates() {
        assertThatThrownBy(() -> service.getByDateRange(
                LocalDate.now(),
                LocalDate.now().minusDays(1),
                PageRequest.of(0, 10)
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dateRangeRejectsMissingBounds() {
        assertThatThrownBy(() -> service.getByDateRange(null, LocalDate.now(), PageRequest.of(0, 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required");
    }

    @Test
    void readMethodsCoverRepositoryQueries() {
        AssetAssignment assignment = AssetAssignment.builder()
                .asset(asset(AssetStatus.ASSIGNED))
                .employee(employee(true))
                .assignedDate(LocalDate.now())
                .status(AssignmentStatus.ACTIVE)
                .assignedBy("IT Admin")
                .build();
        var pageable = PageRequest.of(0, 10);
        when(assignmentRepository.findAll(pageable)).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(assignment)));
        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.findByStatus(AssignmentStatus.ACTIVE, pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(assignment)));
        when(assetRepository.findById(1L)).thenReturn(Optional.of(assignment.getAsset()));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(assignment.getEmployee()));
        when(assignmentRepository.findByAssetId(1L, pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(assignment)));
        when(assignmentRepository.findByEmployeeId(1L, pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(assignment)));
        when(assignmentRepository.findByAssignedDateBetween(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                pageable
        )).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(assignment)));

        assertThat(service.getAll(pageable)).hasSize(1);
        assertThat(service.getById(10L).status()).isEqualTo(AssignmentStatus.ACTIVE);
        assertThat(service.getActive(pageable)).hasSize(1);
        assertThat(service.getByAsset(1L, pageable)).hasSize(1);
        assertThat(service.getByEmployee(1L, pageable)).hasSize(1);
        assertThat(service.getByDateRange(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), pageable)).hasSize(1);
    }

    @Test
    void transferCreatesNewActiveAssignmentForTargetEmployee() {
        Asset asset = asset(AssetStatus.ASSIGNED);
        Employee fromEmployee = employee(1L, true);
        Employee toEmployee = employee(2L, true);
        AssetAssignment current = AssetAssignment.builder()
                .asset(asset)
                .employee(fromEmployee)
                .assignedDate(LocalDate.now().minusDays(1))
                .status(AssignmentStatus.ACTIVE)
                .assignedBy("IT Admin")
                .build();
        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(current));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(toEmployee));
        when(assignmentRepository.save(any(AssetAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.transfer(10L, 2L);

        assertThat(current.getStatus()).isEqualTo(AssignmentStatus.TRANSFERRED);
        assertThat(response.employeeId()).isEqualTo(2L);
        assertThat(response.status()).isEqualTo(AssignmentStatus.ACTIVE);
    }

    @Test
    void deleteActiveAssignmentRestoresAssignedAssetToAvailable() {
        Asset asset = asset(AssetStatus.ASSIGNED);
        AssetAssignment assignment = AssetAssignment.builder()
                .asset(asset)
                .employee(employee(true))
                .assignedDate(LocalDate.now())
                .status(AssignmentStatus.ACTIVE)
                .assignedBy("IT Admin")
                .build();
        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));

        service.delete(10L);

        assertThat(asset.getStatus()).isEqualTo(AssetStatus.AVAILABLE);
        verify(assetRepository).save(asset);
    }

    @Test
    void deleteReturnedAssignmentDoesNotRestoreAssetStatus() {
        Asset asset = asset(AssetStatus.ASSIGNED);
        AssetAssignment assignment = AssetAssignment.builder()
                .asset(asset)
                .employee(employee(true))
                .assignedDate(LocalDate.now())
                .status(AssignmentStatus.RETURNED)
                .assignedBy("IT Admin")
                .build();
        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));

        service.delete(10L);

        assertThat(asset.getStatus()).isEqualTo(AssetStatus.ASSIGNED);
        verify(assignmentRepository).delete(assignment);
        verify(assetRepository, never()).save(any());
    }

    private Asset asset(AssetStatus status) {
        return Asset.builder()
                .id(1L)
                .assetCode("LPT-00001")
                .name("MacBook Pro 14")
                .brand("Apple")
                .model("M3 Pro")
                .type(AssetType.LAPTOP)
                .status(status)
                .purchaseDate(LocalDate.now())
                .purchaseCost(BigDecimal.valueOf(149999))
                .serialNumber("SER-001")
                .build();
    }

    private Employee employee(boolean active) {
        return employee(1L, active);
    }

    private Employee employee(Long id, boolean active) {
        return Employee.builder()
                .id(id)
                .name("Aarav")
                .email("aarav@example.com")
                .department("Engineering")
                .designation("Engineer")
                .employeeCode("EMP-00001")
                .isActive(active)
                .build();
    }
}
