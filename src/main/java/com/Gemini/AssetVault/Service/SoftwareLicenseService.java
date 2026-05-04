package com.Gemini.AssetVault.Service;

import com.Gemini.AssetVault.Dto.SoftwareLicenseRequest;
import com.Gemini.AssetVault.Dto.SoftwareLicenseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SoftwareLicenseService {
    SoftwareLicenseResponse create(SoftwareLicenseRequest request);

    Page<SoftwareLicenseResponse> getAll(Pageable pageable);

    SoftwareLicenseResponse getById(Long id);

    Page<SoftwareLicenseResponse> getExpiringSoon(int days, Pageable pageable);

    Page<SoftwareLicenseResponse> getExpired(Pageable pageable);

    Page<SoftwareLicenseResponse> getLowSeats(Pageable pageable);

    Page<SoftwareLicenseResponse> search(String name, Pageable pageable);

    SoftwareLicenseResponse update(Long id, SoftwareLicenseRequest request);

    SoftwareLicenseResponse assign(Long id, Long employeeId);

    SoftwareLicenseResponse revoke(Long id, Long employeeId);

    SoftwareLicenseResponse deactivate(Long id);

    void delete(Long id);
}
