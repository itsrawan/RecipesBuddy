# RecipesBuddy - Testing Guide

## Overview

This guide provides comprehensive information about testing the RecipesBuddy backend application, including unit tests, integration tests, and how to work with mocked Spoonacular API responses during development.

**Author:** Rawan Sweidan  
**Version:** 1.0.0  
**Date:** November 2025

## Current Test Coverage

### Existing Tests

| Component | Test File | Status |
|-----------|-----------|--------|
| RecipeService | RecipeServiceTest.java | ✅ Comprehensive |
| RecipeController | RecipeControllerTest.java | ✅ Good |
| Application Context | RecipesBuddyApplicationTests.java | ✅ Basic |
| SpoonacularApiService | ❌ No tests | ⚠️ **Needs Creation** |
| Configuration Classes | ❌ No tests | ⚠️ **Needs Creation** |
| Exception Handlers | ❌ No dedicated tests | ⚠️ **Covered indirectly** |
| DTOs | ❌ No tests | ⚠️ **Simple POJOs** |

### Test Statistics

**Total Test Classes:** 3  
**Total Test Methods:** ~15  
**Components with Tests:** 3/7  
**Coverage Status:** Partial - Core business logic covered

## Testing Strategy

### Test Pyramid

```
                  /\
                 /  \
                / E2E\ (Playwright - Frontend)
               /______\
              /        \
             /Integration\ (Spring Boot Test)
            /____________\
           /              \
          /   Unit Tests   \ (JUnit + Mockito)
         /__________________\
```

## Unit Tests

### Location
```
backend/src/test/java/com/rjs/recipesbuddy/
```

### 1. RecipeServiceTest

**Purpose:** Tests the core business logic in RecipeService using mocked dependencies.

**Coverage:**
- ✅ Recipe search with various parameters
- ✅ Recipe search with nutrition filters
- ✅ Recipe search with ingredient exclusions
- ✅ Recipe retrieval by ID (success case)
- ✅ Recipe retrieval when not found
- ✅ Calorie calculations with single exclusion
- ✅ Calorie calculations with multiple exclusions
- ✅ Calorie calculations with no exclusions
- ✅ Edge cases (no ingredients, no calories, null responses)

**Test Structure:**
```java
@Nested
@DisplayName("Search Recipes Tests")
class SearchRecipesTests { }

@Nested
@DisplayName("Get Recipe By ID Tests")
class GetRecipeByIdTests { }

@Nested
@DisplayName("Calculate Updated Calories Tests")
class CalculateUpdatedCaloriesTests { }
```

**Example Test:**
```java
@Test
@DisplayName("Should calculate updated calories when excluding single ingredient")
void testCalculateUpdatedCalories_SingleExclusion() {
    // Arrange
    when(spoonacularApiService.getRecipeDetails(recipeId))
            .thenReturn(mockRecipe);
    when(spoonacularApiService.getIngredientInformation(eq(1L), anyDouble(), anyString()))
            .thenReturn(ingredient1);
    
    // Act
    CalorieUpdateResponse response = recipeService.calculateUpdatedCalories(
            recipeId, excludedIds);
    
    // Assert
    assertEquals(350.0, response.getOriginalCalories());
    assertEquals(150.0, response.getUpdatedCalories());
    verify(spoonacularApiService).getRecipeDetails(recipeId);
}
```

### 2. RecipeControllerTest

**Purpose:** Tests REST endpoints using MockMvc and mocked services.

**Coverage:**
- ✅ Search endpoint with basic query
- ✅ Get recipe by ID (success case)
- ✅ Get recipe by ID (not found case)
- ✅ Calculate updated calories endpoint

**Example Test:**
```java
@Test
void testSearchRecipes_Success() throws Exception {
    when(recipeService.searchRecipes(any())).thenReturn(response);
    
    mockMvc.perform(get("/api/recipes/search")
                    .param("query", "pasta")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalResults").value(0));
}
```

### 3. RecipesBuddyApplicationTests

**Purpose:** Verifies Spring application context loads successfully.

**Coverage:**
- ✅ Application context loads
- ✅ All beans are properly configured

## Tests Needed (Future Work)

### High Priority

#### 1. SpoonacularApiService Tests

**Why:** This service directly interacts with external API and handles critical operations.

