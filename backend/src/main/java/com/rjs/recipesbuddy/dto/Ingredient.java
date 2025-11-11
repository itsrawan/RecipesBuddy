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
public class Ingredient {
    private Long id;
    private String name;
    private String original;
    private Double amount;
    private String unit;
    private String image;
    private NutritionInfo nutrition;
}
