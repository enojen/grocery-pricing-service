package com.online.grocery.pricing.domain.model;

import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.exception.InvalidOrderException;

/**
 * Represents bread items in an order.
 * 
 * @param name Item name/description
 * @param quantity Number of bread units
 * @param daysOld Age of bread in days (0-6 valid)
 */
public record BreadItem(
    String name,
    int quantity,
    int daysOld
) implements OrderItem {
    
    public BreadItem {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (daysOld < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
        if (daysOld > 6) {
            throw new InvalidOrderException("Bread older than 6 days cannot be ordered");
        }
    }

    @Override
    public ProductType getType() { 
        return ProductType.BREAD; 
    }

    @Override
    public String getName() {
        return name;
    }
}
