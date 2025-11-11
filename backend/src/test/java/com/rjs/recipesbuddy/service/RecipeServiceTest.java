package com.rjs.recipesbuddy.service;

import com.rjs.recipesbuddy.dto.*;
import com.rjs.recipesbuddy.exception.RecipeNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecipeService
 * 
 * <p>These tests verify the business logic in RecipeService using mocked dependencies.
 * The SpoonacularApiService is mocked to avoid making actual API calls during testing.
 * 
 * <p>Test Coverage:
 * <ul>
 *   <li>Recipe search with various parameters</li>
 *   <li>Recipe retrieval by ID (success and not found cases)</li>
 *   <li>Calorie calculations with ingredient exclusions</li>
 *   <li>Edge cases and error scenarios</li>
 * </ul>
 * 
 * @author Rawan Sweidan
 * @version 1.0.0
 * @since 2025-11-10
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeService Tests")
class RecipeServiceTest {
    
    @Mock
    private SpoonacularApiService spoonacularApiService;

    @InjectMocks
    private RecipeService recipeService;

    private RecipeDetailResponse mockRecipe;
    private Ingredient ingredient1;
    private Ingredient ingredient2;

    @BeforeEach
    void setUp() {
        // Create mock nutrition data
        Nutrient calorieNutrient1 = Nutrient.builder()
                .name("Calories")
                .amount(200.0)
                .unit("kcal")
                .build();

        Nutrient calorieNutrient2 = Nutrient.builder()
                .name("Calories")
                .amount(150.0)
                .unit("kcal")
                .build();
        
        Nutrient calorieNutrient3 = Nutrient.builder()
                .name("Calories")
                .amount(350.0)
                .unit("kcal")
                .build();

        Nutrient carbsNutrient3 = Nutrient.builder()
                .name("Carbohydrates")
                .amount(60.0)
                .unit("g")
                .build();
        
        Nutrient proteinNutrient3 = Nutrient.builder()
                .name("protein")
                .amount(20.0)
                .unit("g")
                .build();

        NutritionInfo nutrition1 = NutritionInfo.builder()
                .nutrients(List.of(calorieNutrient1))
                .totalCalories(200.0)
                .build();

        NutritionInfo nutrition2 = NutritionInfo.builder()
                .nutrients(List.of(calorieNutrient2))
                .totalCalories(150.0)
                .build();
        
        NutritionInfo nutrition3 = NutritionInfo.builder()
                .nutrients(List.of(calorieNutrient3, carbsNutrient3, proteinNutrient3))
                .totalCalories(350.0)
                .build();

        // Create mock ingredients
        ingredient1 = Ingredient.builder()
                .id(1L)
                .name("Cheese")
                .amount(100.0)
                .unit("g")
                .nutrition(nutrition1)
                .build();

        ingredient2 = Ingredient.builder()
                .id(2L)
                .name("Butter")
                .amount(50.0)
                .unit("g")
                .nutrition(nutrition2)
                .build();
        
        // Create mock recipe
        mockRecipe = RecipeDetailResponse.builder()
                .id(123L)
                .title("Test Recipe")
                .nutrition(nutrition3)
                .extendedIngredients(Arrays.asList(ingredient1, ingredient2))
                .build();
    }

    @Nested
    @DisplayName("Search Recipes Tests")
    class SearchRecipesTests {
        
