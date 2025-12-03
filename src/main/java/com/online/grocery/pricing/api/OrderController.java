package com.online.grocery.pricing.api;

import com.online.grocery.pricing.api.dto.ErrorResponse;
import com.online.grocery.pricing.api.dto.OrderRequest;
import com.online.grocery.pricing.api.dto.ReceiptLineResponse;
import com.online.grocery.pricing.api.dto.ReceiptResponse;
import com.online.grocery.pricing.api.mapper.OrderMapper;
import com.online.grocery.pricing.domain.model.Order;
import com.online.grocery.pricing.domain.model.Receipt;
import com.online.grocery.pricing.service.OrderPricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for order pricing operations.
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order pricing operations")
public class OrderController {

    private final OrderPricingService pricingService;
    private final OrderMapper orderMapper;

    public OrderController(
            OrderPricingService pricingService,
            OrderMapper orderMapper
    ) {
        this.pricingService = pricingService;
        this.orderMapper = orderMapper;
    }

    /**
     * Calculate pricing for an order.
     *
     * @param request Order containing items to price
     * @return Receipt with line items and totals
     */
    @PostMapping("/calculate")
    @Operation(
            summary = "Calculate order total",
            description = "Calculates the total price for an order with all applicable discounts"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order calculated successfully",
                    content = @Content(schema = @Schema(implementation = ReceiptResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Business rule violation",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ReceiptResponse> calculateOrder(
            @Valid @RequestBody OrderRequest request
    ) {
        Order order = orderMapper.mapToOrder(request);
        Receipt receipt = pricingService.calculateReceipt(order);
        return ResponseEntity.ok(mapToResponse(receipt));
    }

    private ReceiptResponse mapToResponse(Receipt receipt) {
        List<ReceiptLineResponse> lineResponses = receipt.lines().stream()
                .map(line -> new ReceiptLineResponse(
                        line.description(),
                        line.originalPrice(),
                        line.discount(),
                        line.finalPrice()
                ))
                .toList();

        return new ReceiptResponse(
                lineResponses,
                receipt.subtotal(),
                receipt.totalDiscount(),
                receipt.total()
        );
    }
}
