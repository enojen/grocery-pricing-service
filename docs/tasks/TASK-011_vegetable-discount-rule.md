# TASK-011: Vegetable Discount Rule

## Status

- [x] Completed

## Phase

Phase 2: Pricing Logic

## Description

Implement VegetableWeightTierRule for weight-based percentage discounts on vegetables.

## Business Rules

**Vegetable Weight-Based Discounts** (applied to ALL vegetables in order):

- **0-99g**: 5% discount
- **100-499g**: 7% discount
- **500g+**: 10% discount

Base price: €1.00 per 100g

## Implementation Details

### VegetableWeightTierRule

```java
package com.grocery.pricing.pricing.discount;

import com.grocery.pricing.config.PricingConfiguration;
import com.grocery.pricing.pricing.context.VegetablePricingContext;
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
        return ctx.totalWeightGrams() > 0; // Always applicable if vegetables exist
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
        return 100; // Weight-based discount
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
```

### Unit Tests

```java
package com.grocery.pricing.pricing.discount;

import com.grocery.pricing.config.PricingConfiguration;
import com.grocery.pricing.pricing.context.VegetablePricingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class VegetableWeightTierRuleTest {

    private PricingConfiguration config;
    private VegetableWeightTierRule rule;

    @BeforeEach
    void setUp() {
        config = mock(PricingConfiguration.class);
        PricingConfiguration.VegetableRules vegRules = mock(PricingConfiguration.VegetableRules.class);

        when(config.getVegetable()).thenReturn(vegRules);
        when(vegRules.getSmallWeightThreshold()).thenReturn(100);
        when(vegRules.getMediumWeightThreshold()).thenReturn(500);
        when(vegRules.getSmallWeightDiscount()).thenReturn(new BigDecimal("0.05"));
        when(vegRules.getMediumWeightDiscount()).thenReturn(new BigDecimal("0.07"));
        when(vegRules.getLargeWeightDiscount()).thenReturn(new BigDecimal("0.10"));

        rule = new VegetableWeightTierRule(config);
    }

    @Test
    void shouldBeApplicableForAnyPositiveWeight() {
        VegetablePricingContext ctx = new VegetablePricingContext(
                1, new BigDecimal("0.01"), new BigDecimal("0.01")
        );

        assertThat(rule.isApplicable(ctx)).isTrue();
    }

    @Test
    void shouldNotBeApplicableForZeroWeight() {
        VegetablePricingContext ctx = new VegetablePricingContext(
                0, BigDecimal.ZERO, BigDecimal.ZERO
        );

        assertThat(rule.isApplicable(ctx)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            // weight, originalPrice, expectedDiscount
            "50, 0.50, 0.025",     // 50g @ €0.50 × 5% = €0.025
            "99, 0.99, 0.0495",    // 99g @ €0.99 × 5% = €0.0495
            "100, 1.00, 0.07",     // 100g @ €1.00 × 7% = €0.07
            "200, 2.00, 0.14",     // 200g @ €2.00 × 7% = €0.14
            "499, 4.99, 0.3493",   // 499g @ €4.99 × 7% = €0.3493
            "500, 5.00, 0.50",     // 500g @ €5.00 × 10% = €0.50
            "1000, 10.00, 1.00"    // 1000g @ €10.00 × 10% = €1.00
    })
    void shouldCalculateCorrectDiscount(int weight, String originalPrice, String expectedDiscount) {
        BigDecimal pricePerGram = new BigDecimal("0.01"); // €1.00 per 100g
        VegetablePricingContext ctx = new VegetablePricingContext(
                weight, pricePerGram, new BigDecimal(originalPrice)
        );

        BigDecimal discount = rule.calculateDiscount(ctx);

        assertThat(discount).isEqualByComparingTo(expectedDiscount);
    }

    @Test
    void shouldApply5PercentForSmallWeight() {
        // 50g vegetables at €0.50
        VegetablePricingContext ctx = new VegetablePricingContext(
                50, new BigDecimal("0.01"), new BigDecimal("0.50")
        );

        BigDecimal discount = rule.calculateDiscount(ctx);

        // 5% of €0.50 = €0.025
        assertThat(discount).isEqualByComparingTo("0.025");
    }

    @Test
    void shouldApply7PercentForMediumWeight() {
        // 200g vegetables at €2.00
        VegetablePricingContext ctx = new VegetablePricingContext(
                200, new BigDecimal("0.01"), new BigDecimal("2.00")
        );

        BigDecimal discount = rule.calculateDiscount(ctx);

        // 7% of €2.00 = €0.14
        assertThat(discount).isEqualByComparingTo("0.14");
    }

    @Test
    void shouldApply10PercentForLargeWeight() {
        // 600g vegetables at €6.00
        VegetablePricingContext ctx = new VegetablePricingContext(
                600, new BigDecimal("0.01"), new BigDecimal("6.00")
        );

        BigDecimal discount = rule.calculateDiscount(ctx);

        // 10% of €6.00 = €0.60
        assertThat(discount).isEqualByComparingTo("0.60");
    }

    @Test
    void shouldHandleBoundaryAt100g() {
        // Exactly 100g should get 7% (medium tier)
        VegetablePricingContext ctx = new VegetablePricingContext(
                100, new BigDecimal("0.01"), new BigDecimal("1.00")
        );

        BigDecimal discount = rule.calculateDiscount(ctx);

        assertThat(discount).isEqualByComparingTo("0.07");
    }

    @Test
    void shouldHandleBoundaryAt500g() {
        // Exactly 500g should get 10% (large tier)
        VegetablePricingContext ctx = new VegetablePricingContext(
                500, new BigDecimal("0.01"), new BigDecimal("5.00")
        );

        BigDecimal discount = rule.calculateDiscount(ctx);

        assertThat(discount).isEqualByComparingTo("0.50");
    }
}
```

## Files to Create

- `src/main/java/com/grocery/pricing/pricing/discount/VegetableWeightTierRule.java`
- `src/test/java/com/grocery/pricing/pricing/discount/VegetableWeightTierRuleTest.java`

## Acceptance Criteria

- [x] VegetableWeightTierRule implements VegetableDiscountRule interface
- [x] 5% discount applied for weight < 100g
- [x] 7% discount applied for weight 100-499g
- [x] 10% discount applied for weight >= 500g
- [x] Boundary conditions handled correctly (100g, 500g)
- [x] Uses configuration for thresholds (not hardcoded)
- [x] All unit tests pass
