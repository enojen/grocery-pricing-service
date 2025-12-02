package com.online.grocery.pricing.api.dto;

import java.math.BigDecimal;

/**
 * Response DTO for a single receipt line item.
 */
public record ReceiptLineResponse(
    String description,
    BigDecimal originalPrice,
    BigDecimal discount,
    BigDecimal finalPrice
) {}
