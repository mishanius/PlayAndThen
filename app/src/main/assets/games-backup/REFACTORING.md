# Games Directory Refactoring

## Overview

The games directory has been refactored to support multiple game types with organized asset management. Each game now has its own dedicated directory for audio files and other assets.

## Changes Made

### 1. Directory Structure Reorganization

**Before:**
```
games/
├── audio/              # All audio files mixed together
│   ├── zero.ogg
│   ├── one.ogg
│   ├── well_done.ogg
│   └── ...
└── src/
    ├── GridGameTS.ts
    ├── NumbersGameTS.ts
    └── index.ts
```

**After:**
```
games/
├── numbers/            # Numbers game assets
│   ├── audio/         # Numbers-specific audio
│   │   ├── zero.ogg
│   │   ├── one.ogg
│   │   ├── well_done.ogg
│   │   └── ...
│   └── README.md
├── match-words/        # Match Words game assets
│   ├── audio/         # Match Words audio
│   │   ├── instructions_match_words.ogg
│   │   ├── apple.ogg
│   │   ├── cat.ogg
│   │   └── ...
│   ├── images/        # Optional images (currently using emojis)
│   └── README.md
└── src/
    ├── GridGameTS.ts
    ├── NumbersGameTS.ts
    ├── MatchWordsGame.ts  # NEW
    └── index.ts
```

### 2. New Game: Match Words

A new educational game has been added where children drag lines from words to corresponding emojis.

**Features:**
- 10 word-emoji pairs (Apple 🍎, Cat 🐱, Sun ☀️, etc.)
- 3 random pairs shown per round
- Touch/mouse drag interaction
- Visual line drawing with canvas
- Correct matches turn green and stay connected
- Wrong matches shake for feedback

**Implementation:**
- `src/MatchWordsGame.ts` - Main game logic
- `match-words/audio/` - Audio files for word pronunciations
- CSS styles added to `styles.css`

### 3. Updated Files

#### TypeScript Files
- **NumbersGameTS.ts**: Updated audio paths from `audio/` to `numbers/audio/`
- **index.ts**: Added game type parameter and MatchWordsGame support
- **MatchWordsGame.ts**: New game implementation

#### CSS Files
- **styles.css**: Added Match Words game styles including:
  - `.match-words-layout` - Two-column layout
  - `.word-item` / `.emoji-item` - Interactive elements
  - `.matched` / `.drawing` / `.wrong` - State styles
  - `#match-canvas` - Canvas for drawing lines
  - Responsive breakpoints for mobile

#### HTML Files
- **demo.html**: Added game type selector dropdown

#### Documentation
- **README.md**: Updated with new structure and Match Words info
- **numbers/README.md**: Numbers game audio requirements
- **match-words/README.md**: Match Words audio requirements
- **REFACTORING.md**: This file

### 4. Audio File Organization

Audio files are now organized by game type:

**Shared Audio** (used by multiple games):
- `well_done.ogg` - Located in `numbers/audio/`
- `try_again.ogg` - Located in `numbers/audio/`

**Numbers Game Audio** (`numbers/audio/`):
- Number pronunciations (0-10)
- Instructions
- Feedback sounds

**Match Words Audio** (`match-words/audio/`):
- Word pronunciations (apple, cat, sun, etc.)
- Instructions
- References shared feedback sounds

## Migration Guide

### For Developers

1. **Update audio file paths** in any custom games:
   ```typescript
   // Old
   instruction: 'audio/instructions.ogg'
   
   // New
   instruction: 'your-game/audio/instructions.ogg'
   ```

2. **Rebuild the project**:
   ```bash
   npm run build
   ```

3. **Test both game types**:
   - Open `demo.html` in browser
   - Select "Numbers Game" and test
   - Select "Match Words Game" and test

### For Content Creators

1. **Add audio files** to appropriate directories:
   - Numbers game: `numbers/audio/`
   - Match Words: `match-words/audio/`

2. **Follow naming conventions**:
   - See individual game README files for required filenames
   - Use OGG format
   - Keep consistent volume levels

## Benefits of Refactoring

1. **Better Organization**: Each game has its own asset directory
2. **Scalability**: Easy to add new games without cluttering
3. **Maintainability**: Clear separation of concerns
4. **Documentation**: Each game has its own README
5. **Flexibility**: Games can have different asset types (audio, images, etc.)

## Testing Checklist

- [x] Numbers game loads and plays correctly
- [x] Match Words game loads and plays correctly
- [x] Audio files play from correct paths
- [x] Demo page game selector works
- [x] TypeScript compiles without errors
- [x] CSS styles apply correctly
- [x] Touch/mouse interactions work
- [x] Line drawing works on canvas
- [x] Game completion callbacks fire
- [x] Round transitions work

## Future Enhancements

1. **More Games**: Add Alphabet and Balloons games to TypeScript
2. **Shared Assets**: Create a `shared/` directory for common assets
3. **Asset Loader**: Implement dynamic asset loading system
4. **Game Registry**: Create a central registry for all available games
5. **Localization**: Support multiple languages per game

## Breaking Changes

⚠️ **Audio Path Changes**: If you have custom games, update audio paths:
- Old: `audio/filename.ogg`
- New: `game-name/audio/filename.ogg`

## Rollback Instructions

If needed, to rollback to the previous structure:

1. Move all audio files back to root `audio/` directory
2. Revert `NumbersGameTS.ts` audio paths
3. Remove `MatchWordsGame.ts`
4. Revert `index.ts` changes
5. Remove game-specific directories
6. Rebuild: `npm run build`

## Questions?

See individual game README files:
- `numbers/README.md`
- `match-words/README.md`

Or check the main `README.md` for development workflow.
