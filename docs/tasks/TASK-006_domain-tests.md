# TASK-006: Domain Tests

## Status

- [x] Completed

## Phase

Phase 1: Foundation

## Description

Write comprehensive unit tests for all domain models including validation and immutability verification.

## Implementation Details

### BreadItemTest

```java
package com.grocery.pricing.domain.model;

import com.grocery.pricing.domain.enums.ProductType;
import com.grocery.pricing.exception.InvalidOrderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class BreadItemTest {

    @Test
    void shouldCreateValidBreadItem() {
        BreadItem bread = new BreadItem("Sourdough", 3, 2);

        assertThat(bread.name()).isEqualTo("Sourdough");
        assertThat(bread.quantity()).isEqualTo(3);
        assertThat(bread.daysOld()).isEqualTo(2);
        assertThat(bread.getType()).isEqualTo(ProductType.BREAD);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void shouldRejectNonPositiveQuantity(int quantity) {
        assertThatThrownBy(() -> new BreadItem("Bread", quantity, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be positive");
    }

    @Test
    void shouldRejectNegativeAge() {
        assertThatThrownBy(() -> new BreadItem("Bread", 1, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Age cannot be negative");
    }

    @Test
    void shouldRejectBreadOlderThan6Days() {
        assertThatThrownBy(() -> new BreadItem("Bread", 1, 7))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("Bread older than 6 days");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6})
    void shouldAcceptValidAges(int age) {
        BreadItem bread = new BreadItem("Bread", 1, age);
        assertThat(bread.daysOld()).isEqualTo(age);
    }
}
```

### VegetableItemTest

```java
package com.grocery.pricing.domain.model;

import com.grocery.pricing.domain.enums.ProductType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class VegetableItemTest {

    @Test
    void shouldCreateValidVegetableItem() {
        VegetableItem vegetable = new VegetableItem("Carrots", 200);
        
        assertThat(vegetable.name()).isEqualTo("Carrots");
        assertThat(vegetable.weightGrams()).isEqualTo(200);
        assertThat(vegetable.getType()).isEqualTo(ProductType.VEGETABLE);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void shouldRejectNonPositiveWeight(int weight) {
        assertThatThrownBy(() -> new VegetableItem("Carrots", weight))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Weight must be positive");
    }

    @Test
    void shouldAcceptMinimumWeight() {
        VegetableItem vegetable = new VegetableItem("Lettuce", 1);
        assertThat(vegetable.weightGrams()).isEqualTo(1);
    }
}
```

### BeerItemTest

```java
package com.grocery.pricing.domain.model;

import com.grocery.pricing.domain.enums.BeerOrigin;
import com.grocery.pricing.domain.enums.ProductType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class BeerItemTest {

    @Test
    void shouldCreateValidBeerItem() {
        BeerItem beer = new BeerItem("Heineken", 6, BeerOrigin.DUTCH);

        assertThat(beer.name()).isEqualTo("Heineken");
        assertThat(beer.quantity()).isEqualTo(6);
        assertThat(beer.origin()).isEqualTo(BeerOrigin.DUTCH);
        assertThat(beer.getType()).isEqualTo(ProductType.BEER);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void shouldRejectNonPositiveQuantity(int quantity) {
        assertThatThrownBy(() -> new BeerItem("Beer", quantity, BeerOrigin.BELGIAN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be positive");
    }

    @Test
    void shouldRejectNullOrigin() {
        assertThatThrownBy(() -> new BeerItem("Beer", 6, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Beer origin required");
    }

    @ParameterizedTest
    @EnumSource(BeerOrigin.class)
    void shouldAcceptAllOrigins(BeerOrigin origin) {
        BeerItem beer = new BeerItem("Beer", 1, origin);
        assertThat(beer.origin()).isEqualTo(origin);
    }
}
```

### OrderTest

