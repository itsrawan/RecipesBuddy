# RecipesBuddy - Security Documentation

## Overview

This document outlines the security measures implemented in the RecipesBuddy application and provides guidelines for maintaining security best practices.

**Author:** Rawan Sweidan  
**Version:** 1.0.0  
**Date:** November 2025

## Table of Contents

1. [Security Architecture](#security-architecture)
2. [API Key Management](#api-key-management)
3. [CORS Configuration](#cors-configuration)
4. [Input Validation](#input-validation)
5. [Error Handling](#error-handling)
6. [Dependencies Security](#dependencies-security)
7. [Docker Security](#docker-security)
8. [Best Practices](#best-practices)
9. [Security Checklist](#security-checklist)

## Security Architecture

### Defense in Depth

The application implements multiple layers of security:

```
┌─────────────────────────────────────┐
│         Frontend (React)            │
│  - Input sanitization               │
│  - HTTPS only (production)          │
└──────────────┬──────────────────────┘
               │ HTTPS
               ▼
┌─────────────────────────────────────┐
│       Backend (Spring Boot)         │
│  - CORS validation                  │
│  - Input validation                 │
│  - Error sanitization               │
│  - Rate limiting (TODO)             │
└──────────────┬──────────────────────┘
               │ HTTPS + API Key
               ▼
┌─────────────────────────────────────┐
│      External API (Spoonacular)     │
└─────────────────────────────────────┘
```

## API Key Management

### Current Implementation

✅ **Secure Storage**
- API key stored in environment variables
- Never hardcoded in source code
- Not committed to version control
- Loaded at application startup

**Implementation:**
```java
// WebClientConfig.java
private String getApiKey() {
    String envApiKey = System.getenv("SPOONACULAR_API_KEY");
    if (envApiKey != null && !envApiKey.trim().isEmpty()) {
        return envApiKey;
    }
    throw new IllegalStateException(
        "Spoonacular API key not found. Please set SPOONACULAR_API_KEY environment variable");
}
```

✅ **Backend-Only Access**
- API key injected via WebClient configuration
- Never exposed in API responses
- Never sent to frontend
- Automatic header injection for all Spoonacular requests

### Setting Environment Variables

**Windows (Command Prompt):**
```cmd
setx SPOONACULAR_API_KEY "your_api_key_here"
```

**Windows (PowerShell):**
```powershell
$env:SPOONACULAR_API_KEY="your_api_key_here"
```

**Linux/Mac:**
```bash
export SPOONACULAR_API_KEY="your_api_key_here"
```

**Docker:**
```bash
docker run -e SPOONACULAR_API_KEY=your_key recipes-buddy
```

**Docker Compose:**
```yaml
environment:
  - SPOONACULAR_API_KEY=${SPOONACULAR_API_KEY}
```

### Security Recommendations

⚠️ **IMPORTANT:**
1. Never log the API key
2. Never expose it in error messages
3. Rotate keys periodically
4. Use different keys for dev/staging/prod
5. Revoke compromised keys immediately

## CORS Configuration

### Current Implementation

✅ **Explicit Origin Whitelist**
```properties
cors.allowed-origins=http://localhost:3000,http://localhost:5173
```

✅ **Explicit Method Whitelist**
```properties
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
```

✅ **Configured Headers**
```properties
cors.allowed-headers=*
cors.allow-credentials=true
```

### Security Considerations

**Development vs Production:**

```properties
# Development
cors.allowed-origins=http://localhost:3000,http://localhost:5173

# Production - MUST be updated!
cors.allowed-origins=https://yourdomain.com,https://www.yourdomain.com
```

⚠️ **NEVER use `*` wildcard in production!**

### Recommendations

1. **Update for Production:**
   - Replace localhost URLs with actual domain
   - Use HTTPS only
   - Minimize allowed methods
   - Consider specific headers instead of `*`

2. **Example Production Config:**
```properties
cors.allowed-origins=https://recipesbuddy.com
cors.allowed-methods=GET,POST
cors.allowed-headers=Content-Type,Authorization
cors.allow-credentials=false
```

## Input Validation

### Current Implementation

✅ **Jakarta Bean Validation**
```java
@RestController
@Validated
public class RecipeController {
    
    @GetMapping("/search")
    public ResponseEntity<RecipeSearchResponse> searchRecipes(
            @RequestParam String query,
            @RequestParam(required = false) @Min(0) @Max(5000) Integer maxCalories,
            @RequestParam(defaultValue = "12") @Min(1) Integer size) {
        // ...
    }
}
```

✅ **Request Body Validation**
```java
@PostMapping("/{id}/calories")
public ResponseEntity<CalorieUpdateResponse> calculateUpdatedCalories(
        @PathVariable @Min(1) Long id,
        @Valid @RequestBody CalorieUpdateRequest request) {
    // ...
}
```

### Validation Rules

| Parameter | Constraint | Reason |
|-----------|------------|--------|
| maxCalories | 0-5000 | Realistic range for recipe calories |
| maxCarbs | 0-5000 | Realistic range for carbs |
| minProtein | 0-5000 | Realistic range for protein |
| maxFat | 0-5000 | Realistic range for fat |
| size | >= 1 | Must request at least 1 result |
| offset | >= 0 | Cannot have negative offset |
| recipeId | >= 1 | Valid recipe IDs are positive |

### Recommendations

⚠️ **Add Rate Limiting:**
```java
// TODO: Implement rate limiting
@RateLimiter(name = "recipeApi", fallbackMethod = "rateLimitFallback")
public ResponseEntity<RecipeSearchResponse> searchRecipes(...) {
    // ...
}
```

## Error Handling

### Current Implementation

✅ **No Sensitive Information in Errors**
```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ApiErrorResponse> handleGenericException(
        Exception ex, HttpServletRequest request) {
    
    log.error("Unexpected error occurred", ex);  // Logged but not exposed
    
    return new ResponseEntity<>(ApiErrorResponse.builder()
            .message("An unexpected error occurred. Please try again later.")  // Generic message
            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
}
```

✅ **Consistent Error Format**
```json
{
  "timestamp": "2025-11-10T22:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Recipe with ID 999 not found",
  "path": "/api/recipes/999"
}
```

✅ **Proper Status Codes**
- 400: Bad Request (validation errors)
- 404: Not Found (recipe doesn't exist)
- 500: Internal Server Error (unexpected errors)
- 502: Bad Gateway (external API errors)

### Security Benefits

1. **No Stack Traces Exposed** - Stack traces only in logs
2. **No Internal Details** - Generic messages for unexpected errors
3. **Consistent Response** - Same format for all errors
4. **Proper Logging** - All errors logged for debugging

## Dependencies Security

### Current Dependencies

```xml
<!-- Core Dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>3.5.7</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Security Scanning

**Maven Dependency Check:**
```bash
mvn org.owasp:dependency-check-maven:check
```

**Update Dependencies Regularly:**
```bash
mvn versions:display-dependency-updates
```

### Recommendations

1. **Regular Updates:**
   - Update Spring Boot regularly for security patches
   - Subscribe to security advisories
   - Test updates in development first

2. **Dependency Scanning:**
   - Add OWASP Dependency Check to CI/CD
   - Use Snyk or similar tools
   - Monitor for CVEs

3. **Minimal Dependencies:**
   - Only include necessary dependencies
   - Remove unused libraries
   - Audit transitive dependencies

## Docker Security

### Current Dockerfile Review

**Backend Dockerfile:**
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Security Recommendations

✅ **Good Practices Implemented:**
- Using official Eclipse Temurin image
- Using JRE (not full JDK) for smaller attack surface
- Using Alpine for minimal footprint

⚠️ **Improvements Needed:**

1. **Run as Non-Root User:**
```dockerfile
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY target/*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]
```

2. **Read-Only Filesystem:**
```dockerfile
ENTRYPOINT ["java", "-jar", "-Djava.io.tmpdir=/tmp", "app.jar"]
```

3. **Health Checks:**
```dockerfile
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
```

4. **Limit Resources:**
```dockerfile
# In docker-compose.yml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 512M
```

## Best Practices

### 1. Principle of Least Privilege

✅ **Implemented:**
- API key has minimal required permissions
- Actuator endpoints limited to health/info/metrics
- CORS restricted to specific origins

⚠️ **TODO:**
- Add authentication for admin endpoints (if added)
- Implement role-based access control (if needed)

### 2. Defense in Depth

✅ **Multiple Security Layers:**
1. Input validation at controller level
2. Business logic validation in service layer
3. Exception handling catches unexpected errors
4. CORS prevents unauthorized origins
5. Environment variable protection for secrets

### 3. Fail Securely

✅ **Secure Failure Modes:**
- Application fails to start if API key missing
- Invalid requests rejected with 400 status
- Unknown errors return generic message
- All errors logged for investigation

### 4. Secure Defaults

✅ **Secure Configuration:**
- CORS defaults to localhost only
- Validation enabled by default
- Actuator endpoints limited by default
- HTTPS enforced in production (via deployment)

### 5. Keep Security Simple

✅ **Simple and Maintainable:**
- Centralized exception handling
- Single configuration point for CORS
- Clear validation rules
- Minimal custom security code

## Security Checklist

### Development

- [ ] Never commit secrets to version control
- [ ] Use environment variables for sensitive data
- [ ] Validate all user inputs
- [ ] Use parameterized queries (when DB added)
- [ ] Keep dependencies updated
- [ ] Run security scans regularly

### Testing

- [ ] Test input validation thoroughly
- [ ] Test error handling scenarios
- [ ] Test CORS configuration
- [ ] Test with invalid API keys
- [ ] Test rate limiting (when implemented)
- [ ] Security penetration testing

### Deployment

- [ ] Use HTTPS in production
- [ ] Update CORS allowed origins
- [ ] Use strong, unique API keys
- [ ] Enable security headers
- [ ] Configure firewall rules
- [ ] Set up monitoring and alerts
- [ ] Regular security audits
- [ ] Implement rate limiting
- [ ] Use secrets management (e.g., AWS Secrets Manager)

### Monitoring

- [ ] Log all authentication attempts
- [ ] Monitor for unusual activity
- [ ] Set up alerts for errors
- [ ] Track API usage
- [ ] Monitor dependency vulnerabilities
- [ ] Review logs regularly

## Security Headers (Production)

### Recommended Headers

Add these security headers in production:

```java
@Configuration
public class SecurityHeadersConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   Object handler) {
                // Prevent clickjacking
                response.setHeader("X-Frame-Options", "DENY");
                
                // Prevent MIME type sniffing
                response.setHeader("X-Content-Type-Options", "nosniff");
                
                // Enable XSS protection
                response.setHeader("X-XSS-Protection", "1; mode=block");
                
                // Content Security Policy
                response.setHeader("Content-Security-Policy", 
                    "default-src 'self'");
                
                // Referrer Policy
                response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                
                // Strict Transport Security (HTTPS only)
                response.setHeader("Strict-Transport-Security", 
                    "max-age=31536000; includeSubDomains");
                
                return true;
            }
        });
    }
}
```

## Incident Response

### If API Key is Compromised

1. **Immediate Actions:**
   - Revoke the compromised key immediately
   - Generate a new key
   - Update environment variables
   - Restart application
   - Review logs for unauthorized usage

2. **Investigation:**
   - Determine how key was exposed
   - Check for unusual API calls
   - Review code commits
   - Audit access logs

3. **Prevention:**
   - Address root cause
   - Update security practices
   - Additional monitoring

### Reporting Security Issues

If you discover a security vulnerability:

1. **DO NOT** create a public GitHub issue
2. Email the development team privately
3. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

## Future Security Enhancements

### Priority 1 (High)
- [ ] Implement rate limiting
- [ ] Add security headers configuration
- [ ] Update Docker to run as non-root
- [ ] Add dependency vulnerability scanning to CI/CD

### Priority 2 (Medium)
- [ ] Implement API authentication (if needed)
- [ ] Add request/response logging
- [ ] Implement audit logging
- [ ] Add monitoring dashboards

### Priority 3 (Low)
- [ ] Consider adding API versioning
- [ ] Implement request throttling
- [ ] Add geographic restrictions (if applicable)
- [ ] Consider adding API gateway

## Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security](https://spring.io/projects/spring-security)
- [Docker Security](https://docs.docker.com/engine/security/)
- [OWASP Dependency Check](https://owasp.org/www-project-dependency-check/)
- [Snyk](https://snyk.io/)

## Contact

For security concerns or questions:

**Developer:** Rawan Sweidan  
**Project:** RecipesBuddy  
**Date:** November 2025

**Remember:** Security is an ongoing process, not a one-time task!
