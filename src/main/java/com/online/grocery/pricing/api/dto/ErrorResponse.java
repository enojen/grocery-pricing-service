package com.online.grocery.pricing.api.dto;

import java.util.Map;

/**
 * Standard error response format.
 */
public record ErrorResponse(
        String code,
        String message,
        Map<String, String> details
) {
}
