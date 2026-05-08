package com.assetvault.controller;

import com.assetvault.dto.AssetAssignmentRequest;
import com.assetvault.dto.AssetAssignmentResponse;
import com.assetvault.service.AssetAssignmentService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

/**
 * REST controller for assignment operations.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assignments")
@Tag(name = "Assignments", description = "Asset assignment, return, transfer, and history APIs")
public class AssignmentController {
    private final AssetAssignmentService assignmentService;

    /**
     * Assigns the requested assignment.
     *
     * @param request the request payload
     * @return the resulting assignment
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Assign asset", description = "Assigns an available asset to an active employee.")
    public AssetAssignmentResponse assign(@Valid @RequestBody AssetAssignmentRequest request) {
        return assignmentService.assign(request);
    }

    /**
     * Returns the requested page of assignments.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @GetMapping
    @Operation(summary = "Get all assignments", description = "Returns paginated asset assignments.")
    public Page<AssetAssignmentResponse> getAll(@ParameterObject @PageableDefault(sort = "id") Pageable pageable) {
        return assignmentService.getAll(pageable);
    }

    /**
     * Returns the assignment identified by the given id.
     *
     * @param id the database identifier
     * @return the resulting assignment
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get assignment by ID", description = "Returns an asset assignment by id.")
    public AssetAssignmentResponse getById(@PathVariable @Positive Long id) {
        return assignmentService.getById(id);
    }

    /**
     * Executes the active operation.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @GetMapping("/active")
    @Operation(summary = "Get active assignments", description = "Returns all active asset assignments.")
    public Page<AssetAssignmentResponse> active(@ParameterObject @PageableDefault(sort = "id") Pageable pageable) {
        return assignmentService.getActive(pageable);
    }

    /**
     * Executes the by asset operation.
     *
     * @param assetId the asset identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @GetMapping("/asset/{assetId}")
    @Operation(summary = "Get asset assignment history", description = "Returns full assignment history for an asset.")
    public Page<AssetAssignmentResponse> byAsset(
            @PathVariable @Positive Long assetId,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return assignmentService.getByAsset(assetId, pageable);
    }

    /**
     * Executes the by employee operation.
     *
     * @param employeeId the employee identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get employee assignment history", description = "Returns full assignment history for an employee.")
    public Page<AssetAssignmentResponse> byEmployee(
            @PathVariable @Positive Long employeeId,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return assignmentService.getByEmployee(employeeId, pageable);
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
    @Operation(summary = "Get assignments by date range", description = "Returns assignments whose assignment date is within a range.")
    public Page<AssetAssignmentResponse> byDateRange(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @ParameterObject @PageableDefault(sort = "assignedDate") Pageable pageable
    ) {
        return assignmentService.getByDateRange(from, to, pageable);
    }

    /**
     * Returns the assigned asset to the available pool.
     *
     * @param id the database identifier
     * @return the resulting assignment
     */
    @PatchMapping("/{id}/return")
    @Operation(summary = "Return asset", description = "Marks an active assignment as returned and frees the asset.")
    public AssetAssignmentResponse returnAsset(@PathVariable @Positive Long id) {
        return assignmentService.returnAsset(id);
    }

    /**
     * Transfers the current assignment to a different employee.
     *
     * @param id the database identifier
     * @param toEmployeeId the target employee identifier
     * @return the resulting assignment
     */
    @PatchMapping("/{id}/transfer")
    @Operation(summary = "Transfer asset", description = "Transfers an active assignment to another active employee.")
    public AssetAssignmentResponse transfer(
            @PathVariable @Positive Long id,
            @RequestParam @Positive Long toEmployeeId
    ) {
        return assignmentService.transfer(id, toEmployeeId);
    }

    /**
     * Deletes the assignment.
     *
     * @param id the database identifier
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete assignment", description = "Deletes an assignment record.")
    public void delete(@PathVariable @Positive Long id) {
        assignmentService.delete(id);
    }
}
