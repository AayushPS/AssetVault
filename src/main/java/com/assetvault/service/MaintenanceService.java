package com.assetvault.service;

import com.assetvault.dto.MaintenanceRecordRequest;
import com.assetvault.dto.MaintenanceRecordResponse;
import com.assetvault.model.enums.MaintenanceStatus;
import com.assetvault.model.enums.MaintenanceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Service contract for maintenance operations.
 */
public interface MaintenanceService {
    /**
     * Creates a new maintenance.
     *
     * @param request the request payload
     * @return the resulting maintenance
     */
    MaintenanceRecordResponse create(MaintenanceRecordRequest request);

    /**
     * Returns the requested page of maintenance records.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<MaintenanceRecordResponse> getAll(Pageable pageable);

    /**
     * Returns the maintenance identified by the given id.
     *
     * @param id the database identifier
     * @return the resulting maintenance
     */
    MaintenanceRecordResponse getById(Integer id);

    /**
     * Executes the get by asset operation.
     *
     * @param assetId the asset identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<MaintenanceRecordResponse> getByAsset(Long assetId, Pageable pageable);

    /**
     * Returns maintenance records filtered by status.
     *
     * @param status the requested status value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<MaintenanceRecordResponse> getByStatus(MaintenanceStatus status, Pageable pageable);

    /**
     * Returns maintenance records filtered by type.
     *
     * @param type the requested type value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<MaintenanceRecordResponse> getByType(MaintenanceType type, Pageable pageable);

    /**
     * Executes the get scheduled operation.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<MaintenanceRecordResponse> getScheduled(Pageable pageable);

    /**
     * Executes the get by date range operation.
     *
     * @param from the window start date
     * @param to the window end date
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<MaintenanceRecordResponse> getByDateRange(LocalDate from, LocalDate to, Pageable pageable);

    /**
     * Updates an existing maintenance.
     *
     * @param id the database identifier
     * @param request the request payload
     * @return the resulting maintenance
     */
    MaintenanceRecordResponse update(Integer id, MaintenanceRecordRequest request);

    /**
     * Moves the maintenance into progress.
     *
     * @param id the database identifier
     * @return the resulting maintenance
     */
    MaintenanceRecordResponse start(Integer id);

    /**
     * Marks the maintenance as completed.
     *
     * @param id the database identifier
     * @return the resulting maintenance
     */
    MaintenanceRecordResponse complete(Integer id);

    /**
     * Cancels the maintenance.
     *
     * @param id the database identifier
     * @return the resulting maintenance
     */
    MaintenanceRecordResponse cancel(Integer id);

    /**
     * Deletes the maintenance.
     *
     * @param id the database identifier
     */
    void delete(Integer id);
}
