import { Flame, TrendingDown, Check } from 'lucide-react';

/**
 * Component: NutritionPanel
 *
 * Displays original and updated calorie values and other nutrition highlights.
 *
 * Responsibilities:
 *  - Accept originalCalories and caloriesData to show current/calculated values.
 *  - Show visual state when exclusions are active or calculation is in progress.
 *
 * Props:
 *  - originalCalories: number
 *  - caloriesData: { originalCalories, updatedCalories, reducedCalories } | null
 *  - hasExclusions: boolean
 *
 * Notes:
 *  - Purely presentational; calculations and data fetching are handled elsewhere.
 *
 * @author Rawan Sweidan
 * @since 2025-11-10
 */
/**
 * Component: NutritionPanel
 *
 * Displays original and updated calorie values and other nutrition highlights.
 *
 * Responsibilities:
 *  - Accept originalCalories and caloriesData to show current/calculated values.
 *  - Show visual state when exclusions are active or calculation is in progress.
 *
 * Props:
 *  - originalCalories: number
 *  - caloriesData: { originalCalories, updatedCalories, reducedCalories } | null
 *  - hasExclusions: boolean
 *
 * Notes:
 *  - Purely presentational; calculations and data fetching are handled elsewhere.
 *
 * @author Rawan Sweidan
 * @since 2025-11-10
 */

const NutritionPanel = ({ 
  originalCalories, 
  caloriesData, 
  hasExclusions 
}) => {
  const displayCalories = caloriesData?.updatedCalories ?? originalCalories;
  const caloriesReduced = caloriesData?.caloriesReduced ?? 0;

  return (
    <div className="bg-white rounded-lg shadow-md p-6 sticky top-4">
      <h2 className="text-2xl font-bold text-gray-800 mb-4">Nutrition Info</h2>

      <div className="space-y-4">
        {/* Original Calories */}
        <div className="bg-gray-50 rounded-lg p-4">
          <div className="flex items-center gap-2 mb-2">
            <Flame size={20} className="text-orange-500" />
            <span className="text-sm font-medium text-gray-600">Original Calories</span>
          </div>
          <p className="text-3xl font-bold text-gray-800">
            {originalCalories?.toFixed(0) || '0'} 
            <span className="text-lg font-normal text-gray-600 ml-1">kcal</span>
          </p>
        </div>

        {/* Updated Calories (if ingredients excluded) */}
        {hasExclusions && caloriesData && (
          <>
            <div className="bg-primary-50 rounded-lg p-4 border-2 border-primary-200">
              <div className="flex items-center gap-2 mb-2">
                <Check size={20} className="text-primary-600" />
                <span className="text-sm font-medium text-primary-700">Updated Calories</span>
              </div>
              <p className="text-3xl font-bold text-primary-700">
                {caloriesData.updatedCalories?.toFixed(0) || '0'}
                <span className="text-lg font-normal text-primary-600 ml-1">kcal</span>
              </p>
            </div>

            <div className="bg-green-50 rounded-lg p-4 border-2 border-green-200">
              <div className="flex items-center gap-2 mb-2">
                <TrendingDown size={20} className="text-green-600" />
                <span className="text-sm font-medium text-green-700">Calories Reduced</span>
              </div>
              <p className="text-3xl font-bold text-green-700">
                -{caloriesReduced.toFixed(0)}
                <span className="text-lg font-normal text-green-600 ml-1">kcal</span>
              </p>
              <p className="text-sm text-green-600 mt-1">
                {caloriesData.ingredientsExcluded} ingredient(s) excluded
              </p>
            </div>
          </>
        )}

        {hasExclusions && !caloriesData && (
          <div className="bg-yellow-50 rounded-lg p-4 border-2 border-yellow-200">
            <p className="text-sm text-yellow-700 text-center">
              Click "Calculate Updated Calories" to see the impact of your exclusions
            </p>
          </div>
        )}
      </div>

      {/* Additional Info */}
      <div className="mt-6 pt-6 border-t border-gray-200">
        <p className="text-xs text-gray-500 text-center">
          Nutritional information is approximate and may vary based on specific ingredients and preparation methods.
        </p>
      </div>
    </div>
  );
};

export default NutritionPanel;