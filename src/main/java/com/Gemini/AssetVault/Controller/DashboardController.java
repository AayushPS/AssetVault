package com.Gemini.AssetVault.Controller;

import com.Gemini.AssetVault.Dto.AssetResponse;
import com.Gemini.AssetVault.Dto.AssetValueResponse;
import com.Gemini.AssetVault.Dto.DashboardSummaryResponse;
import com.Gemini.AssetVault.Dto.DepartmentAssetSummaryResponse;
import com.Gemini.AssetVault.Dto.LicenseAlertsResponse;
import com.Gemini.AssetVault.Dto.MaintenanceSummaryResponse;
import com.Gemini.AssetVault.Dto.TypeBreakdownResponse;
import com.Gemini.AssetVault.Service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public List<TypeBreakdownResponse> typeBreakdown() {
        return dashboardService.getTypeBreakdown();
    }

    @GetMapping("/department-assets")
    @Operation(summary = "Get department asset summary", description = "Returns asset count and value grouped by department.")
    public List<DepartmentAssetSummaryResponse> departmentAssets() {
        return dashboardService.getDepartmentAssets();
    }

    @GetMapping("/warranty-alerts")
    @Operation(summary = "Get warranty alerts", description = "Returns assets with warranties expiring in the next 30 days.")
    public List<AssetResponse> warrantyAlerts() {
        return dashboardService.getWarrantyAlerts();
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
