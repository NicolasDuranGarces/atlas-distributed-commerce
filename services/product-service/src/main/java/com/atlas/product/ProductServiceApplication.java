package com.atlas.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Product Service Application.
 * Handles product catalog, categories, and inventory management.
 */
@SpringBootApplication(scanBasePackages = {"com.atlas.product", "com.atlas.common"})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableCaching
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
