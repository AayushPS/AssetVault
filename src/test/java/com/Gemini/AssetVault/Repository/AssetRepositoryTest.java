package com.Gemini.AssetVault.Repository;

import com.Gemini.AssetVault.Model.Asset;
import com.Gemini.AssetVault.Model.Enum.AssetStatus;
import com.Gemini.AssetVault.Model.Enum.AssetType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(AssetRepositoryTest.JpaAuditingConfig.class)
class AssetRepositoryTest {
    @Autowired
    private AssetRepository assetRepository;

    @Test
    void findsAssetsByTypeAndStatus() {
        assetRepository.save(asset("LPT-00001", AssetType.LAPTOP, AssetStatus.AVAILABLE, "SER-001"));
        assetRepository.save(asset("MON-00001", AssetType.MONITOR, AssetStatus.ASSIGNED, "SER-002"));

        assertThat(assetRepository.findByType(AssetType.LAPTOP, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(assetRepository.findByStatus(AssetStatus.ASSIGNED, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"macbook", "apple", "m3"})
    void searchesByNameBrandOrModel(String keyword) {
        assetRepository.save(asset("LPT-00001", AssetType.LAPTOP, AssetStatus.AVAILABLE, "SER-001"));

        assertThat(assetRepository.searchByKeyword(keyword, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
    }

    @Test
    void findsWarrantyExpiringWithinWindow() {
        assetRepository.save(asset("LPT-00001", AssetType.LAPTOP, AssetStatus.AVAILABLE, "SER-001"));
        Asset laterWarranty = asset("DSK-00001", AssetType.DESKTOP, AssetStatus.AVAILABLE, "SER-002");
        laterWarranty.setWarrantyExpiryDate(LocalDate.now().plusDays(90));
        assetRepository.save(laterWarranty);

        assertThat(assetRepository.findAssetsWithWarrantyExpiring(
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                PageRequest.of(0, 10)
        ).getTotalElements()).isEqualTo(1);
    }

    private Asset asset(String code, AssetType type, AssetStatus status, String serial) {
        return Asset.builder()
                .assetCode(code)
                .name("MacBook Pro")
                .brand("Apple")
                .model("M3")
                .type(type)
                .status(status)
                .purchaseDate(LocalDate.now())
                .purchaseCost(BigDecimal.valueOf(1000))
                .warrantyExpiryDate(LocalDate.now().plusDays(20))
                .serialNumber(serial)
                .location("HQ")
                .build();
    }

    @TestConfiguration
    @EnableJpaAuditing
    static class JpaAuditingConfig {
    }
}
