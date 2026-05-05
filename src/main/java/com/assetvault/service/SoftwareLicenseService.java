package com.assetvault.service;

import com.assetvault.dto.SoftwareLicenseRequest;
import com.assetvault.dto.SoftwareLicenseResponse;
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

    SoftwareLicenseResponse deactivate(Long id);

    void delete(Long id);
}
