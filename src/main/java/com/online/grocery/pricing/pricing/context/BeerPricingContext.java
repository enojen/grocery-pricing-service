package com.online.grocery.pricing.pricing.context;

import com.online.grocery.pricing.domain.enums.BeerOrigin;

import java.math.BigDecimal;

/**
 * Context for beer pricing calculations.
 * Encapsulates all data needed by beer discount rules.
 *
 * @param origin          Beer origin (BELGIAN, DUTCH, GERMAN)
 * @param totalBottles    Total number of bottles in order
 * @param packs           Number of complete 6-packs
 * @param singles         Number of individual bottles (not in packs)
 * @param originBasePrice Base price per bottle for this origin
 * @param originalPrice   Total price before discounts
 */
public record BeerPricingContext(
        BeerOrigin origin,
        int totalBottles,
        int packs,
        int singles,
        BigDecimal originBasePrice,
        BigDecimal originalPrice
) implements PricingContext {
}
