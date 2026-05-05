package com.assetvault.service;

import com.assetvault.dto.AssetRequest;
import com.assetvault.exception.AssetRetiredException;
import com.assetvault.exception.DuplicateSerialNumberException;
import com.assetvault.model.Asset;
import com.assetvault.model.enums.AssetStatus;
import com.assetvault.model.enums.AssetType;
import com.assetvault.repository.AssetRepository;
import com.assetvault.service.impl.AssetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetServiceImplTest {
    @Mock
    private AssetRepository assetRepository;

    private AssetServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AssetServiceImpl(assetRepository);
    }

    @Test
    void deleteRejectsRetiredAsset() {
        Asset retiredAsset = asset(AssetStatus.RETIRED);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(retiredAsset));

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(AssetRetiredException.class);

        verify(assetRepository, never()).delete(retiredAsset);
    }

    @Test
    void createGeneratesCodeAndDefaultsStatusToAvailable() {
        when(assetRepository.existsBySerialNumber("SER-001")).thenReturn(false);
        when(assetRepository.countByType(AssetType.LAPTOP)).thenReturn(0L);
        when(assetRepository.existsByAssetCode("LPT-00001")).thenReturn(false);
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(request(null));

        assertThat(response.assetCode()).isEqualTo("LPT-00001");
        assertThat(response.status()).isEqualTo(AssetStatus.AVAILABLE);
    }

    @Test
    void createRejectsDuplicateSerialNumber() {
        when(assetRepository.existsBySerialNumber("SER-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request(AssetStatus.AVAILABLE)))
                .isInstanceOf(DuplicateSerialNumberException.class);

        verify(assetRepository, never()).save(any());
    }

    @Test
    void updateRejectsRetiredAsset() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset(AssetStatus.RETIRED)));

        assertThatThrownBy(() -> service.update(1L, request(AssetStatus.AVAILABLE)))
                .isInstanceOf(AssetRetiredException.class);
    }

    @Test
    void updateRejectsDuplicateSerialNumber() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset(AssetStatus.AVAILABLE)));
        when(assetRepository.existsBySerialNumberAndIdNot("SER-001", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request(AssetStatus.AVAILABLE)))
                .isInstanceOf(DuplicateSerialNumberException.class);
    }

    @Test
    void statusUpdateCannotMoveRetiredAssetBackToActiveState() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset(AssetStatus.RETIRED)));

        assertThatThrownBy(() -> service.updateStatus(1L, AssetStatus.AVAILABLE))
                .isInstanceOf(AssetRetiredException.class);
    }

    @Test
    void readMethodsCoverRepositoryQueries() {
        Asset asset = asset(AssetStatus.AVAILABLE);
        var pageable = PageRequest.of(0, 10);
        when(assetRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(asset)));
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(assetRepository.findByAssetCode("LPT-00001")).thenReturn(Optional.of(asset));
        when(assetRepository.findByType(AssetType.LAPTOP, pageable)).thenReturn(new PageImpl<>(List.of(asset)));
        when(assetRepository.findByStatus(AssetStatus.AVAILABLE, pageable)).thenReturn(new PageImpl<>(List.of(asset)));
        when(assetRepository.findAssetsByDepartment("Engineering", pageable)).thenReturn(new PageImpl<>(List.of(asset)));
        when(assetRepository.findAssetsWithWarrantyExpiring(any(), any(), any())).thenReturn(new PageImpl<>(List.of(asset)));
        when(assetRepository.findAssetsWithExpiredWarranty(any(), any())).thenReturn(new PageImpl<>(List.of(asset)));
        when(assetRepository.searchByKeyword("mac", pageable)).thenReturn(new PageImpl<>(List.of(asset)));
        when(assetRepository.findByPurchaseCostLessThanEqual(BigDecimal.valueOf(1000), pageable))
                .thenReturn(new PageImpl<>(List.of(asset)));

        assertThat(service.getAll(pageable)).hasSize(1);
        assertThat(service.getById(1L).assetCode()).isEqualTo("LPT-00001");
        assertThat(service.getByCode("LPT-00001").assetCode()).isEqualTo("LPT-00001");
        assertThat(service.getByType(AssetType.LAPTOP, pageable)).hasSize(1);
        assertThat(service.getByStatus(AssetStatus.AVAILABLE, pageable)).hasSize(1);
        assertThat(service.getAvailable(pageable)).hasSize(1);
        assertThat(service.getByDepartment("Engineering", pageable)).hasSize(1);
        assertThat(service.getWarrantyExpiring(30, pageable)).hasSize(1);
        assertThat(service.getWarrantyExpired(pageable)).hasSize(1);
        assertThat(service.search("mac", pageable)).hasSize(1);
        assertThat(service.getLowValue(BigDecimal.valueOf(1000), pageable)).hasSize(1);
    }

    @Test
    void lifecycleMethodsPersistExpectedStateChanges() {
        Asset updateAsset = asset(AssetStatus.AVAILABLE);
        Asset statusAsset = asset(AssetStatus.AVAILABLE);
        Asset retireAsset = asset(AssetStatus.AVAILABLE);
        Asset lostAsset = asset(AssetStatus.AVAILABLE);
        Asset deleteAsset = asset(AssetStatus.AVAILABLE);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(updateAsset));
        when(assetRepository.findById(2L)).thenReturn(Optional.of(statusAsset));
        when(assetRepository.findById(3L)).thenReturn(Optional.of(retireAsset));
        when(assetRepository.findById(4L)).thenReturn(Optional.of(lostAsset));
        when(assetRepository.findById(5L)).thenReturn(Optional.of(deleteAsset));
        when(assetRepository.existsBySerialNumberAndIdNot("SER-001", 1L)).thenReturn(false);
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.update(1L, request(AssetStatus.AVAILABLE)).name()).isEqualTo("MacBook Pro 14");
        assertThat(service.updateStatus(2L, AssetStatus.UNDER_MAINTENANCE).status()).isEqualTo(AssetStatus.UNDER_MAINTENANCE);
        assertThat(service.retire(3L).status()).isEqualTo(AssetStatus.RETIRED);
        assertThat(service.markLost(4L).status()).isEqualTo(AssetStatus.LOST);
        service.delete(5L);

        verify(assetRepository).delete(deleteAsset);
    }

    private AssetRequest request(AssetStatus status) {
        return new AssetRequest(
                "MacBook Pro 14",
                "Apple",
                "M3 Pro",
                AssetType.LAPTOP,
                status,
                LocalDate.now(),
                BigDecimal.valueOf(149999),
                LocalDate.now().plusYears(3),
                "SER-001",
                "Bengaluru HQ",
                "Onboarding"
        );
    }

    private Asset asset(AssetStatus status) {
        return Asset.builder()
                .id(1L)
                .assetCode("LPT-00001")
                .name("MacBook Pro 14")
                .brand("Apple")
                .model("M3 Pro")
                .type(AssetType.LAPTOP)
                .status(status)
                .purchaseDate(LocalDate.now())
                .purchaseCost(BigDecimal.valueOf(149999))
                .serialNumber("SER-001")
                .build();
    }
}
