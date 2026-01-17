import { NumbersGame } from './NumbersGame';
import { GameConfig } from './types';

/**
 * Entry point for Numbers game
 * Exposes global functions for Kotlin to call
 */

let currentGame: NumbersGame | null = null;

/**
 * Initialize game with configuration from Kotlin
 */
window.initGame = (config: GameConfig) => {
  console.log('Initializing Numbers game with config:', config);
  
  if (currentGame) {
    currentGame.destroy();
  }

  currentGame = new NumbersGame(config);
};

/**
 * Reset current game
 */
window.resetGame = () => {
  console.log('Resetting Numbers game');
  if (currentGame) {
    currentGame.reset();
  }
};

/**
 * Log when page is fully loaded
 */
window.addEventListener('DOMContentLoaded', () => {
  console.log('Numbers game page loaded and ready');
});
