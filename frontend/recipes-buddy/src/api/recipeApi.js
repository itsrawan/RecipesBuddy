import axios from 'axios';

/**
 * Recipe API Module
 * 
 * Handles all HTTP communication with the Spring Boot backend
 * 
 * Base URL: http://localhost:8080/api/recipes
 * 
 * Features:
 * - Recipe search with filters
 * - Recipe details retrieval
 * - Calorie calculation with ingredient exclusions
 * 
 * @author Rawan Sweidan
 * @since 2025-11-10
 */

const API_BASE_URL = 'http://localhost:8080/api/recipes';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const recipeApi = {
  /**
   * Search for recipes with optional filters
   * 
   * @param {Object} params - Search parameters
   * @param {string} params.query - Recipe name or keyword
   * @param {Array<string>} params.excludeIngredients - Ingredients to exclude
   * @param {string} params.includeIngredients - Ingredients that must be included
   * @param {number} params.maxCalories - Maximum calories per serving
   * @param {number} params.maxCarbs - Maximum carbs in grams
   * @param {number} params.minProtein - Minimum protein in grams
   * @param {number} params.maxFat - Maximum fat in grams
   * @param {number} params.size - Number of results per page (default: 12)
   * @param {number} params.offset - Pagination offset (default: 0)
   * @returns {Promise<Object>} Search results with recipes array and totalResults
   */
  searchRecipes: async ({
    query, 
    excludeIngredients = [], 
    includeIngredients = '', 
    maxCalories = null,
    maxCarbs = null,
    minProtein = null,
    maxFat = null,
    size = 12, 
    offset = 0 
  }) => {
    const params = {
      query,
      size,
      offset,
    };
    
    if (excludeIngredients.length > 0) {
      params.excludeIngredients = excludeIngredients;
    }

    if (includeIngredients && includeIngredients.trim()) {
      params.includeIngredients = includeIngredients.trim();
    }

    if (maxCalories !== null && maxCalories > 0) {
      params.maxCalories = maxCalories;
    }

    if (maxCarbs !== null && maxCarbs > 0) {
      params.maxCarbs = maxCarbs;
    }

    if (minProtein !== null && minProtein > 0) {
      params.minProtein = minProtein;
    }

    if (maxFat !== null && maxFat > 0) {
      params.maxFat = maxFat;
    }
    
    const response = await api.get('/search', { params });
    return response.data;
  },

  // Get recipe details by ID
  getRecipeById: async (recipeId) => {
    const response = await api.get(`/${recipeId}`);
    return response.data;
  },

  // Calculate updated calories after excluding ingredients
  calculateUpdatedCalories: async (recipeId, excludedIngredientIds) => {
    const response = await api.post(`/${recipeId}/calories`, {
      recipeId,
      excludedIngredientIds,
    });
    return response.data;
  },
};
