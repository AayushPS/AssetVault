package com.Gemini.AssetVault.Controller;

import com.Gemini.AssetVault.Dto.AssetAssignmentRequest;
import com.Gemini.AssetVault.Dto.AssetAssignmentResponse;
import com.Gemini.AssetVault.Service.AssetAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assignments")
@Tag(name = "Assignments", description = "Asset assignment, return, transfer, and history APIs")
public class AssignmentController {
    private final AssetAssignmentService assignmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Assign asset", description = "Assigns an available asset to an active employee.")
    public AssetAssignmentResponse assign(@Valid @RequestBody AssetAssignmentRequest request) {
        return assignmentService.assign(request);
    }

    @GetMapping
    @Operation(summary = "Get all assignments", description = "Returns paginated asset assignments.")
    public Page<AssetAssignmentResponse> getAll(@ParameterObject @PageableDefault(sort = "id") Pageable pageable) {
        return assignmentService.getAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get assignment by ID", description = "Returns an asset assignment by id.")
    public AssetAssignmentResponse getById(@PathVariable Long id) {
        return assignmentService.getById(id);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active assignments", description = "Returns all active asset assignments.")
    public Page<AssetAssignmentResponse> active(@ParameterObject @PageableDefault(sort = "id") Pageable pageable) {
        return assignmentService.getActive(pageable);
    }

    @GetMapping("/asset/{assetId}")
    @Operation(summary = "Get asset assignment history", description = "Returns full assignment history for an asset.")
    public Page<AssetAssignmentResponse> byAsset(
            @PathVariable Long assetId,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return assignmentService.getByAsset(assetId, pageable);
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get employee assignment history", description = "Returns full assignment history for an employee.")
    public Page<AssetAssignmentResponse> byEmployee(
            @PathVariable Long employeeId,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return assignmentService.getByEmployee(employeeId, pageable);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get assignments by date range", description = "Returns assignments whose assignment date is within a range.")
    public Page<AssetAssignmentResponse> byDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @ParameterObject @PageableDefault(sort = "assignedDate") Pageable pageable
    ) {
        return assignmentService.getByDateRange(from, to, pageable);
    }

    @PatchMapping("/{id}/return")
    @Operation(summary = "Return asset", description = "Marks an active assignment as returned and frees the asset.")
    public AssetAssignmentResponse returnAsset(@PathVariable Long id) {
        return assignmentService.returnAsset(id);
    }

    @PatchMapping("/{id}/transfer")
    @Operation(summary = "Transfer asset", description = "Transfers an active assignment to another active employee.")
    public AssetAssignmentResponse transfer(@PathVariable Long id, @RequestParam Long toEmployeeId) {
        return assignmentService.transfer(id, toEmployeeId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete assignment", description = "Deletes an assignment record.")
    public void delete(@PathVariable Long id) {
        assignmentService.delete(id);
    }
}
