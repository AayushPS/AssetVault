package com.assetvault.exception;

public abstract class AssetVaultException extends RuntimeException {
    protected AssetVaultException(String message) {
        super(message);
    }
}
