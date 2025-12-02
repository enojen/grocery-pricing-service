package com.online.grocery.pricing.exception;

/**
 * Exception thrown when an order violates business rules.
 * Results in HTTP 422 Unprocessable Entity response.
 */
public class InvalidOrderException extends RuntimeException {

    public InvalidOrderException(String message) {
        super(message);
    }

    public InvalidOrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
