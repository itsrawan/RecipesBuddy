/**
 * Utility formatters for display values used across the UI.
 *
 * Contains small, pure helper functions to format calories, time and
 * ingredient amounts for presentation in components.
 *
 * Exports:
 *  - formatCalories(calories): rounds and localizes numeric calories.
 *  - formatTime(minutes): formats minutes to "Xh Ym" or "N min".
 *  - formatIngredientAmount(amount, unit): returns amount with unit, trims empties.
 *
 * Usage:
 *  import { formatCalories, formatTime, formatIngredientAmount } from '../utils/formatters';
 *
 * Notes:
 *  - Functions are intentionally small and deterministic to make them easy
 *    to test and reuse in multiple components.
 *
 * @author Rawan Sweidan
 * @since 2025-11-10
 */

export const formatCalories = (calories) => {
  if (!calories) return '0';
  return Math.round(calories).toLocaleString();
};

export const formatTime = (minutes) => {
  if (!minutes) return 'N/A';
  if (minutes < 60) return `${minutes} min`;
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return mins > 0 ? `${hours}h ${mins}m` : `${hours}h`;
};

export const formatIngredientAmount = (amount, unit) => {
  if (!amount) return '';
  const formattedAmount = amount % 1 === 0 ? amount : amount.toFixed(2);
  return `${formattedAmount} ${unit || ''}`.trim();
};