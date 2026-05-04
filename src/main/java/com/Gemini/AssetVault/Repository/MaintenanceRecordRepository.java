package com.Gemini.AssetVault.Repository;

import com.Gemini.AssetVault.Model.Enum.MaintenanceStatus;
import com.Gemini.AssetVault.Model.Enum.MaintenanceType;
import com.Gemini.AssetVault.Model.MaintenanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Integer> {
    @Override
    Page<MaintenanceRecord> findAll(Pageable pageable);

    Page<MaintenanceRecord> findByAssetId(Long assetId, Pageable pageable);

    List<MaintenanceRecord> findAllByAssetIdOrderByScheduledDateDescIdDesc(Long assetId);

    Page<MaintenanceRecord> findByStatus(MaintenanceStatus status, Pageable pageable);

    List<MaintenanceRecord> findAllByStatus(MaintenanceStatus status);

    Page<MaintenanceRecord> findByMaintenanceType(MaintenanceType maintenanceType, Pageable pageable);

    List<MaintenanceRecord> findAllByMaintenanceType(MaintenanceType maintenanceType);

    Page<MaintenanceRecord> findByStatusAndScheduledDateGreaterThanEqualOrderByScheduledDateAscIdAsc(
            MaintenanceStatus status,
            LocalDate today,
            Pageable pageable
    );

    List<MaintenanceRecord> findByStatusAndScheduledDateGreaterThanEqualOrderByScheduledDateAscIdAsc(
            MaintenanceStatus status,
            LocalDate today
    );

    Page<MaintenanceRecord> findByScheduledDateBetween(LocalDate from, LocalDate to, Pageable pageable);

    List<MaintenanceRecord> findAllByScheduledDateBetweenOrderByScheduledDateDescIdDesc(LocalDate from, LocalDate to);

    long countByStatus(MaintenanceStatus status);

    default Page<MaintenanceRecord> findScheduledMaintenance(LocalDate today, Pageable pageable) {
        return findByStatusAndScheduledDateGreaterThanEqualOrderByScheduledDateAscIdAsc(
                MaintenanceStatus.SCHEDULED,
                today,
                pageable
        );
    }

    default List<MaintenanceRecord> findScheduledMaintenance(LocalDate today) {
        return findByStatusAndScheduledDateGreaterThanEqualOrderByScheduledDateAscIdAsc(
                MaintenanceStatus.SCHEDULED,
                today
        );
    }
}
