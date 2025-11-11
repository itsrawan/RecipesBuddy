package com.rjs.recipesbuddy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor 
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecipeSummary {

    private Long id;
    private String title;
    private String image;
    private Integer readyInMinutes;
    private Integer servings;
    private String imageType;
    private NutritionInfo nutrition;
}
