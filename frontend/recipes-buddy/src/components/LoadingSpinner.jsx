import { Loader2 } from 'lucide-react';

/**
 * Component: LoadingSpinner
 *
 * Simple, reusable loading spinner shown during async operations.
 *
 * Responsibilities:
 *  - Render an accessible spinner with an optional message.
 *  - Keep styling lightweight and tailwind-friendly for use across pages.
 *
 * Props:
 *  - message?: string — optional label shown beside the spinner.
 *
 * Notes:
 *  - Use for short waits; for longer operations consider skeleton screens.
 *
 * @author Rawan Sweidan
 * @since 2025-11-10
 */

const LoadingSpinner = ({ message = 'Loading...' }) => {
  return (
    <div className="flex flex-col items-center justify-center min-h-[400px]">
      <Loader2 className="w-12 h-12 text-primary-600 animate-spin mb-4" />
      <p className="text-gray-600 text-lg">{message}</p>
    </div>
  );
};

export default LoadingSpinner;