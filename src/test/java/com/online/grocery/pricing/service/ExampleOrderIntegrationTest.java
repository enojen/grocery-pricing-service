package com.online.grocery.pricing.service;

import com.online.grocery.pricing.domain.enums.BeerOrigin;
import com.online.grocery.pricing.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Critical integration test validating the example order calculation.
 * This test ensures all pricing rules work together correctly.
 */
@SpringBootTest
class ExampleOrderIntegrationTest {

    @Autowired
    private OrderPricingService orderPricingService;

    @Test
    void shouldCalculateExampleOrderCorrectly() {
        // Given: Example order from requirements
        Order order = new Order(List.of(
                new BreadItem(3, 3),
                new VegetableItem(200),
                new BeerItem(6, BeerOrigin.DUTCH)
        ));

        // When
        Receipt receipt = orderPricingService.calculateReceipt(order);

        // Then - Validate bread line
        ReceiptLine breadLine = findLineByDescription(receipt, "Bread");
        assertThat(breadLine.originalPrice()).isEqualByComparingTo("3.00");
        assertThat(breadLine.discount()).isEqualByComparingTo("1.00");
        assertThat(breadLine.finalPrice()).isEqualByComparingTo("2.00");

        // Then - Validate vegetables line
        ReceiptLine vegLine = findLineByDescription(receipt, "Vegetables");
        assertThat(vegLine.originalPrice()).isEqualByComparingTo("2.00");
        assertThat(vegLine.discount()).isEqualByComparingTo("0.14");
        assertThat(vegLine.finalPrice()).isEqualByComparingTo("1.86");

        // Then - Validate beer line
        ReceiptLine beerLine = findLineByDescription(receipt, "Beer");
        assertThat(beerLine.originalPrice()).isEqualByComparingTo("3.00");
        assertThat(beerLine.discount()).isEqualByComparingTo("2.00");
        assertThat(beerLine.finalPrice()).isEqualByComparingTo("1.00");

        // Then - Validate totals (includes 5% combo discount for bread + vegetables)
        // Combo discount: 5% of 4.86 = 0.243, total discount: 3.14 + 0.243 = 3.383
        assertThat(receipt.subtotal()).isEqualByComparingTo("8.00");
        assertThat(receipt.totalDiscount()).isEqualByComparingTo("3.383");
        assertThat(receipt.total()).isEqualByComparingTo("4.617");
    }

    @Test
    void shouldCalculateBreadDiscountFor3DaysOld() {
        // Buy 1 take 2: For 3 breads, 1 is free
        Order order = new Order(List.of(
                new BreadItem(3, 3)
        ));

        Receipt receipt = orderPricingService.calculateReceipt(order);

        assertThat(receipt.total()).isEqualByComparingTo("2.00");
    }

    @Test
    void shouldCalculateBreadDiscountFor6DaysOld() {
        // Buy 1 take 3: For 3 breads, 2 are free (1 group of 3)
        Order order = new Order(List.of(
                new BreadItem(3, 6)
        ));

        Receipt receipt = orderPricingService.calculateReceipt(order);

        // 3 breads x 1.00 = 3.00, discount = 2 x 1.00 = 2.00
        assertThat(receipt.subtotal()).isEqualByComparingTo("3.00");
        assertThat(receipt.totalDiscount()).isEqualByComparingTo("2.00");
        assertThat(receipt.total()).isEqualByComparingTo("1.00");
    }

    @Test
    void shouldCalculateVegetableDiscount5PercentForSmallWeight() {
        // < 100g = 5% discount
        Order order = new Order(List.of(
                new VegetableItem(50)
        ));

        Receipt receipt = orderPricingService.calculateReceipt(order);

        // 50g x 0.01/g = 0.50, discount = 5% = 0.025 (rounded to 0.03)
        assertThat(receipt.subtotal()).isEqualByComparingTo("0.50");
        assertThat(receipt.total()).isEqualByComparingTo("0.47");
    }

    @Test
    void shouldCalculateVegetableDiscount10PercentForLargeWeight() {
        // > 500g = 10% discount
        Order order = new Order(List.of(
                new VegetableItem(501)
        ));

        Receipt receipt = orderPricingService.calculateReceipt(order);

        // 501g x 0.01/g = 5.01, discount = 10% = 0.501
        assertThat(receipt.subtotal()).isEqualByComparingTo("5.01");
        assertThat(receipt.totalDiscount()).isEqualByComparingTo("0.50");
        assertThat(receipt.total()).isEqualByComparingTo("4.51");
    }

    @Test
    void shouldCalculateBeerPackDiscountForBelgian() {
        // Belgian: 6 x 0.60 = 3.60
        // Pack discount: 3.00 + Buy 2 Get 1 Free: 1.20 = 4.20 (capped at 3.60)
        Order order = new Order(List.of(
                new BeerItem(6, BeerOrigin.BELGIAN)
        ));

        Receipt receipt = orderPricingService.calculateReceipt(order);

        assertThat(receipt.subtotal()).isEqualByComparingTo("3.60");
        assertThat(receipt.totalDiscount()).isEqualByComparingTo("3.60");
        assertThat(receipt.total()).isEqualByComparingTo("0.00");
    }

    @Test
    void shouldCalculateBeerPackDiscountForGerman() {
        // German: 12-pack size, 12 x 0.80 = 9.60, discount 4.00, final 5.60
        Order order = new Order(List.of(
                new BeerItem(12, BeerOrigin.GERMAN)
        ));

        Receipt receipt = orderPricingService.calculateReceipt(order);

        assertThat(receipt.subtotal()).isEqualByComparingTo("9.60");
        assertThat(receipt.totalDiscount()).isEqualByComparingTo("4.00");
        assertThat(receipt.total()).isEqualByComparingTo("5.60");
    }

    @Test
    void shouldNotApplyBeerDiscountForSingles() {
        // 5 beers = 0 packs, 5 singles, no discount
        Order order = new Order(List.of(
                new BeerItem(5, BeerOrigin.DUTCH)
        ));

        Receipt receipt = orderPricingService.calculateReceipt(order);

        assertThat(receipt.subtotal()).isEqualByComparingTo("2.50");
        assertThat(receipt.totalDiscount()).isEqualByComparingTo("0.00");
        assertThat(receipt.total()).isEqualByComparingTo("2.50");
    }

    private ReceiptLine findLineByDescription(Receipt receipt, String keyword) {
        return receipt.lines().stream()
                .filter(line -> line.description().toLowerCase().contains(keyword.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No receipt line found containing: " + keyword
                ));
    }
}
