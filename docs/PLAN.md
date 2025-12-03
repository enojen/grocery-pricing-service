# Grocery Pricing Service - Plan

## Executive Summary

Build a clean, extensible grocery pricing REST service that maintaining **simplicity and clarity** appropriate for the
scope.

---

## 1. Requirements Analysis

### Functional Requirements

- Calculate order total with product-specific discount rules
- Support 3 product types: Bread, Vegetables, Beer
- REST endpoints:
    - `POST /orders/calculate` - Calculate order with receipt
    - `GET /discounts/rules` - List current discount rules
    - `GET /products/prices` - List current prices

### Business Rules

**Bread**:

- Base price: €1.00 per unit
- Age-based bundle discounts (applied per group):
    - 0-2 days old: No discount
    - 3-5 days old: "Buy 1 take 2" (in groups of 2, pay for 1)
    - 6 days old: "Buy 1 take 3" (in groups of 3, pay for 1)
    - `>6 days: Invalid (reject order)

**Vegetables**:

- Base price: €1.00 per 100g
- Weight-based % discounts (applied to ALL vegetables in order):
    - 0-99g: 5% discount
    - 100-499g: 7% discount
    - 500g+: 10% discount

**Beer**:

- Base prices per origin (to ensure positive final prices after discounts):
    - Belgian beer: €0.60 per bottle
    - Dutch beer: €0.50 per bottle
    - German beer: €0.80 per bottle
- Pack discounts (6 bottles = 1 pack):
    - Belgian pack: €3.00 discount → Final: €0.60 (€3.60 - €3.00)
    - Dutch pack: €2.00 discount → Final: €1.00 (€3.00 - €2.00)
    - German pack: €4.00 discount → Final: €0.80 (€4.80 - €4.00)
- Single bottles: No discount, pay per-bottle base price

### Non-Functional Requirements

- Java 17+
- Spring Boot 4.0.0
- Config-driven pricing rules
- Extensible for new products/discounts
- Clean, testable code
- Comprehensive unit tests

---

## 2. Architecture Design

### High-Level Flow

```
OrderRequest → OrderController
                     ↓
              OrderPricingService
                     ↓
         ┌───────────┼───────────┐
         ↓           ↓           ↓
   BreadPricer  VegPricer   BeerPricer
         ↓           ↓           ↓
         └───────────┼───────────┘
                     ↓
                 Receipt
```

**Key Principles**:

- Single responsibility per class
- Strategy pattern for product-specific logic
- Config-driven rules (no hardcoded magic numbers)
- Type-safe domain model
- Clear separation: API → Service → Pricing Logic

---

## 3. Package Structure (~28 files)

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

**Total: ~28 production files + tests**

---

## 4. Detailed Design

### 4.1 Domain Model

**OrderItem Hierarchy**

```java
public interface OrderItem {
    ProductType getType();

    String getName();
}

public record BreadItem(
        String name,
        int quantity,
        int daysOld
) implements OrderItem {
    public BreadItem {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (daysOld < 0) throw new IllegalArgumentException("Age cannot be negative");
        if (daysOld > 6) throw new InvalidOrderException("Bread older than 6 days cannot be ordered");
    }

    public ProductType getType() {
        return ProductType.BREAD;
    }
}

public record VegetableItem(
        String name,
        int weightGrams
) implements OrderItem {
    public VegetableItem {
        if (weightGrams <= 0) throw new IllegalArgumentException("Weight must be positive");
    }

    public ProductType getType() {
        return ProductType.VEGETABLE;
    }
}

public record BeerItem(
        String name,
        int quantity,
        BeerOrigin origin
) implements OrderItem {
    public BeerItem {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        Objects.requireNonNull(origin, "Beer origin required");
    }

    public ProductType getType() {
        return ProductType.BEER;
    }
}
```

**Receipt Model**

```java
public record ReceiptLine(
        String description,
        BigDecimal originalPrice,
        BigDecimal discount,
        BigDecimal finalPrice
) {
    public ReceiptLine {
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Final price cannot be negative");
        }
    }
}

public record Receipt(
        List<ReceiptLine> lines,
        BigDecimal subtotal,      // Sum of original prices
        BigDecimal totalDiscount, // Sum of discounts
        BigDecimal total          // subtotal - totalDiscount
) {
    public Receipt {
        lines = List.copyOf(lines); // Defensive copy
    }
}
```

---

### 4.2 Pluggable Discount Architecture

**Core Concept**: New discount = new class (no changes to existing code)

#### 4.2.1 Pricing Contexts

**BeerPricingContext**

```java
public record BeerPricingContext(
        BeerOrigin origin,
        int totalBottles,
        int packs,
        int singles,
        BigDecimal originBasePrice,    // Origin-specific price per bottle
        BigDecimal originalPrice
) {
}
```

**BreadPricingContext**

```java
public record BreadPricingContext(
        int age,
        int totalQuantity,
        BigDecimal unitPrice,
        BigDecimal originalPrice
) {
}
```

**VegetablePricingContext**

```java
public record VegetablePricingContext(
        int totalWeightGrams,
        BigDecimal pricePerGram,
        BigDecimal originalPrice
) {
}
```

#### 4.2.2 Discount Rule Interfaces

**BeerDiscountRule**

```java
public interface BeerDiscountRule {
    /**
     * Is this rule applicable to the given context?
     */
    boolean isApplicable(BeerPricingContext ctx);

