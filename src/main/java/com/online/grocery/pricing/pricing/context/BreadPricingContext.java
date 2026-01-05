package com.online.grocery.pricing.pricing.context;

import java.math.BigDecimal;

/**
 * Context for bread pricing calculations.
 * Encapsulates all data needed by bread discount rules.
 *
 * @param age           Age of bread in days (0-6)
 * @param totalQuantity Total number of bread units
 * @param unitPrice     Price per bread unit
 * @param originalPrice Total price before discounts
 */
public record BreadPricingContext(
        int age,
        int totalQuantity,
        BigDecimal unitPrice,
        BigDecimal originalPrice
) implements PricingContext {
}
