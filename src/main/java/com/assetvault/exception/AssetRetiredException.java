package com.assetvault.exception;

/**
 * Exception thrown when an operation targets a retired asset.
 */
public class AssetRetiredException extends AssetVaultException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    public AssetRetiredException(String message) {
        super(message);
    }
}
