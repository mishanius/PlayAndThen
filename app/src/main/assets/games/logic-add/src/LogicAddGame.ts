import { GameConfig, LogicEquation, AudioFiles, CellState } from './types';

export class LogicAddGame {
  private config: GameConfig;
  private gameActive = false;
  private introPlayed = false;
  private currentEquation: LogicEquation | null = null;
  private correctOptionIndex = 0;
  private gameContainer: HTMLElement;
  private characterContainer: HTMLElement;
  private leftImage: HTMLImageElement;
  private rightImage: HTMLImageElement;
  private resultImage: HTMLImageElement;
  private optionCells: HTMLElement[] = [];
  private currentAudio: HTMLAudioElement | null = null;

  private static readonly EQUATIONS: LogicEquation[] = [
    { left: 'seed', right: 'water', result: 'tree' },
    { left: 'bread', right: 'cheese', result: 'sandwich' },
    { left: 'wool', right: 'needles', result: 'sweater' },
    { left: 'wood', right: 'hammer', result: 'chair' },
    { left: 'cow', right: 'grass', result: 'milk' },
    { left: 'bee', right: 'flower', result: 'honey' },
    { left: 'flour', right: 'oven', result: 'bread' },
    { left: 'paint', right: 'brush', result: 'picture' },
    { left: 'egg', right: 'pan', result: 'omelette' },
    { left: 'thread', right: 'needle', result: 'button' },
    { left: 'apple', right: 'knife', result: 'slices' },
    { left: 'lemon', right: 'water', result: 'lemonade' },
    { left: 'tomato', right: 'pot', result: 'soup' },
    { left: 'clay', right: 'hands', result: 'vase' },
  ];

  constructor(config: GameConfig) {
    this.config = config;
    this.gameContainer = document.getElementById('game-container')!;
    this.characterContainer = document.getElementById('character-container')!;
    this.leftImage = document.getElementById('left-image') as HTMLImageElement;
    this.rightImage = document.getElementById('right-image') as HTMLImageElement;
    this.resultImage = document.getElementById('result-image') as HTMLImageElement;
    this.optionCells = Array.from(document.querySelectorAll<HTMLElement>('.option-cell'));
    this.initializeUI();
  }

  private initializeUI(): void {
    this.characterContainer.addEventListener('click', () => {
      this.playInstructionSound();
      this.introPlayed = true;
    });

    this.optionCells.forEach((cell, index) => {
      cell.addEventListener('click', () => {
        if (this.gameActive) this.handleOptionClick(index);
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
    this.optionCells.forEach(cell => this.setCellState(cell, CellState.DEFAULT));
    this.resultImage.style.opacity = '0.3';
    this.resultImage.src = '';
    this.populateEquation();
    this.gameActive = true;
  }

  private populateEquation(): void {
    this.currentEquation = LogicAddGame.EQUATIONS[Math.floor(Math.random() * LogicAddGame.EQUATIONS.length)];
    this.leftImage.src = this.getImagePath(this.currentEquation.left);
    this.rightImage.src = this.getImagePath(this.currentEquation.right);

    // Generate options: 1 correct + 2 random distractors
    const allResults = LogicAddGame.EQUATIONS.map(e => e.result);
    const distractors = allResults.filter(r => r !== this.currentEquation!.result);
    const shuffledDistractors = distractors.sort(() => Math.random() - 0.5).slice(0, 2);
    
    const options = [this.currentEquation.result, ...shuffledDistractors].sort(() => Math.random() - 0.5);
    this.correctOptionIndex = options.indexOf(this.currentEquation.result);

    options.forEach((opt, i) => {
      const img = this.optionCells[i].querySelector('img') as HTMLImageElement;
      img.src = this.getImagePath(opt);
    });
  }

  private getImagePath(name: string): string {
    const isAndroid = typeof (window as any).Android !== 'undefined' && navigator.userAgent.includes('Android');
    return isAndroid ? `file:///android_asset/games/logic-add/images/${name}.png` : `./images/${name}.png`;
  }

  private handleOptionClick(index: number): void {
    this.gameActive = false;

    if (index === this.correctOptionIndex) {
      this.setCellState(this.optionCells[index], CellState.CORRECT);
      this.resultImage.src = this.getImagePath(this.currentEquation!.result);
      this.resultImage.style.opacity = '1';
      this.playFeedbackSound(true);
      setTimeout(() => { if ((window as any).Android) (window as any).Android.onGameCompleted(); }, 2000);
    } else {
      this.setCellState(this.optionCells[index], CellState.INCORRECT);
      this.setCellState(this.optionCells[this.correctOptionIndex], CellState.CORRECT);
      this.playFeedbackSound(false);
      setTimeout(() => this.setupGame(), 3000);
    }
  }

  private setCellState(cell: HTMLElement, state: CellState): void {
    cell.classList.remove('cell-default', 'cell-correct', 'cell-incorrect');
    cell.classList.add(`cell-${state}`);
  }

  private playAudio(path: string, onComplete?: () => void): void {
    if (this.currentAudio) { this.currentAudio.pause(); this.currentAudio = null; }
    this.currentAudio = new Audio(path);
    this.currentAudio.volume = 1.0;
    this.currentAudio.addEventListener('ended', () => { this.currentAudio = null; onComplete?.(); });
    this.currentAudio.addEventListener('error', () => { this.currentAudio = null; onComplete?.(); });
    this.currentAudio.play().catch(console.error);
  }

  private playInstructionSound(): void {
    this.playAudio(this.getAudioFiles().instruction);
  }

  private playFeedbackSound(isCorrect: boolean): void {
    const files = this.getAudioFiles();
    this.playAudio(isCorrect ? files.wellDone : files.tryAgain);
  }

  private getAudioFiles(): AudioFiles {
    const isAndroid = typeof (window as any).Android !== 'undefined' && navigator.userAgent.includes('Android');
    const base = isAndroid ? 'file:///android_asset/games/audio' : '../audio';
    return {
      instruction: `${base}/instructions_logic_add.ogg`,
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
