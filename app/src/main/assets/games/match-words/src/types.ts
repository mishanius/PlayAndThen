/**
 * Game configuration passed from Kotlin
 */
export interface GameConfig {
  currentRound: number;
  totalRounds: number;
}

/**
 * Audio file paths for the game
 */
export interface AudioFiles {
  instruction: string;
  target: string;
  wellDone: string;
  tryAgain: string;
}

/**
 * Word-Image pair for matching
 */
export interface WordImagePair {
  word: string;
  emoji: string;
  audioFile: string;
}

/**
 * Global window interface for Kotlin bridge
 */
declare global {
  interface Window {
    Android?: {
      onGameCompleted: () => void;
    };
    initGame?: (config: GameConfig) => void;
    resetGame?: () => void;
  }
}
