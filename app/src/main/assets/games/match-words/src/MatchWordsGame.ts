import { GameConfig, AudioFiles } from './types';

interface AssociationPair {
  left: string;
  right: string;
}

export class MatchWordsGame {
  private config: GameConfig;
  private gameActive: boolean = false;
  private introPlayed: boolean = false;
  
  // Animal → Home associations (using images)
  private associations: AssociationPair[] = [
    { left: 'dog.png', right: 'doghouse.png' },
    { left: 'fish.png', right: 'fishbowl.png' },
    { left: 'bird.png', right: 'nest.png' },
    { left: 'bee.png', right: 'beehive.png' },
    { left: 'squirrel.png', right: 'tree.png' },
    { left: 'cow.png', right: 'barn.png' },
    { left: 'bear.png', right: 'cave.png' },
    { left: 'mouse.png', right: 'mousehole.png' },
    { left: 'rabbit.png', right: 'burrow.png' },
    { left: 'spider.png', right: 'web.png' },
  ];

  private selectedPairs: AssociationPair[] = [];
  private shuffledRight: string[] = [];
  private matches: Map<number, number> = new Map();
  
  private canvas: HTMLCanvasElement;
  private ctx: CanvasRenderingContext2D;
  private isDrawing: boolean = false;
  private startWordIndex: number = -1;
  private currentX: number = 0;
  private currentY: number = 0;
  
  private gameContainer: HTMLElement;
  private characterContainer: HTMLElement;
  private wordElements: HTMLElement[] = [];
  private emojiElements: HTMLElement[] = [];
  private currentAudio: HTMLAudioElement | null = null;

  constructor(config: GameConfig) {
    this.config = config;
    this.gameContainer = document.getElementById('game-container')!;
    this.characterContainer = document.getElementById('character-container')!;
    
    this.canvas = document.createElement('canvas');
    this.canvas.id = 'match-canvas';
    this.canvas.style.cssText = 'position:absolute;top:0;left:0;pointer-events:none;z-index:10';
    this.gameContainer.appendChild(this.canvas);
    this.ctx = this.canvas.getContext('2d')!;
    
    this.characterContainer.addEventListener('click', () => {
      if (!this.introPlayed) {
        this.introPlayed = true;
        this.playInstructionSound();
      }
    });
    
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
    if (typeof (window as any).Android !== 'undefined') {
      setTimeout(() => this.playInstructionSound(), 500);
      this.introPlayed = true;
    }
  }

  private setupGame(): void {
    const existingLayout = this.gameContainer.querySelector('.match-words-layout');
    if (existingLayout) existingLayout.remove();
    
    this.selectedPairs = [...this.associations].sort(() => Math.random() - 0.5).slice(0, 3);
    this.shuffledRight = [...this.selectedPairs.map(p => p.right)].sort(() => Math.random() - 0.5);
    
    const layoutContainer = document.createElement('div');
    layoutContainer.className = 'match-words-layout';
    
    const leftColumn = document.createElement('div');
    leftColumn.className = 'words-column';
    this.wordElements = [];
    this.selectedPairs.forEach((pair, index) => {
      const el = this.createLeftElement(pair.left, index);
      leftColumn.appendChild(el);
      this.wordElements.push(el);
    });
    
    const rightColumn = document.createElement('div');
    rightColumn.className = 'emojis-column';
    this.emojiElements = [];
    this.shuffledRight.forEach((emoji, index) => {
      const el = this.createRightElement(emoji, index);
      rightColumn.appendChild(el);
      this.emojiElements.push(el);
    });
    
    layoutContainer.appendChild(leftColumn);
    layoutContainer.appendChild(rightColumn);
    this.gameContainer.appendChild(layoutContainer);
    
    this.matches.clear();
    this.gameActive = true;
    setTimeout(() => this.resizeCanvas(), 100);
  }

  private createLeftElement(imageName: string, index: number): HTMLElement {
    const el = document.createElement('div');
    el.className = 'emoji-item left-item';
    const img = document.createElement('img');
    img.src = this.getImagePath(imageName);
    img.alt = imageName;
    img.draggable = false;
    el.appendChild(img);
    el.dataset.index = index.toString();
    el.addEventListener('mousedown', (e) => this.startDrawing(e, index));
    el.addEventListener('touchstart', (e) => this.startDrawing(e, index));
    return el;
  }

  private createRightElement(imageName: string, index: number): HTMLElement {
    const el = document.createElement('div');
    el.className = 'emoji-item right-item';
    const img = document.createElement('img');
    img.src = this.getImagePath(imageName);
    img.alt = imageName;
    img.draggable = false;
    el.appendChild(img);
    el.dataset.index = index.toString();
    return el;
  }

  private getImagePath(imageName: string): string {
    const isAndroid = typeof (window as any).Android !== 'undefined' && navigator.userAgent.includes('Android');
    return isAndroid 
      ? `file:///android_asset/games/match-words/images/${imageName}`
      : `../images/${imageName}`;
  }

