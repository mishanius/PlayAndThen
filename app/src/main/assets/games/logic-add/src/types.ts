export interface LogicEquation {
  left: string;
  right: string;
  result: string;
}

export interface GameConfig {
  containerId: string;
}

export interface AudioFiles {
  instruction: string;
  wellDone: string;
  tryAgain: string;
}

export enum CellState {
  DEFAULT = 'default',
  CORRECT = 'correct',
  INCORRECT = 'incorrect'
}
