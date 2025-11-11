package com.rjs.recipesbuddy.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

/**
 * WebClient Configuration for Spoonacular API Integration
 * 
 * <p>This configuration class sets up a reactive WebClient for making HTTP requests
 * to the Spoonacular Food API. It includes timeout settings, connection pooling,
 * buffer size configuration, and automatic API key injection.
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Configurable connection and read timeouts</li>
 *   <li>Automatic API key header injection from environment variables</li>
 *   <li>Increased buffer size to handle large API responses (16MB)</li>
 *   <li>Netty-based HTTP client with connection pooling</li>
 *   <li>Timeout handlers for read and write operations</li>
 * </ul>
 * 
 * <p>Security:
 * <ul>
 *   <li>API key loaded from SPOONACULAR_API_KEY environment variable</li>
 *   <li>Never hardcoded or exposed in code</li>
 *   <li>Application fails to start if API key is not provided</li>
 * </ul>
 * 
 * @author Rawan Sweidan
 * @version 1.0.0
 * @since 2025-11-10
 * @see WebClient
 * @see SpoonacularApiService
 */
@Configuration
public class WebClientConfig {

    /**
     * Base URL for Spoonacular API (from application.properties)
     */
    @Value("${spoonacular.api.base-url}")
    private String baseUrl;

    /**
     * API key loaded from SPOONACULAR_API_KEY environment variable
     */
    private String apiKey = getApiKey();

    /**
     * HTTP header name for API authentication (from application.properties)
     */
    @Value("${spoonacular.api.auth.header-name}")
    private String authHeaderName;

    /**
     * Connection timeout in milliseconds (default: 5000ms)
     */
    @Value("${webclient.connection-timeout:5000}")
    private int connectionTimeout;

    /**
     * Read timeout in milliseconds (default: 10000ms)
     */
    @Value("${webclient.read-timeout:10000}")
    private int readTimeout;

    /**
     * Creates and configures a WebClient bean for Spoonacular API calls
     * 
     * <p>Configuration includes:
     * <ul>
     *   <li>Base URL: Spoonacular API endpoint</li>
     *   <li>Connection timeout: Time to establish connection</li>
     *   <li>Read timeout: Time to read response data</li>
     *   <li>Write timeout: Time to send request data</li>
     *   <li>Buffer size: 16MB (handles large recipe responses)</li>
     *   <li>Authentication: API key automatically added to headers</li>
     * </ul>
     * 
     * <p>Uses Netty as the underlying HTTP client for efficient connection pooling
     * and non-blocking I/O operations.
     * 
     * @return Configured WebClient instance for Spoonacular API
     * @throws IllegalStateException if API key is not found in environment variables
     */
    @Bean
    public WebClient spoonacularWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .responseTimeout(Duration.ofMillis(readTimeout))
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS)));

        // Increase in-memory buffer to avoid DataBufferLimitException (default 256KB)
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                // allow up to 16MB in-memory buffer for response bodies (adjust if necessary)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .defaultHeader(authHeaderName, apiKey)
                .build();
    }

    /**
     * Retrieves Spoonacular API key from environment variables
     * 
     * <p>This method loads the API key from the SPOONACULAR_API_KEY environment variable.
     * If the key is not found or is empty, the application will fail to start with
     * an IllegalStateException.
     * 
     * <p>This approach ensures:
     * <ul>
     *   <li>API key is never hardcoded in source code</li>
     *   <li>Key is not committed to version control</li>
     *   <li>Different keys can be used for different environments</li>
     *   <li>Clear error message if key is missing</li>
     * </ul>
     * 
     * @return The Spoonacular API key from environment variables
     * @throws IllegalStateException if SPOONACULAR_API_KEY environment variable is not set
     */
    private String getApiKey() {
        // First try environment variable
        String envApiKey = System.getenv("SPOONACULAR_API_KEY");
        if (envApiKey != null && !envApiKey.trim().isEmpty()) {
            return envApiKey;
        } else {
            throw new IllegalStateException(
                    "Spoonacular API key not found. Please set SPOONACULAR_API_KEY environment variable");
        }
    }
}