  private startDrawing(event: MouseEvent | TouchEvent, wordIndex: number): void {
    if (!this.gameActive) return;
    event.preventDefault();
    this.isDrawing = true;
    this.startWordIndex = wordIndex;
    
    const rect = this.wordElements[wordIndex].getBoundingClientRect();
    const containerRect = this.gameContainer.getBoundingClientRect();
    this.currentX = rect.right - containerRect.left;
    this.currentY = rect.top + rect.height / 2 - containerRect.top;
    
    this.wordElements[wordIndex].classList.add('drawing');
    
    const moveHandler = (e: MouseEvent | TouchEvent) => this.updateDrawing(e);
    const upHandler = (e: MouseEvent | TouchEvent) => {
      document.removeEventListener('mousemove', moveHandler);
      document.removeEventListener('touchmove', moveHandler);
      document.removeEventListener('mouseup', upHandler);
      document.removeEventListener('touchend', upHandler);
      this.handleDrop(e);
    };
    document.addEventListener('mousemove', moveHandler);
    document.addEventListener('touchmove', moveHandler);
    document.addEventListener('mouseup', upHandler);
    document.addEventListener('touchend', upHandler);
  }

  private handleDrop(event: MouseEvent | TouchEvent): void {
    if (!this.isDrawing || this.startWordIndex === -1) return;
    
    let clientX: number, clientY: number;
    if (event instanceof MouseEvent) {
      clientX = event.clientX;
      clientY = event.clientY;
    } else {
      const touch = event.changedTouches[0];
      clientX = touch.clientX;
      clientY = touch.clientY;
    }
    
    // Find which emoji we dropped on
    let targetEmojiIndex = -1;
    this.emojiElements.forEach((el, index) => {
      const rect = el.getBoundingClientRect();
      if (clientX >= rect.left && clientX <= rect.right && clientY >= rect.top && clientY <= rect.bottom) {
        targetEmojiIndex = index;
      }
    });
    
    this.isDrawing = false;
    this.wordElements[this.startWordIndex].classList.remove('drawing');
    
    if (targetEmojiIndex !== -1) {
      if (this.selectedPairs[this.startWordIndex].right === this.shuffledRight[targetEmojiIndex]) {
        this.matches.set(this.startWordIndex, targetEmojiIndex);
        this.wordElements[this.startWordIndex].classList.add('matched');
        this.emojiElements[targetEmojiIndex].classList.add('matched');
        this.redrawLines();
        
        if (this.matches.size === this.selectedPairs.length) {
          this.gameActive = false;
          this.playFeedbackSound(true);
          setTimeout(() => { if (window.Android) window.Android.onGameCompleted(); }, 2000);
        }
      } else {
        this.emojiElements[targetEmojiIndex].classList.add('wrong');
        setTimeout(() => this.emojiElements[targetEmojiIndex].classList.remove('wrong'), 500);
      }
    }
    
    this.startWordIndex = -1;
    this.redrawLines();
  }

  private updateDrawing(event: MouseEvent | TouchEvent): void {
    if (!this.isDrawing) return;
    const containerRect = this.gameContainer.getBoundingClientRect();
    if (event instanceof MouseEvent) {
      this.currentX = event.clientX - containerRect.left;
      this.currentY = event.clientY - containerRect.top;
    } else {
      this.currentX = event.touches[0].clientX - containerRect.left;
      this.currentY = event.touches[0].clientY - containerRect.top;
    }
    this.redrawLines();
    this.drawCurrentLine();
  }

  private drawCurrentLine(): void {
    if (!this.isDrawing || this.startWordIndex === -1) return;
    const rect = this.wordElements[this.startWordIndex].getBoundingClientRect();
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
      const wordRect = this.wordElements[wordIndex].getBoundingClientRect();
      const emojiRect = this.emojiElements[emojiIndex].getBoundingClientRect();
      
      this.ctx.strokeStyle = '#4CAF50';
      this.ctx.lineWidth = 4;
      this.ctx.lineCap = 'round';
      this.ctx.beginPath();
      this.ctx.moveTo(wordRect.right - containerRect.left, wordRect.top + wordRect.height / 2 - containerRect.top);
      this.ctx.lineTo(emojiRect.left - containerRect.left, emojiRect.top + emojiRect.height / 2 - containerRect.top);
      this.ctx.stroke();
    });
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
    this.playAudio(this.getAudioFiles().instruction);
  }

  private playFeedbackSound(isCorrect: boolean): void {
    const files = this.getAudioFiles();
    this.playAudio(isCorrect ? files.wellDone : files.tryAgain);
  }

  private getAudioFiles(): AudioFiles {
    const isAndroid = typeof (window as any).Android !== 'undefined' && navigator.userAgent.includes('Android');
    const base = isAndroid ? 'file:///android_asset/games/audio' : '../../audio';
    return {
      instruction: `${base}/instructions_match_words.ogg`,
      target: `${base}/instructions_match_words.ogg`,
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
