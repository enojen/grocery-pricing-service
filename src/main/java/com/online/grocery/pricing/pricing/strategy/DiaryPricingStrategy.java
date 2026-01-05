package com.online.grocery.pricing.pricing.strategy;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.DiaryItem;
import com.online.grocery.pricing.domain.model.ReceiptLine;
import com.online.grocery.pricing.pricing.context.DiaryPricingContext;
import com.online.grocery.pricing.pricing.discount.DiscountRule;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public final class DiaryPricingStrategy
        extends AbstractPricingStrategy<DiaryItem, DiaryPricingContext> {

    private final PricingConfiguration config;

    public DiaryPricingStrategy(
            PricingConfiguration config,
            List<DiscountRule<DiaryPricingContext>> discountRules
    ) {
        super(discountRules);
        this.config = config;
    }

    @Override
    public ProductType getProductType() {
        return ProductType.DIARY;
    }

    @Override
    protected Class<DiaryItem> getItemType() {
        return DiaryItem.class;
    }

    @Override
    protected List<ReceiptLine> calculateTypedPrice(List<DiaryItem> diaries) {
        int totalQty = diaries.stream().mapToInt(DiaryItem::quantity).sum();
        BigDecimal unitPrice = config.getDiaryPrice();
        BigDecimal originalPrice = unitPrice.multiply(BigDecimal.valueOf(totalQty));

        DiaryPricingContext ctx = new DiaryPricingContext(originalPrice);

        String description = String.format("%d x Diary", totalQty);
        return List.of(applyDiscountsAndCreateLine(description, ctx));
    }
}
