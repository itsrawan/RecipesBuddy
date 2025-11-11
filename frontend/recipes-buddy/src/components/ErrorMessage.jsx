import { AlertCircle } from 'lucide-react';

/**
 * Component: ErrorMessage
 *
 * Reusable UI for displaying an error state with an optional retry action.
 *
 * Responsibilities:
 *  - Display an error title and message.
 *  - Optionally render a retry button via onRetry callback.
 *
 * Props:
 *  - message: string — error description to show
 *  - onRetry?: () => void — optional retry handler
 *
 * Notes:
 *  - Keep messages user-friendly; avoid leaking internal details to users.
 *
 * @author Rawan Sweidan
 * @since 2025-11-10
 */

const ErrorMessage = ({ message, onRetry }) => {
  return (
    <div className="max-w-2xl mx-auto p-6">
      <div className="bg-red-50 border-2 border-red-200 rounded-lg p-6 text-center">
        <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
        <h3 className="text-xl font-semibold text-red-800 mb-2">
          Oops! Something went wrong
        </h3>
        <p className="text-red-600 mb-4">{message}</p>
        {onRetry && (
          <button
            onClick={onRetry}
            className="bg-red-600 text-white px-6 py-2 rounded-lg hover:bg-red-700 transition-colors"
          >
            Try Again
          </button>
        )}
      </div>
    </div>
  );
};

export default ErrorMessage;