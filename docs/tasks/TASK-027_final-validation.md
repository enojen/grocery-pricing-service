# TASK-027: Final Validation

## Status

- [ ] Not Started

## Phase

Phase 5: Polish

## Description

Perform final testing, cleanup, and validation before project completion.

## Implementation Details

### Pre-Completion Checklist

#### 1. Build Verification

```bash
# Clean build
mvn clean install

# Verify no build errors
# Verify all tests pass
```

#### 2. Test Coverage Verification

```bash
# Generate coverage report
mvn test jacoco:report

# Verify coverage > 90%
# Check: target/site/jacoco/index.html
```

#### 3. Code Quality Checks

```bash
# Run static analysis (if configured)
mvn checkstyle:check
mvn spotbugs:check

# Or manual review:
# - No TODO comments left
# - No hardcoded magic numbers
# - No System.out.println statements
# - No commented-out code
# - All public methods have JavaDoc
```

#### 4. Manual API Testing

**Test with curl:**

```bash
# Test calculate order
curl -X POST http://localhost:8080/api/v1/orders/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"type": "BREAD", "name": "Sourdough", "quantity": 3, "daysOld": 3},
      {"type": "VEGETABLE", "name": "Carrots", "weightGrams": 200},
      {"type": "BEER", "name": "Heineken", "quantity": 6, "origin": "DUTCH"}
    ]
  }'

# Expected: total = 4.86

# Test discount rules
curl http://localhost:8080/api/v1/discounts/rules

# Test product prices
curl http://localhost:8080/api/v1/products/prices

# Test validation error
curl -X POST http://localhost:8080/api/v1/orders/calculate \
  -H "Content-Type: application/json" \
  -d '{"items": []}'

# Expected: 400 Bad Request
```

#### 5. Swagger UI Verification

1. Start application: `mvn spring-boot:run`
2. Open: http://localhost:8080/swagger-ui.html
3. Verify:
    - [ ] All 3 endpoints visible
    - [ ] Request/Response schemas correct
    - [ ] "Try it out" works for all endpoints
    - [ ] Error responses documented

#### 6. Configuration Validation

```bash
# Test invalid configuration (should fail fast)
# Temporarily set invalid value in application.yml:
# pricing.bread-price: -1.00

# Run application - should fail on startup with validation error
mvn spring-boot:run
```

### Final Code Review Checklist

#### Domain Layer

- [ ] All records have validation in compact constructor
- [ ] Immutability maintained (defensive copies)
- [ ] No null values accepted where not appropriate

#### Pricing Layer

- [ ] All strategies implement PricingStrategy interface
- [ ] All discount rules implement appropriate interface
- [ ] MoneyUtils.normalize() used for all monetary values
- [ ] No hardcoded prices or discounts (all from config)

#### Service Layer

- [ ] OrderPricingService correctly orchestrates strategies
- [ ] DiscountRuleService correctly aggregates rules

#### API Layer

- [ ] All endpoints return appropriate HTTP status codes
- [ ] Validation errors return 400 with details
- [ ] Business errors return 422 with message
- [ ] OpenAPI annotations on all endpoints

#### Exception Handling

- [ ] GlobalExceptionHandler catches all expected exceptions
- [ ] Error responses use consistent format
- [ ] No stack traces exposed in API responses

#### Configuration

- [ ] All pricing rules in application.yml
- [ ] Validation annotations on configuration properties
- [ ] Fail-fast on invalid configuration

### Performance Considerations

#### Response Time Targets

- [ ] Calculate endpoint < 100ms for typical orders
- [ ] List endpoints < 50ms

#### Load Testing (Optional)

```bash
# Simple load test with Apache Bench
ab -n 1000 -c 10 -p request.json -T application/json \
  http://localhost:8080/api/v1/orders/calculate
```

### Documentation Review

- [ ] README.md complete and accurate
- [ ] All code has appropriate comments
- [ ] Complex logic has explanatory comments
- [ ] No outdated comments

### Cleanup Tasks

- [ ] Remove any debug logging
- [ ] Remove any test data files
- [ ] Verify .gitignore includes build artifacts
- [ ] Remove any unused imports
- [ ] Remove any unused dependencies from pom.xml

### Final Verification Commands

```bash
# Full clean build with tests
mvn clean verify

# Start application
mvn spring-boot:run

# In another terminal, run smoke test
curl -s http://localhost:8080/api/v1/products/prices | jq .
curl -s http://localhost:8080/api/v1/discounts/rules | jq .
curl -s -X POST http://localhost:8080/api/v1/orders/calculate \
  -H "Content-Type: application/json" \
  -d '{"items":[{"type":"BREAD","name":"Test","quantity":1,"daysOld":0}]}' | jq .
```

## Files to Review

- All source files in `src/main/java`
- All test files in `src/test/java`
- `pom.xml` - dependencies and plugins
- `application.yml` - configuration
- `README.md` - documentation

## Acceptance Criteria

- [ ] All tests pass (mvn test)
- [ ] Build succeeds (mvn clean install)
- [ ] Test coverage > 90%
- [ ] No TODO comments remaining
- [ ] No hardcoded values
- [ ] Manual API testing successful
- [ ] Swagger UI functional
- [ ] README documentation complete
- [ ] Example order calculates to €4.86
