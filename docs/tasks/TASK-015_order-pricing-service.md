# TASK-015: Order Pricing Service

## Status

- [X] Completed

## Phase

Phase 3: Service Layer

## Description

Create OrderPricingService that orchestrates pricing strategies to calculate order totals.

## Implementation Details

### OrderPricingService

```java
package com.online.grocery.pricing.service;

import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.Order;
import com.online.grocery.pricing.domain.model.OrderItem;
import com.online.grocery.pricing.domain.model.Receipt;
import com.online.grocery.pricing.domain.model.ReceiptLine;
import com.online.grocery.pricing.pricing.strategy.PricingStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates pricing calculation for orders.
 * Delegates to product-specific pricing strategies.
 */
@Service
public class OrderPricingService {

    private final Map<ProductType, PricingStrategy> strategies;

    /**
     * Constructs the service with auto-discovered pricing strategies.
     * Spring injects all PricingStrategy implementations.
     *
     * @param strategyList All available pricing strategies
     */
    public OrderPricingService(List<PricingStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(
                PricingStrategy::getProductType,
                Function.identity()
            ));
    }

    /**
     * Calculate a complete receipt for an order.
     *
     * @param order The order containing items to price
     * @return Receipt with line items and totals
     * @throws IllegalStateException if no strategy found for a product type
     */
    public Receipt calculateReceipt(Order order) {
        // Group items by product type
        Map<ProductType, List<OrderItem>> itemsByType = order.getItems().stream()
            .collect(Collectors.groupingBy(OrderItem::getType));

        // Calculate for each product type
        List<ReceiptLine> allLines = itemsByType.entrySet().stream()
            .flatMap(entry -> {
                PricingStrategy strategy = strategies.get(entry.getKey());
                if (strategy == null) {
                    throw new IllegalStateException(
                        "No pricing strategy registered for product type: " + entry.getKey()
                    );
                }
                return strategy.calculatePrice(entry.getValue()).stream();
            })
            .toList();

        // Calculate totals
        BigDecimal subtotal = allLines.stream()
            .map(ReceiptLine::originalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = allLines.stream()
            .map(ReceiptLine::discount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = subtotal.subtract(totalDiscount);

        return new Receipt(allLines, subtotal, totalDiscount, total);
    }
}
```

### Unit Tests

```java
package com.online.grocery.pricing.service;

import com.online.grocery.pricing.domain.enums.BeerOrigin;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.*;
import com.online.grocery.pricing.pricing.strategy.PricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderPricingServiceTest {

    private PricingStrategy breadStrategy;
    private PricingStrategy vegetableStrategy;
    private PricingStrategy beerStrategy;
    private OrderPricingService service;

    @BeforeEach
    void setUp() {
        breadStrategy = mock(PricingStrategy.class);
        vegetableStrategy = mock(PricingStrategy.class);
        beerStrategy = mock(PricingStrategy.class);

        when(breadStrategy.getProductType()).thenReturn(ProductType.BREAD);
        when(vegetableStrategy.getProductType()).thenReturn(ProductType.VEGETABLE);
        when(beerStrategy.getProductType()).thenReturn(ProductType.BEER);

        service = new OrderPricingService(List.of(breadStrategy, vegetableStrategy, beerStrategy));
    }

    @Test
    void shouldCalculateReceiptForMixedOrder() {
        // Given
        BreadItem bread = new BreadItem("Bread", 3, 3);
        VegetableItem veg = new VegetableItem("Carrots", 200);
        BeerItem beer = new BeerItem("Heineken", 6, BeerOrigin.DUTCH);
        Order order = new Order(List.of(bread, veg, beer));

        when(breadStrategy.calculatePrice(anyList())).thenReturn(List.of(
            new ReceiptLine("3 x Bread (3 days old)", new BigDecimal("3.00"), new BigDecimal("1.00"), new BigDecimal("2.00"))
        ));
        when(vegetableStrategy.calculatePrice(anyList())).thenReturn(List.of(
            new ReceiptLine("200g Vegetables", new BigDecimal("2.00"), new BigDecimal("0.14"), new BigDecimal("1.86"))
        ));
        when(beerStrategy.calculatePrice(anyList())).thenReturn(List.of(
            new ReceiptLine("6 x DUTCH Beer (1 packs + 0 singles)", new BigDecimal("3.00"), new BigDecimal("2.00"), new BigDecimal("1.00"))
        ));

        // When
        Receipt receipt = service.calculateReceipt(order);

        // Then
        assertThat(receipt.lines()).hasSize(3);
        assertThat(receipt.subtotal()).isEqualByComparingTo("8.00");
        assertThat(receipt.totalDiscount()).isEqualByComparingTo("3.14");
        assertThat(receipt.total()).isEqualByComparingTo("4.86");
    }

    @Test
    void shouldDelegateToCorrectStrategy() {
        // Given
        BreadItem bread = new BreadItem("Bread", 1, 0);
        Order order = new Order(List.of(bread));

        when(breadStrategy.calculatePrice(anyList())).thenReturn(List.of(
            new ReceiptLine("1 x Bread", new BigDecimal("1.00"), BigDecimal.ZERO, new BigDecimal("1.00"))
        ));

        // When
        service.calculateReceipt(order);

        // Then
        verify(breadStrategy).calculatePrice(anyList());
        verify(vegetableStrategy, never()).calculatePrice(anyList());
        verify(beerStrategy, never()).calculatePrice(anyList());
    }

    @Test
    void shouldHandleSingleProductTypeOrder() {
        // Given
        VegetableItem veg1 = new VegetableItem("Carrots", 100);
        VegetableItem veg2 = new VegetableItem("Potatoes", 200);
        Order order = new Order(List.of(veg1, veg2));

        when(vegetableStrategy.calculatePrice(anyList())).thenReturn(List.of(
            new ReceiptLine("300g Vegetables", new BigDecimal("3.00"), new BigDecimal("0.21"), new BigDecimal("2.79"))
        ));

        // When
        Receipt receipt = service.calculateReceipt(order);

        // Then
        assertThat(receipt.lines()).hasSize(1);
        assertThat(receipt.total()).isEqualByComparingTo("2.79");
    }

    @Test
    void shouldCalculateCorrectTotals() {
        // Given
        BreadItem bread = new BreadItem("Bread", 2, 0);
        Order order = new Order(List.of(bread));

        when(breadStrategy.calculatePrice(anyList())).thenReturn(List.of(
            new ReceiptLine("2 x Bread (0 days old)", new BigDecimal("2.00"), BigDecimal.ZERO, new BigDecimal("2.00"))
        ));

        // When
        Receipt receipt = service.calculateReceipt(order);

        // Then
        assertThat(receipt.subtotal()).isEqualByComparingTo("2.00");
        assertThat(receipt.totalDiscount()).isEqualByComparingTo("0.00");
        assertThat(receipt.total()).isEqualByComparingTo("2.00");
    }
}
```

## Files to Create

- `src/main/java/com/online/grocery/pricing/service/OrderPricingService.java`
- `src/test/java/com/online/grocery/pricing/service/OrderPricingServiceTest.java`

## Acceptance Criteria

- [X] OrderPricingService auto-discovers strategies via Spring DI
- [X] Correctly groups items by ProductType
- [X] Delegates to appropriate strategy for each type
- [X] Correctly calculates subtotal, totalDiscount, and total
- [X] Throws IllegalStateException for unknown product types
- [X] All unit tests pass
