# Numbers Game - Audio Files

This directory contains audio files for the Numbers game where children find cells with target numbers.

## Required Audio Files

Place the following audio files in the `audio/` subdirectory in OGG format:

### Instructions
- `instructions_numbers_mode.ogg` - Game instructions (e.g., "Find the number...")

### Number Pronunciations (0-10)
- `zero.ogg` - Number 0
- `one.ogg` - Number 1
- `two.ogg` - Number 2
- `three.ogg` - Number 3
- `four.ogg` - Number 4
- `five.ogg` - Number 5
- `six.ogg` - Number 6
- `seven.ogg` - Number 7
- `eight.ogg` - Number 8
- `nine.ogg` - Number 9
- `ten.ogg` - Number 10

### Feedback Sounds
- `well_done.ogg` - Success message
- `try_again.ogg` - Retry message

## Game Mechanics

1. **Display**: Shows 6 cells with colorful numbers (0-10)
2. **Target**: One random number is selected as the target
3. **Interaction**: Child clicks the cell with the target number
4. **Feedback**: 
   - Correct: Cell turns green, success sound plays
   - Incorrect: Cell turns red, game resets after showing correct answer

## Audio Recording Tips

- Use clear, child-friendly voice
- Keep recordings short (1 second per number)
- Maintain consistent volume across all files
- Use OGG Vorbis format for compatibility
- Sample rate: 44.1kHz or 48kHz recommended
- Bitrate: 128kbps or higher

## File Structure

```
numbers/
├── audio/
│   ├── instructions_numbers_mode.ogg
│   ├── zero.ogg
│   ├── one.ogg
│   ├── two.ogg
│   ├── three.ogg
│   ├── four.ogg
│   ├── five.ogg
│   ├── six.ogg
│   ├── seven.ogg
│   ├── eight.ogg
│   ├── nine.ogg
│   ├── ten.ogg
│   ├── well_done.ogg
│   └── try_again.ogg
└── README.md (this file)
```
