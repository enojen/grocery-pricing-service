package com.online.grocery.pricing.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyUtilsTest {

    @ParameterizedTest
    @CsvSource({
            "1.8667, 1.87",
            "1.8647, 1.86",
            "1.865, 1.87",
            "1.8650, 1.87",
            "1.8, 1.80",
            "1, 1.00",
            "0.005, 0.01",
            "0.004, 0.00"
    })
    void shouldNormalizeToTwoDecimalPlaces(String input, String expected) {
        BigDecimal result = MoneyUtils.normalize(new BigDecimal(input));
        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    void shouldReturnZeroForNull() {
        BigDecimal result = MoneyUtils.normalize(null);
        assertThat(result).isEqualByComparingTo("0.00");
    }

    @Test
    void shouldHandleZero() {
        BigDecimal result = MoneyUtils.normalize(BigDecimal.ZERO);
        assertThat(result).isEqualByComparingTo("0.00");
    }
}
