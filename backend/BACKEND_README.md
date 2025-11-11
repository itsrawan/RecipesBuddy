# RecipesBuddy Backend Documentation

## Overview

The RecipesBuddy backend is a Spring Boot application that serves as a middleware layer between the React frontend and the Spoonacular Food API. It provides RESTful endpoints for recipe search, detailed recipe information, and calorie calculations.

**Author:** Rawan Sweidan  
**Version:** 1.0.0  
**Spring Boot Version:** 3.5.7  
**Java Version:** 17

## Architecture

```
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│  React Frontend │ ◄─────► │ Spring Boot API │ ◄─────► │  Spoonacular    │
│   (Port 5173)   │   HTTP  │   (Port 8080)   │   HTTP  │      API        │
└─────────────────┘         └─────────────────┘         └─────────────────┘
```

### Key Design Principles

1. **Separation of Concerns**: Clear separation between Controller, Service, and API layers
2. **Security First**: API keys never exposed to frontend
3. **Error Handling**: Comprehensive exception handling with meaningful error messages
4. **Validation**: Input validation at controller level using Jakarta Bean Validation
5. **Resilience**: Retry mechanism for transient API failures

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/rjs/recipesbuddy/
│   │   │   ├── RecipesBuddyApplication.java      # Main application entry point
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java                # CORS configuration
│   │   │   │   └── WebClientConfig.java           # WebClient setup for API calls
│   │   │   ├── controller/
│   │   │   │   └── RecipeController.java          # REST API endpoints
│   │   │   ├── dto/
│   │   │   │   ├── ApiErrorResponse.java          # Error response structure
│   │   │   │   ├── CalorieUpdateRequest.java      # Calorie update request
│   │   │   │   ├── CalorieUpdateResponse.java     # Calorie update response
│   │   │   │   ├── Ingredient.java                # Ingredient data model
│   │   │   │   ├── Nutrient.java                  # Nutrient data model
│   │   │   │   ├── NutritionInfo.java             # Nutrition information
│   │   │   │   ├── RecipeDetailResponse.java      # Detailed recipe response
│   │   │   │   ├── RecipeSearchRequest.java       # Search request parameters
│   │   │   │   ├── RecipeSearchResponse.java      # Search results response
│   │   │   │   └── RecipeSummary.java             # Recipe summary model
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java    # Centralized exception handling
│   │   │   │   ├── RecipeNotFoundException.java   # Recipe not found exception
│   │   │   │   └── SpoonacularApiException.java   # API error exception
│   │   │   ├── service/
│   │   │   │   ├── RecipeService.java             # Business logic layer
│   │   │   │   └── SpoonacularApiService.java     # External API integration
│   │   │   └── util/
│   │   │       └── ValueValidator.java            # Validation utilities
│   │   └── resources/
│   │       ├── application.properties             # Application configuration
│   │       └── com/rjs/recipesbuddy/service/
│   │           └── static_search_response.json    # Mock API response
│   └── test/
│       └── java/com/rjs/recipesbuddy/
│           ├── RecipesBuddyApplicationTests.java  # Application context tests
│           ├── controller/
│           │   └── RecipeControllerTest.java      # Controller unit tests
│           └── service/
│               └── RecipeServiceTest.java         # Service unit tests
├── Dockerfile                                      # Docker container configuration
├── .dockerignore                                   # Docker ignore patterns
└── pom.xml                                         # Maven dependencies
```

## API Endpoints

### Base URL
```
http://localhost:8080/api/recipes
```

### Endpoints

#### 1. Search Recipes
```
GET /api/recipes/search
```

**Parameters:**
- `query` (required): Search query (recipe name or keywords)
- `excludeIngredients` (optional): Comma-separated ingredients to exclude
- `includeIngredients` (optional): Comma-separated ingredients that must be included
- `maxCalories` (optional): Maximum calories per serving (0-5000)
- `maxCarbs` (optional): Maximum carbohydrates in grams (0-5000)
- `minProtein` (optional): Minimum protein in grams (0-5000)
- `maxFat` (optional): Maximum fat in grams (0-5000)
- `size` (optional, default: 12): Number of results per page
- `offset` (optional, default: 0): Number of results to skip (pagination)

**Example:**
```bash
curl "http://localhost:8080/api/recipes/search?query=pasta&maxCalories=500&size=10"
```

**Response:**
```json
{
  "results": [
    {
      "id": 654959,
      "title": "Pasta With Tuna",
      "image": "https://spoonacular.com/recipeImages/654959-312x231.jpg",
      "nutrition": {
        "totalCalories": 450.5,
        "nutrients": [...]
      }
    }
  ],
  "totalResults": 42,
  "offset": 0,
  "number": 10
}
```

#### 2. Get Recipe Details
```
GET /api/recipes/{id}
```

**Parameters:**
- `id` (path, required): Recipe ID (minimum: 1)

**Example:**
```bash
curl "http://localhost:8080/api/recipes/654959"
```

**Response:**
```json
{
  "id": 654959,
  "title": "Pasta With Tuna",
  "image": "https://spoonacular.com/recipeImages/654959-556x370.jpg",
  "readyInMinutes": 45,
  "servings": 4,
  "extendedIngredients": [...],
  "instructions": "...",
  "nutrition": {
    "totalCalories": 450.5,
    "nutrients": [...]
  }
}
```

#### 3. Calculate Updated Calories
```
POST /api/recipes/{id}/calories
```

**Parameters:**
- `id` (path, required): Recipe ID

**Request Body:**
```json
{
  "recipeId": 654959,
  "excludedIngredientIds": [1123, 5456]
}
```

**Response:**
```json
{
  "recipeId": 654959,
  "originalCalories": 450.5,
  "updatedCalories": 320.0,
  "caloriesReduced": 130.5,
  "ingredientsExcluded": 2
}
```

### Health Check Endpoints (Actuator)

Spring Boot Actuator provides built-in monitoring endpoints:

```
GET /actuator/health    - Application health status
GET /actuator/info      - Application information
GET /actuator/metrics   - Application metrics
```

## Configuration

### Environment Variables

**Required:**
- `SPOONACULAR_API_KEY`: Your Spoonacular API key (get from https://spoonacular.com/food-api)

**Optional:**
- `SERVER_PORT`: Server port (default: 8080)
- `SPOONACULAR_API_BASE_URL`: Spoonacular API base URL (default: https://api.spoonacular.com)

### Application Properties

Key configurations in `application.properties`:

```properties
# Server
server.port=8080

