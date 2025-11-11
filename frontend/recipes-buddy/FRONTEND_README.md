# RecipesBuddy - Frontend Documentation

## Overview

This is the React frontend for the RecipesBuddy application. It provides a user-friendly interface for searching recipes, viewing detailed recipe information, and calculating calories when excluding ingredients.

**Author:** Rawan Sweidan  
**Version:** 1.0.0  
**Technology Stack:** React 18 + Vite + TailwindCSS

## Project Structure

```
frontend/recipes-buddy/
├── src/
│   ├── App.jsx                 # Main application component with routing
│   ├── main.jsx               # Application entry point
│   ├── api/
│   │   └── recipeApi.js       # Backend API communication
│   ├── components/            # Reusable UI components
│   │   ├── ErrorMessage.jsx   # Error display component
│   │   ├── SearchBar.jsx      # Recipe search input
│   │   ├── RecipeCard.jsx     # Recipe summary card
│   │   ├── RecipeList.jsx     # Grid of recipe cards
│   │   ├── IngredientList.jsx # Ingredients with exclusion checkboxes
│   │   ├── NutritionPanel.jsx # Nutrition information display
│   │   └── LoadingSpinner.jsx # Loading indicator
│   ├── pages/                 # Page components
│   │   ├── HomePage.jsx       # Search and results page
│   │   └── RecipeDetailPage.jsx # Recipe details and calorie calculator
│   ├── hooks/                 # Custom React hooks
│   │   ├── useRecipeSearch.js # Recipe search logic
│   │   └── useRecipeDetail.js # Recipe details logic
│   ├── utils/                 # Utility functions
│   │   └── formatters.js      # Data formatting helpers
│   └── styles/
│       └── global.css         # Global styles
├── tests/                     # E2E tests (Playwright)
│   ├── home.spec.js          # Home page tests
│   ├── recipe-details.spec.js # Recipe details tests
│   └── calorie-calculator.spec.js # Calculator tests
├── public/                    # Static assets
│   ├── chef.png              # App logo
│   └── recipe-placeholder.jpg # Default recipe image
├── playwright.config.js       # Playwright E2E test configuration
├── vite.config.js            # Vite build configuration
├── tailwind.config.js        # TailwindCSS configuration
└── package.json              # Dependencies and scripts
```

## Key Components

### 1. App.jsx
**Purpose:** Main application component with React Router setup

**Features:**
- Client-side routing (BrowserRouter)
- Routes for home and recipe details pages
- Layout wrapper for all pages

**Usage:**
```jsx
<BrowserRouter>
  <Routes>
    <Route path="/" element={<HomePage />} />
    <Route path="/recipe/:id" element={<RecipeDetailPage />} />
  </Routes>
</BrowserRouter>
```

### 2. SearchBar.jsx
**Purpose:** Recipe search input with filters

**Props:**
- `onSearch(params)` - Callback when search is triggered
- `initialQuery` - Initial search text (optional)

**Features:**
- Text input for recipe name
- Optional nutrition filters (calories, carbs, protein, fat)
- Responsive design
- Data test IDs for E2E testing

### 3. RecipeCard.jsx
**Purpose:** Display recipe summary in a card

**Props:**
- `recipe` - Recipe object with id, title, image
- `onClick(id)` - Click handler for navigation

**Features:**
- Recipe image with fallback
- Recipe title
- Clickable to view details
- Hover effects
- Test ID: `RecipeItem`

### 4. RecipeList.jsx
**Purpose:** Grid layout for recipe cards

**Props:**
- `recipes` - Array of recipe objects
- `onRecipeClick(id)` - Navigation handler

**Features:**
- Responsive grid (1-4 columns based on screen size)
- Handles empty states
- Loading states

### 5. IngredientList.jsx
**Purpose:** Display ingredients with exclusion checkboxes

**Props:**
- `ingredients` - Array of ingredient objects
- `onExclusionChange(excludedIds)` - Callback with excluded ingredient IDs

**Features:**
- Checkbox for each ingredient
- Ingredient name and amount display
- Selection tracking
- Accessibility support

### 6. NutritionPanel.jsx
**Purpose:** Display nutrition information

**Props:**
- `nutrition` - Nutrition object with nutrients array
- `originalCalories` - Original calorie count (optional)
- `updatedCalories` - Updated calorie count (optional)

**Features:**
- Displays calories, protein, carbs, fat
- Shows calorie reduction when excluding ingredients
- Visual indicators for changes
- Responsive layout

### 7. LoadingSpinner.jsx
**Purpose:** Loading indicator during API calls

**Features:**
- Animated spinner
- Centered on screen
- Accessible (ARIA labels)
- Test ID: `LoadingSpinner`

### 8. ErrorMessage.jsx
**Purpose:** Display error messages

**Props:**
- `message` - Error message text
- `onRetry()` - Optional retry callback

**Features:**
- User-friendly error display
- Optional retry button
- Dismissible
- Styling with TailwindCSS

## Pages

### HomePage.jsx
**Purpose:** Main page with search and results

**Features:**
- Recipe search interface
- Results displayed in grid
- Loading and error states
- Navigation to recipe details
- State management with useState/useEffect

**Flow:**
```
User enters search → API call → Display results → Click recipe → Navigate to details
```

### RecipeDetailPage.jsx
**Purpose:** Detailed recipe view with calorie calculator

**Features:**
- Recipe image and metadata
- Full ingredients list
- Nutrition information
- Ingredient exclusion (checkboxes)
- Calorie recalculation
- Back navigation

