import { useState, useEffect } from 'react';
import SearchBar from '../components/SearchBar';
import RecipeList from '../components/RecipeList';
import { useRecipeSearch } from '../hooks/useRecipeSearch';
import { ChefHat } from 'lucide-react';

/**
 * HomePage Component
 * 
 * Main landing page that displays:
 * - Recipe search interface
 * - Search results grid
 * - Pagination controls
 * 
 * Features:
 * - Persists search state using sessionStorage
 * - Restores previous search when navigating back
 * - Displays welcome message when no search performed
 * - Responsive layout with TailwindCSS
 * 
 * @author Rawan Sweidan
 * @since 2025-11-10
 */
const HomePage = () => {
  const {
    searchParams,
    results,
    totalResults,
    isLoading,
    error,
    updateSearch,
    nextPage,
    prevPage,
    hasNextPage,
    hasPrevPage,
    currentPage,
    totalPages,
  } = useRecipeSearch();

  const [hasSearched, setHasSearched] = useState(false);

  useEffect(() => {
    if (searchParams.query) {
      setHasSearched(true);
    }
  }, [searchParams.query]);

  const handleSearch = (params) => {
    updateSearch(params);
    setHasSearched(true);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 py-6">
          <div className="flex items-center gap-3">
            <ChefHat size={32} className="text-primary-600" />
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Recipe Finder</h1>
              <p className="text-gray-600">Discover delicious recipes and manage your calories</p>
            </div>
          </div>
        </div>
      </header>

      {/* Search Section */}
      <div className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto">
          <SearchBar 
            onSearch={handleSearch} 
            isLoading={isLoading}
            initialValues={searchParams}
          />
        </div>
      </div>

      {/* Results Section */}
      <div className="py-8">
        {!hasSearched ? (
          <div className="max-w-4xl mx-auto px-4 text-center">
            <ChefHat size={64} className="text-gray-300 mx-auto mb-4" />
            <h2 className="text-2xl font-semibold text-gray-700 mb-2">
              Start Your Culinary Journey
            </h2>
            <p className="text-gray-600 mb-8">
              Search for recipes and customize them by excluding ingredients
            </p>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 text-left max-w-3xl mx-auto">
              <div className="bg-white p-6 rounded-lg shadow-md">
                <h3 className="font-semibold text-gray-800 mb-2">🔍 Search Recipes</h3>
                <p className="text-sm text-gray-600">
                  Find recipes by name, ingredients, or cuisine type
                </p>
              </div>
              <div className="bg-white p-6 rounded-lg shadow-md">
                <h3 className="font-semibold text-gray-800 mb-2">🚫 Exclude Ingredients</h3>
                <p className="text-sm text-gray-600">
                  Filter out allergens or ingredients you don't like
                </p>
              </div>
              <div className="bg-white p-6 rounded-lg shadow-md">
                <h3 className="font-semibold text-gray-800 mb-2">🔥 Track Calories</h3>
                <p className="text-sm text-gray-600">
                  See how excluding ingredients affects calorie count
                </p>
              </div>
            </div>
          </div>
        ) : (
          <>
            {totalResults > 0 && (
              <div className="max-w-7xl mx-auto px-4 mb-4">
                <p className="text-gray-600">
                  Found <span className="font-semibold text-gray-800">{totalResults}</span> recipes
                </p>
              </div>
            )}
            <RecipeList
              recipes={results}
              isLoading={isLoading}
              error={error}
              onNextPage={nextPage}
              onPrevPage={prevPage}
              hasNextPage={hasNextPage}
              hasPrevPage={hasPrevPage}
              currentPage={currentPage}
              totalPages={totalPages}
            />
          </>
        )}
      </div>
    </div>
  );
};

export default HomePage;
