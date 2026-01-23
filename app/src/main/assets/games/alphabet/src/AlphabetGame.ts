import { GameConfig, AudioFiles, CellState } from './types';

export class AlphabetGame {
  private config: GameConfig;
  private gameActive: boolean = false;
  private introPlayed: boolean = false;
  private targetIndex: number = 0;
  private correctCellIndex: number = 0;
  private gameContainer: HTMLElement;
  private characterContainer: HTMLElement;
  private cells: HTMLElement[];
  private currentAudio: HTMLAudioElement | null = null;
  
  // Hebrew letters: Aleph, Bet, Gimel
  private static readonly LETTERS = ['א', 'ב', 'ג'];
  private static readonly LETTER_AUDIO = ['aleph.ogg', 'bet.ogg', 'gimel.ogg'];
  private static readonly LETTER_COLORS = ['#FF69B4', '#32CD32', '#00BFFF'];

  constructor(config: GameConfig) {
    this.config = config;
    this.gameContainer = document.getElementById('game-container')!;
    this.characterContainer = document.getElementById('character-container')!;
    this.cells = Array.from(document.querySelectorAll<HTMLElement>('.cell'));
    this.initializeUI();
  }

  private initializeUI(): void {
    this.characterContainer.addEventListener('click', () => {
      if (!this.introPlayed) {
        this.introPlayed = true;
        this.playInstructionSound();
      } else if (this.gameActive) {
        this.playTargetSound();
      }
    });

    this.cells.forEach((cell, index) => {
      cell.addEventListener('click', () => {
        if (this.gameActive) this.handleCellClick(index);
      });
    });

    this.startGame();
  }

  public startGame(): void {
    this.setupGame();
    if (typeof (window as any).Android !== 'undefined') {
      setTimeout(() => this.playInstructionSound(), 500);
      this.introPlayed = true;
    }
  }

  private setupGame(): void {
    this.cells.forEach(cell => {
      cell.innerHTML = '';
      this.setCellState(cell, CellState.DEFAULT);
    });
    this.populateCells();
    this.gameActive = true;
  }

  private populateCells(): void {
    this.targetIndex = Math.floor(Math.random() * 3);
    this.correctCellIndex = Math.floor(Math.random() * 3);
    
    // Shuffle letters for display
    const indices = [0, 1, 2];
    const shuffled = [...indices].sort(() => Math.random() - 0.5);
    
    // Ensure target is at correct position
    const targetPos = shuffled.indexOf(this.targetIndex);
    if (targetPos !== this.correctCellIndex) {
      [shuffled[targetPos], shuffled[this.correctCellIndex]] = [shuffled[this.correctCellIndex], shuffled[targetPos]];
    }
    
    shuffled.forEach((letterIdx, cellIdx) => {
      this.cells[cellIdx].appendChild(this.createLetterElement(letterIdx));
    });
  }

  private createLetterElement(letterIndex: number): HTMLDivElement {
    const element = document.createElement('div');
    element.className = 'letter-text';
    element.textContent = AlphabetGame.LETTERS[letterIndex];
    element.style.color = AlphabetGame.LETTER_COLORS[letterIndex];
    return element;
  }

  private handleCellClick(clickedIndex: number): void {
    this.gameActive = false;

    if (clickedIndex === this.correctCellIndex) {
      this.setCellState(this.cells[clickedIndex], CellState.CORRECT);
      this.playFeedbackSound(true);
      setTimeout(() => {
        if ((window as any).Android) (window as any).Android.onGameCompleted();
      }, 2000);
    } else {
      this.setCellState(this.cells[clickedIndex], CellState.INCORRECT);
      this.setCellState(this.cells[this.correctCellIndex], CellState.CORRECT);
      this.playFeedbackSound(false);
      setTimeout(() => {
        this.setupGame();
        this.playTargetSound();
      }, 3000);
    }
  }

  private setCellState(cell: HTMLElement, state: CellState): void {
    cell.classList.remove('cell-default', 'cell-correct', 'cell-incorrect');
    cell.classList.add(`cell-${state}`);
  }

  private playAudio(audioPath: string, onComplete?: () => void): void {
    if (this.currentAudio) { this.currentAudio.pause(); this.currentAudio = null; }
    this.currentAudio = new Audio(audioPath);
    this.currentAudio.volume = 1.0;
    this.currentAudio.addEventListener('ended', () => { this.currentAudio = null; if (onComplete) onComplete(); });
    this.currentAudio.addEventListener('error', () => { this.currentAudio = null; if (onComplete) onComplete(); });
    this.currentAudio.play().catch(console.error);
  }

  private playInstructionSound(): void {
    this.playAudio(this.getAudioFiles().instruction, () => {
      setTimeout(() => { if (this.gameActive) this.playTargetSound(); }, 50);
    });
  }

  private playTargetSound(): void {
    this.playAudio(this.getAudioFiles().target);
  }

  private playFeedbackSound(isCorrect: boolean): void {
    const files = this.getAudioFiles();
    this.playAudio(isCorrect ? files.wellDone : files.tryAgain);
  }

  private getAudioFiles(): AudioFiles {
    const isAndroid = typeof (window as any).Android !== 'undefined' && navigator.userAgent.includes('Android');
    const base = isAndroid ? 'file:///android_asset/games/audio' : '../../audio';
    return {
      instruction: `${base}/instructions_alphabet_mode.ogg`,
      target: `${base}/${AlphabetGame.LETTER_AUDIO[this.targetIndex]}`,
      wellDone: `${base}/well_done.ogg`,
      tryAgain: `${base}/try_again.ogg`
    };
  }

  public reset(): void {
    if (this.currentAudio) { this.currentAudio.pause(); this.currentAudio = null; }
    this.setupGame();
  }

  public destroy(): void {
    if (this.currentAudio) { this.currentAudio.pause(); this.currentAudio = null; }
    this.gameActive = false;
  }
}
