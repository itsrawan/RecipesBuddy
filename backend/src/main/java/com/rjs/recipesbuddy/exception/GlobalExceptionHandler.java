package com.rjs.recipesbuddy.exception;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.rjs.recipesbuddy.dto.ApiErrorResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for the Application
 * 
 * <p>This class provides centralized exception handling for all controllers in the application.
 * It intercepts exceptions thrown during request processing and converts them into
 * appropriate HTTP responses with consistent error message formatting.
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Consistent error response format across all endpoints</li>
 *   <li>Appropriate HTTP status codes for different error types</li>
 *   <li>Detailed logging of all errors for debugging</li>
 *   <li>Security: No sensitive information exposed in error messages</li>
 *   <li>User-friendly error messages for client applications</li>
 * </ul>
 * 
 * <p>Handled Exception Types:
 * <ul>
 *   <li>RecipeNotFoundException - Returns 404 NOT FOUND</li>
 *   <li>SpoonacularApiException - Returns 502 BAD GATEWAY or original status</li>
 *   <li>MethodArgumentNotValidException - Returns 400 BAD REQUEST with field errors</li>
 *   <li>Exception (catch-all) - Returns 500 INTERNAL SERVER ERROR</li>
 * </ul>
 * 
 * @author Rawan Sweidan
 * @version 1.0.0
 * @since 2025-11-10
 * @see RestControllerAdvice
 * @see ExceptionHandler
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles RecipeNotFoundException when a recipe is not found
     * 
     * <p>This exception is thrown when a client requests a recipe by ID that doesn't
     * exist in the Spoonacular API database.
     * 
     * @param ex The RecipeNotFoundException that was thrown
     * @param request The HTTP request that caused the exception
     * @return ResponseEntity with 404 NOT FOUND status and error details
     */
    @ExceptionHandler(RecipeNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRecipeNotFound(
            RecipeNotFoundException ex, 
            HttpServletRequest request) {
        
        log.error("Recipe not found: {}", ex.getMessage());
        
        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles SpoonacularApiException when external API calls fail
     * 
     * <p>This exception is thrown when the Spoonacular API returns an error or
     * when there are network/connectivity issues. The handler translates the
     * external API error into an appropriate HTTP status code for the client.
     * 
     * <p>Status Code Mapping:
     * <ul>
     *   <li>5xx errors from Spoonacular → 502 BAD GATEWAY</li>
     *   <li>4xx errors from Spoonacular → Original status code</li>
     * </ul>
     * 
     * @param ex The SpoonacularApiException that was thrown
     * @param request The HTTP request that caused the exception
     * @return ResponseEntity with appropriate status code and error details
     */
    @ExceptionHandler(SpoonacularApiException.class)
    public ResponseEntity<ApiErrorResponse> handleSpoonacularApiException(
            SpoonacularApiException ex, 
            HttpServletRequest request) {
        
        log.error("Spoonacular API error: {}", ex.getMessage());
        
        HttpStatus status = ex.getStatusCode() >= 500 
                ? HttpStatus.BAD_GATEWAY 
                : HttpStatus.valueOf(ex.getStatusCode());
        
        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error("External API Error")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        
        return new ResponseEntity<>(error, status);
    }

    /**
     * Handles validation errors for request parameters and body
     * 
     * <p>This exception is thrown when Jakarta Bean Validation fails on controller
     * method parameters or request bodies. It provides detailed field-level error
     * messages to help clients understand what went wrong.
     * 
     * <p>Response includes:
     * <ul>
     *   <li>Timestamp of the error</li>
     *   <li>HTTP status (400 BAD REQUEST)</li>
     *   <li>Error type</li>
     *   <li>General message</li>
     *   <li>Map of field names to specific error messages</li>
     *   <li>Request path</li>
     * </ul>
     * 
     * @param ex The MethodArgumentNotValidException that was thrown
     * @param request The HTTP request that caused the exception
     * @return ResponseEntity with 400 BAD REQUEST status and detailed validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("message", "Invalid input parameters");
        response.put("errors", errors);
        response.put("path", request.getRequestURI());
        
        log.error("Validation error: {}", errors);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all uncaught exceptions (fallback handler)
     * 
     * <p>This is a catch-all exception handler that handles any exception not
     * specifically caught by other handlers. It logs the full exception stack trace
     * for debugging while returning a generic error message to the client to avoid
     * exposing internal implementation details.
     * 
     * <p>Security Note: The actual exception message is logged but not returned
     * to the client. The client receives a generic "unexpected error" message.
     * 
     * @param ex The uncaught Exception
     * @param request The HTTP request that caused the exception
     * @return ResponseEntity with 500 INTERNAL SERVER ERROR status and generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex, 
            HttpServletRequest request) {
        
        log.error("Unexpected error occurred", ex);
        
        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getRequestURI())
                .build();
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
