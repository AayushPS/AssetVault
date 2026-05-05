package com.assetvault.repository;

import com.assetvault.model.Asset;
import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.model.enums.AssetStatus;
import com.assetvault.model.enums.AssetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface AssetRepository extends JpaRepository<Asset,Long> {
    @Override
    Page<Asset> findAll(Pageable pageable); //get

    Optional<Asset> findByAssetCode(String assetCode); //get
    boolean existsByAssetCode(String assetCode); //exception
    boolean existsBySerialNumber(String serialNumber); //exception
    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);

    Page<Asset> findByType(AssetType type, Pageable pageable); //get
    Page<Asset> findAllByType(AssetType type, Pageable pageable); //get
    List<Asset> findAllByType(AssetType type); //get

    Page<Asset> findByStatus(AssetStatus status, Pageable pageable); //get //get
    Page<Asset> findAllByStatus(AssetStatus status, Pageable pageable); //get
    List<Asset> findAllByStatus(AssetStatus status); //get

    Page<Asset> findDistinctByAssignmentsStatusAndAssignmentsEmployeeDepartmentIgnoreCase(
            AssignmentStatus status,
            String department,
            Pageable pageable
    );

    List<Asset> findDistinctByAssignmentsStatusAndAssignmentsEmployeeDepartmentIgnoreCase(
            AssignmentStatus status,
            String department
    );

    Page<Asset> findByWarrantyExpiryDateBetweenOrderByWarrantyExpiryDateAsc(
            LocalDate today,
            LocalDate threshold,
            Pageable pageable
    );

    List<Asset> findByWarrantyExpiryDateBetweenOrderByWarrantyExpiryDateAsc(
            LocalDate today,
            LocalDate threshold
    );

    Page<Asset> findByWarrantyExpiryDateBeforeOrderByWarrantyExpiryDateDesc(LocalDate today, Pageable pageable);

    List<Asset> findByWarrantyExpiryDateBeforeOrderByWarrantyExpiryDateDesc(LocalDate today);

    Page<Asset> findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
            String nameKeyword,
            String brandKeyword,
            String modelKeyword,
            Pageable pageable
    );

    List<Asset> findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
            String nameKeyword,
            String brandKeyword,
            String modelKeyword
    );

    default Page<Asset> findAssetsByDepartment(String department, Pageable pageable) {
        return findDistinctByAssignmentsStatusAndAssignmentsEmployeeDepartmentIgnoreCase(
                AssignmentStatus.ACTIVE,
                department,
                pageable
        );
    }

    default List<Asset> findAssetsByDepartment(String department) {
        return findDistinctByAssignmentsStatusAndAssignmentsEmployeeDepartmentIgnoreCase(
                AssignmentStatus.ACTIVE,
                department
        );
    }

    default Page<Asset> findAssetsWithWarrantyExpiring(
            LocalDate today,
            LocalDate threshold,
            Pageable pageable
    ) {
        return findByWarrantyExpiryDateBetweenOrderByWarrantyExpiryDateAsc(today, threshold, pageable);
    }

    default List<Asset> findAssetsWithWarrantyExpiring(LocalDate today, LocalDate threshold) {
        return findByWarrantyExpiryDateBetweenOrderByWarrantyExpiryDateAsc(today, threshold);
    }

    default Page<Asset> findAssetsWithExpiredWarranty(LocalDate today, Pageable pageable) {
        return findByWarrantyExpiryDateBeforeOrderByWarrantyExpiryDateDesc(today, pageable);
    }

    default List<Asset> findAssetsWithExpiredWarranty(LocalDate today) {
        return findByWarrantyExpiryDateBeforeOrderByWarrantyExpiryDateDesc(today);
    }

    default Page<Asset> searchByKeyword(String keyword, Pageable pageable) {
        return findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
                keyword,
                keyword,
                keyword,
                pageable
        );
    }

    default List<Asset> searchByKeyword(String keyword) {
        return findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
                keyword,
                keyword,
                keyword
        );
    }

    Page<Asset> findByPurchaseCostLessThanEqual(BigDecimal maxCost, Pageable pageable);

    long countByStatus(AssetStatus status);
    long countByType(AssetType type);

    @Query("""
            SELECT
                a.type,
                COUNT(a)
            FROM Asset a
            GROUP BY
                a.type
            """)
    List<Object[]> countGroupedByType();
    @Query(
            value = """
                    SELECT
                        a.type,
                        COUNT(a)
                    FROM Asset a
                    GROUP BY
                        a.type
                    ORDER BY
                        a.type ASC
                    """,
            countQuery = """
                    SELECT
                        COUNT(DISTINCT a.type)
                    FROM Asset a
                    """
    )
    Page<Object[]> countGroupedByType(Pageable pageable);

    @Query("""
            SELECT
                e.department,
                COUNT(DISTINCT a.id),
                COALESCE(SUM(a.purchaseCost), 0)
            FROM Asset a
            JOIN a.assignments aa
                ON aa.status = com.assetvault.model.enums.AssignmentStatus.ACTIVE
            JOIN aa.employee e
            GROUP BY
                e.department
            ORDER BY
                e.department ASC
            """)
    List<Object[]> getDepartmentAssetSummary();
    @Query(
            value = """
                    SELECT
                        e.department,
                        COUNT(DISTINCT a.id),
                        COALESCE(SUM(a.purchaseCost), 0)
                    FROM Asset a
                    JOIN a.assignments aa
                        ON aa.status = com.assetvault.model.enums.AssignmentStatus.ACTIVE
                    JOIN aa.employee e
                    GROUP BY
                        e.department
                    ORDER BY
                        e.department ASC
                    """,
            countQuery = """
                    SELECT
                        COUNT(DISTINCT e.department)
                    FROM Asset a
                    JOIN a.assignments aa
                        ON aa.status = com.assetvault.model.enums.AssignmentStatus.ACTIVE
                    JOIN aa.employee e
                    """
    )
    Page<Object[]> getDepartmentAssetSummary(Pageable pageable);

    @Query("""
            SELECT
                COALESCE(SUM(a.purchaseCost), 0)
            FROM Asset a
            WHERE
                a.status NOT IN (
                    com.assetvault.model.enums.AssetStatus.RETIRED,
                    com.assetvault.model.enums.AssetStatus.LOST
                )
            """)
    Optional<BigDecimal> getTotalActiveAssetValue();


}
