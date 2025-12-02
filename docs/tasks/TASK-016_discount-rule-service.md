# TASK-016: Discount Rule Service

## Status
- [ ] Not Started

## Phase
Phase 3: Service Layer

## Description
Create DiscountRuleService that provides discount rule metadata for the API documentation endpoint.

## Implementation Details

### DiscountRuleResponse DTO

```java
package com.grocery.pricing.api.dto;

/**
 * Response DTO for discount rule information.
 * 
 * @param productType The product type this rule applies to
 * @param description Human-readable description of the discount rule
 */
public record DiscountRuleResponse(
    String productType,
    String description
) {}
```

### DiscountRuleService

```java
package com.grocery.pricing.service;

import com.grocery.pricing.api.dto.DiscountRuleResponse;
import com.grocery.pricing.domain.enums.ProductType;
import com.grocery.pricing.pricing.discount.BeerDiscountRule;
import com.grocery.pricing.pricing.discount.BreadDiscountRule;
import com.grocery.pricing.pricing.discount.VegetableDiscountRule;
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
```

### Unit Tests

```java
package com.grocery.pricing.service;

import com.grocery.pricing.api.dto.DiscountRuleResponse;
import com.grocery.pricing.domain.enums.ProductType;
import com.grocery.pricing.pricing.discount.BeerDiscountRule;
import com.grocery.pricing.pricing.discount.BreadDiscountRule;
import com.grocery.pricing.pricing.discount.VegetableDiscountRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscountRuleServiceTest {

    private BeerDiscountRule beerRule;
    private BreadDiscountRule breadRule;
    private VegetableDiscountRule vegetableRule;
    private DiscountRuleService service;

    @BeforeEach
    void setUp() {
        beerRule = mock(BeerDiscountRule.class);
        breadRule = mock(BreadDiscountRule.class);
        vegetableRule = mock(VegetableDiscountRule.class);

        when(beerRule.description()).thenReturn("Beer pack discount");
        when(breadRule.description()).thenReturn("Bread age bundle discount");
        when(vegetableRule.description()).thenReturn("Vegetable weight tier discount");

        service = new DiscountRuleService(
            List.of(beerRule),
            List.of(breadRule),
            List.of(vegetableRule)
        );
    }

    @Test
    void shouldReturnAllDiscountRules() {
        List<DiscountRuleResponse> rules = service.getAllRules();

        assertThat(rules).hasSize(3);
        assertThat(rules).extracting(DiscountRuleResponse::productType)
            .containsExactlyInAnyOrder("BREAD", "VEGETABLE", "BEER");
    }

    @Test
    void shouldIncludeDescriptionsFromRules() {
        List<DiscountRuleResponse> rules = service.getAllRules();

        assertThat(rules).extracting(DiscountRuleResponse::description)
            .containsExactlyInAnyOrder(
                "Beer pack discount",
                "Bread age bundle discount",
                "Vegetable weight tier discount"
            );
    }

    @Test
    void shouldFilterByProductType() {
        List<DiscountRuleResponse> beerRules = service.getRulesByProductType(ProductType.BEER);

        assertThat(beerRules).hasSize(1);
        assertThat(beerRules.get(0).productType()).isEqualTo("BEER");
        assertThat(beerRules.get(0).description()).isEqualTo("Beer pack discount");
    }

    @Test
    void shouldHandleMultipleRulesPerType() {
        BeerDiscountRule secondBeerRule = mock(BeerDiscountRule.class);
        when(secondBeerRule.description()).thenReturn("Holiday beer discount");

        DiscountRuleService serviceWithMultipleRules = new DiscountRuleService(
            List.of(beerRule, secondBeerRule),
            List.of(breadRule),
            List.of(vegetableRule)
        );

        List<DiscountRuleResponse> rules = serviceWithMultipleRules.getAllRules();

        assertThat(rules).hasSize(4);
        assertThat(rules.stream().filter(r -> r.productType().equals("BEER")).count()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyListForTypeWithNoRules() {
        DiscountRuleService emptyService = new DiscountRuleService(
            List.of(),
            List.of(),
            List.of()
        );

        List<DiscountRuleResponse> rules = emptyService.getAllRules();

        assertThat(rules).isEmpty();
    }
}
```

## Files to Create

- `src/main/java/com/grocery/pricing/api/dto/DiscountRuleResponse.java`
- `src/main/java/com/grocery/pricing/service/DiscountRuleService.java`
- `src/test/java/com/grocery/pricing/service/DiscountRuleServiceTest.java`

## Acceptance Criteria

- [ ] DiscountRuleService auto-discovers all discount rules via Spring DI
- [ ] getAllRules() returns all rules with product type and description
- [ ] getRulesByProductType() filters rules correctly
- [ ] Descriptions come from rule implementations (self-documenting)
- [ ] All unit tests pass
