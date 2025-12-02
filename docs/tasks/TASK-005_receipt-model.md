# TASK-005: Receipt Model

## Status
- [x] Completed

## Phase
Phase 1: Foundation

## Description
Create Receipt and ReceiptLine records for representing pricing calculation results.

## Implementation Details

### ReceiptLine Record

```java
package com.grocery.pricing.domain.model;

import java.math.BigDecimal;

/**
 * Represents a single line item on a receipt.
 * 
 * @param description Human-readable description of the item
 * @param originalPrice Price before any discounts
 * @param discount Total discount applied to this line
 * @param finalPrice Price after discount (originalPrice - discount)
 */
public record ReceiptLine(
    String description,
    BigDecimal originalPrice,
    BigDecimal discount,
    BigDecimal finalPrice
) {
    public ReceiptLine {
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Final price cannot be negative");
        }
    }
}
```

### Receipt Record

```java
package com.grocery.pricing.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents a complete receipt with all line items and totals.
 * 
 * @param lines Individual line items on the receipt
 * @param subtotal Sum of all original prices
 * @param totalDiscount Sum of all discounts applied
 * @param total Final total (subtotal - totalDiscount)
 */
public record Receipt(
    List<ReceiptLine> lines,
    BigDecimal subtotal,
    BigDecimal totalDiscount,
    BigDecimal total
) {
    public Receipt {
        lines = List.copyOf(lines); // Defensive copy for immutability
    }
}
```

### MoneyUtils Utility Class

```java
package com.grocery.pricing.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for consistent money handling across all calculations.
 */
public final class MoneyUtils {
    
    private static final int CURRENCY_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private MoneyUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Normalize BigDecimal to 2 decimal places using HALF_UP rounding.
     * Applied to all final prices and discount calculations.
     * 
     * @param amount The amount to normalize
     * @return Normalized amount with 2 decimal places
     */
    public static BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(CURRENCY_SCALE, ROUNDING_MODE);
    }
}
```

### Rounding Policy

- All monetary calculations normalize to **2 decimal places**
- Rounding mode: **HALF_UP** (standard for financial systems)
- Applied at: Final price calculations, discount aggregations, receipt totals
- Example: €1.8667 → €1.87

### Usage Example

```java
// In pricing strategies
BigDecimal finalPrice = MoneyUtils.normalize(
    originalPrice.subtract(totalDiscount)
);

// Creating receipt line
return new ReceiptLine(
    description,
    MoneyUtils.normalize(originalPrice),
    MoneyUtils.normalize(totalDiscount),
    MoneyUtils.normalize(finalPrice)
);
```

## Files to Create

- `src/main/java/com/grocery/pricing/domain/model/ReceiptLine.java`
- `src/main/java/com/grocery/pricing/domain/model/Receipt.java`
- `src/main/java/com/grocery/pricing/domain/model/MoneyUtils.java`

## Acceptance Criteria

- [x] ReceiptLine record with validation for non-negative final price
- [x] Receipt record with defensive copy of lines
- [x] MoneyUtils utility class with normalize method
- [x] Consistent 2 decimal place rounding
- [x] HALF_UP rounding mode for financial accuracy
