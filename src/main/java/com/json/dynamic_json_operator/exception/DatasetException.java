package com.json.dynamic_json_operator.exception;

import org.springframework.http.HttpStatus;

public class DatasetException extends RuntimeException {
    private final HttpStatus status;

    public DatasetException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}