package com.assetvault.controller;

import com.assetvault.dto.AssetResponse;
import com.assetvault.dto.AssetValueResponse;
import com.assetvault.dto.DashboardSummaryResponse;
import com.assetvault.dto.DepartmentAssetSummaryResponse;
import com.assetvault.dto.LicenseAlertsResponse;
import com.assetvault.dto.MaintenanceSummaryResponse;
import com.assetvault.dto.TypeBreakdownResponse;
import com.assetvault.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for dashboard operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Dashboard summary, alert, and analytics APIs")
public class DashboardController {
    private final DashboardService dashboardService;

    /**
     * Returns the current summary view.
     *
     * @return the requested summary payload
     */
    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary", description = "Returns asset lifecycle summary counts.")
    public DashboardSummaryResponse summary() {
        return dashboardService.getSummary();
    }

    /**
     * Returns aggregated dashboards grouped by type.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @GetMapping("/type-breakdown")
    @Operation(summary = "Get asset type breakdown", description = "Returns asset count grouped by type.")
    public Page<TypeBreakdownResponse> typeBreakdown(
            @ParameterObject @PageableDefault Pageable pageable
    ) {
        return dashboardService.getTypeBreakdown(pageable);
    }

    /**
     * Returns aggregated asset metrics grouped by department.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @GetMapping("/department-assets")
    @Operation(summary = "Get department asset summary", description = "Returns asset count and value grouped by department.")
    public Page<DepartmentAssetSummaryResponse> departmentAssets(
            @ParameterObject @PageableDefault Pageable pageable
    ) {
        return dashboardService.getDepartmentAssets(pageable);
    }

    /**
     * Returns assets that require warranty attention.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @GetMapping("/warranty-alerts")
    @Operation(summary = "Get warranty alerts", description = "Returns assets with warranties expiring in the next 30 days.")
    public Page<AssetResponse> warrantyAlerts(
            @ParameterObject @PageableDefault(sort = "warrantyExpiryDate") Pageable pageable
    ) {
        return dashboardService.getWarrantyAlerts(pageable);
    }

    /**
     * Returns current license alert metrics.
     *
     * @return the requested summary payload
     */
    @GetMapping("/license-alerts")
    @Operation(summary = "Get license alerts", description = "Returns expiring licenses and seat-exhausted licenses.")
    public LicenseAlertsResponse licenseAlerts() {
        return dashboardService.getLicenseAlerts();
    }

    /**
     * Returns the current maintenance summary.
     *
     * @return the requested summary payload
     */
    @GetMapping("/maintenance-summary")
    @Operation(summary = "Get maintenance summary", description = "Returns scheduled, in-progress, and completed maintenance counts.")
    public MaintenanceSummaryResponse maintenanceSummary() {
        return dashboardService.getMaintenanceSummary();
    }

    /**
     * Returns the aggregated value of active assets.
     *
     * @return the requested summary payload
     */
    @GetMapping("/asset-value")
    @Operation(summary = "Get active asset value", description = "Returns total purchase cost of all active assets.")
    public AssetValueResponse assetValue() {
        return dashboardService.getAssetValue();
    }
}
