package com.online.grocery.pricing.pricing.discount;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.pricing.context.BreadPricingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
            "0, false",
            "1, false",
            "2, false",
            "3, true",
            "4, true",
            "5, true",
            "6, true"
    })
    void shouldCheckApplicability(int age, boolean expected) {
        BreadPricingContext ctx = new BreadPricingContext(
                age, 3, BigDecimal.ONE, new BigDecimal("3.00")
        );

        assertThat(rule.isApplicable(ctx)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "3, 2, 1.00",
            "3, 3, 1.00",
            "3, 4, 2.00",
            "3, 5, 2.00",
            "4, 6, 3.00",
            "5, 1, 0.00"
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
            "3, 2.00",
            "6, 4.00",
            "7, 4.00",
            "9, 6.00",
            "1, 0.00",
            "2, 0.00"
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

        assertThat(rule.isApplicable(ctx)).isFalse();
    }

    @Test
    void shouldHaveCorrectOrder() {
        assertThat(rule.order()).isEqualTo(100);
    }

    @Test
    void shouldHaveDescription() {
        assertThat(rule.description()).contains("bundle");
    }
}
