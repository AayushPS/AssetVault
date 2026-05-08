package com.assetvault.exception;

/**
 * Exception thrown when the requested employee is inactive.
 */
public class InactiveEmployeeException extends AssetVaultException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    public InactiveEmployeeException(String message) {
        super(message);
    }
}
