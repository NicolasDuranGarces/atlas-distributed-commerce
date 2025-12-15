package com.atlas.eureka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Eureka Server.
 * Enables basic authentication for the dashboard and API.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Allow health checks without auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Require authentication for everything else
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {});
        
        return http.build();
    }
}
