package com.assetvault.service;

import com.assetvault.dto.AssetAssignmentResponse;
import com.assetvault.dto.AssetResponse;
import com.assetvault.dto.EmployeeRequest;
import com.assetvault.dto.EmployeeResponse;
import com.assetvault.dto.SoftwareLicenseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    EmployeeResponse create(EmployeeRequest request);

    Page<EmployeeResponse> getAll(Pageable pageable);

    EmployeeResponse getById(Long id);

    Page<EmployeeResponse> searchByName(String name, Pageable pageable);

    Page<EmployeeResponse> getByDepartment(String name, Pageable pageable);

    Page<AssetResponse> getAssignedAssets(Long id, Pageable pageable);

    Page<SoftwareLicenseResponse> getAssignedLicenses(Long id, Pageable pageable);

    Page<AssetAssignmentResponse> getAssignmentHistory(Long id, Pageable pageable);

    EmployeeResponse update(Long id, EmployeeRequest request);

    EmployeeResponse deactivate(Long id);

    void delete(Long id);
}
