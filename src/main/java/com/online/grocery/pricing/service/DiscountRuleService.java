package com.online.grocery.pricing.service;

import com.online.grocery.pricing.api.dto.DiscountRuleResponse;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.pricing.discount.DiscountRule;
import com.online.grocery.pricing.pricing.discount.OrderDiscountRule;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for retrieving discount rule metadata.
 * Auto-discovers all registered discount rules and provides descriptions
 * for the GET /discounts/rules endpoint.
 */
@Service
public class DiscountRuleService {

    private final List<DiscountRule> productRules;
    private final List<OrderDiscountRule> orderRules;

    public DiscountRuleService(
            List<DiscountRule> productRules,
            List<OrderDiscountRule> orderRules) {
        this.productRules = productRules;
        this.orderRules = orderRules;
    }

    /**
     * Get all registered discount rules with their descriptions.
     *
     * @return List of discount rules with product type and description
     */
    public List<DiscountRuleResponse> getAllRules() {
        List<DiscountRuleResponse> responses = new ArrayList<>();

        // Add product-level discount rules
        productRules.stream()
                .map(rule -> new DiscountRuleResponse(
                        rule.productType().name(),
                        rule.description()
                ))
                .forEach(responses::add);

        // Add order-level discount rules
        orderRules.stream()
                .map(rule -> new DiscountRuleResponse(
                        rule.productTypes().stream()
                                .map(ProductType::name)
                                .collect(Collectors.joining(", ")),
                        rule.description()
                ))
                .forEach(responses::add);

        return responses;
    }

    /**
     * Get discount rules for a specific product type.
     *
     * @param productType The product type to filter by
     * @return List of discount rules for the specified type
     */
    public List<DiscountRuleResponse> getRulesByProductType(ProductType productType) {
        List<DiscountRuleResponse> responses = new ArrayList<>();

        // Add matching product-level rules
        productRules.stream()
                .filter(rule -> rule.productType() == productType)
                .map(rule -> new DiscountRuleResponse(
                        rule.productType().name(),
                        rule.description()
                ))
                .forEach(responses::add);

        // Add order-level rules that include this product type
        orderRules.stream()
                .filter(rule -> rule.productTypes().contains(productType))
                .map(rule -> new DiscountRuleResponse(
                        rule.productTypes().stream()
                                .map(ProductType::name)
                                .collect(Collectors.joining(", ")),
                        rule.description()
                ))
                .forEach(responses::add);

        return responses;
    }
}
