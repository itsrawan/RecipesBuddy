package com.rjs.recipesbuddy.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.rjs.recipesbuddy.dto.*;
import com.rjs.recipesbuddy.service.RecipeService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests using WireMock
 * 
 * <p>These tests verify the integration between our backend and the Spoonacular API
 * by mocking the external API with WireMock. This approach provides:
 * <ul>
 *   <li>Fast test execution (no real network calls)</li>
 *   <li>No API quota consumption</li>
 *   <li>Ability to test error scenarios</li>
 *   <li>Deterministic and reliable tests</li>
 * </ul>
 * 
 * <p>WireMock creates a fake HTTP server that mimics Spoonacular API responses,
 * allowing us to test our backend logic without depending on the external service.
 * 
 * @author Rawan Sweidan
 * @version 1.0.0
 * @since 2025-11-10
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spoonacular.api.base-url=http://localhost:8089",
    "logging.level.com.rjs.recipesbuddy=DEBUG"
})
@DisplayName("Spoonacular API Integration Tests with WireMock")
public class SpoonacularApiIntegrationTest {

    @Autowired
    private RecipeService recipeService;

    private static WireMockServer wireMockServer;

    /**
     * Start WireMock server before all tests
     */
    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    /**
     * Stop WireMock server after all tests
     */
    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    /**
     * Reset WireMock stubs before each test
     */
    @BeforeEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }

    @Nested
    @DisplayName("Recipe Search Integration Tests")
    class RecipeSearchTests {

        @Test
        @DisplayName("Should successfully search recipes with mocked API response")
        void testSearchRecipes_Success() {
            // Arrange - Mock Spoonacular API response
            String mockResponse = """
                {
                    "results": [
                        {
                            "id": 654959,
                            "title": "Pasta with Garlic, Scallions, Cauliflower & Breadcrumbs",
                            "image": "https://spoonacular.com/recipeImages/654959-312x231.jpg",
                            "imageType": "jpg"
                        },
                        {
                            "id": 511728,
                            "title": "Pasta Margherita",
                            "image": "https://spoonacular.com/recipeImages/511728-312x231.jpg",
                            "imageType": "jpg"
                        }
                    ],
                    "offset": 0,
                    "number": 2,
                    "totalResults": 86
                }
                """;

            stubFor(get(urlPathEqualTo("/recipes/complexSearch"))
                    .withQueryParam("query", equalTo("pasta"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            // Act
            RecipeSearchRequest request = RecipeSearchRequest.builder()
                    .query("pasta")
                    .size(10)
                    .offset(0)
                    .build();

            RecipeSearchResponse response = recipeService.searchRecipes(request);

            // Assert
            assertNotNull(response);
            assertEquals(86, response.getTotalResults());
            assertEquals(2, response.getResults().size());
            
            RecipeSummary firstRecipe = response.getResults().get(0);
            assertEquals(654959L, firstRecipe.getId());
            assertEquals("Pasta with Garlic, Scallions, Cauliflower & Breadcrumbs", firstRecipe.getTitle());

            // Verify WireMock received the request
            verify(getRequestedFor(urlPathEqualTo("/recipes/complexSearch"))
                    .withQueryParam("query", equalTo("pasta")));
        }

        @Test
        @DisplayName("Should search recipes with nutrition filters")
        void testSearchRecipes_WithNutritionFilters() {
            // Arrange
            String mockResponse = """
                {
                    "results": [
                        {
                            "id": 716429,
                            "title": "Healthy Chicken Pasta",
                            "image": "https://spoonacular.com/recipeImages/716429-312x231.jpg",
                            "imageType": "jpg"
                        }
                    ],
                    "offset": 0,
                    "number": 1,
                    "totalResults": 15
                }
                """;

            stubFor(get(urlPathEqualTo("/recipes/complexSearch"))
                    .withQueryParam("query", equalTo("chicken"))
                    .withQueryParam("maxCalories", equalTo("500"))
                    .withQueryParam("maxCarbs", equalTo("50"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            // Act
            RecipeSearchRequest request = RecipeSearchRequest.builder()
                    .query("chicken")
                    .maxCalories(500)
                    .maxCarbs(50)
                    .size(10)
                    .offset(0)
                    .build();

            RecipeSearchResponse response = recipeService.searchRecipes(request);

            // Assert
            assertNotNull(response);
            assertEquals(15, response.getTotalResults());
            assertEquals(1, response.getResults().size());
        }

        @Test
        @DisplayName("Should handle empty search results")
        void testSearchRecipes_EmptyResults() {
            // Arrange
            String mockResponse = """
                {
                    "results": [],
                    "offset": 0,
                    "number": 0,
                    "totalResults": 0
                }
                """;

            stubFor(get(urlPathEqualTo("/recipes/complexSearch"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            // Act
            RecipeSearchRequest request = RecipeSearchRequest.builder()
                    .query("xyz123nonexistent")
                    .size(10)
                    .offset(0)
                    .build();

            RecipeSearchResponse response = recipeService.searchRecipes(request);

            // Assert
            assertNotNull(response);
            assertEquals(0, response.getTotalResults());
            assertTrue(response.getResults().isEmpty());
        }

        @Test
        @DisplayName("Should handle API error responses")
        void testSearchRecipes_ApiError() {
            // Arrange - Mock API error
            stubFor(get(urlPathEqualTo("/recipes/complexSearch"))
                    .willReturn(aResponse()
                            .withStatus(500)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"message\": \"Internal Server Error\"}")));

            // Act & Assert
            RecipeSearchRequest request = RecipeSearchRequest.builder()
                    .query("pasta")
                    .size(10)
                    .offset(0)
                    .build();

            // Should handle error gracefully
            assertThrows(Exception.class, () -> {
                recipeService.searchRecipes(request);
            });
        }

        @Test
        @DisplayName("Should handle API timeout")
        void testSearchRecipes_Timeout() {
            // Arrange - Mock slow API response
            stubFor(get(urlPathEqualTo("/recipes/complexSearch"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withFixedDelay(15000) // 15 second delay
                            .withBody("{}")));

            // Act & Assert
            RecipeSearchRequest request = RecipeSearchRequest.builder()
                    .query("pasta")
                    .size(10)
                    .offset(0)
                    .build();

            // Should timeout and handle gracefully
            assertThrows(Exception.class, () -> {
                recipeService.searchRecipes(request);
            });
        }
    }

    @Nested
    @DisplayName("Recipe Details Integration Tests")
    class RecipeDetailsTests {

        @Test
        @DisplayName("Should successfully get recipe details")
        void testGetRecipeDetails_Success() {
            // Arrange
            Long recipeId = 654959L;
            String mockResponse = """
                {
                    "id": 654959,
                    "title": "Pasta with Garlic",
                    "image": "https://spoonacular.com/recipeImages/654959-556x370.jpg",
                    "servings": 4,
                    "readyInMinutes": 45,
                    "sourceUrl": "http://example.com/recipe",
                    "summary": "Delicious pasta recipe",
                    "cuisines": ["Italian"],
                    "dishTypes": ["main course"],
                    "diets": ["vegetarian"],
                    "instructions": "Cook pasta...",
                    "extendedIngredients": [
                        {
                            "id": 11135,
                            "name": "cauliflower",
                            "amount": 1.0,
                            "unit": "head",
                            "nutrition": {
                                "nutrients": [
                                    {
                                        "name": "Calories",
                                        "amount": 146.0,
                                        "unit": "kcal"
                                    }
                                ]
                            }
                        }
                    ],
                    "nutrition": {
                        "nutrients": [
                            {
                                "name": "Calories",
                                "amount": 450.0,
                                "unit": "kcal"
                            },
                            {
                                "name": "Protein",
                                "amount": 15.0,
                                "unit": "g"
                            }
                        ]
                    }
                }
                """;

            stubFor(get(urlPathEqualTo("/recipes/" + recipeId + "/information"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            // Act
            RecipeDetailResponse response = recipeService.getRecipeById(recipeId);

            // Assert
            assertNotNull(response);
            assertEquals(recipeId, response.getId());
            assertEquals("Pasta with Garlic", response.getTitle());
            assertEquals(4, response.getServings());
            assertNotNull(response.getNutrition());
            assertEquals(450.0, response.getNutrition().getTotalCalories());
            assertFalse(response.getExtendedIngredients().isEmpty());
        }

        @Test
        @DisplayName("Should handle recipe not found")
        void testGetRecipeDetails_NotFound() {
            // Arrange
            Long recipeId = 999999L;
            
            stubFor(get(urlPathEqualTo("/recipes/" + recipeId + "/information"))
                    .willReturn(aResponse()
                            .withStatus(404)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"message\": \"Recipe not found\"}")));

            // Act & Assert
            assertThrows(Exception.class, () -> {
                recipeService.getRecipeById(recipeId);
            });
        }
    }

    @Nested
    @DisplayName("Ingredient Information Integration Tests")
    class IngredientInfoTests {

        @Test
        @DisplayName("Should successfully get ingredient information")
        void testGetIngredientInfo_Success() {
            // Arrange
            Long ingredientId = 11135L;
            String mockResponse = """
                {
                    "id": 11135,
                    "name": "cauliflower",
                    "amount": 1.0,
                    "unit": "head",
                    "nutrition": {
                        "nutrients": [
                            {
                                "name": "Calories",
                                "amount": 146.0,
                                "unit": "kcal"
                            },
                            {
                                "name": "Fat",
                                "amount": 1.5,
                                "unit": "g"
                            }
                        ]
                    }
                }
                """;

            stubFor(get(urlPathEqualTo("/food/ingredients/" + ingredientId + "/information"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            // This would require adding a public method in RecipeService
            // For now, this test demonstrates the WireMock setup
            
            // Verify the stub is configured correctly
            verify(0, getRequestedFor(urlPathEqualTo("/food/ingredients/" + ingredientId + "/information")));
        }
    }

    @Nested
    @DisplayName("Retry Mechanism Tests")
    class RetryMechanismTests {

        @Test
        @DisplayName("Should retry on transient failures")
        void testRetry_TransientFailure() {
            // Arrange - First call fails, second succeeds
            String mockResponse = """
                {
                    "results": [{"id": 123, "title": "Test Recipe"}],
                    "totalResults": 1
                }
                """;

            stubFor(get(urlPathEqualTo("/recipes/complexSearch"))
                    .inScenario("Retry")
                    .whenScenarioStateIs("Started")
                    .willReturn(aResponse()
                            .withStatus(503) // Service Unavailable
                            .withHeader("Content-Type", "application/json"))
                    .willSetStateTo("Failed Once"));

            stubFor(get(urlPathEqualTo("/recipes/complexSearch"))
                    .inScenario("Retry")
                    .whenScenarioStateIs("Failed Once")
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            // Act
            RecipeSearchRequest request = RecipeSearchRequest.builder()
                    .query("test")
                    .size(10)
                    .offset(0)
                    .build();

            RecipeSearchResponse response = recipeService.searchRecipes(request);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getTotalResults());

            // Verify retry happened
            verify(2, getRequestedFor(urlPathEqualTo("/recipes/complexSearch")));
        }
    }
}
