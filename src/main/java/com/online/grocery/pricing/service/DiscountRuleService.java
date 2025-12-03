package com.online.grocery.pricing.service;

import com.online.grocery.pricing.api.dto.DiscountRuleResponse;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.pricing.discount.DiscountRule;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for retrieving discount rule metadata.
 * Auto-discovers all registered discount rules and provides descriptions
 * for the GET /discounts/rules endpoint.
 */
@Service
public class DiscountRuleService {

    private final List<DiscountRule> allRules;

    public DiscountRuleService(List<DiscountRule> allRules) {
        this.allRules = allRules;
    }

    /**
     * Get all registered discount rules with their descriptions.
     *
     * @return List of discount rules with product type and description
     */
    public List<DiscountRuleResponse> getAllRules() {
        return allRules.stream()
                .map(rule -> new DiscountRuleResponse(
                        rule.productType().name(),
                        rule.description()
                ))
                .toList();
    }

    /**
     * Get discount rules for a specific product type.
     *
     * @param productType The product type to filter by
     * @return List of discount rules for the specified type
     */
    public List<DiscountRuleResponse> getRulesByProductType(ProductType productType) {
        return getAllRules().stream()
                .filter(rule -> rule.productType().equals(productType.name()))
                .toList();
    }
}
