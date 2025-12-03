package com.online.grocery.pricing.pricing.discount;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.pricing.context.BeerPricingContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Pack-based discount rule for beer.
 *
 * <p>Applies fixed discount per 6-pack based on beer origin:</p>
 * <ul>
 *   <li>Belgian: €3.00 per pack</li>
 *   <li>Dutch: €2.00 per pack</li>
 *   <li>German: €4.00 per pack</li>
 * </ul>
 */
@Component
public class BeerPackDiscountRule implements BeerDiscountRule {

    private final PricingConfiguration config;

    public BeerPackDiscountRule(PricingConfiguration config) {
        this.config = config;
    }

    @Override
    public boolean isApplicable(BeerPricingContext ctx) {
        return ctx.packs() > 0;
    }

    @Override
    public BigDecimal calculateDiscount(BeerPricingContext ctx) {
        PricingConfiguration.BeerRules beerRules = config.getBeer();

        BigDecimal perPackDiscount = switch (ctx.origin()) {
            case BELGIAN -> beerRules.getBelgianPackDiscount();
            case DUTCH -> beerRules.getDutchPackDiscount();
            case GERMAN -> beerRules.getGermanPackDiscount();
        };

        return perPackDiscount.multiply(BigDecimal.valueOf(ctx.packs()));
    }

    @Override
    public int order() {
        return 100;
    }

    @Override
    public String description() {
        PricingConfiguration.BeerRules rules = config.getBeer();
        return String.format(
                "Fixed discount per 6-pack: Belgian €%.2f, Dutch €%.2f, German €%.2f",
                rules.getBelgianPackDiscount(),
                rules.getDutchPackDiscount(),
                rules.getGermanPackDiscount()
        );
    }
}
