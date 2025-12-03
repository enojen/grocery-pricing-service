package com.online.grocery.pricing.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReceiptLineTest {

    @Test
    void shouldCreateValidReceiptLine() {
        ReceiptLine line = new ReceiptLine(
                "3 x Bread",
                new BigDecimal("3.00"),
                new BigDecimal("1.00"),
                new BigDecimal("2.00")
        );

        assertThat(line.description()).isEqualTo("3 x Bread");
        assertThat(line.originalPrice()).isEqualByComparingTo("3.00");
        assertThat(line.discount()).isEqualByComparingTo("1.00");
        assertThat(line.finalPrice()).isEqualByComparingTo("2.00");
    }

    @Test
    void shouldRejectNegativeFinalPrice() {
        assertThatThrownBy(() -> new ReceiptLine(
                "Item",
                new BigDecimal("1.00"),
                new BigDecimal("2.00"),
                new BigDecimal("-1.00")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Final price cannot be negative");
    }

    @Test
    void shouldAcceptZeroFinalPrice() {
        ReceiptLine line = new ReceiptLine(
                "Free Item",
                new BigDecimal("1.00"),
                new BigDecimal("1.00"),
                BigDecimal.ZERO
        );

        assertThat(line.finalPrice()).isEqualByComparingTo("0.00");
    }
}
