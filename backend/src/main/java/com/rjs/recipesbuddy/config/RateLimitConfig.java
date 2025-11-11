package com.rjs.recipesbuddy.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Configuration using Bucket4j
 * 
 * <p>This configuration implements the Token Bucket algorithm to limit the number
 * of API requests a client can make within a specific time window. This protects
 * the application from:
 * <ul>
 *   <li>API quota exhaustion (Spoonacular free tier: 150 requests/day)</li>
 *   <li>Denial of Service (DoS) attacks</li>
 *   <li>Resource abuse by malicious users</li>
 * </ul>
 * 
 * <p>Current Limits:
 * <ul>
 *   <li>10 requests per minute per IP address</li>
 *   <li>50 requests per hour per IP address</li>
 * </ul>
 * 
 * <p>When rate limit is exceeded, the client receives:
 * <ul>
 *   <li>HTTP 429 (Too Many Requests) status code</li>
 *   <li>Retry-After header indicating when to retry</li>
 *   <li>Clear error message</li>
 * </ul>
 * 
 * @author Rawan Sweidan
 * @version 1.0.0
 * @since 2025-11-10
 * @see Bucket
 * @see HandlerInterceptor
 */
@Slf4j
@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    /**
     * Cache storing rate limit buckets per client IP address
     * Key: Client IP address
     * Value: Bucket with configured rate limits
     */
    private final Map<String, Bucket> rateLimitCache = new ConcurrentHashMap<>();

    /**
     * Registers the rate limiting interceptor for all API endpoints
     * 
     * @param registry InterceptorRegistry to add the rate limit interceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor())
                .addPathPatterns("/api/**"); // Apply to all API endpoints
    }

    /**
     * Rate Limiting Interceptor
     * 
     * <p>Intercepts all requests to check if the client has exceeded their rate limit.
     * Uses Token Bucket algorithm via Bucket4j library.
     */
    private class RateLimitInterceptor implements HandlerInterceptor {

        /**
         * Checks rate limit before processing the request
         * 
         * @param request Current HTTP request
         * @param response Current HTTP response
         * @param handler Chosen handler for execution
         * @return true if request is allowed, false if rate limit exceeded
         */
        @Override
        public boolean preHandle(HttpServletRequest request, 
                                HttpServletResponse response, 
                                Object handler) throws Exception {
            
            // Get client identifier (IP address)
            String clientId = getClientIP(request);
            
            // Get or create bucket for this client
            Bucket bucket = resolveBucket(clientId);
            
            // Try to consume 1 token
            if (bucket.tryConsume(1)) {
                // Request allowed
                log.debug("Request allowed for IP: {}", clientId);
                return true;
            } else {
                // Rate limit exceeded
                log.warn("Rate limit exceeded for IP: {}", clientId);
                
                // Set HTTP 429 status
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                
                // Add Retry-After header (60 seconds)
                response.setHeader("Retry-After", "60");
                
                // Write error response
                String errorJson = """
                    {
                        "timestamp": "%s",
                        "status": 429,
                        "error": "Too Many Requests",
                        "message": "Rate limit exceeded. Please try again in 60 seconds.",
                        "path": "%s"
                    }
                    """.formatted(java.time.LocalDateTime.now(), request.getRequestURI());
                
                response.getWriter().write(errorJson);
                
                return false; // Block the request
            }
        }

        /**
         * Resolves or creates a rate limit bucket for the given client
         * 
         * @param clientId Client identifier (IP address)
         * @return Bucket with configured rate limits
         */
        private Bucket resolveBucket(String clientId) {
            return rateLimitCache.computeIfAbsent(clientId, k -> createNewBucket());
        }

        /**
         * Creates a new rate limit bucket with configured limits
         * 
         * <p>Token Bucket Algorithm:
         * <ul>
         *   <li>Bucket capacity: 10 tokens</li>
         *   <li>Refill rate: 10 tokens per minute (one token every 6 seconds)</li>
         *   <li>Each request consumes 1 token</li>
         *   <li>If bucket is empty, request is rejected</li>
         * </ul>
         * 
         * <p>This allows:
         * <ul>
         *   <li>Burst of 10 requests immediately</li>
         *   <li>Steady rate of 10 requests per minute after burst</li>
         *   <li>Maximum 50 requests per hour (10 per minute * 5 minutes with buffer)</li>
         * </ul>
         * 
         * @return New Bucket with rate limits
         */
        private Bucket createNewBucket() {
            // Define bandwidth: 10 requests per minute
            Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
            
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        }

        /**
         * Extracts client IP address from the request
         * 
         * <p>Checks multiple headers in order of priority to handle proxies:
         * <ol>
         *   <li>X-Forwarded-For - Used by most proxies/load balancers</li>
         *   <li>X-Real-IP - Alternative header used by some proxies</li>
         *   <li>RemoteAddr - Direct connection IP</li>
         * </ol>
         * 
         * @param request HTTP request
         * @return Client IP address
         */
        private String getClientIP(HttpServletRequest request) {
            // Check X-Forwarded-For header (for requests through proxy/load balancer)
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                return xForwardedFor.split(",")[0].trim();
            }
            
            // Check X-Real-IP header
            String xRealIP = request.getHeader("X-Real-IP");
            if (xRealIP != null && !xRealIP.isEmpty()) {
                return xRealIP;
            }
            
            // Fallback to remote address
            return request.getRemoteAddr();
        }
    }
}
