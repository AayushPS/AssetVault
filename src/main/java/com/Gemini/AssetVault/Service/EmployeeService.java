package com.Gemini.AssetVault.Service;

import com.Gemini.AssetVault.Dto.AssetAssignmentResponse;
import com.Gemini.AssetVault.Dto.AssetResponse;
import com.Gemini.AssetVault.Dto.EmployeeRequest;
import com.Gemini.AssetVault.Dto.EmployeeResponse;
import com.Gemini.AssetVault.Dto.SoftwareLicenseResponse;
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
