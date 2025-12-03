# TASK-014: Pricing Configuration

## Status

- [x] Completed

## Phase

Phase 3: Service Layer

## Description

Create PricingConfiguration class with @ConfigurationProperties and application.yml for externalized pricing rules.

## Implementation Details

### PricingConfiguration

```java
package com.grocery.pricing.config;

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

    // Getters and Setters
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

        // Getters and Setters
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

        // Getters and Setters
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

        // Origin-specific base prices
        @NotNull
        @DecimalMin(value = "0.01", message = "Base price must be positive")
        private BigDecimal belgianBasePrice = new BigDecimal("0.60");

        @NotNull
        @DecimalMin(value = "0.01", message = "Base price must be positive")
        private BigDecimal dutchBasePrice = new BigDecimal("0.50");

        @NotNull
        @DecimalMin(value = "0.01", message = "Base price must be positive")
        private BigDecimal germanBasePrice = new BigDecimal("0.80");

        // Pack discounts
        @NotNull
        @DecimalMin(value = "0.00", message = "Discount cannot be negative")
        private BigDecimal belgianPackDiscount = new BigDecimal("3.00");

        @NotNull
        @DecimalMin(value = "0.00", message = "Discount cannot be negative")
        private BigDecimal dutchPackDiscount = new BigDecimal("2.00");

        @NotNull
        @DecimalMin(value = "0.00", message = "Discount cannot be negative")
        private BigDecimal germanPackDiscount = new BigDecimal("4.00");

        // Getters and Setters
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
```

### application.yml

```yaml
pricing:
  # Base unit prices
  bread-price: 1.00
  vegetable-price-per100g: 1.00

  # Bread discount rules
  bread:
    max-age-days: 6
    bundle-discount-min-age: 3              # "buy 1 take 2" starts at this age
    special-bundle-age: 6                   # "buy 1 take 3" applies at this age

  # Vegetable weight-based discount rules
  vegetable:
    small-weight-threshold: 100      # grams
    medium-weight-threshold: 500     # grams
    small-weight-discount: 0.05      # 5%
    medium-weight-discount: 0.07     # 7%
    large-weight-discount: 0.10      # 10%

  # Beer pricing and discount rules
  beer:
    pack-size: 6
    # Origin-specific base prices (prevents negative final prices after discount)
    belgian-base-price: 0.60         # EUR per bottle
    dutch-base-price: 0.50           # EUR per bottle
    german-base-price: 0.80          # EUR per bottle
    # Pack discounts
    belgian-pack-discount: 3.00      # EUR per pack
    dutch-pack-discount: 2.00        # EUR per pack
    german-pack-discount: 4.00       # EUR per pack

# Server configuration
server:
  port: 8080

# Actuator (optional)
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

## Files to Create

- `src/main/java/com/grocery/pricing/config/PricingConfiguration.java`
- `src/main/resources/application.yml`

## Acceptance Criteria

- [x] PricingConfiguration with @ConfigurationProperties(prefix = "pricing")
- [x] Nested classes: BreadRules, VegetableRules, BeerRules
- [x] All fields have validation annotations (@NotNull, @DecimalMin, etc.)
- [x] application.yml with all pricing rules
- [x] Application fails fast on invalid configuration
- [x] Configuration can be modified without code changes
