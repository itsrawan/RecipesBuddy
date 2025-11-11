package com.rjs.recipesbuddy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjs.recipesbuddy.dto.*;
import com.rjs.recipesbuddy.exception.RecipeNotFoundException;
import com.rjs.recipesbuddy.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecipeController.class)
public class RecipeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecipeService recipeService;

    @Test
    void testSearchRecipes_Success() throws Exception {
        // Arrange
        RecipeSearchResponse response = RecipeSearchResponse.builder()
                .results(List.of())
                .totalResults(0)
                .build();

        when(recipeService.searchRecipes(any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/recipes/search")
                        .param("query", "pasta")
                        .param("number", "10")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").value(0));
    }

    @Test
    void testGetRecipeById_Success() throws Exception {
        // Arrange
        Long recipeId = 123L;
        RecipeDetailResponse recipe = RecipeDetailResponse.builder()
                .id(recipeId)
                .title("Test Recipe")
                .build();

        when(recipeService.getRecipeById(recipeId)).thenReturn(recipe);

        // Act & Assert
        mockMvc.perform(get("/api/recipes/{id}", recipeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(recipeId))
                .andExpect(jsonPath("$.title").value("Test Recipe"));
    }

    @Test
    void testGetRecipeById_NotFound() throws Exception {
        // Arrange
        Long recipeId = 999L;
        when(recipeService.getRecipeById(recipeId))
                .thenThrow(new RecipeNotFoundException(recipeId));

        // Act & Assert
        mockMvc.perform(get("/api/recipes/{id}", recipeId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testCalculateUpdatedCalories_Success() throws Exception {
        // Arrange
        Long recipeId = 123L;
        CalorieUpdateRequest request = CalorieUpdateRequest.builder()
                .recipeId(recipeId)
                .excludedIngredientIds(List.of(1L, 2L))
                .build();

        CalorieUpdateResponse response = CalorieUpdateResponse.builder()
                .recipeId(recipeId)
                .originalCalories(500.0)
                .updatedCalories(300.0)
                .caloriesReduced(200.0)
                .ingredientsExcluded(2)
                .build();

        when(recipeService.calculateUpdatedCalories(eq(recipeId), anyList()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/recipes/{id}/calories", recipeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalCalories").value(500.0))
                .andExpect(jsonPath("$.updatedCalories").value(300.0))
                .andExpect(jsonPath("$.caloriesReduced").value(200.0));
    }
}
