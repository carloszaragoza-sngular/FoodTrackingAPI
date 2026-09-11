package com.foodtracking.usda;

public class UsdaUnavailableException extends Exception {

    public UsdaUnavailableException(String message) {
        super(message);
    }

    public UsdaUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
