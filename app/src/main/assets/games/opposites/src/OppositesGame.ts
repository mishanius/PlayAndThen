import { GameConfig, AudioFiles } from './types';

interface OppositePair {
  left: string;
  right: string;
}

export class OppositesGame {
  private config: GameConfig;
  private gameActive: boolean = false;
  private introPlayed: boolean = false;
  
  private opposites: OppositePair[] = [
    { left: 'happy.png', right: 'sad.png' },
    { left: 'big.png', right: 'small.png' },
    { left: 'hot.png', right: 'cold.png' },
    { left: 'fast.png', right: 'slow.png' },
    { left: 'day.png', right: 'night.png' },
    { left: 'up.png', right: 'down.png' },
  ];

  private selectedPairs: OppositePair[] = [];
  private shuffledRight: string[] = [];
  private matches: Map<number, number> = new Map();
  
  private canvas: HTMLCanvasElement;
  private ctx: CanvasRenderingContext2D;
  private isDrawing: boolean = false;
  private startIndex: number = -1;
  private currentX: number = 0;
  private currentY: number = 0;
  
  private gameContainer: HTMLElement;
  private characterContainer: HTMLElement;
  private leftElements: HTMLElement[] = [];
  private rightElements: HTMLElement[] = [];
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
    const existingLayout = this.gameContainer.querySelector('.opposites-layout');
    if (existingLayout) existingLayout.remove();
    
    this.selectedPairs = [...this.opposites].sort(() => Math.random() - 0.5).slice(0, 3);
    this.shuffledRight = [...this.selectedPairs.map(p => p.right)].sort(() => Math.random() - 0.5);
    
    const layoutContainer = document.createElement('div');
    layoutContainer.className = 'opposites-layout';
    
    const leftColumn = document.createElement('div');
    leftColumn.className = 'left-column';
    this.leftElements = [];
    this.selectedPairs.forEach((pair, index) => {
      const el = this.createImageElement(pair.left, index, true);
      leftColumn.appendChild(el);
      this.leftElements.push(el);
    });
    
    const rightColumn = document.createElement('div');
    rightColumn.className = 'right-column';
    this.rightElements = [];
    this.shuffledRight.forEach((img, index) => {
      const el = this.createImageElement(img, index, false);
      rightColumn.appendChild(el);
      this.rightElements.push(el);
    });
    
    layoutContainer.appendChild(leftColumn);
    layoutContainer.appendChild(rightColumn);
    this.gameContainer.appendChild(layoutContainer);
    
    this.matches.clear();
    this.gameActive = true;
    setTimeout(() => this.resizeCanvas(), 100);
  }

  private createImageElement(imageName: string, index: number, isLeft: boolean): HTMLElement {
    const el = document.createElement('div');
    el.className = `image-item ${isLeft ? 'left-item' : 'right-item'}`;
    const img = document.createElement('img');
    img.src = this.getImagePath(imageName);
    img.alt = imageName;
    img.draggable = false;
    el.appendChild(img);
    el.dataset.index = index.toString();
    if (isLeft) {
      el.addEventListener('mousedown', (e) => this.startDrawing(e, index));
      el.addEventListener('touchstart', (e) => this.startDrawing(e, index));
    }
    return el;
  }

  private getImagePath(imageName: string): string {
    const isAndroid = typeof (window as any).Android !== 'undefined' && navigator.userAgent.includes('Android');
    if (isAndroid) {
      return `file:///android_asset/games/opposites/images/${imageName}`;
    }
    // Check if running from dist/ or root
    const inDist = window.location.pathname.includes('/dist/');
    return inDist ? `../images/${imageName}` : `./images/${imageName}`;
  }

  private startDrawing(event: MouseEvent | TouchEvent, index: number): void {
    if (!this.gameActive) return;
    event.preventDefault();
    this.isDrawing = true;
    this.startIndex = index;
    
    const rect = this.leftElements[index].getBoundingClientRect();
    const containerRect = this.gameContainer.getBoundingClientRect();
    this.currentX = rect.right - containerRect.left;
    this.currentY = rect.top + rect.height / 2 - containerRect.top;
    
    this.leftElements[index].classList.add('drawing');
    
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
    if (!this.isDrawing || this.startIndex === -1) return;
    
    let clientX: number, clientY: number;
    if (event instanceof MouseEvent) {
      clientX = event.clientX;
      clientY = event.clientY;
    } else {
      const touch = event.changedTouches[0];
      clientX = touch.clientX;
      clientY = touch.clientY;
    }
    
    let targetIndex = -1;
    this.rightElements.forEach((el, index) => {
      const rect = el.getBoundingClientRect();
      if (clientX >= rect.left && clientX <= rect.right && clientY >= rect.top && clientY <= rect.bottom) {
        targetIndex = index;
      }
    });
    
    this.isDrawing = false;
    this.leftElements[this.startIndex].classList.remove('drawing');
    
    if (targetIndex !== -1) {
      if (this.selectedPairs[this.startIndex].right === this.shuffledRight[targetIndex]) {
        this.matches.set(this.startIndex, targetIndex);
        this.leftElements[this.startIndex].classList.add('matched');
        this.rightElements[targetIndex].classList.add('matched');
        this.redrawLines();
        
        if (this.matches.size === this.selectedPairs.length) {
          this.gameActive = false;
          this.playFeedbackSound(true);
          setTimeout(() => { if ((window as any).Android) (window as any).Android.onGameCompleted(); }, 2000);
        }
      } else {
        this.rightElements[targetIndex].classList.add('wrong');
        setTimeout(() => this.rightElements[targetIndex].classList.remove('wrong'), 500);
      }
    }
    
    this.startIndex = -1;
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
    if (!this.isDrawing || this.startIndex === -1) return;
    const rect = this.leftElements[this.startIndex].getBoundingClientRect();
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
    
    this.matches.forEach((rightIndex, leftIndex) => {
      const leftRect = this.leftElements[leftIndex].getBoundingClientRect();
      const rightRect = this.rightElements[rightIndex].getBoundingClientRect();
      
      this.ctx.strokeStyle = '#4CAF50';
      this.ctx.lineWidth = 4;
      this.ctx.lineCap = 'round';
      this.ctx.beginPath();
      this.ctx.moveTo(leftRect.right - containerRect.left, leftRect.top + leftRect.height / 2 - containerRect.top);
      this.ctx.lineTo(rightRect.left - containerRect.left, rightRect.top + rightRect.height / 2 - containerRect.top);
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
      instruction: `${base}/instructions_opposites.ogg`,
      target: `${base}/instructions_opposites.ogg`,
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
