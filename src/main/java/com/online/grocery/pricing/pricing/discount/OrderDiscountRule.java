package com.online.grocery.pricing.pricing.discount;

import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.pricing.context.OrderPricingContext;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface for order-level discount rules (combos spanning multiple products).
 * Implementations are auto-discovered by Spring and applied after product-level discounts.
 */
public interface OrderDiscountRule {

    /**
     * Returns the product types that this discount rule applies to.
     * For example, a bread-vegetable combo would return [BREAD, VEGETABLE].
     *
     * @return List of product types involved in this discount rule
     */
    List<ProductType> productTypes();

    /**
     * Check if this discount rule applies to the given order context.
     *
     * @param ctx Order pricing context with order data and current total
     * @return true if this rule should be applied
     */
    boolean isApplicable(OrderPricingContext ctx);

    /**
     * Calculate the discount amount for this rule.
     * Only called if isApplicable() returns true.
     *
     * @param ctx Order pricing context with order data and current total
     * @return Discount amount to subtract from current total
     */
    BigDecimal calculateDiscount(OrderPricingContext ctx);

    /**
     * Order of execution (lower numbers execute first).
     * Order-level discounts should use high values (e.g., 1000+)
     * to ensure they run after product-level discounts.
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
