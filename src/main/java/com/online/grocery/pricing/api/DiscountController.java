package com.online.grocery.pricing.api;

import com.online.grocery.pricing.api.dto.DiscountRuleResponse;
import com.online.grocery.pricing.service.DiscountRuleService;
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
 * REST controller for discount rule information.
 */
@RestController
@RequestMapping("/api/v1/discounts")
@Tag(name = "Discounts", description = "Discount rule information")
public class DiscountController {

    private final DiscountRuleService ruleService;

    public DiscountController(DiscountRuleService ruleService) {
        this.ruleService = ruleService;
    }

    /**
     * List all registered discount rules.
     *
     * @return List of discount rules with descriptions
     */
    @GetMapping("/rules")
    @Operation(
            summary = "List discount rules",
            description = "Returns all registered discount rules with their descriptions"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Discount rules retrieved successfully",
            content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = DiscountRuleResponse.class))
            )
    )
    public ResponseEntity<List<DiscountRuleResponse>> listDiscountRules() {
        return ResponseEntity.ok(ruleService.getAllRules());
    }
}
