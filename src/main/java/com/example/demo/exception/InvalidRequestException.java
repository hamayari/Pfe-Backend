package com.example.demo.exception;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
    
    public InvalidRequestException(String field, String message) {
        super(String.format("Invalid request: %s - %s", field, message));
    }
}
