package com.online.grocery.pricing.domain.model;

import com.online.grocery.pricing.domain.enums.BeerOrigin;
import com.online.grocery.pricing.domain.enums.ProductType;

import java.util.Objects;

/**
 * Represents beer items in an order.
 *
 * @param quantity Number of bottles
 * @param origin   Beer origin (BELGIAN, DUTCH, GERMAN)
 */
public record BeerItem(
        int quantity,
        BeerOrigin origin
) implements OrderItem {

    public BeerItem {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Objects.requireNonNull(origin, "Beer origin required");
    }

    @Override
    public ProductType getType() {
        return ProductType.BEER;
    }
}
