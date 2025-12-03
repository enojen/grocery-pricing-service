# TASK-022: Product Controller

## Status
- [x] Completed

## Phase
Phase 4: REST API

## Description
Create ProductController with GET /products/prices endpoint to list current product prices.

## Implementation Details

### ProductController

```java
package com.grocery.pricing.api;

import com.grocery.pricing.api.dto.PriceInfoResponse;
import com.grocery.pricing.config.PricingConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for product pricing information.
 */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product pricing information")
public class ProductController {

    private final PricingConfiguration config;

    public ProductController(PricingConfiguration config) {
        this.config = config;
    }

    /**
     * List current product prices.
     *
     * @return List of product prices with units
     */
    @GetMapping("/prices")
    @Operation(
        summary = "List product prices",
        description = "Returns current base prices for all product types"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Prices retrieved successfully",
        content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = PriceInfoResponse.class))
        )
    )
    public ResponseEntity<List<PriceInfoResponse>> listPrices() {
        PricingConfiguration.BeerRules beerRules = config.getBeer();
        
        List<PriceInfoResponse> prices = List.of(
            new PriceInfoResponse(
                "Bread",
                config.getBreadPrice(),
                "per unit"
            ),
            new PriceInfoResponse(
                "Vegetables",
                config.getVegetablePricePer100g(),
                "per 100g"
            ),
            new PriceInfoResponse(
                "Beer (Belgian)",
                beerRules.getBelgianBasePrice(),
                "per bottle"
            ),
            new PriceInfoResponse(
                "Beer (Dutch)",
                beerRules.getDutchBasePrice(),
                "per bottle"
            ),
            new PriceInfoResponse(
                "Beer (German)",
                beerRules.getGermanBasePrice(),
                "per bottle"
            )
        );
        
        return ResponseEntity.ok(prices);
    }
}
```

### Example Response

```json
[
  {
    "productName": "Bread",
    "price": 1.00,
    "unit": "per unit"
  },
  {
    "productName": "Vegetables",
    "price": 1.00,
    "unit": "per 100g"
  },
  {
    "productName": "Beer (Belgian)",
    "price": 0.60,
    "unit": "per bottle"
  },
  {
    "productName": "Beer (Dutch)",
    "price": 0.50,
    "unit": "per bottle"
  },
  {
    "productName": "Beer (German)",
    "price": 0.80,
    "unit": "per bottle"
  }
]
```

### Controller Test

```java
package com.grocery.pricing.api;

import com.grocery.pricing.config.PricingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnAllProductPrices() throws Exception {
        mockMvc.perform(get("/api/v1/products/prices"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(5)))
            .andExpect(jsonPath("$[*].productName", hasItems(
                "Bread", "Vegetables", "Beer (Belgian)", "Beer (Dutch)", "Beer (German)"
            )));
    }

    @Test
    void shouldReturnCorrectBreadPrice() throws Exception {
        mockMvc.perform(get("/api/v1/products/prices"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.productName == 'Bread')].price", contains(1.00)))
            .andExpect(jsonPath("$[?(@.productName == 'Bread')].unit", contains("per unit")));
    }

    @Test
    void shouldReturnCorrectVegetablePrice() throws Exception {
        mockMvc.perform(get("/api/v1/products/prices"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.productName == 'Vegetables')].price", contains(1.00)))
            .andExpect(jsonPath("$[?(@.productName == 'Vegetables')].unit", contains("per 100g")));
    }

    @Test
    void shouldReturnCorrectBeerPrices() throws Exception {
        mockMvc.perform(get("/api/v1/products/prices"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.productName == 'Beer (Belgian)')].price", contains(0.60)))
            .andExpect(jsonPath("$[?(@.productName == 'Beer (Dutch)')].price", contains(0.50)))
            .andExpect(jsonPath("$[?(@.productName == 'Beer (German)')].price", contains(0.80)));
    }

    @Test
    void shouldIncludeUnitsForAllProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products/prices"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].unit", everyItem(not(emptyOrNullString()))));
    }
}
```

## Files to Create

- `src/main/java/com/grocery/pricing/api/ProductController.java`
- `src/test/java/com/grocery/pricing/api/ProductControllerTest.java`

## Acceptance Criteria

- [x] GET /api/v1/products/prices endpoint functional
- [x] Returns prices for all product types (Bread, Vegetables, Beer variants)
- [x] Each price includes productName, price, and unit
- [x] Prices come from PricingConfiguration (config-driven)
- [x] OpenAPI annotations for Swagger documentation
- [x] All tests pass
