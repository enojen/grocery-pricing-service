package com.online.grocery.pricing.api;

import com.online.grocery.pricing.api.dto.PriceInfoResponse;
import com.online.grocery.pricing.config.PricingConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for product pricing information.
 */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product pricing information")
public class ProductController {

    private final PricingConfiguration config;

    public ProductController(PricingConfiguration config) {
        this.config = config;
    }

    /**
     * List current product prices.
     *
     * @return List of product prices with units
     */
    @GetMapping("/prices")
    @Operation(
        summary = "List product prices",
        description = "Returns current base prices for all product types"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Prices retrieved successfully",
        content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = PriceInfoResponse.class))
        )
    )
    public ResponseEntity<List<PriceInfoResponse>> listPrices() {
        PricingConfiguration.BeerRules beerRules = config.getBeer();

        List<PriceInfoResponse> prices = List.of(
            new PriceInfoResponse(
                "Bread",
                config.getBreadPrice(),
                "per unit"
            ),
            new PriceInfoResponse(
                "Vegetables",
                config.getVegetablePricePer100g(),
                "per 100g"
            ),
            new PriceInfoResponse(
                "Beer (Belgian)",
                beerRules.getBelgianBasePrice(),
                "per bottle"
            ),
            new PriceInfoResponse(
                "Beer (Dutch)",
                beerRules.getDutchBasePrice(),
                "per bottle"
            ),
            new PriceInfoResponse(
                "Beer (German)",
                beerRules.getGermanBasePrice(),
                "per bottle"
            )
        );

        return ResponseEntity.ok(prices);
    }
}
