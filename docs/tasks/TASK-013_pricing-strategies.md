# TASK-013: Pricing Strategies

## Status
- [x] Completed

## Phase
Phase 2: Pricing Logic

## Description
Implement pricing strategy classes for each product type that orchestrate discount rule application.

## Implementation Details

### BreadPricingStrategy

```java
package com.grocery.pricing.pricing.strategy;

import com.grocery.pricing.config.PricingConfiguration;
import com.grocery.pricing.domain.enums.ProductType;
import com.grocery.pricing.domain.model.BreadItem;
import com.grocery.pricing.domain.model.MoneyUtils;
import com.grocery.pricing.domain.model.OrderItem;
import com.grocery.pricing.domain.model.ReceiptLine;
import com.grocery.pricing.pricing.context.BreadPricingContext;
import com.grocery.pricing.pricing.discount.BreadDiscountRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BreadPricingStrategy implements PricingStrategy {

    private final PricingConfiguration config;
    private final List<BreadDiscountRule> discountRules;

    public BreadPricingStrategy(
        PricingConfiguration config,
        List<BreadDiscountRule> discountRules
    ) {
        this.config = config;
        this.discountRules = discountRules.stream()
            .sorted(Comparator.comparingInt(BreadDiscountRule::order))
            .toList();
    }

    @Override
    public ProductType getProductType() {
        return ProductType.BREAD;
    }

    @Override
    public List<ReceiptLine> calculatePrice(List<OrderItem> items) {
        List<BreadItem> breads = castToType(items, BreadItem.class);

        // Group by age for separate receipt lines
        Map<Integer, List<BreadItem>> byAge = breads.stream()
            .collect(Collectors.groupingBy(BreadItem::daysOld));

        return byAge.entrySet().stream()
            .map(this::priceAgeGroup)
            .toList();
    }

    private ReceiptLine priceAgeGroup(Map.Entry<Integer, List<BreadItem>> entry) {
        int age = entry.getKey();
        List<BreadItem> items = entry.getValue();
        int totalQty = items.stream().mapToInt(BreadItem::quantity).sum();

        BigDecimal unitPrice = config.getBreadPrice();
        BigDecimal originalPrice = unitPrice.multiply(BigDecimal.valueOf(totalQty));

        // Create context
        BreadPricingContext ctx = new BreadPricingContext(
            age,
            totalQty,
            unitPrice,
            originalPrice
        );

        // Apply all applicable discount rules
        BigDecimal totalDiscount = discountRules.stream()
            .filter(rule -> rule.isApplicable(ctx))
            .map(rule -> rule.calculateDiscount(ctx))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalPrice = originalPrice.subtract(totalDiscount);

        String description = String.format("%d x Bread (%d days old)", totalQty, age);
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
```

### VegetablePricingStrategy

```java
package com.grocery.pricing.pricing.strategy;

import com.grocery.pricing.config.PricingConfiguration;
import com.grocery.pricing.domain.enums.ProductType;
import com.grocery.pricing.domain.model.MoneyUtils;
import com.grocery.pricing.domain.model.OrderItem;
import com.grocery.pricing.domain.model.ReceiptLine;
import com.grocery.pricing.domain.model.VegetableItem;
import com.grocery.pricing.pricing.context.VegetablePricingContext;
import com.grocery.pricing.pricing.discount.VegetableDiscountRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Component
public class VegetablePricingStrategy implements PricingStrategy {

    private final PricingConfiguration config;
    private final List<VegetableDiscountRule> discountRules;

    public VegetablePricingStrategy(
        PricingConfiguration config,
        List<VegetableDiscountRule> discountRules
    ) {
        this.config = config;
        this.discountRules = discountRules.stream()
            .sorted(Comparator.comparingInt(VegetableDiscountRule::order))
            .toList();
    }

    @Override
    public ProductType getProductType() {
        return ProductType.VEGETABLE;
    }

    @Override
    public List<ReceiptLine> calculatePrice(List<OrderItem> items) {
        List<VegetableItem> vegetables = castToType(items, VegetableItem.class);

        int totalWeight = vegetables.stream()
            .mapToInt(VegetableItem::weightGrams)
            .sum();

        // Price per 100g → price per gram
        BigDecimal pricePerGram = config.getVegetablePricePer100g()
            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal originalPrice = pricePerGram
            .multiply(BigDecimal.valueOf(totalWeight));

        // Create context
        VegetablePricingContext ctx = new VegetablePricingContext(
            totalWeight,
            pricePerGram,
            originalPrice
        );

        // Apply all applicable discount rules
        BigDecimal totalDiscount = discountRules.stream()
            .filter(rule -> rule.isApplicable(ctx))
            .map(rule -> rule.calculateDiscount(ctx))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalPrice = originalPrice.subtract(totalDiscount);

        String description = String.format("%dg Vegetables", totalWeight);
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
```

