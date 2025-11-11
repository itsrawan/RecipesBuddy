import { useState, useEffect } from 'react';
import { Search, X } from 'lucide-react';

/**
 * SearchBar Component
 * 
 * Provides a comprehensive search interface for recipes with multiple filters:
 * - Recipe name search
 * - Ingredient inclusion/exclusion
 * - Nutrition filters (calories, carbs, protein, fat)
 * 
 * Features:
 * - Persists search state via initialValues prop
 * - Clear button to reset all filters
 * - Real-time validation and disabled states
 * 
 * @param {Function} onSearch - Callback when search is submitted
 * @param {boolean} isLoading - Loading state for search button
 * @param {Object} initialValues - Pre-populated form values from cache
 * 
 * @author Rawan Sweidan
 * @since 2025-11-10
 */
const SearchBar = ({ onSearch, isLoading, initialValues = {} }) => {
  const [query, setQuery] = useState(initialValues.query || '');
  const [excludeInput, setExcludeInput] = useState('');
  const [excludedIngredients, setExcludedIngredients] = useState(initialValues.excludeIngredients || []);
  const [includeInput, setIncludeInput] = useState('');
  const [includedIngredients, setIncludedIngredients] = useState(
    initialValues.includeIngredients ? initialValues.includeIngredients.split(',').filter(Boolean) : []
  );
  const [maxCalories, setMaxCalories] = useState(initialValues.maxCalories?.toString() || '');
  const [maxCarbs, setMaxCarbs] = useState(initialValues.maxCarbs?.toString() || '');
  const [minProtein, setMinProtein] = useState(initialValues.minProtein?.toString() || '');
  const [maxFat, setMaxFat] = useState(initialValues.maxFat?.toString() || '');

  // Update form when initialValues change (e.g., when navigating back)
  useEffect(() => {
    if (initialValues.query) {
      setQuery(initialValues.query);
      setExcludedIngredients(initialValues.excludeIngredients || []);
      setIncludedIngredients(
        initialValues.includeIngredients ? initialValues.includeIngredients.split(',').filter(Boolean) : []
      );
      setMaxCalories(initialValues.maxCalories?.toString() || '');
      setMaxCarbs(initialValues.maxCarbs?.toString() || '');
      setMinProtein(initialValues.minProtein?.toString() || '');
      setMaxFat(initialValues.maxFat?.toString() || '');
    }
  }, [initialValues.query, initialValues.excludeIngredients, initialValues.includeIngredients, 
      initialValues.maxCalories, initialValues.maxCarbs, initialValues.minProtein, initialValues.maxFat]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (query.trim()) {
      onSearch({
        query: query.trim(),
        excludeIngredients: excludedIngredients,
        includeIngredients: includedIngredients.join(','),
        maxCalories: maxCalories ? parseInt(maxCalories) : null,
        maxCarbs: maxCarbs ? parseInt(maxCarbs) : null,
        minProtein: minProtein ? parseInt(minProtein) : null,
        maxFat: maxFat ? parseInt(maxFat) : null,
      });
    }
  };

  const handleAddExclusion = (e) => {
    if (e.key === 'Enter' && excludeInput.trim()) {
      e.preventDefault();
      const ingredient = excludeInput.trim().toLowerCase();
      if (!excludedIngredients.includes(ingredient)) {
        setExcludedIngredients([...excludedIngredients, ingredient]);
      }
      setExcludeInput('');
    }
  };

  const removeExclusion = (ingredient) => {
    setExcludedIngredients(excludedIngredients.filter(i => i !== ingredient));
  };

  const handleAddInclusion = (e) => {
    if (e.key === 'Enter' && includeInput.trim()) {
      e.preventDefault();
      const ingredient = includeInput.trim().toLowerCase();
      if (!includedIngredients.includes(ingredient)) {
        setIncludedIngredients([...includedIngredients, ingredient]);
      }
      setIncludeInput('');
    }
  };

  const removeInclusion = (ingredient) => {
    setIncludedIngredients(includedIngredients.filter(i => i !== ingredient));
  };

  const clearAll = () => {
    setQuery('');
    setExcludedIngredients([]);
    setExcludeInput('');
    setIncludedIngredients([]);
    setIncludeInput('');
    setMaxCalories('');
    setMaxCarbs('');
    setMinProtein('');
    setMaxFat('');
  };

  return (
    <div className="w-full max-w-6xl mx-auto p-4">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="relative">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search for recipes... (e.g., pasta, chicken, salad)"
            className="w-full px-4 py-3 pr-12 text-lg border-2 border-primary-300 rounded-xl focus:outline-none focus:border-primary-500 transition-colors"
            disabled={isLoading}
            data-testid="SearchText"
          />
          <Search className="absolute right-4 top-1/2 transform -translate-y-1/2 text-primary-500" size={22} />
        </div>

        <div className="space-y-2">
          <label className="text-sm font-medium text-gray-700 ">
            Include Ingredients (press Enter to add)
          </label>
          <input
            type="text"
            value={includeInput}
            onChange={(e) => setIncludeInput(e.target.value)}
            onKeyDown={handleAddInclusion}
            placeholder="e.g., chicken, tomato, basil"
            className="w-full px-4 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:border-primary-500"
            disabled={isLoading}
          />
          
          {includedIngredients.length > 0 && (
            <div className="flex flex-wrap gap-2 mt-2">
              {includedIngredients.map((ingredient) => (
                <span
                  key={ingredient}
                  className="inline-flex items-center gap-1 px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm"
                >
                  {ingredient}
                  <button
                    type="button"
                    onClick={() => removeInclusion(ingredient)}
                    className="hover:text-green-900"
                  >
                    <X size={14} />
                  </button>
                </span>
              ))}
            </div>
          )}
        </div>

        <div className="space-y-2">
          <label className="text-sm font-medium text-gray-700">
            Exclude Ingredients (press Enter to add)
          </label>
          <input
            type="text"
            value={excludeInput}
            onChange={(e) => setExcludeInput(e.target.value)}
            onKeyDown={handleAddExclusion}
            placeholder="e.g., cheese, nuts, dairy"
            className="w-full px-4 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:border-primary-500"
            disabled={isLoading}
          />
          
          {excludedIngredients.length > 0 && (
            <div className="flex flex-wrap gap-2 mt-2">
              {excludedIngredients.map((ingredient) => (
                <span
                  key={ingredient}
                  className="inline-flex items-center gap-1 px-3 py-1 bg-red-100 text-red-700 rounded-full text-sm"
                >
                  {ingredient}
                  <button
                    type="button"
                    onClick={() => removeExclusion(ingredient)}
                    className="hover:text-red-900"
                  >
                    <X size={14} />
                  </button>
                </span>
              ))}
            </div>
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-700">
              Max Calories
            </label>
            <input
              type="number"
              value={maxCalories}
              onChange={(e) => setMaxCalories(e.target.value)}
              placeholder="0-5000"
              min="0"
              max="5000"
              className="w-full px-4 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:border-primary-500"
              disabled={isLoading}
            />
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-700">
              Max Carbs (g)
            </label>
            <input
              type="number"
              value={maxCarbs}
              onChange={(e) => setMaxCarbs(e.target.value)}
              placeholder="0-5000"
              min="0"
              max="5000"
              className="w-full px-4 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:border-primary-500"
              disabled={isLoading}
            />
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-700">
              Min Protein (g)
            </label>
            <input
              type="number"
              value={minProtein}
              onChange={(e) => setMinProtein(e.target.value)}
              placeholder="0-5000"
              min="0"
              max="5000"
              className="w-full px-4 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:border-primary-500"
              disabled={isLoading}
            />
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-700">
              Max Fat (g)
            </label>
            <input
              type="number"
              value={maxFat}
              onChange={(e) => setMaxFat(e.target.value)}
              placeholder="0-5000"
              min="0"
              max="5000"
              className="w-full px-4 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:border-primary-500"
              disabled={isLoading}
            />
          </div>
        </div>

        <div className="flex gap-2">
          <button
            type="submit"
            disabled={isLoading || !query.trim()}
            data-testid="SearchButton"
            className="flex-1 bg-primary-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
          >
            {isLoading ? 'Searching...' : 'Search Recipes'}
          </button>
          
          {(query || excludedIngredients.length > 0 || includedIngredients.length > 0 || maxCalories || maxCarbs || minProtein || maxFat) && (
            <button
              type="button"
              onClick={clearAll}
              className="px-6 py-3 border-2 border-gray-300 rounded-lg font-medium hover:bg-gray-50 transition-colors"
            >
              Clear
            </button>
          )}
        </div>
      </form>
    </div>
  );
};

export default SearchBar;
