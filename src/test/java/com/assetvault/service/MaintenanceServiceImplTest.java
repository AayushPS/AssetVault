package com.assetvault.service;

import com.assetvault.dto.MaintenanceRecordRequest;
import com.assetvault.exception.AssetRetiredException;
import com.assetvault.exception.MaintenanceStateException;
import com.assetvault.model.Asset;
import com.assetvault.model.enums.AssetStatus;
import com.assetvault.model.enums.AssetType;
import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.model.enums.MaintenanceStatus;
import com.assetvault.model.enums.MaintenanceType;
import com.assetvault.model.MaintenanceRecord;
import com.assetvault.repository.AssetAssignmentRepository;
import com.assetvault.repository.AssetRepository;
import com.assetvault.repository.MaintenanceRecordRepository;
import com.assetvault.service.impl.MaintenanceServiceImpl;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceImplTest {
    @Mock
    private MaintenanceRecordRepository maintenanceRecordRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetAssignmentRepository assetAssignmentRepository;

    private MaintenanceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MaintenanceServiceImpl(maintenanceRecordRepository, assetRepository, assetAssignmentRepository);
    }

    @Test
    void createMovesScheduledAssetUnderMaintenance() {
        Asset asset = asset(AssetStatus.AVAILABLE);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(maintenanceRecordRepository.save(any(MaintenanceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(request(MaintenanceStatus.SCHEDULED));

        assertThat(asset.getStatus()).isEqualTo(AssetStatus.UNDER_MAINTENANCE);
        verify(assetRepository).save(asset);
    }

    @Test
    void createRejectsRetiredAsset() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset(AssetStatus.RETIRED)));

        assertThatThrownBy(() -> service.create(request(MaintenanceStatus.SCHEDULED)))
                .isInstanceOf(AssetRetiredException.class);
    }

    @Test
    void startScheduledMaintenanceMovesRecordInProgressAndAssetUnderMaintenance() {
        Asset asset = asset(AssetStatus.AVAILABLE);
        MaintenanceRecord record = record(asset, MaintenanceStatus.SCHEDULED);
        when(maintenanceRecordRepository.findById(10)).thenReturn(Optional.of(record));
        when(maintenanceRecordRepository.save(any(MaintenanceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.start(10);

        assertThat(record.getStatus()).isEqualTo(MaintenanceStatus.IN_PROGRESS);
        assertThat(asset.getStatus()).isEqualTo(AssetStatus.UNDER_MAINTENANCE);
        verify(assetRepository).save(asset);
    }

    @Test
    void startRejectsRecordThatIsNotScheduled() {
        Asset asset = asset(AssetStatus.UNDER_MAINTENANCE);
        MaintenanceRecord record = record(asset, MaintenanceStatus.IN_PROGRESS);
        when(maintenanceRecordRepository.findById(10)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> service.start(10))
                .isInstanceOf(MaintenanceStateException.class)
                .hasMessageContaining("SCHEDULED");
    }

    @Test
    void completeRestoresAssignedStatusWhenAssetHasActiveAssignment() {
        Asset asset = asset(AssetStatus.UNDER_MAINTENANCE);
        MaintenanceRecord record = record(asset, MaintenanceStatus.IN_PROGRESS);
        when(maintenanceRecordRepository.findById(10)).thenReturn(Optional.of(record));
        when(assetAssignmentRepository.existsByAssetIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(true);
        when(maintenanceRecordRepository.save(any(MaintenanceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.complete(10);

        assertThat(record.getStatus()).isEqualTo(MaintenanceStatus.COMPLETED);
        assertThat(record.getCompletedDate()).isEqualTo(LocalDate.now());
        assertThat(asset.getStatus()).isEqualTo(AssetStatus.ASSIGNED);
    }

    @Test
    void cancelRestoresAvailableStatusWhenAssetHasNoActiveAssignment() {
        Asset asset = asset(AssetStatus.UNDER_MAINTENANCE);
        MaintenanceRecord record = record(asset, MaintenanceStatus.SCHEDULED);
        when(maintenanceRecordRepository.findById(10)).thenReturn(Optional.of(record));
        when(assetAssignmentRepository.existsByAssetIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(false);
        when(maintenanceRecordRepository.save(any(MaintenanceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.cancel(10);

        assertThat(record.getStatus()).isEqualTo(MaintenanceStatus.CANCELLED);
        assertThat(asset.getStatus()).isEqualTo(AssetStatus.AVAILABLE);
    }

    @Test
    void completeKeepsAssetUnderMaintenanceWhenAnotherActiveMaintenanceExists() {
        Asset asset = asset(AssetStatus.UNDER_MAINTENANCE);
        MaintenanceRecord record = record(asset, MaintenanceStatus.IN_PROGRESS);
        when(maintenanceRecordRepository.findById(10)).thenReturn(Optional.of(record));
        when(maintenanceRecordRepository.existsByAssetIdAndStatusInAndIdNot(eq(1L), any(), eq(10)))
                .thenReturn(true);
        when(maintenanceRecordRepository.save(any(MaintenanceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.complete(10);

        assertThat(record.getStatus()).isEqualTo(MaintenanceStatus.COMPLETED);
        assertThat(asset.getStatus()).isEqualTo(AssetStatus.UNDER_MAINTENANCE);
        verify(assetRepository).save(asset);
    }

    @Test
    void completeRejectsRecordThatIsNotInProgress() {
        Asset asset = asset(AssetStatus.UNDER_MAINTENANCE);
        MaintenanceRecord record = record(asset, MaintenanceStatus.SCHEDULED);
        when(maintenanceRecordRepository.findById(10)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> service.complete(10))
                .isInstanceOf(MaintenanceStateException.class)
                .hasMessageContaining("IN_PROGRESS");
    }

    @Test
    void cancelRejectsAssetThatIsNotUnderMaintenance() {
        Asset asset = asset(AssetStatus.AVAILABLE);
        MaintenanceRecord record = record(asset, MaintenanceStatus.SCHEDULED);
        when(maintenanceRecordRepository.findById(10)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> service.cancel(10))
                .isInstanceOf(MaintenanceStateException.class)
                .hasMessageContaining("UNDER_MAINTENANCE");
    }

    @Test
    void deleteScheduledMaintenanceRestoresAssignedStatusWhenNoOtherMaintenanceExists() {
        Asset asset = asset(AssetStatus.UNDER_MAINTENANCE);
        MaintenanceRecord record = record(asset, MaintenanceStatus.SCHEDULED);
        when(maintenanceRecordRepository.findById(10)).thenReturn(Optional.of(record));
        when(assetAssignmentRepository.existsByAssetIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(true);

        service.delete(10);

        assertThat(asset.getStatus()).isEqualTo(AssetStatus.ASSIGNED);
        verify(maintenanceRecordRepository).delete(record);
        verify(assetRepository).save(asset);
    }

    @Test
    void dateRangeRejectsInvertedDates() {
        assertThatThrownBy(() -> service.getByDateRange(
                LocalDate.now(),
                LocalDate.now().minusDays(1),
                PageRequest.of(0, 10)
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void readMethodsCoverRepositoryQueries() {
        Asset asset = asset(AssetStatus.AVAILABLE);
        MaintenanceRecord record = record(asset, MaintenanceStatus.SCHEDULED);
        var pageable = PageRequest.of(0, 10);
        when(maintenanceRecordRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(record)));
        when(maintenanceRecordRepository.findById(10)).thenReturn(Optional.of(record));
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(maintenanceRecordRepository.findByAssetId(1L, pageable)).thenReturn(new PageImpl<>(List.of(record)));
        when(maintenanceRecordRepository.findByStatus(MaintenanceStatus.SCHEDULED, pageable))
                .thenReturn(new PageImpl<>(List.of(record)));
        when(maintenanceRecordRepository.findByMaintenanceType(MaintenanceType.REPAIR, pageable))
                .thenReturn(new PageImpl<>(List.of(record)));
        when(maintenanceRecordRepository.findScheduledMaintenance(any(), any())).thenReturn(new PageImpl<>(List.of(record)));
        when(maintenanceRecordRepository.findByScheduledDateBetween(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                pageable
        )).thenReturn(new PageImpl<>(List.of(record)));

        assertThat(service.getAll(pageable)).hasSize(1);
        assertThat(service.getById(10).status()).isEqualTo(MaintenanceStatus.SCHEDULED);
        assertThat(service.getByAsset(1L, pageable)).hasSize(1);
        assertThat(service.getByStatus(MaintenanceStatus.SCHEDULED, pageable)).hasSize(1);
        assertThat(service.getByType(MaintenanceType.REPAIR, pageable)).hasSize(1);
        assertThat(service.getScheduled(pageable)).hasSize(1);
        assertThat(service.getByDateRange(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), pageable)).hasSize(1);
    }

    @Test
    void updateAndDeleteCoverMutationPaths() {
        Asset asset = asset(AssetStatus.AVAILABLE);
        MaintenanceRecord record = record(asset, MaintenanceStatus.SCHEDULED);
        when(maintenanceRecordRepository.findById(10)).thenReturn(Optional.of(record));
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(assetAssignmentRepository.existsByAssetIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(false);
        when(maintenanceRecordRepository.save(any(MaintenanceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.update(10, request(MaintenanceStatus.COMPLETED));
        service.delete(10);

        assertThat(response.status()).isEqualTo(MaintenanceStatus.COMPLETED);
        assertThat(asset.getStatus()).isEqualTo(AssetStatus.AVAILABLE);
        verify(maintenanceRecordRepository).delete(record);
    }

    private MaintenanceRecordRequest request(MaintenanceStatus status) {
        return new MaintenanceRecordRequest(
                1L,
                MaintenanceType.REPAIR,
                "Keyboard replacement",
                BigDecimal.valueOf(3500),
                "Apple Service",
                LocalDate.now().plusDays(7),
                null,
                status
        );
    }

    private MaintenanceRecord record(Asset asset, MaintenanceStatus status) {
        return MaintenanceRecord.builder()
                .id(10)
                .asset(asset)
                .maintenanceType(MaintenanceType.REPAIR)
                .description("Keyboard replacement")
                .maintenanceCost(BigDecimal.valueOf(3500))
                .vendor("Apple Service")
                .scheduledDate(LocalDate.now())
                .status(status)
                .build();
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
