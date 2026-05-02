package com.Gemini.AssetVault.Repository;

import com.Gemini.AssetVault.Model.Asset;
import com.Gemini.AssetVault.Model.Enum.AssetStatus;
import com.Gemini.AssetVault.Model.Enum.AssetType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    Page<Asset> findByType(AssetType type, Pageable pageable); //get
    List<Asset> findAllByType(AssetType type); //get

    Page<Asset> findByStatus(AssetStatus status, Pageable pageable); //get //get
    List<Asset> findAllByStatus(AssetStatus status); //get

    @Query("""
            SELECT
                DISTINCT a
            FROM Asset a
            JOIN a.assignments aa
                ON aa.status = com.Gemini.AssetVault.Model.Enum.AssignmentStatus.ACTIVE
            JOIN aa.employee e
                ON LOWER(e.department) = LOWER(:department)
            """)
    Page<Asset> findAssetsByDepartment(@Param("department") String department, Pageable pageable);
    @Query("""
            SELECT
                DISTINCT a
            FROM Asset a
            JOIN a.assignments aa
                ON aa.status = com.Gemini.AssetVault.Model.Enum.AssignmentStatus.ACTIVE
            JOIN aa.employee e
                ON LOWER(e.department) = LOWER(:department)
            """)
    List<Asset> findAssetsByDepartment(@Param("department") String department);

    @Query("""
            SELECT
                a
            FROM Asset a
            WHERE
                a.warrantyExpiryDate IS NOT NULL
                AND
                a.warrantyExpiryDate >= :today
                AND
                a.warrantyExpiryDate <= :threshold
            ORDER BY
                a.warrantyExpiryDate ASC
            """)
    Page<Asset> findAssetsWithWarrantyExpiring(
            @Param("today") LocalDate today,
            @Param("threshold") LocalDate threshold,
            Pageable pageable
            );
    @Query("""
            SELECT
                a
            FROM Asset a
            WHERE
                a.warrantyExpiryDate IS NOT NULL
                AND
                a.warrantyExpiryDate >= :today
                AND
                a.warrantyExpiryDate <= :threshold
            ORDER BY
                a.warrantyExpiryDate ASC
            """)
    List<Asset> findAssetsWithWarrantyExpiring(
            @Param("today") LocalDate today,
            @Param("threshold") LocalDate threshold
    );

    @Query("""
            SELECT a
            FROM Asset a
            WHERE
                a.warrantyExpiryDate IS NOT NULL
                AND
                a.warrantyExpiryDate < :today
            ORDER BY
                a.warrantyExpiryDate DESC
            """)
    Page<Asset> findAssetsWithExpiredWarranty(@Param("today") LocalDate today, Pageable pageable);
    @Query("""
            SELECT a
            FROM Asset a
            WHERE
                a.warrantyExpiryDate IS NOT NULL
                AND
                a.warrantyExpiryDate < :today
            ORDER BY
                a.warrantyExpiryDate DESC
            """)
    List<Asset> findAssetsWithExpiredWarranty(@Param("today") LocalDate today);

    @Query(
            value = """
                    SELECT
                        a
                    FROM Asset a
                    WHERE
                        LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR
                        LOWER(a.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR
                        LOWER(a.model) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    """,
            countQuery = """
                    SELECT
                        COUNT(a)
                    FROM Asset a
                    WHERE
                        LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR
                        LOWER(a.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR
                        LOWER(a.model) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    """
    )
    Page<Asset> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    @Query(
            value = """
                    SELECT
                        a
                    FROM Asset a
                    WHERE
                        LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR
                        LOWER(a.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR
                        LOWER(a.model) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    """,
            countQuery = """
                    SELECT
                        COUNT(a)
                    FROM Asset a
                    WHERE
                        LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR
                        LOWER(a.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR
                        LOWER(a.model) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    """
    )
    List<Asset> searchByKeyword(@Param("keyword") String keyword);


}
