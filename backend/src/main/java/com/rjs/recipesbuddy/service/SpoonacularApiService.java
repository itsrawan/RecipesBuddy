package com.rjs.recipesbuddy.service;

import java.time.Duration;
import java.util.List;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.rjs.recipesbuddy.dto.Ingredient;
import com.rjs.recipesbuddy.dto.RecipeDetailResponse;
import com.rjs.recipesbuddy.dto.RecipeSearchResponse;
import com.rjs.recipesbuddy.exception.RecipeNotFoundException;
import com.rjs.recipesbuddy.exception.SpoonacularApiException;
import com.rjs.recipesbuddy.util.ValueValidator;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spoonacular API Service - External API Integration Layer
 * 
 * <p>This service manages all interactions with the Spoonacular Food API.
 * It acts as an abstraction layer between the application and the external API,
 * handling HTTP communication, error handling, and retry logic.
 * 
 * <p>Key Features:
 * <ul>
 *   <li>WebClient-based reactive HTTP communication</li>
 *   <li>Automatic retry mechanism for transient failures (2 retries with 1-second delay)</li>
 *   <li>Comprehensive error handling and logging</li>
 *   <li>Mock response support for development and testing</li>
 *   <li>Toggle between mock and live mode via configuration</li>
 *   <li>Secure API key management (injected via WebClient configuration)</li>
 * </ul>
 * 
 * <p>Mock Mode Configuration:
 * <p>Set `spoonacular.mock-mode=true` in application.properties to use mock responses
 * and save API quota during development. Set to `false` for production use with live API.
 * 
 * <p>API Documentation: https://spoonacular.com/food-api/docs
 * 
 * <p>Rate Limits: Free tier allows 150 requests per day.
 * 
 * @author Rawan Sweidan
 * @version 1.0.0
 * @since 2025-11-10
 * @see WebClientConfig
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpoonacularApiService {
    
    private final WebClient spoonacularWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Mock mode flag from application.properties
     * When true, uses mock JSON responses instead of real API calls
     */
    @Value("${spoonacular.mock-mode:false}")
    private boolean mockMode;

    /**
     * Read a static JSON file from classpath for mock responses
     * 
     * <p>This method is used during development and testing to load mock API responses
     * from bundled JSON files. The file should be located in the same package as this class
     * under the resources directory.
     * 
     * @param filename optional filename (if null or empty, "static_search_response.json" is used)
     * @return file contents as UTF-8 string
     * @throws SpoonacularApiException when file cannot be read
     */
    public String readStaticSearchJson(String filename) {
        String file = (filename == null || filename.isEmpty()) ? "static_search_response.json" : filename;

        // Try resource relative to the class package first
        String pkgRelative = file;
        String absolutePath = "/" + SpoonacularApiService.class.getPackage().getName().replace('.', '/') + "/" + file;

        InputStream is = null;
        try {
            is = SpoonacularApiService.class.getResourceAsStream(pkgRelative);
            if (is == null) {
                is = SpoonacularApiService.class.getResourceAsStream(absolutePath);
            }

            if (is == null) {
                throw new IOException("Resource not found: " + file + " (tried " + pkgRelative + " and " + absolutePath + ")");
            }

            byte[] bytes = is.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);

        } catch (IOException ex) {
            log.error("Failed to read static JSON file '{}': {}", file, ex.getMessage());
            throw new SpoonacularApiException("Failed to read static JSON file: " + ex.getMessage(), 500);
        } finally {
            if (is != null) {
                try { is.close(); } catch (IOException ignored) {}
            }
        }
    }

    /**
     * Deserialize JSON string to RecipeSearchResponse object
     * 
     * <p>Converts a JSON string into a strongly-typed RecipeSearchResponse object.
     * Used in conjunction with readStaticSearchJson for mock responses.
     * 
     * @param jsonString JSON string to deserialize
     * @return RecipeSearchResponse object
     * @throws SpoonacularApiException if deserialization fails
     */
    public RecipeSearchResponse deserializeSearchResponse(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, RecipeSearchResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to RecipeSearchResponse: {}", e.getMessage());
            throw new SpoonacularApiException("Failed to parse recipe search response: " + e.getMessage(), 500);
        }
    }
    
    /**
     * Deserialize JSON string to RecipeDetailResponse object
     * 
     * @param jsonString JSON string to deserialize
     * @return RecipeDetailResponse object
     * @throws SpoonacularApiException if deserialization fails
     */
    public RecipeDetailResponse deserializeRecipeDetail(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, RecipeDetailResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to RecipeDetailResponse: {}", e.getMessage());
            throw new SpoonacularApiException("Failed to parse recipe detail response: " + e.getMessage(), 500);
        }
    }

    /**
     * Search for recipes using the Spoonacular API or mock data
     * 
     * <p>Executes a complex search query against the Spoonacular recipes database.
     * Supports multiple filtering criteria including ingredients, nutrition values,
     * and pagination.
     * 
     * <p>Mode Selection:
     * <ul>
     *   <li>Mock Mode (mockMode=true): Returns static JSON from resources (FREE - no API quota used)</li>
     *   <li>Live Mode (mockMode=false): Calls actual Spoonacular API (USES 1 API REQUEST)</li>
     * </ul>
     * 
     * <p>API Endpoint: GET /recipes/complexSearch
     * <p>Documentation: https://spoonacular.com/food-api/docs#Search-Recipes-Complex
     * 
     * @param query The search query (recipe name or keywords)
     * @param excludeIngredients List of ingredients to exclude
     * @param includeIngredients Comma-separated ingredients that must be included
     * @param maxCalories Maximum calories per serving
     * @param maxCarbs Maximum carbohydrates in grams
     * @param minProtein Minimum protein in grams
     * @param maxFat Maximum fat in grams
     * @param size Number of results to return (page size)
     * @param offset Number of results to skip (pagination)
     * @return RecipeSearchResponse containing matching recipes and total count
     * @throws SpoonacularApiException if the API call fails
     */
    public RecipeSearchResponse searchRecipes(String query, List<String> excludeIngredients, 
                                               String includeIngredients, Integer maxCalories,
                                               Integer maxCarbs, Integer minProtein, Integer maxFat,
                                               Integer size, Integer offset) {
        log.info("Searching recipes [mockMode={}]: query={}, exclude={}, include={}, maxCal={}, maxCarbs={}, minProt={}, maxFat={}, number={}, offset={}", 
                mockMode, query, excludeIngredients, includeIngredients, maxCalories, maxCarbs, minProtein, maxFat, size, offset);

        try {
            // MOCK MODE - Use static JSON response (FREE)
            if (mockMode) {
                log.info("Using MOCK MODE for recipe search - No API quota consumed");
                String jsonString = this.readStaticSearchJson("static_search_response.json");
                RecipeSearchResponse response = this.deserializeSearchResponse(jsonString);
                log.info("Recipe search successful (MOCK): found {} results", 
                        response != null ? response.getTotalResults() : 0);
                return response;
            }
            
            // LIVE MODE - Call real Spoonacular API (USES API QUOTA)
            log.info("Using LIVE MODE for recipe search - API quota will be consumed");
            
            StringBuilder uriBuilder = new StringBuilder("/recipes/complexSearch")
                    .append("?query=").append(query)
                    .append("&number=").append(size)
                    .append("&offset=").append(offset)
                    .append("&addRecipeInformation=false")
                    .append("&addRecipeNutrition=true")
                    .append("&fillIngredients=false");

            if (! ValueValidator.isVoid(excludeIngredients)) {
                String excludeParam = String.join(",", excludeIngredients);
                uriBuilder.append("&excludeIngredients=").append(excludeParam);
            }

            if (!ValueValidator.isVoid(includeIngredients)) {
                uriBuilder.append("&includeIngredients=").append(includeIngredients.trim());
            }

            if (!ValueValidator.isVoid(maxCalories) && maxCalories > 0) {
                uriBuilder.append("&maxCalories=").append(maxCalories);
            }

            if (!ValueValidator.isVoid(maxCarbs) && maxCarbs > 0) {
                uriBuilder.append("&maxCarbs=").append(maxCarbs);
            }

            if (!ValueValidator.isVoid(minProtein) && minProtein > 0) {
                uriBuilder.append("&minProtein=").append(minProtein);
            }

            if (!ValueValidator.isVoid(maxFat) && maxFat > 0) {
                uriBuilder.append("&maxFat=").append(maxFat);
            }

            RecipeSearchResponse response = spoonacularWebClient
                    .get()
                    .uri(uriBuilder.toString())
                    .retrieve()
                    .bodyToMono(RecipeSearchResponse.class)
                    .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(1)))
                    .block();

            log.info("Recipe search successful (LIVE): found {} results", 
                    response != null ? response.getTotalResults() : 0);
            
            return response;

        } catch (WebClientResponseException ex) {
            log.error("Spoonacular API error during search: status={}, body={}", 
                    ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new SpoonacularApiException(
                    "Failed to search recipes: " + ex.getMessage(), 
                    ex.getStatusCode().value());
        } catch (Exception ex) {
            log.error("Unexpected error during recipe search", ex);
            throw new SpoonacularApiException(
                    "Unexpected error during recipe search: " + ex.getMessage(), 500);
        }
    }


    /**
     * Get detailed recipe information including ingredients and nutrition
     * 
     * <p>Retrieves comprehensive information about a specific recipe.
     * 
     * <p>Mode Selection:
     * <ul>
     *   <li>Mock Mode: Currently uses live API (mock not implemented for details yet)</li>
     *   <li>Live Mode: Calls actual Spoonacular API (USES 1 API REQUEST)</li>
     * </ul>
     * 
     * <p>API Endpoint: GET /recipes/{id}/information?includeNutrition=true
     * <p>Documentation: https://spoonacular.com/food-api/docs#Get-Recipe-Information
     * 
     * @param recipeId The unique identifier of the recipe
     * @return RecipeDetailResponse containing complete recipe information
     * @throws RecipeNotFoundException if the recipe ID doesn't exist (404 response)
     * @throws SpoonacularApiException for other API errors
     */
    public RecipeDetailResponse getRecipeDetails(Long recipeId) {
        log.info("Fetching recipe details for ID: {} [mockMode={}]", recipeId, mockMode);

        // TODO: Implement mock mode for recipe details if needed
        if (mockMode) {
            log.warn("Mock mode enabled but recipe details mock not implemented - using live API");
        }

        try {
            RecipeDetailResponse response = spoonacularWebClient
                    .get()
                    .uri("/recipes/{id}/information?includeNutrition=true", recipeId)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            clientResponse -> Mono.error(new RecipeNotFoundException(recipeId))
                    )
                    .bodyToMono(RecipeDetailResponse.class)
                    .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(1)))
                    .block();

            // Extract calorie information from nutrients
            if (response != null && response.getNutrition() != null) {
                extractTotalCalories(response);
            }

            log.info("Recipe details fetched successfully for ID: {}", recipeId);
            return response;

        } catch (RecipeNotFoundException ex) {
            throw ex;
        } catch (WebClientResponseException ex) {
            log.error("Spoonacular API error fetching recipe {}: status={}, body={}", 
                    recipeId, ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new SpoonacularApiException(
                    "Failed to fetch recipe details: " + ex.getMessage(), 
                    ex.getStatusCode().value());
        } catch (Exception ex) {
            log.error("Unexpected error fetching recipe details for ID: {}", recipeId, ex);
            throw new SpoonacularApiException(
                    "Unexpected error fetching recipe: " + ex.getMessage(), 500);
        }
    }

    /**
     * Extract total calories from nutrition nutrients list
     * 
     * @param recipe The recipe response to process (modified in-place)
     */
    private void extractTotalCalories(RecipeDetailResponse recipe) {
        if (recipe.getNutrition() == null || recipe.getNutrition().getNutrients() == null) {
            return;
        }

        recipe.getNutrition().getNutrients().stream()
                .filter(nutrient -> "Calories".equalsIgnoreCase(nutrient.getName()))
                .findFirst()
                .ifPresent(calorieNutrient -> 
                        recipe.getNutrition().setTotalCalories(calorieNutrient.getAmount()));
    }

    /**
     * Get ingredient information with nutrition data
     * 
     * <p>Mode Selection:
     * <ul>
     *   <li>Mock Mode: Currently uses live API (mock not implemented for ingredients yet)</li>
     *   <li>Live Mode: Calls actual Spoonacular API (USES 1 API REQUEST)</li>
     * </ul>
     * 
     * <p>API Endpoint: GET /food/ingredients/{id}/information?amount={amount}&unit={unit}
     * <p>Documentation: https://spoonacular.com/food-api/docs#Get-Ingredient-Information
     * 
     * @param ingredientId The unique identifier of the ingredient
     * @param amount The quantity of the ingredient
     * @param unit The measurement unit (e.g., "g", "cup", "tbsp")
     * @return Ingredient object with nutrition data, or null if unavailable
     */
    public Ingredient getIngredientInformation(Long ingredientId, Double amount, String unit) {
        log.info("Fetching ingredient information for ID: {}, amount: {} {} [mockMode={}]", 
                ingredientId, amount, unit, mockMode);

        // TODO: Implement mock mode for ingredients if needed
        if (mockMode) {
            log.warn("Mock mode enabled but ingredient info mock not implemented - using live API");
        }

        try {
            String uri = String.format("/food/ingredients/%d/information?amount=%.2f&unit=%s", 
                    ingredientId, amount, unit != null ? unit : "serving");
                    
            Ingredient ingredient = spoonacularWebClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(Ingredient.class)
                    .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(1)))
                    .block();

            log.info("Ingredient information fetched successfully for ID: {}", ingredientId);
            return ingredient;

        } catch (WebClientResponseException ex) {
            log.error("Spoonacular API error fetching ingredient {}: status={}, body={}", 
                    ingredientId, ex.getStatusCode(), ex.getResponseBodyAsString());
            return null; // Return null if ingredient info not available
        } catch (Exception ex) {
            log.error("Unexpected error fetching ingredient info for ID: {}", ingredientId, ex);
            return null;
        }
    }

}
