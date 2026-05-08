package com.assetvault.exception;

/**
 * Exception thrown when a duplicate serial number is detected.
 */
public class DuplicateSerialNumberException extends AssetVaultException {
    /**
     * Creates a new exception instance.
     *
     * @param message the exception detail message
     */
    public DuplicateSerialNumberException(String message) {
        super(message);
    }
}
