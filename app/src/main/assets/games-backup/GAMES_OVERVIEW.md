# PlayAndThen Games Overview

## Available Games

### 1. Numbers Game 🔢

**Objective**: Find the cell containing the target number (0-10)

**Gameplay**:
- 6 cells displayed with colorful numbers
- Audio announces target number
- Child clicks the correct cell
- Correct: Green feedback + success sound
- Incorrect: Red feedback + retry with correct answer shown

**Assets Required**:
- Audio: `zero.ogg` through `ten.ogg`
- Audio: `instructions_numbers_mode.ogg`
- Audio: `well_done.ogg`, `try_again.ogg`

**Location**: `numbers/`

---

### 2. Match Words Game 🎯

**Objective**: Drag lines from words to matching emojis

**Gameplay**:
- 3 random word-emoji pairs displayed
- Words in left column, emojis in right (shuffled)
- Child drags from word to emoji
- Line drawn during drag
- Correct: Green + line stays connected
- Incorrect: Shake animation + retry
- Complete when all 3 pairs matched

**Word-Emoji Pairs**:
1. Apple 🍎
2. Cat 🐱
3. Sun ☀️
4. Tree 🌳
5. Car 🚗
6. House 🏠
7. Star ⭐
8. Heart ❤️
9. Book 📚
10. Ball ⚽

**Assets Required**:
- Audio: `apple.ogg`, `cat.ogg`, `sun.ogg`, etc.
- Audio: `instructions_match_words.ogg`
- Shared: `well_done.ogg`, `try_again.ogg` (from numbers/)

**Location**: `match-words/`

---

## Game Architecture

### TypeScript Implementation

```
GridGameTS (Abstract Base)
    ├── NumbersGameTS
    └── MatchWordsGame
```

**Base Class Methods**:
- `populateCells()` - Set up game UI
- `isCorrect()` - Check answer
- `getAudioFiles()` - Define audio paths
- `getHugeTextContent()` - Optional large text display
- `startGame()` - Initialize round
- `reset()` - Reset current round
- `destroy()` - Cleanup

### Kotlin Integration

```kotlin
// In GameOverlayService.kt
val gameView = GridGameJs(
    context = this,
    currentRound = currentRound,
    totalRounds = numberOfRounds,
    gameType = "numbers"  // or "match-words"
)

gameView.onGameCompleted = {
    // Handle completion
}
```

---

## Adding New Games

### Step-by-Step Guide

1. **Create Game Directory**
   ```bash
   mkdir -p your-game/audio
   ```

2. **Create TypeScript Class**
   ```typescript
   // src/YourGame.ts
   import { GridGameTS } from './GridGameTS';
   
   export class YourGame extends GridGameTS {
     protected populateCells(): void {
       // Your UI logic
     }
     
     protected isCorrect(index: number): boolean {
       // Your validation logic
     }
     
     protected getAudioFiles(): AudioFiles {
       return {
         instruction: 'your-game/audio/instructions.ogg',
         target: 'your-game/audio/target.ogg',
         wellDone: 'numbers/audio/well_done.ogg',
         tryAgain: 'numbers/audio/try_again.ogg'
       };
     }
     
     protected getHugeTextContent(): string | null {
       return null; // or return text to display
     }
   }
   ```

3. **Update Entry Point**
   ```typescript
   // src/index.ts
   import { YourGame } from './YourGame';
   
   window.initGame = (config: GameConfig, gameType: string = 'numbers') => {
     switch (gameType) {
       case 'your-game':
         currentGame = new YourGame(config);
         break;
       // ... other cases
     }
   };
   ```

4. **Add CSS Styles** (if needed)
   ```css
   /* styles.css */
   .your-game-specific-class {
     /* Your styles */
   }
   ```

5. **Create README**
   ```markdown
   # Your Game - Audio Files
   
   ## Required Audio Files
   - instructions.ogg
   - target.ogg
   - etc.
   ```

6. **Build**
   ```bash
   npm run build
   ```

7. **Test**
   - Open `demo.html`
   - Add your game to dropdown
   - Test all interactions

---

## Game Design Guidelines

