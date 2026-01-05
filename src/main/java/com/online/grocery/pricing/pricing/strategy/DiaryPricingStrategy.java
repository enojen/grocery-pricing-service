package com.online.grocery.pricing.pricing.strategy;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.DiaryItem;
import com.online.grocery.pricing.domain.model.MoneyUtils;
import com.online.grocery.pricing.domain.model.OrderItem;
import com.online.grocery.pricing.domain.model.ReceiptLine;
import com.online.grocery.pricing.pricing.context.DiaryPricingContext;
import com.online.grocery.pricing.pricing.discount.DiscountRule;

@Component
public class DiaryPricingStrategy implements PricingStrategy{
    private final PricingConfiguration config;
    private final List<DiscountRule<DiaryPricingContext>> discountRules;

    public DiaryPricingStrategy(
            PricingConfiguration config,
            List<DiscountRule<DiaryPricingContext>> discountRules
    ) {
        this.config = config;
        this.discountRules = discountRules.stream()
                .sorted(Comparator.comparingInt(DiscountRule<DiaryPricingContext>::order))
                .toList();
    }

    @Override
    public ProductType getProductType() {
        return ProductType.DIARY;
    }

    @Override
    public List<ReceiptLine> calculatePrice(List<OrderItem> items) {
        List<DiaryItem> diaries = castToType(items, DiaryItem.class);
        int totalQty = diaries.stream().mapToInt(DiaryItem::quantity).sum();
        BigDecimal unitPrice = config.getDiaryPrice();

        BigDecimal originalPrice = unitPrice.multiply(BigDecimal.valueOf(totalQty));

        DiaryPricingContext ctx = new DiaryPricingContext(
            originalPrice
        );

        var totalDiscount = discountRules.stream()
            .filter(rule -> rule.isApplicable(ctx))
            .map(rule -> rule.calculateDiscount(ctx))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        var finalPrice = originalPrice.subtract(totalDiscount);

        String description = String.format("");
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
