package com.assetvault.exception;

/**
 * Exception thrown when no license are available.
 */
public class NoLicenseSeatsAvailableException extends AssetVaultException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    public NoLicenseSeatsAvailableException(String message) {
        super(message);
    }
}
