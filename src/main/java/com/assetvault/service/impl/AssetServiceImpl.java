package com.assetvault.service.impl;

import com.assetvault.dto.AssetRequest;
import com.assetvault.dto.AssetResponse;
import com.assetvault.exception.AssetNotFoundException;
import com.assetvault.exception.AssetRetiredException;
import com.assetvault.exception.DuplicateSerialNumberException;
import com.assetvault.model.Asset;
import com.assetvault.model.MaintenanceRecord;
import com.assetvault.model.enums.AssetStatus;
import com.assetvault.model.enums.AssetType;
import com.assetvault.model.enums.MaintenanceStatus;
import com.assetvault.repository.AssetRepository;
import com.assetvault.repository.MaintenanceRecordRepository;
import com.assetvault.service.AssetAssignmentService;
import com.assetvault.service.AssetService;
import com.assetvault.util.CodeGenerator;
import com.assetvault.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service implementation for asset operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {
    private final AssetRepository assetRepository;
    private final AssetAssignmentService assetAssignmentService;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    /**
     * Creates a new asset.
     *
     * @param request the request payload
     * @return the resulting asset
     */
    @Override
    @Transactional
    public AssetResponse create(AssetRequest request) {
        if (assetRepository.existsBySerialNumber(request.serialNumber())) {
            throw new DuplicateSerialNumberException("Serial number already exists in the system");
        }
        Asset asset = Asset.builder()
                .assetCode(CodeGenerator.pendingAssetCode())
                .name(request.name())
                .brand(request.brand())
                .model(request.model())
                .type(request.type())
                .status(request.status() == null ? AssetStatus.AVAILABLE : request.status())
                .purchaseDate(request.purchaseDate())
                .purchaseCost(request.purchaseCost())
                .warrantyExpiryDate(request.warrantyExpiryDate())
                .serialNumber(request.serialNumber())
                .location(request.location())
                .notes(request.notes())
                .build();
        Asset saved = assetRepository.saveAndFlush(asset);
        saved.setAssetCode(CodeGenerator.assetCode(request.type(), saved.getId()));
        saved = assetRepository.save(saved);
        log.info("Asset registered: {}", saved.getAssetCode());
        return Mapper.toAssetResponse(saved);
    }

    /**
     * Returns the requested page of assets.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getAll(Pageable pageable) {
        return assetRepository.findAll(pageable).map(Mapper::toAssetResponse);
    }

    /**
     * Returns the asset identified by the given id.
     *
     * @param id the database identifier
     * @return the resulting asset
     */
    @Override
    @Transactional(readOnly = true)
    public AssetResponse getById(Long id) {
        return Mapper.toAssetResponse(findAsset(id));
    }

    /**
     * Returns the asset identified by the supplied code.
     *
     * @param assetCode the generated asset code
     * @return the resulting asset
     */
    @Override
    @Transactional(readOnly = true)
    public AssetResponse getByCode(String assetCode) {
        return Mapper.toAssetResponse(assetRepository.findByAssetCode(assetCode)
                .orElseThrow(() -> new AssetNotFoundException("Asset code %s does not exist".formatted(assetCode))));
    }

    /**
     * Returns assets filtered by type.
     *
     * @param type the requested type value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getByType(AssetType type, Pageable pageable) {
        return assetRepository.findByType(type, pageable).map(Mapper::toAssetResponse);
    }

    /**
     * Returns assets filtered by status.
     *
     * @param status the requested status value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getByStatus(AssetStatus status, Pageable pageable) {
        return assetRepository.findByStatus(status, pageable).map(Mapper::toAssetResponse);
    }

    /**
     * Returns assets that are currently available.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getAvailable(Pageable pageable) {
        return getByStatus(AssetStatus.AVAILABLE, pageable);
    }

    /**
     * Returns assets associated with the supplied department.
     *
     * @param name the name or label value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getByDepartment(String name, Pageable pageable) {
        return assetRepository.findAssetsByDepartment(name, pageable).map(Mapper::toAssetResponse);
    }

    /**
     * Returns assets whose warranty expires inside the requested date window.
     *
     * @param days the number of days to look ahead
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getWarrantyExpiring(int days, Pageable pageable) {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(days);
        Page<AssetResponse> expiringAssets = assetRepository.findAssetsWithWarrantyExpiring(today, threshold, pageable)
                .map(Mapper::toAssetResponse);
        if (expiringAssets.hasContent()) {
            log.warn("{} assets have warranties expiring within {} days", expiringAssets.getTotalElements(), days);
        }
        return expiringAssets;
    }

    /**
     * Returns assets whose warranty has already expired.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getWarrantyExpired(Pageable pageable) {
        return assetRepository.findAssetsWithExpiredWarranty(LocalDate.now(), pageable)
                .map(Mapper::toAssetResponse);
    }

    /**
     * Searches assets using the supplied keyword.
     *
     * @param keyword the search keyword
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> search(String keyword, Pageable pageable) {
        return assetRepository.searchByKeyword(keyword, pageable).map(Mapper::toAssetResponse);
    }

    /**
     * Returns assets at or below the supplied maximum cost.
     *
     * @param maxCost the maximum accepted purchase cost
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getLowValue(BigDecimal maxCost, Pageable pageable) {
        return assetRepository.findByPurchaseCostLessThanEqual(maxCost, pageable)
                .map(Mapper::toAssetResponse);
    }

    /**
     * Updates an existing asset.
     *
     * @param id the database identifier
     * @param request the request payload
     * @return the resulting asset
     */
    @Override
    @Transactional
    public AssetResponse update(Long id, AssetRequest request) {
        Asset asset = findAsset(id);
        ensureNotRetired(asset);
        if (assetRepository.existsBySerialNumberAndIdNot(request.serialNumber(), id)) {
            throw new DuplicateSerialNumberException("Serial number already exists in the system");
        }
        AssetStatus targetStatus = request.status() == null ? asset.getStatus() : request.status();
        if (targetStatus == AssetStatus.RETIRED) {
            releaseActiveAssignment(id, "Returned automatically before asset retirement");
        }
        if (targetStatus == AssetStatus.LOST) {
            cancelInProgressMaintenance(asset);
        }
        asset.setName(request.name());
        asset.setBrand(request.brand());
        asset.setModel(request.model());
        asset.setType(request.type());
        asset.setStatus(targetStatus);
        asset.setPurchaseDate(request.purchaseDate());
        asset.setPurchaseCost(request.purchaseCost());
        asset.setWarrantyExpiryDate(request.warrantyExpiryDate());
        asset.setSerialNumber(request.serialNumber());
        asset.setLocation(request.location());
        asset.setNotes(request.notes());
        return Mapper.toAssetResponse(assetRepository.save(asset));
    }

    /**
     * Updates the status of the asset.
     *
     * @param id the database identifier
     * @param status the requested status value
     * @return the resulting asset
     */
    @Override
    @Transactional
    public AssetResponse updateStatus(Long id, AssetStatus status) {
        Asset asset = findAsset(id);
        if (asset.getStatus() == AssetStatus.RETIRED
                && status != AssetStatus.RETIRED) {
            throw new AssetRetiredException("Operation is not allowed on retired asset %s".formatted(asset.getAssetCode()));
        }
        if (status == AssetStatus.RETIRED) {
            releaseActiveAssignment(id, "Returned automatically before asset retirement");
        }
        if (status == AssetStatus.LOST) {
            cancelInProgressMaintenance(asset);
        }
        asset.setStatus(status);
        Asset saved = assetRepository.save(asset);
        log.info("Asset {} status updated to {}", saved.getAssetCode(), saved.getStatus());
        return Mapper.toAssetResponse(saved);
    }

    /**
     * Marks the asset as retired.
     *
     * @param id the database identifier
     * @return the resulting asset
     */
    @Override
    @Transactional
    public AssetResponse retire(Long id) {
        Asset asset = findAsset(id);
        int releasedAssignments = releaseActiveAssignment(id, "Returned automatically before asset retirement");
        asset.setStatus(AssetStatus.RETIRED);
        Asset saved = assetRepository.save(asset);
        log.info("Asset retired: {} (releasedAssignments={})", saved.getAssetCode(), releasedAssignments);
        return Mapper.toAssetResponse(saved);
    }

    /**
     * Marks the asset as lost.
     *
     * @param id the database identifier
     * @return the resulting asset
     */
    @Override
    @Transactional
    public AssetResponse markLost(Long id) {
        Asset asset = findAsset(id);
        ensureNotRetired(asset);
        int cancelledMaintenance = cancelInProgressMaintenance(asset);
        asset.setStatus(AssetStatus.LOST);
        Asset saved = assetRepository.save(asset);
        log.info("Asset marked lost: {} (cancelledMaintenance={})", saved.getAssetCode(), cancelledMaintenance);
        return Mapper.toAssetResponse(saved);
    }

    /**
     * Deletes the asset.
     *
     * @param id the database identifier
     */
    @Override
    @Transactional
    public void delete(Long id) {
        Asset asset = findAsset(id);
        int releasedAssignments = releaseActiveAssignment(id, "Returned automatically before asset deletion");
        assetRepository.delete(asset);
        log.info("Asset deleted: {} (releasedAssignments={})", asset.getAssetCode(), releasedAssignments);
    }

    /**
     * Finds the asset entity for the supplied identifier.
     *
     * @param id the database identifier
     * @return the resulting asset
     */
    private Asset findAsset(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new AssetNotFoundException("Asset ID %d does not exist".formatted(id)));
    }

    /**
     * Ensures that the supplied asset is not retired.
     *
     * @param asset the asset entity to map or validate
     */
    private void ensureNotRetired(Asset asset) {
        if (asset.getStatus() == AssetStatus.RETIRED) {
            throw new AssetRetiredException("Operation is not allowed on retired asset %s".formatted(asset.getAssetCode()));
        }
    }

    /**
     * Releases active asset assignments for the supplied asset.
     *
     * @param assetId the asset identifier
     * @param remarks the reason captured for the state change
     * @return the computed numeric result
     */
    private int releaseActiveAssignment(Long assetId, String remarks) {
        return assetAssignmentService.releaseActiveAssignmentsForAsset(assetId, remarks);
    }

    /**
     * Cancels in-progress maintenance records for the supplied asset.
     *
     * @param asset the asset entity to map or validate
     * @return the computed numeric result
     */
    private int cancelInProgressMaintenance(Asset asset) {
        List<MaintenanceRecord> records = maintenanceRecordRepository
                .findAllByAssetIdAndStatusOrderByScheduledDateDescIdDesc(
                        asset.getId(),
                        MaintenanceStatus.IN_PROGRESS
                );
        records.forEach(record -> record.setStatus(MaintenanceStatus.CANCELLED));
        if (!records.isEmpty()) {
            maintenanceRecordRepository.saveAll(records);
            log.info("{} in-progress maintenance records cancelled for asset {}", records.size(), asset.getAssetCode());
        }
        return records.size();
    }
}
