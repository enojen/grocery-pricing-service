package com.online.grocery.pricing.pricing.strategy;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.domain.enums.BeerOrigin;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.BeerItem;
import com.online.grocery.pricing.domain.model.MoneyUtils;
import com.online.grocery.pricing.domain.model.OrderItem;
import com.online.grocery.pricing.domain.model.ReceiptLine;
import com.online.grocery.pricing.pricing.context.BeerPricingContext;
import com.online.grocery.pricing.pricing.discount.BeerDiscountRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public final class BeerPricingStrategy implements PricingStrategy {

    private final PricingConfiguration config;
    private final List<BeerDiscountRule> discountRules;

    public BeerPricingStrategy(
            PricingConfiguration config,
            List<BeerDiscountRule> discountRules
    ) {
        this.config = config;
        this.discountRules = discountRules.stream()
                .sorted(Comparator.comparingInt(BeerDiscountRule::order))
                .toList();
    }

    @Override
    public ProductType getProductType() {
        return ProductType.BEER;
    }

    @Override
    public List<ReceiptLine> calculatePrice(List<OrderItem> items) {
        List<BeerItem> beers = castToType(items, BeerItem.class);

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
            case SPANISH -> beerRules.getSpanishBasePrice();
        };

        BigDecimal originalPrice = originBasePrice.multiply(BigDecimal.valueOf(totalBottles));

        int packSize = switch (origin) {
            case GERMAN -> beerRules.getGermanPackSize();
            default -> beerRules.getPackSize();
        };

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

        BigDecimal totalDiscount = discountRules.stream()
                .filter(rule -> rule.isApplicable(ctx))
                .map(rule -> rule.calculateDiscount(ctx))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Cap discount at original price to prevent negative final price
        if (totalDiscount.compareTo(originalPrice) > 0) {
            totalDiscount = originalPrice;
        }

        BigDecimal finalPrice = originalPrice.subtract(totalDiscount);

        String description = String.format(
                "%d x %s Beer (%d packs + %d singles)",
                totalBottles, origin, packs, singles
        );
        return new ReceiptLine(
                description,
                MoneyUtils.normalize(originalPrice),
                MoneyUtils.normalize(totalDiscount),
                MoneyUtils.normalize(finalPrice)
        );
    }

    @SuppressWarnings("unchecked")
    private <T extends OrderItem> List<T> castToType(List<OrderItem> items, Class<T> type) {
        return items.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }
}
