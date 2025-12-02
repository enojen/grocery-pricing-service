# TASK-018: DTO Classes

## Status
- [x] Completed

## Phase
Phase 4: REST API

## Description
Create Request/Response DTOs for the REST API with validation annotations.

## Implementation Details

### OrderItemRequest

```java
package com.online.grocery.pricing.api.dto;

import com.online.grocery.pricing.domain.enums.BeerOrigin;
import com.online.grocery.pricing.domain.enums.ProductType;
import jakarta.validation.constraints.*;

/**
 * Unified request DTO for all product types.
 * Type-specific validation is handled by OrderMapper.
 */
public record OrderItemRequest(
    @NotNull(message = "Product type required")
    ProductType type,

    @NotBlank(message = "Item name required")
    String name,

    @Positive(message = "Quantity must be positive")
    Integer quantity,

    @Min(value = 0, message = "Age cannot be negative")
    @Max(value = 6, message = "Bread older than 6 days not allowed")
    Integer daysOld,

    @Positive(message = "Weight must be positive")
    Integer weightGrams,

    BeerOrigin origin
) {}
```

### OrderRequest

```java
package com.online.grocery.pricing.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for order calculation endpoint.
 */
public record OrderRequest(
    @NotEmpty(message = "At least one item required")
    @Valid
    List<OrderItemRequest> items
) {}
```

### ReceiptLineResponse

```java
package com.online.grocery.pricing.api.dto;

import java.math.BigDecimal;

/**
 * Response DTO for a single receipt line item.
 */
public record ReceiptLineResponse(
    String description,
    BigDecimal originalPrice,
    BigDecimal discount,
    BigDecimal finalPrice
) {}
```

### ReceiptResponse

```java
package com.online.grocery.pricing.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for the complete receipt.
 */
public record ReceiptResponse(
    List<ReceiptLineResponse> lines,
    BigDecimal subtotal,
    BigDecimal totalDiscount,
    BigDecimal total
) {}
```

### PriceInfoResponse

```java
package com.online.grocery.pricing.api.dto;

import java.math.BigDecimal;

/**
 * Response DTO for product price information.
 */
public record PriceInfoResponse(
    String productName,
    BigDecimal price,
    String unit
) {}
```

### Example Request JSON

```json
{
  "items": [
    {
      "type": "BREAD",
      "name": "Sourdough",
      "quantity": 3,
      "daysOld": 3,
      "weightGrams": null,
      "origin": null
    },
    {
      "type": "VEGETABLE",
      "name": "Carrots",
      "quantity": null,
      "daysOld": null,
      "weightGrams": 200,
      "origin": null
    },
    {
      "type": "BEER",
      "name": "Heineken",
      "quantity": 6,
      "daysOld": null,
      "weightGrams": null,
      "origin": "DUTCH"
    }
  ]
}
```

### Example Response JSON

```json
{
  "lines": [
    {
      "description": "3 x Bread (3 days old)",
      "originalPrice": 3.00,
      "discount": 1.00,
      "finalPrice": 2.00
    },
    {
      "description": "200g Vegetables",
      "originalPrice": 2.00,
      "discount": 0.14,
      "finalPrice": 1.86
    },
    {
      "description": "6 x Dutch Beer (1 packs + 0 singles)",
      "originalPrice": 3.00,
      "discount": 2.00,
      "finalPrice": 1.00
    }
  ],
  "subtotal": 8.00,
  "totalDiscount": 3.14,
  "total": 4.86
}
```

### Design Rationale

- **Unified OrderItemRequest**: All product types in one DTO for API flexibility
- **Nullable fields**: Type-specific validation in OrderMapper
- **Bean Validation**: Standard @Valid annotations for automatic validation
- **Record types**: Immutable DTOs with minimal boilerplate

## Files to Create

- `src/main/java/com/online/grocery/pricing/api/dto/OrderItemRequest.java`
- `src/main/java/com/online/grocery/pricing/api/dto/OrderRequest.java`
- `src/main/java/com/online/grocery/pricing/api/dto/ReceiptLineResponse.java`
- `src/main/java/com/online/grocery/pricing/api/dto/ReceiptResponse.java`
- `src/main/java/com/online/grocery/pricing/api/dto/PriceInfoResponse.java`

## Acceptance Criteria

- [x] All DTOs created as Java records
- [x] OrderItemRequest with validation annotations
- [x] OrderRequest with @NotEmpty validation
- [x] Response DTOs for receipt and price info
- [x] Bean validation annotations properly configured
