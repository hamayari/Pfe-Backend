package com.example.demo.exception;

public class DataNotFoundException extends RuntimeException {
    public DataNotFoundException(String message) {
        super(message);
    }

    public DataNotFoundException(String resource, String id) {
        super(String.format("%s not found with id: %s", resource, id));
    }
}
