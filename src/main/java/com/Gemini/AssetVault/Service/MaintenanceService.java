package com.Gemini.AssetVault.Service;

import com.Gemini.AssetVault.Dto.MaintenanceRecordRequest;
import com.Gemini.AssetVault.Dto.MaintenanceRecordResponse;
import com.Gemini.AssetVault.Model.Enum.MaintenanceStatus;
import com.Gemini.AssetVault.Model.Enum.MaintenanceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface MaintenanceService {
    MaintenanceRecordResponse create(MaintenanceRecordRequest request);

    Page<MaintenanceRecordResponse> getAll(Pageable pageable);

    MaintenanceRecordResponse getById(Integer id);

    Page<MaintenanceRecordResponse> getByAsset(Long assetId, Pageable pageable);

    Page<MaintenanceRecordResponse> getByStatus(MaintenanceStatus status, Pageable pageable);

    Page<MaintenanceRecordResponse> getByType(MaintenanceType type, Pageable pageable);

    Page<MaintenanceRecordResponse> getScheduled(Pageable pageable);

    Page<MaintenanceRecordResponse> getByDateRange(LocalDate from, LocalDate to, Pageable pageable);

    MaintenanceRecordResponse update(Integer id, MaintenanceRecordRequest request);

    MaintenanceRecordResponse complete(Integer id);

    MaintenanceRecordResponse cancel(Integer id);

    void delete(Integer id);
}
