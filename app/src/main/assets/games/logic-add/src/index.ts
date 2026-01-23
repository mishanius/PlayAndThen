import { LogicAddGame } from './LogicAddGame';

declare global {
  interface Window {
    Android?: { onGameCompleted: () => void };
    game?: LogicAddGame;
  }
}

document.addEventListener('DOMContentLoaded', () => {
  window.game = new LogicAddGame({ containerId: 'game-container' });
});
