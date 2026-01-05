package com.online.grocery.pricing.pricing.strategy;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.BreadItem;
import com.online.grocery.pricing.domain.model.ReceiptLine;
import com.online.grocery.pricing.pricing.context.BreadPricingContext;
import com.online.grocery.pricing.pricing.discount.DiscountRule;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public final class BreadPricingStrategy
        extends AbstractPricingStrategy<BreadItem, BreadPricingContext> {

    private final PricingConfiguration config;

    public BreadPricingStrategy(
            PricingConfiguration config,
            List<DiscountRule<BreadPricingContext>> discountRules
    ) {
        super(discountRules);
        this.config = config;
    }

    @Override
    public ProductType getProductType() {
        return ProductType.BREAD;
    }

    @Override
    protected Class<BreadItem> getItemType() {
        return BreadItem.class;
    }

    @Override
    protected List<ReceiptLine> calculateTypedPrice(List<BreadItem> breads) {
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

        String description = String.format("%d x Bread (%d days old)", totalQty, age);
        return applyDiscountsAndCreateLine(description, ctx);
    }
}
