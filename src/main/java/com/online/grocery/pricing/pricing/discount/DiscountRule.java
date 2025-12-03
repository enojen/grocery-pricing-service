package com.online.grocery.pricing.pricing.discount;

import com.online.grocery.pricing.domain.enums.ProductType;

/**
 * Base interface for all discount rules.
 * Implementations are auto-discovered by Spring and registered with DiscountRuleService.
 */
public interface DiscountRule {

    /**
     * The product type this discount rule applies to.
     *
     * @return Product type for this rule
     */
    ProductType productType();

    /**
     * Human-readable description of this discount rule.
     * Used by GET /discounts/rules endpoint.
     *
     * @return Description for API documentation
     */
    String description();
}
