package com.online.grocery.pricing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.online.grocery.pricing.api.dto.OrderItemRequest;
import com.online.grocery.pricing.api.dto.OrderRequest;
import com.online.grocery.pricing.domain.enums.BeerOrigin;
import com.online.grocery.pricing.domain.enums.ProductType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full flow integration test validating all endpoints work together.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FullFlowIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;

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
                new OrderItemRequest(ProductType.BREAD, 3, 3, null, null),
                new OrderItemRequest(ProductType.VEGETABLE, null, null, 200, null),
                new OrderItemRequest(ProductType.BEER, 6, null, null, BeerOrigin.DUTCH)
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
