package com.healix.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.healix.model.ErrorResponse;
import com.healix.model.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getFieldErrors()
//                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                .body(String.join(", ", errors.values()));
//    }

    /**
     * Handle ResourceNotFoundException
     * Returns 404 NOT FOUND
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle validation errors (Bean Validation)
     * Returns 400 BAD REQUEST
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ValidationErrorResponse errorResponse = ValidationErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Invalid input data")
                .validationErrors(validationErrors)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle JSON parsing errors (e.g., invalid type conversions)
     * Returns 400 BAD REQUEST
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        String message = "Invalid request format";
        String fieldName = null;
        String invalidValue = null;
        String expectedType = null;

        // Extract more specific error message with null-safety checks
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException ifx) {
            // Extract field name from path
            if (ifx.getPath() != null && !ifx.getPath().isEmpty()) {
                var lastPath = ifx.getPath().getLast();
                if (lastPath != null && lastPath.getFieldName() != null) {
                    fieldName = lastPath.getFieldName();
                }
            }

            invalidValue = ifx.getValue() != null ? ifx.getValue().toString() : "null";
            expectedType = ifx.getTargetType() != null ?
                ifx.getTargetType().getSimpleName() : null;

            if (fieldName != null) {
                message = String.format("Invalid value '%s' for field '%s'%s",
                    invalidValue, fieldName,
                    expectedType != null ? ". Expected type: " + expectedType : "");
            } else {
                message = String.format("Invalid value '%s'%s",
                    invalidValue,
                    expectedType != null ? ". Expected type: " + expectedType : "");
            }

        } else if (cause instanceof MismatchedInputException mix) {
            // Extract field name from path
            if (mix.getPath() != null && !mix.getPath().isEmpty()) {
                var lastPath = mix.getPath().getLast();
                if (lastPath != null && lastPath.getFieldName() != null) {
                    fieldName = lastPath.getFieldName();
                }
            }

            expectedType = mix.getTargetType() != null ?
                mix.getTargetType().getSimpleName() : null;

            if (fieldName != null) {
                message = String.format("Invalid input for field '%s'%s",
                    fieldName,
                    expectedType != null ? ". Expected type: " + expectedType : "");
            } else if (expectedType != null) {
                message = String.format("Invalid input. Expected type: %s", expectedType);
            }

        } else {
            // Try to extract field info from the exception message itself
            Throwable mostSpecificCause = ex.getMostSpecificCause();
            if (mostSpecificCause != null && mostSpecificCause.getMessage() != null) {
                String causeMessage = mostSpecificCause.getMessage();

                // Try to parse field name from error message patterns
                // Pattern: "Cannot deserialize value of type `X` from String \"Y\": ..."
                // Pattern: "JSON parse error: Cannot deserialize instance of `X` ..."
                if (causeMessage.contains("Cannot deserialize")) {
                    message = "Cannot parse request body: " + causeMessage;

                    // Try to extract more info from the message
                    if (causeMessage.contains("from String")) {
                        int fromIdx = causeMessage.indexOf("from String \"");
                        int endIdx = causeMessage.indexOf("\":", fromIdx + 13);
                        if (fromIdx != -1 && endIdx != -1) {
                            invalidValue = causeMessage.substring(fromIdx + 13, endIdx);
                        }
                    }

                    if (causeMessage.contains("type `") || causeMessage.contains("instance of `")) {
                        int typeStart = causeMessage.indexOf("type `");
                        if (typeStart == -1) typeStart = causeMessage.indexOf("instance of `");
                        if (typeStart != -1) {
                            int typeEnd = causeMessage.indexOf("`", typeStart + 6);
                            if (typeEnd != -1) {
                                String fullType = causeMessage.substring(typeStart + 6, typeEnd);
                                expectedType = fullType.substring(fullType.lastIndexOf('.') + 1);
                            }
                        }
                    }

                    // Build better message if we extracted details
                    if (invalidValue != null && expectedType != null) {
                        message = String.format("Invalid value '%s'. Expected type: %s",
                            invalidValue, expectedType);
                    } else if (expectedType != null) {
                        message = String.format("Invalid input. Expected type: %s", expectedType);
                    }

                } else if (causeMessage.contains("not a valid")) {
                    message = "Invalid data format: " + causeMessage;
                } else {
                    // Include the original error message for better debugging
                    message = "Invalid request format: " + causeMessage;
                }
            }

            // Last resort: check the main exception message
            if (message.equals("Invalid request format") && ex.getMessage() != null) {
                String exMessage = ex.getMessage();
                if (exMessage.length() > 200) {
                    message = "Invalid request format: " + exMessage.substring(0, 200) + "...";
                } else {
                    message = "Invalid request format: " + exMessage;
                }
            }
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle constraint violations (e.g., from @Valid on entity level)
     * Returns 400 BAD REQUEST
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        Map<String, String> validationErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                    violation -> violation.getPropertyPath().toString(),
                    ConstraintViolation::getMessage,
                    (existing, replacement) -> existing + "; " + replacement
                ));

        ValidationErrorResponse errorResponse = ValidationErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Constraint violations occurred")
                .validationErrors(validationErrors)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle method argument type mismatch (e.g., string in path variable expecting number)
     * Returns 400 BAD REQUEST
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String typeName = ex.getRequiredType() != null ?
            ex.getRequiredType().getSimpleName() : "unknown type";

        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), typeName);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalArgumentException
     * Returns 400 BAD REQUEST
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle AccessDeniedException (Spring Security)
     * Returns 403 FORBIDDEN
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message("Access denied: " + ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle duplicate resource (e.g., duplicate mobile number)
     * Returns 409 CONFLICT
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle all other exceptions
     * Returns 500 INTERNAL SERVER ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred: " + ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle CsvProcessingException
     * Returns 400 BAD REQUEST
     */
    @ExceptionHandler(CsvProcessingException.class)
    public ResponseEntity<ErrorResponse> handleCsvProcessingException(
            CsvProcessingException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}

