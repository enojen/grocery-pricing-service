package com.online.grocery.pricing.pricing.discount;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.domain.enums.BeerOrigin;
import com.online.grocery.pricing.pricing.context.BeerPricingContext;

@Component
public class BeerBuyTwoGetOne implements BeerDiscountRule {
    private final PricingConfiguration config;

    public BeerBuyTwoGetOne(PricingConfiguration config) {
        this.config = config;
    }

    @Override
    public boolean isApplicable(BeerPricingContext ctx) {
        return ctx.totalBottles() >= config.getBeer().getOneFreeThreshold() && ctx.origin() == BeerOrigin.BELGIAN; 
    }

    @Override
    public BigDecimal calculateDiscount(BeerPricingContext ctx) {
        PricingConfiguration.BeerRules beerRules = config.getBeer();

        BigDecimal basePrice = switch (ctx.origin()) {
            case BELGIAN -> beerRules.getBelgianBasePrice();
            case DUTCH -> beerRules.getDutchBasePrice();
            case GERMAN -> beerRules.getGermanBasePrice();
            case SPANISH -> beerRules.getSpanishBasePrice();
        };

        var free = ctx.totalBottles() / beerRules.getOneFreeThreshold();
        return BigDecimal.valueOf(free).multiply(basePrice);
    }

    @Override
    public int order() {
        return 150;
    }

    @Override
    public String description() {
        PricingConfiguration.BeerRules rules = config.getBeer();
        return String.format(
                "Buy %d Get 1 Free on Belgian beer",
                rules.getOneFreeThreshold()
        );
    }
    
}
