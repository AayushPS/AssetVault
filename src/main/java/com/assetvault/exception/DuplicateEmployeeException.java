package com.assetvault.exception;

/**
 * Exception thrown when a duplicate employee is detected.
 */
public class DuplicateEmployeeException extends AssetVaultException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    public DuplicateEmployeeException(String message) {
        super(message);
    }
}
