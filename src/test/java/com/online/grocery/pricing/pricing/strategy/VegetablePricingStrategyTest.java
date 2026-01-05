package com.online.grocery.pricing.pricing.strategy;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.OrderItem;
import com.online.grocery.pricing.domain.model.ReceiptLine;
import com.online.grocery.pricing.domain.model.VegetableItem;
import com.online.grocery.pricing.pricing.context.VegetablePricingContext;
import com.online.grocery.pricing.pricing.discount.DiscountRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class VegetablePricingStrategyTest {

    private PricingConfiguration config;
    private DiscountRule<VegetablePricingContext> discountRule;
    private VegetablePricingStrategy strategy;

    @BeforeEach
    void setUp() {
        config = mock(PricingConfiguration.class);
        discountRule = mock(DiscountRule.class);

        when(config.getVegetablePricePer100g()).thenReturn(new BigDecimal("1.00"));
        when(discountRule.order()).thenReturn(100);

        strategy = new VegetablePricingStrategy(config, List.of(discountRule));
    }

    @Test
    void shouldReturnVegetableProductType() {
        assertThat(strategy.getProductType()).isEqualTo(ProductType.VEGETABLE);
    }

    @Test
    void shouldCalculatePriceForSmallWeight() {
        when(discountRule.isApplicable(any())).thenReturn(true);
        when(discountRule.calculateDiscount(any())).thenReturn(new BigDecimal("0.025"));

        List<OrderItem> items = List.of(new VegetableItem(50));
        List<ReceiptLine> result = strategy.calculatePrice(items);

        assertThat(result).hasSize(1);
        ReceiptLine line = result.get(0);
        assertThat(line.description()).isEqualTo("50g Vegetables");
        assertThat(line.originalPrice()).isEqualByComparingTo("0.50");
    }

    @Test
    void shouldCalculatePriceForMediumWeight() {
        when(discountRule.isApplicable(any())).thenReturn(true);
        when(discountRule.calculateDiscount(any())).thenReturn(new BigDecimal("0.21"));

        List<OrderItem> items = List.of(new VegetableItem(300));
        List<ReceiptLine> result = strategy.calculatePrice(items);

        assertThat(result).hasSize(1);
        ReceiptLine line = result.get(0);
        assertThat(line.description()).isEqualTo("300g Vegetables");
        assertThat(line.originalPrice()).isEqualByComparingTo("3.00");
    }

    @Test
    void shouldCalculatePriceForLargeWeight() {
        when(discountRule.isApplicable(any())).thenReturn(true);
        when(discountRule.calculateDiscount(any())).thenReturn(new BigDecimal("0.50"));

        List<OrderItem> items = List.of(new VegetableItem(500));
        List<ReceiptLine> result = strategy.calculatePrice(items);

        assertThat(result).hasSize(1);
        ReceiptLine line = result.get(0);
        assertThat(line.description()).isEqualTo("500g Vegetables");
        assertThat(line.originalPrice()).isEqualByComparingTo("5.00");
    }

    @Test
    void shouldAggregateAllVegetables() {
        when(discountRule.isApplicable(any())).thenReturn(false);

        List<OrderItem> items = List.of(
                new VegetableItem(100),
                new VegetableItem(200),
                new VegetableItem(150)
        );
        List<ReceiptLine> result = strategy.calculatePrice(items);

        assertThat(result).hasSize(1);
        ReceiptLine line = result.get(0);
        assertThat(line.description()).isEqualTo("450g Vegetables");
        assertThat(line.originalPrice()).isEqualByComparingTo("4.50");
        assertThat(line.discount()).isEqualByComparingTo("0.00");
        assertThat(line.finalPrice()).isEqualByComparingTo("4.50");
    }

    @Test
    void shouldApplyDiscount() {
        when(discountRule.isApplicable(any())).thenReturn(true);
        when(discountRule.calculateDiscount(any())).thenReturn(new BigDecimal("0.50"));

        List<OrderItem> items = List.of(new VegetableItem(500));
        List<ReceiptLine> result = strategy.calculatePrice(items);

        ReceiptLine line = result.get(0);
        assertThat(line.originalPrice()).isEqualByComparingTo("5.00");
        assertThat(line.discount()).isEqualByComparingTo("0.50");
        assertThat(line.finalPrice()).isEqualByComparingTo("4.50");
    }

    @Test
    void shouldApplyMultipleDiscountRulesInOrder() {
        DiscountRule<VegetablePricingContext> rule1 = mock(DiscountRule.class);
        DiscountRule<VegetablePricingContext> rule2 = mock(DiscountRule.class);

        when(rule1.order()).thenReturn(200);
        when(rule2.order()).thenReturn(100);
        when(rule1.isApplicable(any())).thenReturn(true);
        when(rule2.isApplicable(any())).thenReturn(true);
        when(rule1.calculateDiscount(any())).thenReturn(new BigDecimal("0.10"));
        when(rule2.calculateDiscount(any())).thenReturn(new BigDecimal("0.20"));

        VegetablePricingStrategy strategyWithMultipleRules = new VegetablePricingStrategy(
                config, List.of(rule1, rule2)
        );

        List<OrderItem> items = List.of(new VegetableItem(200));
        List<ReceiptLine> result = strategyWithMultipleRules.calculatePrice(items);

        assertThat(result.get(0).discount()).isEqualByComparingTo("0.30");
    }

    @Test
    void shouldNormalizeMonetaryValues() {
        when(config.getVegetablePricePer100g()).thenReturn(new BigDecimal("1.00"));
        when(discountRule.isApplicable(any())).thenReturn(false);

        List<OrderItem> items = List.of(new VegetableItem(33));
        List<ReceiptLine> result = strategy.calculatePrice(items);

        assertThat(result.get(0).originalPrice().scale()).isEqualTo(2);
        assertThat(result.get(0).finalPrice().scale()).isEqualTo(2);
    }

    @Test
    void shouldReturnSingleReceiptLine() {
        when(discountRule.isApplicable(any())).thenReturn(false);

        List<OrderItem> items = List.of(
                new VegetableItem(100),
                new VegetableItem(100),
                new VegetableItem(100)
        );
        List<ReceiptLine> result = strategy.calculatePrice(items);

        assertThat(result).hasSize(1);
    }
}
