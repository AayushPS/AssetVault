package com.assetvault.exception;

/**
 * Exception thrown when an employee still has active asset assignments.
 */
public class EmployeeHasActiveAssetsException extends AssetVaultException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    public EmployeeHasActiveAssetsException(String message) {
        super(message);
    }
}
