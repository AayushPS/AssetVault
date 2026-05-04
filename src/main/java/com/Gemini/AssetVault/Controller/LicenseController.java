package com.Gemini.AssetVault.Controller;

import com.Gemini.AssetVault.Dto.SoftwareLicenseRequest;
import com.Gemini.AssetVault.Dto.SoftwareLicenseResponse;
import com.Gemini.AssetVault.Service.SoftwareLicenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/licenses")
@Tag(name = "Software Licenses", description = "Software license inventory, seats, expiry, and assignment APIs")
public class LicenseController {
    private final SoftwareLicenseService softwareLicenseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add software license", description = "Creates a new software license record.")
    public SoftwareLicenseResponse create(@Valid @RequestBody SoftwareLicenseRequest request) {
        return softwareLicenseService.create(request);
    }

    @GetMapping
    @Operation(summary = "Get all licenses", description = "Returns paginated software licenses.")
    public Page<SoftwareLicenseResponse> getAll(@ParameterObject @PageableDefault(sort = "id") Pageable pageable) {
        return softwareLicenseService.getAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get license by ID", description = "Returns a software license by id.")
    public SoftwareLicenseResponse getById(@PathVariable Long id) {
        return softwareLicenseService.getById(id);
    }

    @GetMapping("/expiring-soon")
    @Operation(summary = "Get licenses expiring soon", description = "Returns licenses expiring within N days.")
    public Page<SoftwareLicenseResponse> expiringSoon(
            @RequestParam(defaultValue = "30") @Min(1) int days,
            @ParameterObject @PageableDefault(sort = "expiryDate") Pageable pageable
    ) {
        return softwareLicenseService.getExpiringSoon(days, pageable);
    }

    @GetMapping("/expired")
    @Operation(summary = "Get expired licenses", description = "Returns licenses whose expiry date is in the past.")
    public Page<SoftwareLicenseResponse> expired(
            @ParameterObject @PageableDefault(sort = "expiryDate") Pageable pageable
    ) {
        return softwareLicenseService.getExpired(pageable);
    }

    @GetMapping("/low-seats")
    @Operation(summary = "Get licenses with no remaining seats", description = "Returns licenses where used seats meet or exceed total seats.")
    public Page<SoftwareLicenseResponse> lowSeats(@ParameterObject @PageableDefault(sort = "id") Pageable pageable) {
        return softwareLicenseService.getLowSeats(pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Search licenses", description = "Searches licenses by software name.")
    public Page<SoftwareLicenseResponse> search(
            @RequestParam @NotBlank String name,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return softwareLicenseService.search(name, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update license", description = "Updates full software license details.")
    public SoftwareLicenseResponse update(@PathVariable Long id, @Valid @RequestBody SoftwareLicenseRequest request) {
        return softwareLicenseService.update(id, request);
    }

    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assign license seat", description = "Assigns a license seat to an active employee.")
    public SoftwareLicenseResponse assign(@PathVariable Long id, @RequestParam Long employeeId) {
        return softwareLicenseService.assign(id, employeeId);
    }

    @PatchMapping("/{id}/revoke")
    @Operation(summary = "Revoke license seat", description = "Revokes a license seat from an employee.")
    public SoftwareLicenseResponse revoke(@PathVariable Long id, @RequestParam Long employeeId) {
        return softwareLicenseService.revoke(id, employeeId);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate license", description = "Marks a software license as inactive.")
    public SoftwareLicenseResponse deactivate(@PathVariable Long id) {
        return softwareLicenseService.deactivate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete license", description = "Deletes a software license record.")
    public void delete(@PathVariable Long id) {
        softwareLicenseService.delete(id);
    }
}
