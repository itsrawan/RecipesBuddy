package com.rjs.recipesbuddy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeSearchRequest {
    
    @NotBlank(message = "Search query cannot be empty")
    private String query;
    
    private List<String> excludeIngredients;
    
    private String includeIngredients;
    
    @Min(value = 0, message = "Max calories cannot be negative")
    @Max(value = 5000, message = "Max calories cannot exceed 5000")
    private Integer maxCalories;
    
    @Min(value = 0, message = "Max carbs cannot be negative")
    @Max(value = 5000, message = "Max carbs cannot exceed 5000")
    private Integer maxCarbs;
    
    @Min(value = 0, message = "Min protein cannot be negative")
    @Max(value = 5000, message = "Min protein cannot exceed 5000")
    private Integer minProtein;
    
    @Min(value = 0, message = "Max fat cannot be negative")
    @Max(value = 5000, message = "Max fat cannot exceed 5000")
    private Integer maxFat;
    
    @Min(value = 1, message = "Number must be at least 1")
    @lombok.Builder.Default
    private Integer size = 12;
    
    @Min(value = 0, message = "Offset cannot be negative")
    @lombok.Builder.Default
    private Integer offset = 0;
}
