package com.assetvault.service.impl;

import com.assetvault.dto.EmployeeResponse;
import com.assetvault.dto.SoftwareLicenseRequest;
import com.assetvault.dto.SoftwareLicenseResponse;
import com.assetvault.exception.DuplicateLicenseException;
import com.assetvault.exception.LicenseNotFoundException;
import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.model.SoftwareAssignment;
import com.assetvault.model.SoftwareLicense;
import com.assetvault.repository.SoftwareAssignmentRepository;
import com.assetvault.repository.SoftwareLicenseRepository;
import com.assetvault.service.SoftwareAssignmentService;
import com.assetvault.service.SoftwareLicenseService;
import com.assetvault.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service implementation for software license operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SoftwareLicenseServiceImpl implements SoftwareLicenseService {
    private final SoftwareLicenseRepository softwareLicenseRepository;
    private final SoftwareAssignmentRepository softwareAssignmentRepository;
    private final SoftwareAssignmentService softwareAssignmentService;

    /**
     * Creates a new software license.
     *
     * @param request the request payload
     * @return the resulting software license
     */
    @Override
    @Transactional
    public SoftwareLicenseResponse create(SoftwareLicenseRequest request) {
        if (softwareLicenseRepository.existsByLicenseKey(request.licenseKey())) {
            throw new DuplicateLicenseException("License key already exists");
        }
        int usedSeats = request.usedSeats() == null ? 0 : request.usedSeats();
        if (usedSeats > request.totalSeats()) {
            throw new IllegalArgumentException("Used seats cannot exceed total seats");
        }
        SoftwareLicense license = SoftwareLicense.builder()
                .softwareName(request.softwareName())
                .licenceKey(request.licenseKey())
                .licenseType(request.licenseType())
                .vendor(request.vendor())
                .totalSeats(request.totalSeats())
                .usedSeats(usedSeats)
                .purchaseDate(request.purchaseDate())
                .purchaseCost(request.purchaseCost())
                .expiryDate(request.expiryDate())
                .isActive(request.active() == null || request.active())
                .build();
        SoftwareLicense saved = softwareLicenseRepository.save(license);
        log.info("Software license registered: {}", saved.getSoftwareName());
        return toLicenseResponse(saved);
    }

    /**
     * Returns the requested page of software licenses.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SoftwareLicenseResponse> getAll(Pageable pageable) {
        return softwareLicenseRepository.findAll(pageable).map(this::toLicenseResponse);
    }

    /**
     * Returns the software license identified by the given id.
     *
     * @param id the database identifier
     * @return the resulting software license
     */
    @Override
    @Transactional(readOnly = true)
    public SoftwareLicenseResponse getById(Long id) {
        return toLicenseResponse(findLicense(id));
    }

    /**
     * Executes the get expiring soon operation.
     *
     * @param days the number of days to look ahead
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SoftwareLicenseResponse> getExpiringSoon(int days, Pageable pageable) {
        LocalDate today = LocalDate.now();
        Page<SoftwareLicenseResponse> expiringLicenses = softwareLicenseRepository
                .findLicensesExpiringSoon(today, today.plusDays(days), pageable)
                .map(this::toLicenseResponse);
        if (expiringLicenses.hasContent()) {
            log.warn("{} software licenses expire within {} days", expiringLicenses.getTotalElements(), days);
        }
        return expiringLicenses;
    }

    /**
     * Executes the get expired operation.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SoftwareLicenseResponse> getExpired(Pageable pageable) {
        return softwareLicenseRepository.findExpiredLicenses(LocalDate.now(), pageable).map(this::toLicenseResponse);
    }

    /**
     * Executes the get low seats operation.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SoftwareLicenseResponse> getLowSeats(Pageable pageable) {
        Page<SoftwareLicenseResponse> lowSeatLicenses = softwareLicenseRepository
                .findLicensesWithNoRemainingSeats(pageable)
                .map(this::toLicenseResponse);
        if (lowSeatLicenses.hasContent()) {
            log.warn("{} software licenses have exhausted seats", lowSeatLicenses.getTotalElements());
        }
        return lowSeatLicenses;
    }

    /**
     * Searches software licenses using the supplied keyword.
     *
     * @param name the name or label value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SoftwareLicenseResponse> search(String name, Pageable pageable) {
        return softwareLicenseRepository.searchBySoftwareName(name, pageable).map(this::toLicenseResponse);
    }

    /**
     * Updates an existing software license.
     *
     * @param id the database identifier
     * @param request the request payload
     * @return the resulting software license
     */
    @Override
    @Transactional
    public SoftwareLicenseResponse update(Long id, SoftwareLicenseRequest request) {
        SoftwareLicense license = findLicense(id);
        if (softwareLicenseRepository.existsByLicenceKeyAndIdNot(request.licenseKey(), id)) {
            throw new DuplicateLicenseException("License key already exists");
        }
        int usedSeats = request.usedSeats() == null ? license.getUsedSeats() : request.usedSeats();
        if (usedSeats > request.totalSeats()) {
            throw new IllegalArgumentException("Used seats cannot exceed total seats");
        }
        license.setSoftwareName(request.softwareName());
        license.setLicenceKey(request.licenseKey());
        license.setLicenseType(request.licenseType());
        license.setVendor(request.vendor());
        license.setTotalSeats(request.totalSeats());
        license.setUsedSeats(usedSeats);
        license.setPurchaseDate(request.purchaseDate());
        license.setPurchaseCost(request.purchaseCost());
        license.setExpiryDate(request.expiryDate());
        license.setIsActive(request.active() == null || request.active());
        SoftwareLicense saved = softwareLicenseRepository.save(license);
        log.info("Software license updated: {}", saved.getSoftwareName());
        return toLicenseResponse(saved);
    }

    /**
     * Deactivates the software license.
     *
     * @param id the database identifier
     * @return the resulting software license
     */
    @Override
    @Transactional
    public SoftwareLicenseResponse deactivate(Long id) {
        SoftwareLicense license = findLicenseForUpdate(id);
        int revokedAssignments = softwareAssignmentService.revokeActiveAssignmentsForLicense(
                id,
                "License seat returned automatically before license deactivation"
        );
        license.setUsedSeats(0);
        license.setIsActive(false);
        SoftwareLicense saved = softwareLicenseRepository.save(license);
        log.info("Software license deactivated: {} (revokedAssignments={})", saved.getSoftwareName(), revokedAssignments);
        return toLicenseResponse(saved);
    }

    /**
     * Deletes the software license.
     *
     * @param id the database identifier
     */
    @Override
    @Transactional
    public void delete(Long id) {
        SoftwareLicense license = findLicenseForUpdate(id);
        int revokedAssignments = softwareAssignmentService.revokeActiveAssignmentsForLicense(
                id,
                "License seat returned automatically before license deletion"
        );
        long deletedAssignmentRecords = softwareAssignmentService.deleteAssignmentsForLicense(id);
        softwareLicenseRepository.delete(license);
        log.info(
                "Software license deleted: {} (revokedAssignments={}, deletedAssignmentRecords={})",
                license.getSoftwareName(),
                revokedAssignments,
                deletedAssignmentRecords
        );
    }

    /**
     * Executes the find license operation.
     *
     * @param id the database identifier
     * @return the resulting software license
     */
    private SoftwareLicense findLicense(Long id) {
        return softwareLicenseRepository.findById(id)
                .orElseThrow(() -> new LicenseNotFoundException("License ID %d does not exist".formatted(id)));
    }

    /**
     * Executes the find license for update operation.
     *
     * @param id the database identifier
     * @return the resulting software license
     */
    private SoftwareLicense findLicenseForUpdate(Long id) {
        return softwareLicenseRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new LicenseNotFoundException("License ID %d does not exist".formatted(id)));
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

}