    /**
     * Calculate discount amount for this rule.
     */
    BigDecimal calculateDiscount(BeerPricingContext ctx);

    /**
     * Order of execution (lower = earlier).
     */
    int order();

    /**
     * Human-readable description for /discounts/rules endpoint.
     */
    String description();
}
```

**BreadDiscountRule** (same structure)

```java
public interface BreadDiscountRule {
    boolean isApplicable(BreadPricingContext ctx);

    BigDecimal calculateDiscount(BreadPricingContext ctx);

    int order();

    String description();
}
```

**VegetableDiscountRule** (same structure)

```java
public interface VegetableDiscountRule {
    boolean isApplicable(VegetablePricingContext ctx);

    BigDecimal calculateDiscount(VegetablePricingContext ctx);

    int order();

    String description();
}
```

#### 4.2.3 Pricing Strategy Interface

**PricingStrategy**

```java
public interface PricingStrategy {
    ProductType getProductType();

    List<ReceiptLine> calculatePrice(List<OrderItem> items);
}
```

#### 4.2.4 Discount Rule Implementations

**BreadAgeBundleRule** (Config-Driven)

```java

@Component
public class BreadAgeBundleRule implements BreadDiscountRule {

    private final PricingConfiguration config;

    public BreadAgeBundleRule(PricingConfiguration config) {
        this.config = config;
    }

    @Override
    public boolean isApplicable(BreadPricingContext ctx) {
        int minAge = config.getBread().getBundleDiscountMinAge();
        int specialAge = config.getBread().getSpecialBundleAge();
        return ctx.age() >= minAge && ctx.age() <= specialAge;
    }

    @Override
    public BigDecimal calculateDiscount(BreadPricingContext ctx) {
        int age = ctx.age();
        int qty = ctx.totalQuantity();
        BigDecimal unitPrice = ctx.unitPrice();

        int minAge = config.getBread().getBundleDiscountMinAge();
        int specialAge = config.getBread().getSpecialBundleAge();

        if (age >= minAge && age < specialAge) {
            // "Buy 1 take 2": In groups of 2, pay for 1
            int freeItems = qty / 2;
            return unitPrice.multiply(BigDecimal.valueOf(freeItems));
        }

        if (age == specialAge) {
            // "Buy 1 take 3": In groups of 3, pay for 1
            int groups = qty / 3;
            int freeItems = groups * 2;
            return unitPrice.multiply(BigDecimal.valueOf(freeItems));
        }

        return BigDecimal.ZERO;
    }

    @Override
    public int order() {
        return 100; // Age-based discounts first
    }

    @Override
    public String description() {
        int minAge = config.getBread().getBundleDiscountMinAge();
        int specialAge = config.getBread().getSpecialBundleAge();
        return String.format(
                "Age-based bundle discounts: %d-%d days old = buy 1 take 2, %d days old = buy 1 take 3",
                minAge, specialAge - 1, specialAge
        );
    }
}
```

**BeerPackDiscountRule**

```java

@Component
public class BeerPackDiscountRule implements BeerDiscountRule {

    private final PricingConfiguration config;

    public BeerPackDiscountRule(PricingConfiguration config) {
        this.config = config;
    }

    @Override
    public boolean isApplicable(BeerPricingContext ctx) {
        return ctx.packs() > 0; // At least 1 pack
    }

    @Override
    public BigDecimal calculateDiscount(BeerPricingContext ctx) {
        var beerRules = config.getBeer();
        BigDecimal perPackDiscount = switch (ctx.origin()) {
            case BELGIAN -> beerRules.getBelgianPackDiscount();
            case DUTCH -> beerRules.getDutchPackDiscount();
            case GERMAN -> beerRules.getGermanPackDiscount();
        };
        return perPackDiscount.multiply(BigDecimal.valueOf(ctx.packs()));
    }

    @Override
    public int order() {
        return 100; // Pack discounts
    }

    @Override
    public String description() {
        var rules = config.getBeer();
        return String.format(
                "Fixed discount per 6-pack: Belgian €%.2f, Dutch €%.2f, German €%.2f",
                rules.getBelgianPackDiscount(),
                rules.getDutchPackDiscount(),
                rules.getGermanPackDiscount()
        );
    }
}
```

**VegetableWeightTierRule**

```java

@Component
public class VegetableWeightTierRule implements VegetableDiscountRule {

    private final PricingConfiguration config;

    public VegetableWeightTierRule(PricingConfiguration config) {
        this.config = config;
    }

    @Override
    public boolean isApplicable(VegetablePricingContext ctx) {
        return ctx.totalWeightGrams() > 0; // Always applicable if vegetables exist
    }

