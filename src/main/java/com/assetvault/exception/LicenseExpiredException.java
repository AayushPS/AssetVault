package com.assetvault.exception;

/**
 * Exception thrown when the requested license has expired.
 */
public class LicenseExpiredException extends AssetVaultException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    public LicenseExpiredException(String message) {
        super(message);
    }
}
