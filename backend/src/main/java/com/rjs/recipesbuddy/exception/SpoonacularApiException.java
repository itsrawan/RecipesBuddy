package com.rjs.recipesbuddy.exception;

public class SpoonacularApiException extends RuntimeException {
    private final int statusCode;
    
    public SpoonacularApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public SpoonacularApiException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}
