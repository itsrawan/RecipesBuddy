/**
 * Recipe detail page
 *
 * Renders full recipe details including image, summary, time/servings,
 * ingredient list (with exclusion controls) and the nutrition panel.
 *
 * Responsibilities:
 *  - Read recipe id from route params and obtain data via useRecipeDetail hook.
 *  - Show loading and error states.
 *  - Render IngredientList and NutritionPanel and forward handlers from the hook.
 *
 * Notes:
 *  - Uses dangerouslySetInnerHTML for summary and instructions since the
 *    backend returns HTML fragments from the external API.
 *  - Keeps a small fallback image when recipe.image is missing.
 *
 * @author Rawan Sweidan
 * @since 2025-11-10
 */

import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Clock, Users, Leaf } from 'lucide-react';
import { useRecipeDetail } from '../hooks/useRecipeDetail';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import IngredientList from '../components/IngredientList';
import NutritionPanel from '../components/NutritionPanel';

const RecipeDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const {
    recipe,
    isLoading,
    error,
    excludedIngredients,
    toggleIngredient,
    calculateCalories,
    clearExclusions,
    caloriesData,
    isCalculating,
    originalCalories,
    hasExclusions,
  } = useRecipeDetail(id);

  if (isLoading) {
    return <LoadingSpinner message="Loading recipe details..." />;
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 p-8">
        <ErrorMessage 
          message={error.message} 
          onRetry={() => window.location.reload()}
        />
      </div>
    );
  }

  if (!recipe) {
    return (
      <div className="min-h-screen bg-gray-50 p-8">
        <ErrorMessage message="Recipe not found" />
      </div>
    );
  }

  const placeholderImage = `https://via.placeholder.com/800x400/10b981/ffffff?text=${encodeURIComponent(recipe.title)}`;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b border-gray-200 sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <button
            onClick={() => navigate('/')}
            className="flex items-center gap-2 text-gray-600 hover:text-gray-900 transition-colors"
          >
            <ArrowLeft size={20} />
            Back to Search
          </button>
        </div>
      </div>

      {/* Recipe Header */}
      <div className="bg-white">
        <div className="max-w-7xl mx-auto px-4 py-8">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <div>
              <img
                src={recipe.image || placeholderImage}
                alt={recipe.title}
                className="w-full h-96 object-cover rounded-lg shadow-lg"
                onError={(e) => {
                  e.target.src = placeholderImage;
                }}
              />
            </div>
            
            <div>
              <h1 className="text-4xl font-bold text-gray-900 mb-4">
                {recipe.title}
              </h1>
              
              <div className="flex flex-wrap gap-4 mb-6">
                {recipe.readyInMinutes && (
                  <div className="flex items-center gap-2 bg-gray-100 px-4 py-2 rounded-lg">
                    <Clock size={20} className="text-gray-600" />
                    <span className="font-medium">{recipe.readyInMinutes} minutes</span>
                  </div>
                )}
                
                {recipe.servings && (
                  <div className="flex items-center gap-2 bg-gray-100 px-4 py-2 rounded-lg">
                    <Users size={20} className="text-gray-600" />
                    <span className="font-medium">{recipe.servings} servings</span>
                  </div>
                )}
              </div>

              {/* Diet Tags */}
              <div className="flex flex-wrap gap-2 mb-6">
                {recipe.vegetarian && (
                  <span className="inline-flex items-center gap-1 bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-medium">
                    <Leaf size={14} />
                    Vegetarian
                  </span>
                )}
                {recipe.vegan && (
                  <span className="inline-flex items-center gap-1 bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-medium">
                    <Leaf size={14} />
                    Vegan
                  </span>
                )}
                {recipe.glutenFree && (
                  <span className="bg-amber-100 text-amber-800 px-3 py-1 rounded-full text-sm font-medium">
                    Gluten-Free
                  </span>
                )}
                {recipe.dairyFree && (
                  <span className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm font-medium">
                    Dairy-Free
                  </span>
                )}
              </div>

              {recipe.summary && (
                <div 
                  className="text-gray-700 leading-relaxed prose prose-sm max-w-none"
                  dangerouslySetInnerHTML={{ __html: recipe.summary }}
                />
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Ingredients Section */}
          <div className="lg:col-span-2">
            <IngredientList
              ingredients={recipe.extendedIngredients}
              excludedIds={excludedIngredients}
              onToggleIngredient={toggleIngredient}
              onCalculate={calculateCalories}
              onClearExclusions={clearExclusions}
              isCalculating={isCalculating}
            />

            {/* Instructions */}
            {recipe.instructions && (
              <div className="bg-white rounded-lg shadow-md p-6 mt-6">
                <h2 className="text-2xl font-bold text-gray-800 mb-4">Instructions</h2>
                <div 
                  className="prose prose-sm max-w-none text-gray-700"
                  dangerouslySetInnerHTML={{ __html: recipe.instructions }}
                />
              </div>
            )}
          </div>

          {/* Nutrition Panel */}
          <div>
            <NutritionPanel
              originalCalories={originalCalories}
              caloriesData={caloriesData}
              hasExclusions={hasExclusions}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default RecipeDetailPage;
