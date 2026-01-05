package com.online.grocery.pricing.pricing.strategy;

import com.online.grocery.pricing.domain.model.MoneyUtils;
import com.online.grocery.pricing.domain.model.OrderItem;
import com.online.grocery.pricing.domain.model.ReceiptLine;
import com.online.grocery.pricing.pricing.context.PricingContext;
import com.online.grocery.pricing.pricing.discount.DiscountRule;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Abstract base class for pricing strategies using Template Method pattern.
 * Provides common discount application logic and type casting utilities.
 *
 * <p>This is a sealed class - only the permitted implementations can extend it.
 * When adding a new product type, you must add the new strategy here.</p>
 *
 * @param <I> The specific OrderItem subtype this strategy handles
 * @param <C> The specific PricingContext type used for discount rules
 */
public abstract sealed class AbstractPricingStrategy<I extends OrderItem, C extends PricingContext>
        implements PricingStrategy
        permits BeerPricingStrategy, BreadPricingStrategy, VegetablePricingStrategy, DiaryPricingStrategy {

    protected final List<? extends DiscountRule<C>> discountRules;

    /**
     * Constructor that sorts rules by order.
     *
     * @param discountRules List of discount rules (will be sorted by order)
     */
    protected AbstractPricingStrategy(List<? extends DiscountRule<C>> discountRules) {
        this.discountRules = discountRules.stream()
                .sorted(Comparator.comparingInt(DiscountRule::order))
                .toList();
    }

    @Override
    public final List<ReceiptLine> calculatePrice(List<OrderItem> items) {
        List<I> typedItems = castToType(items);
        return calculateTypedPrice(typedItems);
    }

    /**
     * Template method: subclasses implement product-specific pricing logic.
     *
     * @param items Typed list of order items
     * @return List of receipt lines
     */
    protected abstract List<ReceiptLine> calculateTypedPrice(List<I> items);

    /**
     * Template method: subclasses specify the item type they handle.
     *
     * @return Class of the item type
     */
    protected abstract Class<I> getItemType();

    /**
     * Apply discount rules and create a receipt line.
     * Handles all common logic: aggregation, capping, normalization.
     *
     * @param description Line description
     * @param context     Pricing context with all data needed by rules
     * @return Receipt line with calculated prices
     */
    protected ReceiptLine applyDiscountsAndCreateLine(String description, C context) {
        BigDecimal totalDiscount = aggregateDiscounts(context);

        BigDecimal originalPrice = context.originalPrice();
        BigDecimal finalPrice = originalPrice.subtract(totalDiscount);

        return new ReceiptLine(
                description,
                MoneyUtils.normalize(originalPrice),
                MoneyUtils.normalize(totalDiscount),
                MoneyUtils.normalize(finalPrice)
        );
    }

    /**
     * Aggregates discounts by summing all applicable discounts.
     * Caps total discount at original price to prevent negative final price.
     *
     * @param context The pricing context
     * @return Total discount amount (guaranteed <= originalPrice)
     */
    private BigDecimal aggregateDiscounts(C context) {
        BigDecimal originalPrice = context.originalPrice();

        BigDecimal totalDiscount = discountRules.stream()
                .filter(rule -> rule.isApplicable(context))
                .map(rule -> rule.calculateDiscount(context))
                .peek(discount -> {
                    if (discount.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalStateException("Discount rule returned negative value");
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Cap at original price to prevent negative final price
        return totalDiscount.min(originalPrice);
    }

    /**
     * Safely cast items to the expected type.
     *
     * @param items List of generic order items
     * @return List of typed items
     */
    protected List<I> castToType(List<OrderItem> items) {
        Class<I> type = getItemType();
        return items.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }
}
