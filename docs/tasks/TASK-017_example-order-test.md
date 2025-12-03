# TASK-017: Example Order Test

## Status

- [x] Completed

## Phase

Phase 3: Service Layer

## Description

Create a critical integration test that validates the example order calculation produces the expected €4.86 total.

## Implementation Details

### Example Order Calculation

**Order Contents:**

- 3 x Bread (3 days old)
- 200g Vegetables
- 6 x Dutch Beer

**Expected Calculations:**

| Item                    | Original  | Discount  | Final     |
|-------------------------|-----------|-----------|-----------|
| Bread (3x, 3 days)      | €3.00     | €1.00     | €2.00     |
| Vegetables (200g)       | €2.00     | €0.14     | €1.86     |
| Dutch Beer (6x, 1 pack) | €3.00     | €2.00     | €1.00     |
| **TOTAL**               | **€8.00** | **€3.14** | **€4.86** |

### Bread Calculation Details

- 3 units × €1.00 = €3.00 original
- Age 3 days = "buy 1 take 2" discount
- 3 units ÷ 2 = 1 free item
- Discount: 1 × €1.00 = €1.00
- Final: €3.00 - €1.00 = €2.00

### Vegetable Calculation Details

- 200g × €0.01/g = €2.00 original
- 200g is in 100-499g tier = 7% discount
- Discount: €2.00 × 0.07 = €0.14
- Final: €2.00 - €0.14 = €1.86

### Beer Calculation Details

- 6 bottles × €0.50 (Dutch base) = €3.00 original
- 6 bottles = 1 complete pack
- Dutch pack discount: €2.00
- Final: €3.00 - €2.00 = €1.00

### ExampleOrderIntegrationTest

