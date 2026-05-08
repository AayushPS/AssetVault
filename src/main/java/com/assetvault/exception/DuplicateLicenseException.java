package com.assetvault.exception;

/**
 * Exception thrown when a duplicate license is detected.
 */
public class DuplicateLicenseException extends AssetVaultException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    public DuplicateLicenseException(String message) {
        super(message);
    }
}
