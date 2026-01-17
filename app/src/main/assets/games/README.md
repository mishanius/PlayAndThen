# Games Directory

This directory contains completely isolated game implementations. Each game is a standalone application with its own:
- Build system (package.json, webpack, TypeScript)
- Dependencies (node_modules)
- Assets (audio, images)
- Source code
- Documentation

## Structure

```
games/
├── numbers/          # Numbers game (0-10)
│   ├── package.json
│   ├── webpack.config.js
│   ├── tsconfig.json
│   ├── src/
│   ├── dist/
│   ├── audio/
│   └── README.md
│
├── match-words/      # Match words to emojis
│   ├── package.json
│   ├── webpack.config.js
│   ├── tsconfig.json
│   ├── src/
│   ├── dist/
│   ├── audio/
│   └── README.md
│
└── README.md         # This file
```

## Complete Isolation

Each game directory is **completely independent**:
- No shared code between games
- No shared dependencies
- No shared assets
- Each game can be developed, built, and deployed separately

## Adding a New Game

1. Create new directory: `mkdir my-game`
2. Copy structure from existing game
3. Customize package.json, source code, assets
4. Build independently: `cd my-game && npm install && npm run build`

## Building All Games

```bash
# Build numbers game
cd numbers && npm install && npm run build && cd ..

# Build match-words game
cd match-words && npm install && npm run build && cd ..
```

Or use a helper script (create `build-all.sh`):
```bash
#!/bin/bash
for dir in */; do
  if [ -f "$dir/package.json" ]; then
    echo "Building $dir..."
    (cd "$dir" && npm install && npm run build)
  fi
done
```
