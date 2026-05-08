package com.assetvault.exception;

/**
 * Exception thrown when the requested asset is not available.
 */
public class AssetNotAvailableException extends AssetVaultException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    public AssetNotAvailableException(String message) {
        super(message);
    }
}
