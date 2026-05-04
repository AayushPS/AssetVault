package com.Gemini.AssetVault.Service;

import com.Gemini.AssetVault.Dto.AssetResponse;
import com.Gemini.AssetVault.Dto.AssetValueResponse;
import com.Gemini.AssetVault.Dto.DashboardSummaryResponse;
import com.Gemini.AssetVault.Dto.DepartmentAssetSummaryResponse;
import com.Gemini.AssetVault.Dto.LicenseAlertsResponse;
import com.Gemini.AssetVault.Dto.MaintenanceSummaryResponse;
import com.Gemini.AssetVault.Dto.TypeBreakdownResponse;

import java.util.List;

public interface DashboardService {
    DashboardSummaryResponse getSummary();

    List<TypeBreakdownResponse> getTypeBreakdown();

    List<DepartmentAssetSummaryResponse> getDepartmentAssets();

    List<AssetResponse> getWarrantyAlerts();

    LicenseAlertsResponse getLicenseAlerts();

    MaintenanceSummaryResponse getMaintenanceSummary();

    AssetValueResponse getAssetValue();
}
