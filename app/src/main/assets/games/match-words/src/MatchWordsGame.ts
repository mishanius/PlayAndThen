import { GameConfig, AudioFiles, WordImagePair } from './types';

/**
 * Match Words game - child drags lines from words to corresponding emojis.
 * Uses touch/mouse events to draw connecting lines.
 */
export class MatchWordsGame {
  private config: GameConfig;
  private gameActive: boolean = false;
  
  private wordImagePairs: WordImagePair[] = [
    { word: 'Apple', emoji: '🍎', audioFile: 'apple.ogg' },
    { word: 'Cat', emoji: '🐱', audioFile: 'cat.ogg' },
    { word: 'Sun', emoji: '☀️', audioFile: 'sun.ogg' },
    { word: 'Tree', emoji: '🌳', audioFile: 'tree.ogg' },
    { word: 'Car', emoji: '🚗', audioFile: 'car.ogg' },
    { word: 'House', emoji: '🏠', audioFile: 'house.ogg' },
    { word: 'Star', emoji: '⭐', audioFile: 'star.ogg' },
    { word: 'Heart', emoji: '❤️', audioFile: 'heart.ogg' },
    { word: 'Book', emoji: '📚', audioFile: 'book.ogg' },
    { word: 'Ball', emoji: '⚽', audioFile: 'ball.ogg' }
  ];

  private selectedPairs: WordImagePair[] = [];
  private shuffledEmojis: string[] = [];
  private matches: Map<number, number> = new Map();
  
  // Drawing state
  private canvas: HTMLCanvasElement;
  private ctx: CanvasRenderingContext2D;
  private isDrawing: boolean = false;
  private startWordIndex: number = -1;
  private currentX: number = 0;
  private currentY: number = 0;
  
  // Layout
  private gameContainer: HTMLElement;
  private wordElements: HTMLElement[] = [];
  private emojiElements: HTMLElement[] = [];
  
  // Audio
  private currentAudio: HTMLAudioElement | null = null;

  constructor(config: GameConfig) {
    this.config = config;
    this.gameContainer = document.getElementById('game-container')!;
    
    this.canvas = document.createElement('canvas');
    this.canvas.id = 'match-canvas';
    this.canvas.style.position = 'absolute';
    this.canvas.style.top = '0';
    this.canvas.style.left = '0';
    this.canvas.style.pointerEvents = 'none';
    this.canvas.style.zIndex = '10';
    this.gameContainer.appendChild(this.canvas);
    
    this.ctx = this.canvas.getContext('2d')!;
    this.resizeCanvas();
    
    window.addEventListener('resize', () => this.resizeCanvas());
    
    this.startGame();
  }

  private resizeCanvas(): void {
    this.canvas.width = this.gameContainer.offsetWidth;
    this.canvas.height = this.gameContainer.offsetHeight;
    this.redrawLines();
  }

  public startGame(): void {
    this.setupGame();
    
    setTimeout(() => {
      this.playInstructionSound();
    }, 500);
  }

  private setupGame(): void {
    this.gameContainer.innerHTML = '';
    this.gameContainer.appendChild(this.canvas);
    
    this.selectedPairs = [...this.wordImagePairs]
      .sort(() => Math.random() - 0.5)
      .slice(0, 3);
    
    this.shuffledEmojis = [...this.selectedPairs.map(p => p.emoji)]
      .sort(() => Math.random() - 0.5);
    
    const layoutContainer = document.createElement('div');
    layoutContainer.className = 'match-words-layout';
    
    const wordsColumn = document.createElement('div');
    wordsColumn.className = 'words-column';
    
    this.wordElements = [];
    this.selectedPairs.forEach((pair, index) => {
      const wordElement = this.createWordElement(pair.word, index);
      wordsColumn.appendChild(wordElement);
      this.wordElements.push(wordElement);
    });
    
    const emojisColumn = document.createElement('div');
    emojisColumn.className = 'emojis-column';
    
    this.emojiElements = [];
    this.shuffledEmojis.forEach((emoji, index) => {
      const emojiElement = this.createEmojiElement(emoji, index);
      emojisColumn.appendChild(emojiElement);
      this.emojiElements.push(emojiElement);
    });
    
    layoutContainer.appendChild(wordsColumn);
    layoutContainer.appendChild(emojisColumn);
    this.gameContainer.appendChild(layoutContainer);
    
    this.matches.clear();
    this.gameActive = true;
    
    setTimeout(() => this.resizeCanvas(), 100);
  }

  private createWordElement(word: string, index: number): HTMLElement {
    const element = document.createElement('div');
    element.className = 'word-item';
    element.textContent = word;
    element.dataset.index = index.toString();
    
    element.addEventListener('mousedown', (e) => this.startDrawing(e, index));
    element.addEventListener('touchstart', (e) => this.startDrawing(e, index));
    
    return element;
  }

  private createEmojiElement(emoji: string, index: number): HTMLElement {
    const element = document.createElement('div');
    element.className = 'emoji-item';
    element.textContent = emoji;
    element.dataset.index = index.toString();
    
    element.addEventListener('mouseup', (e) => this.endDrawing(e, index));
    element.addEventListener('touchend', (e) => this.endDrawing(e, index));
    
    return element;
  }