### BeerPricingStrategy

```java
package com.grocery.pricing.pricing.strategy;

import com.grocery.pricing.config.PricingConfiguration;
import com.grocery.pricing.domain.enums.BeerOrigin;
import com.grocery.pricing.domain.enums.ProductType;
import com.grocery.pricing.domain.model.BeerItem;
import com.grocery.pricing.domain.model.MoneyUtils;
import com.grocery.pricing.domain.model.OrderItem;
import com.grocery.pricing.domain.model.ReceiptLine;
import com.grocery.pricing.pricing.context.BeerPricingContext;
import com.grocery.pricing.pricing.discount.BeerDiscountRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BeerPricingStrategy implements PricingStrategy {

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

        // Group by origin for separate receipt lines
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

        // Get origin-specific base price
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

        // Create context
        BeerPricingContext ctx = new BeerPricingContext(
            origin,
            totalBottles,
            packs,
            singles,
            originBasePrice,
            originalPrice
        );

        // Apply all applicable discount rules
        BigDecimal totalDiscount = discountRules.stream()
            .filter(rule -> rule.isApplicable(ctx))
            .map(rule -> rule.calculateDiscount(ctx))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

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
```

### Unit Tests Structure

```java
// BreadPricingStrategyTest.java
@Test
void shouldCalculatePriceForFreshBread() { /* age 0-2, no discount */ }

@Test
void shouldCalculatePriceForBreadAge3To5() { /* buy 1 take 2 */ }

@Test
void shouldCalculatePriceForBreadAge6() { /* buy 1 take 3 */ }

@Test
void shouldGroupBreadByAge() { /* multiple ages in one order */ }

// VegetablePricingStrategyTest.java
@Test
void shouldCalculatePriceForSmallWeight() { /* < 100g, 5% */ }

@Test
void shouldCalculatePriceForMediumWeight() { /* 100-499g, 7% */ }

@Test
void shouldCalculatePriceForLargeWeight() { /* >= 500g, 10% */ }

@Test
void shouldAggregateAllVegetables() { /* multiple items combined */ }

// BeerPricingStrategyTest.java
@Test
void shouldCalculatePriceForSingleBottles() { /* no pack discount */ }

@Test
void shouldCalculatePriceForPacks() { /* pack discount applied */ }

@Test
void shouldCalculatePriceForMixedPacksAndSingles() { /* 7, 13 bottles etc */ }

@Test
void shouldGroupBeerByOrigin() { /* multiple origins in one order */ }
```

## Files to Create

- `src/main/java/com/grocery/pricing/pricing/strategy/BreadPricingStrategy.java`
- `src/main/java/com/grocery/pricing/pricing/strategy/VegetablePricingStrategy.java`
- `src/main/java/com/grocery/pricing/pricing/strategy/BeerPricingStrategy.java`
- `src/test/java/com/grocery/pricing/pricing/strategy/BreadPricingStrategyTest.java`
- `src/test/java/com/grocery/pricing/pricing/strategy/VegetablePricingStrategyTest.java`
- `src/test/java/com/grocery/pricing/pricing/strategy/BeerPricingStrategyTest.java`

## Acceptance Criteria

- [x] All three strategies implement PricingStrategy interface
- [x] Strategies auto-discover and apply discount rules via Spring DI
- [x] BreadPricingStrategy groups items by age
- [x] VegetablePricingStrategy aggregates all vegetables for weight calculation
- [x] BeerPricingStrategy groups items by origin
- [x] MoneyUtils.normalize() applied to all monetary values
- [x] All unit tests pass
