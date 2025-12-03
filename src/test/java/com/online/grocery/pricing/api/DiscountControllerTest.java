package com.online.grocery.pricing.api;

import com.online.grocery.pricing.api.dto.DiscountRuleResponse;
import com.online.grocery.pricing.service.DiscountRuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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

    @MockitoBean
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
