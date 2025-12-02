package com.online.grocery.pricing.pricing.discount;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.pricing.context.VegetablePricingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        "50, 0.50, 0.025",
        "99, 0.99, 0.0495",
        "100, 1.00, 0.07",
        "200, 2.00, 0.14",
        "499, 4.99, 0.3493",
        "500, 5.00, 0.50",
        "1000, 10.00, 1.00"
    })
    void shouldCalculateCorrectDiscount(int weight, String originalPrice, String expectedDiscount) {
        BigDecimal pricePerGram = new BigDecimal("0.01");
        VegetablePricingContext ctx = new VegetablePricingContext(
            weight, pricePerGram, new BigDecimal(originalPrice)
        );

        BigDecimal discount = rule.calculateDiscount(ctx);

        assertThat(discount).isEqualByComparingTo(expectedDiscount);
    }

    @Test
    void shouldApply5PercentForSmallWeight() {
        VegetablePricingContext ctx = new VegetablePricingContext(
            50, new BigDecimal("0.01"), new BigDecimal("0.50")
        );

        BigDecimal discount = rule.calculateDiscount(ctx);

        assertThat(discount).isEqualByComparingTo("0.025");
    }

    @Test
    void shouldApply7PercentForMediumWeight() {
        VegetablePricingContext ctx = new VegetablePricingContext(
            200, new BigDecimal("0.01"), new BigDecimal("2.00")
        );

        BigDecimal discount = rule.calculateDiscount(ctx);

        assertThat(discount).isEqualByComparingTo("0.14");
    }

    @Test
    void shouldApply10PercentForLargeWeight() {
        VegetablePricingContext ctx = new VegetablePricingContext(
            600, new BigDecimal("0.01"), new BigDecimal("6.00")
        );

        BigDecimal discount = rule.calculateDiscount(ctx);

        assertThat(discount).isEqualByComparingTo("0.60");
    }

    @Test
    void shouldHandleBoundaryAt100g() {
        VegetablePricingContext ctx = new VegetablePricingContext(
            100, new BigDecimal("0.01"), new BigDecimal("1.00")
        );

        BigDecimal discount = rule.calculateDiscount(ctx);

        assertThat(discount).isEqualByComparingTo("0.07");
    }

    @Test
    void shouldHandleBoundaryAt500g() {
        VegetablePricingContext ctx = new VegetablePricingContext(
            500, new BigDecimal("0.01"), new BigDecimal("5.00")
        );

        BigDecimal discount = rule.calculateDiscount(ctx);

        assertThat(discount).isEqualByComparingTo("0.50");
    }

    @Test
    void shouldReturnCorrectOrder() {
        assertThat(rule.order()).isEqualTo(100);
    }

    @Test
    void shouldReturnDescription() {
        String description = rule.description();

        assertThat(description).contains("Weight-based discounts");
        assertThat(description).contains("100g");
        assertThat(description).contains("500g");
    }
}
