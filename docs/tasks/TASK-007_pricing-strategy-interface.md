# TASK-007: Pricing Strategy Interface

## Status
- [ ] Not Started

## Phase
Phase 2: Pricing Logic

## Description
Create the PricingStrategy interface that defines the contract for product-specific pricing logic.

## Implementation Details

### PricingStrategy Interface

```java
package com.grocery.pricing.pricing.strategy;

import com.grocery.pricing.domain.enums.ProductType;
import com.grocery.pricing.domain.model.OrderItem;
import com.grocery.pricing.domain.model.ReceiptLine;

import java.util.List;

/**
 * Strategy interface for product-specific pricing calculations.
 * Each product type (Bread, Beer, Vegetable) has its own implementation.
 * 
 * <p>Implementations are discovered by Spring DI and registered by product type
 * in OrderPricingService.</p>
 */
public interface PricingStrategy {
    
    /**
     * Returns the product type this strategy handles.
     * Used for automatic registration and dispatch.
     * 
     * @return The ProductType this strategy is responsible for
     */
    ProductType getProductType();
    
    /**
     * Calculate prices for a list of order items.
     * All items passed to this method will be of the type returned by getProductType().
     * 
     * @param items List of order items to price (pre-filtered by type)
     * @return List of receipt lines with pricing details
     */
    List<ReceiptLine> calculatePrice(List<OrderItem> items);
}
```

### Type-Safe Casting Helper

Each strategy implementation should include this helper method:

```java
/**
 * Safely cast OrderItems to the specific type.
 * Items are pre-filtered by OrderPricingService, but this provides defensive programming.
 */
@SuppressWarnings("unchecked")
private <T extends OrderItem> List<T> castToType(List<OrderItem> items, Class<T> type) {
    return items.stream()
        .filter(type::isInstance)
        .map(type::cast)
        .toList();
}
```

### Architecture Flow

```
OrderRequest → OrderController
                     ↓
              OrderPricingService
                     ↓
         ┌───────────┼───────────┐
         ↓           ↓           ↓
   BreadPricer  VegPricer   BeerPricer
         ↓           ↓           ↓
         └───────────┼───────────┘
                     ↓
                 Receipt
```

### Design Rationale

- **Strategy Pattern**: Each product type has dedicated pricing logic
- **Auto-discovery**: Spring DI automatically finds all implementations
- **Type-safe dispatch**: No string lookups or manual type checking
- **Single responsibility**: Each strategy handles one product type
- **Extensibility**: Adding new product type = new strategy implementation

## Files to Create

- `src/main/java/com/grocery/pricing/pricing/strategy/PricingStrategy.java`

## Acceptance Criteria

- [ ] PricingStrategy interface defined with getProductType() and calculatePrice() methods
- [ ] Proper JavaDoc documentation
- [ ] Interface is generic enough for all product types
- [ ] Ready for BreadPricingStrategy, VegetablePricingStrategy, BeerPricingStrategy implementations
