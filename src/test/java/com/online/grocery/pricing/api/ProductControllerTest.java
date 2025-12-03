package com.online.grocery.pricing.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
