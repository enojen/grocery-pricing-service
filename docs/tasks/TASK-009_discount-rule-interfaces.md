# TASK-009: Discount Rule Interfaces

## Status
- [ ] Not Started

## Phase
Phase 2: Pricing Logic

## Description
Create interfaces for pluggable discount rules for each product type.

## Implementation Details

### BeerDiscountRule Interface

```java
package com.grocery.pricing.pricing.discount;

import com.grocery.pricing.pricing.context.BeerPricingContext;

import java.math.BigDecimal;

/**
 * Interface for beer discount rules.
 * Implementations are auto-discovered by Spring and applied by BeerPricingStrategy.
 */
public interface BeerDiscountRule {
    
    /**
     * Check if this discount rule applies to the given context.
     * 
     * @param ctx Beer pricing context with all relevant data
     * @return true if this rule should be applied
     */
    boolean isApplicable(BeerPricingContext ctx);

    /**
     * Calculate the discount amount for this rule.
     * Only called if isApplicable() returns true.
     * 
     * @param ctx Beer pricing context with all relevant data
     * @return Discount amount to subtract from original price
     */
    BigDecimal calculateDiscount(BeerPricingContext ctx);

    /**
     * Order of execution (lower numbers execute first).
     * Use values like 100, 200, 300 to allow insertion between rules.
     * 
     * @return Execution order priority
     */
    int order();

    /**
     * Human-readable description of this discount rule.
     * Used by GET /discounts/rules endpoint.
     * 
     * @return Description for API documentation
     */
    String description();
}
```

### BreadDiscountRule Interface

```java
package com.grocery.pricing.pricing.discount;

import com.grocery.pricing.pricing.context.BreadPricingContext;

import java.math.BigDecimal;

/**
 * Interface for bread discount rules.
 * Implementations are auto-discovered by Spring and applied by BreadPricingStrategy.
 */
public interface BreadDiscountRule {
    
    /**
     * Check if this discount rule applies to the given context.
     * 
     * @param ctx Bread pricing context with age, quantity, and price data
     * @return true if this rule should be applied
     */
    boolean isApplicable(BreadPricingContext ctx);

    /**
     * Calculate the discount amount for this rule.
     * Only called if isApplicable() returns true.
     * 
     * @param ctx Bread pricing context with age, quantity, and price data
     * @return Discount amount to subtract from original price
     */
    BigDecimal calculateDiscount(BreadPricingContext ctx);

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
```

### VegetableDiscountRule Interface

```java
package com.grocery.pricing.pricing.discount;

import com.grocery.pricing.pricing.context.VegetablePricingContext;

import java.math.BigDecimal;

/**
 * Interface for vegetable discount rules.
 * Implementations are auto-discovered by Spring and applied by VegetablePricingStrategy.
 */
public interface VegetableDiscountRule {
    
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
```

### Design Principles

**Pluggable Architecture**:
- New discount = new class implementing the interface
- No changes to existing strategies needed
- Spring auto-discovers all `@Component` implementations

**Open/Closed Principle**:
- Open for extension (new rules)
- Closed for modification (existing code unchanged)

**Self-Documenting**:
- `description()` method provides API documentation automatically
- GET /discounts/rules endpoint reads from implementations

### Adding a New Discount Rule (Example)

```java
@Component
public class NewYearBeerPromoRule implements BeerDiscountRule {

    @Override
    public boolean isApplicable(BeerPricingContext ctx) {
        LocalDate now = LocalDate.now();
        return now.getMonth() == Month.JANUARY && now.getDayOfMonth() <= 7;
    }

    @Override
    public BigDecimal calculateDiscount(BeerPricingContext ctx) {
        return ctx.originalPrice().multiply(new BigDecimal("0.20")); // 20% off
    }

    @Override
    public int order() {
        return 200; // After pack discounts
    }

    @Override
    public String description() {
        return "New Year promotion: 20% off all beers (Jan 1-7)";
    }
}
```

## Files to Create

- `src/main/java/com/grocery/pricing/pricing/discount/BeerDiscountRule.java`
- `src/main/java/com/grocery/pricing/pricing/discount/BreadDiscountRule.java`
- `src/main/java/com/grocery/pricing/pricing/discount/VegetableDiscountRule.java`

## Acceptance Criteria

- [ ] BeerDiscountRule interface with isApplicable, calculateDiscount, order, description
- [ ] BreadDiscountRule interface with same method signatures
- [ ] VegetableDiscountRule interface with same method signatures
- [ ] Proper JavaDoc documentation for all methods
- [ ] Ready for concrete implementations
