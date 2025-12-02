package com.online.grocery.pricing.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for consistent money handling across all calculations.
 */
public final class MoneyUtils {
    
    private static final int CURRENCY_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private MoneyUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Normalize BigDecimal to 2 decimal places using HALF_UP rounding.
     * Applied to all final prices and discount calculations.
     * 
     * @param amount The amount to normalize
     * @return Normalized amount with 2 decimal places
     */
    public static BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(CURRENCY_SCALE, ROUNDING_MODE);
    }
}
