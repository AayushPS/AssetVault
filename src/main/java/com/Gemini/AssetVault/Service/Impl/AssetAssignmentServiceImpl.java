package com.Gemini.AssetVault.Service.Impl;

import com.Gemini.AssetVault.Dto.AssetAssignmentRequest;
import com.Gemini.AssetVault.Dto.AssetAssignmentResponse;
import com.Gemini.AssetVault.Exception.AssetAlreadyAssignedException;
import com.Gemini.AssetVault.Exception.AssetNotAvailableException;
import com.Gemini.AssetVault.Exception.AssetNotFoundException;
import com.Gemini.AssetVault.Exception.AssetRetiredException;
import com.Gemini.AssetVault.Exception.AssignmentNotFoundException;
import com.Gemini.AssetVault.Exception.EmployeeNotFoundException;
import com.Gemini.AssetVault.Exception.InactiveEmployeeException;
import com.Gemini.AssetVault.Model.Asset;
import com.Gemini.AssetVault.Model.AssetAssignment;
import com.Gemini.AssetVault.Model.Employee;
import com.Gemini.AssetVault.Model.Enum.AssetStatus;
import com.Gemini.AssetVault.Model.Enum.AssignmentStatus;
import com.Gemini.AssetVault.Repository.AssetAssignmentRepository;
import com.Gemini.AssetVault.Repository.AssetRepository;
import com.Gemini.AssetVault.Repository.EmployeeRepository;
import com.Gemini.AssetVault.Service.AssetAssignmentService;
import com.Gemini.AssetVault.Util.AssetVaultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetAssignmentServiceImpl implements AssetAssignmentService {
    private final AssetAssignmentRepository assignmentRepository;
    private final AssetRepository assetRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public AssetAssignmentResponse assign(AssetAssignmentRequest request) {
        Asset asset = findAsset(request.assetId());
        Employee employee = findEmployee(request.employeeId());
        ensureEmployeeActive(employee);
        ensureAssetAssignable(asset);
        if (assignmentRepository.existsByAssetIdAndStatus(asset.getId(), AssignmentStatus.ACTIVE)) {
            throw new AssetAlreadyAssignedException(
                    "Asset %s is already actively assigned".formatted(asset.getAssetCode())
            );
        }
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
        return AssetVaultMapper.toAssetAssignmentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetAssignmentResponse> getAll(Pageable pageable) {
        return assignmentRepository.findAll(pageable).map(AssetVaultMapper::toAssetAssignmentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AssetAssignmentResponse getById(Long id) {
        return AssetVaultMapper.toAssetAssignmentResponse(findAssignment(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetAssignmentResponse> getActive(Pageable pageable) {
        return assignmentRepository.findByStatus(AssignmentStatus.ACTIVE, pageable)
                .map(AssetVaultMapper::toAssetAssignmentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetAssignmentResponse> getByAsset(Long assetId, Pageable pageable) {
        findAsset(assetId);
        return assignmentRepository.findByAssetId(assetId, pageable).map(AssetVaultMapper::toAssetAssignmentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetAssignmentResponse> getByEmployee(Long employeeId, Pageable pageable) {
        findEmployee(employeeId);
        return assignmentRepository.findByEmployeeId(employeeId, pageable)
                .map(AssetVaultMapper::toAssetAssignmentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetAssignmentResponse> getByDateRange(LocalDate from, LocalDate to, Pageable pageable) {
        validateDateRange(from, to);
        return assignmentRepository.findByAssignedDateBetween(from, to, pageable)
                .map(AssetVaultMapper::toAssetAssignmentResponse);
    }

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
        return AssetVaultMapper.toAssetAssignmentResponse(saved);
    }

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
        return AssetVaultMapper.toAssetAssignmentResponse(saved);
    }

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

    private AssetAssignment findAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment ID %d does not exist".formatted(id)));
    }

    private Asset findAsset(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new AssetNotFoundException("Asset ID %d does not exist".formatted(id)));
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee ID %d does not exist".formatted(id)));
    }

    private void ensureEmployeeActive(Employee employee) {
        if (!employee.isActive()) {
            throw new InactiveEmployeeException("Cannot assign to a deactivated employee");
        }
    }

    private void ensureAssetAssignable(Asset asset) {
        if (asset.getStatus() == AssetStatus.RETIRED) {
            throw new AssetRetiredException("Operation is not allowed on retired asset %s".formatted(asset.getAssetCode()));
        }
        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new AssetNotAvailableException(
                    "Asset %s is currently %s and cannot be assigned".formatted(asset.getAssetCode(), asset.getStatus())
            );
        }
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to dates are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be on or before to");
        }
    }
}
