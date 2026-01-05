package com.online.grocery.pricing.api.mapper;

import com.online.grocery.pricing.api.dto.OrderItemRequest;
import com.online.grocery.pricing.api.dto.OrderRequest;
import com.online.grocery.pricing.domain.model.BreadItem;
import com.online.grocery.pricing.domain.model.Order;
import com.online.grocery.pricing.domain.model.OrderItem;
import com.online.grocery.pricing.domain.model.VegetableItem;
import com.online.grocery.pricing.domain.model.BeerItem;
import com.online.grocery.pricing.exception.InvalidOrderException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting API DTOs to domain models.
 * Handles type-specific validation for OrderItemRequest.
 */
@Component
public class OrderMapper {

    /**
     * Convert OrderRequest DTO to domain Order model.
     * Validates type-specific field requirements before conversion.
     *
     * @param request The order request from API
     * @return Domain Order object
     * @throws InvalidOrderException if required fields for type are missing
     */
    public Order mapToOrder(OrderRequest request) {
        List<OrderItem> items = request.items().stream()
                .map(this::mapToOrderItem)
                .toList();

        return new Order(items);
    }

    /**
     * Convert single OrderItemRequest to appropriate OrderItem domain model.
     * Throws InvalidOrderException if required fields for type are missing.
     */
    private OrderItem mapToOrderItem(OrderItemRequest itemRequest) {
        validateItemRequest(itemRequest);

        return switch (itemRequest.type()) {
            case BREAD -> new BreadItem(
                    itemRequest.quantity(),
                    itemRequest.daysOld()
            );
            case VEGETABLE -> new VegetableItem(
                    itemRequest.weightGrams()
            );
            case BEER -> new BeerItem(
                    itemRequest.quantity(),
                    itemRequest.origin()
            );
        };
    }

    /**
     * Validate that required fields for the product type are present.
     * Throws InvalidOrderException with descriptive error message.
     */
    private void validateItemRequest(OrderItemRequest request) {
        switch (request.type()) {
            case BREAD:
                if (request.quantity() == null) {
                    throw new InvalidOrderException(
                            "quantity field required for product type BREAD"
                    );
                }
                if (request.daysOld() == null) {
                    throw new InvalidOrderException(
                            "daysOld field required for product type BREAD"
                    );
                }
                break;

            case VEGETABLE:
                if (request.weightGrams() == null) {
                    throw new InvalidOrderException(
                            "weightGrams field required for product type VEGETABLE"
                    );
                }
                break;

            case BEER:
                if (request.quantity() == null) {
                    throw new InvalidOrderException(
                            "quantity field required for product type BEER"
                    );
                }
                if (request.origin() == null) {
                    throw new InvalidOrderException(
                            "origin field required for product type BEER"
                    );
                }
                break;
        }
    }
}
