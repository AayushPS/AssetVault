package com.Gemini.AssetVault.Service.Impl;

import com.Gemini.AssetVault.Dto.MaintenanceRecordRequest;
import com.Gemini.AssetVault.Dto.MaintenanceRecordResponse;
import com.Gemini.AssetVault.Exception.AssetNotFoundException;
import com.Gemini.AssetVault.Exception.AssetRetiredException;
import com.Gemini.AssetVault.Exception.MaintenanceRecordNotFoundException;
import com.Gemini.AssetVault.Model.Asset;
import com.Gemini.AssetVault.Model.Enum.AssetStatus;
import com.Gemini.AssetVault.Model.Enum.AssignmentStatus;
import com.Gemini.AssetVault.Model.Enum.MaintenanceStatus;
import com.Gemini.AssetVault.Model.Enum.MaintenanceType;
import com.Gemini.AssetVault.Model.MaintenanceRecord;
import com.Gemini.AssetVault.Repository.AssetAssignmentRepository;
import com.Gemini.AssetVault.Repository.AssetRepository;
import com.Gemini.AssetVault.Repository.MaintenanceRecordRepository;
import com.Gemini.AssetVault.Service.MaintenanceService;
import com.Gemini.AssetVault.Util.AssetVaultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final AssetRepository assetRepository;
    private final AssetAssignmentRepository assetAssignmentRepository;

    @Override
    @Transactional
    public MaintenanceRecordResponse create(MaintenanceRecordRequest request) {
        Asset asset = findAsset(request.assetId());
        ensureNotRetired(asset);
        MaintenanceStatus status = request.status() == null ? MaintenanceStatus.SCHEDULED : request.status();
        MaintenanceRecord record = MaintenanceRecord.builder()
                .asset(asset)
                .maintenanceType(request.maintenanceType())
                .description(request.description())
                .maintenanceCost(request.maintenanceCost())
                .vendor(request.vendor())
                .scheduledDate(request.scheduledDate())
                .completedDate(request.completedDate())
                .status(status)
                .build();
        syncAssetForMaintenance(asset, status);
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);
        return AssetVaultMapper.toMaintenanceResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRecordResponse> getAll(Pageable pageable) {
        return maintenanceRecordRepository.findAll(pageable).map(AssetVaultMapper::toMaintenanceResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceRecordResponse getById(Integer id) {
        return AssetVaultMapper.toMaintenanceResponse(findRecord(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRecordResponse> getByAsset(Long assetId, Pageable pageable) {
        findAsset(assetId);
        return maintenanceRecordRepository.findByAssetId(assetId, pageable)
                .map(AssetVaultMapper::toMaintenanceResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRecordResponse> getByStatus(MaintenanceStatus status, Pageable pageable) {
        return maintenanceRecordRepository.findByStatus(status, pageable).map(AssetVaultMapper::toMaintenanceResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRecordResponse> getByType(MaintenanceType type, Pageable pageable) {
        return maintenanceRecordRepository.findByMaintenanceType(type, pageable)
                .map(AssetVaultMapper::toMaintenanceResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRecordResponse> getScheduled(Pageable pageable) {
        return maintenanceRecordRepository.findScheduledMaintenance(LocalDate.now(), pageable)
                .map(AssetVaultMapper::toMaintenanceResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRecordResponse> getByDateRange(LocalDate from, LocalDate to, Pageable pageable) {
        validateDateRange(from, to);
        return maintenanceRecordRepository.findByScheduledDateBetween(from, to, pageable)
                .map(AssetVaultMapper::toMaintenanceResponse);
    }

    @Override
    @Transactional
    public MaintenanceRecordResponse update(Integer id, MaintenanceRecordRequest request) {
        MaintenanceRecord record = findRecord(id);
        Asset asset = findAsset(request.assetId());
        ensureNotRetired(asset);
        MaintenanceStatus status = request.status() == null ? record.getStatus() : request.status();
        record.setAsset(asset);
        record.setMaintenanceType(request.maintenanceType());
        record.setDescription(request.description());
        record.setMaintenanceCost(request.maintenanceCost());
        record.setVendor(request.vendor());
        record.setScheduledDate(request.scheduledDate());
        record.setCompletedDate(request.completedDate());
        record.setStatus(status);
        syncAssetForMaintenance(asset, status);
        return AssetVaultMapper.toMaintenanceResponse(maintenanceRecordRepository.save(record));
    }

    @Override
    @Transactional
    public MaintenanceRecordResponse complete(Integer id) {
        MaintenanceRecord record = findRecord(id);
        record.setStatus(MaintenanceStatus.COMPLETED);
        record.setCompletedDate(LocalDate.now());
        restoreAssetStatus(record.getAsset());
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);
        log.info("Maintenance record {} completed", id);
        return AssetVaultMapper.toMaintenanceResponse(saved);
    }

    @Override
    @Transactional
    public MaintenanceRecordResponse cancel(Integer id) {
        MaintenanceRecord record = findRecord(id);
        record.setStatus(MaintenanceStatus.CANCELLED);
        restoreAssetStatus(record.getAsset());
        return AssetVaultMapper.toMaintenanceResponse(maintenanceRecordRepository.save(record));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        maintenanceRecordRepository.delete(findRecord(id));
    }

    private MaintenanceRecord findRecord(Integer id) {
        return maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new MaintenanceRecordNotFoundException(
                        "Maintenance record ID %d does not exist".formatted(id)
                ));
    }

    private Asset findAsset(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new AssetNotFoundException("Asset ID %d does not exist".formatted(id)));
    }

    private void ensureNotRetired(Asset asset) {
        if (asset.getStatus() == AssetStatus.RETIRED) {
            throw new AssetRetiredException("Operation is not allowed on retired asset %s".formatted(asset.getAssetCode()));
        }
    }

    private void syncAssetForMaintenance(Asset asset, MaintenanceStatus status) {
        if (status == MaintenanceStatus.SCHEDULED || status == MaintenanceStatus.IN_PROGRESS) {
            asset.setStatus(AssetStatus.UNDER_MAINTENANCE);
            assetRepository.save(asset);
        } else {
            restoreAssetStatus(asset);
        }
    }

    private void restoreAssetStatus(Asset asset) {
        if (asset.getStatus() == AssetStatus.RETIRED || asset.getStatus() == AssetStatus.LOST) {
            return;
        }
        boolean activelyAssigned = assetAssignmentRepository.existsByAssetIdAndStatus(
                asset.getId(),
                AssignmentStatus.ACTIVE
        );
        asset.setStatus(activelyAssigned ? AssetStatus.ASSIGNED : AssetStatus.AVAILABLE);
        assetRepository.save(asset);
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to dates are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be on or before to");
        }
    }
}
