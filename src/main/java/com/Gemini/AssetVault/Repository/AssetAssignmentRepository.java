package com.Gemini.AssetVault.Repository;

import com.Gemini.AssetVault.Model.AssetAssignment;
import com.Gemini.AssetVault.Model.Enum.AssignmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {
    @Override
    Page<AssetAssignment> findAll(Pageable pageable);

    Page<AssetAssignment> findByStatus(AssignmentStatus status, Pageable pageable);

    List<AssetAssignment> findAllByStatus(AssignmentStatus status);

    Page<AssetAssignment> findByAssetId(Long assetId, Pageable pageable);

    List<AssetAssignment> findAllByAssetIdOrderByAssignedDateDescIdDesc(Long assetId);

    Page<AssetAssignment> findByEmployeeId(Long employeeId, Pageable pageable);

    List<AssetAssignment> findAllByEmployeeIdOrderByAssignedDateDescIdDesc(Long employeeId);

    Page<AssetAssignment> findByAssignedDateBetween(LocalDate from, LocalDate to, Pageable pageable);

    List<AssetAssignment> findAllByAssignedDateBetweenOrderByAssignedDateDescIdDesc(LocalDate from, LocalDate to);

    boolean existsByAssetIdAndStatus(Long assetId, AssignmentStatus status);

    boolean existsByEmployeeIdAndStatus(Long employeeId, AssignmentStatus status);

    Optional<AssetAssignment> findByAssetIdAndStatus(Long assetId, AssignmentStatus status);

    Optional<AssetAssignment> findByEmployeeIdAndAssetIdAndStatus(
            Long employeeId,
            Long assetId,
            AssignmentStatus status
    );

    long countByStatus(AssignmentStatus status);
}
