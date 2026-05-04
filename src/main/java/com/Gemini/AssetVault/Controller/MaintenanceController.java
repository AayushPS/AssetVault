package com.Gemini.AssetVault.Controller;

import com.Gemini.AssetVault.Dto.MaintenanceRecordRequest;
import com.Gemini.AssetVault.Dto.MaintenanceRecordResponse;
import com.Gemini.AssetVault.Model.Enum.MaintenanceStatus;
import com.Gemini.AssetVault.Model.Enum.MaintenanceType;
import com.Gemini.AssetVault.Service.MaintenanceService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/maintenance")
@Tag(name = "Maintenance", description = "Asset maintenance scheduling, status, and history APIs")
public class MaintenanceController {
    private final MaintenanceService maintenanceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create maintenance record", description = "Creates a maintenance record for an asset.")
    public MaintenanceRecordResponse create(@Valid @RequestBody MaintenanceRecordRequest request) {
        return maintenanceService.create(request);
    }

    @GetMapping
    @Operation(summary = "Get all maintenance records", description = "Returns paginated maintenance records.")
    public Page<MaintenanceRecordResponse> getAll(@ParameterObject @PageableDefault(sort = "id") Pageable pageable) {
        return maintenanceService.getAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get maintenance by ID", description = "Returns a maintenance record by id.")
    public MaintenanceRecordResponse getById(@PathVariable Integer id) {
        return maintenanceService.getById(id);
    }

    @GetMapping("/asset/{assetId}")
    @Operation(summary = "Get asset maintenance records", description = "Returns maintenance records for an asset.")
    public Page<MaintenanceRecordResponse> byAsset(
            @PathVariable Long assetId,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return maintenanceService.getByAsset(assetId, pageable);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get maintenance by status", description = "Filters maintenance records by status.")
    public Page<MaintenanceRecordResponse> byStatus(
            @PathVariable MaintenanceStatus status,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return maintenanceService.getByStatus(status, pageable);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get maintenance by type", description = "Filters maintenance records by type.")
    public Page<MaintenanceRecordResponse> byType(
            @PathVariable MaintenanceType type,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return maintenanceService.getByType(type, pageable);
    }

    @GetMapping("/scheduled")
    @Operation(summary = "Get upcoming scheduled maintenance", description = "Returns scheduled maintenance from today onward.")
    public Page<MaintenanceRecordResponse> scheduled(
            @ParameterObject @PageableDefault(sort = "scheduledDate") Pageable pageable
    ) {
        return maintenanceService.getScheduled(pageable);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get maintenance by date range", description = "Returns maintenance records within a scheduled date range.")
    public Page<MaintenanceRecordResponse> byDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @ParameterObject @PageableDefault(sort = "scheduledDate") Pageable pageable
    ) {
        return maintenanceService.getByDateRange(from, to, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update maintenance record", description = "Updates full maintenance record details.")
    public MaintenanceRecordResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody MaintenanceRecordRequest request
    ) {
        return maintenanceService.update(id, request);
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Complete maintenance", description = "Marks maintenance as completed.")
    public MaintenanceRecordResponse complete(@PathVariable Integer id) {
        return maintenanceService.complete(id);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel maintenance", description = "Cancels a scheduled maintenance record.")
    public MaintenanceRecordResponse cancel(@PathVariable Integer id) {
        return maintenanceService.cancel(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete maintenance record", description = "Deletes a maintenance record.")
    public void delete(@PathVariable Integer id) {
        maintenanceService.delete(id);
    }
}