**Suggested Tests:**
```java
@ExtendWith(MockitoExtension.class)
class SpoonacularApiServiceTest {
    
    @Mock
    private WebClient webClient;
    
    @Test
    void testSearchRecipes_Success() { }
    
    @Test
    void testSearchRecipes_ApiError() { }
    
    @Test
    void testGetRecipeDetails_Success() { }
    
    @Test
    void testGetRecipeDetails_NotFound() { }
    
    @Test
    void testRetryMechanism() { }
    
    @Test
    void testReadStaticSearchJson() { }
}
```

#### 2. Configuration Tests

**WebClientConfig Tests:**
```java
class WebClientConfigTest {
    
    @Test
    void testWebClientCreation() { }
    
    @Test
    void testApiKeyLoadedFromEnvironment() { }
    
    @Test
    void testConnectionTimeoutConfiguration() { }
    
    @Test
    void testMissingApiKeyThrowsException() { }
}
```

**CorsConfig Tests:**
```java
class CorsConfigTest {
    
    @Test
    void testCorsFilterCreation() { }
    
    @Test
    void testAllowedOriginsConfiguration() { }
    
    @Test
    void testAllowedMethodsConfiguration() { }
}
```

### Medium Priority

#### 3. Exception Handler Tests

While exception handling is tested indirectly through controller tests, dedicated tests would be beneficial:

```java
class GlobalExceptionHandlerTest {
    
    @Test
    void testRecipeNotFoundException() { }
    
    @Test
    void testSpoonacularApiException() { }
    
    @Test
    void testValidationException() { }
    
    @Test
    void testGenericException() { }
}
```

### Low Priority

#### 4. DTO Tests

DTOs are simple POJOs and generally don't need extensive testing unless they contain complex logic. Consider testing:
- Builder pattern functionality
- Lombok-generated code (if issues arise)

## Mock Data Strategy

### Development Mode

The application supports using mock responses during development to:
- Avoid consuming API quota (150 requests/day on free tier)
- Enable offline development
- Provide consistent test data
- Speed up development

### Mock Response Location

```
backend/src/main/resources/com/rjs/recipesbuddy/service/static_search_response.json
```

### Switching Between Mock and Live Data

**Current Implementation (Mock Mode - for Development):**
```java
// In SpoonacularApiService.java - searchRecipes() method
String jsonString = this.readStaticSearchJson("static_search_response.json");
RecipeSearchResponse response = this.deserializeSearchResponse(jsonString);
```

**Production Mode (Live API - Comment out mock, uncomment this):**
```java
RecipeSearchResponse response = spoonacularWebClient
    .get()
    .uri(uriBuilder.toString())
    .retrieve()
    .bodyToMono(RecipeSearchResponse.class)
    .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(1)))
    .block();
```

### Creating Custom Mock Responses

1. **Capture Real Response:**
```bash
curl "https://api.spoonacular.com/recipes/complexSearch?query=pasta&apiKey=YOUR_KEY" > mock_response.json
```

2. **Format and Save:**
```
backend/src/main/resources/com/rjs/recipesbuddy/service/custom_mock.json
```

3. **Load in Code:**
```java
String jsonString = readStaticSearchJson("custom_mock.json");
```

## Running Tests

### Command Line

**Run all tests:**
```bash
cd backend
./mvnw test
```

**Run specific test class:**
```bash
./mvnw test -Dtest=RecipeServiceTest
```

**Run specific test method:**
```bash
./mvnw test -Dtest=RecipeServiceTest#testSearchRecipes_Basic
```

**Run tests with verbose output:**
```bash
./mvnw test -X
```

**Skip tests during build:**
```bash
./mvnw clean install -DskipTests
```

### IDE (IntelliJ IDEA / Eclipse)

1. Right-click on test class/method
2. Select "Run 'TestName'"
3. View results in test runner panel
4. Use coverage tool for code coverage analysis

### Windows Command Prompt

```cmd
cd backend
mvnw.cmd test
```

### Expected Output

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.rjs.recipesbuddy.RecipesBuddyApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.rjs.recipesbuddy.controller.RecipeControllerTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.rjs.recipesbuddy.service.RecipeServiceTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

## Test Coverage Reports

### Generating Coverage with JaCoCo

1. **Add JaCoCo Plugin to pom.xml:**
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

2. **Run Tests with Coverage:**
```bash
./mvnw test jacoco:report
```

3. **View Report:**
```
backend/target/site/jacoco/index.html
```

## Best Practices

### 1. Test Naming Conventions

