package com.rjs.recipesbuddy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecipeDetailResponse {
    private Long id;
    private String title;
    private String image;
    private Integer servings;
    private Integer readyInMinutes;
    private String summary;
    private List<Ingredient> extendedIngredients;
    private String instructions;
    private NutritionInfo nutrition;
    private boolean vegetarian;
    private boolean vegan;
    private boolean glutenFree;
    private boolean dairyFree;
}
