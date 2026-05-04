package com.Gemini.AssetVault.Exception;

public abstract class AssetVaultException extends RuntimeException {
    protected AssetVaultException(String message) {
        super(message);
    }
}
