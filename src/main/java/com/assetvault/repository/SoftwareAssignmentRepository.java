package com.assetvault.repository;

import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.model.SoftwareAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for software assignment persistence and reporting operations.
 */
@Repository
public interface SoftwareAssignmentRepository extends JpaRepository<SoftwareAssignment, Long> {
    /**
     * Returns the requested page of software assignments.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    Page<SoftwareAssignment> findAll(Pageable pageable);

    /**
     * Returns software assignments filtered by status.
     *
     * @param status the requested status value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<SoftwareAssignment> findByStatus(AssignmentStatus status, Pageable pageable);

    /**
     * Returns software assignments filtered by status.
     *
     * @param status the requested status value
     * @return the matching results
     */
    List<SoftwareAssignment> findAllByStatus(AssignmentStatus status);

    /**
     * Executes the find by software license id operation.
     *
     * @param licenseId the license identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<SoftwareAssignment> findBySoftwareLicenseId(Long licenseId, Pageable pageable);

    /**
     * Executes the find all by software license id order by assigned date desc id desc operation.
     *
     * @param licenseId the license identifier
     * @return the matching results
     */
    List<SoftwareAssignment> findAllBySoftwareLicenseIdOrderByAssignedDateDescIdDesc(Long licenseId);

    /**
     * Executes the find all by software license id and status order by assigned date desc id desc operation.
     *
     * @param licenseId the license identifier
     * @param status the requested status value
     * @return the matching results
     */
    List<SoftwareAssignment> findAllBySoftwareLicenseIdAndStatusOrderByAssignedDateDescIdDesc(
            Long licenseId,
            AssignmentStatus status
    );

    /**
     * Executes the find all by software license id and status operation.
     *
     * @param licenseId the license identifier
     * @param status the requested status value
     * @return the matching results
     */
    List<SoftwareAssignment> findAllBySoftwareLicenseIdAndStatus(
            Long licenseId,
            AssignmentStatus status
    );

    /**
     * Executes the find by employee id operation.
     *
     * @param employeeId the employee identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<SoftwareAssignment> findByEmployeeId(Long employeeId, Pageable pageable);

    /**
     * Executes the find all by employee id order by assigned date desc id desc operation.
     *
     * @param employeeId the employee identifier
     * @return the matching results
     */
    List<SoftwareAssignment> findAllByEmployeeIdOrderByAssignedDateDescIdDesc(Long employeeId);

    /**
     * Executes the find by employee id and software license id and status operation.
     *
     * @param employeeId the employee identifier
     * @param licenseId the license identifier
     * @param status the requested status value
     * @return the matching value when one exists
     */
    Optional<SoftwareAssignment> findByEmployeeIdAndSoftwareLicenseIdAndStatus(
            Long employeeId,
            Long licenseId,
            AssignmentStatus status
    );

    /**
     * Checks whether a matching software assignment exists.
     *
     * @param employeeId the employee identifier
     * @param licenseId the license identifier
     * @param status the requested status value
     * @return true when a matching record exists; otherwise false
     */
    boolean existsByEmployeeIdAndSoftwareLicenseIdAndStatus(
            Long employeeId,
            Long licenseId,
            AssignmentStatus status
    );

    /**
     * Counts software assignments that match the supplied filter.
     *
     * @param licenseId the license identifier
     * @param status the requested status value
     * @return the computed count
     */
    long countBySoftwareLicenseIdAndStatus(Long licenseId, AssignmentStatus status);

    /**
     * Executes the delete by software license id operation.
     *
     * @param licenseId the license identifier
     * @return the computed numeric result
     */
    long deleteBySoftwareLicenseId(Long licenseId);
}
