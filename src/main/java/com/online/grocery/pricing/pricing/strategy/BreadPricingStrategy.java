package com.online.grocery.pricing.pricing.strategy;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.BreadItem;
import com.online.grocery.pricing.domain.model.MoneyUtils;
import com.online.grocery.pricing.domain.model.OrderItem;
import com.online.grocery.pricing.domain.model.ReceiptLine;
import com.online.grocery.pricing.pricing.context.BreadPricingContext;
import com.online.grocery.pricing.pricing.discount.BreadDiscountRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public final class BreadPricingStrategy implements PricingStrategy {

    private final PricingConfiguration config;
    private final List<BreadDiscountRule> discountRules;

    public BreadPricingStrategy(
            PricingConfiguration config,
            List<BreadDiscountRule> discountRules
    ) {
        this.config = config;
        this.discountRules = discountRules.stream()
                .sorted(Comparator.comparingInt(BreadDiscountRule::order))
                .toList();
    }

    @Override
    public ProductType getProductType() {
        return ProductType.BREAD;
    }

    @Override
    public List<ReceiptLine> calculatePrice(List<OrderItem> items) {
        List<BreadItem> breads = castToType(items, BreadItem.class);

        Map<Integer, List<BreadItem>> byAge = breads.stream()
                .collect(Collectors.groupingBy(BreadItem::daysOld));

        return byAge.entrySet().stream()
                .map(this::priceAgeGroup)
                .toList();
    }

    private ReceiptLine priceAgeGroup(Map.Entry<Integer, List<BreadItem>> entry) {
        int age = entry.getKey();
        List<BreadItem> items = entry.getValue();
        int totalQty = items.stream().mapToInt(BreadItem::quantity).sum();

        BigDecimal unitPrice = config.getBreadPrice();
        BigDecimal originalPrice = unitPrice.multiply(BigDecimal.valueOf(totalQty));

        BreadPricingContext ctx = new BreadPricingContext(
                age,
                totalQty,
                unitPrice,
                originalPrice
        );

        BigDecimal totalDiscount = discountRules.stream()
                .filter(rule -> rule.isApplicable(ctx))
                .map(rule -> rule.calculateDiscount(ctx))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalPrice = originalPrice.subtract(totalDiscount);

        String description = String.format("%d x Bread (%d days old)", totalQty, age);
        return new ReceiptLine(
                description,
                MoneyUtils.normalize(originalPrice),
                MoneyUtils.normalize(totalDiscount),
                MoneyUtils.normalize(finalPrice)
        );
    }

    @SuppressWarnings("unchecked")
    private <T extends OrderItem> List<T> castToType(List<OrderItem> items, Class<T> type) {
        return items.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }
}
