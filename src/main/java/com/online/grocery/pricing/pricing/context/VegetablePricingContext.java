package com.online.grocery.pricing.pricing.context;

import java.math.BigDecimal;

/**
 * Context for vegetable pricing calculations.
 * Encapsulates all data needed by vegetable discount rules.
 * 
 * @param totalWeightGrams Total weight of all vegetables in grams
 * @param pricePerGram Price per gram (derived from price per 100g)
 * @param originalPrice Total price before discounts
 */
public record VegetablePricingContext(
    int totalWeightGrams,
    BigDecimal pricePerGram,
    BigDecimal originalPrice
) {}