  private startDrawing(event: MouseEvent | TouchEvent, wordIndex: number): void {
    if (!this.gameActive) return;
    
    event.preventDefault();
    this.isDrawing = true;
    this.startWordIndex = wordIndex;
    
    const element = this.wordElements[wordIndex];
    const rect = element.getBoundingClientRect();
    const containerRect = this.gameContainer.getBoundingClientRect();
    
    this.currentX = rect.right - containerRect.left;
    this.currentY = rect.top + rect.height / 2 - containerRect.top;
    
    element.classList.add('drawing');
    
    const moveHandler = (e: MouseEvent | TouchEvent) => this.updateDrawing(e);
    document.addEventListener('mousemove', moveHandler);
    document.addEventListener('touchmove', moveHandler);
    
    const upHandler = () => {
      document.removeEventListener('mousemove', moveHandler);
      document.removeEventListener('touchmove', moveHandler);
      document.removeEventListener('mouseup', upHandler);
      document.removeEventListener('touchend', upHandler);
      
      if (this.isDrawing) {
        this.cancelDrawing();
      }
    };
    document.addEventListener('mouseup', upHandler);
    document.addEventListener('touchend', upHandler);
  }

  private updateDrawing(event: MouseEvent | TouchEvent): void {
    if (!this.isDrawing) return;
    
    const containerRect = this.gameContainer.getBoundingClientRect();
    
    if (event instanceof MouseEvent) {
      this.currentX = event.clientX - containerRect.left;
      this.currentY = event.clientY - containerRect.top;
    } else {
      const touch = event.touches[0];
      this.currentX = touch.clientX - containerRect.left;
      this.currentY = touch.clientY - containerRect.top;
    }
    
    this.redrawLines();
    this.drawCurrentLine();
  }

  private endDrawing(event: MouseEvent | TouchEvent, emojiIndex: number): void {
    if (!this.isDrawing || this.startWordIndex === -1) return;
    
    event.preventDefault();
    this.isDrawing = false;
    
    this.wordElements[this.startWordIndex].classList.remove('drawing');
    
    const wordPair = this.selectedPairs[this.startWordIndex];
    const selectedEmoji = this.shuffledEmojis[emojiIndex];
    
    if (wordPair.emoji === selectedEmoji) {
      this.matches.set(this.startWordIndex, emojiIndex);
      this.wordElements[this.startWordIndex].classList.add('matched');
      this.emojiElements[emojiIndex].classList.add('matched');
      
      this.redrawLines();
      
      if (this.matches.size === this.selectedPairs.length) {
        this.gameActive = false;
        this.playFeedbackSound(true);
        
        setTimeout(() => {
          if (window.Android) {
            window.Android.onGameCompleted();
          }
        }, 2000);
      }
    } else {
      this.emojiElements[emojiIndex].classList.add('wrong');
      setTimeout(() => {
        this.emojiElements[emojiIndex].classList.remove('wrong');
      }, 500);
    }
    
    this.startWordIndex = -1;
  }

  private cancelDrawing(): void {
    this.isDrawing = false;
    if (this.startWordIndex !== -1) {
      this.wordElements[this.startWordIndex].classList.remove('drawing');
    }
    this.startWordIndex = -1;
    this.redrawLines();
  }

  private drawCurrentLine(): void {
    if (!this.isDrawing || this.startWordIndex === -1) return;
    
    const element = this.wordElements[this.startWordIndex];
    const rect = element.getBoundingClientRect();
    const containerRect = this.gameContainer.getBoundingClientRect();
    
    const startX = rect.right - containerRect.left;
    const startY = rect.top + rect.height / 2 - containerRect.top;
    
    this.ctx.strokeStyle = '#667eea';
    this.ctx.lineWidth = 4;
    this.ctx.lineCap = 'round';
    this.ctx.setLineDash([5, 5]);
    
    this.ctx.beginPath();
    this.ctx.moveTo(startX, startY);
    this.ctx.lineTo(this.currentX, this.currentY);
    this.ctx.stroke();
    
    this.ctx.setLineDash([]);
  }

  private redrawLines(): void {
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    
    const containerRect = this.gameContainer.getBoundingClientRect();
    
    this.matches.forEach((emojiIndex, wordIndex) => {
      const wordElement = this.wordElements[wordIndex];
      const emojiElement = this.emojiElements[emojiIndex];
      
      const wordRect = wordElement.getBoundingClientRect();
      const emojiRect = emojiElement.getBoundingClientRect();
      
      const startX = wordRect.right - containerRect.left;
      const startY = wordRect.top + wordRect.height / 2 - containerRect.top;
      const endX = emojiRect.left - containerRect.left;
      const endY = emojiRect.top + emojiRect.height / 2 - containerRect.top;
      
      this.ctx.strokeStyle = '#4CAF50';
      this.ctx.lineWidth = 4;
      this.ctx.lineCap = 'round';
      
      this.ctx.beginPath();
      this.ctx.moveTo(startX, startY);
      this.ctx.lineTo(endX, endY);
      this.ctx.stroke();
    });
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

    this.currentAudio.addEventListener('error', (e) => {
      console.error('Audio playback error:', e);
      this.currentAudio = null;
      if (onComplete) onComplete();
    });

    this.currentAudio.play().catch(err => {
      console.error('Failed to play audio:', err);
    });
  }

  private playInstructionSound(): void {
    const audioFiles = this.getAudioFiles();
    this.playAudio(audioFiles.instruction);
  }

  private playFeedbackSound(isCorrect: boolean): void {
    const audioFiles = this.getAudioFiles();
    const soundPath = isCorrect ? audioFiles.wellDone : audioFiles.tryAgain;
    this.playAudio(soundPath);
  }

  private getAudioFiles(): AudioFiles {
    return {
      instruction: 'audio/instructions_match_words.ogg',
      target: 'audio/instructions_match_words.ogg',
      wellDone: 'audio/well_done.ogg',
      tryAgain: 'audio/try_again.ogg'
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
    window.removeEventListener('resize', () => this.resizeCanvas());
  }
}
