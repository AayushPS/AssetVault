package com.Gemini.AssetVault.Service;

import com.Gemini.AssetVault.Dto.AssetAssignmentRequest;
import com.Gemini.AssetVault.Dto.AssetAssignmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AssetAssignmentService {
    AssetAssignmentResponse assign(AssetAssignmentRequest request);

    Page<AssetAssignmentResponse> getAll(Pageable pageable);

    AssetAssignmentResponse getById(Long id);

    Page<AssetAssignmentResponse> getActive(Pageable pageable);

    Page<AssetAssignmentResponse> getByAsset(Long assetId, Pageable pageable);

    Page<AssetAssignmentResponse> getByEmployee(Long employeeId, Pageable pageable);

    Page<AssetAssignmentResponse> getByDateRange(LocalDate from, LocalDate to, Pageable pageable);

    AssetAssignmentResponse returnAsset(Long id);

    AssetAssignmentResponse transfer(Long id, Long toEmployeeId);

    void delete(Long id);
}
