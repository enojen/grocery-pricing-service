package com.online.grocery.pricing.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/**
 * Configuration properties for all pricing rules.
 * Values are loaded from application.yml under the "pricing" prefix.
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "pricing")
@Validated
public class PricingConfiguration {

    @NotNull(message = "Bread price is required")
    @DecimalMin(value = "0.01", message = "Bread price must be at least 0.01")
    private BigDecimal breadPrice;

    @NotNull(message = "Vegetable price per 100g is required")
    @DecimalMin(value = "0.01", message = "Vegetable price must be at least 0.01")
    private BigDecimal vegetablePricePer100g;

    @Valid
    private BreadRules bread = new BreadRules();

    @Valid
    private VegetableRules vegetable = new VegetableRules();

    @Valid
    private BeerRules beer = new BeerRules();

    /**
     * Bread-specific discount rules configuration.
     */
    @Setter
    @Getter
    @Validated
    public static class BreadRules {

        @Min(value = 0, message = "Max age days cannot be negative")
        private int maxAgeDays = 6;

        @Min(value = 0, message = "Buy one take two age cannot be negative")
        @Max(value = 6, message = "Buy one take two age cannot exceed 6")
        private int buyOneTakeTwoAge = 3;

        @Min(value = 0, message = "Pay one take three age cannot be negative")
        @Max(value = 6, message = "Pay one take three age cannot exceed 6")
        private int payOneTakeThreeAge = 6;

    }

    /**
     * Vegetable-specific discount rules configuration.
     */
    @Setter
    @Getter
    @Validated
    public static class VegetableRules {

        @Min(value = 1, message = "Small weight threshold must be positive")
        private int smallWeightThreshold = 100;

        @Min(value = 1, message = "Medium weight threshold must be positive")
        private int mediumWeightThreshold = 500;

        @NotNull
        @DecimalMin(value = "0.00", message = "Discount cannot be negative")
        @DecimalMax(value = "1.00", message = "Discount cannot exceed 100%")
        private BigDecimal smallWeightDiscount = new BigDecimal("0.05");

        @NotNull
        @DecimalMin(value = "0.00", message = "Discount cannot be negative")
        @DecimalMax(value = "1.00", message = "Discount cannot exceed 100%")
        private BigDecimal mediumWeightDiscount = new BigDecimal("0.07");

        @NotNull
        @DecimalMin(value = "0.00", message = "Discount cannot be negative")
        @DecimalMax(value = "1.00", message = "Discount cannot exceed 100%")
        private BigDecimal largeWeightDiscount = new BigDecimal("0.10");

    }

    /**
     * Beer-specific pricing and discount rules configuration.
     */
    @Setter
    @Getter
    @Validated
    public static class BeerRules {

        @Min(value = 1, message = "Pack size must be at least 1")
        private int packSize = 6;

        @NotNull
        @DecimalMin(value = "0.01", message = "Base price must be positive")
        private BigDecimal belgianBasePrice = new BigDecimal("0.60");

        @NotNull
        @DecimalMin(value = "0.01", message = "Base price must be positive")
        private BigDecimal dutchBasePrice = new BigDecimal("0.50");

        @NotNull
        @DecimalMin(value = "0.01", message = "Base price must be positive")
        private BigDecimal germanBasePrice = new BigDecimal("0.80");

        @NotNull
        @DecimalMin(value = "0.00", message = "Discount cannot be negative")
        private BigDecimal belgianPackDiscount = new BigDecimal("3.00");

        @NotNull
        @DecimalMin(value = "0.00", message = "Discount cannot be negative")
        private BigDecimal dutchPackDiscount = new BigDecimal("2.00");

        @NotNull
        @DecimalMin(value = "0.00", message = "Discount cannot be negative")
        private BigDecimal germanPackDiscount = new BigDecimal("4.00");

    }
}
