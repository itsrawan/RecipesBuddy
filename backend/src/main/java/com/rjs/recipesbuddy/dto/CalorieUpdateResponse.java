package com.rjs.recipesbuddy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalorieUpdateResponse {
    private Long recipeId;
    private Double originalCalories;
    private Double updatedCalories;
    private Double caloriesReduced;
    private Integer ingredientsExcluded;
}
