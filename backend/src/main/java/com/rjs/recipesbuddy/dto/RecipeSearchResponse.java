package com.rjs.recipesbuddy.dto;

import java.util.List;

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
public class RecipeSearchResponse {
    private List<RecipeSummary> results;
    private Integer offset;
    private Integer number;
    private Integer totalResults;
}