    @Override
    public BigDecimal calculateDiscount(VegetablePricingContext ctx) {
        VegetableRules rules = config.getVegetable();
        BigDecimal discountPercent;

        int weight = ctx.totalWeightGrams();
        if (weight < rules.getSmallWeightThreshold()) {
            discountPercent = rules.getSmallWeightDiscount();
        } else if (weight < rules.getMediumWeightThreshold()) {
            discountPercent = rules.getMediumWeightDiscount();
        } else {
            discountPercent = rules.getLargeWeightDiscount();
        }

        return ctx.originalPrice().multiply(discountPercent);
    }

    @Override
    public int order() {
        return 100; // Weight-based discount
    }

    @Override
    public String description() {
        var rules = config.getVegetable();
        return String.format(
                "Weight-based discounts: <100g = %.0f%%, 100-499g = %.0f%%, 500g+ = %.0f%%",
                rules.getSmallWeightDiscount().multiply(new BigDecimal("100")),
                rules.getMediumWeightDiscount().multiply(new BigDecimal("100")),
                rules.getLargeWeightDiscount().multiply(new BigDecimal("100"))
        );
    }
}
```

#### 4.2.5 Pricing Strategies (Orchestrators)

**Type-Safe Casting Helper** (used in all strategies)

```java
// Private helper in each strategy - defensive programming
@SuppressWarnings("unchecked")
private <T extends OrderItem> List<T> castToType(List<OrderItem> items, Class<T> type) {
    return items.stream()
        .filter(type::isInstance)
        .map(type::cast)
        .toList();
}
```

---

**BreadPricingStrategy**

```java

@Component
public class BreadPricingStrategy implements PricingStrategy {

    private final PricingConfiguration config;
    private final List<BreadDiscountRule> discountRules;

    public BreadPricingStrategy(
            PricingConfiguration config,
            List<BreadDiscountRule> discountRules
    ) {
        this.config = config;
        this.discountRules = discountRules.stream()
                .sorted(Comparator.comparingInt(BreadDiscountRule::order))
                .toList();
    }

    @Override
    public ProductType getProductType() {
        return ProductType.BREAD;
    }

    @Override
    public List<ReceiptLine> calculatePrice(List<OrderItem> items) {
        List<BreadItem> breads = castToType(items, BreadItem.class);

        // Group by age
        Map<Integer, List<BreadItem>> byAge = breads.stream()
                .collect(Collectors.groupingBy(BreadItem::daysOld));

        return byAge.entrySet().stream()
                .map(this::priceAgeGroup)
                .toList();
    }

    private ReceiptLine priceAgeGroup(Map.Entry<Integer, List<BreadItem>> entry) {
        int age = entry.getKey();
        List<BreadItem> items = entry.getValue();
        int totalQty = items.stream().mapToInt(BreadItem::quantity).sum();

        BigDecimal unitPrice = config.getBreadPrice();
        BigDecimal originalPrice = unitPrice.multiply(BigDecimal.valueOf(totalQty));

        // Create context
        BreadPricingContext ctx = new BreadPricingContext(
                age,
                totalQty,
                unitPrice,
                originalPrice
        );

        // Apply all applicable discount rules
        BigDecimal totalDiscount = discountRules.stream()
                .filter(rule -> rule.isApplicable(ctx))
                .map(rule -> rule.calculateDiscount(ctx))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalPrice = originalPrice.subtract(totalDiscount);

        String description = String.format("%d x Bread (%d days old)", totalQty, age);
        return new ReceiptLine(description, originalPrice, totalDiscount, finalPrice);
    }
}
```

**VegetablePricingStrategy**

```java

@Component
public class VegetablePricingStrategy implements PricingStrategy {

    private final PricingConfiguration config;
    private final List<VegetableDiscountRule> discountRules;

    public VegetablePricingStrategy(
            PricingConfiguration config,
            List<VegetableDiscountRule> discountRules
    ) {
        this.config = config;
        this.discountRules = discountRules.stream()
                .sorted(Comparator.comparingInt(VegetableDiscountRule::order))
                .toList();
    }

    @Override
    public ProductType getProductType() {
        return ProductType.VEGETABLE;
    }

    @Override
    public List<ReceiptLine> calculatePrice(List<OrderItem> items) {
        List<VegetableItem> vegetables = castToType(items, VegetableItem.class);

        int totalWeight = vegetables.stream()
                .mapToInt(VegetableItem::weightGrams)
                .sum();

        BigDecimal pricePerGram = config.getVegetablePricePer100g()
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal originalPrice = pricePerGram
                .multiply(BigDecimal.valueOf(totalWeight));

        // Create context
        VegetablePricingContext ctx = new VegetablePricingContext(
                totalWeight,
                pricePerGram,
                originalPrice
        );

        // Apply all applicable discount rules
        BigDecimal totalDiscount = discountRules.stream()
                .filter(rule -> rule.isApplicable(ctx))
                .map(rule -> rule.calculateDiscount(ctx))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalPrice = originalPrice.subtract(totalDiscount);

        String description = String.format("%dg Vegetables", totalWeight);
        return List.of(new ReceiptLine(description, originalPrice, totalDiscount, finalPrice));
    }
}
```

**BeerPricingStrategy**

```java

@Component
public class BeerPricingStrategy implements PricingStrategy {

    private final PricingConfiguration config;
    private final List<BeerDiscountRule> discountRules;

