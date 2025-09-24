package com.example.demo.exception;

public class MustChangePasswordException extends RuntimeException {
    public MustChangePasswordException(String message) {
        super(message);
    }
    
    public MustChangePasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
