package com.online.grocery.pricing.pricing.context;

import com.online.grocery.pricing.domain.model.Order;

import java.math.BigDecimal;

/**
 * Context for order-level pricing calculations.
 * Used by combo discount rules that span multiple product types.
 *
 * @param order        The complete order with all items
 * @param subtotal     Total before any discounts
 * @param currentTotal Total after product-level discounts have been applied
 */
public record OrderPricingContext(
        Order order,
        BigDecimal subtotal,
        BigDecimal currentTotal
) implements PricingContext {
}