    public BeerPricingStrategy(
            PricingConfiguration config,
            List<BeerDiscountRule> discountRules
    ) {
        this.config = config;
        this.discountRules = discountRules.stream()
                .sorted(Comparator.comparingInt(BeerDiscountRule::order))
                .toList();
    }

    @Override
    public ProductType getProductType() {
        return ProductType.BEER;
    }

    @Override
    public List<ReceiptLine> calculatePrice(List<OrderItem> items) {
        List<BeerItem> beers = castToType(items, BeerItem.class);

        Map<BeerOrigin, List<BeerItem>> byOrigin = beers.stream()
                .collect(Collectors.groupingBy(BeerItem::origin));

        return byOrigin.entrySet().stream()
                .map(this::priceOriginGroup)
                .toList();
    }

    private ReceiptLine priceOriginGroup(Map.Entry<BeerOrigin, List<BeerItem>> entry) {
        BeerOrigin origin = entry.getKey();
        int totalBottles = entry.getValue().stream()
                .mapToInt(BeerItem::quantity)
                .sum();

        // Get origin-specific base price
        var beerRules = config.getBeer();
        BigDecimal originBasePrice = switch (origin) {
            case BELGIAN -> beerRules.getBelgianBasePrice();
            case DUTCH -> beerRules.getDutchBasePrice();
            case GERMAN -> beerRules.getGermanBasePrice();
        };

        BigDecimal originalPrice = originBasePrice.multiply(BigDecimal.valueOf(totalBottles));

        int packs = totalBottles / beerRules.getPackSize();
        int singles = totalBottles % beerRules.getPackSize();

        // Create context
        BeerPricingContext ctx = new BeerPricingContext(
                origin,
                totalBottles,
                packs,
                singles,
                originBasePrice,
                originalPrice
        );

        // Apply all applicable discount rules
        BigDecimal totalDiscount = discountRules.stream()
                .filter(rule -> rule.isApplicable(ctx))
                .map(rule -> rule.calculateDiscount(ctx))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalPrice = originalPrice.subtract(totalDiscount);

        String description = String.format(
                "%d x %s Beer (%d packs + %d singles)",
                totalBottles, origin, packs, singles
        );
        return new ReceiptLine(description, originalPrice, totalDiscount, finalPrice);
    }
}
```

---

### 4.3 Service Layer

**DiscountRuleService** (Auto-generated documentation)

```java

@Service
public class DiscountRuleService {

    private final List<BeerDiscountRule> beerRules;
    private final List<BreadDiscountRule> breadRules;
    private final List<VegetableDiscountRule> vegetableRules;

    public DiscountRuleService(
            List<BeerDiscountRule> beerRules,
            List<BreadDiscountRule> breadRules,
            List<VegetableDiscountRule> vegetableRules
    ) {
        this.beerRules = beerRules;
        this.breadRules = breadRules;
        this.vegetableRules = vegetableRules;
    }

    public List<DiscountRuleResponse> getAllRules() {
        Stream<DiscountRuleResponse> beer = beerRules.stream()
                .map(rule -> new DiscountRuleResponse(
                        ProductType.BEER.name(),
                        rule.description()
                ));

        Stream<DiscountRuleResponse> bread = breadRules.stream()
                .map(rule -> new DiscountRuleResponse(
                        ProductType.BREAD.name(),
                        rule.description()
                ));

        Stream<DiscountRuleResponse> veg = vegetableRules.stream()
                .map(rule -> new DiscountRuleResponse(
                        ProductType.VEGETABLE.name(),
                        rule.description()
                ));

        return Stream.of(beer, bread, veg)
                .flatMap(Function.identity())
                .toList();
    }
}
```

**OrderPricingService**

```java

@Service
public class OrderPricingService {

    private final Map<ProductType, PricingStrategy> strategies;

