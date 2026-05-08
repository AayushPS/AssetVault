package com.assetvault.service.impl;

import com.assetvault.dto.AssetAssignmentRequest;
import com.assetvault.dto.AssetAssignmentResponse;
import com.assetvault.exception.AssetAlreadyAssignedException;
import com.assetvault.exception.AssetNotAvailableException;
import com.assetvault.exception.AssetNotFoundException;
import com.assetvault.exception.AssetRetiredException;
import com.assetvault.exception.AssignmentNotFoundException;
import com.assetvault.exception.EmployeeNotFoundException;
import com.assetvault.exception.InactiveEmployeeException;
import com.assetvault.model.Asset;
import com.assetvault.model.AssetAssignment;
import com.assetvault.model.Employee;
import com.assetvault.model.enums.AssetStatus;
import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.repository.AssetAssignmentRepository;
import com.assetvault.repository.AssetRepository;
import com.assetvault.repository.EmployeeRepository;
import com.assetvault.service.AssetAssignmentService;
import com.assetvault.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Service implementation for asset assignment operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetAssignmentServiceImpl implements AssetAssignmentService {
    private final AssetAssignmentRepository assignmentRepository;
    private final AssetRepository assetRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Assigns the requested asset assignment.
     *
     * @param request the request payload
     * @return the resulting asset assignment
     */
    @Override
    @Transactional
    public AssetAssignmentResponse assign(AssetAssignmentRequest request) {
        Asset asset = findAsset(request.assetId());
        Employee employee = findEmployee(request.employeeId());
        ensureEmployeeActive(employee);
        ensureAssetAssignable(asset);

        AssetAssignment assignment = AssetAssignment.builder()
                .asset(asset)
                .employee(employee)
                .assignedDate(request.assignedDate() == null ? LocalDate.now() : request.assignedDate())
                .status(AssignmentStatus.ACTIVE)
                .assignedBy(request.assignedBy())
                .remarks(request.remarks())
                .build();
        asset.setStatus(AssetStatus.ASSIGNED);
        assetRepository.save(asset);
        AssetAssignment saved = assignmentRepository.save(assignment);
        log.info("Asset {} assigned to employee {}", asset.getAssetCode(), employee.getEmployeeCode());
        return Mapper.toAssetAssignmentResponse(saved);
    }

    /**
     * Returns the requested page of asset assignments.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AssetAssignmentResponse> getAll(Pageable pageable) {
        return assignmentRepository.findAll(pageable).map(Mapper::toAssetAssignmentResponse);
    }

    /**
     * Returns the asset assignment identified by the given id.
     *
     * @param id the database identifier
     * @return the resulting asset assignment
     */
    @Override
    @Transactional(readOnly = true)
    public AssetAssignmentResponse getById(Long id) {
        return Mapper.toAssetAssignmentResponse(findAssignment(id));
    }

    /**
     * Executes the get active operation.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AssetAssignmentResponse> getActive(Pageable pageable) {
        return assignmentRepository.findByStatus(AssignmentStatus.ACTIVE, pageable)
                .map(Mapper::toAssetAssignmentResponse);
    }

    /**
     * Executes the get by asset operation.
     *
     * @param assetId the asset identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AssetAssignmentResponse> getByAsset(Long assetId, Pageable pageable) {
        findAsset(assetId);
        return assignmentRepository.findByAssetId(assetId, pageable).map(Mapper::toAssetAssignmentResponse);
    }

    /**
     * Executes the get by employee operation.
     *
     * @param employeeId the employee identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AssetAssignmentResponse> getByEmployee(Long employeeId, Pageable pageable) {
        findEmployee(employeeId);
        return assignmentRepository.findByEmployeeId(employeeId, pageable)
                .map(Mapper::toAssetAssignmentResponse);
    }

    /**
     * Executes the get by date range operation.
     *
     * @param from the window start date
     * @param to the window end date
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AssetAssignmentResponse> getByDateRange(LocalDate from, LocalDate to, Pageable pageable) {
        validateDateRange(from, to);
        return assignmentRepository.findByAssignedDateBetween(from, to, pageable)
                .map(Mapper::toAssetAssignmentResponse);
    }

    /**
     * Returns the assigned asset to the available pool.
     *
     * @param id the database identifier
     * @return the resulting asset assignment
     */
    @Override
    @Transactional
    public AssetAssignmentResponse returnAsset(Long id) {
        AssetAssignment assignment = findAssignment(id);
        if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new AssignmentNotFoundException("Active assignment ID %d does not exist".formatted(id));
        }
        assignment.setStatus(AssignmentStatus.RETURNED);
        assignment.setReturnedDate(LocalDate.now());
        Asset asset = assignment.getAsset();
        if (asset.getStatus() == AssetStatus.ASSIGNED) {
            asset.setStatus(AssetStatus.AVAILABLE);
            assetRepository.save(asset);
        }
        AssetAssignment saved = assignmentRepository.save(assignment);
        log.info("Asset {} returned by employee {}", asset.getAssetCode(), assignment.getEmployee().getEmployeeCode());
        return Mapper.toAssetAssignmentResponse(saved);
    }

    /**
     * Transfers the current asset assignment to a different employee.
     *
     * @param id the database identifier
     * @param toEmployeeId the target employee identifier
     * @return the resulting asset assignment
     */
    @Override
    @Transactional
    public AssetAssignmentResponse transfer(Long id, Long toEmployeeId) {
        AssetAssignment current = findAssignment(id);
        if (current.getStatus() != AssignmentStatus.ACTIVE) {
            throw new AssignmentNotFoundException("Active assignment ID %d does not exist".formatted(id));
        }
        Employee newEmployee = findEmployee(toEmployeeId);
        ensureEmployeeActive(newEmployee);
        if (current.getEmployee().getId().equals(toEmployeeId)) {
            throw new IllegalArgumentException("Transfer target employee must be different");
        }
        current.setStatus(AssignmentStatus.TRANSFERRED);
        current.setReturnedDate(LocalDate.now());
        assignmentRepository.save(current);

        AssetAssignment transferred = AssetAssignment.builder()
                .asset(current.getAsset())
                .employee(newEmployee)
                .assignedDate(LocalDate.now())
                .status(AssignmentStatus.ACTIVE)
                .assignedBy(current.getAssignedBy())
                .remarks("Transferred from employee %s".formatted(current.getEmployee().getEmployeeCode()))
                .build();
        AssetAssignment saved = assignmentRepository.save(transferred);
        log.info("Asset {} transferred to employee {}", current.getAsset().getAssetCode(), newEmployee.getEmployeeCode());
        return Mapper.toAssetAssignmentResponse(saved);
    }

    /**
     * Releases active asset assignments for the supplied asset.
     *
     * @param assetId the asset identifier
     * @param remarks the reason captured for the state change
     * @return the computed numeric result
     */
    @Override
    @Transactional
    public int releaseActiveAssignmentsForAsset(Long assetId, String remarks) {
        return assignmentRepository.findByAssetIdAndStatus(assetId, AssignmentStatus.ACTIVE)
                .map(assignment -> releaseAssignment(assignment, remarks))
                .orElse(0);
    }

    /**
     * Deletes the asset assignment.
     *
     * @param id the database identifier
     */
    @Override
    @Transactional
    public void delete(Long id) {
        AssetAssignment assignment = findAssignment(id);
        Asset asset = assignment.getAsset();
        boolean active = assignment.getStatus() == AssignmentStatus.ACTIVE;
        assignmentRepository.delete(assignment);
        if (active && asset.getStatus() == AssetStatus.ASSIGNED) {
            asset.setStatus(AssetStatus.AVAILABLE);
            assetRepository.save(asset);
        }
    }

    /**
     * Executes the release assignment operation.
     *
     * @param assignment the assignment entity to map or update
     * @param remarks the reason captured for the state change
     * @return the computed numeric result
     */
    private int releaseAssignment(AssetAssignment assignment, String remarks) {
        assignment.setStatus(AssignmentStatus.RETURNED);
        assignment.setReturnedDate(LocalDate.now());
        assignment.setRemarks(appendRemark(assignment.getRemarks(), remarks));
        Asset asset = assignment.getAsset();
        if (asset.getStatus() == AssetStatus.ASSIGNED) {
            asset.setStatus(AssetStatus.AVAILABLE);
            assetRepository.save(asset);
        }
        assignmentRepository.save(assignment);
        log.info(
                "Asset {} automatically unassigned from employee {}",
                asset.getAssetCode(),
                assignment.getEmployee().getEmployeeCode()
        );
        return 1;
    }

    /**
     * Executes the append remark operation.
     *
     * @param current the current value
     * @param addition the addition value
     * @return the resulting asset assignment
     */
    private String appendRemark(String current, String addition) {
        if (addition == null || addition.isBlank()) {
            return current;
        }
        if (current == null || current.isBlank()) {
            return addition;
        }
        return current + "\n" + addition;
    }

    /**
     * Executes the find assignment operation.
     *
     * @param id the database identifier
     * @return the resulting asset assignment
     */
    private AssetAssignment findAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment ID %d does not exist".formatted(id)));
    }

    /**
     * Finds the asset entity for the supplied identifier.
     *
     * @param id the database identifier
     * @return the resulting asset assignment
     */
    private Asset findAsset(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new AssetNotFoundException("Asset ID %d does not exist".formatted(id)));
    }

    /**
     * Executes the find employee operation.
     *
     * @param id the database identifier
     * @return the resulting asset assignment
     */
    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee ID %d does not exist".formatted(id)));
    }

    /**
     * Executes the ensure employee active operation.
     *
     * @param employee the employee entity to map or validate
     */
    private void ensureEmployeeActive(Employee employee) {
        if (!employee.isActive()) {
            throw new InactiveEmployeeException("Cannot assign to a deactivated employee");
        }
    }

    /**
     * Executes the ensure asset assignable operation.
     *
     * @param asset the asset entity to map or validate
     */
    private void ensureAssetAssignable(Asset asset) {
        if (asset.getStatus() == AssetStatus.RETIRED) {
            throw new AssetRetiredException("Operation is not allowed on retired asset %s".formatted(asset.getAssetCode()));
        }
        if (assignmentRepository.existsByAssetIdAndStatus(asset.getId(), AssignmentStatus.ACTIVE)) {
            throw new AssetAlreadyAssignedException(
                    "Asset %s is already actively assigned".formatted(asset.getAssetCode())
            );
        }
        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new AssetNotAvailableException(
                    "Asset %s is currently %s and cannot be assigned".formatted(asset.getAssetCode(), asset.getStatus())
            );
        }
    }

    /**
     * Executes the validate date range operation.
     *
     * @param from the window start date
     * @param to the window end date
     */
    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to dates are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be on or before to");
        }
    }
}
