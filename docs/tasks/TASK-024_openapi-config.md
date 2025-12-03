# TASK-024: OpenAPI Configuration

## Status
- [x] Completed

## Phase
Phase 4: REST API

## Description
Configure OpenAPI/Swagger documentation for the REST API.

## Implementation Details

### OpenApiConfiguration

```java
package com.grocery.pricing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger documentation configuration.
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Grocery Pricing Service API")
                .version("1.0.0")
                .description("""
                    REST API for calculating grocery order totals with product-specific discounts.
                    
                    ## Features
                    - Calculate order totals with automatic discount application
                    - Support for Bread, Vegetables, and Beer products
                    - Age-based discounts for bread
                    - Weight-based discounts for vegetables
                    - Pack discounts for beer
                    
                    ## Business Rules
                    
                    ### Bread
                    - Base price: €1.00 per unit
                    - 0-2 days old: No discount
                    - 3-5 days old: "Buy 1 take 2" (50% off in groups of 2)
                    - 6 days old: "Buy 1 take 3" (66% off in groups of 3)
                    
                    ### Vegetables
                    - Base price: €1.00 per 100g
                    - 0-99g: 5% discount
                    - 100-499g: 7% discount
                    - 500g+: 10% discount
                    
                    ### Beer
                    - Belgian: €0.60/bottle, €3.00 pack discount
                    - Dutch: €0.50/bottle, €2.00 pack discount
                    - German: €0.80/bottle, €4.00 pack discount
                    - Pack = 6 bottles
                    """)
                .contact(new Contact()
                    .name("API Support")
                    .email("support@example.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Local development server")
            ));
    }
}
```

### application.yml additions

```yaml
# OpenAPI/Swagger configuration
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
```

### Swagger UI Access

After starting the application:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **OpenAPI YAML**: http://localhost:8080/api-docs.yaml

### Example API Documentation

The Swagger UI will show:

**Orders Tag:**
- `POST /api/v1/orders/calculate` - Calculate order total

**Discounts Tag:**
- `GET /api/v1/discounts/rules` - List discount rules

**Products Tag:**
- `GET /api/v1/products/prices` - List product prices

### Controller Annotations

Make sure all controllers have proper annotations:

```java
@Tag(name = "Orders", description = "Order pricing operations")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

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
    @PostMapping("/calculate")
    public ResponseEntity<ReceiptResponse> calculateOrder(
        @Valid @RequestBody OrderRequest request
    ) { ... }
}
```

### Integration Test

```java
package com.grocery.pricing.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldExposeOpenApiDocs() throws Exception {
        mockMvc.perform(get("/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.info.title").value("Grocery Pricing Service API"))
            .andExpect(jsonPath("$.info.version").value("1.0.0"));
    }

    @Test
    void shouldExposeSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isOk());
    }
}
```

## Files to Create

- `src/main/java/com/grocery/pricing/config/OpenApiConfiguration.java`
- `src/test/java/com/grocery/pricing/config/OpenApiConfigurationTest.java`

## Files to Modify

- `src/main/resources/application.yml` - Add springdoc configuration

## Acceptance Criteria

- [x] OpenApiConfiguration with API info, description, servers
- [x] Swagger UI accessible at /swagger-ui.html
- [x] OpenAPI docs accessible at /api-docs
- [x] All endpoints documented with descriptions
- [x] Request/Response schemas visible in Swagger UI
- [x] Business rules documented in API description
- [x] Integration test verifies OpenAPI docs availability
