package com.online.grocery.pricing.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.online.grocery.pricing.api.dto.OrderItemRequest;
import com.online.grocery.pricing.api.dto.OrderRequest;
import com.online.grocery.pricing.domain.enums.BeerOrigin;
import com.online.grocery.pricing.domain.enums.ProductType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
class OrderControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCalculateExampleOrderCorrectly() throws Exception {
        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(ProductType.BREAD, 3, 3, null, null),
                new OrderItemRequest(ProductType.VEGETABLE, null, null, 200, null),
                new OrderItemRequest(ProductType.BEER, 6, null, null, BeerOrigin.DUTCH)
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
                new OrderItemRequest(ProductType.BREAD, 5, 0, null, null)
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
                new OrderItemRequest(ProductType.VEGETABLE, null, null, 501, null)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal", is(5.01)))
                .andExpect(jsonPath("$.totalDiscount", is(0.50)))
                .andExpect(jsonPath("$.total", is(4.51)));
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
                new OrderItemRequest(ProductType.BEER, qty, null, null, origin)
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
                new OrderItemRequest(ProductType.BREAD, 3, null, null, null)
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
    void shouldHandleLargeOrder() throws Exception {
        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(ProductType.BREAD, 100, 3, null, null),
                new OrderItemRequest(ProductType.BREAD, 100, 6, null, null),
                new OrderItemRequest(ProductType.VEGETABLE, null, null, 10000, null),
                new OrderItemRequest(ProductType.BEER, 60, null, null, BeerOrigin.BELGIAN),
                new OrderItemRequest(ProductType.BEER, 60, null, null, BeerOrigin.DUTCH),
                new OrderItemRequest(ProductType.BEER, 60, null, null, BeerOrigin.GERMAN)
        ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines", hasSize(6)));
    }
}