# Spoonacular API
spoonacular.api.base-url=https://api.spoonacular.com
spoonacular.api.auth.header-name=x-api-key

# WebClient Timeouts
webclient.connection-timeout=5000
webclient.read-timeout=10000

# CORS
cors.allowed-origins=http://localhost:3000,http://localhost:5173

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
```

## Security Features

### 1. API Key Management
- API key stored securely in environment variables
- Never exposed to frontend or in responses
- Injected via WebClientConfig

### 2. CORS Configuration
- Explicitly configured allowed origins
- Prevents unauthorized cross-origin requests
- Configurable via application.properties

### 3. Input Validation
- Jakarta Bean Validation on all inputs
- Parameter constraints (min/max values)
- Request body validation

### 4. Error Handling
- No sensitive information in error responses
- Consistent error response format
- Proper HTTP status codes

## Development Features

### Mock Response Support

For development and testing, the application can use mock responses to avoid consuming API quota:

```java
// In SpoonacularApiService.java
// Current: Uses mock response
String jsonString = this.readStaticSearchJson("static_search_response.json");
RecipeSearchResponse response = this.deserializeSearchResponse(jsonString);

// Production: Uncomment to use live API
// RecipeSearchResponse response = spoonacularWebClient
//     .get()
//     .uri(uriBuilder.toString())
//     .retrieve()
//     .bodyToMono(RecipeSearchResponse.class)
//     .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(1)))
//     .block();
```

### Retry Mechanism

The application automatically retries failed API calls:
- 2 retry attempts
- 1-second delay between retries
- Helps handle transient network issues

## Testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=RecipeServiceTest

# Run with coverage
./mvnw test jacoco:report
```

