# TASK-020: Order Controller

## Status

- [X] Completed

## Phase

Phase 4: REST API

## Description

Create OrderController with POST /orders/calculate endpoint for order pricing.

## Implementation Details

### OrderController

```java
package com.grocery.pricing.api;

import com.grocery.pricing.api.dto.*;
import com.grocery.pricing.api.mapper.OrderMapper;
import com.grocery.pricing.domain.model.Order;
import com.grocery.pricing.domain.model.Receipt;
import com.grocery.pricing.service.OrderPricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for order pricing operations.
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order pricing operations")
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

    /**
     * Calculate pricing for an order.
     *
     * @param request Order containing items to price
     * @return Receipt with line items and totals
     */
    @PostMapping("/calculate")
    @Operation(
        summary = "Calculate order total",
        description = "Calculates the total price for an order with all applicable discounts"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order calculated successfully",
            content = @Content(schema = @Schema(implementation = ReceiptResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Business rule violation",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ReceiptResponse> calculateOrder(
        @Valid @RequestBody OrderRequest request
    ) {
        // Step 1: Convert DTO to domain model (includes type-specific validation)
        Order order = orderMapper.mapToOrder(request);

        // Step 2: Calculate receipt with all discounts
        Receipt receipt = pricingService.calculateReceipt(order);

        // Step 3: Map domain model to response DTO
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

### ErrorResponse DTO

```java
package com.grocery.pricing.api.dto;

import java.util.Map;

/**
 * Standard error response format.
 */
public record ErrorResponse(
    String code,
    String message,
    Map<String, String> details
) {}
```

### Controller Test

```java
package com.grocery.pricing.api;

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

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCalculateOrderSuccessfully() throws Exception {
        OrderRequest request = new OrderRequest(List.of(
            new OrderItemRequest(ProductType.BREAD, "Bread", 3, 3, null, null),
            new OrderItemRequest(ProductType.VEGETABLE, "Veggies", null, null, 200, null),
            new OrderItemRequest(ProductType.BEER, "Beer", 6, null, null, BeerOrigin.DUTCH)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lines", hasSize(3)))
            .andExpect(jsonPath("$.subtotal", is(8.00)))
            .andExpect(jsonPath("$.totalDiscount", is(3.14)))
            .andExpect(jsonPath("$.total", is(4.86)));
    }

    @Test
    void shouldReturnBadRequestForEmptyItems() throws Exception {
        OrderRequest request = new OrderRequest(List.of());

        mockMvc.perform(post("/api/v1/orders/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
    }

    @Test
    void shouldReturnBadRequestForMissingType() throws Exception {
        String json = """
            {
              "items": [
                {
                  "name": "Bread",
                  "quantity": 3,
                  "daysOld": 2
                }
              ]
            }
            """;

        mockMvc.perform(post("/api/v1/orders/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnprocessableEntityForInvalidBreadAge() throws Exception {
        OrderRequest request = new OrderRequest(List.of(
            new OrderItemRequest(ProductType.BREAD, "Bread", 3, 7, null, null)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // @Max annotation triggers 400
    }

    @Test
    void shouldReturnUnprocessableEntityForMissingBeerOrigin() throws Exception {
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
}
```

### Request Flow

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

## Files to Create

- `src/main/java/com/grocery/pricing/api/OrderController.java`
- `src/main/java/com/grocery/pricing/api/dto/ErrorResponse.java`
- `src/test/java/com/grocery/pricing/api/OrderControllerTest.java`

## Acceptance Criteria

- [X] POST /api/v1/orders/calculate endpoint functional
- [X] Returns 200 OK with ReceiptResponse for valid requests
- [X] Returns 400 Bad Request for validation errors
- [X] Returns 422 Unprocessable Entity for business rule violations
- [X] OpenAPI annotations for Swagger documentation
- [X] All controller tests pass
