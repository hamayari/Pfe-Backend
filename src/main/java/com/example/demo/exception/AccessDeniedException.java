package com.example.demo.exception;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
    
    public AccessDeniedException(String resource, String action) {
        super(String.format("Access denied: Cannot %s the resource: %s", action, resource));
    }
}
