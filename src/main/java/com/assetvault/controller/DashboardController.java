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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Dashboard summary, alert, and analytics APIs")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary", description = "Returns asset lifecycle summary counts.")
    public DashboardSummaryResponse summary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/type-breakdown")
    @Operation(summary = "Get asset type breakdown", description = "Returns asset count grouped by type.")
    public Page<TypeBreakdownResponse> typeBreakdown(
            @ParameterObject @PageableDefault Pageable pageable
    ) {
        return dashboardService.getTypeBreakdown(pageable);
    }

    @GetMapping("/department-assets")
    @Operation(summary = "Get department asset summary", description = "Returns asset count and value grouped by department.")
    public Page<DepartmentAssetSummaryResponse> departmentAssets(
            @ParameterObject @PageableDefault Pageable pageable
    ) {
        return dashboardService.getDepartmentAssets(pageable);
    }

    @GetMapping("/warranty-alerts")
    @Operation(summary = "Get warranty alerts", description = "Returns assets with warranties expiring in the next 30 days.")
    public Page<AssetResponse> warrantyAlerts(
            @ParameterObject @PageableDefault(sort = "warrantyExpiryDate") Pageable pageable
    ) {
        return dashboardService.getWarrantyAlerts(pageable);
    }

    @GetMapping("/license-alerts")
    @Operation(summary = "Get license alerts", description = "Returns expiring licenses and seat-exhausted licenses.")
    public LicenseAlertsResponse licenseAlerts() {
        return dashboardService.getLicenseAlerts();
    }

    @GetMapping("/maintenance-summary")
    @Operation(summary = "Get maintenance summary", description = "Returns scheduled, in-progress, and completed maintenance counts.")
    public MaintenanceSummaryResponse maintenanceSummary() {
        return dashboardService.getMaintenanceSummary();
    }

    @GetMapping("/asset-value")
    @Operation(summary = "Get active asset value", description = "Returns total purchase cost of all active assets.")
    public AssetValueResponse assetValue() {
        return dashboardService.getAssetValue();
    }
}