    public OrderPricingService(List<PricingStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        PricingStrategy::getProductType,
                        Function.identity()
                ));
    }

    public Receipt calculateReceipt(Order order) {
        // Group items by product type
        Map<ProductType, List<OrderItem>> itemsByType = order.getItems().stream()
                .collect(Collectors.groupingBy(OrderItem::getType));

        // Calculate for each type
        List<ReceiptLine> allLines = itemsByType.entrySet().stream()
                .flatMap(entry -> {
                    PricingStrategy strategy = strategies.get(entry.getKey());
                    if (strategy == null) {
                        throw new IllegalStateException(
                                "No pricing strategy registered for product type: " + entry.getKey()
                        );
                    }
                    return strategy.calculatePrice(entry.getValue()).stream();
                })
                .toList();

        // Calculate totals
        BigDecimal subtotal = allLines.stream()
                .map(ReceiptLine::originalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = allLines.stream()
                .map(ReceiptLine::discount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = subtotal.subtract(totalDiscount);

        return new Receipt(allLines, subtotal, totalDiscount, total);
    }
}
```

---

### 4.4 Configuration

**PricingConfiguration**

```java

@Configuration
@ConfigurationProperties(prefix = "pricing")
@Validated
public class PricingConfiguration {

    // Base prices
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal breadPrice;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal vegetablePricePer100g;

    // Product-specific rules
    @Valid
    private BreadRules bread = new BreadRules();

    @Valid
    private VegetableRules vegetable = new VegetableRules();

    @Valid
    private BeerRules beer = new BeerRules();

    // Getters/setters
    // ...

    @Validated
    public static class BreadRules {
        @Min(0)
        private int maxAgeDays = 6;

        @Min(0)
        @Max(6)
        private int bundleDiscountMinAge = 3;  // Start of "buy 1 take 2" tier (3-5 days)

        @Min(0)
        @Max(6)
        private int specialBundleAge = 6;      // "buy 1 take 3" special rule age

        // Getters/setters
    }

    @Validated
    public static class VegetableRules {
        @Min(1)
        private int smallWeightThreshold = 100;

        @Min(1)
        private int mediumWeightThreshold = 500;

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("1.00")
        private BigDecimal smallWeightDiscount = new BigDecimal("0.05");

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("1.00")
        private BigDecimal mediumWeightDiscount = new BigDecimal("0.07");

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("1.00")
        private BigDecimal largeWeightDiscount = new BigDecimal("0.10");

        // Getters/setters
    }

    @Validated
    public static class BeerRules {
        @Min(1)
        private int packSize = 6;

        // Origin-specific base prices (to prevent negative final prices)
        @NotNull
        @DecimalMin("0.01")
        private BigDecimal belgianBasePrice = new BigDecimal("0.60");

        @NotNull
        @DecimalMin("0.01")
        private BigDecimal dutchBasePrice = new BigDecimal("0.50");

        @NotNull
        @DecimalMin("0.01")
        private BigDecimal germanBasePrice = new BigDecimal("0.80");

        // Pack discounts
        @NotNull
        @DecimalMin("0.00")
        private BigDecimal belgianPackDiscount = new BigDecimal("3.00");

        @NotNull
        @DecimalMin("0.00")
        private BigDecimal dutchPackDiscount = new BigDecimal("2.00");

        @NotNull
        @DecimalMin("0.00")
        private BigDecimal germanPackDiscount = new BigDecimal("4.00");

        // Getters/setters
    }
}
```

**application.yml**

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
    belgian-base-price: 0.60         # € per bottle
    dutch-base-price: 0.50           # € per bottle
    german-base-price: 0.80          # € per bottle
    # Pack discounts
    belgian-pack-discount: 3.00      # € per pack
    dutch-pack-discount: 2.00        # € per pack
    german-pack-discount: 4.00       # € per pack
```

---

### 4.5 Money Utilities and Rounding Policy

**MoneyUtils** (Utility class for consistent money handling)

```java
public final class MoneyUtils {
    private static final int CURRENCY_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private MoneyUtils() {
    }

    /**
     * Normalize BigDecimal to 2 decimal places using HALF_UP rounding.
     * Applied to all final prices and discount calculations.
     */
    public static BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(CURRENCY_SCALE, ROUNDING_MODE);
    }
}
```

**Rounding Policy**:

- All monetary calculations normalize to 2 decimal places
- Rounding mode: `HALF_UP` (standard for financial systems)
- Applied at: Final price calculations, discount aggregations, receipt totals
- Example: €1.8667 → €1.87

**Usage in Strategies**:

```java
BigDecimal finalPrice = MoneyUtils.normalize(
        originalPrice.subtract(totalDiscount)
);
```

---

### 4.6 REST API

**OrderController**

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderPricingService pricingService;

    @PostMapping("/calculate")
    public ResponseEntity<ReceiptResponse> calculateOrder(
        @Valid @RequestBody OrderRequest request
    ) {
        Order order = mapToOrder(request);
        Receipt receipt = pricingService.calculateReceipt(order);
        return ResponseEntity.ok(mapToResponse(receipt));
    }
}
```

**DiscountController**

```java

@RestController
@RequestMapping("/api/v1/discounts")
public class DiscountController {

    private final DiscountRuleService ruleService;

    @GetMapping("/rules")
    public ResponseEntity<List<DiscountRuleResponse>> listDiscountRules() {
        return ResponseEntity.ok(ruleService.getAllRules());
    }
}
```

**ProductController**

```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final PricingConfiguration config;

    @GetMapping("/prices")
    public ResponseEntity<List<PriceInfoResponse>> listPrices() {
        var beerRules = config.getBeer();
        List<PriceInfoResponse> prices = List.of(
            new PriceInfoResponse("Bread", config.getBreadPrice(), "per unit"),
            new PriceInfoResponse("Vegetables", config.getVegetablePricePer100g(), "per 100g"),
            new PriceInfoResponse("Beer (Belgian)", beerRules.getBelgianBasePrice(), "per bottle"),
            new PriceInfoResponse("Beer (Dutch)", beerRules.getDutchBasePrice(), "per bottle"),
            new PriceInfoResponse("Beer (German)", beerRules.getGermanBasePrice(), "per bottle")
        );
        return ResponseEntity.ok(prices);
    }
}
```

---

### 4.7 DTO Desing & Request Mapping

#### 4.7.1 DTO Classes

**OrderItemRequest** (Unified approach for all product types)

```java
public record OrderItemRequest(
        @NotNull(message = "Product type required")
        ProductType type,

        @NotBlank(message = "Item name required")
        String name,

        @Positive(message = "Quantity must be positive")
        Integer quantity,

        @Min(value = 0, message = "Age cannot be negative")
        @Max(value = 6, message = "Bread older than 6 days not allowed")
        Integer daysOld,

        @Positive(message = "Weight must be positive")
        Integer weightGrams,

        BeerOrigin origin
) {
}
```

**OrderRequest**

```java
public record OrderRequest(
    @NotEmpty(message = "At least one item required")
    List<OrderItemRequest> items
) {}
```

**ReceiptResponse**

```java
public record ReceiptResponse(
        List<ReceiptLineResponse> lines,
        BigDecimal subtotal,
        BigDecimal totalDiscount,
        BigDecimal total
) {
}

