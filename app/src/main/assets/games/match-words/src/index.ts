import { MatchWordsGame } from './MatchWordsGame';
import { GameConfig } from './types';

/**
 * Entry point for Match Words game
 * Exposes global functions for Kotlin to call
 */

let currentGame: MatchWordsGame | null = null;

/**
 * Initialize game with configuration from Kotlin
 */
window.initGame = (config: GameConfig) => {
  console.log('Initializing Match Words game with config:', config);
  
  if (currentGame) {
    currentGame.destroy();
  }

  currentGame = new MatchWordsGame(config);
};

/**
 * Reset current game
 */
window.resetGame = () => {
  console.log('Resetting Match Words game');
  if (currentGame) {
    currentGame.reset();
  }
};

/**
 * Log when page is fully loaded
 */
window.addEventListener('DOMContentLoaded', () => {
  console.log('Match Words game page loaded and ready');
});
