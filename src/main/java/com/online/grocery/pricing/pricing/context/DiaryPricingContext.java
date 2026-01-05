package com.online.grocery.pricing.pricing.context;

import java.math.BigDecimal;

public record DiaryPricingContext(
        BigDecimal originalPrice
) implements PricingContext{


}