public record ReceiptLineResponse(
        String description,
        BigDecimal originalPrice,
        BigDecimal discount,
        BigDecimal finalPrice
) {
}
```

**Design Rationale**:

- **Unified OrderItemRequest**: All product types in one DTO → extendable without changing API contract
- **Optional fields**: Nullable Integer/BeerOrigin → type-specific validation handles requirement checking
- **Bean Validation annotations**: @NotNull, @Positive, @Min, @Max applied at DTO level
- **Request-level validation**: OrderMapper validates type-specific field requirements

#### 4.7.2 OrderMapper (Responsibility: DTO → Domain Conversion + Type-Specific Validation)

```java

@Component
public class OrderMapper {

    /**
     * Convert OrderRequest DTO to domain Order model.
     * Validates type-specific field requirements before conversion.
     */
    public Order mapToOrder(OrderRequest request) {
        List<OrderItem> items = request.items().stream()
                .map(this::mapToOrderItem)
                .toList();

        return new Order(items);
    }

    /**
     * Convert single OrderItemRequest to appropriate OrderItem domain model.
     * Throws InvalidOrderException if required fields for type are missing.
     */
    private OrderItem mapToOrderItem(OrderItemRequest itemRequest) {
        // Validate type-specific required fields
        validateItemRequest(itemRequest);

        return switch (itemRequest.type()) {
            case BREAD -> new BreadItem(
                    itemRequest.name(),
                    itemRequest.quantity(),      // NotNull after validation
                    itemRequest.daysOld()         // NotNull after validation
            );
            case VEGETABLE -> new VegetableItem(
                    itemRequest.name(),
                    itemRequest.weightGrams()     // NotNull after validation
            );
            case BEER -> new BeerItem(
                    itemRequest.name(),
                    itemRequest.quantity(),       // NotNull after validation
                    itemRequest.origin()          // NotNull after validation
            );
        };
    }

    /**
     * Validate that required fields for the product type are present.
     * Throws InvalidOrderException with descriptive error message.
     */
    private void validateItemRequest(OrderItemRequest request) {
        switch (request.type()) {
            case BREAD:
                if (request.quantity() == null) {
                    throw new InvalidOrderException(
                            "quantity field required for product type BREAD"
                    );
                }
                if (request.daysOld() == null) {
                    throw new InvalidOrderException(
                            "daysOld field required for product type BREAD"
                    );
                }
                break;

            case VEGETABLE:
                if (request.weightGrams() == null) {
                    throw new InvalidOrderException(
                            "weightGrams field required for product type VEGETABLE"
                    );
                }
                break;

            case BEER:
                if (request.quantity() == null) {
                    throw new InvalidOrderException(
                            "quantity field required for product type BEER"
                    );
                }
                if (request.origin() == null) {
                    throw new InvalidOrderException(
                            "origin field required for product type BEER"
                    );
                }
                break;
        }
    }
}
```

#### 4.7.3 OrderController Integration

```java

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderPricingService pricingService;
    private final OrderMapper orderMapper;

    public OrderController(
            OrderPricingService pricingService,
            OrderMapper orderMapper
    ) {
        this.pricingService = pricingService;
        this.orderMapper = orderMapper;
    }

    @PostMapping("/calculate")
    public ResponseEntity<ReceiptResponse> calculateOrder(
            @Valid @RequestBody OrderRequest request
    ) {
        // Step 1: Spring validates @Valid constraints on OrderRequest
        // Step 2: OrderMapper converts DTO to domain Order + validates type-specific fields
        Order order = orderMapper.mapToOrder(request);

        // Step 3: PricingService calculates receipt with all discounts
        Receipt receipt = pricingService.calculateReceipt(order);

        // Step 4: Map domain Receipt to ReceiptResponse DTO for API response
        return ResponseEntity.ok(mapToResponse(receipt));
    }

    private ReceiptResponse mapToResponse(Receipt receipt) {
        List<ReceiptLineResponse> lineResponses = receipt.lines().stream()
                .map(line -> new ReceiptLineResponse(
                        line.description(),
                        line.originalPrice(),
                        line.discount(),
                        line.finalPrice()
                ))
                .toList();

        return new ReceiptResponse(
                lineResponses,
                receipt.subtotal(),
                receipt.totalDiscount(),
                receipt.total()
        );
    }
}
```

**Request Flow Diagram**

```
HTTP POST /api/v1/orders/calculate
        ↓
    OrderRequest (JSON)
        ↓