```java
package com.grocery.pricing.domain.model;

import com.grocery.pricing.domain.enums.BeerOrigin;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    @Test
    void shouldCreateValidOrder() {
        List<OrderItem> items = List.of(
            new BreadItem("Bread", 2, 1),
            new VegetableItem("Carrots", 150),
            new BeerItem("Heineken", 6, BeerOrigin.DUTCH)
        );
        
        Order order = new Order(items);
        
        assertThat(order.getItems()).hasSize(3);
    }

    @Test
    void shouldRejectNullItems() {
        assertThatThrownBy(() -> new Order(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldCreateDefensiveCopy() {
        List<OrderItem> mutableList = new ArrayList<>();
        mutableList.add(new BreadItem("Bread", 1, 0));
        
        Order order = new Order(mutableList);
        
        // Modifying original list should not affect order
        mutableList.add(new BreadItem("More Bread", 1, 0));
        
        assertThat(order.getItems()).hasSize(1);
    }

    @Test
    void shouldReturnImmutableList() {
        Order order = new Order(List.of(new BreadItem("Bread", 1, 0)));
        
        assertThatThrownBy(() -> order.getItems().add(new BreadItem("More", 1, 0)))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
```

### ReceiptLineTest

```java
package com.grocery.pricing.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class ReceiptLineTest {

    @Test
    void shouldCreateValidReceiptLine() {
        ReceiptLine line = new ReceiptLine(
                "3 x Bread",
                new BigDecimal("3.00"),
                new BigDecimal("1.00"),
                new BigDecimal("2.00")
        );

        assertThat(line.description()).isEqualTo("3 x Bread");
        assertThat(line.originalPrice()).isEqualByComparingTo("3.00");
        assertThat(line.discount()).isEqualByComparingTo("1.00");
        assertThat(line.finalPrice()).isEqualByComparingTo("2.00");
    }

    @Test
    void shouldRejectNegativeFinalPrice() {
        assertThatThrownBy(() -> new ReceiptLine(
                "Item",
                new BigDecimal("1.00"),
                new BigDecimal("2.00"),
                new BigDecimal("-1.00")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Final price cannot be negative");
    }

    @Test
    void shouldAcceptZeroFinalPrice() {
        ReceiptLine line = new ReceiptLine(
                "Free Item",
                new BigDecimal("1.00"),
                new BigDecimal("1.00"),
                BigDecimal.ZERO
        );

        assertThat(line.finalPrice()).isEqualByComparingTo("0.00");
    }
}
```

### MoneyUtilsTest

```java
package com.grocery.pricing.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class MoneyUtilsTest {

    @ParameterizedTest
    @CsvSource({
            "1.8667, 1.87",      // Round up
            "1.8647, 1.86",      // Round down
            "1.865, 1.87",       // Half up
            "1.8650, 1.87",      // Half up exact
            "1.8, 1.80",         // Add trailing zero
            "1, 1.00",           // Integer to 2 decimals
            "0.005, 0.01",       // Small value round up
            "0.004, 0.00"        // Small value round down
    })
    void shouldNormalizeToTwoDecimalPlaces(String input, String expected) {
        BigDecimal result = MoneyUtils.normalize(new BigDecimal(input));
        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    void shouldReturnZeroForNull() {
        BigDecimal result = MoneyUtils.normalize(null);
        assertThat(result).isEqualByComparingTo("0.00");
    }

    @Test
    void shouldHandleZero() {
        BigDecimal result = MoneyUtils.normalize(BigDecimal.ZERO);
        assertThat(result).isEqualByComparingTo("0.00");
    }
}
```

## Files to Create

- `src/test/java/com/grocery/pricing/domain/model/BreadItemTest.java`
- `src/test/java/com/grocery/pricing/domain/model/VegetableItemTest.java`
- `src/test/java/com/grocery/pricing/domain/model/BeerItemTest.java`
- `src/test/java/com/grocery/pricing/domain/model/OrderTest.java`
- `src/test/java/com/grocery/pricing/domain/model/ReceiptLineTest.java`
- `src/test/java/com/grocery/pricing/domain/model/MoneyUtilsTest.java`

## Acceptance Criteria

- [x] All domain records have comprehensive tests
- [x] Validation edge cases covered
- [x] Immutability verified for Order and Receipt
- [x] MoneyUtils rounding behavior tested
- [x] All tests pass with `mvn test`
