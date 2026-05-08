package com.assetvault.exception;

/**
 * Base runtime exception for AssetVault domain failures.
 */
public abstract class AssetVaultException extends RuntimeException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    protected AssetVaultException(String message) {
        super(message);
    }
}
