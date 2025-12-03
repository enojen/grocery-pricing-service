# TASK-031: Document Beer Per-Bottle Minimum Price Feature

## Status

- [x] Completed

## Phase

Phase 3: Documentation

## Description

Document the existing per-bottle minimum price feature for beer in the README. This feature prevents the discount from reducing the total price below zero or to an unreasonably low value.

## Background

The beer pack discounts are fixed amounts:
- Belgian pack (6 bottles): €3.00 discount
- Dutch pack (6 bottles): €2.00 discount
- German pack (6 bottles): €4.00 discount

Without a minimum price constraint, if the per-bottle base price is lower than expected, the discount could potentially exceed the total price, resulting in negative totals.

**Example Problem (without protection):**
- 6 Dutch beers at €0.50/bottle = €3.00 total
- Dutch pack discount = €2.00
- Final price = €1.00 (OK)

But if prices were different:
- 6 Belgian beers at €0.40/bottle = €2.40 total
- Belgian pack discount = €3.00
- Final price = -€0.60 (Problem!)

## Current Implementation

The per-bottle minimum price feature is already implemented to prevent this issue. The discount is capped so that the final price cannot go below a minimum threshold per bottle.

## Documentation to Add

### README.md Updates

Add a section explaining the beer pricing protection mechanism:

```markdown
### Beer Pricing - Minimum Price Protection

To prevent discounts from exceeding the total price, the system ensures that 
pack discounts never reduce the per-bottle price below a configured minimum.

**Current per-bottle minimum prices:**
- Belgian beer: [configured value]
- Dutch beer: [configured value]
- German beer: [configured value]

This ensures that even with pack discounts applied, customers always pay at 
least the minimum price per bottle.
```

## Files to Modify

- `README.md` - Add beer minimum price documentation section

## Acceptance Criteria

- [x] README.md updated with beer minimum price protection explanation
- [x] Current configured minimum values documented
- [x] Example calculation included to illustrate the protection mechanism
