package com.online.grocery.pricing.exception;

import com.online.grocery.pricing.api.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API.
 * Converts exceptions to standardized error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle Bean Validation errors (@Valid annotation failures).
     * Returns HTTP 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null
                                ? error.getDefaultMessage()
                                : "Invalid value",
                        (first, second) -> first
                ));

        ErrorResponse response = new ErrorResponse(
                "VALIDATION_ERROR",
                "Invalid request data",
                errors
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle business rule violations.
     * Returns HTTP 422 Unprocessable Entity.
     */
    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrder(
            InvalidOrderException ex
    ) {
        ErrorResponse response = new ErrorResponse(
                "INVALID_ORDER",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    /**
     * Handle illegal argument exceptions from domain validation.
     * Returns HTTP 422 Unprocessable Entity.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex
    ) {
        ErrorResponse response = new ErrorResponse(
                "INVALID_ORDER",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    /**
     * Handle JSON parsing/deserialization errors (invalid enum values, malformed JSON).
     * Returns HTTP 400 Bad Request.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex
    ) {
        String message = ex.getMostSpecificCause().getMessage();
        ErrorResponse response = new ErrorResponse(
                "VALIDATION_ERROR",
                message != null ? message : "Malformed JSON request",
                null
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle unexpected errors.
     * Returns HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralError(Exception ex) {
        ErrorResponse response = new ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
