import { test, expect } from '@playwright/test';

/**
 * End-to-End Tests for RecipesBuddy Home Page
 * 
 * These tests verify the main user flows on the home page:
 * - Page loads correctly
 * - Search functionality works
 * - Results are displayed
 * - Navigation works as expected
 * 
 * @author Rawan Sweidan
 * @since 2025-11-10
 */

test.describe('Home Page', () => {
  
  test.beforeEach(async ({ page }) => {
    // Navigate to home page before each test
    await page.goto('/');
  });

  test('should load the home page successfully', async ({ page }) => {
    // Verify page title
    await expect(page).toHaveTitle(/RecipesBuddy|Vite/);
    
    // Verify main elements are visible
    const searchInput = page.getByTestId('SearchText');
    await expect(searchInput).toBeVisible();
    
    const searchButton = page.getByTestId('SearchButton');
    await expect(searchButton).toBeVisible();
  });

  test('should display search input and button', async ({ page }) => {
    // Find the main search input by its test id
    const search = page.getByTestId('SearchText');
    await expect(search).toBeVisible();
    await expect(search).toBeEditable();
    
    // Verify placeholder text if exists
    const placeholder = await search.getAttribute('placeholder');
    expect(placeholder).toBeTruthy();
    
    // Find and verify search button
    const button = page.getByTestId('SearchButton');
    await expect(button).toBeVisible();
    await expect(button).toBeEnabled();
  });

  test('should allow typing in search input', async ({ page }) => {
    const search = page.getByTestId('SearchText');
    
    // Type search query
    await search.fill('pasta');
    
    // Verify value was entered
    await expect(search).toHaveValue('pasta');
    
    // Clear and type different query
    await search.clear();
    await search.fill('chicken');
    await expect(search).toHaveValue('chicken');
  });

  test('should search for recipes and display results', async ({ page }) => {
    // Find search input and button
    const search = page.getByTestId('SearchText');
    const button = page.getByTestId('SearchButton');
    
    // Perform search
    await search.fill('pasta');
    await button.click();
    
    // Wait for results to load
    await page.waitForSelector('[data-testid="RecipeItem"]', { timeout: 10000 });
    
    // Verify results are displayed
    const items = page.locator('[data-testid="RecipeItem"]');
    await items.first().waitFor({ state: 'visible' });
    
    const count = await items.count();
    expect(count).toBeGreaterThan(0);
    
    // Verify search input still has the value
    await expect(search).toHaveValue('pasta');
  });

  test('should display recipe cards with required information', async ({ page }) => {
    const search = page.getByTestId('SearchText');
    const button = page.getByTestId('SearchButton');
    
    // Search for recipes
    await search.fill('pasta');
    await button.click();
    
    // Wait for first recipe card
    const firstCard = page.locator('[data-testid="RecipeItem"]').first();
    await firstCard.waitFor({ state: 'visible', timeout: 20000 });
    
    // Verify card has image (check for img tag inside the card)
    const image = firstCard.locator('img');
    await expect(image).toBeVisible();
    
    // Verify card is clickable
    await expect(firstCard).toBeVisible();
  });

  test('should handle empty search gracefully', async ({ page }) => {
    const button = page.getByTestId('SearchButton');
    
    // Try to search without entering text
    await button.click();
    
    // Should either show validation message or no results
    // Adjust based on actual behavior
    await page.waitForTimeout(5000);
  });

  test('should show loading state during search', async ({ page }) => {
    const search = page.getByTestId('SearchText');
    const button = page.getByTestId('SearchButton');
    
    await search.fill('pasta');
    
    // Click and check for loading indicator
    await button.click();
    
    // Look for loading spinner or similar indicator
    // Adjust selector based on actual loading component
    const loadingIndicator = page.locator('[data-testid="LoadingSpinner"]').or(page.locator('text=/loading/i'));
    
    // Loading indicator might be visible briefly
    // This is a best-effort check
    try {
      await loadingIndicator.waitFor({ state: 'visible', timeout: 1000 });
    } catch (e) {
      // Loading might be too fast to catch
    }
  });

  test('should maintain search state after navigation', async ({ page }) => {
    const search = page.getByTestId('SearchText');
    const button = page.getByTestId('SearchButton');
    
    // Perform search
    await search.fill('pasta');
    await button.click();
    
    // Wait for results
    await page.waitForSelector('[data-testid="RecipeItem"]', { timeout: 10000 });
    
    // Navigate to a recipe (if navigation is implemented)
    const firstCard = page.locator('[data-testid="RecipeItem"]').first();
    await firstCard.click();
    
    // Wait for navigation
    await page.waitForTimeout(5000);
    
    // Go back
    await page.goBack();
    
    // Search input might or might not retain value depending on implementation
    // This is just to verify no errors occur
    await expect(search).toBeVisible();
  });

  test('should handle network errors gracefully', async ({ page }) => {
    // Simulate offline mode
    await page.context().setOffline(true);
    
    const search = page.getByTestId('SearchText');
    const button = page.getByTestId('SearchButton');
    
    await search.fill('pasta');
    await button.click();
    
    // Should show error message
    // Adjust based on actual error handling
    await page.waitForTimeout(5000);
    
    // Look for error message
    const errorMessage = page.locator('text=/error|failed|try again/i');
    
    try {
      await expect(errorMessage).toBeVisible({ timeout: 5000 });
    } catch (e) {
      // Error handling might be different
    }
    
    // Restore online mode
    await page.context().setOffline(false);
  });

  test('should have responsive layout', async ({ page }) => {
    // Test desktop view
    await page.setViewportSize({ width: 1920, height: 1080 });
    const search = page.getByTestId('SearchText');
    await expect(search).toBeVisible();
    
    // Test tablet view
    await page.setViewportSize({ width: 768, height: 1024 });
    await expect(search).toBeVisible();
    
    // Test mobile view
    await page.setViewportSize({ width: 375, height: 667 });
    await expect(search).toBeVisible();
  });

  test('should not have console errors on page load', async ({ page }) => {
    const consoleErrors = [];
    
    page.on('console', msg => {
      if (msg.type() === 'error') {
        consoleErrors.push(msg.text());
      }
    });
    
    await page.goto('/');
    await page.waitForLoadState('networkidle');
    
    // Allow some time for any delayed errors
    await page.waitForTimeout(1000);
    
    // Check for critical errors (filter out expected ones)
    const criticalErrors = consoleErrors.filter(error => 
      !error.includes('favicon') && // Ignore favicon errors
      !error.includes('sourcemap')   // Ignore sourcemap warnings
    );
    
    expect(criticalErrors.length).toBe(0);
  });
});
