package com.online.grocery.pricing.pricing.strategy;

import com.online.grocery.pricing.config.PricingConfiguration;
import com.online.grocery.pricing.domain.enums.BeerOrigin;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.BeerItem;
import com.online.grocery.pricing.domain.model.OrderItem;
import com.online.grocery.pricing.domain.model.ReceiptLine;
import com.online.grocery.pricing.pricing.discount.BeerDiscountRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BeerPricingStrategyTest {

    private PricingConfiguration config;
    private PricingConfiguration.BeerRules beerRules;
    private BeerDiscountRule discountRule;
    private BeerPricingStrategy strategy;

    @BeforeEach
    void setUp() {
        config = mock(PricingConfiguration.class);
        beerRules = mock(PricingConfiguration.BeerRules.class);
        discountRule = mock(BeerDiscountRule.class);
        
        when(config.getBeer()).thenReturn(beerRules);
        when(beerRules.getPackSize()).thenReturn(6);
        when(beerRules.getBelgianBasePrice()).thenReturn(new BigDecimal("0.60"));
        when(beerRules.getDutchBasePrice()).thenReturn(new BigDecimal("0.50"));
        when(beerRules.getGermanBasePrice()).thenReturn(new BigDecimal("0.80"));
        when(discountRule.order()).thenReturn(100);
        
        strategy = new BeerPricingStrategy(config, List.of(discountRule));
    }

    @Test
    void shouldReturnBeerProductType() {
        assertThat(strategy.getProductType()).isEqualTo(ProductType.BEER);
    }

    @Test
    void shouldCalculatePriceForSingleBottles() {
        when(discountRule.isApplicable(any())).thenReturn(false);
        
        List<OrderItem> items = List.of(new BeerItem("Belgian Ale", 3, BeerOrigin.BELGIAN));
        List<ReceiptLine> result = strategy.calculatePrice(items);
        
        assertThat(result).hasSize(1);
        ReceiptLine line = result.get(0);
        assertThat(line.description()).isEqualTo("3 x BELGIAN Beer (0 packs + 3 singles)");
        assertThat(line.originalPrice()).isEqualByComparingTo("1.80");
        assertThat(line.discount()).isEqualByComparingTo("0.00");
        assertThat(line.finalPrice()).isEqualByComparingTo("1.80");
    }

    @Test
    void shouldCalculatePriceForPacks() {
        when(discountRule.isApplicable(any())).thenReturn(true);
        when(discountRule.calculateDiscount(any())).thenReturn(new BigDecimal("3.00"));
        
        List<OrderItem> items = List.of(new BeerItem("Belgian Ale", 6, BeerOrigin.BELGIAN));
        List<ReceiptLine> result = strategy.calculatePrice(items);
        
        assertThat(result).hasSize(1);
        ReceiptLine line = result.get(0);
        assertThat(line.description()).isEqualTo("6 x BELGIAN Beer (1 packs + 0 singles)");
        assertThat(line.originalPrice()).isEqualByComparingTo("3.60");
        assertThat(line.discount()).isEqualByComparingTo("3.00");
        assertThat(line.finalPrice()).isEqualByComparingTo("0.60");
    }

    @Test
    void shouldCalculatePriceForMixedPacksAndSingles() {
        when(discountRule.isApplicable(any())).thenReturn(true);
        when(discountRule.calculateDiscount(any())).thenReturn(new BigDecimal("2.00"));
        
        List<OrderItem> items = List.of(new BeerItem("Dutch Lager", 7, BeerOrigin.DUTCH));
        List<ReceiptLine> result = strategy.calculatePrice(items);
        
        assertThat(result).hasSize(1);
        ReceiptLine line = result.get(0);
        assertThat(line.description()).isEqualTo("7 x DUTCH Beer (1 packs + 1 singles)");
        assertThat(line.originalPrice()).isEqualByComparingTo("3.50");
    }

    @Test
    void shouldGroupBeerByOrigin() {
        when(discountRule.isApplicable(any())).thenReturn(false);
        
        List<OrderItem> items = List.of(
            new BeerItem("Belgian", 3, BeerOrigin.BELGIAN),
            new BeerItem("Dutch", 4, BeerOrigin.DUTCH),
            new BeerItem("More Belgian", 2, BeerOrigin.BELGIAN)
        );
        List<ReceiptLine> result = strategy.calculatePrice(items);
        
        assertThat(result).hasSize(2);
        
        ReceiptLine belgianLine = result.stream()
            .filter(l -> l.description().contains("BELGIAN"))
            .findFirst().orElseThrow();
        assertThat(belgianLine.description()).isEqualTo("5 x BELGIAN Beer (0 packs + 5 singles)");
        assertThat(belgianLine.originalPrice()).isEqualByComparingTo("3.00");
        
        ReceiptLine dutchLine = result.stream()
            .filter(l -> l.description().contains("DUTCH"))
            .findFirst().orElseThrow();
        assertThat(dutchLine.description()).isEqualTo("4 x DUTCH Beer (0 packs + 4 singles)");
        assertThat(dutchLine.originalPrice()).isEqualByComparingTo("2.00");
    }

    @Test
    void shouldCalculatePriceForGermanBeer() {
        when(discountRule.isApplicable(any())).thenReturn(false);
        
        List<OrderItem> items = List.of(new BeerItem("German Pilsner", 5, BeerOrigin.GERMAN));
        List<ReceiptLine> result = strategy.calculatePrice(items);
        
        assertThat(result).hasSize(1);
        ReceiptLine line = result.get(0);
        assertThat(line.description()).isEqualTo("5 x GERMAN Beer (0 packs + 5 singles)");
        assertThat(line.originalPrice()).isEqualByComparingTo("4.00");
    }

    @Test
    void shouldApplyDiscountRulesInOrder() {
        BeerDiscountRule rule1 = mock(BeerDiscountRule.class);
        BeerDiscountRule rule2 = mock(BeerDiscountRule.class);
        
        when(rule1.order()).thenReturn(200);
        when(rule2.order()).thenReturn(100);
        when(rule1.isApplicable(any())).thenReturn(true);
        when(rule2.isApplicable(any())).thenReturn(true);
        when(rule1.calculateDiscount(any())).thenReturn(new BigDecimal("1.00"));
        when(rule2.calculateDiscount(any())).thenReturn(new BigDecimal("0.50"));
        
        BeerPricingStrategy strategyWithMultipleRules = new BeerPricingStrategy(
            config, List.of(rule1, rule2)
        );
        
        List<OrderItem> items = List.of(new BeerItem("Beer", 6, BeerOrigin.BELGIAN));
        List<ReceiptLine> result = strategyWithMultipleRules.calculatePrice(items);
        
        assertThat(result.get(0).discount()).isEqualByComparingTo("1.50");
    }

    @Test
    void shouldCalculateMultiplePacks() {
        when(discountRule.isApplicable(any())).thenReturn(true);
        when(discountRule.calculateDiscount(any())).thenReturn(new BigDecimal("6.00"));
        
        List<OrderItem> items = List.of(new BeerItem("Belgian", 13, BeerOrigin.BELGIAN));
        List<ReceiptLine> result = strategy.calculatePrice(items);
        
        assertThat(result).hasSize(1);
        ReceiptLine line = result.get(0);
        assertThat(line.description()).isEqualTo("13 x BELGIAN Beer (2 packs + 1 singles)");
        assertThat(line.originalPrice()).isEqualByComparingTo("7.80");
    }

    @Test
    void shouldNormalizeMonetaryValues() {
        when(discountRule.isApplicable(any())).thenReturn(false);
        
        List<OrderItem> items = List.of(new BeerItem("Beer", 1, BeerOrigin.BELGIAN));
        List<ReceiptLine> result = strategy.calculatePrice(items);
        
        assertThat(result.get(0).originalPrice().scale()).isEqualTo(2);
        assertThat(result.get(0).finalPrice().scale()).isEqualTo(2);
    }
}
