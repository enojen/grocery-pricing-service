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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCalculateOrderSuccessfully() throws Exception {
        OrderRequest request = new OrderRequest(List.of(
            new OrderItemRequest(ProductType.BREAD, "Bread", 3, 3, null, null),
            new OrderItemRequest(ProductType.VEGETABLE, "Vegetables", null, null, 200, null),
            new OrderItemRequest(ProductType.BEER, "Dutch Beer", 6, null, null, BeerOrigin.DUTCH)
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
    void shouldReturnBadRequestForInvalidBreadAge() throws Exception {
        OrderRequest request = new OrderRequest(List.of(
            new OrderItemRequest(ProductType.BREAD, "Bread", 3, 7, null, null)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
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

    @Test
    void shouldReturnUnprocessableEntityForMissingBreadQuantity() throws Exception {
        OrderRequest request = new OrderRequest(List.of(
            new OrderItemRequest(ProductType.BREAD, "Bread", null, 3, null, null)
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
            new OrderItemRequest(ProductType.VEGETABLE, "Veggies", null, null, null, null)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.code", is("INVALID_ORDER")))
            .andExpect(jsonPath("$.message", containsString("weightGrams")));
    }
}
