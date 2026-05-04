package com.Gemini.AssetVault.Repository;

import com.Gemini.AssetVault.Model.SoftwareLicense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SoftwareLicenseRepository extends JpaRepository<SoftwareLicense, Long> {
    @Override
    Page<SoftwareLicense> findAll(Pageable pageable);

    Optional<SoftwareLicense> findByLicenceKey(String licenceKey);

    boolean existsByLicenceKey(String licenceKey);
    boolean existsByLicenceKeyAndIdNot(String licenceKey, Long id);

    default Optional<SoftwareLicense> findByLicenseKey(String licenseKey) {
        return findByLicenceKey(licenseKey);
    }

    default boolean existsByLicenseKey(String licenseKey) {
        return existsByLicenceKey(licenseKey);
    }

    Page<SoftwareLicense> findByIsActive(Boolean isActive, Pageable pageable);

    List<SoftwareLicense> findAllByIsActive(Boolean isActive);

    Page<SoftwareLicense> findBySoftwareNameContainingIgnoreCaseOrderBySoftwareNameAscIdAsc(
            String name,
            Pageable pageable
    );

    List<SoftwareLicense> findBySoftwareNameContainingIgnoreCaseOrderBySoftwareNameAscIdAsc(String name);

    Page<SoftwareLicense> findByExpiryDateBetweenOrderByExpiryDateAscIdAsc(
            LocalDate today,
            LocalDate threshold,
            Pageable pageable
    );

    List<SoftwareLicense> findByExpiryDateBetweenOrderByExpiryDateAscIdAsc(LocalDate today, LocalDate threshold);

    Page<SoftwareLicense> findByExpiryDateBeforeOrderByExpiryDateDescIdDesc(LocalDate today, Pageable pageable);

    List<SoftwareLicense> findByExpiryDateBeforeOrderByExpiryDateDescIdDesc(LocalDate today);

    default Page<SoftwareLicense> searchBySoftwareName(String name, Pageable pageable) {
        return findBySoftwareNameContainingIgnoreCaseOrderBySoftwareNameAscIdAsc(name, pageable);
    }

    default List<SoftwareLicense> searchBySoftwareName(String name) {
        return findBySoftwareNameContainingIgnoreCaseOrderBySoftwareNameAscIdAsc(name);
    }

    default Page<SoftwareLicense> findLicensesExpiringSoon(
            LocalDate today,
            LocalDate threshold,
            Pageable pageable
    ) {
        return findByExpiryDateBetweenOrderByExpiryDateAscIdAsc(today, threshold, pageable);
    }

    default List<SoftwareLicense> findLicensesExpiringSoon(LocalDate today, LocalDate threshold) {
        return findByExpiryDateBetweenOrderByExpiryDateAscIdAsc(today, threshold);
    }

    default Page<SoftwareLicense> findExpiredLicenses(LocalDate today, Pageable pageable) {
        return findByExpiryDateBeforeOrderByExpiryDateDescIdDesc(today, pageable);
    }

    default List<SoftwareLicense> findExpiredLicenses(LocalDate today) {
        return findByExpiryDateBeforeOrderByExpiryDateDescIdDesc(today);
    }

    @Query(
            value = """
                    SELECT
                        sl
                    FROM SoftwareLicense sl
                    WHERE
                        sl.usedSeats >= sl.totalSeats
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
                    """
    )
    Page<SoftwareLicense> findLicensesWithNoRemainingSeats(Pageable pageable);

    @Query("""
            SELECT
                sl
            FROM SoftwareLicense sl
            WHERE
                sl.usedSeats >= sl.totalSeats
            ORDER BY
                sl.softwareName ASC,
                sl.id ASC
            """)
    List<SoftwareLicense> findLicensesWithNoRemainingSeats();
}
