package com.assetvault.repository;

import com.assetvault.model.Asset;
import com.assetvault.model.Employee;
import com.assetvault.model.SoftwareLicense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Override
    Page<Employee> findAll(Pageable pageable);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByEmployeeCodeAndIdNot(String employeeCode, Long id);

    Page<Employee> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Employee> findAllByNameContainingIgnoreCase(String name);

    Page<Employee> findByDepartmentIgnoreCase(String department, Pageable pageable);

    List<Employee> findAllByDepartmentIgnoreCase(String department);

    Page<Employee> findByIsActive(boolean isActive, Pageable pageable);

    List<Employee> findAllByIsActive(boolean isActive);

    default Optional<Employee> findByEmail(String email) {
        return findByEmailIgnoreCase(email);
    }

    default boolean existsByEmail(String email) {
        return existsByEmailIgnoreCase(email);
    }

    default Page<Employee> searchByName(String name, Pageable pageable) {
        return findByNameContainingIgnoreCase(name, pageable);
    }

    default List<Employee> searchByName(String name) {
        return findAllByNameContainingIgnoreCase(name);
    }

    default Page<Employee> findByDepartment(String department, Pageable pageable) {
        return findByDepartmentIgnoreCase(department, pageable);
    }

    default List<Employee> findByDepartment(String department) {
        return findAllByDepartmentIgnoreCase(department);
    }

    @Query(
            value = """
                    SELECT
                        DISTINCT a
                    FROM AssetAssignment aa
                    JOIN aa.asset a
                    WHERE
                        aa.employee.id = :employeeId
                        AND
                        aa.status = com.assetvault.model.enums.AssignmentStatus.ACTIVE
                    ORDER BY
                        a.name ASC,
                        a.id ASC
                    """,
            countQuery = """
                    SELECT
                        COUNT(DISTINCT aa.asset.id)
                    FROM AssetAssignment aa
                    WHERE
                        aa.employee.id = :employeeId
                        AND
                        aa.status = com.assetvault.model.enums.AssignmentStatus.ACTIVE
                    """
    )
    Page<Asset> findActiveAssetsForEmployee(@Param("employeeId") Long employeeId, Pageable pageable);

    @Query("""
            SELECT
                DISTINCT a
            FROM AssetAssignment aa
            JOIN aa.asset a
            WHERE
                aa.employee.id = :employeeId
                AND
                aa.status = com.assetvault.model.enums.AssignmentStatus.ACTIVE
            ORDER BY
                a.name ASC,
                a.id ASC
            """)
    List<Asset> findActiveAssetsForEmployee(@Param("employeeId") Long employeeId);

    @Query(
            value = """
                    SELECT
                        DISTINCT sl
                    FROM SoftwareAssignment sa
                    JOIN sa.softwareLicense sl
                    WHERE
                        sa.employee.id = :employeeId
                        AND
                        sa.status = com.assetvault.model.enums.AssignmentStatus.ACTIVE
                    ORDER BY
                        sl.softwareName ASC,
                        sl.id ASC
                    """,
            countQuery = """
                    SELECT
                        COUNT(DISTINCT sa.softwareLicense.id)
                    FROM SoftwareAssignment sa
                    WHERE
                        sa.employee.id = :employeeId
                        AND
                        sa.status = com.assetvault.model.enums.AssignmentStatus.ACTIVE
                    """
    )
    Page<SoftwareLicense> findLicensesAssignedToEmployee(@Param("employeeId") Long employeeId, Pageable pageable);

    @Query("""
            SELECT
                DISTINCT sl
            FROM SoftwareAssignment sa
            JOIN sa.softwareLicense sl
            WHERE
                sa.employee.id = :employeeId
                AND
                sa.status = com.assetvault.model.enums.AssignmentStatus.ACTIVE
            ORDER BY
                sl.softwareName ASC,
                sl.id ASC
            """)
    List<SoftwareLicense> findLicensesAssignedToEmployee(@Param("employeeId") Long employeeId);
}
