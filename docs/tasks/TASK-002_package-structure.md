# TASK-002: Package Structure

## Status
- [ ] Not Started

## Phase
Phase 1: Foundation

## Description
Create the package hierarchy for the grocery pricing service following clean architecture principles.

## Implementation Details

### Package Structure (~28 files)

```
com.grocery.pricing/
├── api/                              # REST Layer (8 files)
│   ├── OrderController              # POST /orders/calculate
│   ├── DiscountController           # GET /discounts/rules
│   ├── ProductController            # GET /products/prices
│   └── dto/
│       ├── OrderRequest             # Order input
│       ├── BeerItemDto              # Beer order item
│       ├── BreadItemDto             # Bread order item
│       ├── VegetableItemDto         # Vegetable order item
│       └── ReceiptResponse          # Order output
│
├── domain/                           # Domain Model (9 files)
│   ├── model/
│   │   ├── OrderItem                # Interface: getType(), getName()
│   │   ├── BeerItem                 # Record (name, quantity, origin)
│   │   ├── BreadItem                # Record (name, quantity, daysOld)
│   │   ├── VegetableItem            # Record (name, weightGrams)
│   │   ├── Order                    # Aggregate of items
│   │   ├── ReceiptLine              # Single line on receipt
│   │   └── Receipt                  # Final result with totals
│   └── enums/
│       ├── ProductType              # BREAD, VEGETABLE, BEER
│       └── BeerOrigin               # BELGIAN, DUTCH, GERMAN
│
├── pricing/                          # Pricing Logic (12 files)
│   ├── context/                     # Pricing contexts
│   │   ├── BeerPricingContext       # Record (origin, bottles, packs, etc.)
│   │   ├── BreadPricingContext      # Record (age, quantity, unitPrice, etc.)
│   │   └── VegetablePricingContext  # Record (weight, pricePerGram, etc.)
│   │
│   ├── discount/                    # Discount rules
│   │   ├── BeerDiscountRule         # Interface (isApplicable, calculate, etc.)
│   │   ├── BreadDiscountRule        # Interface
│   │   ├── VegetableDiscountRule    # Interface
│   │   ├── BeerPackDiscountRule     # Pack discount implementation
│   │   ├── BreadAgeBundleRule       # Age-based bundle discount
│   │   └── VegetableWeightTierRule  # Weight-based % discount
│   │
│   └── strategy/
│       ├── PricingStrategy          # Interface: price(List<OrderItem>)
│       ├── BeerPricingStrategy      # Beer pricing orchestrator
│       ├── BreadPricingStrategy     # Bread pricing orchestrator
│       └── VegetablePricingStrategy # Vegetable pricing orchestrator
│
├── service/                          # Application Services (2 files)
│   ├── OrderPricingService          # Orchestrates pricing
│   └── DiscountRuleService          # Provides discount metadata
│
├── config/                           # Configuration (2 files)
│   ├── PricingConfiguration         # @ConfigurationProperties
│   └── OpenApiConfiguration         # Swagger docs
│
└── exception/                        # Error Handling (3 files)
    ├── InvalidOrderException        # Business rule violations
    ├── ErrorResponse                # Standard error format
    └── GlobalExceptionHandler       # @RestControllerAdvice
```

### Key Design Principles

- **Single responsibility** per class
- **Strategy pattern** for product-specific logic
- **Config-driven** rules (no hardcoded magic numbers)
- **Type-safe** domain model
- **Clear separation**: API → Service → Pricing Logic

## Files to Create

### Directory Structure
```
src/main/java/com/grocery/pricing/
├── api/
│   └── dto/
├── domain/
│   ├── model/
│   └── enums/
├── pricing/
│   ├── context/
│   ├── discount/
│   └── strategy/
├── service/
├── config/
└── exception/
```

## Acceptance Criteria

- [ ] All package directories created
- [ ] Package structure follows clean architecture
- [ ] Clear separation between layers
- [ ] Ready for component implementation
