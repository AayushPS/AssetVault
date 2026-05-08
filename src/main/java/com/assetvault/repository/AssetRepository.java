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


/**
 * Repository for asset persistence and reporting operations.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset,Long> {
    /**
     * Returns the requested page of assets.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    Page<Asset> findAll(Pageable pageable);

    /**
     * Returns the asset identified by the supplied code.
     *
     * @param assetCode the generated asset code
     * @return the matching value when one exists
     */
    Optional<Asset> findByAssetCode(String assetCode);
    /**
     * Checks whether a matching asset exists.
     *
     * @param assetCode the generated asset code
     * @return true when a matching record exists; otherwise false
     */
    boolean existsByAssetCode(String assetCode);
    /**
     * Checks whether a matching asset exists.
     *
     * @param serialNumber the serial number value
     * @return true when a matching record exists; otherwise false
     */
    boolean existsBySerialNumber(String serialNumber);
    /**
     * Checks whether a matching asset exists.
     *
     * @param serialNumber the serial number value
     * @param id the database identifier
     * @return true when a matching record exists; otherwise false
     */
    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);

    /**
     * Returns assets filtered by type.
     *
     * @param type the requested type value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<Asset> findByType(AssetType type, Pageable pageable);
    /**
     * Returns assets filtered by type.
     *
     * @param type the requested type value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<Asset> findAllByType(AssetType type, Pageable pageable);
    /**
     * Returns assets filtered by type.
     *
     * @param type the requested type value
     * @return the matching results
     */
    List<Asset> findAllByType(AssetType type);

    /**
     * Returns assets filtered by status.
     *
     * @param status the requested status value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<Asset> findByStatus(AssetStatus status, Pageable pageable);
    /**
     * Returns assets filtered by status.
     *
     * @param status the requested status value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<Asset> findAllByStatus(AssetStatus status, Pageable pageable);
    /**
     * Returns assets filtered by status.
     *
     * @param status the requested status value
     * @return the matching results
     */
    List<Asset> findAllByStatus(AssetStatus status);

    /**
     * Executes the find distinct by assignments status and assignments employee department ignore case operation.
     *
     * @param status the requested status value
     * @param department the department name
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<Asset> findDistinctByAssignmentsStatusAndAssignmentsEmployeeDepartmentIgnoreCase(
            AssignmentStatus status,
            String department,
            Pageable pageable
    );

    /**
     * Executes the find distinct by assignments status and assignments employee department ignore case operation.
     *
     * @param status the requested status value
     * @param department the department name
     * @return the matching results
     */
    List<Asset> findDistinctByAssignmentsStatusAndAssignmentsEmployeeDepartmentIgnoreCase(
            AssignmentStatus status,
            String department
    );

    /**
     * Returns assets whose warranty expires inside the requested date window.
     *
     * @param today the window start date
     * @param threshold the window end date
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<Asset> findByWarrantyExpiryDateBetweenOrderByWarrantyExpiryDateAsc(
            LocalDate today,
            LocalDate threshold,
            Pageable pageable
    );

    /**
     * Returns assets whose warranty expires inside the requested date window.
     *
     * @param today the window start date
     * @param threshold the window end date
     * @return the matching results
     */
    List<Asset> findByWarrantyExpiryDateBetweenOrderByWarrantyExpiryDateAsc(
            LocalDate today,
            LocalDate threshold
    );

    /**
     * Returns assets whose warranty has already expired.
     *
     * @param today the window start date
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<Asset> findByWarrantyExpiryDateBeforeOrderByWarrantyExpiryDateDesc(LocalDate today, Pageable pageable);

    /**
     * Returns assets whose warranty has already expired.
     *
     * @param today the window start date
     * @return the matching results
     */
    List<Asset> findByWarrantyExpiryDateBeforeOrderByWarrantyExpiryDateDesc(LocalDate today);

    /**
     * Searches assets using the supplied keyword.
     *
     * @param nameKeyword the name keyword value
     * @param brandKeyword the brand keyword value
     * @param modelKeyword the model keyword value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<Asset> findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
            String nameKeyword,
            String brandKeyword,
            String modelKeyword,
            Pageable pageable
    );

    /**
     * Searches assets using the supplied keyword.
     *
     * @param nameKeyword the name keyword value
     * @param brandKeyword the brand keyword value
     * @param modelKeyword the model keyword value
     * @return the matching results
     */
    List<Asset> findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
            String nameKeyword,
            String brandKeyword,
            String modelKeyword
    );

    /**
     * Returns assets associated with the supplied department.
     *
     * @param department the department name
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    default Page<Asset> findAssetsByDepartment(String department, Pageable pageable) {
        return findDistinctByAssignmentsStatusAndAssignmentsEmployeeDepartmentIgnoreCase(
                AssignmentStatus.ACTIVE,
                department,
                pageable
        );
    }

    /**
     * Returns assets associated with the supplied department.
     *
     * @param department the department name
     * @return the matching results
     */
    default List<Asset> findAssetsByDepartment(String department) {
        return findDistinctByAssignmentsStatusAndAssignmentsEmployeeDepartmentIgnoreCase(
                AssignmentStatus.ACTIVE,
                department
        );
    }

    /**
     * Returns assets whose warranty expires inside the requested date window.
     *
     * @param today the window start date
     * @param threshold the window end date
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    default Page<Asset> findAssetsWithWarrantyExpiring(
            LocalDate today,
            LocalDate threshold,
            Pageable pageable
    ) {
        return findByWarrantyExpiryDateBetweenOrderByWarrantyExpiryDateAsc(today, threshold, pageable);
    }

    /**
     * Returns assets whose warranty expires inside the requested date window.
     *
     * @param today the window start date
     * @param threshold the window end date
     * @return the matching results
     */
    default List<Asset> findAssetsWithWarrantyExpiring(LocalDate today, LocalDate threshold) {
        return findByWarrantyExpiryDateBetweenOrderByWarrantyExpiryDateAsc(today, threshold);
    }

    /**
     * Returns assets whose warranty has already expired.
     *
     * @param today the window start date
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    default Page<Asset> findAssetsWithExpiredWarranty(LocalDate today, Pageable pageable) {
        return findByWarrantyExpiryDateBeforeOrderByWarrantyExpiryDateDesc(today, pageable);
    }

    /**
     * Returns assets whose warranty has already expired.
     *
     * @param today the window start date
     * @return the matching results
     */
    default List<Asset> findAssetsWithExpiredWarranty(LocalDate today) {
        return findByWarrantyExpiryDateBeforeOrderByWarrantyExpiryDateDesc(today);
    }

    /**
     * Searches assets using the supplied keyword.
     *
     * @param keyword the search keyword
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    default Page<Asset> searchByKeyword(String keyword, Pageable pageable) {
        return findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
                keyword,
                keyword,
                keyword,
                pageable
        );
    }

    /**
     * Searches assets using the supplied keyword.
     *
     * @param keyword the search keyword
     * @return the matching results
     */
    default List<Asset> searchByKeyword(String keyword) {
        return findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
                keyword,
                keyword,
                keyword
        );
    }

    /**
     * Returns assets at or below the supplied maximum cost.
     *
     * @param maxCost the maximum accepted purchase cost
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<Asset> findByPurchaseCostLessThanEqual(BigDecimal maxCost, Pageable pageable);

    /**
     * Counts assets that match the supplied filter.
     *
     * @param status the requested status value
     * @return the computed count
     */
    long countByStatus(AssetStatus status);
    /**
     * Counts assets that match the supplied filter.
     *
     * @param type the requested type value
     * @return the computed count
     */
    long countByType(AssetType type);

    /**
     * Returns aggregated assets grouped by type.
     *
     * @return the computed count
     */
    @Query("""
            SELECT
                a.type,
                COUNT(a)
            FROM Asset a
            GROUP BY
                a.type
            """)
    List<Object[]> countGroupedByType();
    /**
     * Returns aggregated assets grouped by type.
     *
     * @param pageable the pagination and sorting information
     * @return the computed count
     */
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

    /**
     * Returns aggregated asset metrics grouped by department.
     *
     * @return the matching results
     */
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
    /**
     * Returns aggregated asset metrics grouped by department.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
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

    /**
     * Returns the aggregated value of active assets.
     *
     * @return the matching value when one exists
     */
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
