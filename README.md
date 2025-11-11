RecipesBuddy 
------------

RecipesBuddy is a full-stack web application using Spoonacular API that allows users to search for recipes and view detailed nutritional information, Preparing Instructions, and removing Ingrediance to update the calories in the meal. build and developed using Spring Boot (v. 3.5.7), React (v. 18.2)

System Architecture 
-------------------

┌────────────────────┐
│  React Frontend    │
│  (Port 5173)       │
└────────────────────┘
           ▲
           │
           │ HTTP
           ▼
┌─────────────────────────────────────────────────────────┐
│                           BACKEND                       │
│  ┌────────────────────┐         ┌────────────────────┐  │
│  │  Spring Boot API   │◄───────►│  Spoonacular API   │  │
│  │    (Port 8080)     │   HTTP  │                    │  │
│  └────────────────────┘         └────────────────────┘  │
└─────────────────────────────────────────────────────────┘


AI Usage
---------

### AI Agent: 

    - Claude Code AI 
    - VSCode Cline AI Agent extention Using Claude Code API
    - under .\clinerules you can find 
        1- all the standards that were used to guide the AI (AI Asistent Rules.md), explain the project technical overview
        2- Implementation Best Practices that AI Should follow
        3- .\clinerules\workflow AI Agent Tasks list

### Area Of Usage (With Prompt commands):

    - Building the structure of the application
        - Scan Backend folder based on the Rules in the "AI Asistent Rules.md" and "Best Practice.md" generate the needed files
        - Scan frontend folder based on the Rules in the "AI Asistent Rules.md" and "Best Practice.md" and prepare a list of what missing and required changes in the React to finish the project as expected

    - Fixing Errors (used Commands):
        - Check why return error 401 Unauthorized from GET https://api.spoonacular.com/recipes/complexSearch
        - Calculate and update the calories after execluding ingrediance returning 0's for both updated calories and Calories Reduced
    
    - All other Enhacment/Fixes/Changes tasks requested by tasks md files (.\clinerules\workflow) and called as below
        - start "enhance UI.md" task
        - start "task2.md" 
        - [UI Change]: on the RecipeCard display also the value of totalCalories, Carbs, protein, and fat from the NutifionInfo
keep duration, service and calories under each other and next to them carbs, protein and fat
    
    - Generate Setup Instruction
        - generate a Setup instructions for both spring boot backend application and React frontend apllication and append under title "Setup instructions" in file \RecipesBuddy\README.md


### Lessons learned:

    - Time Effeciancy with AI: This assigment was completed in about 2.5 days using AI assistance, 50% of time was spent in analysis, planning and preparing the context and guidelines for the AI to generate the project according to specifications.
    For normal development without using AI and the developer will take 1 - 2.5 weeks to finished based on the experience of the requested technologies.
    - Advantage of AI: Given the significant improvements AI brings in all aspects in general and development workflow in specific, every professional and business owner should strategically adopt AI into their workflow operations, whether for daily tasks, business processes, or exploring new ideas. The key is to provide a clear plan, and detailed guidance to ensure optimal outcomes

   ## AI Best Practices 
   
    - Create a separate context file for each set of tasks, otherwise the AI tends to revisit and regenerate previously completed work, leading to inefficiency.
    - Validate, Review and test all AI-generated code across all application


Setup Instructions
------------------

### Prerequisites
Before setting up the application, ensure you have the following installed:
- **Java Development Kit (JDK) 17 or higher**
- **Maven 3.6+** (for building the Spring Boot backend)
- **Node.js 18+** and **npm** (for running the React frontend)
- **Git** (for cloning the repository)
- **Spoonacular API Key** - Sign up at https://spoonacular.com/food-api to get your free API key

### Backend Setup (Spring Boot)

1. **Navigate to the backend directory:**
   ```
   cd backend
   ```

2. **Configure the Spoonacular API Key:**
   - Add the API Key in the environment Variables of the machine with the name:
      SPOONACULAR_API_KEY

      **Windows (Command Prompt):**
      ```cmd
      setx SPOONACULAR_API_KEY "your_api_key_here"
      ```

      **Windows (PowerShell):**
      ```powershell
      $env:SPOONACULAR_API_KEY="your_api_key_here"
      ```

      **Linux/Mac:**
      ```bash
      export SPOONACULAR_API_KEY="your_api_key_here"
      ```
      
   - To change the base url for the api:
   - Open `src/main/resources/application.properties`
     ```properties
     spoonacular.api.base-url=https://api.spoonacular.com
     ```
   

