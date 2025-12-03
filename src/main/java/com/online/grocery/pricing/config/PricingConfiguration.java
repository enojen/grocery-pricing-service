package com.online.grocery.pricing.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/**
 * Configuration properties for all pricing rules.
 * Values are loaded from application.yml under the "pricing" prefix.
 */
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

    public BigDecimal getBreadPrice() {
        return breadPrice;
    }

    public void setBreadPrice(BigDecimal breadPrice) {
        this.breadPrice = breadPrice;
    }

    public BigDecimal getVegetablePricePer100g() {
        return vegetablePricePer100g;
    }

    public void setVegetablePricePer100g(BigDecimal vegetablePricePer100g) {
        this.vegetablePricePer100g = vegetablePricePer100g;
    }

    public BreadRules getBread() {
        return bread;
    }

    public void setBread(BreadRules bread) {
        this.bread = bread;
    }

    public VegetableRules getVegetable() {
        return vegetable;
    }

    public void setVegetable(VegetableRules vegetable) {
        this.vegetable = vegetable;
    }

    public BeerRules getBeer() {
        return beer;
    }

    public void setBeer(BeerRules beer) {
        this.beer = beer;
    }

    /**
     * Bread-specific discount rules configuration.
     */
    @Validated
    public static class BreadRules {

        @Min(value = 0, message = "Max age days cannot be negative")
        private int maxAgeDays = 6;

        @Min(value = 0, message = "Bundle discount min age cannot be negative")
        @Max(value = 6, message = "Bundle discount min age cannot exceed 6")
        private int bundleDiscountMinAge = 3;

        @Min(value = 0, message = "Special bundle age cannot be negative")
        @Max(value = 6, message = "Special bundle age cannot exceed 6")
        private int specialBundleAge = 6;

        public int getMaxAgeDays() {
            return maxAgeDays;
        }

        public void setMaxAgeDays(int maxAgeDays) {
            this.maxAgeDays = maxAgeDays;
        }

        public int getBundleDiscountMinAge() {
            return bundleDiscountMinAge;
        }

        public void setBundleDiscountMinAge(int bundleDiscountMinAge) {
            this.bundleDiscountMinAge = bundleDiscountMinAge;
        }

        public int getSpecialBundleAge() {
            return specialBundleAge;
        }

        public void setSpecialBundleAge(int specialBundleAge) {
            this.specialBundleAge = specialBundleAge;
        }
    }

    /**
     * Vegetable-specific discount rules configuration.
     */
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

        public int getSmallWeightThreshold() {
            return smallWeightThreshold;
        }

        public void setSmallWeightThreshold(int smallWeightThreshold) {
            this.smallWeightThreshold = smallWeightThreshold;
        }

        public int getMediumWeightThreshold() {
            return mediumWeightThreshold;
        }

        public void setMediumWeightThreshold(int mediumWeightThreshold) {
            this.mediumWeightThreshold = mediumWeightThreshold;
        }

        public BigDecimal getSmallWeightDiscount() {
            return smallWeightDiscount;
        }

        public void setSmallWeightDiscount(BigDecimal smallWeightDiscount) {
            this.smallWeightDiscount = smallWeightDiscount;
        }

        public BigDecimal getMediumWeightDiscount() {
            return mediumWeightDiscount;
        }

        public void setMediumWeightDiscount(BigDecimal mediumWeightDiscount) {
            this.mediumWeightDiscount = mediumWeightDiscount;
        }

        public BigDecimal getLargeWeightDiscount() {
            return largeWeightDiscount;
        }

        public void setLargeWeightDiscount(BigDecimal largeWeightDiscount) {
            this.largeWeightDiscount = largeWeightDiscount;
        }
    }

    /**
     * Beer-specific pricing and discount rules configuration.
     */
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

        public int getPackSize() {
            return packSize;
        }

        public void setPackSize(int packSize) {
            this.packSize = packSize;
        }

        public BigDecimal getBelgianBasePrice() {
            return belgianBasePrice;
        }

        public void setBelgianBasePrice(BigDecimal belgianBasePrice) {
            this.belgianBasePrice = belgianBasePrice;
        }

        public BigDecimal getDutchBasePrice() {
            return dutchBasePrice;
        }

        public void setDutchBasePrice(BigDecimal dutchBasePrice) {
            this.dutchBasePrice = dutchBasePrice;
        }

        public BigDecimal getGermanBasePrice() {
            return germanBasePrice;
        }

        public void setGermanBasePrice(BigDecimal germanBasePrice) {
            this.germanBasePrice = germanBasePrice;
        }

        public BigDecimal getBelgianPackDiscount() {
            return belgianPackDiscount;
        }

        public void setBelgianPackDiscount(BigDecimal belgianPackDiscount) {
            this.belgianPackDiscount = belgianPackDiscount;
        }

        public BigDecimal getDutchPackDiscount() {
            return dutchPackDiscount;
        }

        public void setDutchPackDiscount(BigDecimal dutchPackDiscount) {
            this.dutchPackDiscount = dutchPackDiscount;
        }

        public BigDecimal getGermanPackDiscount() {
            return germanPackDiscount;
        }

        public void setGermanPackDiscount(BigDecimal germanPackDiscount) {
            this.germanPackDiscount = germanPackDiscount;
        }
    }
}
