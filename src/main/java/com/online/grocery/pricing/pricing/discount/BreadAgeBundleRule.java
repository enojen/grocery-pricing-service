package com.online.grocery.pricing.pricing.discount;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.pricing.context.BreadPricingContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Age-based bundle discount rule for bread.
 *
 * <ul>
 *   <li>3-5 days old: "Buy 1 take 2" - In groups of 2, pay for 1</li>
 *   <li>6 days old: "Buy 1 take 3" - In groups of 3, pay for 1</li>
 * </ul>
 */
@Component
public final class BreadAgeBundleRule implements BreadDiscountRule {

    private final PricingConfiguration config;

    public BreadAgeBundleRule(PricingConfiguration config) {
        this.config = config;
    }

    @Override
    public boolean isApplicable(BreadPricingContext ctx) {
        int minAge = config.getBread().getBundleDiscountMinAge();
        int specialAge = config.getBread().getSpecialBundleAge();
        return ctx.age() >= minAge && ctx.age() <= specialAge;
    }

    @Override
    public BigDecimal calculateDiscount(BreadPricingContext ctx) {
        int age = ctx.age();
        int qty = ctx.totalQuantity();
        BigDecimal unitPrice = ctx.unitPrice();

        int minAge = config.getBread().getBundleDiscountMinAge();
        int specialAge = config.getBread().getSpecialBundleAge();

        if (age >= minAge && age < specialAge) {
            // "Buy 1 take 2": In groups of 2, pay for 1
            int freeItems = qty / 2;
            return unitPrice.multiply(BigDecimal.valueOf(freeItems));
        }

        if (age == specialAge) {
            // "Buy 1 take 3": In groups of 3, pay for 1
            int groups = qty / 3;
            int freeItems = groups * 2;
            return unitPrice.multiply(BigDecimal.valueOf(freeItems));
        }

        return BigDecimal.ZERO;
    }

    @Override
    public int order() {
        return 100;
    }

    @Override
    public String description() {
        int minAge = config.getBread().getBundleDiscountMinAge();
        int specialAge = config.getBread().getSpecialBundleAge();
        return String.format(
                "Age-based bundle discounts: %d-%d days old = buy 1 take 2, %d days old = buy 1 take 3",
                minAge, specialAge - 1, specialAge
        );
    }
}
