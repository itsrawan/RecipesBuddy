package com.rjs.recipesbuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RecipesBuddy Application - Main Entry Point
 * 
 * <p>This is a full-stack web application that allows users to search for recipes
 * and view detailed nutritional information using the Spoonacular API.
 * 
 * <p>The application architecture consists of:
 * <ul>
 *   <li>Spring Boot backend (middleware) - Handles API requests and business logic</li>
 *   <li>React frontend - User interface for searching and viewing recipes</li>
 *   <li>Spoonacular API - External recipe and nutrition data source</li>
 * </ul>
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Search recipes with various filters (ingredients, nutrition values)</li>
 *   <li>View detailed recipe information including ingredients and instructions</li>
 *   <li>Calculate updated calories by excluding specific ingredients</li>
 *   <li>Secure API key management (never exposed to frontend)</li>
 * </ul>
 * 
 * <p>Configuration:
 * <ul>
 *   <li>Server runs on port 8080 (configurable in application.properties)</li>
 *   <li>Requires SPOONACULAR_API_KEY environment variable</li>
 *   <li>CORS enabled for frontend development (localhost:3000, localhost:5173)</li>
 * </ul>
 * 
 * @author Rawan Sweidan
 * @version 1.0.0
 * @since 2025-11-10
 */
@SpringBootApplication
public class RecipesBuddyApplication {

	/**
	 * Main method - Application entry point
	 * 
	 * <p>Initializes and starts the Spring Boot application with embedded Tomcat server.
	 * Loads configuration from application.properties and environment variables.
	 * 
	 * @param args Command line arguments (not currently used)
	 */
	public static void main(String[] args) {
		SpringApplication.run(RecipesBuddyApplication.class, args);
	}

}
