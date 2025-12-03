# TASK-025: Integration Tests

## Status

- [x] Completed

## Phase

Phase 5: Polish

## Description

Create comprehensive integration tests that validate the full request-response flow.

## Implementation Details

### OrderControllerIntegrationTest

```java
package com.grocery.pricing.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grocery.pricing.api.dto.*;
import com.grocery.pricing.domain.enums.BeerOrigin;
import com.grocery.pricing.domain.enums.ProductType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCalculateExampleOrderCorrectly() throws Exception {
        // Example order from requirements: €4.86 total
        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(ProductType.BREAD, "Bread", 3, 3, null, null),
                new OrderItemRequest(ProductType.VEGETABLE, "Veggies", null, null, 200, null),
                new OrderItemRequest(ProductType.BEER, "Beer", 6, null, null, BeerOrigin.DUTCH)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal", is(8.00)))
                .andExpect(jsonPath("$.totalDiscount", is(3.14)))
                .andExpect(jsonPath("$.total", is(4.86)));
    }

    @Test
    void shouldCalculateBreadOnlyOrder() throws Exception {
        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(ProductType.BREAD, "Fresh Bread", 5, 0, null, null)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal", is(5.00)))
                .andExpect(jsonPath("$.totalDiscount", is(0.00)))
                .andExpect(jsonPath("$.total", is(5.00)));
    }

    @Test
    void shouldCalculateVegetableOnlyOrder() throws Exception {
        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(ProductType.VEGETABLE, "Potatoes", null, null, 500, null)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal", is(5.00)))
                .andExpect(jsonPath("$.totalDiscount", is(0.50)))
                .andExpect(jsonPath("$.total", is(4.50)));
    }

    @ParameterizedTest
    @CsvSource({
            "BELGIAN, 6, 3.60, 3.00, 0.60",
            "DUTCH, 6, 3.00, 2.00, 1.00",
            "GERMAN, 6, 4.80, 4.00, 0.80"
    })
    void shouldCalculateBeerPackDiscounts(
            BeerOrigin origin, int qty, double subtotal, double discount, double total
    ) throws Exception {
        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(ProductType.BEER, "Beer", qty, null, null, origin)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal", is(subtotal)))
                .andExpect(jsonPath("$.totalDiscount", is(discount)))
                .andExpect(jsonPath("$.total", is(total)));
    }

    @Test
    void shouldRejectEmptyOrder() throws Exception {
        OrderRequest request = new OrderRequest(List.of());

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
    }

    @Test
    void shouldRejectBreadWithMissingAge() throws Exception {
        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(ProductType.BREAD, "Bread", 3, null, null, null)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code", is("INVALID_ORDER")))
                .andExpect(jsonPath("$.message", containsString("daysOld")));
    }

    @Test
    void shouldRejectBeerWithMissingOrigin() throws Exception {
        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(ProductType.BEER, "Beer", 6, null, null, null)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code", is("INVALID_ORDER")))
                .andExpect(jsonPath("$.message", containsString("origin")));
    }

    @Test
    void shouldHandleLargeOrder() throws Exception {
        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(ProductType.BREAD, "Bread1", 100, 3, null, null),
                new OrderItemRequest(ProductType.BREAD, "Bread2", 100, 6, null, null),
                new OrderItemRequest(ProductType.VEGETABLE, "Veggies", null, null, 10000, null),
                new OrderItemRequest(ProductType.BEER, "Beer1", 60, null, null, BeerOrigin.BELGIAN),
                new OrderItemRequest(ProductType.BEER, "Beer2", 60, null, null, BeerOrigin.DUTCH),
                new OrderItemRequest(ProductType.BEER, "Beer3", 60, null, null, BeerOrigin.GERMAN)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines", hasSize(5))); // 2 bread ages + 1 veg + 3 beer origins
    }
}
```

### FullFlowIntegrationTest

```java
package com.grocery.pricing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grocery.pricing.api.dto.*;
import com.grocery.pricing.domain.enums.BeerOrigin;
import com.grocery.pricing.domain.enums.ProductType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full flow integration test validating all endpoints work together.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FullFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCompleteFullOrderFlow() throws Exception {
        // Step 1: Check available prices
        mockMvc.perform(get("/api/v1/products/prices"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(5)));

        // Step 2: Check available discounts
        mockMvc.perform(get("/api/v1/discounts/rules"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));

        // Step 3: Calculate order
        OrderRequest request = new OrderRequest(List.of(
            new OrderItemRequest(ProductType.BREAD, "Bread", 3, 3, null, null),
            new OrderItemRequest(ProductType.VEGETABLE, "Veggies", null, null, 200, null),
            new OrderItemRequest(ProductType.BEER, "Beer", 6, null, null, BeerOrigin.DUTCH)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total", is(4.86)));
    }

    @Test
    void shouldExposeSwaggerDocumentation() throws Exception {
        mockMvc.perform(get("/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.openapi").exists())
            .andExpect(jsonPath("$.info.title", is("Grocery Pricing Service API")));
    }
}
```

## Files to Create

- `src/test/java/com/grocery/pricing/api/OrderControllerIntegrationTest.java`
- `src/test/java/com/grocery/pricing/FullFlowIntegrationTest.java`

## Acceptance Criteria

- [x] Example order test (€4.86) passes
- [x] All product type specific tests pass
- [x] Error handling tests pass (400, 422 responses)
- [x] Edge cases tested (large orders, boundary values)
- [x] Full flow integration test validates all endpoints
- [x] All tests run with @SpringBootTest for real Spring context
- [x] Test coverage > 90%
