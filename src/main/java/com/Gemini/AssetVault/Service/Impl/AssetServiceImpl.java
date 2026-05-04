package com.Gemini.AssetVault.Service.Impl;

import com.Gemini.AssetVault.Dto.AssetRequest;
import com.Gemini.AssetVault.Dto.AssetResponse;
import com.Gemini.AssetVault.Exception.AssetNotFoundException;
import com.Gemini.AssetVault.Exception.AssetRetiredException;
import com.Gemini.AssetVault.Exception.DuplicateSerialNumberException;
import com.Gemini.AssetVault.Model.Asset;
import com.Gemini.AssetVault.Model.Enum.AssetStatus;
import com.Gemini.AssetVault.Model.Enum.AssetType;
import com.Gemini.AssetVault.Repository.AssetRepository;
import com.Gemini.AssetVault.Service.AssetService;
import com.Gemini.AssetVault.Util.AssetCodeGenerator;
import com.Gemini.AssetVault.Util.AssetVaultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {
    private final AssetRepository assetRepository;

    @Override
    @Transactional
    public AssetResponse create(AssetRequest request) {
        log.debug("Creating asset with serial {}", request.serialNumber());
        if (assetRepository.existsBySerialNumber(request.serialNumber())) {
            throw new DuplicateSerialNumberException("Serial number already exists in the system");
        }
        Asset asset = Asset.builder()
                .assetCode(generateAssetCode(request.type()))
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
        Asset saved = assetRepository.save(asset);
        log.info("Asset registered: {}", saved.getAssetCode());
        return AssetVaultMapper.toAssetResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getAll(Pageable pageable) {
        log.debug("Fetching assets page {}", pageable);
        return assetRepository.findAll(pageable).map(AssetVaultMapper::toAssetResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AssetResponse getById(Long id) {
        return AssetVaultMapper.toAssetResponse(findAsset(id));
    }

    @Override
    @Transactional(readOnly = true)
    public AssetResponse getByCode(String assetCode) {
        return AssetVaultMapper.toAssetResponse(assetRepository.findByAssetCode(assetCode)
                .orElseThrow(() -> new AssetNotFoundException("Asset code %s does not exist".formatted(assetCode))));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getByType(AssetType type, Pageable pageable) {
        return assetRepository.findByType(type, pageable).map(AssetVaultMapper::toAssetResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getByStatus(AssetStatus status, Pageable pageable) {
        return assetRepository.findByStatus(status, pageable).map(AssetVaultMapper::toAssetResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getAvailable(Pageable pageable) {
        return getByStatus(AssetStatus.AVAILABLE, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getByDepartment(String name, Pageable pageable) {
        return assetRepository.findAssetsByDepartment(name, pageable).map(AssetVaultMapper::toAssetResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getWarrantyExpiring(int days, Pageable pageable) {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(days);
        Page<AssetResponse> expiringAssets = assetRepository.findAssetsWithWarrantyExpiring(today, threshold, pageable)
                .map(AssetVaultMapper::toAssetResponse);
        if (expiringAssets.hasContent()) {
            log.warn("{} assets have warranties expiring within {} days", expiringAssets.getTotalElements(), days);
        }
        return expiringAssets;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getWarrantyExpired(Pageable pageable) {
        return assetRepository.findAssetsWithExpiredWarranty(LocalDate.now(), pageable)
                .map(AssetVaultMapper::toAssetResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> search(String keyword, Pageable pageable) {
        return assetRepository.searchByKeyword(keyword, pageable).map(AssetVaultMapper::toAssetResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getLowValue(BigDecimal maxCost, Pageable pageable) {
        return assetRepository.findByPurchaseCostLessThanEqual(maxCost, pageable)
                .map(AssetVaultMapper::toAssetResponse);
    }

    @Override
    @Transactional
    public AssetResponse update(Long id, AssetRequest request) {
        Asset asset = findAsset(id);
        ensureNotRetired(asset);
        if (assetRepository.existsBySerialNumberAndIdNot(request.serialNumber(), id)) {
            throw new DuplicateSerialNumberException("Serial number already exists in the system");
        }
        asset.setName(request.name());
        asset.setBrand(request.brand());
        asset.setModel(request.model());
        asset.setType(request.type());
        asset.setStatus(request.status() == null ? asset.getStatus() : request.status());
        asset.setPurchaseDate(request.purchaseDate());
        asset.setPurchaseCost(request.purchaseCost());
        asset.setWarrantyExpiryDate(request.warrantyExpiryDate());
        asset.setSerialNumber(request.serialNumber());
        asset.setLocation(request.location());
        asset.setNotes(request.notes());
        return AssetVaultMapper.toAssetResponse(assetRepository.save(asset));
    }

    @Override
    @Transactional
    public AssetResponse updateStatus(Long id, AssetStatus status) {
        Asset asset = findAsset(id);
        if (asset.getStatus() == AssetStatus.RETIRED && status != AssetStatus.RETIRED) {
            throw new AssetRetiredException("Operation is not allowed on retired asset %s".formatted(asset.getAssetCode()));
        }
        asset.setStatus(status);
        return AssetVaultMapper.toAssetResponse(assetRepository.save(asset));
    }

    @Override
    @Transactional
    public AssetResponse retire(Long id) {
        Asset asset = findAsset(id);
        asset.setStatus(AssetStatus.RETIRED);
        Asset saved = assetRepository.save(asset);
        log.info("Asset retired: {}", saved.getAssetCode());
        return AssetVaultMapper.toAssetResponse(saved);
    }

    @Override
    @Transactional
    public AssetResponse markLost(Long id) {
        Asset asset = findAsset(id);
        ensureNotRetired(asset);
        asset.setStatus(AssetStatus.LOST);
        return AssetVaultMapper.toAssetResponse(assetRepository.save(asset));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Asset asset = findAsset(id);
        ensureNotRetired(asset);
        assetRepository.delete(asset);
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

    private String generateAssetCode(AssetType type) {
        long sequence = assetRepository.countByType(type) + 1;
        String code = AssetCodeGenerator.assetCode(type, sequence);
        while (assetRepository.existsByAssetCode(code)) {
            sequence++;
            code = AssetCodeGenerator.assetCode(type, sequence);
        }
        return code;
    }
}
