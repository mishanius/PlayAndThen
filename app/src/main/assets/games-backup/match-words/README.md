# Match Words Game - Audio Files

This directory contains audio files for the Match Words game where children drag lines from words to corresponding emojis.

## Required Audio Files

Place the following audio files in the `audio/` subdirectory in OGG format:

### Instructions
- `instructions_match_words.ogg` - Game instructions (e.g., "Match each word to its picture")

### Word Pronunciations
The game uses 10 word-emoji pairs. You need audio files for each word:

1. `apple.ogg` - 🍎 Apple
2. `cat.ogg` - 🐱 Cat
3. `sun.ogg` - ☀️ Sun
4. `tree.ogg` - 🌳 Tree
5. `car.ogg` - 🚗 Car
6. `house.ogg` - 🏠 House
7. `star.ogg` - ⭐ Star
8. `heart.ogg` - ❤️ Heart
9. `book.ogg` - 📚 Book
10. `ball.ogg` - ⚽ Ball

### Feedback Sounds
These are shared with the numbers game:
- `well_done.ogg` - Success message (located in `../numbers/audio/`)
- `try_again.ogg` - Retry message (located in `../numbers/audio/`)

## Game Mechanics

1. **Display**: Shows 3 random word-emoji pairs
   - Words appear in the left column
   - Emojis appear in the right column (shuffled order)

2. **Interaction**: 
   - Child drags from a word to an emoji
   - A line is drawn during the drag
   - Correct matches turn green and stay connected
   - Wrong matches shake and allow retry

3. **Completion**: 
   - Game completes when all 3 pairs are correctly matched
   - Success sound plays
   - Advances to next round

## Audio Recording Tips

- Use clear, child-friendly voice
- Keep recordings short (1-2 seconds per word)
- Maintain consistent volume across all files
- Use OGG Vorbis format for compatibility
- Sample rate: 44.1kHz or 48kHz recommended
- Bitrate: 128kbps or higher

## File Structure

```
match-words/
├── audio/
│   ├── instructions_match_words.ogg
│   ├── apple.ogg
│   ├── cat.ogg
│   ├── sun.ogg
│   ├── tree.ogg
│   ├── car.ogg
│   ├── house.ogg
│   ├── star.ogg
│   ├── heart.ogg
│   ├── book.ogg
│   └── ball.ogg
├── images/ (optional - currently using emojis)
└── README.md (this file)
```

## Customization

To add more word-emoji pairs, edit `src/MatchWordsGame.ts` and add entries to the `wordImagePairs` array:

```typescript
{ word: 'NewWord', emoji: '🆕', audioFile: 'newword.ogg' }
```

Then record and add the corresponding audio file to the `audio/` directory.