Spring @Valid validates:
  - items not empty
  - each item: type not null, name not blank
  - quantity, weightGrams, daysOld @Positive/@Min/@Max
        ↓
OrderController.calculateOrder()
        ↓
OrderMapper.mapToOrder()
  - Validates type-specific fields
  - Converts each OrderItemRequest → OrderItem
        ↓
Order domain object
        ↓
OrderPricingService.calculateReceipt()
  - Groups by ProductType
  - Applies pricing strategies
  - Calculates discounts
        ↓
Receipt domain object
        ↓
OrderController.mapToResponse()
  - ReceiptResponse DTO
        ↓
HTTP 200 OK (JSON)
```

#### 4.7.4 Example Request/Response

**Request Example: Mixed Order (Bread + Vegetables + Beer)**

```json
POST /api/v1/orders/calculate
Content-Type: application/json

{
"items": [
{
"type": "BREAD",
"name": "Sourdough",
"quantity": 3,
"daysOld": 3,
"weightGrams": null,
"origin": null
},
{
"type": "VEGETABLE",
"name": "Carrots",
"quantity": null,
"daysOld": null,
"weightGrams": 200,
"origin": null
},
{
"type": "BEER",
"name": "Heineken",
"quantity": 6,
"daysOld": null,
"weightGrams": null,
"origin": "DUTCH"
}
]
}
```

**Success Response (200 OK)**

```json
{
  "lines": [
    {
      "description": "3 x Bread (3 days old)",
      "originalPrice": 3.00,
      "discount": 1.00,
      "finalPrice": 2.00
    },
    {
      "description": "200g Vegetables",
      "originalPrice": 2.00,
      "discount": 0.14,
      "finalPrice": 1.86
    },
    {
      "description": "6 x Dutch Beer (1 packs + 0 singles)",
      "originalPrice": 3.00,
      "discount": 2.00,
      "finalPrice": 1.00
    }
  ],
  "subtotal": 8.00,
  "totalDiscount": 3.14,
  "total": 4.86
}
```

**Validation Error Response (400 Bad Request)**

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Invalid request data",
  "details": {
    "items": "At least one item required"
  }
}
```

**Business Logic Error Response (422 Unprocessable Entity)**

```json
{
  "code": "INVALID_ORDER",
  "message": "daysOld field required for product type BREAD",
  "details": null
}
```

**Example Request with Missing Type-Specific Field**

```json
// This will pass @Valid (no annotation on weightGrams)
// but fail in OrderMapper.validateItemRequest()
{
  "items": [
    {
      "type": "BREAD",
      "name": "Sourdough",
      "quantity": 3
      // daysOld missing! ← Will be caught by OrderMapper
    }
  ]
}
```

---

### 4.6 Exception Handling

