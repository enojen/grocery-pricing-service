package com.online.grocery.pricing.domain.model;

import com.online.grocery.pricing.domain.enums.BeerOrigin;
import com.online.grocery.pricing.domain.enums.ProductType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeerItemTest {

    @Test
    void shouldCreateValidBeerItem() {
        BeerItem beer = new BeerItem("Heineken", 6, BeerOrigin.DUTCH);

        assertThat(beer.name()).isEqualTo("Heineken");
        assertThat(beer.quantity()).isEqualTo(6);
        assertThat(beer.origin()).isEqualTo(BeerOrigin.DUTCH);
        assertThat(beer.getType()).isEqualTo(ProductType.BEER);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void shouldRejectNonPositiveQuantity(int quantity) {
        assertThatThrownBy(() -> new BeerItem("Beer", quantity, BeerOrigin.BELGIAN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be positive");
    }

    @Test
    void shouldRejectNullOrigin() {
        assertThatThrownBy(() -> new BeerItem("Beer", 6, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Beer origin required");
    }

    @ParameterizedTest
    @EnumSource(BeerOrigin.class)
    void shouldAcceptAllOrigins(BeerOrigin origin) {
        BeerItem beer = new BeerItem("Beer", 1, origin);
        assertThat(beer.origin()).isEqualTo(origin);
    }
}
