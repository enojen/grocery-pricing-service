package com.online.grocery.pricing.pricing.strategy;

import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.OrderItem;
import com.online.grocery.pricing.domain.model.ReceiptLine;

import java.util.List;

/**
 * Strategy interface for product-specific pricing calculations.
 * Each product type (Bread, Beer, Vegetable) has its own implementation.
 *
 * <p>Implementations should extend AbstractPricingStrategy.
 * Discovered by Spring DI and registered by product type in OrderPricingService.</p>
 */
public interface PricingStrategy {

    /**
     * Returns the product type this strategy handles.
     * Used for automatic registration and dispatch.
     *
     * @return The ProductType this strategy is responsible for
     */
    ProductType getProductType();

    /**
     * Calculate prices for a list of order items.
     * All items passed to this method will be of the type returned by getProductType().
     *
     * @param items List of order items to price (pre-filtered by type)
     * @return List of receipt lines with pricing details
     */
    List<ReceiptLine> calculatePrice(List<OrderItem> items);
}
