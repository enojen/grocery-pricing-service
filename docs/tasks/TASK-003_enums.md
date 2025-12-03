# TASK-003: Enums

## Status

- [x] Completed

## Phase

Phase 1: Foundation

## Description

Create ProductType and BeerOrigin enums for type-safe product categorization.

## Implementation Details

### ProductType Enum

```java
package com.grocery.pricing.domain.enums;

/**
 * Enumeration of supported product types in the grocery pricing system.
 */
public enum ProductType {
    BREAD,
    VEGETABLE,
    BEER
}
```

### BeerOrigin Enum

```java
package com.grocery.pricing.domain.enums;

/**
 * Enumeration of beer origins with specific pricing rules.
 */
public enum BeerOrigin {
    BELGIAN,
    DUTCH,
    GERMAN
}
```

### Usage Context

These enums are used throughout the application:

- `ProductType` - Used by `OrderItem.getType()` to categorize items
- `BeerOrigin` - Used by `BeerItem` to determine origin-specific pricing

### Type Safety Benefits

- No string comparisons for product types
- Compile-time checking for invalid values
- Enhanced switch expressions in Java 17+:

```java
BigDecimal originBasePrice = switch (origin) {
    case BELGIAN -> beerRules.getBelgianBasePrice();
    case DUTCH -> beerRules.getDutchBasePrice();
    case GERMAN -> beerRules.getGermanBasePrice();
};
```

## Files to Create

- `src/main/java/com/grocery/pricing/domain/enums/ProductType.java`
- `src/main/java/com/grocery/pricing/domain/enums/BeerOrigin.java`

## Acceptance Criteria

- [x] ProductType enum with BREAD, VEGETABLE, BEER values
- [x] BeerOrigin enum with BELGIAN, DUTCH, GERMAN values
- [x] Proper package location under domain/enums
- [x] JavaDoc comments for documentation