**GlobalExceptionHandler**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
        MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                error -> error.getDefaultMessage()
            ));

        ErrorResponse response = new ErrorResponse(
            "VALIDATION_ERROR",
            "Invalid request data",
            errors
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrder(
        InvalidOrderException ex
    ) {
        ErrorResponse response = new ErrorResponse(
            "INVALID_ORDER",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralError(Exception ex) {
        ErrorResponse response = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

---

## 5. Testing Strategy

### Unit Tests (Core Focus)

**Strategy Tests** (most important)

- `BreadPricingStrategyTest`
    - No discount (age 0-1)
    - 50% discount (age 3)
    - 66% discount (age 6)
    - Multiple breads different ages
    - Edge cases (negative age, >6 days)

- `VegetablePricingStrategyTest`
    - 5% discount (<100g)
    - 7% discount (100-499g)
    - 10% discount (500g+)
    - Edge cases (0g, boundary values)

- `BeerPricingStrategyTest`
    - Pack discount calculations
    - Mixed packs + singles
    - All three origins
    - Edge cases (0 beers, 1 beer, 5 beers, 6 beers, 7 beers)

**Service Tests**

- `OrderPricingServiceTest`
    - Mixed order (all product types)
    - Single product type orders
    - Empty order handling

**Example Order Test (CRITICAL)**

```java

@Test
void shouldCalculateExampleOrderCorrectly() {
    // Given: Example order from requirements
    Order order = Order.builder()
            .items(List.of(
                    new BreadItem("Bread", 3, 3),              // 3 breads, 3 days old
                    new VegetableItem("Vegetables", 200),       // 200g vegetables
                    new BeerItem("Dutch Beer", 6, BeerOrigin.DUTCH)  // 6 Dutch beers
            ))
            .build();

    // When
    Receipt receipt = orderPricingService.calculateReceipt(order);

    // Then
    // Bread: 3 units at 3 days old
    //   - Original: 3 × €1.00 = €3.00
    //   - Discount: "buy 1 take 2" → 3/2 = 1 free → €1.00 discount
    //   - Final: €2.00
    ReceiptLine breadLine = findLine(receipt, "Bread");
    assertThat(breadLine.originalPrice()).isEqualByComparingTo("3.00");
    assertThat(breadLine.discount()).isEqualByComparingTo("1.00");
    assertThat(breadLine.finalPrice()).isEqualByComparingTo("2.00");

    // Vegetables: 200g
    //   - Original: 200g × €0.01/g = €2.00
    //   - Discount: 7% (100-499g tier) = €0.14
    //   - Final: €1.86
    ReceiptLine vegLine = findLine(receipt, "Vegetables");
    assertThat(vegLine.originalPrice()).isEqualByComparingTo("2.00");
    assertThat(vegLine.discount()).isEqualByComparingTo("0.14");
    assertThat(vegLine.finalPrice()).isEqualByComparingTo("1.86");

    // Beer: 6 Dutch beers (1 pack)
    //   - Original: 6 × €0.50 (Dutch base price) = €3.00
    //   - Discount: 1 pack × €2.00 = €2.00
    //   - Final: €1.00
    ReceiptLine beerLine = findLine(receipt, "Beer");
    assertThat(beerLine.originalPrice()).isEqualByComparingTo("3.00");
    assertThat(beerLine.discount()).isEqualByComparingTo("2.00");
    assertThat(beerLine.finalPrice()).isEqualByComparingTo("1.00");

    // Total
    assertThat(receipt.subtotal()).isEqualByComparingTo("8.00");
    assertThat(receipt.totalDiscount()).isEqualByComparingTo("3.14");
    assertThat(receipt.total()).isEqualByComparingTo("4.86");
}
```

**Domain Tests**

- Record validation tests
- Immutability verification

### Integration Tests

- `OrderControllerIntegrationTest` (with `@SpringBootTest`)
    - Full request → response flow
    - Validation error handling
    - Configuration loading

---

## 6. Extensibility Guide

### Adding New Product Type

1. Add enum value to `ProductType`
2. Create `FruitItem` record implementing `OrderItem`
3. Create `FruitPricingStrategy` implementing `PricingStrategy`
4. Add fruit rules to `PricingConfiguration`
5. Create tests

**Estimated effort: 2-3 hours**

### Adding New Discount Rule

**Example: New Year Beer Promotion (20% off all beers)**

1. Create new rule class:

```java

@Component
public class NewYearBeerPromoRule implements BeerDiscountRule {

    @Override
    public boolean isApplicable(BeerPricingContext ctx) {
        LocalDate now = LocalDate.now();
        return now.getMonth() == Month.JANUARY && now.getDayOfMonth() <= 7;
    }

    @Override
    public BigDecimal calculateDiscount(BeerPricingContext ctx) {
        return ctx.originalPrice().multiply(new BigDecimal("0.20"));
    }

    @Override
    public int order() {
        return 200; // After pack discounts
    }

    @Override
    public String description() {
        return "New Year promotion: 20% off all beers (Jan 1-7)";
    }
}
```

2. Write tests
3. **No changes to BeerPricingStrategy needed!**

**Estimated effort: 30 minutes**

### Changing Pricing Rules

1. Edit `application.yml`
2. Restart application
3. No code changes needed

**Estimated effort: 5 minutes**

---

## 7. Success Criteria

### Functional

- ✅ All business rules correctly implemented
- ✅ Origin-specific beer pricing prevents negative final prices
- ✅ Example order calculation matches expected result (€4.86)
- ✅ All three endpoints working
- ✅ Configuration validated on startup

### Technical

- ✅ Clean architecture with clear separation
- ✅ Strategy pattern properly applied
- ✅ Config-driven pricing rules
- ✅ >90% test coverage
- ✅ No code smells (magic numbers, duplication, etc.)

### Presentation

- ✅ README
- ✅ Code is self-documenting
- ✅ Easy to extend (demonstrated with guide)
- ✅ Professional error handling
- ✅ API documentation available

---

## 9. README Outline

```markdown
# Grocery Pricing Service

## Overview
REST API for calculating grocery order totals with product-specific discounts.

## Architecture
- **Domain Layer**: Pure business logic (records, enums)
- **Pricing Layer**: Strategy pattern for product-specific pricing
- **Service Layer**: Orchestration and business workflows
- **API Layer**: REST endpoints with validation

## Quick Start
```bash
# Build
mvn clean install

# Run tests
mvn test

# Start application
mvn spring-boot:run

# API available at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui.html
```

## API Endpoints

- `POST /api/v1/orders/calculate` - Calculate order with receipt
- `GET /api/v1/discounts/rules` - List discount rules
- `GET /api/v1/products/prices` - List product prices

## Configuration

All pricing rules are in `application.yml`. Modify and restart to apply changes.

## Extensibility

### Adding New Discount Rule

No need to modify existing pricing code!

For beers:

1. Implement `BeerDiscountRule` as a new `@Component`
2. (Optional) Add config parameters under `pricing.beer`
3. Write unit tests

The `BeerPricingStrategy` automatically discovers and applies all registered rules in order.

Example: See `BeerPackDiscountRule` implementation.

## Design Decisions

- Strategy pattern for product-specific logic
- Config-driven rules (no hardcoded magic numbers)
- Records for immutable domain models
- Comprehensive validation and error handling

