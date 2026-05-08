package com.assetvault.service;

import com.assetvault.dto.AssetAssignmentRequest;
import com.assetvault.dto.AssetAssignmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Service contract for asset assignment operations.
 */
public interface AssetAssignmentService {
    /**
     * Assigns the requested asset assignment.
     *
     * @param request the request payload
     * @return the resulting asset assignment
     */
    AssetAssignmentResponse assign(AssetAssignmentRequest request);

    /**
     * Returns the requested page of asset assignments.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetAssignmentResponse> getAll(Pageable pageable);

    /**
     * Returns the asset assignment identified by the given id.
     *
     * @param id the database identifier
     * @return the resulting asset assignment
     */
    AssetAssignmentResponse getById(Long id);

    /**
     * Executes the get active operation.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetAssignmentResponse> getActive(Pageable pageable);

    /**
     * Executes the get by asset operation.
     *
     * @param assetId the asset identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetAssignmentResponse> getByAsset(Long assetId, Pageable pageable);

    /**
     * Executes the get by employee operation.
     *
     * @param employeeId the employee identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetAssignmentResponse> getByEmployee(Long employeeId, Pageable pageable);

    /**
     * Executes the get by date range operation.
     *
     * @param from the window start date
     * @param to the window end date
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetAssignmentResponse> getByDateRange(LocalDate from, LocalDate to, Pageable pageable);

    /**
     * Returns the assigned asset to the available pool.
     *
     * @param id the database identifier
     * @return the resulting asset assignment
     */
    AssetAssignmentResponse returnAsset(Long id);

    /**
     * Transfers the current asset assignment to a different employee.
     *
     * @param id the database identifier
     * @param toEmployeeId the target employee identifier
     * @return the resulting asset assignment
     */
    AssetAssignmentResponse transfer(Long id, Long toEmployeeId);

    /**
     * Releases active asset assignments for the supplied asset.
     *
     * @param assetId the asset identifier
     * @param remarks the reason captured for the state change
     * @return the computed numeric result
     */
    int releaseActiveAssignmentsForAsset(Long assetId, String remarks);

    /**
     * Deletes the asset assignment.
     *
     * @param id the database identifier
     */
    void delete(Long id);
}
