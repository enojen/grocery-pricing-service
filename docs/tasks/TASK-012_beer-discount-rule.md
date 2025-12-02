# TASK-012: Beer Discount Rule

## Status
- [ ] Not Started

## Phase
Phase 2: Pricing Logic

## Description
Implement BeerPackDiscountRule for pack-based fixed discounts on beer items.

## Business Rules

**Beer Pricing** (origin-specific base prices):
- Belgian beer: €0.60 per bottle
- Dutch beer: €0.50 per bottle
- German beer: €0.80 per bottle

**Pack Discounts** (6 bottles = 1 pack):
- Belgian pack: €3.00 discount → Final: €0.60 (€3.60 - €3.00)
- Dutch pack: €2.00 discount → Final: €1.00 (€3.00 - €2.00)
- German pack: €4.00 discount → Final: €0.80 (€4.80 - €4.00)

**Single bottles**: No discount, pay per-bottle base price

## Implementation Details

### BeerPackDiscountRule

```java
package com.grocery.pricing.pricing.discount;

import com.grocery.pricing.config.PricingConfiguration;
import com.grocery.pricing.pricing.context.BeerPricingContext;
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
        return ctx.packs() > 0; // At least 1 complete pack
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
        return 100; // Pack discounts first
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
```

### Unit Tests

