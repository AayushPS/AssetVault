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

/**
 * Service contract for dashboard operations.
 */
public interface DashboardService {
    /**
     * Executes the get summary operation.
     *
     * @return the resulting dashboard
     */
    DashboardSummaryResponse getSummary();

    /**
     * Executes the get type breakdown operation.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<TypeBreakdownResponse> getTypeBreakdown(Pageable pageable);

    /**
     * Executes the get department assets operation.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<DepartmentAssetSummaryResponse> getDepartmentAssets(Pageable pageable);

    /**
     * Executes the get warranty alerts operation.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetResponse> getWarrantyAlerts(Pageable pageable);

    /**
     * Executes the get license alerts operation.
     *
     * @return the resulting dashboard
     */
    LicenseAlertsResponse getLicenseAlerts();

    /**
     * Executes the get maintenance summary operation.
     *
     * @return the resulting dashboard
     */
    MaintenanceSummaryResponse getMaintenanceSummary();

    /**
     * Executes the get asset value operation.
     *
     * @return the resulting dashboard
     */
    AssetValueResponse getAssetValue();
}
