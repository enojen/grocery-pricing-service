package com.online.grocery.pricing.api.dto;

import java.math.BigDecimal;

/**
 * Response DTO for product price information.
 */
public record PriceInfoResponse(
        String productName,
        BigDecimal price,
        String unit
) {
}
