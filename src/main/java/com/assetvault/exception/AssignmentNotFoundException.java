package com.assetvault.exception;

/**
 * Exception thrown when the requested assignment cannot be found.
 */
public class AssignmentNotFoundException extends AssetVaultException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    public AssignmentNotFoundException(String message) {
        super(message);
    }
}
