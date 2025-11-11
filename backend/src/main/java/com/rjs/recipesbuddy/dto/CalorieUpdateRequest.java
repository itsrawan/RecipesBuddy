package com.rjs.recipesbuddy.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalorieUpdateRequest {
    
    @NotNull(message = "Recipe ID cannot be null")
    private Long recipeId;
    
    @NotEmpty(message = "Excluded ingredient IDs cannot be empty")
    private List<Long> excludedIngredientIds;
}