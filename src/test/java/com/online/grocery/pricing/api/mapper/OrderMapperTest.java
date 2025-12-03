package com.online.grocery.pricing.api.mapper;

import com.online.grocery.pricing.api.dto.OrderItemRequest;
import com.online.grocery.pricing.api.dto.OrderRequest;
import com.online.grocery.pricing.domain.enums.BeerOrigin;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.BeerItem;
import com.online.grocery.pricing.domain.model.BreadItem;
import com.online.grocery.pricing.domain.model.Order;
import com.online.grocery.pricing.domain.model.VegetableItem;
import com.online.grocery.pricing.exception.InvalidOrderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
