# TASK-019: Order Mapper

## Status
- [ ] Not Started

## Phase
Phase 4: REST API

## Description
Create OrderMapper component for converting DTOs to domain models with type-specific validation.

## Implementation Details

### OrderMapper

```java
package com.grocery.pricing.api.mapper;

import com.grocery.pricing.api.dto.OrderItemRequest;
import com.grocery.pricing.api.dto.OrderRequest;
import com.grocery.pricing.domain.model.*;
import com.grocery.pricing.exception.InvalidOrderException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting API DTOs to domain models.
 * Handles type-specific validation for OrderItemRequest.
 */
@Component
public class OrderMapper {

    /**
     * Convert OrderRequest DTO to domain Order model.
     * Validates type-specific field requirements before conversion.
     *
     * @param request The order request from API
     * @return Domain Order object
     * @throws InvalidOrderException if required fields for type are missing
     */
    public Order mapToOrder(OrderRequest request) {
        List<OrderItem> items = request.items().stream()
            .map(this::mapToOrderItem)
            .toList();

        return new Order(items);
    }

    /**
     * Convert single OrderItemRequest to appropriate OrderItem domain model.
     * Throws InvalidOrderException if required fields for type are missing.
     */
    private OrderItem mapToOrderItem(OrderItemRequest itemRequest) {
        // Validate type-specific required fields
        validateItemRequest(itemRequest);

        return switch (itemRequest.type()) {
            case BREAD -> new BreadItem(
                itemRequest.name(),
                itemRequest.quantity(),
                itemRequest.daysOld()
            );
            case VEGETABLE -> new VegetableItem(
                itemRequest.name(),
                itemRequest.weightGrams()
            );
            case BEER -> new BeerItem(
                itemRequest.name(),
                itemRequest.quantity(),
                itemRequest.origin()
            );
        };
    }

    /**
     * Validate that required fields for the product type are present.
     * Throws InvalidOrderException with descriptive error message.
     */
    private void validateItemRequest(OrderItemRequest request) {
        switch (request.type()) {
            case BREAD:
                if (request.quantity() == null) {
                    throw new InvalidOrderException(
                        "quantity field required for product type BREAD"
                    );
                }
                if (request.daysOld() == null) {
                    throw new InvalidOrderException(
                        "daysOld field required for product type BREAD"
                    );
                }
                break;

            case VEGETABLE:
                if (request.weightGrams() == null) {
                    throw new InvalidOrderException(
                        "weightGrams field required for product type VEGETABLE"
                    );
                }
                break;

            case BEER:
                if (request.quantity() == null) {
                    throw new InvalidOrderException(
                        "quantity field required for product type BEER"
                    );
                }
                if (request.origin() == null) {
                    throw new InvalidOrderException(
                        "origin field required for product type BEER"
                    );
                }
                break;
        }
    }
}
```

### Unit Tests

