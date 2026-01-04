package com.online.grocery.pricing.pricing.discount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.OrderItem;
import com.online.grocery.pricing.pricing.context.OrderPricingContext;

/**
 * Combo discount for orders containing both bread and vegetables.
 * Applied as percentage off the total after product-level discounts.
 */
@Component
public class BreadVegetableComboRule implements OrderDiscountRule {

    private final PricingConfiguration config;

    public BreadVegetableComboRule(PricingConfiguration config) {
        this.config = config;
    }

    @Override
    public List<ProductType> productTypes() {
        return List.of(ProductType.BREAD, ProductType.VEGETABLE);
    }

    @Override
    public boolean isApplicable(OrderPricingContext ctx) {
        Set<ProductType> orderProductTypes = ctx.order().getItems().stream()
                .map(OrderItem::getType)
                .collect(Collectors.toSet());
        return orderProductTypes.containsAll(productTypes());
    }

    @Override
    public BigDecimal calculateDiscount(OrderPricingContext ctx) {
        return ctx.currentTotal().multiply(config.getComboDiscountRate());
    }

    @Override
    public int order() {
        return 1000;
    }

    @Override
    public String description() {
        return String.format(
                "%.0f%% off when buying bread and vegetables together",
                config.getComboDiscountRate().multiply(BigDecimal.valueOf(100))
        );
    }
}
