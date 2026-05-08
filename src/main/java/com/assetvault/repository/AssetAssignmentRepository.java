package com.assetvault.repository;

import com.assetvault.model.AssetAssignment;
import com.assetvault.model.enums.AssignmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for asset assignment persistence and reporting operations.
 */
@Repository
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {
    /**
     * Returns the requested page of asset assignments.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    Page<AssetAssignment> findAll(Pageable pageable);

    /**
     * Returns asset assignments filtered by status.
     *
     * @param status the requested status value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetAssignment> findByStatus(AssignmentStatus status, Pageable pageable);

    /**
     * Returns asset assignments filtered by status.
     *
     * @param status the requested status value
     * @return the matching results
     */
    List<AssetAssignment> findAllByStatus(AssignmentStatus status);

    /**
     * Executes the find by asset id operation.
     *
     * @param assetId the asset identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetAssignment> findByAssetId(Long assetId, Pageable pageable);

    /**
     * Executes the find all by asset id order by assigned date desc id desc operation.
     *
     * @param assetId the asset identifier
     * @return the matching results
     */
    List<AssetAssignment> findAllByAssetIdOrderByAssignedDateDescIdDesc(Long assetId);

    /**
     * Executes the find by employee id operation.
     *
     * @param employeeId the employee identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetAssignment> findByEmployeeId(Long employeeId, Pageable pageable);

    /**
     * Executes the find all by employee id order by assigned date desc id desc operation.
     *
     * @param employeeId the employee identifier
     * @return the matching results
     */
    List<AssetAssignment> findAllByEmployeeIdOrderByAssignedDateDescIdDesc(Long employeeId);

    /**
     * Executes the find by assigned date between operation.
     *
     * @param from the window start date
     * @param to the window end date
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetAssignment> findByAssignedDateBetween(LocalDate from, LocalDate to, Pageable pageable);

    /**
     * Executes the find all by assigned date between order by assigned date desc id desc operation.
     *
     * @param from the window start date
     * @param to the window end date
     * @return the matching results
     */
    List<AssetAssignment> findAllByAssignedDateBetweenOrderByAssignedDateDescIdDesc(LocalDate from, LocalDate to);

    /**
     * Checks whether a matching asset assignment exists.
     *
     * @param assetId the asset identifier
     * @param status the requested status value
     * @return true when a matching record exists; otherwise false
     */
    boolean existsByAssetIdAndStatus(Long assetId, AssignmentStatus status);

    /**
     * Checks whether a matching asset assignment exists.
     *
     * @param employeeId the employee identifier
     * @param status the requested status value
     * @return true when a matching record exists; otherwise false
     */
    boolean existsByEmployeeIdAndStatus(Long employeeId, AssignmentStatus status);

    /**
     * Executes the find by asset id and status operation.
     *
     * @param assetId the asset identifier
     * @param status the requested status value
     * @return the matching value when one exists
     */
    Optional<AssetAssignment> findByAssetIdAndStatus(Long assetId, AssignmentStatus status);

    /**
     * Executes the find by employee id and asset id and status operation.
     *
     * @param employeeId the employee identifier
     * @param assetId the asset identifier
     * @param status the requested status value
     * @return the matching value when one exists
     */
    Optional<AssetAssignment> findByEmployeeIdAndAssetIdAndStatus(
            Long employeeId,
            Long assetId,
            AssignmentStatus status
    );

    /**
     * Counts asset assignments that match the supplied filter.
     *
     * @param status the requested status value
     * @return the computed count
     */
    long countByStatus(AssignmentStatus status);
}
