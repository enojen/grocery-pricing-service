package com.online.grocery.pricing.domain.model;

import com.online.grocery.pricing.domain.enums.ProductType;
import com.online.grocery.pricing.exception.InvalidOrderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BreadItemTest {

    @Test
    void shouldCreateValidBreadItem() {
        BreadItem bread = new BreadItem(3, 2);

        assertThat(bread.quantity()).isEqualTo(3);
        assertThat(bread.daysOld()).isEqualTo(2);
        assertThat(bread.getType()).isEqualTo(ProductType.BREAD);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void shouldRejectNonPositiveQuantity(int quantity) {
        assertThatThrownBy(() -> new BreadItem(quantity, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be positive");
    }

    @Test
    void shouldRejectNegativeAge() {
        assertThatThrownBy(() -> new BreadItem(1, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Age cannot be negative");
    }

    @Test
    void shouldRejectBreadOlderThan6Days() {
        assertThatThrownBy(() -> new BreadItem(1, 7))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("Bread older than 6 days");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6})
    void shouldAcceptValidAges(int age) {
        BreadItem bread = new BreadItem(1, age);
        assertThat(bread.daysOld()).isEqualTo(age);
    }
}
