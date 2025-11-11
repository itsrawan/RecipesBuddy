import { useState } from 'react';
import { Check, X } from 'lucide-react';

/**
 * Component: IngredientList
 *
 * Renders a list of ingredients with controls to exclude/include items and trigger calorie recalculation.
 *
 * Responsibilities:
 *  - Show each ingredient with amount and unit.
 *  - Allow toggling exclusion state and trigger calculation via parent callbacks.
 *  - Provide "Calculate" and "Clear Exclusions" controls and show calculation state.
 *
 * Props:
 *  - ingredients: Array<Ingredient>
 *  - excludedIds: Array<number>
 *  - onToggleIngredient: (id: number) => void
 *  - onCalculate: () => void
 *  - onClearExclusions: () => void
 *  - isCalculating: boolean
 *
 * Notes:
 *  - Presentational component; business logic lives in useRecipeDetail hook.
 *
 * @author Rawan Sweidan
 * @since 2025-11-10
 */

const IngredientList = ({ 
  ingredients, 
  excludedIds, 
  onToggleIngredient, 
  onCalculate,
  onClearExclusions,
  isCalculating 
}) => {
  if (!ingredients || ingredients.length === 0) {
    return <div className="text-gray-600">No ingredients available</div>;
  }

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-2xl font-bold text-gray-800">Ingredients</h2>
        {excludedIds.length > 0 && (
          <button
            onClick={onClearExclusions}
            className="text-sm text-gray-600 hover:text-gray-800 underline"
          >
            Clear exclusions
          </button>
        )}
      </div>

      <div className="space-y-2 mb-4">
        {ingredients.map((ingredient) => {
          const isExcluded = excludedIds.includes(ingredient.id);
          
          return (
            <div
              key={ingredient.id}
              className={`flex items-center gap-3 p-3 rounded-lg border-2 transition-all cursor-pointer hover:bg-gray-50 ${
                isExcluded 
                  ? 'border-red-300 bg-red-50 opacity-60' 
                  : 'border-gray-200'
              }`}
              onClick={() => onToggleIngredient(ingredient.id)}
            >
              <div className={`flex-shrink-0 w-6 h-6 rounded border-2 flex items-center justify-center ${
                isExcluded 
                  ? 'bg-red-500 border-red-500' 
                  : 'border-gray-300'
              }`}>
                {isExcluded && <Check size={16} className="text-white" />}
              </div>

              <div className="flex-1">
                <p className={`font-medium ${isExcluded ? 'line-through text-gray-500' : 'text-gray-800'}`}>
                  {ingredient.name}
                </p>
                <p className="text-sm text-gray-600">
                  {ingredient.amount} {ingredient.unit}
                  {ingredient.original && ` - ${ingredient.original}`}
                </p>
              </div>

              {isExcluded && (
                <X size={20} className="text-red-500 flex-shrink-0" />
              )}
            </div>
          );
        })}
      </div>

      {excludedIds.length > 0 && (
        <button
          onClick={onCalculate}
          disabled={isCalculating}
          className="w-full bg-primary-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
        >
          {isCalculating ? 'Calculating...' : 'Calculate Updated Calories'}
        </button>
      )}

      <p className="text-sm text-gray-500 mt-3 text-center">
        Click on ingredients to exclude them from calorie calculation
      </p>
    </div>
  );
};

export default IngredientList;