```java
package com.grocery.pricing.pricing.discount;

import com.grocery.pricing.config.PricingConfiguration;
import com.grocery.pricing.domain.enums.BeerOrigin;
import com.grocery.pricing.pricing.context.BeerPricingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BeerPackDiscountRuleTest {

    private PricingConfiguration config;
    private BeerPackDiscountRule rule;

    @BeforeEach
    void setUp() {
        config = mock(PricingConfiguration.class);
        PricingConfiguration.BeerRules beerRules = mock(PricingConfiguration.BeerRules.class);
        
        when(config.getBeer()).thenReturn(beerRules);
        when(beerRules.getPackSize()).thenReturn(6);
        when(beerRules.getBelgianBasePrice()).thenReturn(new BigDecimal("0.60"));
        when(beerRules.getDutchBasePrice()).thenReturn(new BigDecimal("0.50"));
        when(beerRules.getGermanBasePrice()).thenReturn(new BigDecimal("0.80"));
        when(beerRules.getBelgianPackDiscount()).thenReturn(new BigDecimal("3.00"));
        when(beerRules.getDutchPackDiscount()).thenReturn(new BigDecimal("2.00"));
        when(beerRules.getGermanPackDiscount()).thenReturn(new BigDecimal("4.00"));
        
        rule = new BeerPackDiscountRule(config);
    }

    @Test
    void shouldBeApplicableWhenAtLeastOnePack() {
        BeerPricingContext ctx = new BeerPricingContext(
            BeerOrigin.BELGIAN, 6, 1, 0, new BigDecimal("0.60"), new BigDecimal("3.60")
        );
        
        assertThat(rule.isApplicable(ctx)).isTrue();
    }

    @Test
    void shouldNotBeApplicableForSinglesOnly() {
        BeerPricingContext ctx = new BeerPricingContext(
            BeerOrigin.BELGIAN, 5, 0, 5, new BigDecimal("0.60"), new BigDecimal("3.00")
        );
        
        assertThat(rule.isApplicable(ctx)).isFalse();
    }

    @Test
    void shouldCalculateBelgianPackDiscount() {
        // 1 pack of Belgian beer
        BeerPricingContext ctx = new BeerPricingContext(
            BeerOrigin.BELGIAN, 6, 1, 0, new BigDecimal("0.60"), new BigDecimal("3.60")
        );
        
        BigDecimal discount = rule.calculateDiscount(ctx);
        
        assertThat(discount).isEqualByComparingTo("3.00");
    }

    @Test
    void shouldCalculateDutchPackDiscount() {
        // 1 pack of Dutch beer
        BeerPricingContext ctx = new BeerPricingContext(
            BeerOrigin.DUTCH, 6, 1, 0, new BigDecimal("0.50"), new BigDecimal("3.00")
        );
        
        BigDecimal discount = rule.calculateDiscount(ctx);
        
        assertThat(discount).isEqualByComparingTo("2.00");
    }

    @Test
    void shouldCalculateGermanPackDiscount() {
        // 1 pack of German beer
        BeerPricingContext ctx = new BeerPricingContext(
            BeerOrigin.GERMAN, 6, 1, 0, new BigDecimal("0.80"), new BigDecimal("4.80")
        );
        
        BigDecimal discount = rule.calculateDiscount(ctx);
        
        assertThat(discount).isEqualByComparingTo("4.00");
    }

    @ParameterizedTest
    @CsvSource({
        // packs, expectedDiscount (Belgian)
        "1, 3.00",
        "2, 6.00",
        "3, 9.00",
        "5, 15.00"
    })
    void shouldCalculateMultiplePackDiscounts(int packs, String expectedDiscount) {
        int totalBottles = packs * 6;
        BigDecimal originalPrice = new BigDecimal("0.60").multiply(BigDecimal.valueOf(totalBottles));
        
        BeerPricingContext ctx = new BeerPricingContext(
            BeerOrigin.BELGIAN, totalBottles, packs, 0, new BigDecimal("0.60"), originalPrice
        );
        
        BigDecimal discount = rule.calculateDiscount(ctx);
        
        assertThat(discount).isEqualByComparingTo(expectedDiscount);
    }

    @Test
    void shouldCalculateDiscountForMixedPacksAndSingles() {
        // 7 Belgian beers = 1 pack + 1 single
        BeerPricingContext ctx = new BeerPricingContext(
            BeerOrigin.BELGIAN, 7, 1, 1, new BigDecimal("0.60"), new BigDecimal("4.20")
        );
        
        BigDecimal discount = rule.calculateDiscount(ctx);
        
        // Only pack discount, singles have no discount
        assertThat(discount).isEqualByComparingTo("3.00");
    }

    @ParameterizedTest
    @EnumSource(BeerOrigin.class)
    void shouldHandleAllOrigins(BeerOrigin origin) {
        BeerPricingContext ctx = new BeerPricingContext(
            origin, 6, 1, 0, new BigDecimal("0.50"), new BigDecimal("3.00")
        );
        
        BigDecimal discount = rule.calculateDiscount(ctx);
        
        assertThat(discount).isPositive();
    }

    @Test
    void shouldEnsurePositiveFinalPriceForBelgian() {
        // Belgian: 6 × €0.60 = €3.60, discount €3.00, final €0.60
        BigDecimal originalPrice = new BigDecimal("3.60");
        BeerPricingContext ctx = new BeerPricingContext(
            BeerOrigin.BELGIAN, 6, 1, 0, new BigDecimal("0.60"), originalPrice
        );
        
        BigDecimal discount = rule.calculateDiscount(ctx);
        BigDecimal finalPrice = originalPrice.subtract(discount);
        
        assertThat(finalPrice).isEqualByComparingTo("0.60");
        assertThat(finalPrice).isPositive();
    }

    @Test
    void shouldEnsurePositiveFinalPriceForDutch() {
        // Dutch: 6 × €0.50 = €3.00, discount €2.00, final €1.00
        BigDecimal originalPrice = new BigDecimal("3.00");
        BeerPricingContext ctx = new BeerPricingContext(
            BeerOrigin.DUTCH, 6, 1, 0, new BigDecimal("0.50"), originalPrice
        );
        
        BigDecimal discount = rule.calculateDiscount(ctx);
        BigDecimal finalPrice = originalPrice.subtract(discount);
        
        assertThat(finalPrice).isEqualByComparingTo("1.00");
        assertThat(finalPrice).isPositive();
    }

    @Test
    void shouldEnsurePositiveFinalPriceForGerman() {
        // German: 6 × €0.80 = €4.80, discount €4.00, final €0.80
        BigDecimal originalPrice = new BigDecimal("4.80");
        BeerPricingContext ctx = new BeerPricingContext(
            BeerOrigin.GERMAN, 6, 1, 0, new BigDecimal("0.80"), originalPrice
        );
        
        BigDecimal discount = rule.calculateDiscount(ctx);
        BigDecimal finalPrice = originalPrice.subtract(discount);
        
        assertThat(finalPrice).isEqualByComparingTo("0.80");
        assertThat(finalPrice).isPositive();
    }
}
```

## Files to Create

- `src/main/java/com/grocery/pricing/pricing/discount/BeerPackDiscountRule.java`
- `src/test/java/com/grocery/pricing/pricing/discount/BeerPackDiscountRuleTest.java`

## Acceptance Criteria

- [ ] BeerPackDiscountRule implements BeerDiscountRule interface
- [ ] Only applicable when at least 1 complete pack exists
- [ ] Belgian pack discount: €3.00 per pack
- [ ] Dutch pack discount: €2.00 per pack
- [ ] German pack discount: €4.00 per pack
- [ ] Multiple packs calculated correctly
- [ ] Final price is always positive (design prevents negative prices)
- [ ] Uses configuration for discounts (not hardcoded)
- [ ] All unit tests pass
