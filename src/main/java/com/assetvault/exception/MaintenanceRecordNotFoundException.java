package com.assetvault.exception;

/**
 * Exception thrown when the requested maintenance record cannot be found.
 */
public class MaintenanceRecordNotFoundException extends AssetVaultException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    public MaintenanceRecordNotFoundException(String message) {
        super(message);
    }
}
