package com.assetvault.exception;

/**
 * Exception thrown when the requested license cannot be found.
 */
public class LicenseNotFoundException extends AssetVaultException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    public LicenseNotFoundException(String message) {
        super(message);
    }
}
