package com.atlas.gateway.config;

import jakarta.annotation.PostConstruct;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashSet;

/**
 * Configuration for Swagger UI aggregation of all microservices.
 * Injects OpenAPI documentation URLs for each service into Swagger UI.
 */
@Configuration
public class SwaggerConfig {

        private final SwaggerUiConfigProperties swaggerUiConfigProperties;

        public SwaggerConfig(SwaggerUiConfigProperties swaggerUiConfigProperties) {
                this.swaggerUiConfigProperties = swaggerUiConfigProperties;
        }

        @PostConstruct
        public void init() {
                // Initialize URLs set if null
                if (swaggerUiConfigProperties.getUrls() == null) {
                        swaggerUiConfigProperties.setUrls(new LinkedHashSet<>());
                }

                // Add all microservices' OpenAPI documentation URLs
                swaggerUiConfigProperties.getUrls().add(
                                new SwaggerUiConfigProperties.SwaggerUrl(
                                                "user-service",
                                                "/v3/api-docs/user-service",
                                                "User Service"));

                swaggerUiConfigProperties.getUrls().add(
                                new SwaggerUiConfigProperties.SwaggerUrl(
                                                "product-service",
                                                "/v3/api-docs/product-service",
                                                "Product Service"));

                swaggerUiConfigProperties.getUrls().add(
                                new SwaggerUiConfigProperties.SwaggerUrl(
                                                "order-service",
                                                "/v3/api-docs/order-service",
                                                "Order Service"));

                swaggerUiConfigProperties.getUrls().add(
                                new SwaggerUiConfigProperties.SwaggerUrl(
                                                "payment-service",
                                                "/v3/api-docs/payment-service",
                                                "Payment Service"));

                swaggerUiConfigProperties.getUrls().add(
                                new SwaggerUiConfigProperties.SwaggerUrl(
                                                "notification-service",
                                                "/v3/api-docs/notification-service",
                                                "Notification Service"));
        }
}
