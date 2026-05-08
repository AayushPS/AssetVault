package com.assetvault.repository;

import com.assetvault.model.SoftwareLicense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for software license persistence and reporting operations.
 */
@Repository
public interface SoftwareLicenseRepository extends JpaRepository<SoftwareLicense, Long> {
    /**
     * Returns the requested page of software licenses.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    Page<SoftwareLicense> findAll(Pageable pageable);

    /**
     * Executes the find by licence key operation.
     *
     * @param licenceKey the licence key value
     * @return the matching value when one exists
     */
    Optional<SoftwareLicense> findByLicenceKey(String licenceKey);

    /**
     * Executes the find by id for update operation.
     *
     * @param id the database identifier
     * @return the matching value when one exists
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sl FROM SoftwareLicense sl WHERE sl.id = :id")
    Optional<SoftwareLicense> findByIdForUpdate(@Param("id") Long id);

    /**
     * Checks whether a matching software license exists.
     *
     * @param licenceKey the licence key value
     * @return true when a matching record exists; otherwise false
     */
    boolean existsByLicenceKey(String licenceKey);
    /**
     * Checks whether a matching software license exists.
     *
     * @param licenceKey the licence key value
     * @param id the database identifier
     * @return true when a matching record exists; otherwise false
     */
    boolean existsByLicenceKeyAndIdNot(String licenceKey, Long id);

    /**
     * Executes the find by license key operation.
     *
     * @param licenseKey the license key value
     * @return the matching value when one exists
     */
    default Optional<SoftwareLicense> findByLicenseKey(String licenseKey) {
        return findByLicenceKey(licenseKey);
    }

    /**
     * Checks whether a matching software license exists.
     *
     * @param licenseKey the license key value
     * @return true when a matching record exists; otherwise false
     */
    default boolean existsByLicenseKey(String licenseKey) {
        return existsByLicenceKey(licenseKey);
    }

    /**
     * Executes the find by is active operation.
     *
     * @param isActive the is active value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<SoftwareLicense> findByIsActive(Boolean isActive, Pageable pageable);

    /**
     * Executes the find all by is active operation.
     *
     * @param isActive the is active value
     * @return the matching results
     */
    List<SoftwareLicense> findAllByIsActive(Boolean isActive);

    /**
     * Executes the find by software name containing ignore case order by software name asc id asc operation.
     *
     * @param name the name or label value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<SoftwareLicense> findBySoftwareNameContainingIgnoreCaseOrderBySoftwareNameAscIdAsc(
            String name,
            Pageable pageable
    );

    /**
     * Executes the find by software name containing ignore case order by software name asc id asc operation.
     *
     * @param name the name or label value
     * @return the matching results
     */
    List<SoftwareLicense> findBySoftwareNameContainingIgnoreCaseOrderBySoftwareNameAscIdAsc(String name);

    /**
     * Executes the find by expiry date between order by expiry date asc id asc operation.
     *
     * @param today the window start date
     * @param threshold the window end date
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<SoftwareLicense> findByExpiryDateBetweenOrderByExpiryDateAscIdAsc(
            LocalDate today,
            LocalDate threshold,
            Pageable pageable
    );

    /**
     * Executes the find by expiry date between order by expiry date asc id asc operation.
     *
     * @param today the window start date
     * @param threshold the window end date
     * @return the matching results
     */
    List<SoftwareLicense> findByExpiryDateBetweenOrderByExpiryDateAscIdAsc(LocalDate today, LocalDate threshold);

    /**
     * Executes the find by expiry date before order by expiry date desc id desc operation.
     *
     * @param today the window start date
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<SoftwareLicense> findByExpiryDateBeforeOrderByExpiryDateDescIdDesc(LocalDate today, Pageable pageable);

    /**
     * Executes the find by expiry date before order by expiry date desc id desc operation.
     *
     * @param today the window start date
     * @return the matching results
     */
    List<SoftwareLicense> findByExpiryDateBeforeOrderByExpiryDateDescIdDesc(LocalDate today);

    /**
     * Executes the search by software name operation.
     *
     * @param name the name or label value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    default Page<SoftwareLicense> searchBySoftwareName(String name, Pageable pageable) {
        return findBySoftwareNameContainingIgnoreCaseOrderBySoftwareNameAscIdAsc(name, pageable);
    }

    /**
     * Executes the search by software name operation.
     *
     * @param name the name or label value
     * @return the matching results
     */
    default List<SoftwareLicense> searchBySoftwareName(String name) {
        return findBySoftwareNameContainingIgnoreCaseOrderBySoftwareNameAscIdAsc(name);
    }

    /**
     * Executes the find licenses expiring soon operation.
     *
     * @param today the window start date
     * @param threshold the window end date
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    default Page<SoftwareLicense> findLicensesExpiringSoon(
            LocalDate today,
            LocalDate threshold,
            Pageable pageable
    ) {
        return findByExpiryDateBetweenOrderByExpiryDateAscIdAsc(today, threshold, pageable);
    }

    /**
     * Executes the find licenses expiring soon operation.
     *
     * @param today the window start date
     * @param threshold the window end date
     * @return the matching results
     */
    default List<SoftwareLicense> findLicensesExpiringSoon(LocalDate today, LocalDate threshold) {
        return findByExpiryDateBetweenOrderByExpiryDateAscIdAsc(today, threshold);
    }

    /**
     * Executes the find expired licenses operation.
     *
     * @param today the window start date
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    default Page<SoftwareLicense> findExpiredLicenses(LocalDate today, Pageable pageable) {
        return findByExpiryDateBeforeOrderByExpiryDateDescIdDesc(today, pageable);
    }

    /**
     * Executes the find expired licenses operation.
     *
     * @param today the window start date
     * @return the matching results
     */
    default List<SoftwareLicense> findExpiredLicenses(LocalDate today) {
        return findByExpiryDateBeforeOrderByExpiryDateDescIdDesc(today);
    }

    /**
     * Executes the find licenses with no remaining seats operation.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Query(
            value = """
                    SELECT
                        sl
                    FROM SoftwareLicense sl
                    WHERE
                        sl.usedSeats >= sl.totalSeats
                        AND sl.licenseType <> com.assetvault.model.enums.LicenseType.INDIVIDUAL
                    ORDER BY
                        sl.softwareName ASC,
                        sl.id ASC
                    """,
            countQuery = """
                    SELECT
                        COUNT(sl)
                    FROM SoftwareLicense sl
                    WHERE
                        sl.usedSeats >= sl.totalSeats
                        AND sl.licenseType <> com.assetvault.model.enums.LicenseType.INDIVIDUAL
                    """
    )
    Page<SoftwareLicense> findLicensesWithNoRemainingSeats(Pageable pageable);

    /**
     * Executes the find licenses with no remaining seats operation.
     *
     * @return the matching results
     */
    @Query("""
            SELECT
                sl
            FROM SoftwareLicense sl
            WHERE
                sl.usedSeats >= sl.totalSeats
                AND sl.licenseType <> com.assetvault.model.enums.LicenseType.INDIVIDUAL
            ORDER BY
                sl.softwareName ASC,
                sl.id ASC
            """)
    List<SoftwareLicense> findLicensesWithNoRemainingSeats();
}