**Flow:**
```
Load recipe → Display details → User excludes ingredients → Calculate → Show updated calories
```

## Custom Hooks

### useRecipeSearch.js
**Purpose:** Encapsulate recipe search logic

**Returns:**
```javascript
{
  recipes: [],           // Search results
  loading: boolean,      // Loading state
  error: string,         // Error message
  search: (params) => {} // Search function
}
```

**Usage:**
```jsx
const { recipes, loading, error, search } = useRecipeSearch();

useEffect(() => {
  search({ query: 'pasta', maxCalories: 500 });
}, []);
```

### useRecipeDetail.js
**Purpose:** Encapsulate recipe details and calorie calculation

**Returns:**
```javascript
{
  recipe: {},                    // Recipe details
  loading: boolean,              // Loading state
  error: string,                 // Error message
  updatedCalories: number,       // Calculated calories
  calculateCalories: (ids) => {} // Calculate function
}
```

## API Communication (recipeApi.js)

### Functions

**searchRecipes(params)**
```javascript
// Search for recipes
const results = await searchRecipes({
  query: 'pasta',
  maxCalories: 500,
  size: 12,
  offset: 0
});
```

**getRecipeById(id)**
```javascript
// Get recipe details
const recipe = await getRecipeById(123);
```

**calculateUpdatedCalories(recipeId, excludedIngredientIds)**
```javascript
// Calculate calories with exclusions
const result = await calculateUpdatedCalories(123, [1, 2, 3]);
// Returns: { originalCalories, updatedCalories, caloriesReduced }
```

### Configuration
- Base URL: `http://localhost:8080/api/recipes`
- Timeout: 10 seconds
- Error handling: Throws errors for failed requests

## Styling

### TailwindCSS
The application uses Tailwind for styling with:
- Responsive utilities (sm:, md:, lg:, xl:)
- Custom colors and spacing
- Component-specific classes
- Dark mode support (optional)

### Global Styles (global.css)
- Font family: Inter
- Base typography
- Custom animations
- Reset styles

## Running the Application

### Development
```bash
npm run dev
```
- Starts Vite dev server on port 5173
- Hot module replacement enabled
- Fast refresh for React components

### Build
```bash
npm run build
```
- Creates optimized production build
- Output in `dist/` directory
- Minified and tree-shaken

### Preview
```bash
npm run preview
```
- Preview production build locally
- Tests optimized bundle

### Testing
```bash
# Run E2E tests
npm run test:e2e

# Run E2E tests in UI mode
npm run test:e2e:ui

# Run tests in headed mode
npm run test:e2e:headed
```

## Environment Variables

Create `.env` file (optional):
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_NAME=RecipesBuddy
```

Access in code:
```javascript
const apiUrl = import.meta.env.VITE_API_BASE_URL;
```

## Best Practices

### Component Design
1. **Keep components small and focused**
   - Single responsibility
   - Reusable where possible
   - Props for customization

2. **Use semantic HTML**
   - Proper heading hierarchy
   - ARIA labels for accessibility
   - Semantic tags (nav, main, article)

3. **Handle loading and error states**
   - Show loading spinners
   - Display user-friendly errors
   - Provide retry options

### State Management
1. **Use appropriate state location**
   - Local state for UI-only data
   - Props for parent-child communication
   - Custom hooks for shared logic

2. **Avoid prop drilling**
   - Use composition
   - Extract to custom hooks
   - Consider Context for deep trees

### Performance
1. **Optimize re-renders**
   - Use React.memo for expensive components
   - Memoize callbacks with useCallback
   - Memoize computed values with useMemo

2. **Code splitting**
   - Lazy load routes
   - Split large bundles
   - Optimize images

### Testing
1. **Add test IDs to components**
```jsx
<input data-testid="SearchText" />
<button data-testid="SearchButton" />
<div data-testid="RecipeItem" />
```

2. **Test user flows, not implementation**
   - Focus on user behavior
   - Avoid testing internal state
   - Test accessibility

## Common Issues & Solutions

### Issue: CORS errors
**Solution:** Ensure backend CORS is configured for `http://localhost:5173`

### Issue: API calls fail
**Solution:** 
1. Check backend is running on port 8080
2. Verify API endpoints in recipeApi.js
3. Check browser console for errors

### Issue: Images not loading
**Solution:** 
1. Check image URLs from API
2. Verify fallback image exists in public/
3. Check network tab for 404s

### Issue: Build fails
**Solution:**
1. Delete `node_modules` and `package-lock.json`
2. Run `npm install`
3. Check for dependency conflicts

## Future Enhancements

### Planned Features
- [ ] Recipe favorites/bookmarks
- [ ] User authentication
- [ ] Recipe sharing
- [ ] Print recipe functionality
- [ ] Meal planning
- [ ] Shopping list generation

### Technical Improvements
- [ ] Add React Testing Library unit tests
- [ ] Implement Redux/Zustand for state management
- [ ] Add service worker for offline support
- [ ] Implement virtual scrolling for large lists
- [ ] Add animation library (Framer Motion)
- [ ] Implement skeleton loaders

## Resources

- [React Documentation](https://react.dev/)
- [Vite Documentation](https://vitejs.dev/)
- [TailwindCSS Documentation](https://tailwindcss.com/)
- [Playwright Documentation](https://playwright.dev/)
- [React Router Documentation](https://reactrouter.com/)

## Contact

**Developer:** Rawan Sweidan  
**Project:** RecipesBuddy  
**Date:** November 2025

For questions or improvements, please refer to the main project README.md
