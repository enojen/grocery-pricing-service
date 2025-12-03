package com.online.grocery.pricing.pricing.discount;

import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.pricing.context.BeerPricingContext;

import java.math.BigDecimal;

/**
 * Interface for beer discount rules.
 * Implementations are auto-discovered by Spring and applied by BeerPricingStrategy.
 */
public interface BeerDiscountRule extends DiscountRule {

    @Override
    default ProductType productType() {
        return ProductType.BEER;
    }

    /**
     * Check if this discount rule applies to the given context.
     *
     * @param ctx Beer pricing context with all relevant data
     * @return true if this rule should be applied
     */
    boolean isApplicable(BeerPricingContext ctx);

    /**
     * Calculate the discount amount for this rule.
     * Only called if isApplicable() returns true.
     *
     * @param ctx Beer pricing context with all relevant data
     * @return Discount amount to subtract from original price
     */
    BigDecimal calculateDiscount(BeerPricingContext ctx);

    /**
     * Order of execution (lower numbers execute first).
     * Use values like 100, 200, 300 to allow insertion between rules.
     *
     * @return Execution order priority
     */
    int order();

    /**
     * Human-readable description of this discount rule.
     * Used by GET /discounts/rules endpoint.
     *
     * @return Description for API documentation
     */
    String description();
}
