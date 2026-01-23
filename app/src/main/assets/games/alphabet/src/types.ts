export interface GameConfig {
  containerId: string;
}

export interface AudioFiles {
  instruction: string;
  target: string;
  wellDone: string;
  tryAgain: string;
}

export enum CellState {
  DEFAULT = 'default',
  CORRECT = 'correct',
  INCORRECT = 'incorrect'
}
