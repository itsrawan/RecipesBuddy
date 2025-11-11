package com.rjs.recipesbuddy.service;

import com.rjs.recipesbuddy.dto.*;
import com.rjs.recipesbuddy.exception.RecipeNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Recipe Service - Business Logic Layer
 * 
 * <p>This service handles the core business logic for recipe operations.
 * It coordinates between the controller layer and the Spoonacular API service,
 * processing data and implementing business rules for recipe search and calorie calculations.
 * 
 * <p>Key Responsibilities:
 * <ul>
 *   <li>Recipe search coordination and data transformation</li>
 *   <li>Recipe detail retrieval and validation</li>
 *   <li>Complex calorie calculations when ingredients are excluded</li>
 *   <li>Error handling and logging</li>
 * </ul>
 * 
 * <p>This service is stateless and thread-safe.
 * 
 * @author Rawan Sweidan
 * @version 1.0.0
 * @since 2025-11-10
 * @see SpoonacularApiService
 * @see RecipeController
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final SpoonacularApiService spoonacularApiService;

    /**
     * Search for recipes with optional ingredient exclusions and nutrition filters
     * 
     * <p>Delegates the search request to the Spoonacular API service with all specified filters.
     * 
     * @param request RecipeSearchRequest containing search query and filter criteria
     * @return RecipeSearchResponse containing matching recipes and total result count
     */
    public RecipeSearchResponse searchRecipes(RecipeSearchRequest request) {
        log.info("Processing recipe search request: {}", request);
        
        return spoonacularApiService.searchRecipes(
                request.getQuery(),
                request.getExcludeIngredients(),
                request.getIncludeIngredients(),
                request.getMaxCalories(),
                request.getMaxCarbs(),
                request.getMinProtein(),
                request.getMaxFat(),
                request.getSize(),
                request.getOffset()
        );
    }

    /**
     * Get complete recipe details by ID
     * 
     * <p>Retrieves full recipe information including ingredients, instructions,
     * and comprehensive nutritional data. Validates that the recipe exists.
     * 
     * @param recipeId The unique identifier of the recipe
     * @return RecipeDetailResponse containing complete recipe information
     * @throws RecipeNotFoundException if no recipe exists with the given ID
     */
    public RecipeDetailResponse getRecipeById(Long recipeId) {
        log.info("Fetching recipe by ID: {}", recipeId);
        
        RecipeDetailResponse recipe = spoonacularApiService.getRecipeDetails(recipeId);
        
        if (recipe == null) {
            throw new RecipeNotFoundException(recipeId);
        }
        
        return recipe;
    }

    /**
     * Calculate updated calories after excluding specific ingredients
     * 
     * <p>This method performs a multi-step calculation:
     * <ol>
     *   <li>Fetches the complete recipe details</li>
     *   <li>Identifies the original total calories</li>
     *   <li>For each excluded ingredient, fetches its individual calorie information</li>
     *   <li>Subtracts excluded ingredient calories from the total</li>
     *   <li>Returns original, updated, and reduced calorie values</li>
     * </ol>
     * 
     * <p>Note: This method makes multiple API calls to the Spoonacular service
     * to get accurate calorie data for each ingredient.
     * 
     * @param recipeId The ID of the recipe to calculate calories for
     * @param excludedIngredientIds List of ingredient IDs to exclude from the calculation
     * @return CalorieUpdateResponse containing original, updated, and reduced calorie values
     * @throws RecipeNotFoundException if the recipe doesn't exist
     * @throws IllegalStateException if the recipe has no calorie or ingredient data
     */
    public CalorieUpdateResponse calculateUpdatedCalories(
            Long recipeId, 
            List<Long> excludedIngredientIds) {
        
        log.info("Calculating updated calories for recipe {} with exclusions: {}", 
                recipeId, excludedIngredientIds);

        RecipeDetailResponse recipe = getRecipeById(recipeId);
        
        if (recipe.getExtendedIngredients() == null || recipe.getExtendedIngredients().isEmpty()) {
            throw new IllegalStateException("Recipe has no ingredients data");
        }

        // Get original total calories from recipe nutrition data
        double originalCalories = recipe.getNutrition() != null && recipe.getNutrition().getTotalCalories() != null
                ? recipe.getNutrition().getTotalCalories()
                : 0.0;

        if (originalCalories == 0.0) {
            log.warn("Recipe {} has no calorie information", recipeId);
            throw new IllegalStateException("Recipe has no calorie information available");
        }

        // Calculate calories to subtract from excluded ingredients
        double excludedCalories = 0.0;
        for (Long ingredientId : excludedIngredientIds) {
            Ingredient excludedIngredient = recipe.getExtendedIngredients().stream()
                    .filter(ing -> ing.getId().equals(ingredientId))
                    .findFirst()
                    .orElse(null);
             
            if (excludedIngredient != null) {
                double ingredientCalories = getIngredientCalories(
                        excludedIngredient.getId(),
                        excludedIngredient.getAmount(),
                        excludedIngredient.getUnit()
                );
                excludedCalories += ingredientCalories;
                log.debug("Ingredient {} ({}) contributes {} calories", 
                        excludedIngredient.getName(), ingredientId, ingredientCalories);
            }
        }

        double updatedCalories = Math.max(0, originalCalories - excludedCalories);
        double caloriesReduced = originalCalories - updatedCalories;

        log.info("Calorie calculation complete: original={}, excluded={}, updated={}, reduced={}", 
                originalCalories, excludedCalories, updatedCalories, caloriesReduced);

        return CalorieUpdateResponse.builder()
                .recipeId(recipeId)
                .originalCalories(originalCalories)
                .updatedCalories(updatedCalories)
                .caloriesReduced(caloriesReduced)
                .ingredientsExcluded(excludedIngredientIds.size())
                .build();
    }

    /**
     * Get calories for a specific ingredient by fetching its nutrition data
     * 
     * <p>Queries the Spoonacular API for detailed ingredient information and extracts
     * the calorie value from the nutrition data. Returns 0 if the ingredient data
     * is unavailable or if an error occurs.
     * 
     * @param ingredientId The unique identifier of the ingredient
     * @param amount The quantity of the ingredient
     * @param unit The measurement unit (e.g., "g", "cup", "serving")
     * @return The calorie count for the specified amount of ingredient, or 0 if unavailable
     */
    private double getIngredientCalories(Long ingredientId, Double amount, String unit) {
        try {
            Ingredient ingredientInfo = spoonacularApiService.getIngredientInformation(
                    ingredientId, 
                    amount != null ? amount : 1.0, 
                    unit != null ? unit : "serving"
            );

            if (ingredientInfo != null && ingredientInfo.getNutrition() != null 
                    && ingredientInfo.getNutrition().getNutrients() != null) {
                
                return ingredientInfo.getNutrition().getNutrients().stream()
                        .filter(nutrient -> "Calories".equalsIgnoreCase(nutrient.getName()))
                        .mapToDouble(Nutrient::getAmount)
                        .sum();
            }
        } catch (Exception ex) {
            log.error("Error fetching ingredient calories for ID {}: {}", ingredientId, ex.getMessage());
        }

        return 0.0;
    }
}
