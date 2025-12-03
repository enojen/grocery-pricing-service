# TASK-021: Discount Controller

## Status
- [x] Completed

## Phase
Phase 4: REST API

## Description
Create DiscountController with GET /discounts/rules endpoint to list all discount rules.

## Implementation Details

### DiscountController

```java
package com.grocery.pricing.api;

import com.grocery.pricing.api.dto.DiscountRuleResponse;
import com.grocery.pricing.service.DiscountRuleService;
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
 * REST controller for discount rule information.
 */
@RestController
@RequestMapping("/api/v1/discounts")
@Tag(name = "Discounts", description = "Discount rule information")
public class DiscountController {

    private final DiscountRuleService ruleService;

    public DiscountController(DiscountRuleService ruleService) {
        this.ruleService = ruleService;
    }

    /**
     * List all registered discount rules.
     *
     * @return List of discount rules with descriptions
     */
    @GetMapping("/rules")
    @Operation(
        summary = "List discount rules",
        description = "Returns all registered discount rules with their descriptions"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Discount rules retrieved successfully",
        content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = DiscountRuleResponse.class))
        )
    )
    public ResponseEntity<List<DiscountRuleResponse>> listDiscountRules() {
        return ResponseEntity.ok(ruleService.getAllRules());
    }
}
```

### Example Response

```json
[
  {
    "productType": "BREAD",
    "description": "Age-based bundle discounts: 3-5 days old = buy 1 take 2, 6 days old = buy 1 take 3"
  },
  {
    "productType": "VEGETABLE",
    "description": "Weight-based discounts: <100g = 5%, 100-499g = 7%, 500g+ = 10%"
  },
  {
    "productType": "BEER",
    "description": "Fixed discount per 6-pack: Belgian €3.00, Dutch €2.00, German €4.00"
  }
]
```

### Controller Test

```java
package com.grocery.pricing.api;

import com.grocery.pricing.api.dto.DiscountRuleResponse;
import com.grocery.pricing.service.DiscountRuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DiscountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiscountRuleService ruleService;

    @Test
    void shouldReturnAllDiscountRules() throws Exception {
        when(ruleService.getAllRules()).thenReturn(List.of(
            new DiscountRuleResponse("BREAD", "Bread discount rule"),
            new DiscountRuleResponse("VEGETABLE", "Vegetable discount rule"),
            new DiscountRuleResponse("BEER", "Beer discount rule")
        ));

        mockMvc.perform(get("/api/v1/discounts/rules"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].productType", is("BREAD")))
            .andExpect(jsonPath("$[0].description", is("Bread discount rule")));
    }

    @Test
    void shouldReturnEmptyListWhenNoRules() throws Exception {
        when(ruleService.getAllRules()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/discounts/rules"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }
}
```

### Integration Test

```java
package com.grocery.pricing.api;

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
class DiscountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnActualDiscountRules() throws Exception {
        mockMvc.perform(get("/api/v1/discounts/rules"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
            .andExpect(jsonPath("$[*].productType", hasItems("BREAD", "VEGETABLE", "BEER")));
    }
}
```

## Files to Create

- `src/main/java/com/grocery/pricing/api/DiscountController.java`
- `src/test/java/com/grocery/pricing/api/DiscountControllerTest.java`
- `src/test/java/com/grocery/pricing/api/DiscountControllerIntegrationTest.java`

## Acceptance Criteria

- [x] GET /api/v1/discounts/rules endpoint functional
- [x] Returns all registered discount rules
- [x] Each rule includes productType and description
- [x] Descriptions come from discount rule implementations (self-documenting)
- [x] OpenAPI annotations for Swagger documentation
- [x] All tests pass
