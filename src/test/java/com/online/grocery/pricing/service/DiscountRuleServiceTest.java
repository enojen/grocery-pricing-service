package com.online.grocery.pricing.service;

import com.online.grocery.pricing.api.dto.DiscountRuleResponse;
import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.pricing.discount.BeerDiscountRule;
import com.online.grocery.pricing.pricing.discount.BreadDiscountRule;
import com.online.grocery.pricing.pricing.discount.VegetableDiscountRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DiscountRuleServiceTest {

    private BeerDiscountRule beerRule;
    private BreadDiscountRule breadRule;
    private VegetableDiscountRule vegetableRule;
    private DiscountRuleService service;

    @BeforeEach
    void setUp() {
        beerRule = mock(BeerDiscountRule.class);
        breadRule = mock(BreadDiscountRule.class);
        vegetableRule = mock(VegetableDiscountRule.class);

        when(beerRule.description()).thenReturn("Beer pack discount");
        when(breadRule.description()).thenReturn("Bread age bundle discount");
        when(vegetableRule.description()).thenReturn("Vegetable weight tier discount");

        service = new DiscountRuleService(
            List.of(beerRule),
            List.of(breadRule),
            List.of(vegetableRule)
        );
    }

    @Test
    void shouldReturnAllDiscountRules() {
        List<DiscountRuleResponse> rules = service.getAllRules();

        assertThat(rules).hasSize(3);
        assertThat(rules).extracting(DiscountRuleResponse::productType)
            .containsExactlyInAnyOrder("BREAD", "VEGETABLE", "BEER");
    }

    @Test
    void shouldIncludeDescriptionsFromRules() {
        List<DiscountRuleResponse> rules = service.getAllRules();

        assertThat(rules).extracting(DiscountRuleResponse::description)
            .containsExactlyInAnyOrder(
                "Beer pack discount",
                "Bread age bundle discount",
                "Vegetable weight tier discount"
            );
    }

    @Test
    void shouldFilterByProductType() {
        List<DiscountRuleResponse> beerRules = service.getRulesByProductType(ProductType.BEER);

        assertThat(beerRules).hasSize(1);
        assertThat(beerRules.get(0).productType()).isEqualTo("BEER");
        assertThat(beerRules.get(0).description()).isEqualTo("Beer pack discount");
    }

    @Test
    void shouldHandleMultipleRulesPerType() {
        BeerDiscountRule secondBeerRule = mock(BeerDiscountRule.class);
        when(secondBeerRule.description()).thenReturn("Holiday beer discount");

        DiscountRuleService serviceWithMultipleRules = new DiscountRuleService(
            List.of(beerRule, secondBeerRule),
            List.of(breadRule),
            List.of(vegetableRule)
        );

        List<DiscountRuleResponse> rules = serviceWithMultipleRules.getAllRules();

        assertThat(rules).hasSize(4);
        assertThat(rules.stream().filter(r -> r.productType().equals("BEER")).count()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyListForTypeWithNoRules() {
        DiscountRuleService emptyService = new DiscountRuleService(
            List.of(),
            List.of(),
            List.of()
        );

        List<DiscountRuleResponse> rules = emptyService.getAllRules();

        assertThat(rules).isEmpty();
    }
}
