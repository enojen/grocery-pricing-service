# TASK-010: Bread Discount Rule

## Status
- [x] Completed

## Phase
Phase 2: Pricing Logic

## Description
Implement BreadAgeBundleRule for age-based bundle discounts on bread items.

## Business Rules

**Bread Age-Based Discounts** (applied per age group):
- **0-2 days old**: No discount
- **3-5 days old**: "Buy 1 take 2" (in groups of 2, pay for 1)
- **6 days old**: "Buy 1 take 3" (in groups of 3, pay for 1)
- **>6 days**: Invalid (rejected at domain level)

## Implementation Details

### BreadAgeBundleRule

```java
package com.grocery.pricing.pricing.discount;

import com.grocery.pricing.config.PricingConfiguration;
import com.grocery.pricing.pricing.context.BreadPricingContext;
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
public class BreadAgeBundleRule implements BreadDiscountRule {

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
            // For qty=3: 3/2 = 1 free item
            // For qty=4: 4/2 = 2 free items
            int freeItems = qty / 2;
            return unitPrice.multiply(BigDecimal.valueOf(freeItems));
        }

        if (age == specialAge) {
            // "Buy 1 take 3": In groups of 3, pay for 1
            // For qty=3: 1 group × 2 free = 2 free items
            // For qty=6: 2 groups × 2 free = 4 free items
            int groups = qty / 3;
            int freeItems = groups * 2;
            return unitPrice.multiply(BigDecimal.valueOf(freeItems));
        }

        return BigDecimal.ZERO;
    }

    @Override
    public int order() {
        return 100; // Age-based discounts first
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
```

### Unit Tests

```java
package com.grocery.pricing.pricing.discount;

import com.grocery.pricing.config.PricingConfiguration;
import com.grocery.pricing.pricing.context.BreadPricingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BreadAgeBundleRuleTest {

    private PricingConfiguration config;
    private BreadAgeBundleRule rule;

    @BeforeEach
    void setUp() {
        config = mock(PricingConfiguration.class);
        PricingConfiguration.BreadRules breadRules = mock(PricingConfiguration.BreadRules.class);
        
        when(config.getBread()).thenReturn(breadRules);
        when(breadRules.getBundleDiscountMinAge()).thenReturn(3);
        when(breadRules.getSpecialBundleAge()).thenReturn(6);
        
        rule = new BreadAgeBundleRule(config);
    }

    @ParameterizedTest
    @CsvSource({
        "0, false",  // Too fresh
        "1, false",  // Too fresh
        "2, false",  // Too fresh
        "3, true",   // In discount range
        "4, true",   // In discount range
        "5, true",   // In discount range
        "6, true"    // Special age
    })
    void shouldCheckApplicability(int age, boolean expected) {
        BreadPricingContext ctx = new BreadPricingContext(
            age, 3, BigDecimal.ONE, new BigDecimal("3.00")
        );
        
        assertThat(rule.isApplicable(ctx)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        // age, quantity, expectedDiscount
        "3, 2, 1.00",   // 2 breads at 3 days: 1 free
        "3, 3, 1.00",   // 3 breads at 3 days: 1 free (3/2=1)
        "3, 4, 2.00",   // 4 breads at 3 days: 2 free (4/2=2)
        "3, 5, 2.00",   // 5 breads at 3 days: 2 free (5/2=2)
        "4, 6, 3.00",   // 6 breads at 4 days: 3 free (6/2=3)
        "5, 1, 0.00"    // 1 bread at 5 days: 0 free (1/2=0)
    })
    void shouldCalculateBuyOneTakeTwoDiscount(int age, int qty, String expectedDiscount) {
        BreadPricingContext ctx = new BreadPricingContext(
            age, qty, BigDecimal.ONE, BigDecimal.valueOf(qty)
        );
        
        BigDecimal discount = rule.calculateDiscount(ctx);
        
        assertThat(discount).isEqualByComparingTo(expectedDiscount);
    }

    @ParameterizedTest
    @CsvSource({
        // quantity, expectedDiscount
        "3, 2.00",   // 1 group × 2 free = 2 free
        "6, 4.00",   // 2 groups × 2 free = 4 free
        "7, 4.00",   // 2 groups × 2 free = 4 free (7/3=2)
        "9, 6.00",   // 3 groups × 2 free = 6 free
        "1, 0.00",   // 0 groups
        "2, 0.00"    // 0 groups
    })
    void shouldCalculateBuyOneTakeThreeDiscount(int qty, String expectedDiscount) {
        BreadPricingContext ctx = new BreadPricingContext(
            6, qty, BigDecimal.ONE, BigDecimal.valueOf(qty)
        );
        
        BigDecimal discount = rule.calculateDiscount(ctx);
        
        assertThat(discount).isEqualByComparingTo(expectedDiscount);
    }

    @Test
    void shouldReturnZeroForFreshBread() {
        BreadPricingContext ctx = new BreadPricingContext(
            1, 10, BigDecimal.ONE, new BigDecimal("10.00")
        );
        
        // Rule is not applicable for age < 3
        assertThat(rule.isApplicable(ctx)).isFalse();
    }
}
```

## Files to Create

- `src/main/java/com/grocery/pricing/pricing/discount/BreadAgeBundleRule.java`
- `src/test/java/com/grocery/pricing/pricing/discount/BreadAgeBundleRuleTest.java`

## Acceptance Criteria

- [x] BreadAgeBundleRule implements BreadDiscountRule interface
- [x] Correctly identifies applicable age ranges (3-6 days)
- [x] "Buy 1 take 2" discount calculated correctly for 3-5 days old
- [x] "Buy 1 take 3" discount calculated correctly for 6 days old
- [x] Returns zero discount for bread 0-2 days old
- [x] Uses configuration for age thresholds (not hardcoded)
- [x] All unit tests pass
