package com.Gemini.AssetVault.Controller;

import com.Gemini.AssetVault.Dto.AssetRequest;
import com.Gemini.AssetVault.Dto.AssetResponse;
import com.Gemini.AssetVault.Model.Enum.AssetStatus;
import com.Gemini.AssetVault.Model.Enum.AssetType;
import com.Gemini.AssetVault.Service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assets")
@Tag(name = "Assets", description = "Asset registration, search, lifecycle, and warranty APIs")
public class AssetController {
    private final AssetService assetService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new asset", description = "Creates an asset and auto-generates a unique asset code.")
    public AssetResponse create(@Valid @RequestBody AssetRequest request) {
        return assetService.create(request);
    }

    @GetMapping
    @Operation(summary = "Get all assets", description = "Returns a paginated and sortable list of assets.")
    public Page<AssetResponse> getAll(@ParameterObject @PageableDefault(sort = "id") Pageable pageable) {
        return assetService.getAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get asset by ID", description = "Returns a single asset by database id.")
    public AssetResponse getById(@PathVariable Long id) {
        return assetService.getById(id);
    }

    @GetMapping("/code/{assetCode}")
    @Operation(summary = "Get asset by code", description = "Returns an asset by its generated unique asset code.")
    public AssetResponse getByCode(@PathVariable String assetCode) {
        return assetService.getByCode(assetCode);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get assets by type", description = "Filters assets by asset type.")
    public Page<AssetResponse> getByType(
            @PathVariable AssetType type,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return assetService.getByType(type, pageable);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get assets by status", description = "Filters assets by lifecycle status.")
    public Page<AssetResponse> getByStatus(
            @PathVariable AssetStatus status,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return assetService.getByStatus(status, pageable);
    }

    @GetMapping("/available")
    @Operation(summary = "Get available assets", description = "Returns assets currently available for assignment.")
    public Page<AssetResponse> getAvailable(@ParameterObject @PageableDefault(sort = "id") Pageable pageable) {
        return assetService.getAvailable(pageable);
    }

    @GetMapping("/department")
    @Operation(summary = "Get assets assigned to a department", description = "Returns active assigned assets in a department.")
    public Page<AssetResponse> getByDepartment(
            @RequestParam @NotBlank String name,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return assetService.getByDepartment(name, pageable);
    }

    @GetMapping("/warranty-expiring")
    @Operation(summary = "Get assets with warranty expiring", description = "Returns assets whose warranty expires within N days.")
    public Page<AssetResponse> getWarrantyExpiring(
            @RequestParam(defaultValue = "30") @Min(1) int days,
            @ParameterObject @PageableDefault(sort = "warrantyExpiryDate") Pageable pageable
    ) {
        return assetService.getWarrantyExpiring(days, pageable);
    }

    @GetMapping("/warranty-expired")
    @Operation(summary = "Get assets with expired warranty", description = "Returns assets whose warranty expiry date is in the past.")
    public Page<AssetResponse> getWarrantyExpired(
            @ParameterObject @PageableDefault(sort = "warrantyExpiryDate") Pageable pageable
    ) {
        return assetService.getWarrantyExpired(pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Search assets", description = "Searches assets by name, brand, or model.")
    public Page<AssetResponse> search(
            @RequestParam @NotBlank String keyword,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return assetService.search(keyword, pageable);
    }

    @GetMapping("/low-value")
    @Operation(summary = "Get low value assets", description = "Returns assets below a maximum purchase cost.")
    public Page<AssetResponse> getLowValue(
            @RequestParam @NotNull BigDecimal maxCost,
            @ParameterObject @PageableDefault(sort = "purchaseCost") Pageable pageable
    ) {
        return assetService.getLowValue(maxCost, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update asset", description = "Updates full asset details.")
    public AssetResponse update(@PathVariable Long id, @Valid @RequestBody AssetRequest request) {
        return assetService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update asset status", description = "Updates only the asset lifecycle status.")
    public AssetResponse updateStatus(@PathVariable Long id, @RequestParam AssetStatus status) {
        return assetService.updateStatus(id, status);
    }

    @PatchMapping("/{id}/retire")
    @Operation(summary = "Retire asset", description = "Marks an asset as retired.")
    public AssetResponse retire(@PathVariable Long id) {
        return assetService.retire(id);
    }

    @PatchMapping("/{id}/mark-lost")
    @Operation(summary = "Mark asset lost", description = "Marks an asset as lost.")
    public AssetResponse markLost(@PathVariable Long id) {
        return assetService.markLost(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete asset", description = "Deletes an asset record.")
    public void delete(@PathVariable Long id) {
        assetService.delete(id);
    }
}
