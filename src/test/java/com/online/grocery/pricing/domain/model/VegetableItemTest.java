package com.online.grocery.pricing.domain.model;

import com.online.grocery.pricing.domain.enums.ProductType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VegetableItemTest {

    @Test
    void shouldCreateValidVegetableItem() {
        VegetableItem vegetable = new VegetableItem("Carrots", 200);

        assertThat(vegetable.name()).isEqualTo("Carrots");
        assertThat(vegetable.weightGrams()).isEqualTo(200);
        assertThat(vegetable.getType()).isEqualTo(ProductType.VEGETABLE);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void shouldRejectNonPositiveWeight(int weight) {
        assertThatThrownBy(() -> new VegetableItem("Carrots", weight))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Weight must be positive");
    }

    @Test
    void shouldAcceptMinimumWeight() {
        VegetableItem vegetable = new VegetableItem("Lettuce", 1);
        assertThat(vegetable.weightGrams()).isEqualTo(1);
    }
}
