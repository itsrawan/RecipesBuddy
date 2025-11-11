import { Clock, Users, Flame, Beef, Cookie, Droplet } from 'lucide-react';
import { Link } from 'react-router-dom';

/**
 * Component: RecipeCard
 *
 * Compact card used in search results to show recipe thumbnail, title and meta.
 *
 * Responsibilities:
 *  - Render recipe image, title, ready time and brief tags.
 *  - Emit click/navigation events when activated.
 *
 * Props:
 *  - recipe: RecipeSummary
 *  - onClick?: () => void
 *
 * Notes:
 *  - Keep markup simple so the card can be used in grid or list layouts responsively.
 *
 * @author Rawan Sweidan
 * @since 2025-11-10
 */
/**
 * Component: RecipeCard
 *
 * Compact card used in search results to show recipe thumbnail, title and meta.
 *
 * Responsibilities:
 *  - Render recipe image, title, ready time and brief tags.
 *  - Emit click/navigation events when activated.
 *
 * Props:
 *  - recipe: RecipeSummary
 *  - onClick?: () => void
 *
 * Notes:
 *  - Keep markup simple so the card can be used in grid or list layouts responsively.
 *
 * @author Rawan Sweidan
 * @since 2025-11-10
 */

const RecipeCard = ({ recipe }) => {
  const placeholderImage = `/recipe-placeholder.jpg`;

  // Extract nutrition info from recipe.nutrition
  const getNutrientValue = (nutrientName) => {
    if (!recipe.nutrition?.nutrients) return null;
    const nutrient = recipe.nutrition.nutrients.find(n => n.name === nutrientName);
    return nutrient ? Math.round(nutrient.amount) : null;
  };

  const calories = getNutrientValue('Calories');
  const carbs = getNutrientValue('Carbohydrates');
  const protein = getNutrientValue('Protein');
  const fat = getNutrientValue('Fat');

  return (
    <Link
      to={`/recipe/${recipe.id}`}
      data-testid="RecipeItem"
      className="group block bg-white rounded-lg shadow-md overflow-hidden hover:shadow-xl transition-shadow duration-300"
    >
      {/* Title centered at top */}
      <div className="p-4 pb-2 border-b border-gray-100">
        <h3 className="text-center text-lg font-semibold text-gray-800 line-clamp-2 group-hover:text-primary-600 transition-colors" data-testid="RecipeItemName">
          {recipe.title}
        </h3>
      </div>
      
      {/* Image on left, details on right */}
      <div className="flex gap-4 p-4">
        {/* Small image on the left */}
        <div className="flex-shrink-0">
          <div className="relative w-24 h-24 overflow-hidden rounded-lg bg-gray-200">
            <img
              src={recipe.image || placeholderImage}
              alt={recipe.title}
              className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300"
              loading="lazy"
              onError={(e) => {
                e.target.src = placeholderImage;
              }}
            />
          </div>
        </div>
        
        {/* Details on the right - Two columns */}
        <div className="flex-1 flex gap-4 text-sm text-gray-600">
          {/* Left column: Duration, Servings, Calories */}
          <div className="flex flex-col justify-center gap-2 flex-1">
            {recipe.readyInMinutes && (
              <div className="flex items-center gap-2">
                <Clock size={16} className="text-primary-500" />
                <span className="font-medium">{recipe.readyInMinutes} min</span>
              </div>
            )}
            
            {recipe.servings && (
              <div className="flex items-center gap-2">
                <Users size={16} className="text-primary-500" />
                <span className="font-medium">{recipe.servings} servings</span>
              </div>
            )}

            {calories && (
              <div className="flex items-center gap-2">
                <Flame size={16} className="text-orange-500" />
                <span className="font-medium">{calories} cal</span>
              </div>
            )}
          </div>

          {/* Right column: Carbs, Protein, Fat */}
          <div className="flex flex-col justify-center gap-2 flex-1">
            {carbs && (
              <div className="flex items-center gap-2">
                <Cookie size={16} className="text-amber-500" />
                <span className="font-medium">{carbs}g carbs</span>
              </div>
            )}
            
            {protein && (
              <div className="flex items-center gap-2">
                <Beef size={16} className="text-red-500" />
                <span className="font-medium">{protein}g protein</span>
              </div>
            )}

            {fat && (
              <div className="flex items-center gap-2">
                <Droplet size={16} className="text-yellow-500" />
                <span className="font-medium">{fat}g fat</span>
              </div>
            )}
          </div>
        </div>
      </div>
    </Link>
  );
};

export default RecipeCard;
