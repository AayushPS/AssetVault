package com.assetvault.service;

import com.assetvault.dto.SoftwareLicenseResponse;

/**
 * Service contract for software assignment operations.
 */
public interface SoftwareAssignmentService {
    /**
     * Assigns the requested software assignment.
     *
     * @param licenseId the license identifier
     * @param employeeId the employee identifier
     * @return the resulting software assignment
     */
    SoftwareLicenseResponse assign(Long licenseId, Long employeeId);

    /**
     * Revokes the requested software assignment assignment.
     *
     * @param licenseId the license identifier
     * @param employeeId the employee identifier
     * @return the resulting software assignment
     */
    SoftwareLicenseResponse revoke(Long licenseId, Long employeeId);

    /**
     * Executes the revoke active assignments for license operation.
     *
     * @param licenseId the license identifier
     * @param remarks the reason captured for the state change
     * @return the computed numeric result
     */
    int revokeActiveAssignmentsForLicense(Long licenseId, String remarks);

    /**
     * Executes the delete assignments for license operation.
     *
     * @param licenseId the license identifier
     * @return the computed numeric result
     */
    long deleteAssignmentsForLicense(Long licenseId);
}
