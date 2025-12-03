# TASK-029: Remove Name Fields from Products and Orders

## Status

- [x] Completed

## Phase

Phase 3: Business Rule Updates

## Description

Remove the `name` field from all product-related DTOs and domain records. The product type and other attributes are sufficient for identification.

## Current State

The following classes contain `name` fields that should be removed:

### DTOs
- `OrderItemRequest` - has `@NotBlank String name` field

### Domain Records
- `BreadItem` - has `String name` field
- `VegetableItem` - has `String name` field
- `BeerItem` - has `String name` field

### Interface
- `OrderItem` - has `String getName()` method

## Implementation Details

### OrderItemRequest Changes

```java
public record OrderItemRequest(
        @NotNull(message = "Product type required")
        ProductType type,

        // Remove: @NotBlank(message = "Item name required") String name,

        @Positive(message = "Quantity must be positive")
        Integer quantity,

        @Min(value = 0, message = "Age cannot be negative")
        @Max(value = 6, message = "Bread older than 6 days not allowed")
        Integer daysOld,

        @Positive(message = "Weight must be positive")
        Integer weightGrams,

        BeerOrigin origin
) {
}
```

### Domain Record Changes

```java
// BreadItem
public record BreadItem(
        int quantity,
        int daysOld
) implements OrderItem { }

// VegetableItem
public record VegetableItem(
        int weightGrams
) implements OrderItem { }

// BeerItem
public record BeerItem(
        int quantity,
        BeerOrigin origin
) implements OrderItem { }
```

### OrderItem Interface Changes

Remove `getName()` method from the interface, or replace with `getProductType()` if needed for identification.

### OrderMapper Changes

Update mapping logic to not require/use the `name` field.

## Files to Modify

- `src/main/java/com/online/grocery/pricing/api/dto/OrderItemRequest.java`
- `src/main/java/com/online/grocery/pricing/domain/model/BreadItem.java`
- `src/main/java/com/online/grocery/pricing/domain/model/VegetableItem.java`
- `src/main/java/com/online/grocery/pricing/domain/model/BeerItem.java`
- `src/main/java/com/online/grocery/pricing/domain/model/OrderItem.java`
- `src/main/java/com/online/grocery/pricing/api/mapper/OrderMapper.java`
- All related test files

## Acceptance Criteria

- [x] `name` field removed from `OrderItemRequest`
- [x] `name` field removed from `BreadItem`, `VegetableItem`, `BeerItem`
- [x] `getName()` method removed from `OrderItem` interface
- [x] `OrderMapper` updated to work without name field
- [x] All unit tests updated and passing
- [x] All integration tests updated and passing
- [x] API documentation (OpenAPI) updated to reflect changes
