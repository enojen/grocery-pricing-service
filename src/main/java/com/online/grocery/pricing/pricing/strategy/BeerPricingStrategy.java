package com.online.grocery.pricing.pricing.strategy;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.domain.enums.BeerOrigin;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.BeerItem;
import com.online.grocery.pricing.domain.model.ReceiptLine;
import com.online.grocery.pricing.pricing.context.BeerPricingContext;
import com.online.grocery.pricing.pricing.discount.DiscountRule;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public final class BeerPricingStrategy
        extends AbstractPricingStrategy<BeerItem, BeerPricingContext> {

    private final PricingConfiguration config;

    public BeerPricingStrategy(
            PricingConfiguration config,
            List<DiscountRule<BeerPricingContext>> discountRules
    ) {
        super(discountRules);
        this.config = config;
    }

    @Override
    public ProductType getProductType() {
        return ProductType.BEER;
    }

    @Override
    protected Class<BeerItem> getItemType() {
        return BeerItem.class;
    }

    @Override
    protected List<ReceiptLine> calculateTypedPrice(List<BeerItem> beers) {
        Map<BeerOrigin, List<BeerItem>> byOrigin = beers.stream()
                .collect(Collectors.groupingBy(BeerItem::origin));

        return byOrigin.entrySet().stream()
                .map(this::priceOriginGroup)
                .toList();
    }

    private ReceiptLine priceOriginGroup(Map.Entry<BeerOrigin, List<BeerItem>> entry) {
        BeerOrigin origin = entry.getKey();
        int totalBottles = entry.getValue().stream()
                .mapToInt(BeerItem::quantity)
                .sum();

        PricingConfiguration.BeerRules beerRules = config.getBeer();
        BigDecimal originBasePrice = switch (origin) {
            case BELGIAN -> beerRules.getBelgianBasePrice();
            case DUTCH -> beerRules.getDutchBasePrice();
            case GERMAN -> beerRules.getGermanBasePrice();
        };

        BigDecimal originalPrice = originBasePrice.multiply(BigDecimal.valueOf(totalBottles));

        int packSize = beerRules.getPackSize();

        int packs = totalBottles / packSize;
        int singles = totalBottles % packSize;

        BeerPricingContext ctx = new BeerPricingContext(
                origin,
                totalBottles,
                packs,
                singles,
                originBasePrice,
                originalPrice
        );

        String description = String.format(
                "%d x %s Beer (%d packs + %d singles)",
                totalBottles, origin, packs, singles
        );
        return applyDiscountsAndCreateLine(description, ctx);
    }
}
