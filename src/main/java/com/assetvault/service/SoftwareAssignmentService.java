package com.assetvault.service;

import com.assetvault.dto.SoftwareLicenseResponse;

public interface SoftwareAssignmentService {
    SoftwareLicenseResponse assign(Long licenseId, Long employeeId);

    SoftwareLicenseResponse revoke(Long licenseId, Long employeeId);

    int revokeActiveAssignmentsForLicense(Long licenseId, String remarks);

    long deleteAssignmentsForLicense(Long licenseId);
}
