package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class DashboardException extends RuntimeException {
    private final HttpStatus status;

    public DashboardException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public DashboardException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
