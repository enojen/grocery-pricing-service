# TASK-028: Update Bread Discount Rules

## Status

- [x] Completed

## Phase

Phase 6: Maintenance & Improvements

## Description

Update the bread discount rules to match the new business requirements. The current implementation applies discounts for ages 3-5 days, but the new rules are more specific.

## Current Business Rules (to be replaced)

- **0-2 days old**: No discount
- **3 days old**: "Buy 1 take 2" (in groups of 2, pay for 1)
- **6 days old**: "Buy 1 take 3" (in groups of 3, pay for 1)
- **>6 days**: Invalid (rejected at domain level)

## New Business Rules

- **0-1 days old**: No discount (fresh bread)
- **3 days old (exactly)**: "Buy 1 take 2" discount
- **4-5 days old**: No discount
- **6 days old (exactly)**: "Pay 1 take 3" discount
- **>6 days**: Cannot be added to orders (rejected at domain level)

## Implementation Details

### BreadAgeBundleRule Changes

```java
@Override
public boolean isApplicable(BreadPricingContext ctx) {
    int age = ctx.age();
    // Only applicable for exactly 3 days or exactly 6 days
    return age == 3 || age == 6;
}

@Override
public BigDecimal calculateDiscount(BreadPricingContext ctx) {
    int age = ctx.age();
    int qty = ctx.totalQuantity();
    BigDecimal unitPrice = ctx.unitPrice();

    if (age == 3) {
        // "Buy 1 take 2": In groups of 2, pay for 1
        int freeItems = qty / 2;
        return unitPrice.multiply(BigDecimal.valueOf(freeItems));
    }

    if (age == 6) {
        // "Pay 1 take 3": In groups of 3, pay for 1
        int groups = qty / 3;
        int freeItems = groups * 2;
        return unitPrice.multiply(BigDecimal.valueOf(freeItems));
    }

    return BigDecimal.ZERO;
}
```

### Configuration Naming Changes

Current config names are unclear and should be renamed to better reflect the two distinct discount types:

**Current (to be replaced):**
```yaml
bread:
  bundle-discount-min-age: 3
  special-bundle-age: 6
```

**New naming:**
```yaml
bread:
  buy-one-take-two-age: 3    # Age for "Buy 1 take 2" discount
  pay-one-take-three-age: 6  # Age for "Pay 1 take 3" discount
```

**PricingConfiguration.BreadRules changes:**
- `bundleDiscountMinAge` -> `buyOneTakeTwoAge`
- `specialBundleAge` -> `payOneTakeThreeAge`

## Files to Modify

- `src/main/java/com/online/grocery/pricing/config/PricingConfiguration.java`
- `src/main/java/com/online/grocery/pricing/pricing/discount/BreadAgeBundleRule.java`
- `src/main/resources/application.yaml`
- `src/test/java/com/online/grocery/pricing/pricing/discount/BreadAgeBundleRuleTest.java`
- Related integration tests

## Acceptance Criteria

- [x] Age 0-1 days: No discount applied
- [x] Age 2 days: No discount applied
- [x] Age 3 days (exactly): "Buy 1 take 2" discount calculated correctly
- [x] Age 4-5 days: No discount applied
- [x] Age 6 days (exactly): "Pay 1 take 3" discount calculated correctly
- [x] Age > 6 days: Rejected at validation level
- [x] Config properties renamed to `buy-one-take-two-age` and `pay-one-take-three-age`
- [x] All unit tests updated and passing
- [x] Integration tests updated and passing
