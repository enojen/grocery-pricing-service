package com.online.grocery.pricing.pricing.discount;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.pricing.context.VegetablePricingContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Weight-based percentage discount rule for vegetables.
 * 
 * <ul>
 *   <li>0-99g: 5% discount</li>
 *   <li>100-499g: 7% discount</li>
 *   <li>500g+: 10% discount</li>
 * </ul>
 */
@Component
public class VegetableWeightTierRule implements VegetableDiscountRule {

    private final PricingConfiguration config;

    public VegetableWeightTierRule(PricingConfiguration config) {
        this.config = config;
    }

    @Override
    public boolean isApplicable(VegetablePricingContext ctx) {
        return ctx.totalWeightGrams() > 0;
    }

    @Override
    public BigDecimal calculateDiscount(VegetablePricingContext ctx) {
        PricingConfiguration.VegetableRules rules = config.getVegetable();
        BigDecimal discountPercent;

        int weight = ctx.totalWeightGrams();

        if (weight < rules.getSmallWeightThreshold()) {
            discountPercent = rules.getSmallWeightDiscount();
        } else if (weight < rules.getMediumWeightThreshold()) {
            discountPercent = rules.getMediumWeightDiscount();
        } else {
            discountPercent = rules.getLargeWeightDiscount();
        }

        return ctx.originalPrice().multiply(discountPercent);
    }

    @Override
    public int order() {
        return 100;
    }

    @Override
    public String description() {
        PricingConfiguration.VegetableRules rules = config.getVegetable();
        return String.format(
            "Weight-based discounts: <%dg = %.0f%%, %d-%dg = %.0f%%, %dg+ = %.0f%%",
            rules.getSmallWeightThreshold(),
            rules.getSmallWeightDiscount().multiply(new BigDecimal("100")),
            rules.getSmallWeightThreshold(),
            rules.getMediumWeightThreshold() - 1,
            rules.getMediumWeightDiscount().multiply(new BigDecimal("100")),
            rules.getMediumWeightThreshold(),
            rules.getLargeWeightDiscount().multiply(new BigDecimal("100"))
        );
    }
}
