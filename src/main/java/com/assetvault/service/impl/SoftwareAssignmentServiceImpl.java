package com.assetvault.service.impl;

import com.assetvault.dto.EmployeeResponse;
import com.assetvault.dto.SoftwareLicenseResponse;
import com.assetvault.exception.AssignmentNotFoundException;
import com.assetvault.exception.EmployeeNotFoundException;
import com.assetvault.exception.InactiveEmployeeException;
import com.assetvault.exception.LicenseAlreadyAssignedException;
import com.assetvault.exception.LicenseExpiredException;
import com.assetvault.exception.LicenseNotFoundException;
import com.assetvault.exception.NoLicenseSeatsAvailableException;
import com.assetvault.model.Employee;
import com.assetvault.model.SoftwareAssignment;
import com.assetvault.model.SoftwareLicense;
import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.repository.EmployeeRepository;
import com.assetvault.repository.SoftwareAssignmentRepository;
import com.assetvault.repository.SoftwareLicenseRepository;
import com.assetvault.service.SoftwareAssignmentService;
import com.assetvault.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service implementation for software assignment operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SoftwareAssignmentServiceImpl implements SoftwareAssignmentService {
    private final SoftwareLicenseRepository softwareLicenseRepository;
    private final SoftwareAssignmentRepository softwareAssignmentRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Assigns the requested software assignment.
     *
     * @param licenseId the license identifier
     * @param employeeId the employee identifier
     * @return the resulting software assignment
     */
    @Override
    @Transactional
    public SoftwareLicenseResponse assign(Long licenseId, Long employeeId) {
        SoftwareLicense license = findLicenseForUpdate(licenseId);
        Employee employee = findEmployee(employeeId);
        if (!employee.isActive()) {
            throw new InactiveEmployeeException("Cannot assign license to a deactivated employee");
        }
        if (!Boolean.TRUE.equals(license.getIsActive())) {
            throw new NoLicenseSeatsAvailableException("License is inactive");
        }
        if (license.getExpiryDate() != null && license.getExpiryDate().isBefore(LocalDate.now())) {
            throw new LicenseExpiredException("Cannot assign an expired software license");
        }
        if (softwareAssignmentRepository.existsByEmployeeIdAndSoftwareLicenseIdAndStatus(
                employeeId,
                licenseId,
                AssignmentStatus.ACTIVE
        )) {
            throw new LicenseAlreadyAssignedException("Employee already holds this license");
        }
        int usedSeats = activeSeatCount(licenseId);
        if (usedSeats >= license.getTotalSeats()) {
            throw new NoLicenseSeatsAvailableException("All seats for this license are already used");
        }
        SoftwareAssignment assignment = SoftwareAssignment.builder()
                .softwareLicense(license)
                .employee(employee)
                .seatIndex(usedSeats + 1)
                .assignedDate(LocalDate.now())
                .status(AssignmentStatus.ACTIVE)
                .assignedBy("SYSTEM")
                .remarks("License seat assigned")
                .build();
        softwareAssignmentRepository.save(assignment);
        license.setUsedSeats(usedSeats + 1);
        SoftwareLicense saved = softwareLicenseRepository.save(license);
        log.info("Software license {} assigned to employee {}", license.getSoftwareName(), employee.getEmployeeCode());
        return toLicenseResponse(saved);
    }

    /**
     * Revokes the requested software assignment assignment.
     *
     * @param licenseId the license identifier
     * @param employeeId the employee identifier
     * @return the resulting software assignment
     */
    @Override
    @Transactional
    public SoftwareLicenseResponse revoke(Long licenseId, Long employeeId) {
        SoftwareLicense license = findLicenseForUpdate(licenseId);
        SoftwareAssignment assignment = softwareAssignmentRepository.findByEmployeeIdAndSoftwareLicenseIdAndStatus(
                employeeId,
                licenseId,
                AssignmentStatus.ACTIVE
        ).orElseThrow(() -> new AssignmentNotFoundException("Active software assignment does not exist"));
        releaseAssignment(assignment, "License seat revoked");
        softwareAssignmentRepository.save(assignment);
        license.setUsedSeats(activeSeatCount(licenseId));
        SoftwareLicense saved = softwareLicenseRepository.save(license);
        log.info("Software license {} revoked from employee {}", license.getSoftwareName(), assignment.getEmployee().getEmployeeCode());
        return toLicenseResponse(saved);
    }

    /**
     * Executes the revoke active assignments for license operation.
     *
     * @param licenseId the license identifier
     * @param remarks the reason captured for the state change
     * @return the computed numeric result
     */
    @Override
    @Transactional
    public int revokeActiveAssignmentsForLicense(Long licenseId, String remarks) {
        List<SoftwareAssignment> activeAssignments = softwareAssignmentRepository
                .findAllBySoftwareLicenseIdAndStatus(licenseId, AssignmentStatus.ACTIVE);
        activeAssignments.forEach(assignment -> releaseAssignment(assignment, remarks));
        if (!activeAssignments.isEmpty()) {
            softwareAssignmentRepository.saveAll(activeAssignments);
            log.info("{} active software assignments revoked for license {}", activeAssignments.size(), licenseId);
        }
        return activeAssignments.size();
    }

    /**
     * Executes the delete assignments for license operation.
     *
     * @param licenseId the license identifier
     * @return the computed numeric result
     */
    @Override
    @Transactional
    public long deleteAssignmentsForLicense(Long licenseId) {
        long deletedAssignments = softwareAssignmentRepository.deleteBySoftwareLicenseId(licenseId);
        if (deletedAssignments > 0) {
            log.info("{} software assignment records deleted for license {}", deletedAssignments, licenseId);
        }
        return deletedAssignments;
    }

    /**
     * Executes the find license for update operation.
     *
     * @param id the database identifier
     * @return the resulting software assignment
     */
    private SoftwareLicense findLicenseForUpdate(Long id) {
        return softwareLicenseRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new LicenseNotFoundException("License ID %d does not exist".formatted(id)));
    }

    /**
     * Executes the find employee operation.
     *
     * @param id the database identifier
     * @return the resulting software assignment
     */
    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee ID %d does not exist".formatted(id)));
    }

    /**
     * Executes the release assignment operation.
     *
     * @param assignment the assignment entity to map or update
     * @param remarks the reason captured for the state change
     */
    private void releaseAssignment(SoftwareAssignment assignment, String remarks) {
        assignment.setStatus(AssignmentStatus.RETURNED);
        assignment.setReturnedDate(LocalDate.now());
        assignment.setRemarks(appendRemark(assignment.getRemarks(), remarks));
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
     * Executes the active seat count operation.
     *
     * @param licenseId the license identifier
     * @return the computed numeric result
     */
    private int activeSeatCount(Long licenseId) {
        return Math.toIntExact(softwareAssignmentRepository.countBySoftwareLicenseIdAndStatus(
                licenseId,
                AssignmentStatus.ACTIVE
        ));
    }

    /**
     * Executes the append remark operation.
     *
     * @param current the current value
     * @param addition the addition value
     * @return the resulting software assignment
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
}
