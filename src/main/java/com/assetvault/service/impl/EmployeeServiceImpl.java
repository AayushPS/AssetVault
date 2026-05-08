package com.assetvault.service.impl;

import com.assetvault.dto.AssetAssignmentResponse;
import com.assetvault.dto.AssetResponse;
import com.assetvault.dto.EmployeeRequest;
import com.assetvault.dto.EmployeeResponse;
import com.assetvault.dto.SoftwareLicenseResponse;
import com.assetvault.exception.DuplicateEmployeeException;
import com.assetvault.exception.EmployeeHasActiveAssetsException;
import com.assetvault.exception.EmployeeNotFoundException;
import com.assetvault.model.Employee;
import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.model.SoftwareAssignment;
import com.assetvault.model.SoftwareLicense;
import com.assetvault.repository.AssetAssignmentRepository;
import com.assetvault.repository.EmployeeRepository;
import com.assetvault.repository.SoftwareAssignmentRepository;
import com.assetvault.service.EmployeeService;
import com.assetvault.util.CodeGenerator;
import com.assetvault.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for employee operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final AssetAssignmentRepository assetAssignmentRepository;
    private final SoftwareAssignmentRepository softwareAssignmentRepository;

    /**
     * Creates a new employee.
     *
     * @param request the request payload
     * @return the resulting employee
     */
    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.email())) {
            throw new DuplicateEmployeeException("Employee email already exists");
        }
        boolean generatedCode = !hasText(request.employeeCode());
        String employeeCode = generatedCode ? CodeGenerator.pendingEmployeeCode() : request.employeeCode();
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
        Employee saved = generatedCode ? employeeRepository.saveAndFlush(employee) : employeeRepository.save(employee);
        if (generatedCode) {
            saved.setEmployeeCode(CodeGenerator.employeeCode(saved.getId()));
            saved = employeeRepository.save(saved);
        }
        log.info("Employee registered: {}", saved.getEmployeeCode());
        return Mapper.toEmployeeResponse(saved);
    }

    /**
     * Returns the requested page of employees.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAll(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(Mapper::toEmployeeResponse);
    }

    /**
     * Returns the employee identified by the given id.
     *
     * @param id the database identifier
     * @return the resulting employee
     */
    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        return Mapper.toEmployeeResponse(findEmployee(id));
    }

    /**
     * Executes the search by name operation.
     *
     * @param name the name or label value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> searchByName(String name, Pageable pageable) {
        return employeeRepository.searchByName(name, pageable).map(Mapper::toEmployeeResponse);
    }

    /**
     * Returns employees associated with the supplied department.
     *
     * @param name the name or label value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getByDepartment(String name, Pageable pageable) {
        return employeeRepository.findByDepartment(name, pageable).map(Mapper::toEmployeeResponse);
    }

    /**
     * Executes the get assigned assets operation.
     *
     * @param id the database identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getAssignedAssets(Long id, Pageable pageable) {
        findEmployee(id);
        return employeeRepository.findActiveAssetsForEmployee(id, pageable).map(Mapper::toAssetResponse);
    }

    /**
     * Executes the get assigned licenses operation.
     *
     * @param id the database identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SoftwareLicenseResponse> getAssignedLicenses(Long id, Pageable pageable) {
        findEmployee(id);
        return employeeRepository.findLicensesAssignedToEmployee(id, pageable)
                .map(this::toLicenseResponse);
    }

    /**
     * Executes the get assignment history operation.
     *
     * @param id the database identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AssetAssignmentResponse> getAssignmentHistory(Long id, Pageable pageable) {
        findEmployee(id);
        return assetAssignmentRepository.findByEmployeeId(id, pageable)
                .map(Mapper::toAssetAssignmentResponse);
    }

    /**
     * Updates an existing employee.
     *
     * @param id the database identifier
     * @param request the request payload
     * @return the resulting employee
     */
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
        return Mapper.toEmployeeResponse(employeeRepository.save(employee));
    }

    /**
     * Deactivates the employee.
     *
     * @param id the database identifier
     * @return the resulting employee
     */
    @Override
    @Transactional
    public EmployeeResponse deactivate(Long id) {
        Employee employee = findEmployee(id);
        if (assetAssignmentRepository.existsByEmployeeIdAndStatus(id, AssignmentStatus.ACTIVE)) {
            throw new EmployeeHasActiveAssetsException("Cannot deactivate employee with unreturned assets");
        }
        employee.setActive(false);
        return Mapper.toEmployeeResponse(employeeRepository.save(employee));
    }

    /**
     * Deletes the employee.
     *
     * @param id the database identifier
     */
    @Override
    @Transactional
    public void delete(Long id) {
        employeeRepository.delete(findEmployee(id));
    }

    /**
     * Executes the find employee operation.
     *
     * @param id the database identifier
     * @return the resulting employee
     */
    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee ID %d does not exist".formatted(id)));
    }

    /**
     * Maps the supplied domain object to its response representation.
     *
     * @param license the software license entity to map or update
     * @return the mapped response payload
     */
    private SoftwareLicenseResponse toLicenseResponse(SoftwareLicense license) {
        List<EmployeeResponse> assignedEmployees = softwareAssignmentRepository
                .findAllBySoftwareLicenseIdAndStatusOrderByAssignedDateDescIdDesc(
                        license.getId(),
                        AssignmentStatus.ACTIVE
                )
                .stream()
                .map(SoftwareAssignment::getEmployee)
                .map(Mapper::toEmployeeResponse)
                .toList();
        return Mapper.toSoftwareLicenseResponse(license, assignedEmployees);
    }

    /**
     * Executes the has text operation.
     *
     * @param value the value to inspect
     * @return true when the requested condition is satisfied; otherwise false
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
