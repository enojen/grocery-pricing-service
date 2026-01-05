package com.online.grocery.pricing.pricing.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.MoneyUtils;
import com.online.grocery.pricing.domain.model.OrderItem;
import com.online.grocery.pricing.domain.model.ReceiptLine;
import com.online.grocery.pricing.domain.model.VegetableItem;
import com.online.grocery.pricing.pricing.context.VegetablePricingContext;
import com.online.grocery.pricing.pricing.discount.DiscountRule;

@Component
public final class VegetablePricingStrategy implements PricingStrategy {

    private final PricingConfiguration config;
    private final List<DiscountRule<VegetablePricingContext>> discountRules;

    public VegetablePricingStrategy(
            PricingConfiguration config,
            List<DiscountRule<VegetablePricingContext>> discountRules
    ) {
        this.config = config;
        this.discountRules = discountRules.stream()
                .sorted(Comparator.comparingInt(DiscountRule<VegetablePricingContext>::order))
                .toList();
    }

    @Override
    public ProductType getProductType() {
        return ProductType.VEGETABLE;
    }

    @Override
    public List<ReceiptLine> calculatePrice(List<OrderItem> items) {
        List<VegetableItem> vegetables = castToType(items, VegetableItem.class);

        int totalWeight = vegetables.stream()
                .mapToInt(VegetableItem::weightGrams)
                .sum();

        BigDecimal pricePerGram = config.getVegetablePricePer100g()
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal originalPrice = pricePerGram
                .multiply(BigDecimal.valueOf(totalWeight));

        VegetablePricingContext ctx = new VegetablePricingContext(
                totalWeight,
                pricePerGram,
                originalPrice
        );

        BigDecimal totalDiscount = discountRules.stream()
                .filter(rule -> rule.isApplicable(ctx))
                .map(rule -> rule.calculateDiscount(ctx))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalPrice = originalPrice.subtract(totalDiscount);

        String description = String.format("%dg Vegetables", totalWeight);
        return List.of(new ReceiptLine(
                description,
                MoneyUtils.normalize(originalPrice),
                MoneyUtils.normalize(totalDiscount),
                MoneyUtils.normalize(finalPrice)
        ));
    }

    @SuppressWarnings("unchecked")
    private <T extends OrderItem> List<T> castToType(List<OrderItem> items, Class<T> type) {
        return items.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }
}