### Audio
- **Format**: OGG Vorbis
- **Sample Rate**: 44.1kHz or 48kHz
- **Bitrate**: 128kbps minimum
- **Duration**: 1-3 seconds per clip
- **Voice**: Clear, child-friendly
- **Volume**: Consistent across all files

### Visual Design
- **Colors**: Bright, high contrast
- **Fonts**: Large, bold, easy to read
- **Animations**: Smooth, not too fast
- **Feedback**: Immediate and clear
- **Touch Targets**: Minimum 44x44px

### Interaction
- **Response Time**: < 100ms
- **Feedback**: Visual + audio
- **Error Handling**: Graceful, encouraging
- **Accessibility**: Touch and mouse support

### Difficulty
- **Progressive**: Start easy, increase complexity
- **Age Appropriate**: 3-7 years old
- **Completion Time**: 30-60 seconds per round
- **Success Rate**: 70-80% target

---

## Testing Checklist

### Functional Testing
- [ ] Game loads without errors
- [ ] Audio plays correctly
- [ ] Touch/click interactions work
- [ ] Correct answers recognized
- [ ] Incorrect answers handled
- [ ] Feedback sounds play
- [ ] Round completion triggers callback
- [ ] Multi-round progression works

### Visual Testing
- [ ] Layout responsive on different screens
- [ ] Colors accessible (contrast ratio)
- [ ] Animations smooth
- [ ] No visual glitches
- [ ] Text readable

### Performance Testing
- [ ] Loads in < 500ms
- [ ] No memory leaks
- [ ] Smooth 60fps animations
- [ ] Audio latency < 100ms

### Edge Cases
- [ ] Rapid clicking handled
- [ ] Audio interruption handled
- [ ] Screen rotation handled
- [ ] Low memory conditions
- [ ] Network issues (if applicable)

---

## Troubleshooting

### Audio Not Playing
1. Check file exists in correct directory
2. Verify path in `getAudioFiles()`
3. Check OGG format compatibility
4. Review browser console for errors

### Game Not Loading
1. Run `npm run build`
2. Check `dist/bundle.js` exists
3. Review TypeScript compilation errors
4. Check WebView console logs

### Touch/Click Not Working
1. Verify event listeners attached
2. Check z-index layering
3. Test on different devices
4. Review CSS pointer-events

### Visual Issues
1. Check CSS class names
2. Verify styles.css loaded
3. Test responsive breakpoints
4. Review browser compatibility

---

## Performance Optimization

### Best Practices
- Preload audio files
- Use CSS transforms for animations
- Minimize DOM manipulations
- Debounce rapid events
- Clean up event listeners
- Optimize canvas drawing

### Memory Management
- Remove unused event listeners
- Clear canvas when not needed
- Dispose audio objects
- Avoid memory leaks in closures

---

## Future Game Ideas

1. **Alphabet Game** - Find Hebrew letters (א-ת)
2. **Balloons Game** - Count balloons (0-10)
3. **Colors Game** - Match color names to swatches
4. **Shapes Game** - Identify geometric shapes
5. **Animals Game** - Match animal sounds to pictures
6. **Sequence Game** - Complete number/letter sequences
7. **Memory Game** - Match pairs of cards
8. **Puzzle Game** - Simple jigsaw puzzles
9. **Sorting Game** - Sort items by category
10. **Pattern Game** - Complete visual patterns

---

## Resources

### Documentation
- Main README: `README.md`
- Numbers Game: `numbers/README.md`
- Match Words: `match-words/README.md`
- Refactoring Guide: `REFACTORING.md`

### Tools
- TypeScript: https://www.typescriptlang.org/
- Webpack: https://webpack.js.org/
- Canvas API: https://developer.mozilla.org/en-US/docs/Web/API/Canvas_API
- Web Audio API: https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API

### Testing
- Demo Page: `demo.html`
- Chrome DevTools: `chrome://inspect`
- Android Logcat: Filter by "GridGameJs"

---

## Contributing

When adding new games:
1. Follow existing patterns
2. Document audio requirements
3. Add to demo.html dropdown
4. Update this overview
5. Test thoroughly
6. Submit with examples

---

## License

Part of PlayAndThen project. See main LICENSE file.
