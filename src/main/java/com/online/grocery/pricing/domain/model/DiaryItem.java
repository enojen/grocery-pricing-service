package com.online.grocery.pricing.domain.model;

import com.online.grocery.pricing.domain.enums.ProductType;

public record DiaryItem(
    int quantity
) implements OrderItem {

    public DiaryItem {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    @Override
    public ProductType getType() {
        return ProductType.DIARY;
    }

    
}
