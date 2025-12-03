package com.online.grocery.pricing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger documentation configuration.
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Grocery Pricing Service API")
                        .version("1.0.0")
                        .description("""
                                REST API for calculating grocery order totals with product-specific discounts.
                                                   \s
                                ## Features
                                - Calculate order totals with automatic discount application
                                - Support for Bread, Vegetables, and Beer products
                                - Age-based discounts for bread
                                - Weight-based discounts for vegetables
                                - Pack discounts for beer
                                                   \s
                                ## Business Rules
                                                   \s
                                ### Bread
                                - Base price: €1.00 per unit
                                - 0-2 days old: No discount
                                - 3-5 days old: "Buy 1 take 2" (50% off in groups of 2)
                                - 6 days old: "Buy 1 take 3" (66% off in groups of 3)
                                                   \s
                                ### Vegetables
                                - Base price: €1.00 per 100g
                                - 0-99g: 5% discount
                                - 100-499g: 7% discount
                                - 500g+: 10% discount
                                                   \s
                                ### Beer
                                - Belgian: €0.60/bottle, €3.00 pack discount
                                - Dutch: €0.50/bottle, €2.00 pack discount
                                - German: €0.80/bottle, €4.00 pack discount
                                - Pack = 6 bottles
                               \s""")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server")
                ));
    }
}
