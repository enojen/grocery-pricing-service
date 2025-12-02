package com.online.grocery.pricing.domain.model;

import com.online.grocery.pricing.domain.enums.ProductType;

/**
 * Base interface for all order items.
 */
public interface OrderItem {
    ProductType getType();
    String getName();
}
