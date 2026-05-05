package com.assetvault.service;

import com.assetvault.dto.AssetRequest;
import com.assetvault.dto.AssetResponse;
import com.assetvault.model.enums.AssetStatus;
import com.assetvault.model.enums.AssetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface AssetService {
    AssetResponse create(AssetRequest request);

    Page<AssetResponse> getAll(Pageable pageable);

    AssetResponse getById(Long id);

    AssetResponse getByCode(String assetCode);

    Page<AssetResponse> getByType(AssetType type, Pageable pageable);

    Page<AssetResponse> getByStatus(AssetStatus status, Pageable pageable);

    Page<AssetResponse> getAvailable(Pageable pageable);

    Page<AssetResponse> getByDepartment(String name, Pageable pageable);

    Page<AssetResponse> getWarrantyExpiring(int days, Pageable pageable);

    Page<AssetResponse> getWarrantyExpired(Pageable pageable);

    Page<AssetResponse> search(String keyword, Pageable pageable);

    Page<AssetResponse> getLowValue(BigDecimal maxCost, Pageable pageable);

    AssetResponse update(Long id, AssetRequest request);

    AssetResponse updateStatus(Long id, AssetStatus status);

    AssetResponse retire(Long id);

    AssetResponse markLost(Long id);

    void delete(Long id);
}
