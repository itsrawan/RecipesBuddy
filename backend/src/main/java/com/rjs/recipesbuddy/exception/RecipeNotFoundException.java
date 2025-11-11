package com.rjs.recipesbuddy.exception;

/**
 * Exception thrown when a recipe is not found
 * 
 * <p>This exception is thrown when a client requests a recipe by ID that doesn't
 * exist in the Spoonacular API database or when the recipe data cannot be retrieved.
 * 
 * <p>This is a RuntimeException, so it doesn't need to be declared in method signatures.
 * It's caught by the GlobalExceptionHandler and converted to a 404 NOT FOUND response.
 * 
 * @author Rawan Sweidan
 * @version 1.0.0
 * @since 2025-11-10
 * @see GlobalExceptionHandler
 */
public class RecipeNotFoundException extends RuntimeException {
    /**
     * Creates a RecipeNotFoundException with a custom message
     * 
     * @param message The error message describing why the recipe was not found
     */
    public RecipeNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Creates a RecipeNotFoundException with a message and underlying cause
     * 
     * @param message The error message
     * @param cause The underlying exception that caused this exception
     */
    public RecipeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Creates a RecipeNotFoundException with a standardized message for a recipe ID
     * 
     * <p>This is the most commonly used constructor. It creates a user-friendly
     * error message that includes the recipe ID that wasn't found.
     * 
     * @param recipeId The ID of the recipe that was not found
     */
    public RecipeNotFoundException(Long recipeId) {
        super(String.format("Recipe with ID %d not found", recipeId));
    }
}
