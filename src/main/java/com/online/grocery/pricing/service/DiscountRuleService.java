package com.online.grocery.pricing.service;

import com.online.grocery.pricing.api.dto.DiscountRuleResponse;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.pricing.discount.BeerDiscountRule;
import com.online.grocery.pricing.pricing.discount.BreadDiscountRule;
import com.online.grocery.pricing.pricing.discount.VegetableDiscountRule;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Service for retrieving discount rule metadata.
 * Auto-discovers all registered discount rules and provides descriptions
 * for the GET /discounts/rules endpoint.
 */
@Service
public class DiscountRuleService {

    private final List<BeerDiscountRule> beerRules;
    private final List<BreadDiscountRule> breadRules;
    private final List<VegetableDiscountRule> vegetableRules;

    public DiscountRuleService(
            List<BeerDiscountRule> beerRules,
            List<BreadDiscountRule> breadRules,
            List<VegetableDiscountRule> vegetableRules
    ) {
        this.beerRules = beerRules;
        this.breadRules = breadRules;
        this.vegetableRules = vegetableRules;
    }

    /**
     * Get all registered discount rules with their descriptions.
     *
     * @return List of discount rules with product type and description
     */
    public List<DiscountRuleResponse> getAllRules() {
        Stream<DiscountRuleResponse> beer = beerRules.stream()
                .map(rule -> new DiscountRuleResponse(
                        ProductType.BEER.name(),
                        rule.description()
                ));

        Stream<DiscountRuleResponse> bread = breadRules.stream()
                .map(rule -> new DiscountRuleResponse(
                        ProductType.BREAD.name(),
                        rule.description()
                ));

        Stream<DiscountRuleResponse> veg = vegetableRules.stream()
                .map(rule -> new DiscountRuleResponse(
                        ProductType.VEGETABLE.name(),
                        rule.description()
                ));

        return Stream.of(bread, veg, beer)
                .flatMap(Function.identity())
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
