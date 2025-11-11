import RecipeCard from './RecipeCard';
import LoadingSpinner from './LoadingSpinner';
import { ChevronLeft, ChevronRight } from 'lucide-react';

 /** 
 * Component: RecipeList
 *
 * Presents a list or grid of RecipeCard items with loading and empty states.
 *
 * Responsibilities:
 *  - Receive list of recipes and render RecipeCard for each item.
 *  - Render loading/empty states and basic pagination or "load more" controls.
 *
 * Props:
 *  - recipes: Array<RecipeSummary>
 *  - isLoading: boolean
 *
 * Notes:
 *  - Keep list logic minimal; filtering and search are handled by parent hook/page.
 *
 * @author Rawan Sweidan
 * @since 2025-11-10
 */

const RecipeList = ({ 
  recipes, 
  isLoading, 
  error, 
  onNextPage, 
  onPrevPage,
  hasNextPage,
  hasPrevPage,
  currentPage,
  totalPages 
}) => {
  if (isLoading) {
    return <LoadingSpinner message="Searching for delicious recipes..." />;
  }

  if (error) {
    return (
      <div className="max-w-4xl mx-auto p-8 text-center">
        <div className="bg-red-50 border-2 border-red-200 rounded-lg p-6">
          <h3 className="text-xl font-semibold text-red-800 mb-2">Error Loading Recipes</h3>
          <p className="text-red-600">{error.message}</p>
        </div>
      </div>
    );
  }

  if (!recipes || recipes.length === 0) {
    return (
      <div className="max-w-4xl mx-auto p-8 text-center">
        <div className="bg-gray-50 border-2 border-gray-200 rounded-lg p-8">
          <h3 className="text-xl font-semibold text-gray-700 mb-2">No recipes found</h3>
          <p className="text-gray-600">Try adjusting your search or removing some filters</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto p-4">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {recipes.map((recipe) => (
          <RecipeCard key={recipe.id} recipe={recipe} />
        ))}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-4 mt-8">
          <button
            onClick={onPrevPage}
            disabled={!hasPrevPage}
            className="flex items-center gap-2 px-4 py-2 bg-white border-2 border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <ChevronLeft size={20} />
            Previous
          </button>
          
          <span className="text-gray-700 font-medium">
            Page {currentPage} of {totalPages}
          </span>
          
          <button
            onClick={onNextPage}
            disabled={!hasNextPage}
            className="flex items-center gap-2 px-4 py-2 bg-white border-2 border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            Next
            <ChevronRight size={20} />
          </button>
        </div>
      )}
    </div>
  );
};

export default RecipeList;
