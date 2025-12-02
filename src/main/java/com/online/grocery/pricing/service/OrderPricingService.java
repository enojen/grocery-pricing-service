package com.online.grocery.pricing.service;

import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.Order;
import com.online.grocery.pricing.domain.model.OrderItem;
import com.online.grocery.pricing.domain.model.Receipt;
import com.online.grocery.pricing.domain.model.ReceiptLine;
import com.online.grocery.pricing.pricing.strategy.PricingStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates pricing calculation for orders.
 * Delegates to product-specific pricing strategies.
 */
@Service
public class OrderPricingService {

    private final Map<ProductType, PricingStrategy> strategies;

    /**
     * Constructs the service with auto-discovered pricing strategies.
     * Spring injects all PricingStrategy implementations.
     *
     * @param strategyList All available pricing strategies
     */
    public OrderPricingService(List<PricingStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(
                PricingStrategy::getProductType,
                Function.identity()
            ));
    }

    /**
     * Calculate a complete receipt for an order.
     *
     * @param order The order containing items to price
     * @return Receipt with line items and totals
     * @throws IllegalStateException if no strategy found for a product type
     */
    public Receipt calculateReceipt(Order order) {
        Map<ProductType, List<OrderItem>> itemsByType = order.getItems().stream()
            .collect(Collectors.groupingBy(OrderItem::getType));

        List<ReceiptLine> allLines = itemsByType.entrySet().stream()
            .flatMap(entry -> {
                PricingStrategy strategy = strategies.get(entry.getKey());
                if (strategy == null) {
                    throw new IllegalStateException(
                        "No pricing strategy registered for product type: " + entry.getKey()
                    );
                }
                return strategy.calculatePrice(entry.getValue()).stream();
            })
            .toList();

        BigDecimal subtotal = allLines.stream()
            .map(ReceiptLine::originalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = allLines.stream()
            .map(ReceiptLine::discount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = subtotal.subtract(totalDiscount);

        return new Receipt(allLines, subtotal, totalDiscount, total);
    }
}
