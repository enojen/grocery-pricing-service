package com.online.grocery.pricing.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for the complete receipt.
 */
public record ReceiptResponse(
        List<ReceiptLineResponse> lines,
        BigDecimal subtotal,
        BigDecimal totalDiscount,
        BigDecimal total
) {
}
