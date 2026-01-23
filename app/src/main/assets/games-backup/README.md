# PlayAndThen TypeScript Games

This directory contains the TypeScript/JavaScript implementation of grid-based educational games for the PlayAndThen app.

## Architecture

### Overview
- **GridGameJs.kt**: Kotlin WebView wrapper that loads and manages JS games
- **GridGameTS.ts**: Abstract TypeScript base class (mirrors GridGame.kt)
- **NumbersGameTS.ts**: TypeScript implementation of the Numbers game
- **HTML/CSS**: Full WebView UI rendered in `dist/index.html` and `styles.css`

### Communication Bridge
- **Kotlin → TypeScript**: `initGame(config)`, `resetGame()`
- **TypeScript → Kotlin**: `window.Android.onGameCompleted()`

## Project Structure

```
games/
├── package.json              # npm configuration
├── tsconfig.json            # TypeScript compiler config
├── webpack.config.js        # Webpack bundler config
├── src/                     # TypeScript source files
│   ├── types.ts            # Shared TypeScript interfaces
│   ├── GridGameTS.ts       # Abstract base class
│   ├── NumbersGameTS.ts    # Numbers game implementation
│   ├── MatchWordsGame.ts   # Match Words game implementation
│   └── index.ts            # Entry point
├── dist/                    # Compiled output
│   ├── index.html          # Game HTML page
│   └── bundle.js           # Compiled JavaScript (generated)
├── numbers/                 # Numbers game assets
│   ├── audio/              # Numbers game audio files
│   │   ├── zero.ogg
│   │   ├── one.ogg
│   │   ├── ...
│   │   ├── well_done.ogg
│   │   ├── try_again.ogg
│   │   └── instructions_numbers_mode.ogg
│   └── README.md           # Numbers game documentation
├── match-words/             # Match Words game assets
│   ├── audio/              # Match Words audio files
│   │   ├── instructions_match_words.ogg
│   │   ├── apple.ogg
│   │   ├── cat.ogg
│   │   └── ...
│   ├── images/             # Optional images (currently using emojis)
│   └── README.md           # Match Words game documentation
├── demo.html                # Debug/testing page
└── styles.css               # Game CSS styles
```

## Development Workflow

### Prerequisites
- Node.js and npm installed
- TypeScript knowledge

### Initial Setup
```bash
cd app/src/main/assets/games
npm install
```

### Development (watch mode)
```bash
npm run dev
```
This watches for TypeScript file changes and automatically recompiles.

### Production Build
```bash
npm run build
```
Compiles TypeScript to optimized JavaScript bundle at `dist/bundle.js`.

### Adding Audio Files
Audio files are now organized by game type:
```bash
# Numbers game audio
cp app/src/main/res/raw/new_number.ogg app/src/main/assets/games/numbers/audio/

# Match Words game audio
cp app/src/main/res/raw/new_word.ogg app/src/main/assets/games/match-words/audio/
```

See individual game README files for required audio files:
- `numbers/README.md` - Numbers game audio requirements
- `match-words/README.md` - Match Words game audio requirements

## Creating New Games

### Available Game Types

1. **Numbers Game** (`NumbersGameTS`) - Find cells with target numbers (0-10)
2. **Match Words Game** (`MatchWordsGame`) - Drag lines from words to matching emojis

### 1. Create New Game Class
Create a new file `src/NewGameTS.ts`:

```typescript
import { GridGameTS } from './GridGameTS';
import { GameConfig, AudioFiles } from './types';

export class NewGameTS extends GridGameTS {
  constructor(config: GameConfig) {
    super(config);
  }

  protected populateCells(): void {
    // Your cell population logic
    // For custom layouts, override the entire game container
  }

  protected isCorrect(clickedCellIndex: number): boolean {
    // Your correctness check logic
    // Return true if the answer is correct
  }

  protected getAudioFiles(): AudioFiles {
    return {
      instruction: 'your-game/audio/instructions.ogg',
      target: 'your-game/audio/target.ogg',
      wellDone: 'numbers/audio/well_done.ogg',  // Shared feedback
      tryAgain: 'numbers/audio/try_again.ogg'   // Shared feedback
    };
  }

  protected getHugeTextContent(): string | null {
    // Return text to display above the game, or null
    return null;
  }
}
```

### 2. Update Entry Point
Modify `src/index.ts` to support the new game type:

```typescript
import { NewGameTS } from './NewGameTS';

// In initGame function:
switch (gameType) {
  case 'new-game':
    currentGame = new NewGameTS(config);
    break;
  // ... other cases
}
```

### 3. Create Game Assets Directory
```bash
mkdir -p your-game/audio
# Add required audio files
```

### 4. Rebuild
```bash
npm run build
```

## Integration with Android

### Using GridGameJs in Kotlin
```kotlin
val gameView = GridGameJs(
    context = this,
    currentRound = 1,
    totalRounds = 5,
    gameType = "numbers"  // or "match-words"
)

gameView.onGameCompleted = {
    // Handle game completion
}

// Add to view hierarchy
container.addView(gameView)
```

### GameOverlayService Integration
GridGameJs is automatically included in the random game selection in `GameOverlayService.kt`:
- Index 0-2: Kotlin games (Numbers, Bloons, Alphabet)
- Index 3: JavaScript/TypeScript games (Numbers, Match Words)

## Debugging

### WebView Console Logs
All JavaScript console logs appear in Android Logcat with tag `GridGameJs`:
```
Log.d("GridGameJs", "JS Console [INFO]: message")
```

### Chrome DevTools
WebView debugging is enabled. Connect via:
1. Open Chrome browser
2. Navigate to `chrome://inspect`
3. Find your WebView under "Remote Target"

### Common Issues

**Audio not playing:**
- Check audio files exist in `audio/` directory
- Verify file paths in `getAudioFiles()` method
- Check WebView console for errors

**WebView blank screen:**
- Verify `dist/bundle.js` exists and is up-to-date
- Run `npm run build` to regenerate bundle
- Check Android Logcat for WebView errors

**TypeScript compilation errors:**
- Run `npm run build` and check for errors
- Verify all TypeScript files have correct imports
- Check `tsconfig.json` configuration

## Testing

### Manual Testing
1. Build the TypeScript: `npm run build`
2. Build and install Android app
3. Trigger game overlay
4. Observe GridGameJs randomly appears alongside Kotlin games

### Verification Checklist
- [ ] TypeScript compiles without errors
- [ ] bundle.js generated in dist/ folder
- [ ] Audio files play correctly
- [ ] Cell clicks register and provide feedback
- [ ] Correct/incorrect answers show proper colors
- [ ] Game completion callback fires
- [ ] Round transitions work
- [ ] No WebView console errors

## Performance Notes

- WebView adds ~10-15MB memory overhead
- First load may take 100-200ms
- Subsequent game resets are fast (<50ms)
- Audio playback uses HTML5 Audio API
- No performance issues observed on mid-range devices

## Future Enhancements

Potential improvements for the TypeScript game system:

1. **More Games**: Port Bloons and Alphabet games to TypeScript
2. **Animations**: Add CSS animations for better visual feedback
3. **Shared Audio Manager**: Centralize audio handling between games
4. **Theming**: Support different visual themes
5. **Accessibility**: Add ARIA labels and keyboard navigation
6. **Multiplayer**: Add local multiplayer support via WebSockets

## Maintenance

### Updating Dependencies
```bash
npm update
npm audit fix  # Fix security vulnerabilities
```

### Cleaning Build Artifacts
```bash
rm -rf node_modules dist/bundle.js
npm install
npm run build
```

## License

Part of the PlayAndThen project. See main project LICENSE file.
