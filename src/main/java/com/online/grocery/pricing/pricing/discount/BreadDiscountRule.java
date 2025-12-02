package com.online.grocery.pricing.pricing.discount;

import com.online.grocery.pricing.pricing.context.BreadPricingContext;

import java.math.BigDecimal;

/**
 * Interface for bread discount rules.
 * Implementations are auto-discovered by Spring and applied by BreadPricingStrategy.
 */
public interface BreadDiscountRule {
    
    /**
     * Check if this discount rule applies to the given context.
     * 
     * @param ctx Bread pricing context with age, quantity, and price data
     * @return true if this rule should be applied
     */
    boolean isApplicable(BreadPricingContext ctx);

    /**
     * Calculate the discount amount for this rule.
     * Only called if isApplicable() returns true.
     * 
     * @param ctx Bread pricing context with age, quantity, and price data
     * @return Discount amount to subtract from original price
     */
    BigDecimal calculateDiscount(BreadPricingContext ctx);

    /**
     * Order of execution (lower numbers execute first).
     * 
     * @return Execution order priority
     */
    int order();

    /**
     * Human-readable description of this discount rule.
     * 
     * @return Description for API documentation
     */
    String description();
}
