import { defineConfig, devices } from '@playwright/test';
import { fileURLToPath } from 'url';
import { dirname } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

/**
 * Playwright End-to-End Testing Configuration
 * 
 * This configuration sets up E2E tests for the RecipesBuddy application.
 * It includes multiple reporters for different use cases:
 * - HTML: Interactive report with screenshots and traces
 * - JSON: Machine-readable results for CI/CD integration
 * - List: Console output during test execution
 * 
 * @author Rawan Sweidan
 * @since 2025-11-10
 */
export default defineConfig({
  testDir: 'tests',
  timeout: 30000,
  expect: { timeout: 5000 },
  fullyParallel: true,
  
  // Multiple reporters for comprehensive test results
  reporter: [
    ['list'],
    ['html', { 
      outputFolder: 'playwright-report',
      open: 'never' // Don't auto-open report after tests
    }],
    ['json', { 
      outputFile: 'test-results/results.json' 
    }],
    ['junit', { 
      outputFile: 'test-results/junit.xml' 
    }]
  ],
  
  use: {
    baseURL: 'http://localhost:5173',
    headless: true,
    viewport: { width: 1280, height: 720 },
    actionTimeout: 0,
    
    // Enhanced tracing and debugging
    trace: 'retain-on-failure', // Keep traces for failed tests
    video: 'retain-on-failure', // Keep videos for failed tests
    screenshot: 'only-on-failure', // Screenshots on failure
    
    // Browser context options
    acceptDownloads: false,
    ignoreHTTPSErrors: true, // For development
    
    // Additional context for debugging
    contextOptions: {
      recordVideo: {
        dir: 'test-results/videos/',
        size: { width: 1280, height: 720 }
      }
    }
  },
  
  // Configure projects for different browsers (optional)
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    }
    // Uncomment to test on multiple browsers
    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] }
    // },
    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] }
    // }
  ],
  
  webServer: {
    command: 'npm run dev',
    cwd: __dirname,
    port: 5173,
    reuseExistingServer: true,
    timeout: 120000
  }
});
