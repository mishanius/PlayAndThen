# Complete Game Isolation Plan

## Current Status

The games directory has been partially refactored:
- ✅ Audio files moved to game-specific directories
- ✅ Game-specific README files created
- ❌ Games still share build system (root package.json, webpack, tsconfig)
- ❌ Games still share source code (src/ directory)
- ❌ Games still share dependencies (node_modules)

## Goal

Create **completely isolated** game directories where each game is a standalone application:

```
games/
├── numbers/
│   ├── package.json          # Own dependencies
│   ├── webpack.config.js     # Own build config
│   ├── tsconfig.json         # Own TypeScript config
│   ├── src/
│   │   ├── index.ts
│   │   ├── NumbersGame.ts
│   │   ├── types.ts
│   │   └── styles.css
│   ├── dist/
│   │   ├── index.html
│   │   └── bundle.js
│   ├── audio/
│   │   ├── zero.ogg
│   │   ├── one.ogg
│   │   └── ...
│   ├── node_modules/         # Own dependencies
│   ├── demo.html             # Own test page
│   └── README.md
│
├── match-words/
│   ├── package.json
│   ├── webpack.config.js
│   ├── tsconfig.json
│   ├── src/
│   │   ├── index.ts
│   │   ├── MatchWordsGame.ts
│   │   ├── types.ts
│   │   └── styles.css
│   ├── dist/
│   │   ├── index.html
│   │   └── bundle.js
│   ├── audio/
│   │   ├── apple.ogg
│   │   ├── cat.ogg
│   │   └── ...
│   ├── node_modules/
│   ├── demo.html
│   └── README.md
│
└── README.md
```

## Implementation Steps

### Step 1: Create Numbers Game Structure

```bash
cd app/src/main/assets/games/numbers

# Create package.json
cat > package.json << 'EOF'
{
  "name": "playandthen-numbers-game",
  "version": "1.0.0",
  "description": "Numbers game (0-10) for PlayAndThen",
  "private": true,
  "scripts": {
    "build": "webpack --mode production",
    "dev": "webpack --mode development --watch"
  },
  "devDependencies": {
    "typescript": "^5.3.3",
    "webpack": "^5.89.0",
    "webpack-cli": "^5.1.4",
    "ts-loader": "^9.5.1"
  }
}
EOF

# Create webpack.config.js
# Create tsconfig.json
# Create src/ directory with game code
# Create dist/index.html template
# Create demo.html for testing
```

### Step 2: Create Match Words Game Structure

Same process as Step 1, but for match-words game.

### Step 3: Remove Shared Files

```bash
cd app/src/main/assets/games
rm -rf src/ dist/ node_modules/
rm package.json package-lock.json webpack.config.js tsconfig.json
rm demo.html styles.css
```

### Step 4: Update Kotlin Integration

Update `GridGameJs.kt` to load games from their isolated directories:

```kotlin
// Old path
webView.loadUrl("file:///android_asset/games/dist/index.html")

// New path for numbers game
webView.loadUrl("file:///android_asset/games/numbers/dist/index.html")

// New path for match-words game
webView.loadUrl("file:///android_asset/games/match-words/dist/index.html")
```

## Benefits of Complete Isolation

1. **Independent Development**: Each game can be developed without affecting others
2. **Independent Versioning**: Each game can have different dependency versions
3. **Easier Maintenance**: Changes to one game don't risk breaking others
4. **Clearer Structure**: Each game is self-contained and easy to understand
5. **Easier Testing**: Each game can be tested independently
6. **Easier Deployment**: Games can be deployed/updated independently
7. **Better Scalability**: Easy to add new games without complexity

## Migration Checklist

### For Each Game:

- [ ] Create game directory structure
- [ ] Create package.json with dependencies
- [ ] Create webpack.config.js
- [ ] Create tsconfig.json
- [ ] Create src/ directory
- [ ] Copy/create game TypeScript files
- [ ] Copy/create styles.css
- [ ] Create dist/index.html template
- [ ] Create demo.html for testing
- [ ] Copy audio files (already done)
- [ ] Run `npm install`
- [ ] Run `npm run build`
- [ ] Test with demo.html
- [ ] Update README.md

### Global:

- [ ] Remove shared files from games/ root
- [ ] Update Kotlin GridGameJs.kt
- [ ] Update GameOverlayService.kt
- [ ] Test all games in Android app
- [ ] Update documentation

## File Templates

### package.json Template

```json
{
  "name": "playandthen-GAME_NAME-game",
  "version": "1.0.0",
  "description": "GAME_DESCRIPTION for PlayAndThen",
  "private": true,
  "scripts": {
    "build": "webpack --mode production",
    "dev": "webpack --mode development --watch"
  },
  "devDependencies": {
    "typescript": "^5.3.3",
    "webpack": "^5.89.0",
    "webpack-cli": "^5.1.4",
    "ts-loader": "^9.5.1"
  }
}
```

### webpack.config.js Template

```javascript
const path = require('path');

module.exports = {
  entry: './src/index.ts',
  module: {
    rules: [
      {
        test: /\.ts$/,
        use: 'ts-loader',
        exclude: /node_modules/,
      },
    ],
  },
  resolve: {
    extensions: ['.ts', '.js'],
  },
  output: {
    filename: 'bundle.js',
    path: path.resolve(__dirname, 'dist'),
  },
};
```

### tsconfig.json Template

```json
{
  "compilerOptions": {
    "target": "ES2015",
    "module": "commonjs",
    "lib": ["ES2015", "DOM"],
    "outDir": "./dist",
    "rootDir": "./src",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true
  },
  "include": ["src/**/*"],
  "exclude": ["node_modules", "dist"]
}
```

### dist/index.html Template

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>GAME_NAME Game</title>
    <style>
        /* Inline styles or link to styles.css */
    </style>
</head>
<body>
    <div id="game-container">
        <!-- Game UI -->
    </div>
    <script src="bundle.js"></script>
</body>
</html>
```

## Testing Strategy

1. **Unit Testing**: Test each game's TypeScript code independently
2. **Integration Testing**: Test game loading in Android WebView
3. **Manual Testing**: Use demo.html for each game
4. **Regression Testing**: Ensure existing games still work after refactoring

## Rollback Plan

If issues arise:
1. Backup exists at `games-backup/`
2. Restore shared structure
3. Revert Kotlin changes
4. Rebuild with original structure

## Timeline Estimate

- Numbers game isolation: 2-3 hours
- Match Words game isolation: 2-3 hours
- Kotlin integration updates: 1 hour
- Testing and debugging: 2-3 hours
- Documentation updates: 1 hour

**Total: 8-12 hours**

## Next Steps

1. Review this plan
2. Create isolated numbers game
3. Test numbers game
4. Create isolated match-words game
5. Test match-words game
6. Remove shared files
7. Update Kotlin code
8. Final testing
9. Update documentation

## Questions to Answer

1. Should games share any common utilities? (Recommendation: No, duplicate if needed)
2. Should there be a shared types file? (Recommendation: No, each game defines its own)
3. How to handle shared audio files like well_done.ogg? (Recommendation: Duplicate in each game)
4. Should demo.html be in each game or separate? (Recommendation: In each game for isolation)

## Conclusion

Complete isolation provides the best long-term maintainability and scalability. While it requires more initial setup and some code duplication, the benefits far outweigh the costs for a multi-game application.
