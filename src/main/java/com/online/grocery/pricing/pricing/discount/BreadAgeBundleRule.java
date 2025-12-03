package com.online.grocery.pricing.pricing.discount;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.pricing.context.BreadPricingContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Age-based bundle discount rule for bread.
 */
@Component
public final class BreadAgeBundleRule implements BreadDiscountRule {

    private final PricingConfiguration config;

    public BreadAgeBundleRule(PricingConfiguration config) {
        this.config = config;
    }

    @Override
    public boolean isApplicable(BreadPricingContext ctx) {
        int age = ctx.age();
        int buyOneTakeTwoAge = config.getBread().getBuyOneTakeTwoAge();
        int payOneTakeThreeAge = config.getBread().getPayOneTakeThreeAge();
        return age == buyOneTakeTwoAge || age == payOneTakeThreeAge;
    }

    @Override
    public BigDecimal calculateDiscount(BreadPricingContext ctx) {
        int age = ctx.age();
        int qty = ctx.totalQuantity();
        BigDecimal unitPrice = ctx.unitPrice();

        int buyOneTakeTwoAge = config.getBread().getBuyOneTakeTwoAge();
        int payOneTakeThreeAge = config.getBread().getPayOneTakeThreeAge();

        if (age == buyOneTakeTwoAge) {
            // "Buy 1 take 2": In groups of 2, pay for 1
            int freeItems = qty / 2;
            return unitPrice.multiply(BigDecimal.valueOf(freeItems));
        }

        if (age == payOneTakeThreeAge) {
            // "Pay 1 take 3": In groups of 3, pay for 1
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
        int buyOneTakeTwoAge = config.getBread().getBuyOneTakeTwoAge();
        int payOneTakeThreeAge = config.getBread().getPayOneTakeThreeAge();
        return String.format(
                "Age-based bundle discounts: %d days old = buy 1 take 2, %d days old = pay 1 take 3",
                buyOneTakeTwoAge, payOneTakeThreeAge
        );
    }
}
