import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { recipeApi } from '../api/recipeApi';


/**
 * Hook: useRecipeDetail
 *
 * Encapsulates data loading and actions for the recipe detail page.
 *
 * Responsibilities:
 *  - Fetch recipe details by id using react-query.
 *  - Maintain local UI state for excluded ingredients and calculated calories.
 *  - Expose handlers for toggling exclusions, clearing exclusions, and triggering calorie calculations.
 *
 * Usage:
 *  const { recipe, isLoading, error, excludedIngredients, toggleIngredient, calculateCalories, ... } = useRecipeDetail(id);
 *
 * Notes:
 *  - Keeps the hook small and focused on UI state; network calls are delegated to recipeApi.
 *  - Uses react-query for caching and background updates.
 *
 * @author Rawan Sweidan
 * @since 2025-11-10
 */

export const useRecipeDetail = (recipeId) => {
  const [excludedIngredients, setExcludedIngredients] = useState([]);
  const [caloriesData, setCaloriesData] = useState(null);

  const { data: recipe, isLoading, error } = useQuery({
    queryKey: ['recipe', recipeId],
    queryFn: () => recipeApi.getRecipeById(recipeId),
    enabled: !!recipeId,
  });

  const { mutate: calculateCalories, isLoading: isCalculating } = useMutation({
    mutationFn: (excludedIds) => 
      recipeApi.calculateUpdatedCalories(recipeId, excludedIds),
    onSuccess: (data) => {
      setCaloriesData(data);
    },
  });

  const toggleIngredient = (ingredientId) => {
    setExcludedIngredients((prev) =>
      prev.includes(ingredientId)
        ? prev.filter((id) => id !== ingredientId)
        : [...prev, ingredientId]
    );
    setCaloriesData(null); // Reset calculation
  };

  const clearExclusions = () => {
    setExcludedIngredients([]);
    setCaloriesData(null);
  };

  const handleCalculateCalories = () => {
    if (excludedIngredients.length > 0) {
      calculateCalories(excludedIngredients);
    }
  };

  const originalCalories = recipe?.nutrition?.totalCalories || 0;

  return {
    recipe,
    isLoading,
    error,
    excludedIngredients,
    toggleIngredient,
    calculateCalories: handleCalculateCalories,
    clearExclusions,
    caloriesData,
    isCalculating,
    originalCalories,
    hasExclusions: excludedIngredients.length > 0,
  };
};