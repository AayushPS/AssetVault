package com.assetvault.exception;

public class EmployeeHasActiveAssetsException extends AssetVaultException {
    public EmployeeHasActiveAssetsException(String message) {
        super(message);
    }
}
