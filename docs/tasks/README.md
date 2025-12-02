# Grocery Pricing Service - Task List

## Overview

This document serves as an index for all implementation tasks based on the architecture plan in `PLAN.md`.

**Total Tasks:** 27

---

## Phase 1: Foundation

| Task Code | Name | Description |
|-----------|------|-------------|
| [TASK-001](TASK-001_project-setup.md) | Project Setup | Spring Boot project & Maven dependencies |
| [TASK-002](TASK-002_package-structure.md) | Package Structure | Create package hierarchy |
| [TASK-003](TASK-003_enums.md) | Enums | ProductType & BeerOrigin enums |
| [TASK-004](TASK-004_domain-records.md) | Domain Records | OrderItem hierarchy (BreadItem, BeerItem, VegetableItem) |
| [TASK-005](TASK-005_receipt-model.md) | Receipt Model | Receipt & ReceiptLine records |
| [TASK-006](TASK-006_domain-tests.md) | Domain Tests | Unit tests for domain models |

---

## Phase 2: Pricing Logic

| Task Code | Name | Description |
|-----------|------|-------------|
| [TASK-007](TASK-007_pricing-strategy-interface.md) | Pricing Strategy Interface | PricingStrategy interface definition |
| [TASK-008](TASK-008_pricing-contexts.md) | Pricing Contexts | Context records for pricing calculations |
| [TASK-009](TASK-009_discount-rule-interfaces.md) | Discount Rule Interfaces | Interfaces for pluggable discount rules |
| [TASK-010](TASK-010_bread-discount-rule.md) | Bread Discount Rule | BreadAgeBundleRule implementation |
| [TASK-011](TASK-011_vegetable-discount-rule.md) | Vegetable Discount Rule | VegetableWeightTierRule implementation |
| [TASK-012](TASK-012_beer-discount-rule.md) | Beer Discount Rule | BeerPackDiscountRule implementation |
| [TASK-013](TASK-013_pricing-strategies.md) | Pricing Strategies | Strategy implementations for all product types |

---

## Phase 3: Service Layer

| Task Code | Name | Description |
|-----------|------|-------------|
| [TASK-014](TASK-014_pricing-configuration.md) | Pricing Configuration | PricingConfiguration & application.yml |
| [TASK-015](TASK-015_order-pricing-service.md) | Order Pricing Service | OrderPricingService implementation |
| [TASK-016](TASK-016_discount-rule-service.md) | Discount Rule Service | DiscountRuleService for API documentation |
| [TASK-017](TASK-017_example-order-test.md) | Example Order Test | Validation test for €4.86 expected total |

---

## Phase 4: REST API

| Task Code | Name | Description |
|-----------|------|-------------|
| [TASK-018](TASK-018_dto-classes.md) | DTO Classes | Request/Response DTOs |
| [TASK-019](TASK-019_order-mapper.md) | Order Mapper | OrderMapper component for DTO conversion |
| [TASK-020](TASK-020_order-controller.md) | Order Controller | POST /orders/calculate endpoint |
| [TASK-021](TASK-021_discount-controller.md) | Discount Controller | GET /discounts/rules endpoint |
| [TASK-022](TASK-022_product-controller.md) | Product Controller | GET /products/prices endpoint |
| [TASK-023](TASK-023_exception-handling.md) | Exception Handling | GlobalExceptionHandler & ErrorResponse |
| [TASK-024](TASK-024_openapi-config.md) | OpenAPI Configuration | Swagger/OpenAPI setup |

---

## Phase 5: Polish

| Task Code | Name | Description |
|-----------|------|-------------|
| [TASK-025](TASK-025_integration-tests.md) | Integration Tests | Full flow integration tests |
| [TASK-026](TASK-026_readme.md) | README | Comprehensive project documentation |
| [TASK-027](TASK-027_final-validation.md) | Final Validation | Manual testing & cleanup |

---

## Progress Tracking

- [X] Phase 1: Foundation (6/6)
- [X] Phase 2: Pricing Logic (7/7)
- [ ] Phase 3: Service Layer (1/4)
- [ ] Phase 4: REST API (0/7)
- [ ] Phase 5: Polish (0/3)

**Overall Progress:** 14/27 tasks completed
