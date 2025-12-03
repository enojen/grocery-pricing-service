# Grocery Pricing Service

REST API for calculating grocery order totals with product-specific discounts.

## Overview

This service calculates order totals for a grocery store, applying automatic discounts based on:

- **Bread**: Age-based bundle discounts
- **Vegetables**: Weight-based percentage discounts
- **Beer**: Origin-specific pack discounts

## Architecture

### High-Level Design

```
┌─────────────────────────────────────────────────────────────────┐
│                          REST API Layer                         │
│  OrderController │ DiscountController │ ProductController       │
└─────────────────────────────┬───────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                        Service Layer                            │
│            OrderPricingService │ DiscountRuleService            │
└─────────────────────────────┬───────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                       Pricing Layer                             │
│  BreadPricingStrategy │ VegetablePricingStrategy │ BeerPricing │
│                              │                                  │
│  ┌───────────────────────────▼─────────────────────────────┐   │
│  │              Pluggable Discount Rules                    │   │
│  │  BreadAgeBundleRule │ VegetableWeightTierRule │ BeerPack │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                        Domain Layer                             │
│     OrderItem │ BreadItem │ VegetableItem │ BeerItem │ Receipt │
└─────────────────────────────────────────────────────────────────┘
```

### Key Design Patterns

- **Strategy Pattern**: Product-specific pricing logic
- **Pluggable Rules**: Add new discounts without modifying existing code
- **Config-driven**: All pricing rules externalized to application.yaml

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.6+

### Build & Run

```bash
# Build
mvn clean install

# Run tests
mvn test

# Start application
mvn spring-boot:run

# Or run the JAR
java -jar target/grocery-pricing-service-0.0.1-SNAPSHOT.jar
```

### Access

- **API**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8080/api-docs

## API Endpoints

### Calculate Order

```http
POST /api/v1/orders/calculate
Content-Type: application/json

{
  "items": [
    {"type": "BREAD", "name": "Sourdough", "quantity": 3, "daysOld": 3},
    {"type": "VEGETABLE", "name": "Carrots", "weightGrams": 200},
    {"type": "BEER", "name": "Heineken", "quantity": 6, "origin": "DUTCH"}
  ]
}
```

**Response:**

```json
{
  "lines": [
    {"description": "3 x Bread (3 days old)", "originalPrice": 3.00, "discount": 1.00, "finalPrice": 2.00},
    {"description": "200g Vegetables", "originalPrice": 2.00, "discount": 0.14, "finalPrice": 1.86},
    {"description": "6 x DUTCH Beer (1 packs + 0 singles)", "originalPrice": 3.00, "discount": 2.00, "finalPrice": 1.00}
  ],
  "subtotal": 8.00,
  "totalDiscount": 3.14,
  "total": 4.86
}
```

### List Discount Rules

```http
GET /api/v1/discounts/rules
```

### List Product Prices

```http
GET /api/v1/products/prices
```

## Business Rules

### Bread

| Age      | Discount                                |
|----------|-----------------------------------------|
| 0-2 days | No discount                             |
| 3-5 days | "Buy 1 take 2" (50% off in groups of 2) |
| 6 days   | "Buy 1 take 3" (66% off in groups of 3) |
| >6 days  | Not allowed                             |

### Vegetables

| Weight   | Discount |
|----------|----------|
| 0-99g    | 5%       |
| 100-499g | 7%       |
| 500g+    | 10%      |

### Beer

| Origin  | Base Price   | Pack Discount | Final (6-pack) |
|---------|--------------|---------------|----------------|
| Belgian | €0.60/bottle | €3.00         | €0.60          |
| Dutch   | €0.50/bottle | €2.00         | €1.00          |
| German  | €0.80/bottle | €4.00         | €0.80          |

## Configuration

All pricing rules can be modified in `application.yaml`:

```yaml
pricing:
  bread-price: 1.00
  vegetable-price-per100g: 1.00

  bread:
    max-age-days: 6
    bundle-discount-min-age: 3
    special-bundle-age: 6

  vegetable:
    small-weight-threshold: 100
    medium-weight-threshold: 500
    small-weight-discount: 0.05
    medium-weight-discount: 0.07
    large-weight-discount: 0.10

  beer:
    pack-size: 6
    belgian-base-price: 0.60
    dutch-base-price: 0.50
    german-base-price: 0.80
    belgian-pack-discount: 3.00
    dutch-pack-discount: 2.00
    german-pack-discount: 4.00
```

## Extensibility

### Adding a New Discount Rule

1. Create a class implementing the appropriate interface:

```java
@Component
public class HolidayBeerPromoRule implements BeerDiscountRule {
    @Override
    public boolean isApplicable(BeerPricingContext ctx) {
        return isHolidaySeason();
    }

    @Override
    public BigDecimal calculateDiscount(BeerPricingContext ctx) {
        return ctx.originalPrice().multiply(new BigDecimal("0.15"));
    }

    @Override
    public int order() { return 200; }

    @Override
    public String description() {
        return "Holiday promotion: 15% off all beers";
    }
}
```

2. The rule is automatically discovered and applied - no other code changes needed!

### Adding a New Product Type

1. Add enum value to `ProductType`
2. Create item record implementing `OrderItem`
3. Create `PricingStrategy` implementation
4. Add configuration to `PricingConfiguration`

## Testing

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

## Project Structure

```
src/main/java/com/online/grocery/pricing/
├── api/                    # REST controllers and DTOs
│   ├── dto/                # Request/Response DTOs
│   └── mapper/             # Object mappers
├── config/                 # Configuration classes
├── domain/                 # Domain models and enums
│   ├── enums/              # ProductType, BeerOrigin
│   └── model/              # OrderItem, Receipt, etc.
├── exception/              # Exception handling
├── pricing/                # Pricing strategies and rules
│   ├── context/            # Pricing contexts
│   ├── discount/           # Discount rule implementations
│   └── strategy/           # Pricing strategies
└── service/                # Business services
```

## License

MIT License
