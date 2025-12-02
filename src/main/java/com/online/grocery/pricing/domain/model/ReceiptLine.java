package com.online.grocery.pricing.domain.model;

import java.math.BigDecimal;

/**
 * Represents a single line item on a receipt.
 * 
 * @param description Human-readable description of the item
 * @param originalPrice Price before any discounts
 * @param discount Total discount applied to this line
 * @param finalPrice Price after discount (originalPrice - discount)
 */
public record ReceiptLine(
    String description,
    BigDecimal originalPrice,
    BigDecimal discount,
    BigDecimal finalPrice
) {
    public ReceiptLine {
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Final price cannot be negative");
        }
    }
}
