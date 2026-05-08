package com.assetvault.controller;

import com.assetvault.dto.MaintenanceRecordRequest;
import com.assetvault.dto.MaintenanceRecordResponse;
import com.assetvault.model.enums.MaintenanceStatus;
import com.assetvault.model.enums.MaintenanceType;
import com.assetvault.service.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * REST controller for maintenance operations.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/maintenance")
@Tag(name = "Maintenance", description = "Asset maintenance scheduling, status, and history APIs")
public class MaintenanceController {
    private final MaintenanceService maintenanceService;

    /**
     * Creates a new maintenance.
     *
     * @param request the request payload
     * @return the resulting maintenance
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create maintenance record", description = "Creates a maintenance record for an asset.")
    public MaintenanceRecordResponse create(@Valid @RequestBody MaintenanceRecordRequest request) {
        return maintenanceService.create(request);
    }

    /**
     * Returns the requested page of maintenance records.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @GetMapping
    @Operation(summary = "Get all maintenance records", description = "Returns paginated maintenance records.")
    public Page<MaintenanceRecordResponse> getAll(@ParameterObject @PageableDefault(sort = "id") Pageable pageable) {
        return maintenanceService.getAll(pageable);
    }

    /**
     * Returns the maintenance identified by the given id.
     *
     * @param id the database identifier
     * @return the resulting maintenance
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get maintenance by ID", description = "Returns a maintenance record by id.")
    public MaintenanceRecordResponse getById(@PathVariable @Positive Integer id) {
        return maintenanceService.getById(id);
    }

    /**
     * Executes the by asset operation.
     *
     * @param assetId the asset identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @GetMapping("/asset/{assetId}")
    @Operation(summary = "Get asset maintenance records", description = "Returns maintenance records for an asset.")
    public Page<MaintenanceRecordResponse> byAsset(
            @PathVariable @Positive Long assetId,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return maintenanceService.getByAsset(assetId, pageable);
    }

    /**
     * Executes the by status operation.
     *
     * @param status the requested status value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get maintenance by status", description = "Filters maintenance records by status.")
    public Page<MaintenanceRecordResponse> byStatus(
            @PathVariable MaintenanceStatus status,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return maintenanceService.getByStatus(status, pageable);
    }

    /**
     * Executes the by type operation.
     *
     * @param type the requested type value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Get maintenance by type", description = "Filters maintenance records by type.")
    public Page<MaintenanceRecordResponse> byType(
            @PathVariable MaintenanceType type,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return maintenanceService.getByType(type, pageable);
    }

    /**
     * Executes the scheduled operation.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @GetMapping("/scheduled")
    @Operation(summary = "Get upcoming scheduled maintenance", description = "Returns scheduled maintenance from today onward.")
    public Page<MaintenanceRecordResponse> scheduled(
            @ParameterObject @PageableDefault(sort = "scheduledDate") Pageable pageable
    ) {
        return maintenanceService.getScheduled(pageable);
    }

    /**
     * Executes the by date range operation.
     *
     * @param from the window start date
     * @param to the window end date
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @GetMapping("/date-range")
    @Operation(summary = "Get maintenance by date range", description = "Returns maintenance records within a scheduled date range.")
    public Page<MaintenanceRecordResponse> byDateRange(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @ParameterObject @PageableDefault(sort = "scheduledDate") Pageable pageable
    ) {
        return maintenanceService.getByDateRange(from, to, pageable);
    }

    /**
     * Updates an existing maintenance.
     *
     * @param id the database identifier
     * @param request the request payload
     * @return the resulting maintenance
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update maintenance record", description = "Updates full maintenance record details.")
    public MaintenanceRecordResponse update(
            @PathVariable @Positive Integer id,
            @Valid @RequestBody MaintenanceRecordRequest request
    ) {
        return maintenanceService.update(id, request);
    }

    /**
     * Moves the maintenance into progress.
     *
     * @param id the database identifier
     * @return the resulting maintenance
     */
    @PatchMapping("/{id}/start")
    @Operation(summary = "Start maintenance", description = "Moves scheduled maintenance to in-progress.")
    public MaintenanceRecordResponse start(@PathVariable @Positive Integer id) {
        return maintenanceService.start(id);
    }

    /**
     * Marks the maintenance as completed.
     *
     * @param id the database identifier
     * @return the resulting maintenance
     */
    @PatchMapping("/{id}/complete")
    @Operation(summary = "Complete maintenance", description = "Marks maintenance as completed.")
    public MaintenanceRecordResponse complete(@PathVariable @Positive Integer id) {
        return maintenanceService.complete(id);
    }

    /**
     * Cancels the maintenance.
     *
     * @param id the database identifier
     * @return the resulting maintenance
     */
    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel maintenance", description = "Cancels a scheduled maintenance record.")
    public MaintenanceRecordResponse cancel(@PathVariable @Positive Integer id) {
        return maintenanceService.cancel(id);
    }

    /**
     * Deletes the maintenance.
     *
     * @param id the database identifier
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete maintenance record", description = "Deletes a maintenance record.")
    public void delete(@PathVariable @Positive Integer id) {
        maintenanceService.delete(id);
    }
}
