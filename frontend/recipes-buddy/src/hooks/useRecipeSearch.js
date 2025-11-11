import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { recipeApi } from '../api/recipeApi';

const SEARCH_CACHE_KEY = 'recipesBuddy_lastSearch';

/**
 * useRecipeSearch Hook
 * 
 * Custom hook for managing recipe search state and API calls
 * 
 * Features:
 * - Persists search params in sessionStorage
 * - Restores previous search on page reload
 * - Integrates with React Query for caching
 * - Provides pagination controls
 * 
 * @returns {Object} Search state and control functions
 * @property {Object} searchParams - Current search parameters
 * @property {Array} results - Recipe search results
 * @property {number} totalResults - Total number of matching recipes
 * @property {boolean} isLoading - Loading state
 * @property {Error} error - Error object if request failed
 * @property {Function} updateSearch - Update search parameters
 * @property {Function} nextPage - Navigate to next page
 * @property {Function} prevPage - Navigate to previous page
 * @property {boolean} hasNextPage - Whether next page exists
 * @property {boolean} hasPrevPage - Whether previous page exists
 * @property {number} currentPage - Current page number
 * @property {number} totalPages - Total number of pages
 * 
 * @author Rawan Sweidan
 * @since 2025-11-10
 */
export const useRecipeSearch = () => {
  // Initialize state from sessionStorage if available
  const [searchParams, setSearchParams] = useState(() => {
    const cached = sessionStorage.getItem(SEARCH_CACHE_KEY);
    if (cached) {
      try {
        return JSON.parse(cached);
      } catch (e) {
        console.error('Failed to parse cached search params:', e);
      }
    }
    return {
      query: '',
      excludeIngredients: [],
      size: 12,
      offset: 0,
    };
  });

  // Save search params to sessionStorage whenever they change
  useEffect(() => {
    if (searchParams.query) {
      sessionStorage.setItem(SEARCH_CACHE_KEY, JSON.stringify(searchParams));
    }
  }, [searchParams]);

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['recipes', searchParams],
    queryFn: () => recipeApi.searchRecipes(searchParams),
    enabled: !!searchParams.query,
  });

  const updateSearch = (newParams) => {
    setSearchParams((prev) => ({
      ...prev,
      ...newParams,
      offset: 0,
    }));
  };

  const nextPage = () => {
    setSearchParams((prev) => ({
      ...prev,
      offset: prev.offset + prev.size,
    }));
  };

  const prevPage = () => {
    setSearchParams((prev) => ({
      ...prev,
      offset: Math.max(0, prev.offset - prev.size),
    }));
  };

  const currentPage = Math.floor(searchParams.offset / searchParams.size) + 1;
  const totalPages = data?.totalResults 
    ? Math.ceil(data.totalResults / searchParams.size) 
    : 0;

  return {
    searchParams,
    results: data?.results || [],
    totalResults: data?.totalResults || 0,
    isLoading,
    error,
    updateSearch,
    nextPage,
    prevPage,
    hasNextPage: data?.totalResults > searchParams.offset + searchParams.size,
    hasPrevPage: searchParams.offset > 0,
    currentPage,
    totalPages,
    refetch,
  };
};
