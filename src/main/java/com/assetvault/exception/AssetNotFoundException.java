package com.assetvault.exception;

/**
 * Exception thrown when the requested asset cannot be found.
 */
public class AssetNotFoundException extends AssetVaultException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    public AssetNotFoundException(String message) {
        super(message);
    }
}
