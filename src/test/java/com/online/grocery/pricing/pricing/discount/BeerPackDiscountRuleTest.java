package com.online.grocery.pricing.pricing.discount;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.domain.enums.BeerOrigin;
import com.online.grocery.pricing.pricing.context.BeerPricingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        BeerPricingContext ctx = new BeerPricingContext(
                BeerOrigin.BELGIAN, 6, 1, 0, new BigDecimal("0.60"), new BigDecimal("3.60")
        );

        BigDecimal discount = rule.calculateDiscount(ctx);

        assertThat(discount).isEqualByComparingTo("3.00");
    }

    @Test
    void shouldCalculateDutchPackDiscount() {
        BeerPricingContext ctx = new BeerPricingContext(
                BeerOrigin.DUTCH, 6, 1, 0, new BigDecimal("0.50"), new BigDecimal("3.00")
        );

        BigDecimal discount = rule.calculateDiscount(ctx);

        assertThat(discount).isEqualByComparingTo("2.00");
    }

    @Test
    void shouldCalculateGermanPackDiscount() {
        BeerPricingContext ctx = new BeerPricingContext(
                BeerOrigin.GERMAN, 6, 1, 0, new BigDecimal("0.80"), new BigDecimal("4.80")
        );

        BigDecimal discount = rule.calculateDiscount(ctx);

        assertThat(discount).isEqualByComparingTo("4.00");
    }

    @ParameterizedTest
    @CsvSource({
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
        BeerPricingContext ctx = new BeerPricingContext(
                BeerOrigin.BELGIAN, 7, 1, 1, new BigDecimal("0.60"), new BigDecimal("4.20")
        );

        BigDecimal discount = rule.calculateDiscount(ctx);

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
        BigDecimal originalPrice = new BigDecimal("4.80");
        BeerPricingContext ctx = new BeerPricingContext(
                BeerOrigin.GERMAN, 6, 1, 0, new BigDecimal("0.80"), originalPrice
        );

        BigDecimal discount = rule.calculateDiscount(ctx);
        BigDecimal finalPrice = originalPrice.subtract(discount);

        assertThat(finalPrice).isEqualByComparingTo("0.80");
        assertThat(finalPrice).isPositive();
    }

    @Test
    void shouldReturnCorrectOrder() {
        assertThat(rule.order()).isEqualTo(100);
    }

    @Test
    void shouldReturnDescription() {
        String description = rule.description();

        assertThat(description).contains("Belgian");
        assertThat(description).contains("Dutch");
        assertThat(description).contains("German");
        assertThat(description).containsPattern("3[.,]00");
        assertThat(description).containsPattern("2[.,]00");
        assertThat(description).containsPattern("4[.,]00");
    }
}
