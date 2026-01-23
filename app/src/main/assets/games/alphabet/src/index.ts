import { AlphabetGame } from './AlphabetGame';

declare global {
  interface Window {
    Android?: { onGameCompleted: () => void };
    game?: AlphabetGame;
  }
}

document.addEventListener('DOMContentLoaded', () => {
  window.game = new AlphabetGame({ containerId: 'game-container' });
});