```java
package com.grocery.pricing.service;

import com.grocery.pricing.domain.enums.BeerOrigin;
import com.grocery.pricing.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Critical integration test validating the example order calculation.
 * This test ensures all pricing rules work together correctly.
 */
@SpringBootTest
class ExampleOrderIntegrationTest {

    @Autowired
    private OrderPricingService orderPricingService;

    @Test
    void shouldCalculateExampleOrderCorrectly() {
        // Given: Example order from requirements
        Order order = new Order(List.of(
                new BreadItem("Bread", 3, 3),                    // 3 breads, 3 days old
                new VegetableItem("Vegetables", 200),            // 200g vegetables
                new BeerItem("Dutch Beer", 6, BeerOrigin.DUTCH)  // 6 Dutch beers
        ));

        // When
        Receipt receipt = orderPricingService.calculateReceipt(order);

        // Then - Validate bread line
        ReceiptLine breadLine = findLineByDescription(receipt, "Bread");
        assertThat(breadLine.originalPrice()).isEqualByComparingTo("3.00");
        assertThat(breadLine.discount()).isEqualByComparingTo("1.00");
        assertThat(breadLine.finalPrice()).isEqualByComparingTo("2.00");

        // Then - Validate vegetables line
        ReceiptLine vegLine = findLineByDescription(receipt, "Vegetables");
        assertThat(vegLine.originalPrice()).isEqualByComparingTo("2.00");
        assertThat(vegLine.discount()).isEqualByComparingTo("0.14");
        assertThat(vegLine.finalPrice()).isEqualByComparingTo("1.86");

        // Then - Validate beer line
        ReceiptLine beerLine = findLineByDescription(receipt, "Beer");
        assertThat(beerLine.originalPrice()).isEqualByComparingTo("3.00");
        assertThat(beerLine.discount()).isEqualByComparingTo("2.00");
        assertThat(beerLine.finalPrice()).isEqualByComparingTo("1.00");

        // Then - Validate totals
        assertThat(receipt.subtotal()).isEqualByComparingTo("8.00");
        assertThat(receipt.totalDiscount()).isEqualByComparingTo("3.14");
        assertThat(receipt.total()).isEqualByComparingTo("4.86");
    }

    @Test
    void shouldCalculateBreadDiscountFor3DaysOld() {
        // Buy 1 take 2: For 3 breads, 1 is free
        Order order = new Order(List.of(
                new BreadItem("Bread", 3, 3)
        ));

        Receipt receipt = orderPricingService.calculateReceipt(order);

        assertThat(receipt.total()).isEqualByComparingTo("2.00");
    }

    @Test
    void shouldCalculateBreadDiscountFor6DaysOld() {
        // Buy 1 take 3: For 3 breads, 2 are free (1 group of 3)
        Order order = new Order(List.of(
                new BreadItem("Bread", 3, 6)
        ));

        Receipt receipt = orderPricingService.calculateReceipt(order);

        // 3 breads × €1.00 = €3.00, discount = 2 × €1.00 = €2.00
        assertThat(receipt.subtotal()).isEqualByComparingTo("3.00");
        assertThat(receipt.totalDiscount()).isEqualByComparingTo("2.00");
        assertThat(receipt.total()).isEqualByComparingTo("1.00");
    }

    @Test
    void shouldCalculateVegetableDiscount5PercentForSmallWeight() {
        // < 100g = 5% discount
        Order order = new Order(List.of(
                new VegetableItem("Carrots", 50)
        ));

        Receipt receipt = orderPricingService.calculateReceipt(order);

        // 50g × €0.01/g = €0.50, discount = 5% = €0.025 (rounded to €0.03)
        assertThat(receipt.subtotal()).isEqualByComparingTo("0.50");
        assertThat(receipt.total()).isEqualByComparingTo("0.48"); // 0.50 - 0.025 rounded
    }

    @Test
    void shouldCalculateVegetableDiscount10PercentForLargeWeight() {
        // >= 500g = 10% discount
        Order order = new Order(List.of(
                new VegetableItem("Potatoes", 500)
        ));

        Receipt receipt = orderPricingService.calculateReceipt(order);

        // 500g × €0.01/g = €5.00, discount = 10% = €0.50
        assertThat(receipt.subtotal()).isEqualByComparingTo("5.00");
        assertThat(receipt.totalDiscount()).isEqualByComparingTo("0.50");
        assertThat(receipt.total()).isEqualByComparingTo("4.50");
    }

    @Test
    void shouldCalculateBeerPackDiscountForBelgian() {
        // Belgian: 6 × €0.60 = €3.60, discount €3.00, final €0.60
        Order order = new Order(List.of(
                new BeerItem("Leffe", 6, BeerOrigin.BELGIAN)
        ));

        Receipt receipt = orderPricingService.calculateReceipt(order);

        assertThat(receipt.subtotal()).isEqualByComparingTo("3.60");
        assertThat(receipt.totalDiscount()).isEqualByComparingTo("3.00");
        assertThat(receipt.total()).isEqualByComparingTo("0.60");
    }

    @Test
    void shouldCalculateBeerPackDiscountForGerman() {
        // German: 6 × €0.80 = €4.80, discount €4.00, final €0.80
        Order order = new Order(List.of(
                new BeerItem("Beck's", 6, BeerOrigin.GERMAN)
        ));

        Receipt receipt = orderPricingService.calculateReceipt(order);

        assertThat(receipt.subtotal()).isEqualByComparingTo("4.80");
        assertThat(receipt.totalDiscount()).isEqualByComparingTo("4.00");
        assertThat(receipt.total()).isEqualByComparingTo("0.80");
    }

    @Test
    void shouldNotApplyBeerDiscountForSingles() {
        // 5 beers = 0 packs, 5 singles, no discount
        Order order = new Order(List.of(
                new BeerItem("Heineken", 5, BeerOrigin.DUTCH)
        ));

        Receipt receipt = orderPricingService.calculateReceipt(order);

        assertThat(receipt.subtotal()).isEqualByComparingTo("2.50");
        assertThat(receipt.totalDiscount()).isEqualByComparingTo("0.00");
        assertThat(receipt.total()).isEqualByComparingTo("2.50");
    }

    private ReceiptLine findLineByDescription(Receipt receipt, String keyword) {
        return receipt.lines().stream()
                .filter(line -> line.description().toLowerCase().contains(keyword.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No receipt line found containing: " + keyword
                ));
    }
}
```

## Files to Create

- `src/test/java/com/grocery/pricing/service/ExampleOrderIntegrationTest.java`

## Acceptance Criteria

- [x] Example order test passes with €4.86 total
- [x] Individual product type tests validate correct discount application
- [x] Bread: 3 units at 3 days = €2.00 final (€1.00 discount)
- [x] Vegetables: 200g = €1.86 final (€0.14 discount / 7%)
- [x] Beer: 6 Dutch = €1.00 final (€2.00 pack discount)
- [x] Test uses @SpringBootTest for full integration
- [x] All edge cases covered (boundary values, no discount scenarios)
