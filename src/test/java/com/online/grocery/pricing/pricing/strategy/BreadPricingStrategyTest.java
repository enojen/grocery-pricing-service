package com.online.grocery.pricing.pricing.strategy;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.BreadItem;
import com.online.grocery.pricing.domain.model.OrderItem;
import com.online.grocery.pricing.domain.model.ReceiptLine;
import com.online.grocery.pricing.pricing.discount.BreadDiscountRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BreadPricingStrategyTest {

    private PricingConfiguration config;
    private BreadDiscountRule discountRule;
    private BreadPricingStrategy strategy;

    @BeforeEach
    void setUp() {
        config = mock(PricingConfiguration.class);
        discountRule = mock(BreadDiscountRule.class);

        when(config.getBreadPrice()).thenReturn(new BigDecimal("1.00"));
        when(discountRule.order()).thenReturn(100);

        strategy = new BreadPricingStrategy(config, List.of(discountRule));
    }

    @Test
    void shouldReturnBreadProductType() {
        assertThat(strategy.getProductType()).isEqualTo(ProductType.BREAD);
    }

    @Test
    void shouldCalculatePriceForFreshBread() {
        when(discountRule.isApplicable(any())).thenReturn(false);

        List<OrderItem> items = List.of(new BreadItem(2, 0));
        List<ReceiptLine> result = strategy.calculatePrice(items);

        assertThat(result).hasSize(1);
        ReceiptLine line = result.get(0);
        assertThat(line.description()).isEqualTo("2 x Bread (0 days old)");
        assertThat(line.originalPrice()).isEqualByComparingTo("2.00");
        assertThat(line.discount()).isEqualByComparingTo("0.00");
        assertThat(line.finalPrice()).isEqualByComparingTo("2.00");
    }

    @Test
    void shouldCalculatePriceWithDiscount() {
        when(discountRule.isApplicable(any())).thenReturn(true);
        when(discountRule.calculateDiscount(any())).thenReturn(new BigDecimal("1.00"));

        List<OrderItem> items = List.of(new BreadItem(4, 3));
        List<ReceiptLine> result = strategy.calculatePrice(items);

        assertThat(result).hasSize(1);
        ReceiptLine line = result.get(0);
        assertThat(line.description()).isEqualTo("4 x Bread (3 days old)");
        assertThat(line.originalPrice()).isEqualByComparingTo("4.00");
        assertThat(line.discount()).isEqualByComparingTo("1.00");
        assertThat(line.finalPrice()).isEqualByComparingTo("3.00");
    }

    @Test
    void shouldGroupBreadByAge() {
        when(discountRule.isApplicable(any())).thenReturn(false);

        List<OrderItem> items = List.of(
                new BreadItem(2, 0),
                new BreadItem(3, 3),
                new BreadItem(1, 0)
        );
        List<ReceiptLine> result = strategy.calculatePrice(items);

        assertThat(result).hasSize(2);

        ReceiptLine freshLine = result.stream()
                .filter(l -> l.description().contains("0 days old"))
                .findFirst().orElseThrow();
        assertThat(freshLine.description()).isEqualTo("3 x Bread (0 days old)");
        assertThat(freshLine.originalPrice()).isEqualByComparingTo("3.00");

        ReceiptLine oldLine = result.stream()
                .filter(l -> l.description().contains("3 days old"))
                .findFirst().orElseThrow();
        assertThat(oldLine.description()).isEqualTo("3 x Bread (3 days old)");
        assertThat(oldLine.originalPrice()).isEqualByComparingTo("3.00");
    }

    @Test
    void shouldApplyDiscountRulesInOrder() {
        BreadDiscountRule rule1 = mock(BreadDiscountRule.class);
        BreadDiscountRule rule2 = mock(BreadDiscountRule.class);

        when(rule1.order()).thenReturn(200);
        when(rule2.order()).thenReturn(100);
        when(rule1.isApplicable(any())).thenReturn(true);
        when(rule2.isApplicable(any())).thenReturn(true);
        when(rule1.calculateDiscount(any())).thenReturn(new BigDecimal("0.50"));
        when(rule2.calculateDiscount(any())).thenReturn(new BigDecimal("0.30"));

        BreadPricingStrategy strategyWithMultipleRules = new BreadPricingStrategy(
                config, List.of(rule1, rule2)
        );

        List<OrderItem> items = List.of(new BreadItem(2, 4));
        List<ReceiptLine> result = strategyWithMultipleRules.calculatePrice(items);

        assertThat(result.get(0).discount()).isEqualByComparingTo("0.80");
    }

    @Test
    void shouldFilterNonBreadItems() {
        when(discountRule.isApplicable(any())).thenReturn(false);

        List<OrderItem> items = List.of(new BreadItem(1, 0));
        List<ReceiptLine> result = strategy.calculatePrice(items);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldNormalizeMonetaryValues() {
        when(config.getBreadPrice()).thenReturn(new BigDecimal("1.005"));
        when(discountRule.isApplicable(any())).thenReturn(false);

        BreadPricingStrategy strategyWithPrecision = new BreadPricingStrategy(
                config, List.of(discountRule)
        );

        List<OrderItem> items = List.of(new BreadItem(1, 0));
        List<ReceiptLine> result = strategyWithPrecision.calculatePrice(items);

        assertThat(result.get(0).originalPrice().scale()).isEqualTo(2);
    }
}
