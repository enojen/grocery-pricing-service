package com.online.grocery.pricing.api.dto;

import com.online.grocery.pricing.domain.enums.BeerOrigin;
import com.online.grocery.pricing.domain.enums.ProductType;
import jakarta.validation.constraints.*;

/**
 * Unified request DTO for all product types.
 * Type-specific validation is handled by OrderMapper.
 */
public record OrderItemRequest(
        @NotNull(message = "Product type required")
        ProductType type,

        @NotBlank(message = "Item name required")
        String name,

        @Positive(message = "Quantity must be positive")
        Integer quantity,

        @Min(value = 0, message = "Age cannot be negative")
        @Max(value = 6, message = "Bread older than 6 days not allowed")
        Integer daysOld,

        @Positive(message = "Weight must be positive")
        Integer weightGrams,

        BeerOrigin origin
) {
}
