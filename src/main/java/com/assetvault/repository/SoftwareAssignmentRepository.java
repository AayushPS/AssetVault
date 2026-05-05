package com.assetvault.repository;

import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.model.SoftwareAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SoftwareAssignmentRepository extends JpaRepository<SoftwareAssignment, Long> {
    @Override
    Page<SoftwareAssignment> findAll(Pageable pageable);

    Page<SoftwareAssignment> findByStatus(AssignmentStatus status, Pageable pageable);

    List<SoftwareAssignment> findAllByStatus(AssignmentStatus status);

    Page<SoftwareAssignment> findBySoftwareLicenseId(Long licenseId, Pageable pageable);

    List<SoftwareAssignment> findAllBySoftwareLicenseIdOrderByAssignedDateDescIdDesc(Long licenseId);

    List<SoftwareAssignment> findAllBySoftwareLicenseIdAndStatusOrderByAssignedDateDescIdDesc(
            Long licenseId,
            AssignmentStatus status
    );

    List<SoftwareAssignment> findAllBySoftwareLicenseIdAndStatus(
            Long licenseId,
            AssignmentStatus status
    );

    Page<SoftwareAssignment> findByEmployeeId(Long employeeId, Pageable pageable);

    List<SoftwareAssignment> findAllByEmployeeIdOrderByAssignedDateDescIdDesc(Long employeeId);

    Optional<SoftwareAssignment> findByEmployeeIdAndSoftwareLicenseIdAndStatus(
            Long employeeId,
            Long licenseId,
            AssignmentStatus status
    );

    boolean existsByEmployeeIdAndSoftwareLicenseIdAndStatus(
            Long employeeId,
            Long licenseId,
            AssignmentStatus status
    );

    long countBySoftwareLicenseIdAndStatus(Long licenseId, AssignmentStatus status);

    long deleteBySoftwareLicenseId(Long licenseId);
}