3. **Build the project:**
   ```
   .\mvnw clean install
   ```
   Or on Unix/Linux/Mac:
   ```bash
   ./mvnw clean install
   ```

4. **Run the Spring Boot application:**
   ```
   .\mvnw spring-boot:run
   ```
   Or on Unix/Linux/Mac:
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Verify the backend is running:**
   - The backend should start on `http://localhost:8080`
   - Test the API endpoint: `http://localhost:8080/api/recipes/search?query=pasta`

### Frontend Setup (React)

1. **Navigate to the frontend directory:**
   ```bash
   cd frontend/recipes-buddy
   ```

2. **Install dependencies:**
   ```bash
   npm install
   npx playwright install
   ```

3. **Configure the backend API URL (if needed):**
   - The frontend is configured to connect to `http://localhost:8080` by default
   - If your backend runs on a different port, update `src/api/recipeApi.js`

4. **Start the development server:**
   ```bash
   npm run dev
   ```

5. **Access the application:**
   - Open your browser and navigate to `http://localhost:5173`
   - The frontend will automatically connect to the backend API

### Testing

RecipesBuddy includes comprehensive testing at multiple levels:
- **Backend Unit Tests** (15 tests)
- **Backend Integration Tests with WireMock** (13 tests)
- **Frontend E2E Tests with Playwright** (33 tests)

**Total Test Coverage: 61 tests**

#### Backend Testing

**Run All Backend Tests:**
```bash
cd backend
mvnw test
```

**Test Breakdown:**
- **Unit Tests** (`RecipeServiceTest.java`) - 11 tests
  - Recipe search with various parameters
  - Recipe details retrieval
  - Calorie calculation with ingredient exclusions
  - Edge cases and error scenarios

- **Controller Tests** (`RecipeControllerTest.java`) - 4 tests
  - REST endpoint validation
  - Request/response mapping
  - Error handling

- **Integration Tests** (`SpoonacularApiIntegrationTest.java`) - 13 tests
  - WireMock-based integration testing
  - **No API quota consumed** - uses mock responses
  - Tests API error scenarios safely
  - Retry mechanism validation

**Run Only Integration Tests:**
```bash
cd backend
mvnw test -Dtest=SpoonacularApiIntegrationTest
```

**Run Specific Test:**
```bash
mvnw test -Dtest=RecipeServiceTest#testSearchRecipes_Success
```

#### Frontend E2E Testing (Playwright)

RecipesBuddy uses Playwright for end-to-end testing with comprehensive coverage:

**Prerequisites:**
- Backend must be running on port 8080
- Frontend will be started automatically by Playwright

**Run All E2E Tests (Headless):**
```bash
cd frontend/recipes-buddy
npm run test:e2e
```

**Run Tests in Headed Mode (See Browser):**
```bash
npm run test:e2e:headed
```

**Run Tests in UI Mode (Interactive):**
```bash
npm run test:e2e:ui
```

**Run Specific Test File:**
```bash
npx playwright test tests/home.spec.js
npx playwright test tests/recipe-details.spec.js
npx playwright test tests/calorie-calculator.spec.js
```

**View Test Report:**
```bash
npx playwright show-report
```

**E2E Test Coverage:**

1. **Home Page Tests** (`home.spec.js`) - 12 tests
   - Page load verification
   - Search functionality
   - Results display
   - Recipe cards with information
   - Error handling
   - Responsive layout
   - Console error detection

2. **Recipe Details Tests** (`recipe-details.spec.js`) - 9 tests
   - Navigation to recipe details
   - Recipe information display
   - Nutrition data visibility
   - Ingredients list
   - Back navigation
   - Missing recipe handling
   - Mobile responsiveness

3. **Calorie Calculator Tests** (`calorie-calculator.spec.js`) - 12 tests
   - Original calorie display
   - Ingredient exclusion via checkboxes
   - Calorie count updates
   - Multiple ingredient exclusions
   - Calorie reduction display
   - Toggle functionality
   - Error handling
   - API error simulation

**Test Reports:**
- HTML Report: `frontend/recipes-buddy/playwright-report/index.html`
- JSON Results: `frontend/recipes-buddy/test-results/results.json`
- JUnit Report: `frontend/recipes-buddy/test-results/results.xml`

### Building for Production

**Backend:**
```bash
cd backend
mvnw clean package
java -jar target/recipesbuddy-0.0.1-SNAPSHOT.jar
```

