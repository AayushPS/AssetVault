package com.assetvault.exception;

/**
 * Exception thrown when the requested asset is already assigned.
 */
public class AssetAlreadyAssignedException extends AssetVaultException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    public AssetAlreadyAssignedException(String message) {
        super(message);
    }
}
