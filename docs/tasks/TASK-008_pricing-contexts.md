# TASK-008: Pricing Contexts

## Status

- [x] Completed

## Phase

Phase 2: Pricing Logic

## Description

Create context records that encapsulate all data needed for pricing and discount calculations.

## Implementation Details

### BeerPricingContext

```java
package com.grocery.pricing.pricing.context;

import com.grocery.pricing.domain.enums.BeerOrigin;

import java.math.BigDecimal;

/**
 * Context for beer pricing calculations.
 * Encapsulates all data needed by beer discount rules.
 *
 * @param origin Beer origin (BELGIAN, DUTCH, GERMAN)
 * @param totalBottles Total number of bottles in order
 * @param packs Number of complete 6-packs
 * @param singles Number of individual bottles (not in packs)
 * @param originBasePrice Base price per bottle for this origin
 * @param originalPrice Total price before discounts
 */
public record BeerPricingContext(
        BeerOrigin origin,
        int totalBottles,
        int packs,
        int singles,
        BigDecimal originBasePrice,
        BigDecimal originalPrice
) {
}
```

### BreadPricingContext

```java
package com.grocery.pricing.pricing.context;

import java.math.BigDecimal;

/**
 * Context for bread pricing calculations.
 * Encapsulates all data needed by bread discount rules.
 *
 * @param age Age of bread in days (0-6)
 * @param totalQuantity Total number of bread units
 * @param unitPrice Price per bread unit
 * @param originalPrice Total price before discounts
 */
public record BreadPricingContext(
        int age,
        int totalQuantity,
        BigDecimal unitPrice,
        BigDecimal originalPrice
) {
}
```

### VegetablePricingContext

```java
package com.grocery.pricing.pricing.context;

import java.math.BigDecimal;

/**
 * Context for vegetable pricing calculations.
 * Encapsulates all data needed by vegetable discount rules.
 *
 * @param totalWeightGrams Total weight of all vegetables in grams
 * @param pricePerGram Price per gram (derived from price per 100g)
 * @param originalPrice Total price before discounts
 */
public record VegetablePricingContext(
        int totalWeightGrams,
        BigDecimal pricePerGram,
        BigDecimal originalPrice
) {
}
```

### Usage in Discount Rules

The context objects are passed to discount rules to provide all necessary data:

```java
// In BeerPricingStrategy
BeerPricingContext ctx = new BeerPricingContext(
        origin,
        totalBottles,
        packs,
        singles,
        originBasePrice,
        originalPrice
);

// Apply discount rules
BigDecimal totalDiscount = discountRules.stream()
        .filter(rule -> rule.isApplicable(ctx))
        .map(rule -> rule.calculateDiscount(ctx))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
```

### Design Rationale

- **Immutable Records**: All data is read-only after creation
- **Self-contained**: Each context has all data needed for its product type
- **Decoupled**: Discount rules don't need to know about configuration or services
- **Testable**: Easy to create contexts for unit testing discount rules

## Files to Create

- `src/main/java/com/grocery/pricing/pricing/context/BeerPricingContext.java`
- `src/main/java/com/grocery/pricing/pricing/context/BreadPricingContext.java`
- `src/main/java/com/grocery/pricing/pricing/context/VegetablePricingContext.java`

## Acceptance Criteria

- [x] BeerPricingContext with origin, bottles, packs, singles, prices
- [x] BreadPricingContext with age, quantity, unit price, original price
- [x] VegetablePricingContext with weight, price per gram, original price
- [x] All contexts are immutable records
- [x] Proper JavaDoc documentation
