package com.online.grocery.pricing.pricing.discount;

import java.math.BigDecimal;

import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.pricing.context.PricingContext;

/**
 * Base interface for all discount rules.
 * Implementations are auto-discovered by Spring and registered with DiscountRuleService.
 */
public interface DiscountRule<C extends PricingContext> {

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

    boolean isApplicable(C ctx);

    BigDecimal calculateDiscount(C ctx);

    int order();


}
