package com.online.grocery.pricing.pricing.discount;

import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.pricing.context.VegetablePricingContext;

import java.math.BigDecimal;

/**
 * Interface for vegetable discount rules.
 * Implementations are auto-discovered by Spring and applied by VegetablePricingStrategy.
 */
public interface VegetableDiscountRule extends DiscountRule {

    @Override
    default ProductType productType() {
        return ProductType.VEGETABLE;
    }

    /**
     * Check if this discount rule applies to the given context.
     *
     * @param ctx Vegetable pricing context with weight and price data
     * @return true if this rule should be applied
     */
    boolean isApplicable(VegetablePricingContext ctx);

    /**
     * Calculate the discount amount for this rule.
     * Only called if isApplicable() returns true.
     *
     * @param ctx Vegetable pricing context with weight and price data
     * @return Discount amount to subtract from original price
     */
    BigDecimal calculateDiscount(VegetablePricingContext ctx);

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
