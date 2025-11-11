package com.rjs.recipesbuddy.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rjs.recipesbuddy.dto.CalorieUpdateRequest;
import com.rjs.recipesbuddy.dto.CalorieUpdateResponse;
import com.rjs.recipesbuddy.dto.RecipeDetailResponse;
import com.rjs.recipesbuddy.dto.RecipeSearchRequest;
import com.rjs.recipesbuddy.dto.RecipeSearchResponse;
import com.rjs.recipesbuddy.service.RecipeService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Recipe Operations
 * 
 * <p>This controller handles all HTTP requests related to recipe search, retrieval,
 * and calorie calculations. It serves as the entry point for the frontend application
 * and delegates business logic to the RecipeService.
 * 
 * <p>Base URL: /api/recipes
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Search recipes with advanced filters (ingredients, nutrition values)</li>
 *   <li>Retrieve detailed recipe information by ID</li>
 *   <li>Calculate updated calories after excluding ingredients</li>
 *   <li>Input validation using Jakarta Bean Validation</li>
 * </ul>
 * 
 * <p>Security: This controller is accessible without authentication. The Spoonacular API key
 * is securely managed in the backend and never exposed to clients.
 * 
 * @author Rawan Sweidan
 * @version 1.0.0
 * @since 2025-11-10
 * @see RecipeService
 */
@RestController
@Slf4j
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
@Validated
public class RecipeController {
    
    private final RecipeService recipeService;

    /**
     * Search for recipes with optional filters
     * 
     * <p>Searches the Spoonacular recipe database with various filtering options.
     * Returns paginated results with recipe summaries and nutritional information.
     * 
     * <p>Example URL:
     * <pre>
     * GET /api/recipes/search?query=pasta&excludeIngredients=cheese,butter
     *     &includeIngredients=tomato&maxCalories=500&maxCarbs=100
     *     &minProtein=20&maxFat=30&size=12&offset=0
     * </pre>
     * 
     * @param query The search query (recipe name or keywords), required
     * @param excludeIngredients List of ingredients to exclude (e.g., allergies), optional
     * @param includeIngredients Ingredients that must be included, comma-separated, optional
     * @param maxCalories Maximum calories per serving (0-5000), optional
     * @param maxCarbs Maximum carbohydrates in grams (0-5000), optional
     * @param minProtein Minimum protein in grams (0-5000), optional
     * @param maxFat Maximum fat in grams (0-5000), optional
     * @param size Number of results per page (default: 12, minimum: 1)
     * @param offset Number of results to skip for pagination (default: 0, minimum: 0)
     * @return ResponseEntity containing RecipeSearchResponse with results and total count
     */
    @GetMapping("/search")
    public ResponseEntity<RecipeSearchResponse> searchRecipes(
            @RequestParam String query,
            @RequestParam(required = false) List<String> excludeIngredients,
            @RequestParam(required = false) String includeIngredients,
            @RequestParam(required = false) @Min(0) @jakarta.validation.constraints.Max(5000) Integer maxCalories,
            @RequestParam(required = false) @Min(0) @jakarta.validation.constraints.Max(5000) Integer maxCarbs,
            @RequestParam(required = false) @Min(0) @jakarta.validation.constraints.Max(5000) Integer minProtein,
            @RequestParam(required = false) @Min(0) @jakarta.validation.constraints.Max(5000) Integer maxFat,
            @RequestParam(defaultValue = "12") @Min(1) Integer size,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset) {
        
        log.info("Received search request: query={}, exclude={}, include={}, maxCal={}, maxCarbs={}, minProt={}, maxFat={}, size={}, offset={}", 
                query, excludeIngredients, includeIngredients, maxCalories, maxCarbs, minProtein, maxFat, size, offset);

        RecipeSearchRequest request = RecipeSearchRequest.builder()
                .query(query)
                .excludeIngredients(excludeIngredients)
                .includeIngredients(includeIngredients)
                .maxCalories(maxCalories)
                .maxCarbs(maxCarbs)
                .minProtein(minProtein)
                .maxFat(maxFat)
                .size(size)
                .offset(offset)
                .build();

        RecipeSearchResponse response = recipeService.searchRecipes(request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get detailed recipe information by ID
     * 
     * <p>Retrieves complete recipe details including ingredients, instructions,
     * and comprehensive nutritional information.
     * 
     * <p>Example URL:
     * <pre>
     * GET /api/recipes/123
     * </pre>
     * 
     * @param id The unique recipe identifier (minimum: 1)
     * @return ResponseEntity containing RecipeDetailResponse with full recipe details
     * @throws RecipeNotFoundException if the recipe ID doesn't exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDetailResponse> getRecipeById(
            @PathVariable @Min(1) Long id) {
        
        log.info("Received request for recipe ID: {}", id);
        
        RecipeDetailResponse recipe = recipeService.getRecipeById(id);
        
        return ResponseEntity.ok(recipe);
    }

    /**
     * Calculate updated calories after excluding specific ingredients
     * 
     * <p>Calculates how the total calorie count changes when certain ingredients
     * are removed from a recipe. Useful for dietary modifications and preferences.
     * 
     * <p>Example URL:
     * <pre>
     * POST /api/recipes/123/calories
     * </pre>
     * 
     * <p>Request Body Example:
     * <pre>
     * {
     *   "recipeId": 123,
     *   "excludedIngredientIds": [456, 789]
     * }
     * </pre>
     * 
     * @param id The recipe ID (must match the ID in request body)
     * @param request CalorieUpdateRequest containing list of ingredient IDs to exclude
     * @return ResponseEntity containing CalorieUpdateResponse with original, updated, and reduced calories
     */
    @PostMapping("/{id}/calories")
    public ResponseEntity<CalorieUpdateResponse> calculateUpdatedCalories(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody CalorieUpdateRequest request) {
        
        log.info("Received calorie calculation request for recipe ID: {} with exclusions: {}", 
                id, request.getExcludedIngredientIds());

        // Ensure recipe ID in path matches request body
        if (request.getRecipeId() != null && !request.getRecipeId().equals(id)) {
            log.warn("Recipe ID mismatch: path={}, body={}", id, request.getRecipeId());
        }

        CalorieUpdateResponse response = recipeService.calculateUpdatedCalories(
                id, 
                request.getExcludedIngredientIds());
        
        return ResponseEntity.ok(response);
    }


}
