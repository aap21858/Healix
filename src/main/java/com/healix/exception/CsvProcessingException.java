package com.healix.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when CSV processing fails
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CsvProcessingException extends RuntimeException {

    public CsvProcessingException(String message) {
        super(message);
    }

    public CsvProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}