package com.example.demo.exception;

public class HierarchyViolationException extends RuntimeException {
    public HierarchyViolationException(String message) {
        super(message);
    }
}
