package com.assetvault.service;

import com.assetvault.dto.AssetResponse;
import com.assetvault.dto.AssetValueResponse;
import com.assetvault.dto.DashboardSummaryResponse;
import com.assetvault.dto.DepartmentAssetSummaryResponse;
import com.assetvault.dto.LicenseAlertsResponse;
import com.assetvault.dto.MaintenanceSummaryResponse;
import com.assetvault.dto.TypeBreakdownResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DashboardService {
    DashboardSummaryResponse getSummary();

    Page<TypeBreakdownResponse> getTypeBreakdown(Pageable pageable);

    Page<DepartmentAssetSummaryResponse> getDepartmentAssets(Pageable pageable);

    Page<AssetResponse> getWarrantyAlerts(Pageable pageable);

    LicenseAlertsResponse getLicenseAlerts();

    MaintenanceSummaryResponse getMaintenanceSummary();

    AssetValueResponse getAssetValue();
}
