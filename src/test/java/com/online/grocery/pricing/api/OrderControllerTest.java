package com.online.grocery.pricing.api;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCalculateOrderSuccessfully() throws Exception {
        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(ProductType.BREAD, 3, 3, null, null),
                new OrderItemRequest(ProductType.VEGETABLE, null, null, 200, null),
                new OrderItemRequest(ProductType.BEER, 6, null, null, BeerOrigin.DUTCH)
        ));

        // Includes 5% combo discount for bread + vegetables (5% of 4.86 = 0.243)
        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines", hasSize(3)))
                .andExpect(jsonPath("$.subtotal", is(8.00)))
                .andExpect(jsonPath("$.totalDiscount", is(3.383)))
                .andExpect(jsonPath("$.total", is(4.617)));
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
    void shouldReturnBadRequestForInvalidBreadAge() throws Exception {
        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(ProductType.BREAD, 3, 7, null, null)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnprocessableEntityForMissingBeerOrigin() throws Exception {
        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(ProductType.BEER, 6, null, null, null)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code", is("INVALID_ORDER")))
                .andExpect(jsonPath("$.message", containsString("origin")));
    }

    @Test
    void shouldReturnUnprocessableEntityForMissingBreadQuantity() throws Exception {
        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(ProductType.BREAD, null, 3, null, null)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code", is("INVALID_ORDER")))
                .andExpect(jsonPath("$.message", containsString("quantity")));
    }

    @Test
    void shouldReturnUnprocessableEntityForMissingVegetableWeight() throws Exception {
        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(ProductType.VEGETABLE, null, null, null, null)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code", is("INVALID_ORDER")))
                .andExpect(jsonPath("$.message", containsString("weightGrams")));
    }
}
