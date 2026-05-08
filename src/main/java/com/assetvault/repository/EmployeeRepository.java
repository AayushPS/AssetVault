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

/**
 * Repository for employee persistence and reporting operations.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    /**
     * Returns the requested page of employees.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    @Override
    Page<Employee> findAll(Pageable pageable);

    /**
     * Executes the find by employee code operation.
     *
     * @param employeeCode the employee code value
     * @return the matching value when one exists
     */
    Optional<Employee> findByEmployeeCode(String employeeCode);

    /**
     * Executes the find by email ignore case operation.
     *
     * @param email the email value
     * @return the matching value when one exists
     */
    Optional<Employee> findByEmailIgnoreCase(String email);

    /**
     * Checks whether a matching employee exists.
     *
     * @param email the email value
     * @return true when a matching record exists; otherwise false
     */
    boolean existsByEmailIgnoreCase(String email);
    /**
     * Checks whether a matching employee exists.
     *
     * @param email the email value
     * @param id the database identifier
     * @return true when a matching record exists; otherwise false
     */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    /**
     * Checks whether a matching employee exists.
     *
     * @param employeeCode the employee code value
     * @return true when a matching record exists; otherwise false
     */
    boolean existsByEmployeeCode(String employeeCode);
    /**
     * Checks whether a matching employee exists.
     *
     * @param employeeCode the employee code value
     * @param id the database identifier
     * @return true when a matching record exists; otherwise false
     */
    boolean existsByEmployeeCodeAndIdNot(String employeeCode, Long id);

    /**
     * Executes the find by name containing ignore case operation.
     *
     * @param name the name or label value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<Employee> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Executes the find all by name containing ignore case operation.
     *
     * @param name the name or label value
     * @return the matching results
     */
    List<Employee> findAllByNameContainingIgnoreCase(String name);

    /**
     * Executes the find by department ignore case operation.
     *
     * @param department the department name
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<Employee> findByDepartmentIgnoreCase(String department, Pageable pageable);

    /**
     * Executes the find all by department ignore case operation.
     *
     * @param department the department name
     * @return the matching results
     */
    List<Employee> findAllByDepartmentIgnoreCase(String department);

    /**
     * Executes the find by is active operation.
     *
     * @param isActive the is active value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<Employee> findByIsActive(boolean isActive, Pageable pageable);

    /**
     * Executes the find all by is active operation.
     *
     * @param isActive the is active value
     * @return the matching results
     */
    List<Employee> findAllByIsActive(boolean isActive);

    /**
     * Executes the find by email operation.
     *
     * @param email the email value
     * @return the matching value when one exists
     */
    default Optional<Employee> findByEmail(String email) {
        return findByEmailIgnoreCase(email);
    }

    /**
     * Checks whether a matching employee exists.
     *
     * @param email the email value
     * @return true when a matching record exists; otherwise false
     */
    default boolean existsByEmail(String email) {
        return existsByEmailIgnoreCase(email);
    }

    /**
     * Executes the search by name operation.
     *
     * @param name the name or label value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    default Page<Employee> searchByName(String name, Pageable pageable) {
        return findByNameContainingIgnoreCase(name, pageable);
    }

    /**
     * Executes the search by name operation.
     *
     * @param name the name or label value
     * @return the matching results
     */
    default List<Employee> searchByName(String name) {
        return findAllByNameContainingIgnoreCase(name);
    }

    /**
     * Executes the find by department operation.
     *
     * @param department the department name
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    default Page<Employee> findByDepartment(String department, Pageable pageable) {
        return findByDepartmentIgnoreCase(department, pageable);
    }

    /**
     * Executes the find by department operation.
     *
     * @param department the department name
     * @return the matching results
     */
    default List<Employee> findByDepartment(String department) {
        return findAllByDepartmentIgnoreCase(department);
    }

    /**
     * Executes the find active assets for employee operation.
     *
     * @param employeeId the employee identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
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

    /**
     * Executes the find active assets for employee operation.
     *
     * @param employeeId the employee identifier
     * @return the matching results
     */
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

    /**
     * Executes the find licenses assigned to employee operation.
     *
     * @param employeeId the employee identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
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

    /**
     * Executes the find licenses assigned to employee operation.
     *
     * @param employeeId the employee identifier
     * @return the matching results
     */
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