        @Test
        @DisplayName("Should successfully search recipes with basic query")
        void testSearchRecipes_Basic() {
            // Arrange
            RecipeSearchRequest request = RecipeSearchRequest.builder()
                    .query("pasta")
                    .size(10)
                    .offset(0)
                    .build();

            RecipeSearchResponse expectedResponse = RecipeSearchResponse.builder()
                    .results(List.of())
                    .totalResults(0)
                    .build();

            when(spoonacularApiService.searchRecipes(
                    anyString(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(expectedResponse);

            // Act
            RecipeSearchResponse actualResponse = recipeService.searchRecipes(request);

            // Assert
            assertNotNull(actualResponse);
            assertEquals(0, actualResponse.getTotalResults());
            verify(spoonacularApiService).searchRecipes(
                    eq("pasta"),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    eq(10),
                    eq(0)
            );
        }
        
        @Test
        @DisplayName("Should search recipes with nutrition filters")
        void testSearchRecipes_WithNutritionFilters() {
            // Arrange
            RecipeSearchRequest request = RecipeSearchRequest.builder()
                    .query("chicken")
                    .maxCalories(500)
                    .maxCarbs(50)
                    .minProtein(30)
                    .maxFat(20)
                    .size(12)
                    .offset(0)
                    .build();

            RecipeSearchResponse expectedResponse = RecipeSearchResponse.builder()
                    .results(List.of())
                    .totalResults(5)
                    .build();

            when(spoonacularApiService.searchRecipes(
                    anyString(), any(), any(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt()))
                    .thenReturn(expectedResponse);

            // Act
            RecipeSearchResponse actualResponse = recipeService.searchRecipes(request);

            // Assert
            assertNotNull(actualResponse);
            assertEquals(5, actualResponse.getTotalResults());
            verify(spoonacularApiService).searchRecipes(
                    eq("chicken"),
                    any(),
                    any(),
                    eq(500),
                    eq(50),
                    eq(30),
                    eq(20),
                    eq(12),
                    eq(0)
            );
        }
        
        @Test
        @DisplayName("Should search recipes with ingredient exclusions")
        void testSearchRecipes_WithExclusions() {
            // Arrange
            List<String> exclusions = Arrays.asList("dairy", "nuts");
            RecipeSearchRequest request = RecipeSearchRequest.builder()
                    .query("salad")
                    .excludeIngredients(exclusions)
                    .size(10)
                    .offset(0)
                    .build();

            RecipeSearchResponse expectedResponse = RecipeSearchResponse.builder()
                    .results(List.of())
                    .totalResults(3)
                    .build();

            when(spoonacularApiService.searchRecipes(
                    anyString(), anyList(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(expectedResponse);

            // Act
            RecipeSearchResponse actualResponse = recipeService.searchRecipes(request);

            // Assert
            assertNotNull(actualResponse);
            verify(spoonacularApiService).searchRecipes(
                    eq("salad"),
                    eq(exclusions),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    eq(10),
                    eq(0)
            );
        }
    }
    
    @Nested
    @DisplayName("Get Recipe By ID Tests")
    class GetRecipeByIdTests {
        
        @Test
        @DisplayName("Should successfully retrieve recipe by ID")
        void testGetRecipeById_Success() {
            // Arrange
            Long recipeId = 123L;
            when(spoonacularApiService.getRecipeDetails(recipeId))
                    .thenReturn(mockRecipe);

            // Act
            RecipeDetailResponse result = recipeService.getRecipeById(recipeId);

            // Assert
            assertNotNull(result);
            assertEquals(recipeId, result.getId());
            assertEquals("Test Recipe", result.getTitle());
            verify(spoonacularApiService).getRecipeDetails(recipeId);
        }

        @Test
        @DisplayName("Should throw RecipeNotFoundException when recipe not found")
        void testGetRecipeById_NotFound() {
            // Arrange
            Long recipeId = 999L;
            when(spoonacularApiService.getRecipeDetails(recipeId))
                    .thenReturn(null);

            // Act & Assert
            RecipeNotFoundException exception = assertThrows(
                RecipeNotFoundException.class,
                () -> recipeService.getRecipeById(recipeId)
            );
            
            assertTrue(exception.getMessage().contains("999"));
            verify(spoonacularApiService).getRecipeDetails(recipeId);
        }
    }
    
    @Nested
    @DisplayName("Calculate Updated Calories Tests")
    class CalculateUpdatedCaloriesTests {
        
        @Test
        @DisplayName("Should calculate updated calories when excluding single ingredient")
        void testCalculateUpdatedCalories_SingleExclusion() {
            // Arrange
            Long recipeId = 123L;
            List<Long> excludedIds = List.of(1L); // Exclude cheese (200 cal)

            when(spoonacularApiService.getRecipeDetails(recipeId))
                    .thenReturn(mockRecipe);
            
            when(spoonacularApiService.getIngredientInformation(eq(1L), anyDouble(), anyString()))
                    .thenReturn(ingredient1);

            // Act
            CalorieUpdateResponse response = recipeService.calculateUpdatedCalories(
                    recipeId,
                    excludedIds
            );

            // Assert
            assertNotNull(response);
            assertEquals(recipeId, response.getRecipeId());
            assertEquals(350.0, response.getOriginalCalories());
            assertEquals(150.0, response.getUpdatedCalories());
            assertEquals(200.0, response.getCaloriesReduced());
            assertEquals(1, response.getIngredientsExcluded());
            
            verify(spoonacularApiService).getRecipeDetails(recipeId);
            verify(spoonacularApiService).getIngredientInformation(eq(1L), anyDouble(), anyString());
        }

        @Test
        @DisplayName("Should calculate updated calories when excluding multiple ingredients")
        void testCalculateUpdatedCalories_MultipleExclusions() {
            // Arrange
            Long recipeId = 123L;
            List<Long> excludedIds = Arrays.asList(1L, 2L);

            when(spoonacularApiService.getRecipeDetails(recipeId))
                    .thenReturn(mockRecipe);
            
            when(spoonacularApiService.getIngredientInformation(eq(1L), anyDouble(), anyString()))
                    .thenReturn(ingredient1);
            when(spoonacularApiService.getIngredientInformation(eq(2L), anyDouble(), anyString()))
                    .thenReturn(ingredient2);

            // Act
            CalorieUpdateResponse response = recipeService.calculateUpdatedCalories(
                    recipeId,
                    excludedIds
            );

            // Assert
            assertEquals(350.0, response.getOriginalCalories());
            assertEquals(0.0, response.getUpdatedCalories());
            assertEquals(350.0, response.getCaloriesReduced());
            assertEquals(2, response.getIngredientsExcluded());
            
            verify(spoonacularApiService, times(2)).getIngredientInformation(anyLong(), anyDouble(), anyString());
        }
        
        @Test
        @DisplayName("Should handle excluding no ingredients")
        void testCalculateUpdatedCalories_NoExclusions() {
            // Arrange
            Long recipeId = 123L;
            List<Long> excludedIds = Collections.emptyList();

            when(spoonacularApiService.getRecipeDetails(recipeId))
                    .thenReturn(mockRecipe);

            // Act
            CalorieUpdateResponse response = recipeService.calculateUpdatedCalories(
                    recipeId,
                    excludedIds
            );

            // Assert
            assertEquals(350.0, response.getOriginalCalories());
            assertEquals(350.0, response.getUpdatedCalories());
            assertEquals(0.0, response.getCaloriesReduced());
            assertEquals(0, response.getIngredientsExcluded());
            
            verify(spoonacularApiService, never()).getIngredientInformation(anyLong(), anyDouble(), anyString());
        }
        
        @Test
        @DisplayName("Should throw exception when recipe has no ingredients")
        void testCalculateUpdatedCalories_NoIngredients() {
            // Arrange
            Long recipeId = 123L;
            RecipeDetailResponse recipeWithoutIngredients = RecipeDetailResponse.builder()
                    .id(recipeId)
                    .title("Empty Recipe")
                    .extendedIngredients(Collections.emptyList())
                    .build();

            when(spoonacularApiService.getRecipeDetails(recipeId))
                    .thenReturn(recipeWithoutIngredients);

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> recipeService.calculateUpdatedCalories(recipeId, List.of(1L))
            );
            
            assertTrue(exception.getMessage().contains("no ingredients data"));
        }
        
        @Test
        @DisplayName("Should throw exception when recipe has no calorie information")
        void testCalculateUpdatedCalories_NoCalorieInfo() {
            // Arrange
            Long recipeId = 123L;
            RecipeDetailResponse recipeWithoutCalories = RecipeDetailResponse.builder()
                    .id(recipeId)
                    .title("Recipe Without Calories")
                    .extendedIngredients(Arrays.asList(ingredient1))
                    .nutrition(NutritionInfo.builder().totalCalories(0.0).build())
                    .build();

            when(spoonacularApiService.getRecipeDetails(recipeId))
                    .thenReturn(recipeWithoutCalories);

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> recipeService.calculateUpdatedCalories(recipeId, List.of(1L))
            );
            
            assertTrue(exception.getMessage().contains("no calorie information"));
        }
        
        @Test
        @DisplayName("Should handle ingredient API returning null")
        void testCalculateUpdatedCalories_IngredientApiReturnsNull() {
            // Arrange
            Long recipeId = 123L;
            List<Long> excludedIds = List.of(1L);

            when(spoonacularApiService.getRecipeDetails(recipeId))
                    .thenReturn(mockRecipe);
            
            // Simulate ingredient API returning null
            when(spoonacularApiService.getIngredientInformation(eq(1L), anyDouble(), anyString()))
                    .thenReturn(null);

            // Act
            CalorieUpdateResponse response = recipeService.calculateUpdatedCalories(
                    recipeId,
                    excludedIds
            );

            // Assert - should still work but with 0 calories excluded
            assertNotNull(response);
            assertEquals(350.0, response.getOriginalCalories());
            assertEquals(350.0, response.getUpdatedCalories());  // No reduction if ingredient info unavailable
            assertEquals(0.0, response.getCaloriesReduced());
        }
        
        @Test
        @DisplayName("Should handle excluding non-existent ingredient ID")
        void testCalculateUpdatedCalories_NonExistentIngredientId() {
            // Arrange
            Long recipeId = 123L;
            List<Long> excludedIds = List.of(999L); // Non-existent ingredient

            when(spoonacularApiService.getRecipeDetails(recipeId))
                    .thenReturn(mockRecipe);

            // Act
            CalorieUpdateResponse response = recipeService.calculateUpdatedCalories(
                    recipeId,
                    excludedIds
            );

            // Assert - should work but no calories excluded
            assertEquals(350.0, response.getOriginalCalories());
            assertEquals(350.0, response.getUpdatedCalories());
            assertEquals(0.0, response.getCaloriesReduced());
            assertEquals(1, response.getIngredientsExcluded());
            
            // Should not call ingredient API for non-existent ingredient
            verify(spoonacularApiService, never()).getIngredientInformation(anyLong(), anyDouble(), anyString());
        }
    }
}
