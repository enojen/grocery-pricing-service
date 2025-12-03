# TASK-004: Domain Records

## Status

- [x] Completed

## Phase

Phase 1: Foundation

## Description

Create the OrderItem interface and implementing records (BreadItem, VegetableItem, BeerItem) with validation.

## Implementation Details

### OrderItem Interface

```java
package com.grocery.pricing.domain.model;

import com.grocery.pricing.domain.enums.ProductType;

/**
 * Base interface for all order items.
 */
public interface OrderItem {
    ProductType getType();
    String getName();
}
```

### BreadItem Record

```java
package com.grocery.pricing.domain.model;

import com.grocery.pricing.domain.enums.ProductType;
import com.grocery.pricing.exception.InvalidOrderException;

/**
 * Represents bread items in an order.
 *
 * @param name Item name/description
 * @param quantity Number of bread units
 * @param daysOld Age of bread in days (0-6 valid)
 */
public record BreadItem(
        String name,
        int quantity,
        int daysOld
) implements OrderItem {

    public BreadItem {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (daysOld < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
        if (daysOld > 6) {
            throw new InvalidOrderException("Bread older than 6 days cannot be ordered");
        }
    }

    @Override
    public ProductType getType() {
        return ProductType.BREAD;
    }
}
```

### VegetableItem Record

```java
package com.grocery.pricing.domain.model;

import com.grocery.pricing.domain.enums.ProductType;

/**
 * Represents vegetable items in an order.
 *
 * @param name Item name/description
 * @param weightGrams Weight of vegetables in grams
 */
public record VegetableItem(
        String name,
        int weightGrams
) implements OrderItem {

    public VegetableItem {
        if (weightGrams <= 0) {
            throw new IllegalArgumentException("Weight must be positive");
        }
    }

    @Override
    public ProductType getType() {
        return ProductType.VEGETABLE;
    }
}
```

### BeerItem Record

```java
package com.grocery.pricing.domain.model;

import com.grocery.pricing.domain.enums.BeerOrigin;
import com.grocery.pricing.domain.enums.ProductType;

import java.util.Objects;

/**
 * Represents beer items in an order.
 *
 * @param name Item name/description
 * @param quantity Number of bottles
 * @param origin Beer origin (BELGIAN, DUTCH, GERMAN)
 */
public record BeerItem(
        String name,
        int quantity,
        BeerOrigin origin
) implements OrderItem {

    public BeerItem {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Objects.requireNonNull(origin, "Beer origin required");
    }

    @Override
    public ProductType getType() {
        return ProductType.BEER;
    }
}
```

### Order Aggregate

```java
package com.grocery.pricing.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents a customer order containing multiple items.
 */
public record Order(List<OrderItem> items) {
    
    public Order {
        Objects.requireNonNull(items, "Items cannot be null");
        items = List.copyOf(items); // Defensive copy for immutability
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
}
```

### Design Rationale

- **Records** = immutable, clean, minimal boilerplate
- **Validation in compact constructor** = fail-fast on invalid data
- **Type-safe** = no stringly-typed data
- **No sealed types** = Java 17 compatible

## Files to Create

- `src/main/java/com/grocery/pricing/domain/model/OrderItem.java`
- `src/main/java/com/grocery/pricing/domain/model/BreadItem.java`
- `src/main/java/com/grocery/pricing/domain/model/VegetableItem.java`
- `src/main/java/com/grocery/pricing/domain/model/BeerItem.java`
- `src/main/java/com/grocery/pricing/domain/model/Order.java`

## Acceptance Criteria

- [x] OrderItem interface with getType() and getName() methods
- [x] BreadItem record with quantity and daysOld validation
- [x] VegetableItem record with weightGrams validation
- [x] BeerItem record with quantity and origin validation
- [x] Order aggregate with defensive copy
- [x] All validation throws appropriate exceptions
