package com.assetvault.exception;

/**
 * Exception thrown when the requested employee cannot be found.
 */
public class EmployeeNotFoundException extends AssetVaultException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
