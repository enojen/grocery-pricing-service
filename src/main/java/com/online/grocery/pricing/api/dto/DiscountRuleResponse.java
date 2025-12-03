package com.online.grocery.pricing.api.dto;

/**
 * Response DTO for discount rule information.
 *
 * @param productType The product type this rule applies to
 * @param description Human-readable description of the discount rule
 */
public record DiscountRuleResponse(
        String productType,
        String description
) {
}