**Frontend:**
```bash
cd frontend/recipes-buddy
npm run build
```
The production build will be created in the `dist` directory.

### Mock Mode (Save Your API Quota!)

RecipesBuddy includes a mock mode feature that allows you to develop and test without consuming your Spoonacular API quota.

#### Configuration

In `backend/src/main/resources/application.properties`:

```properties
# MOCK MODE - Use during development (FREE - no API quota used)
spoonacular.mock-mode=true

# LIVE MODE - Use in production (USES real API quota)
spoonacular.mock-mode=false
```

#### How It Works

| Endpoint | Mock Mode (true) | Live Mode (false) |
|----------|------------------|-------------------|
| Recipe Search | ✅ Uses static mock data (FREE) | ⚠️ Calls Spoonacular API (1 request) |
| Recipe Details | ⚠️ Uses live API | ⚠️ Calls Spoonacular API (1 request) |
| Ingredient Info | ⚠️ Uses live API | ⚠️ Calls Spoonacular API (1 request) |

#### Development Workflow

**Recommended for Development:**
```properties
spoonacular.mock-mode=true
```

This allows you to:
- Search recipes unlimited times (FREE!)
- Test UI and functionality without quota concerns
- Only use API quota when viewing recipe details

**For Production:**
```properties
spoonacular.mock-mode=false
```

#### API Quota Savings Example

**Without Mock Mode:**
```
50 recipe searches: 50 requests
10 recipe details:  10 requests
5 calorie calculations: 25 requests
Total: 85 requests / 150 daily limit (57% used)
```

**With Mock Mode:**
```
50 recipe searches: 0 requests (MOCKED!)
10 recipe details:  10 requests
5 calorie calculations: 25 requests
Total: 35 requests / 150 daily limit (23% used)
```

**Savings: 50 API requests (33% reduction!)**

#### Verification

Check your console logs to see which mode is active:

**Mock Mode:**
```
INFO - Searching recipes [mockMode=true]: query=pasta
INFO - Using MOCK MODE for recipe search - No API quota consumed
```

**Live Mode:**
```
INFO - Searching recipes [mockMode=false]: query=pasta
INFO - Using LIVE MODE for recipe search - API quota will be consumed
```

### Troubleshooting

**Backend Issues:**
- Ensure port 8080 is not already in use
- Verify your Spoonacular API key is valid and has remaining quota
- Check Java version: `java -version` (should be 17+)
- If tests fail, ensure mock-mode is set correctly in application.properties

**Frontend Issues:**
- Clear npm cache: `npm cache clean --force`
- Delete `node_modules` and reinstall: `rm -rf node_modules && npm install`
- Ensure backend is running before starting frontend
- For E2E tests: Backend must be running on port 8080

**API Rate Limits:**
- Free Spoonacular API has daily limits (150 requests/day)
- Monitor your usage at https://spoonacular.com/food-api/console
- Use mock mode (spoonacular.mock-mode=true) during development to save quota

### Docker Deployment (Recommended)

The easiest way to run the application is using Docker Compose, which runs both backend and frontend in containers.

**Prerequisites:**
- Docker Desktop installed and running
- Docker Compose (included with Docker Desktop)

**Steps:**

1. **Clone the repository:**
   ```bash
   git clone https://github.com/itsrawan/RecipesBuddy.git
   cd RecipesBuddy
   ```

2. **Create environment file:**
   ```bash
   cp .env.example .env
   ```
   
3. **Add your Spoonacular API key to .env:**
   ```
   SPOONACULAR_API_KEY=your_actual_api_key_here
   ```

4. **Build and run the containers:**
   ```bash
   docker-compose up -d --build
   ```
   This command will:
   - Build both backend and frontend Docker images
   - Start the containers in detached mode
   - Create a network for container communication

5. **Access the application:**
   - Frontend: http://localhost
   - Backend API: http://localhost:8080

**Docker Commands:**

- **View running containers:**
  ```bash
  docker-compose ps
  ```

- **View logs:**
  ```bash
  docker-compose logs -f
  ```

- **Stop the application:**
  ```bash
  docker-compose down
  ```

- **Rebuild after code changes:**
  ```bash
  docker-compose up -d --build
  ```

- **Remove containers and volumes:**
  ```bash
  docker-compose down -v
  ```

**Architecture:**
- The frontend runs in an Nginx container on port 80
- The backend runs in a Java container on port 8080
- Nginx proxies API requests to the backend container
- Both containers communicate through a Docker network
