package com.online.grocery.pricing.domain.model;

import com.online.grocery.pricing.domain.enums.ProductType;

/**
 * Represents vegetable items in an order.
 * 
 * @param name Item name/description
 * @param weightGrams Weight of vegetables in grams
 */
public record VegetableItem(
    String name,
    int weightGrams
) implements OrderItem {
    
    public VegetableItem {
        if (weightGrams <= 0) {
            throw new IllegalArgumentException("Weight must be positive");
        }
    }

    @Override
    public ProductType getType() { 
        return ProductType.VEGETABLE; 
    }

    @Override
    public String getName() {
        return name;
    }
}
