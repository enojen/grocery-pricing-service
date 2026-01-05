package com.online.grocery.pricing.pricing.context;

import java.math.BigDecimal;

/**
 * Base interface for all pricing contexts.
 * Each product type has its own implementation with additional fields.
 */
public interface PricingContext {

    /**
     * Returns the original price before any discounts.
     * Used by AbstractPricingStrategy for discount cap calculations.
     *
     * @return Original price before discounts
     */
    BigDecimal originalPrice();
}
