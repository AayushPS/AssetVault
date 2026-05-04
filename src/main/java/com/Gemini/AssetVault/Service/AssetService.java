package com.Gemini.AssetVault.Service;

import com.Gemini.AssetVault.Dto.AssetRequest;
import com.Gemini.AssetVault.Dto.AssetResponse;
import com.Gemini.AssetVault.Model.Enum.AssetStatus;
import com.Gemini.AssetVault.Model.Enum.AssetType;
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