```java
// ❌ Bad - Unclear what is being tested
@Test
void test1() { }

// ✅ Good - Clear and descriptive
@Test
@DisplayName("Should throw RecipeNotFoundException when recipe not found")
void testGetRecipeById_NotFound() { }
```

### 2. Arrange-Act-Assert Pattern

```java
@Test
void testExample() {
    // Arrange - Set up test data and mocks
    RecipeSearchRequest request = createRequest();
    when(service.search(any())).thenReturn(response);
    
    // Act - Execute the method being tested
    RecipeSearchResponse result = service.searchRecipes(request);
    
    // Assert - Verify the results
    assertNotNull(result);
    assertEquals(expected, result.getTotalResults());
}
```

### 3. One Assertion Per Test (When Possible)

```java
// ❌ Testing multiple things makes debugging harder
@Test
void testRecipe() {
    assertEquals(123L, recipe.getId());
    assertEquals("Title", recipe.getTitle());
    assertNotNull(recipe.getNutrition());
    assertTrue(recipe.getServings() > 0);
}

// ✅ Separate tests for clarity
@Test
void testRecipeId() {
    assertEquals(123L, recipe.getId());
}

@Test
void testRecipeTitle() {
    assertEquals("Title", recipe.getTitle());
}
```

### 4. Use Descriptive Mock Data

```java
// ❌ Magic numbers
Ingredient ingredient = Ingredient.builder()
        .id(1L)
        .amount(100.0)
        .build();

// ✅ Clear constants
private static final Long CHEESE_ID = 1L;
private static final Double CHEESE_AMOUNT_GRAMS = 100.0;

Ingredient cheese = Ingredient.builder()
        .id(CHEESE_ID)
        .name("Cheese")
        .amount(CHEESE_AMOUNT_GRAMS)
        .unit("g")
        .build();
```

### 5. Test Edge Cases

Always test:
- ✅ Null values
- ✅ Empty collections
- ✅ Boundary values (0, negative numbers, max values)
- ✅ Error conditions
- ✅ Concurrent scenarios (if applicable)

### 6. Keep Tests Independent

```java
// ❌ Tests depending on execution order
private static Recipe sharedRecipe;

@Test
void test1_CreateRecipe() {
    sharedRecipe = new Recipe();
}

@Test
void test2_UseRecipe() {
    // Fails if test1 doesn't run first
    assertNotNull(sharedRecipe);
}

// ✅ Each test is independent
@BeforeEach
void setUp() {
    recipe = createRecipe();
}

@Test
void testRecipe() {
    assertNotNull(recipe);
}
```

### 7. Use MockMvc for Controller Tests

```java
@WebMvcTest(RecipeController.class)
class RecipeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private RecipeService recipeService;
    
    @Test
    void testEndpoint() throws Exception {
        mockMvc.perform(get("/api/recipes/search")
                .param("query", "pasta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").exists());
    }
}
```

## Troubleshooting

### Common Issues

**1. Tests Pass Locally But Fail in CI**
- Verify environment variables are set
- Check for timezone/locale dependencies
- Ensure test data isolation

**2. Mockito Mocks Not Working**
- Verify `@ExtendWith(MockitoExtension.class)` annotation
- Check mock initialization in `@BeforeEach`
- Ensure using correct argument matchers

**3. JsonPath Assertions Failing**
- Check JSON response structure
- Verify field names match exactly (case-sensitive)
- Use online JSON path evaluators for testing

**4. WebClient Mocking Issues**
- WebClient is complex to mock
- Consider using WireMock for integration tests
- Or keep using mock JSON responses as currently implemented

## Integration with CI/CD

### GitHub Actions Example

```yaml
name: Backend Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Run tests
      run: |
        cd backend
        ./mvnw test
```

## Future Enhancements

### Priority 1: Add Missing Tests
- [ ] SpoonacularApiService unit tests
- [ ] WebClientConfig tests
- [ ] CorsConfig tests
- [ ] GlobalExceptionHandler tests

### Priority 2: Integration Tests
- [ ] End-to-end request/response tests
- [ ] WebClient integration with WireMock
- [ ] Database integration tests (when DB added)

### Priority 3: Advanced Testing
- [ ] Performance/load testing
- [ ] Contract testing with frontend
- [ ] Mutation testing with PIT
- [ ] Security testing

## Resources

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [MockMvc Documentation](https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html)

## Contact

For questions about testing:

**Developer:** Rawan Sweidan  
**Project:** RecipesBuddy  
**Date:** November 2025
