# TASK-032: Dynamic Discount Rules Discovery

## Status

- [x] Completed

## Phase

Phase 6: Maintenance & Improvements

## Description

Refactor `DiscountRuleService` to dynamically discover all discount rules instead of maintaining hardcoded dependencies for each product type. Currently, adding a new discount rule type requires manual updates to `DiscountRuleService`.

## Current Implementation (to be replaced)

```java
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
    // ...
}
```

## New Implementation

### 1. Create Base Interface

Create `DiscountRule.java` as a common marker interface:

```java
public interface DiscountRule {
    ProductType productType();
    String description();
}
```

### 2. Extend Existing Interfaces

Update each product-specific discount rule interface to extend `DiscountRule`:

- `BeerDiscountRule extends DiscountRule`
- `BreadDiscountRule extends DiscountRule`
- `VegetableDiscountRule extends DiscountRule`

Add default `productType()` method to each interface returning the appropriate `ProductType`.

### 3. Refactor DiscountRuleService

```java
@Service
public class DiscountRuleService {
    private final List<DiscountRule> allRules;

    public DiscountRuleService(List<DiscountRule> allRules) {
        this.allRules = allRules;
    }

    public List<DiscountRuleResponse> getAllRules() {
        return allRules.stream()
            .map(rule -> new DiscountRuleResponse(
                rule.productType().name(),
                rule.description()
            ))
            .toList();
    }
}
```

## Files to Create

- `src/main/java/com/online/grocery/pricing/pricing/discount/DiscountRule.java`

## Files to Modify

- `src/main/java/com/online/grocery/pricing/pricing/discount/BeerDiscountRule.java`
- `src/main/java/com/online/grocery/pricing/pricing/discount/BreadDiscountRule.java`
- `src/main/java/com/online/grocery/pricing/pricing/discount/VegetableDiscountRule.java`
- `src/main/java/com/online/grocery/pricing/service/DiscountRuleService.java`
- `src/test/java/com/online/grocery/pricing/service/DiscountRuleServiceTest.java`

## Acceptance Criteria

- [x] `DiscountRule` base interface created with `productType()` and `description()` methods
- [x] All product-specific discount rule interfaces extend `DiscountRule`
- [x] `DiscountRuleService` uses single `List<DiscountRule>` injection
- [x] GET `/api/v1/discounts/rules` returns same response as before
- [x] Adding a new discount rule only requires creating a `@Component` class
- [x] All existing unit tests pass
- [x] All integration tests pass

## Benefits

- **Open/Closed Principle**: New discount rules can be added without modifying `DiscountRuleService`
- **Reduced Coupling**: Service no longer needs to know about specific discount rule types
- **Easier Extension**: Future product types automatically supported
