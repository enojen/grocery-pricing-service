package com.online.grocery.pricing.pricing.strategy;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.ReceiptLine;
import com.online.grocery.pricing.domain.model.VegetableItem;
import com.online.grocery.pricing.pricing.context.VegetablePricingContext;
import com.online.grocery.pricing.pricing.discount.DiscountRule;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public final class VegetablePricingStrategy
        extends AbstractPricingStrategy<VegetableItem, VegetablePricingContext> {

    private final PricingConfiguration config;

    public VegetablePricingStrategy(
            PricingConfiguration config,
            List<DiscountRule<VegetablePricingContext>> discountRules
    ) {
        super(discountRules);
        this.config = config;
    }

    @Override
    public ProductType getProductType() {
        return ProductType.VEGETABLE;
    }

    @Override
    protected Class<VegetableItem> getItemType() {
        return VegetableItem.class;
    }

    @Override
    protected List<ReceiptLine> calculateTypedPrice(List<VegetableItem> vegetables) {
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

        String description = String.format("%dg Vegetables", totalWeight);
        return List.of(applyDiscountsAndCreateLine(description, ctx));
    }
}
