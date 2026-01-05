package com.online.grocery.pricing.pricing.discount;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.pricing.context.DiaryPricingContext;

@Component
public class DiaryDiscountRule implements DiscountRule<DiaryPricingContext>{
    private final PricingConfiguration config;

    public DiaryDiscountRule(PricingConfiguration config) {
        this.config = config;
    }

    @Override
    public ProductType productType() {
        return ProductType.DIARY;
    }

    @Override
    public String description() {
        BigDecimal buyOneTakeTwoAge = config.getDiary().getDiscountPercents();
        BigDecimal payOneTakeThreeAge = config.getDiary().getPriceThreshold();
        return String.format(
                "Age-based bundle discounts: %.2f days old = buy 1 take 2, %.2f days old = pay 1 take 3",
                buyOneTakeTwoAge, payOneTakeThreeAge
        );
    }

    @Override
    public boolean isApplicable(DiaryPricingContext ctx) {
        var isApplicable = ctx.originalPrice().compareTo(config.getDiary().getPriceThreshold()) >= 0;
        return  isApplicable;
    }

    @Override
    public BigDecimal calculateDiscount(DiaryPricingContext ctx) {
        var originalPrice = ctx.originalPrice();
        var discountPercent = config.getDiary().getDiscountPercents();
        return originalPrice.multiply(discountPercent);
    }

    @Override
    public int order() {
        return 100;
    }
    
}
