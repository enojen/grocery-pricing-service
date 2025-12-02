package com.online.grocery.pricing.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents a complete receipt with all line items and totals.
 * 
 * @param lines Individual line items on the receipt
 * @param subtotal Sum of all original prices
 * @param totalDiscount Sum of all discounts applied
 * @param total Final total (subtotal - totalDiscount)
 */
public record Receipt(
    List<ReceiptLine> lines,
    BigDecimal subtotal,
    BigDecimal totalDiscount,
    BigDecimal total
) {
    public Receipt {
        lines = List.copyOf(lines);
    }
}
