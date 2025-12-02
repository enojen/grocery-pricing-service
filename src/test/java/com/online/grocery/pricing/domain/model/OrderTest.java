package com.online.grocery.pricing.domain.model;

import com.online.grocery.pricing.domain.enums.BeerOrigin;
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
