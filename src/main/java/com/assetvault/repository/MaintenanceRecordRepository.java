package com.assetvault.repository;

import com.assetvault.model.enums.MaintenanceStatus;
import com.assetvault.model.enums.MaintenanceType;
import com.assetvault.model.MaintenanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Repository for maintenance record persistence and reporting operations.
 */
@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Integer> {
    /**
     * Returns the requested page of maintenance records.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    Page<MaintenanceRecord> findAll(Pageable pageable);

    /**
     * Executes the find by asset id operation.
     *
     * @param assetId the asset identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<MaintenanceRecord> findByAssetId(Long assetId, Pageable pageable);

    /**
     * Executes the find all by asset id order by scheduled date desc id desc operation.
     *
     * @param assetId the asset identifier
     * @return the matching results
     */
    List<MaintenanceRecord> findAllByAssetIdOrderByScheduledDateDescIdDesc(Long assetId);

    /**
     * Executes the find all by asset id and status order by scheduled date desc id desc operation.
     *
     * @param assetId the asset identifier
     * @param status the requested status value
     * @return the matching results
     */
    List<MaintenanceRecord> findAllByAssetIdAndStatusOrderByScheduledDateDescIdDesc(
            Long assetId,
            MaintenanceStatus status
    );

    /**
     * Returns maintenance records filtered by status.
     *
     * @param status the requested status value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<MaintenanceRecord> findByStatus(MaintenanceStatus status, Pageable pageable);

    /**
     * Returns maintenance records filtered by status.
     *
     * @param status the requested status value
     * @return the matching results
     */
    List<MaintenanceRecord> findAllByStatus(MaintenanceStatus status);

    /**
     * Executes the find by maintenance type operation.
     *
     * @param maintenanceType the maintenance type value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<MaintenanceRecord> findByMaintenanceType(MaintenanceType maintenanceType, Pageable pageable);

    /**
     * Executes the find all by maintenance type operation.
     *
     * @param maintenanceType the maintenance type value
     * @return the matching results
     */
    List<MaintenanceRecord> findAllByMaintenanceType(MaintenanceType maintenanceType);

    /**
     * Executes the find by status and scheduled date greater than equal order by scheduled date asc id asc operation.
     *
     * @param status the requested status value
     * @param today the window start date
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<MaintenanceRecord> findByStatusAndScheduledDateGreaterThanEqualOrderByScheduledDateAscIdAsc(
            MaintenanceStatus status,
            LocalDate today,
            Pageable pageable
    );

    /**
     * Executes the find by status and scheduled date greater than equal order by scheduled date asc id asc operation.
     *
     * @param status the requested status value
     * @param today the window start date
     * @return the matching results
     */
    List<MaintenanceRecord> findByStatusAndScheduledDateGreaterThanEqualOrderByScheduledDateAscIdAsc(
            MaintenanceStatus status,
            LocalDate today
    );

    /**
     * Executes the find by scheduled date between operation.
     *
     * @param from the window start date
     * @param to the window end date
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<MaintenanceRecord> findByScheduledDateBetween(LocalDate from, LocalDate to, Pageable pageable);

    /**
     * Executes the find all by scheduled date between order by scheduled date desc id desc operation.
     *
     * @param from the window start date
     * @param to the window end date
     * @return the matching results
     */
    List<MaintenanceRecord> findAllByScheduledDateBetweenOrderByScheduledDateDescIdDesc(LocalDate from, LocalDate to);

    /**
     * Counts maintenance records that match the supplied filter.
     *
     * @param status the requested status value
     * @return the computed count
     */
    long countByStatus(MaintenanceStatus status);

    /**
     * Checks whether a matching maintenance record exists.
     *
     * @param assetId the asset identifier
     * @param statuses the statuses value
     * @return true when a matching record exists; otherwise false
     */
    boolean existsByAssetIdAndStatusIn(Long assetId, Collection<MaintenanceStatus> statuses);

    /**
     * Checks whether a matching maintenance record exists.
     *
     * @param assetId the asset identifier
     * @param statuses the statuses value
     * @param id the database identifier
     * @return true when a matching record exists; otherwise false
     */
    boolean existsByAssetIdAndStatusInAndIdNot(
            Long assetId,
            Collection<MaintenanceStatus> statuses,
            Integer id
    );

    /**
     * Executes the find scheduled maintenance operation.
     *
     * @param today the window start date
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    default Page<MaintenanceRecord> findScheduledMaintenance(LocalDate today, Pageable pageable) {
        return findByStatusAndScheduledDateGreaterThanEqualOrderByScheduledDateAscIdAsc(
                MaintenanceStatus.SCHEDULED,
                today,
                pageable
        );
    }

    /**
     * Executes the find scheduled maintenance operation.
     *
     * @param today the window start date
     * @return the matching results
     */
    default List<MaintenanceRecord> findScheduledMaintenance(LocalDate today) {
        return findByStatusAndScheduledDateGreaterThanEqualOrderByScheduledDateAscIdAsc(
                MaintenanceStatus.SCHEDULED,
                today
        );
    }
}