```java
package com.grocery.pricing.api.mapper;

import com.grocery.pricing.api.dto.OrderItemRequest;
import com.grocery.pricing.api.dto.OrderRequest;
import com.grocery.pricing.domain.enums.BeerOrigin;
import com.grocery.pricing.domain.enums.ProductType;
import com.grocery.pricing.domain.model.*;
import com.grocery.pricing.exception.InvalidOrderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class OrderMapperTest {

    private OrderMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderMapper();
    }

    @Test
    void shouldMapBreadItemCorrectly() {
        OrderItemRequest itemRequest = new OrderItemRequest(
            ProductType.BREAD, "Sourdough", 3, 2, null, null
        );
        OrderRequest request = new OrderRequest(List.of(itemRequest));

        Order order = mapper.mapToOrder(request);

        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0)).isInstanceOf(BreadItem.class);
        BreadItem bread = (BreadItem) order.getItems().get(0);
        assertThat(bread.name()).isEqualTo("Sourdough");
        assertThat(bread.quantity()).isEqualTo(3);
        assertThat(bread.daysOld()).isEqualTo(2);
    }

    @Test
    void shouldMapVegetableItemCorrectly() {
        OrderItemRequest itemRequest = new OrderItemRequest(
            ProductType.VEGETABLE, "Carrots", null, null, 200, null
        );
        OrderRequest request = new OrderRequest(List.of(itemRequest));

        Order order = mapper.mapToOrder(request);

        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0)).isInstanceOf(VegetableItem.class);
        VegetableItem veg = (VegetableItem) order.getItems().get(0);
        assertThat(veg.name()).isEqualTo("Carrots");
        assertThat(veg.weightGrams()).isEqualTo(200);
    }

    @Test
    void shouldMapBeerItemCorrectly() {
        OrderItemRequest itemRequest = new OrderItemRequest(
            ProductType.BEER, "Heineken", 6, null, null, BeerOrigin.DUTCH
        );
        OrderRequest request = new OrderRequest(List.of(itemRequest));

        Order order = mapper.mapToOrder(request);

        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0)).isInstanceOf(BeerItem.class);
        BeerItem beer = (BeerItem) order.getItems().get(0);
        assertThat(beer.name()).isEqualTo("Heineken");
        assertThat(beer.quantity()).isEqualTo(6);
        assertThat(beer.origin()).isEqualTo(BeerOrigin.DUTCH);
    }

    @Test
    void shouldMapMixedOrder() {
        List<OrderItemRequest> items = List.of(
            new OrderItemRequest(ProductType.BREAD, "Bread", 3, 3, null, null),
            new OrderItemRequest(ProductType.VEGETABLE, "Veggies", null, null, 200, null),
            new OrderItemRequest(ProductType.BEER, "Beer", 6, null, null, BeerOrigin.DUTCH)
        );
        OrderRequest request = new OrderRequest(items);

        Order order = mapper.mapToOrder(request);

        assertThat(order.getItems()).hasSize(3);
    }

    @Test
    void shouldThrowExceptionWhenBreadMissingQuantity() {
        OrderItemRequest itemRequest = new OrderItemRequest(
            ProductType.BREAD, "Bread", null, 2, null, null
        );
        OrderRequest request = new OrderRequest(List.of(itemRequest));

        assertThatThrownBy(() -> mapper.mapToOrder(request))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessageContaining("quantity field required for product type BREAD");
    }

    @Test
    void shouldThrowExceptionWhenBreadMissingDaysOld() {
        OrderItemRequest itemRequest = new OrderItemRequest(
            ProductType.BREAD, "Bread", 3, null, null, null
        );
        OrderRequest request = new OrderRequest(List.of(itemRequest));

        assertThatThrownBy(() -> mapper.mapToOrder(request))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessageContaining("daysOld field required for product type BREAD");
    }

    @Test
    void shouldThrowExceptionWhenVegetableMissingWeight() {
        OrderItemRequest itemRequest = new OrderItemRequest(
            ProductType.VEGETABLE, "Carrots", null, null, null, null
        );
        OrderRequest request = new OrderRequest(List.of(itemRequest));

        assertThatThrownBy(() -> mapper.mapToOrder(request))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessageContaining("weightGrams field required for product type VEGETABLE");
    }

    @Test
    void shouldThrowExceptionWhenBeerMissingQuantity() {
        OrderItemRequest itemRequest = new OrderItemRequest(
            ProductType.BEER, "Beer", null, null, null, BeerOrigin.DUTCH
        );
        OrderRequest request = new OrderRequest(List.of(itemRequest));

        assertThatThrownBy(() -> mapper.mapToOrder(request))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessageContaining("quantity field required for product type BEER");
    }

    @Test
    void shouldThrowExceptionWhenBeerMissingOrigin() {
        OrderItemRequest itemRequest = new OrderItemRequest(
            ProductType.BEER, "Beer", 6, null, null, null
        );
        OrderRequest request = new OrderRequest(List.of(itemRequest));

        assertThatThrownBy(() -> mapper.mapToOrder(request))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessageContaining("origin field required for product type BEER");
    }
}
```

## Files to Create

- `src/main/java/com/grocery/pricing/api/mapper/OrderMapper.java`
- `src/test/java/com/grocery/pricing/api/mapper/OrderMapperTest.java`

## Acceptance Criteria

- [ ] OrderMapper converts OrderRequest to Order domain model
- [ ] Type-specific validation for BREAD (quantity, daysOld required)
- [ ] Type-specific validation for VEGETABLE (weightGrams required)
- [ ] Type-specific validation for BEER (quantity, origin required)
- [ ] Throws InvalidOrderException with descriptive messages
- [ ] All unit tests pass
