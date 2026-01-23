import { OppositesGame } from './OppositesGame';

declare global {
  interface Window {
    Android?: { onGameCompleted: () => void };
    game?: OppositesGame;
  }
}

document.addEventListener('DOMContentLoaded', () => {
  window.game = new OppositesGame({ containerId: 'game-container' });
});
