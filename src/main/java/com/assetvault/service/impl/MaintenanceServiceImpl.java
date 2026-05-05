package com.assetvault.service.impl;

import com.assetvault.dto.MaintenanceRecordRequest;
import com.assetvault.dto.MaintenanceRecordResponse;
import com.assetvault.exception.AssetNotFoundException;
import com.assetvault.exception.AssetRetiredException;
import com.assetvault.exception.MaintenanceStateException;
import com.assetvault.exception.MaintenanceRecordNotFoundException;
import com.assetvault.model.Asset;
import com.assetvault.model.enums.AssetStatus;
import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.model.enums.MaintenanceStatus;
import com.assetvault.model.enums.MaintenanceType;
import com.assetvault.model.MaintenanceRecord;
import com.assetvault.repository.AssetAssignmentRepository;
import com.assetvault.repository.AssetRepository;
import com.assetvault.repository.MaintenanceRecordRepository;
import com.assetvault.service.MaintenanceService;
import com.assetvault.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {
    private static final List<MaintenanceStatus> ACTIVE_MAINTENANCE_STATUSES = List.of(
            MaintenanceStatus.SCHEDULED,
            MaintenanceStatus.IN_PROGRESS
    );

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
        syncAssetForMaintenance(asset, status, null);
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);
        return Mapper.toMaintenanceResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRecordResponse> getAll(Pageable pageable) {
        return maintenanceRecordRepository.findAll(pageable).map(Mapper::toMaintenanceResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceRecordResponse getById(Integer id) {
        return Mapper.toMaintenanceResponse(findRecord(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRecordResponse> getByAsset(Long assetId, Pageable pageable) {
        findAsset(assetId);
        return maintenanceRecordRepository.findByAssetId(assetId, pageable)
                .map(Mapper::toMaintenanceResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRecordResponse> getByStatus(MaintenanceStatus status, Pageable pageable) {
        return maintenanceRecordRepository.findByStatus(status, pageable).map(Mapper::toMaintenanceResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRecordResponse> getByType(MaintenanceType type, Pageable pageable) {
        return maintenanceRecordRepository.findByMaintenanceType(type, pageable)
                .map(Mapper::toMaintenanceResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRecordResponse> getScheduled(Pageable pageable) {
        return maintenanceRecordRepository.findScheduledMaintenance(LocalDate.now(), pageable)
                .map(Mapper::toMaintenanceResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRecordResponse> getByDateRange(LocalDate from, LocalDate to, Pageable pageable) {
        validateDateRange(from, to);
        return maintenanceRecordRepository.findByScheduledDateBetween(from, to, pageable)
                .map(Mapper::toMaintenanceResponse);
    }

    @Override
    @Transactional
    public MaintenanceRecordResponse update(Integer id, MaintenanceRecordRequest request) {
        MaintenanceRecord record = findRecord(id);
        Asset previousAsset = record.getAsset();
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
        syncAssetForMaintenance(asset, status, record.getId());
        if (!previousAsset.getId().equals(asset.getId())) {
            restoreAssetStatus(previousAsset, record.getId());
        }
        return Mapper.toMaintenanceResponse(maintenanceRecordRepository.save(record));
    }

    @Override
    @Transactional
    public MaintenanceRecordResponse start(Integer id) {
        MaintenanceRecord record = findRecord(id);
        ensureRecordStatus(record, MaintenanceStatus.SCHEDULED, "start");
        ensureNotRetired(record.getAsset());
        record.setStatus(MaintenanceStatus.IN_PROGRESS);
        syncAssetForMaintenance(record.getAsset(), MaintenanceStatus.IN_PROGRESS, record.getId());
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);
        log.info("Maintenance record {} started", id);
        return Mapper.toMaintenanceResponse(saved);
    }

    @Override
    @Transactional
    public MaintenanceRecordResponse complete(Integer id) {
        MaintenanceRecord record = findRecord(id);
        ensureRecordStatus(record, MaintenanceStatus.IN_PROGRESS, "complete");
        ensureAssetUnderMaintenance(record.getAsset(), "complete");
        record.setStatus(MaintenanceStatus.COMPLETED);
        record.setCompletedDate(LocalDate.now());
        restoreAssetStatus(record.getAsset(), record.getId());
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);
        log.info("Maintenance record {} completed", id);
        return Mapper.toMaintenanceResponse(saved);
    }

    @Override
    @Transactional
    public MaintenanceRecordResponse cancel(Integer id) {
        MaintenanceRecord record = findRecord(id);
        ensureRecordStatus(record, MaintenanceStatus.SCHEDULED, "cancel");
        ensureAssetUnderMaintenance(record.getAsset(), "cancel");
        record.setStatus(MaintenanceStatus.CANCELLED);
        restoreAssetStatus(record.getAsset(), record.getId());
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);
        log.info("Maintenance record {} cancelled", id);
        return Mapper.toMaintenanceResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        MaintenanceRecord record = findRecord(id);
        Asset asset = record.getAsset();
        boolean activeMaintenance = isActiveMaintenanceStatus(record.getStatus());
        maintenanceRecordRepository.delete(record);
        if (activeMaintenance) {
            restoreAssetStatus(asset, record.getId());
        }
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

    private void ensureRecordStatus(MaintenanceRecord record, MaintenanceStatus requiredStatus, String action) {
        if (record.getStatus() != requiredStatus) {
            throw new MaintenanceStateException(
                    "Maintenance record %d must be %s before it can be %s"
                            .formatted(record.getId(), requiredStatus, action)
            );
        }
    }

    private void ensureAssetUnderMaintenance(Asset asset, String action) {
        if (asset.getStatus() != AssetStatus.UNDER_MAINTENANCE) {
            throw new MaintenanceStateException(
                    "Asset %s must be UNDER_MAINTENANCE before maintenance can be %s"
                            .formatted(asset.getAssetCode(), action)
            );
        }
    }

    private void syncAssetForMaintenance(Asset asset, MaintenanceStatus status, Integer currentRecordId) {
        if (isActiveMaintenanceStatus(status)) {
            asset.setStatus(AssetStatus.UNDER_MAINTENANCE);
            assetRepository.save(asset);
        } else {
            restoreAssetStatus(asset, currentRecordId);
        }
    }

    private void restoreAssetStatus(Asset asset, Integer currentRecordId) {
        if (asset.getStatus() == AssetStatus.RETIRED || asset.getStatus() == AssetStatus.LOST) {
            return;
        }
        if (hasOtherActiveMaintenance(asset.getId(), currentRecordId)) {
            asset.setStatus(AssetStatus.UNDER_MAINTENANCE);
            assetRepository.save(asset);
            return;
        }
        boolean activelyAssigned = assetAssignmentRepository.existsByAssetIdAndStatus(
                asset.getId(),
                AssignmentStatus.ACTIVE
        );
        asset.setStatus(activelyAssigned ? AssetStatus.ASSIGNED : AssetStatus.AVAILABLE);
        assetRepository.save(asset);
    }

    private boolean hasOtherActiveMaintenance(Long assetId, Integer currentRecordId) {
        if (currentRecordId == null) {
            return maintenanceRecordRepository.existsByAssetIdAndStatusIn(
                    assetId,
                    ACTIVE_MAINTENANCE_STATUSES
            );
        }
        return maintenanceRecordRepository.existsByAssetIdAndStatusInAndIdNot(
                assetId,
                ACTIVE_MAINTENANCE_STATUSES,
                currentRecordId
        );
    }

    private boolean isActiveMaintenanceStatus(MaintenanceStatus status) {
        return ACTIVE_MAINTENANCE_STATUSES.contains(status);
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
