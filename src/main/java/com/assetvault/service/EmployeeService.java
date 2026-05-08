package com.assetvault.service;

import com.assetvault.dto.AssetAssignmentResponse;
import com.assetvault.dto.AssetResponse;
import com.assetvault.dto.EmployeeRequest;
import com.assetvault.dto.EmployeeResponse;
import com.assetvault.dto.SoftwareLicenseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service contract for employee operations.
 */
public interface EmployeeService {
    /**
     * Creates a new employee.
     *
     * @param request the request payload
     * @return the resulting employee
     */
    EmployeeResponse create(EmployeeRequest request);

    /**
     * Returns the requested page of employees.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<EmployeeResponse> getAll(Pageable pageable);

    /**
     * Returns the employee identified by the given id.
     *
     * @param id the database identifier
     * @return the resulting employee
     */
    EmployeeResponse getById(Long id);

    /**
     * Executes the search by name operation.
     *
     * @param name the name or label value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<EmployeeResponse> searchByName(String name, Pageable pageable);

    /**
     * Returns employees associated with the supplied department.
     *
     * @param name the name or label value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<EmployeeResponse> getByDepartment(String name, Pageable pageable);

    /**
     * Executes the get assigned assets operation.
     *
     * @param id the database identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetResponse> getAssignedAssets(Long id, Pageable pageable);

    /**
     * Executes the get assigned licenses operation.
     *
     * @param id the database identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<SoftwareLicenseResponse> getAssignedLicenses(Long id, Pageable pageable);

    /**
     * Executes the get assignment history operation.
     *
     * @param id the database identifier
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<AssetAssignmentResponse> getAssignmentHistory(Long id, Pageable pageable);

    /**
     * Updates an existing employee.
     *
     * @param id the database identifier
     * @param request the request payload
     * @return the resulting employee
     */
    EmployeeResponse update(Long id, EmployeeRequest request);

    /**
     * Deactivates the employee.
     *
     * @param id the database identifier
     * @return the resulting employee
     */
    EmployeeResponse deactivate(Long id);

    /**
     * Deletes the employee.
     *
     * @param id the database identifier
     */
    void delete(Long id);
}
