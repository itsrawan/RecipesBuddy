import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import HomePage from './pages/HomePage';
import RecipeDetailPage from './pages/RecipeDetailPage';

/**
 * App Component
 * 
 * Root component that sets up:
 * - React Router for client-side routing
 * - React Query for data fetching and caching
 * - Application routes
 * 
 * Routes:
 * - / : Home page with recipe search
 * - /recipe/:id : Recipe details page
 * 
 * React Query Configuration:
 * - No refetch on window focus
 * - Single retry on failure
 * - 5-minute cache lifetime
 * 
 * @author Rawan Sweidan
 * @since 2025-11-10
 */

// Create a query client with optimized defaults
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 5 * 60 * 1000, // 5 minutes
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/recipe/:id" element={<RecipeDetailPage />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
