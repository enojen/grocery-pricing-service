package com.online.grocery.pricing.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for order calculation endpoint.
 */
public record OrderRequest(
        @NotEmpty(message = "At least one item required")
        @Valid
        List<OrderItemRequest> items
) {
}
