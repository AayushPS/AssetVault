package com.assetvault.exception;

/**
 * Exception thrown when a maintenance status transition is invalid.
 */
public class MaintenanceStateException extends AssetVaultException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    public MaintenanceStateException(String message) {
        super(message);
    }
}
