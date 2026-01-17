import { GameConfig, AudioFiles, CellState } from './types';

export class NumbersGame {
  private config: GameConfig;
  private gameActive: boolean = false;
  private introPlayed: boolean = false;
  private targetNumber: number = 0;
  private correctCellIndex: number = 0;
  private gameContainer: HTMLElement;
  private hugeTextElement: HTMLElement;
  private characterContainer: HTMLElement;
  private cells: HTMLElement[];
  private currentAudio: HTMLAudioElement | null = null;
  
  private static readonly NUMBER_COLORS = [
    '#FF69B4', '#FF4500', '#32CD32', '#FF1493', '#00BFFF',
    '#FFD700', '#FF6347', '#9370DB', '#00CED1', '#FFA500'
  ];

  constructor(config: GameConfig) {
    this.config = config;
    this.gameContainer = document.getElementById('game-container')!;
    this.hugeTextElement = document.getElementById('huge-text')!;
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
        if (this.gameActive) {
          this.handleCellClick(index);
        }
      });
    });

    this.startGame();
  }

  public startGame(): void {
    this.setupGame();
    // Android WebView has bridge injected, allows autoplay
    const hasAndroidBridge = typeof (window as any).Android !== 'undefined';
    console.log('startGame: hasAndroidBridge =', hasAndroidBridge);
    if (hasAndroidBridge) {
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
    this.hugeTextElement.style.display = 'none';
    this.gameActive = true;
  }

  private populateCells(): void {
    this.targetNumber = Math.floor(Math.random() * 11);
    this.correctCellIndex = Math.floor(Math.random() * 6);
    const counts = this.generateCounts();
    counts.forEach((count, index) => {
      this.cells[index].appendChild(this.createNumberElement(count));
    });
  }

  private generateCounts(): number[] {
    const counts: number[] = new Array(6);
    const usedNumbers = new Set<number>([this.targetNumber]);
    counts[this.correctCellIndex] = this.targetNumber;

    for (let i = 0; i < 6; i++) {
      if (i === this.correctCellIndex) continue;
      let randomCount: number;
      do {
        randomCount = Math.floor(Math.random() * 11);
      } while (usedNumbers.has(randomCount));
      counts[i] = randomCount;
      usedNumbers.add(randomCount);
    }
    return counts;
  }

  private createNumberElement(number: number): HTMLDivElement {
    const element = document.createElement('div');
    element.className = 'number-text';
    element.textContent = number.toString();
    element.style.color = NumbersGame.NUMBER_COLORS[Math.floor(Math.random() * NumbersGame.NUMBER_COLORS.length)];
    return element;
  }

  private handleCellClick(clickedIndex: number): void {
    this.gameActive = false;

    if (clickedIndex === this.correctCellIndex) {
      this.setCellState(this.cells[clickedIndex], CellState.CORRECT);
      this.playFeedbackSound(true);
      setTimeout(() => {
        if (window.Android) window.Android.onGameCompleted();
      }, 2000);
    } else {
      this.setCellState(this.cells[clickedIndex], CellState.INCORRECT);
      this.cells.forEach((cell, index) => {
        if (index === this.correctCellIndex) this.setCellState(cell, CellState.CORRECT);
      });
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
    if (this.currentAudio) {
      this.currentAudio.pause();
      this.currentAudio = null;
    }

    this.currentAudio = new Audio(audioPath);
    this.currentAudio.volume = 1.0;
    this.currentAudio.addEventListener('ended', () => {
      this.currentAudio = null;
      if (onComplete) onComplete();
    });
    this.currentAudio.addEventListener('error', () => {
      this.currentAudio = null;
      if (onComplete) onComplete();
    });
    this.currentAudio.play().catch(console.error);
  }

  private playInstructionSound(): void {
    const audioFiles = this.getAudioFiles();
    this.playAudio(audioFiles.instruction, () => {
      setTimeout(() => {
        if (this.gameActive) this.playTargetSound();
      }, 50);
    });
  }

  private playTargetSound(): void {
    this.playAudio(this.getAudioFiles().target);
  }

  private playFeedbackSound(isCorrect: boolean): void {
    const audioFiles = this.getAudioFiles();
    this.playAudio(isCorrect ? audioFiles.wellDone : audioFiles.tryAgain);
  }

  private getAudioFiles(): AudioFiles {
    const numberAudioFiles = [
      'zero.ogg', 'one.ogg', 'two.ogg', 'three.ogg', 'four.ogg',
      'five.ogg', 'six.ogg', 'seven.ogg', 'eight.ogg', 'nine.ogg', 'ten.ogg'
    ];
    const isAndroidWebView = typeof (window as any).Android !== 'undefined' && navigator.userAgent.includes('Android');
    const audioBase = isAndroidWebView ? 'file:///android_asset/games/audio' : '../../audio';

    return {
      instruction: `${audioBase}/instructions_numbers_mode.ogg`,
      target: `${audioBase}/${numberAudioFiles[this.targetNumber]}`,
      wellDone: `${audioBase}/well_done.ogg`,
      tryAgain: `${audioBase}/try_again.ogg`
    };
  }

  public reset(): void {
    if (this.currentAudio) {
      this.currentAudio.pause();
      this.currentAudio = null;
    }
    this.setupGame();
  }

  public destroy(): void {
    if (this.currentAudio) {
      this.currentAudio.pause();
      this.currentAudio = null;
    }
    this.gameActive = false;
  }
}
