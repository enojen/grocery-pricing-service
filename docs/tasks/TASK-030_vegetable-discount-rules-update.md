# TASK-030: Update Vegetable Discount Rules

## Status

- [x] Completed

## Phase

Phase 6: Maintenance & Improvements

## Description

Update the vegetable discount thresholds to match the new business requirements with inclusive boundaries.

## Current Business Rules (to be replaced)

- **0-99g** (weight < 100): 5% discount
- **100-499g** (weight < 500): 7% discount
- **500g+** (weight >= 500): 10% discount

## New Business Rules

- **0-99g** (weight > 0 and weight < 100): 5% discount
- **100-500g** (weight >= 100 and weight <= 500, inclusive): 7% discount
- **>500g** (weight > 500): 10% discount

## Key Difference

The medium tier now includes 500g (previously 500g was in the large tier).

## Implementation Details

### VegetableWeightTierRule Changes

```java
@Override
public BigDecimal calculateDiscount(VegetablePricingContext ctx) {
    PricingConfiguration.VegetableRules rules = config.getVegetable();
    BigDecimal discountPercent;

    int weight = ctx.totalWeightGrams();

    if (weight < rules.getSmallWeightThreshold()) {
        // 0-99g: 5% discount
        discountPercent = rules.getSmallWeightDiscount();
    } else if (weight <= rules.getMediumWeightThreshold()) {
        // 100-500g (inclusive): 7% discount
        discountPercent = rules.getMediumWeightDiscount();
    } else {
        // >500g: 10% discount
        discountPercent = rules.getLargeWeightDiscount();
    }

    return ctx.originalPrice().multiply(discountPercent);
}
```

### Configuration (application.yaml)

Current configuration should remain the same:
```yaml
vegetable:
  small-weight-threshold: 100
  medium-weight-threshold: 500
  small-weight-discount: 0.05
  medium-weight-discount: 0.07
  large-weight-discount: 0.10
```

The logic change is from `< 500` to `<= 500` for the medium tier.

## Files to Modify

- `src/main/java/com/online/grocery/pricing/pricing/discount/VegetableWeightTierRule.java`
- `src/test/java/com/online/grocery/pricing/pricing/discount/VegetableWeightTierRuleTest.java`
- Related integration tests

## Acceptance Criteria

- [x] 0-99g: 5% discount applied correctly
- [x] 100g: 7% discount applied (boundary test)
- [x] 250g: 7% discount applied
- [x] 500g: 7% discount applied (boundary test - now inclusive)
- [x] 501g+: 10% discount applied
- [x] All unit tests updated and passing
- [x] Integration tests updated and passing
