package com.assetvault.service;

import com.assetvault.dto.EmployeeRequest;
import com.assetvault.exception.DuplicateEmployeeException;
import com.assetvault.exception.EmployeeHasActiveAssetsException;
import com.assetvault.exception.EmployeeNotFoundException;
import com.assetvault.model.Asset;
import com.assetvault.model.AssetAssignment;
import com.assetvault.model.Employee;
import com.assetvault.model.enums.AssetStatus;
import com.assetvault.model.enums.AssetType;
import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.model.enums.LicenseType;
import com.assetvault.model.SoftwareLicense;
import com.assetvault.repository.AssetAssignmentRepository;
import com.assetvault.repository.EmployeeRepository;
import com.assetvault.repository.SoftwareAssignmentRepository;
import com.assetvault.service.impl.EmployeeServiceImpl;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AssetAssignmentRepository assetAssignmentRepository;

    @Mock
    private SoftwareAssignmentRepository softwareAssignmentRepository;

    private EmployeeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmployeeServiceImpl(
                employeeRepository,
                assetAssignmentRepository,
                softwareAssignmentRepository
        );
    }

    @Test
    void createAutoGeneratesEmployeeCodeWhenMissing() {
        when(employeeRepository.existsByEmail("aarav@example.com")).thenReturn(false);
        when(employeeRepository.count()).thenReturn(0L);
        when(employeeRepository.existsByEmployeeCode("EMP-00001")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(request(null));

        assertThat(response.employeeCode()).isEqualTo("EMP-00001");
        assertThat(response.active()).isTrue();
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(employeeRepository.existsByEmail("aarav@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request(null)))
                .isInstanceOf(DuplicateEmployeeException.class)
                .hasMessageContaining("email");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicateEmployeeCode() {
        when(employeeRepository.existsByEmail("aarav@example.com")).thenReturn(false);
        when(employeeRepository.existsByEmployeeCode("EMP-00099")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("EMP-00099")))
                .isInstanceOf(DuplicateEmployeeException.class)
                .hasMessageContaining("code");
    }

    @Test
    void updateRejectsDuplicateEmail() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee()));
        when(employeeRepository.existsByEmailIgnoreCaseAndIdNot("aarav@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request("EMP-00001")))
                .isInstanceOf(DuplicateEmployeeException.class)
                .hasMessageContaining("email");
    }

    @Test
    void deactivateRejectsEmployeeWithActiveAssets() {
        Employee employee = employee();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(assetAssignmentRepository.existsByEmployeeIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> service.deactivate(1L))
                .isInstanceOf(EmployeeHasActiveAssetsException.class);

        assertThat(employee.isActive()).isTrue();
    }

    @Test
    void assignedAssetsRequireExistingEmployee() {
        when(employeeRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAssignedAssets(404L, PageRequest.of(0, 10)))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void readAndMutationMethodsCoverHappyPaths() {
        Employee employee = employee();
        var pageable = PageRequest.of(0, 10);
        SoftwareLicense license = license();
        when(employeeRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(employee)));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.searchByName("aar", pageable)).thenReturn(new PageImpl<>(List.of(employee)));
        when(employeeRepository.findByDepartment("Engineering", pageable)).thenReturn(new PageImpl<>(List.of(employee)));
        when(employeeRepository.findActiveAssetsForEmployee(1L, pageable)).thenReturn(new PageImpl<>(List.of(asset())));
        when(employeeRepository.findLicensesAssignedToEmployee(1L, pageable)).thenReturn(new PageImpl<>(List.of(license)));
        when(softwareAssignmentRepository.findAllBySoftwareLicenseIdAndStatusOrderByAssignedDateDescIdDesc(
                1L,
                AssignmentStatus.ACTIVE
        )).thenReturn(List.of());
        when(assetAssignmentRepository.findByEmployeeId(1L, pageable)).thenReturn(new PageImpl<>(List.of(assignment(employee))));
        when(employeeRepository.existsByEmailIgnoreCaseAndIdNot("aarav@example.com", 1L)).thenReturn(false);
        when(employeeRepository.existsByEmployeeCodeAndIdNot("EMP-00001", 1L)).thenReturn(false);
        when(assetAssignmentRepository.existsByEmployeeIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.getAll(pageable)).hasSize(1);
        assertThat(service.getById(1L).employeeCode()).isEqualTo("EMP-00001");
        assertThat(service.searchByName("aar", pageable)).hasSize(1);
        assertThat(service.getByDepartment("Engineering", pageable)).hasSize(1);
        assertThat(service.getAssignedAssets(1L, pageable)).hasSize(1);
        assertThat(service.getAssignedLicenses(1L, pageable)).hasSize(1);
        assertThat(service.getAssignmentHistory(1L, pageable)).hasSize(1);
        assertThat(service.update(1L, request("EMP-00001")).employeeCode()).isEqualTo("EMP-00001");
        assertThat(service.deactivate(1L).active()).isFalse();
        service.delete(1L);

        verify(employeeRepository).delete(employee);
    }

    private EmployeeRequest request(String employeeCode) {
        return new EmployeeRequest(
                "Aarav",
                "aarav@example.com",
                "Engineering",
                "Engineer",
                employeeCode
        );
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
                .build();
    }

    private Asset asset() {
        return Asset.builder()
                .id(1L)
                .assetCode("LPT-00001")
                .name("MacBook Pro 14")
                .brand("Apple")
                .model("M3 Pro")
                .type(AssetType.LAPTOP)
                .status(AssetStatus.ASSIGNED)
                .purchaseDate(LocalDate.now())
                .purchaseCost(BigDecimal.valueOf(149999))
                .serialNumber("SER-001")
                .build();
    }

    private AssetAssignment assignment(Employee employee) {
        return AssetAssignment.builder()
                .id(1L)
                .asset(asset())
                .employee(employee)
                .assignedDate(LocalDate.now())
                .status(AssignmentStatus.ACTIVE)
                .assignedBy("IT Admin")
                .build();
    }

    private SoftwareLicense license() {
        return SoftwareLicense.builder()
                .id(1L)
                .softwareName("IntelliJ IDEA")
                .licenceKey("LIC-001")
                .licenseType(LicenseType.FLOATING)
                .vendor("JetBrains")
                .totalSeats(2)
                .usedSeats(1)
                .purchaseDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusDays(30))
                .isActive(true)
                .build();
    }
}
