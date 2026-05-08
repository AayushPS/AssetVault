package com.assetvault.exception;

/**
 * Exception thrown when the requested license is already assigned.
 */
public class LicenseAlreadyAssignedException extends AssetVaultException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    public LicenseAlreadyAssignedException(String message) {
        super(message);
    }
}
