# TASK-023: Exception Handling

## Status
- [ ] Not Started

## Phase
Phase 4: REST API

## Description
Create GlobalExceptionHandler and InvalidOrderException for comprehensive error handling.

## Implementation Details

### InvalidOrderException

```java
package com.grocery.pricing.exception;

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
```

### GlobalExceptionHandler

```java
package com.grocery.pricing.exception;

import com.grocery.pricing.api.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
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
                (first, second) -> first // Handle duplicate keys
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
     * Handle unexpected errors.
     * Returns HTTP 500 Internal Server Error.
     * Logs the actual error but returns generic message for security.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralError(Exception ex) {
        // Log the actual error for debugging
        // In production, use proper logging framework
        ex.printStackTrace();
        
        ErrorResponse response = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

### Error Response Examples

**Validation Error (400 Bad Request):**
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Invalid request data",
  "details": {
    "items": "At least one item required",
    "items[0].type": "Product type required"
  }
}
```

**Business Rule Violation (422 Unprocessable Entity):**
```json
{
  "code": "INVALID_ORDER",
  "message": "origin field required for product type BEER",
  "details": null
}
```

**Domain Validation Error (422 Unprocessable Entity):**
```json
{
  "code": "INVALID_ORDER",
  "message": "Bread older than 6 days cannot be ordered",
  "details": null
}
```

**Internal Error (500 Internal Server Error):**
```json
{
  "code": "INTERNAL_ERROR",
  "message": "An unexpected error occurred",
  "details": null
}
```

### Unit Tests

```java
package com.grocery.pricing.exception;

import com.grocery.pricing.api.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleValidationErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError = new FieldError(
            "orderRequest", "items", "At least one item required"
        );
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().details()).containsKey("items");
    }

    @Test
    void shouldHandleInvalidOrderException() {
        InvalidOrderException ex = new InvalidOrderException("Test error message");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidOrder(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().code()).isEqualTo("INVALID_ORDER");
        assertThat(response.getBody().message()).isEqualTo("Test error message");
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Quantity must be positive");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().code()).isEqualTo("INVALID_ORDER");
        assertThat(response.getBody().message()).isEqualTo("Quantity must be positive");
    }

    @Test
    void shouldHandleGeneralError() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGeneralError(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
    }
}
```

## Files to Create

- `src/main/java/com/grocery/pricing/exception/InvalidOrderException.java`
- `src/main/java/com/grocery/pricing/exception/GlobalExceptionHandler.java`
- `src/test/java/com/grocery/pricing/exception/GlobalExceptionHandlerTest.java`

## Acceptance Criteria

- [ ] InvalidOrderException for business rule violations
- [ ] GlobalExceptionHandler with @RestControllerAdvice
- [ ] Handles MethodArgumentNotValidException → 400 Bad Request
- [ ] Handles InvalidOrderException → 422 Unprocessable Entity
- [ ] Handles IllegalArgumentException → 422 Unprocessable Entity
- [ ] Handles generic Exception → 500 Internal Server Error
- [ ] All error responses use standard ErrorResponse format
- [ ] All unit tests pass
