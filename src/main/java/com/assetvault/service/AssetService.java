package com.assetvault.service;

import com.assetvault.dto.AssetRequest;
import com.assetvault.dto.AssetResponse;
import com.assetvault.model.enums.AssetStatus;
import com.assetvault.model.enums.AssetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/**
 * Service contract for asset operations.
 */
public interface AssetService {
    /**
     * Creates a new asset.
     *
     * @param request the request payload
     * @return the resulting asset
     */
    AssetResponse create(AssetRequest request);

    /**
     * Returns the requested page of assets.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetResponse> getAll(Pageable pageable);

    /**
     * Returns the asset identified by the given id.
     *
     * @param id the database identifier
     * @return the resulting asset
     */
    AssetResponse getById(Long id);

    /**
     * Returns the asset identified by the supplied code.
     *
     * @param assetCode the generated asset code
     * @return the resulting asset
     */
    AssetResponse getByCode(String assetCode);

    /**
     * Returns assets filtered by type.
     *
     * @param type the requested type value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetResponse> getByType(AssetType type, Pageable pageable);

    /**
     * Returns assets filtered by status.
     *
     * @param status the requested status value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetResponse> getByStatus(AssetStatus status, Pageable pageable);

    /**
     * Returns assets that are currently available.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetResponse> getAvailable(Pageable pageable);

    /**
     * Returns assets associated with the supplied department.
     *
     * @param name the name or label value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetResponse> getByDepartment(String name, Pageable pageable);

    /**
     * Returns assets whose warranty expires inside the requested date window.
     *
     * @param days the number of days to look ahead
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetResponse> getWarrantyExpiring(int days, Pageable pageable);

    /**
     * Returns assets whose warranty has already expired.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetResponse> getWarrantyExpired(Pageable pageable);

    /**
     * Searches assets using the supplied keyword.
     *
     * @param keyword the search keyword
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetResponse> search(String keyword, Pageable pageable);

    /**
     * Returns assets at or below the supplied maximum cost.
     *
     * @param maxCost the maximum accepted purchase cost
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetResponse> getLowValue(BigDecimal maxCost, Pageable pageable);

    /**
     * Updates an existing asset.
     *
     * @param id the database identifier
     * @param request the request payload
     * @return the resulting asset
     */
    AssetResponse update(Long id, AssetRequest request);

    /**
     * Updates the status of the asset.
     *
     * @param id the database identifier
     * @param status the requested status value
     * @return the resulting asset
     */
    AssetResponse updateStatus(Long id, AssetStatus status);

    /**
     * Marks the asset as retired.
     *
     * @param id the database identifier
     * @return the resulting asset
     */
    AssetResponse retire(Long id);

    /**
     * Marks the asset as lost.
     *
     * @param id the database identifier
     * @return the resulting asset
     */
    AssetResponse markLost(Long id);

    /**
     * Deletes the asset.
     *
     * @param id the database identifier
     */
    void delete(Long id);
}
