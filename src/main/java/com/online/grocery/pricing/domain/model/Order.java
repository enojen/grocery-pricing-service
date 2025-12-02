package com.online.grocery.pricing.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents a customer order containing multiple items.
 */
public record Order(List<OrderItem> items) {
    
    public Order {
        Objects.requireNonNull(items, "Items cannot be null");
        items = List.copyOf(items);
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
}
