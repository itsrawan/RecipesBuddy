package com.rjs.recipesbuddy.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS (Cross-Origin Resource Sharing) Configuration
 * 
 * <p>This configuration enables the backend API to accept requests from the React frontend
 * running on different origins (domains/ports). It's essential for local development where
 * the frontend (port 5173) and backend (port 8080) run on different ports.
 * 
 * <p>CORS prevents unauthorized websites from making requests to your API, while allowing
 * your own frontend application to communicate with the backend.
 * 
 * <p>Configuration is loaded from application.properties:
 * <ul>
 *   <li>cors.allowed-origins: Whitelist of allowed frontend URLs</li>
 *   <li>cors.allowed-methods: HTTP methods allowed (GET, POST, etc.)</li>
 *   <li>cors.allowed-headers: Headers that can be included in requests</li>
 *   <li>cors.allow-credentials: Whether to allow cookies/authentication</li>
 * </ul>
 * 
 * <p>Security Note: In production, only specify the actual frontend domain
 * in allowed-origins. Never use "*" wildcard in production as it allows any website
 * to make requests to your API.
 * 
 * @author Rawan Sweidan
 * @version 1.0.0
 * @since 2025-11-10
 * @see CorsFilter
 * @see CorsConfiguration
 */
@Configuration
public class CorsConfig {
    /**
     * List of allowed frontend origins that can make requests to this API
     * Example: http://localhost:5173, http://localhost:3000
     */
    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * List of allowed HTTP methods
     * Example: GET, POST, PUT, DELETE, OPTIONS
     */
    @Value("${cors.allowed-methods}")
    private String[] allowedMethods;

    /**
     * Allowed request headers
     * Using "*" allows all headers (acceptable for development)
     */
    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    /**
     * Whether to allow credentials (cookies, authorization headers) in cross-origin requests
     */
    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    /**
     * Creates and configures the CORS filter bean
     * 
     * <p>This filter is applied to all endpoints (/**) and validates incoming requests
     * against the configured CORS rules. If a request doesn't match the rules,
     * it will be rejected with an appropriate CORS error.
     * 
     * <p>The filter handles:
     * <ul>
     *   <li>Preflight OPTIONS requests</li>
     *   <li>Origin validation</li>
     *   <li>Method validation</li>
     *   <li>Header validation</li>
     *   <li>Credential handling</li>
     * </ul>
     * 
     * @return Configured CorsFilter bean
     */
    @Bean
    public CorsFilter corsFilter(){
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader(allowedHeaders);
        config.setAllowCredentials(allowCredentials);
        config.setAllowedMethods(Arrays.asList(allowedMethods));
        config.setAllowedOrigins(Arrays.asList(allowedOrigins));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
