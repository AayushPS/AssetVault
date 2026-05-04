package com.Gemini.AssetVault.Service.Impl;

import com.Gemini.AssetVault.Dto.EmployeeResponse;
import com.Gemini.AssetVault.Dto.SoftwareLicenseRequest;
import com.Gemini.AssetVault.Dto.SoftwareLicenseResponse;
import com.Gemini.AssetVault.Exception.AssignmentNotFoundException;
import com.Gemini.AssetVault.Exception.DuplicateLicenseException;
import com.Gemini.AssetVault.Exception.EmployeeNotFoundException;
import com.Gemini.AssetVault.Exception.InactiveEmployeeException;
import com.Gemini.AssetVault.Exception.LicenseAlreadyAssignedException;
import com.Gemini.AssetVault.Exception.LicenseExpiredException;
import com.Gemini.AssetVault.Exception.LicenseNotFoundException;
import com.Gemini.AssetVault.Exception.NoLicenseSeatsAvailableException;
import com.Gemini.AssetVault.Model.Employee;
import com.Gemini.AssetVault.Model.Enum.AssignmentStatus;
import com.Gemini.AssetVault.Model.SoftwareAssignment;
import com.Gemini.AssetVault.Model.SoftwareLicense;
import com.Gemini.AssetVault.Repository.EmployeeRepository;
import com.Gemini.AssetVault.Repository.SoftwareAssignmentRepository;
import com.Gemini.AssetVault.Repository.SoftwareLicenseRepository;
import com.Gemini.AssetVault.Service.SoftwareLicenseService;
import com.Gemini.AssetVault.Util.AssetVaultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SoftwareLicenseServiceImpl implements SoftwareLicenseService {
    private final SoftwareLicenseRepository softwareLicenseRepository;
    private final SoftwareAssignmentRepository softwareAssignmentRepository;
    private final EmployeeRepository employeeRepository;

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
        return toLicenseResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SoftwareLicenseResponse> getAll(Pageable pageable) {
        return softwareLicenseRepository.findAll(pageable).map(this::toLicenseResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SoftwareLicenseResponse getById(Long id) {
        return toLicenseResponse(findLicense(id));
    }

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

    @Override
    @Transactional(readOnly = true)
    public Page<SoftwareLicenseResponse> getExpired(Pageable pageable) {
        return softwareLicenseRepository.findExpiredLicenses(LocalDate.now(), pageable).map(this::toLicenseResponse);
    }

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

    @Override
    @Transactional(readOnly = true)
    public Page<SoftwareLicenseResponse> search(String name, Pageable pageable) {
        return softwareLicenseRepository.searchBySoftwareName(name, pageable).map(this::toLicenseResponse);
    }

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
        return toLicenseResponse(softwareLicenseRepository.save(license));
    }

    @Override
    @Transactional
    public SoftwareLicenseResponse assign(Long id, Long employeeId) {
        SoftwareLicense license = findLicense(id);
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
                id,
                AssignmentStatus.ACTIVE
        )) {
            throw new LicenseAlreadyAssignedException("Employee already holds this license");
        }
        int usedSeats = activeSeatCount(id);
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

    @Override
    @Transactional
    public SoftwareLicenseResponse revoke(Long id, Long employeeId) {
        SoftwareLicense license = findLicense(id);
        SoftwareAssignment assignment = softwareAssignmentRepository.findByEmployeeIdAndSoftwareLicenseIdAndStatus(
                employeeId,
                id,
                AssignmentStatus.ACTIVE
        ).orElseThrow(() -> new AssignmentNotFoundException("Active software assignment does not exist"));
        assignment.setStatus(AssignmentStatus.RETURNED);
        assignment.setReturnedDate(LocalDate.now());
        softwareAssignmentRepository.save(assignment);
        license.setUsedSeats(Math.max(activeSeatCount(id), 0));
        SoftwareLicense saved = softwareLicenseRepository.save(license);
        return toLicenseResponse(saved);
    }

    @Override
    @Transactional
    public SoftwareLicenseResponse deactivate(Long id) {
        SoftwareLicense license = findLicense(id);
        license.setIsActive(false);
        return toLicenseResponse(softwareLicenseRepository.save(license));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        softwareLicenseRepository.delete(findLicense(id));
    }

    private SoftwareLicense findLicense(Long id) {
        return softwareLicenseRepository.findById(id)
                .orElseThrow(() -> new LicenseNotFoundException("License ID %d does not exist".formatted(id)));
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee ID %d does not exist".formatted(id)));
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

    private int activeSeatCount(Long licenseId) {
        return Math.toIntExact(softwareAssignmentRepository.countBySoftwareLicenseIdAndStatus(
                licenseId,
                AssignmentStatus.ACTIVE
        ));
    }
}
