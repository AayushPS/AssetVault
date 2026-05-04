package com.Gemini.AssetVault.Service.Impl;

import com.Gemini.AssetVault.Dto.AssetAssignmentResponse;
import com.Gemini.AssetVault.Dto.AssetResponse;
import com.Gemini.AssetVault.Dto.EmployeeRequest;
import com.Gemini.AssetVault.Dto.EmployeeResponse;
import com.Gemini.AssetVault.Dto.SoftwareLicenseResponse;
import com.Gemini.AssetVault.Exception.DuplicateEmployeeException;
import com.Gemini.AssetVault.Exception.EmployeeHasActiveAssetsException;
import com.Gemini.AssetVault.Exception.EmployeeNotFoundException;
import com.Gemini.AssetVault.Model.Employee;
import com.Gemini.AssetVault.Model.Enum.AssignmentStatus;
import com.Gemini.AssetVault.Model.SoftwareAssignment;
import com.Gemini.AssetVault.Model.SoftwareLicense;
import com.Gemini.AssetVault.Repository.AssetAssignmentRepository;
import com.Gemini.AssetVault.Repository.EmployeeRepository;
import com.Gemini.AssetVault.Repository.SoftwareAssignmentRepository;
import com.Gemini.AssetVault.Service.EmployeeService;
import com.Gemini.AssetVault.Util.AssetCodeGenerator;
import com.Gemini.AssetVault.Util.AssetVaultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final AssetAssignmentRepository assetAssignmentRepository;
    private final SoftwareAssignmentRepository softwareAssignmentRepository;

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.email())) {
            throw new DuplicateEmployeeException("Employee email already exists");
        }
        String employeeCode = hasText(request.employeeCode()) ? request.employeeCode() : generateEmployeeCode();
        if (employeeRepository.existsByEmployeeCode(employeeCode)) {
            throw new DuplicateEmployeeException("Employee code already exists");
        }
        Employee employee = Employee.builder()
                .name(request.name())
                .email(request.email())
                .department(request.department())
                .designation(request.designation())
                .employeeCode(employeeCode)
                .isActive(true)
                .build();
        Employee saved = employeeRepository.save(employee);
        log.info("Employee registered: {}", saved.getEmployeeCode());
        return AssetVaultMapper.toEmployeeResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAll(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(AssetVaultMapper::toEmployeeResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        return AssetVaultMapper.toEmployeeResponse(findEmployee(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> searchByName(String name, Pageable pageable) {
        return employeeRepository.searchByName(name, pageable).map(AssetVaultMapper::toEmployeeResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getByDepartment(String name, Pageable pageable) {
        return employeeRepository.findByDepartment(name, pageable).map(AssetVaultMapper::toEmployeeResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getAssignedAssets(Long id, Pageable pageable) {
        findEmployee(id);
        return employeeRepository.findActiveAssetsForEmployee(id, pageable).map(AssetVaultMapper::toAssetResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SoftwareLicenseResponse> getAssignedLicenses(Long id, Pageable pageable) {
        findEmployee(id);
        return employeeRepository.findLicensesAssignedToEmployee(id, pageable)
                .map(this::toLicenseResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetAssignmentResponse> getAssignmentHistory(Long id, Pageable pageable) {
        findEmployee(id);
        return assetAssignmentRepository.findByEmployeeId(id, pageable)
                .map(AssetVaultMapper::toAssetAssignmentResponse);
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = findEmployee(id);
        if (employeeRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new DuplicateEmployeeException("Employee email already exists");
        }
        if (hasText(request.employeeCode())
                && employeeRepository.existsByEmployeeCodeAndIdNot(request.employeeCode(), id)) {
            throw new DuplicateEmployeeException("Employee code already exists");
        }
        employee.setName(request.name());
        employee.setEmail(request.email());
        employee.setDepartment(request.department());
        employee.setDesignation(request.designation());
        if (hasText(request.employeeCode())) {
            employee.setEmployeeCode(request.employeeCode());
        }
        return AssetVaultMapper.toEmployeeResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponse deactivate(Long id) {
        Employee employee = findEmployee(id);
        if (assetAssignmentRepository.existsByEmployeeIdAndStatus(id, AssignmentStatus.ACTIVE)) {
            throw new EmployeeHasActiveAssetsException("Cannot deactivate employee with unreturned assets");
        }
        employee.setActive(false);
        return AssetVaultMapper.toEmployeeResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        employeeRepository.delete(findEmployee(id));
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee ID %d does not exist".formatted(id)));
    }

    private String generateEmployeeCode() {
        long sequence = employeeRepository.count() + 1;
        String code = AssetCodeGenerator.employeeCode(sequence);
        while (employeeRepository.existsByEmployeeCode(code)) {
            sequence++;
            code = AssetCodeGenerator.employeeCode(sequence);
        }
        return code;
    }

    private SoftwareLicenseResponse toLicenseResponse(SoftwareLicense license) {
        List<EmployeeResponse> assignedEmployees = softwareAssignmentRepository
                .findAllBySoftwareLicenseIdAndStatusOrderByAssignedDateDescIdDesc(
                        license.getId(),
                        AssignmentStatus.ACTIVE
                )
                .stream()
                .map(SoftwareAssignment::getEmployee)
                .map(AssetVaultMapper::toEmployeeResponse)
                .toList();
        return AssetVaultMapper.toSoftwareLicenseResponse(license, assignedEmployees);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
