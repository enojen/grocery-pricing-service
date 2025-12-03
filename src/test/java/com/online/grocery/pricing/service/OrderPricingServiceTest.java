package com.online.grocery.pricing.service;

import com.online.grocery.pricing.domain.enums.BeerOrigin;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.domain.model.*;
import com.online.grocery.pricing.pricing.strategy.PricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OrderPricingServiceTest {

    private PricingStrategy breadStrategy;
    private PricingStrategy vegetableStrategy;
    private PricingStrategy beerStrategy;
    private OrderPricingService service;

    @BeforeEach
    void setUp() {
        breadStrategy = mock(PricingStrategy.class);
        vegetableStrategy = mock(PricingStrategy.class);
        beerStrategy = mock(PricingStrategy.class);

        when(breadStrategy.getProductType()).thenReturn(ProductType.BREAD);
        when(vegetableStrategy.getProductType()).thenReturn(ProductType.VEGETABLE);
        when(beerStrategy.getProductType()).thenReturn(ProductType.BEER);

        service = new OrderPricingService(List.of(breadStrategy, vegetableStrategy, beerStrategy));
    }

    @Test
    void shouldCalculateReceiptForMixedOrder() {
        BreadItem bread = new BreadItem("Bread", 3, 3);
        VegetableItem veg = new VegetableItem("Carrots", 200);
        BeerItem beer = new BeerItem("Heineken", 6, BeerOrigin.DUTCH);
        Order order = new Order(List.of(bread, veg, beer));

        when(breadStrategy.calculatePrice(anyList())).thenReturn(List.of(
                new ReceiptLine("3 x Bread (3 days old)", new BigDecimal("3.00"), new BigDecimal("1.00"), new BigDecimal("2.00"))
        ));
        when(vegetableStrategy.calculatePrice(anyList())).thenReturn(List.of(
                new ReceiptLine("200g Vegetables", new BigDecimal("2.00"), new BigDecimal("0.14"), new BigDecimal("1.86"))
        ));
        when(beerStrategy.calculatePrice(anyList())).thenReturn(List.of(
                new ReceiptLine("6 x DUTCH Beer (1 packs + 0 singles)", new BigDecimal("3.00"), new BigDecimal("2.00"), new BigDecimal("1.00"))
        ));

        Receipt receipt = service.calculateReceipt(order);

        assertThat(receipt.lines()).hasSize(3);
        assertThat(receipt.subtotal()).isEqualByComparingTo("8.00");
        assertThat(receipt.totalDiscount()).isEqualByComparingTo("3.14");
        assertThat(receipt.total()).isEqualByComparingTo("4.86");
    }

    @Test
    void shouldDelegateToCorrectStrategy() {
        BreadItem bread = new BreadItem("Bread", 1, 0);
        Order order = new Order(List.of(bread));

        when(breadStrategy.calculatePrice(anyList())).thenReturn(List.of(
                new ReceiptLine("1 x Bread", new BigDecimal("1.00"), BigDecimal.ZERO, new BigDecimal("1.00"))
        ));

        service.calculateReceipt(order);

        verify(breadStrategy).calculatePrice(anyList());
        verify(vegetableStrategy, never()).calculatePrice(anyList());
        verify(beerStrategy, never()).calculatePrice(anyList());
    }

    @Test
    void shouldHandleSingleProductTypeOrder() {
        VegetableItem veg1 = new VegetableItem("Carrots", 100);
        VegetableItem veg2 = new VegetableItem("Potatoes", 200);
        Order order = new Order(List.of(veg1, veg2));

        when(vegetableStrategy.calculatePrice(anyList())).thenReturn(List.of(
                new ReceiptLine("300g Vegetables", new BigDecimal("3.00"), new BigDecimal("0.21"), new BigDecimal("2.79"))
        ));

        Receipt receipt = service.calculateReceipt(order);

        assertThat(receipt.lines()).hasSize(1);
        assertThat(receipt.total()).isEqualByComparingTo("2.79");
    }

    @Test
    void shouldCalculateCorrectTotals() {
        BreadItem bread = new BreadItem("Bread", 2, 0);
        Order order = new Order(List.of(bread));

        when(breadStrategy.calculatePrice(anyList())).thenReturn(List.of(
                new ReceiptLine("2 x Bread (0 days old)", new BigDecimal("2.00"), BigDecimal.ZERO, new BigDecimal("2.00"))
        ));

        Receipt receipt = service.calculateReceipt(order);

        assertThat(receipt.subtotal()).isEqualByComparingTo("2.00");
        assertThat(receipt.totalDiscount()).isEqualByComparingTo("0.00");
        assertThat(receipt.total()).isEqualByComparingTo("2.00");
    }

    @Test
    void shouldThrowExceptionForUnknownProductType() {
        PricingStrategy onlyBreadStrategy = mock(PricingStrategy.class);
        when(onlyBreadStrategy.getProductType()).thenReturn(ProductType.BREAD);
        OrderPricingService limitedService = new OrderPricingService(List.of(onlyBreadStrategy));

        VegetableItem veg = new VegetableItem("Carrots", 100);
        Order order = new Order(List.of(veg));

        assertThatThrownBy(() -> limitedService.calculateReceipt(order))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No pricing strategy registered for product type: VEGETABLE");
    }
}