### Test Structure

1. **Unit Tests**
   - `RecipeServiceTest`: Tests business logic with mocked dependencies
   - `RecipeControllerTest`: Tests REST endpoints with MockMvc

2. **Integration Tests**
   - `RecipesBuddyApplicationTests`: Verifies application context loads

### Test Configuration

Test-specific configuration in `src/test/resources/application-test.properties`

## Building and Running

### Local Development

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Production Build

```bash
# Create executable JAR
./mvnw clean package

# Run the JAR
java -jar target/recipesbuddy-0.0.1-SNAPSHOT.jar
```

### Docker

```bash
# Build Docker image
docker build -t recipes-buddy-backend .

# Run container
docker run -p 8080:8080 -e SPOONACULAR_API_KEY=your_key recipes-buddy-backend
```

## Troubleshooting

### Common Issues

**1. Port Already in Use**
```
Error: Port 8080 is already in use
Solution: Kill the process or change the port in application.properties
```

**2. API Key Invalid**
```
Error: 401 Unauthorized from Spoonacular API
Solution: Verify SPOONACULAR_API_KEY environment variable is set correctly
```

**3. Connection Timeout**
```
Error: WebClientRequestException - Connection timeout
Solution: Check internet connection and increase timeout values
```

**4. Rate Limit Exceeded**
```
Error: 402 Payment Required from Spoonacular API
Solution: Free tier limit reached (150 requests/day). Wait or upgrade plan.
```

### Debugging

Enable detailed logging:
```properties
logging.level.com.rjs.recipesbuddy=DEBUG
logging.level.org.springframework.web.reactive.function.client=DEBUG
```

## Performance Considerations

### API Rate Limiting

Spoonacular free tier allows 150 requests per day. Consider:
- Implementing caching for frequently requested recipes
- Using mock responses during development
- Upgrading to paid tier for production

### Optimization Tips

1. **Caching**: Implement Spring Cache for frequently accessed data
2. **Connection Pooling**: WebClient uses connection pooling by default
3. **Async Processing**: Consider CompletableFuture for multiple API calls
4. **Database**: Add database layer for caching recipe data

## Future Enhancements

1. **Database Integration**: Add PostgreSQL/MySQL for data persistence
2. **Caching Layer**: Implement Redis for API response caching
3. **Rate Limiting**: Add rate limiting for frontend requests
4. **Authentication**: Implement JWT-based user authentication
5. **Favorites**: Add user favorite recipes feature
6. **Search History**: Track and suggest recent searches
7. **Batch Processing**: Bulk recipe imports
8. **GraphQL**: Alternative API using GraphQL

## Dependencies

### Core Dependencies
- Spring Boot 3.5.7
- Spring Web (REST API)
- Spring WebFlux (Reactive HTTP client)
- Spring Validation (Input validation)
- Spring Boot Actuator (Monitoring)

### Development Dependencies
- Lombok (Reduces boilerplate)
- Spring Boot DevTools (Hot reload)

### Testing Dependencies
- Spring Boot Test
- JUnit 5
- Mockito
- MockMvc

## Contributing

When contributing to the backend:

1. Follow existing code structure
2. Add Javadoc comments for public methods
3. Write unit tests for new features
4. Update this documentation
5. Follow Spring Boot best practices
6. Keep security in mind

## License

This project is developed as part of a technical assessment and is intended for educational purposes.

## Contact

**Developer:** Rawan Sweidan  
**Project:** RecipesBuddy  
**Date:** November 2025
