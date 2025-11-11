### Objective
	- Build a full-stack web application called (RecipesBuddy) using the Spoonacular API that allows users to search for recipes and view detailed nutritional information. You are required to use React for the frontend and Spring Boot for the backend.
### Architecture Overview
	- The Spring Boot backend must act as a middleware between the frontend and the Spoonacular API.
	- The React frontend should only communicate with your backend, not directly with Spoonacular.
### Frontend (React)
	- Build a responsive UI that allows users to:
	- Search for recipes.
	- View detailed recipe information including:
	- Ingredients.
    - Total calories.
    - Ability to exclude ingredients and see updated calorie counts.
	- All data must be fetched from your Spring Boot backend.
	- Ensure good UX/UI design, accessibility, and responsiveness.
### Backend (Java  + Spring Boot)
	## Create RESTful endpoints that:
		- Search for recipes based on name and optional filters.
		- Return detailed recipe information including total calories.
		- Allow users to exclude ingredients and get updated calorie counts.
		- Use Spoonacular API internally to fetch and process data.
		- Do not expose your Spoonacular API key to the frontend.
### Evaluation Criteria
	- Application usability and completeness.
	- Code quality, structure, and readability.
	- Clear Names for Classes, Methods, and parameters
	- Documentation 
	- unit testing for Backend
	- UI presentation and accessibility.
	- Responsive UI
### Backend Structure
	Folder name: \RecipesBuddy\backend
	src/main/java/com/rjs/recipesbuddy/
	├── RecipeAppApplication.java
	├── config/
	│   ├── WebClientConfig.java
	│   └── CorsConfig.java
	├── controller/
	│   └── RecipeController.java
	├── service/
	│   ├── RecipeService.java
	│   └── SpoonacularApiService.java
	├── dto/
	│   ├── RecipeSearchRequest.java
	│   ├── RecipeSearchResponse.java
	│   ├── RecipeDetailResponse.java
	│   └── NutritionResponse.java
	├── exception/
	│   ├── RecipeNotFoundException.java
	│   └── GlobalExceptionHandler.java
	└── util/
		└── CalorieCalculator.java
### Frontend Structure
	Folder name: \RecipesBuddy\frontend\recipes-buddy
	src/
	├── App.jsx
	├── main.jsx
	├── api/
	│   └── recipeApi.js
	├── components/
	│	├── ErrorMessage.jsx
	│   ├── SearchBar.jsx
	│   ├── RecipeCard.jsx
	│   ├── RecipeList.jsx
	│   ├── RecipeDetail.jsx
	│   ├── IngredientList.jsx
	│   ├── NutritionPanel.jsx
	│   └── LoadingSpinner.jsx
	├── pages/
	│   ├── HomePage.jsx
	│   └── RecipeDetailPage.jsx
	├── hooks/
	│   ├── useRecipeSearch.js
	│   └── useRecipeDetail.js
	├── utils/
	│   └── formatters.js
	└── styles/
		└── global.css
