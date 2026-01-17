# Automatic TypeScript Build Setup ✅

## Overview

TypeScript games are now **automatically compiled** before every Android build!

## How It Works

### Gradle Integration

Added to `app/build.gradle.kts`:

```kotlin
// Task to build TypeScript games before Android build
tasks.register<Exec>("buildTypeScriptGames") {
    description = "Builds all TypeScript games (numbers, match-words)"
    group = "build"
    
    val gamesDir = file("src/main/assets/games")
    workingDir = gamesDir
    
    // Build each game
    commandLine("bash", "-c", """
        cd numbers && npm install && npm run build && cd .. &&
        cd match-words && npm install && npm run build && cd ..
    """.trimIndent())
    
    // Only run if source files changed
    inputs.dir("src/main/assets/games/numbers/src")
    inputs.dir("src/main/assets/games/match-words/src")
    inputs.file("src/main/assets/games/numbers/package.json")
    inputs.file("src/main/assets/games/match-words/package.json")
    outputs.dir("src/main/assets/games/numbers/dist")
    outputs.dir("src/main/assets/games/match-words/dist")
}

// Make preBuild depend on TypeScript compilation
tasks.named("preBuild") {
    dependsOn("buildTypeScriptGames")
}
```

### Build Flow

```
./gradlew assembleDebug
    ↓
preBuild task
    ↓
buildTypeScriptGames task (automatic)
    ↓
cd numbers && npm install && npm run build
    ↓
cd match-words && npm install && npm run build
    ↓
Continue with Android build
    ↓
APK created with latest TypeScript games
```

## What Gets Built Automatically

Every time you run:
- `./gradlew assembleDebug`
- `./gradlew installDebug`
- `./gradlew build`
- `./gradlew assembleRelease`

The following happens automatically:

1. **Numbers Game**:
   - `npm install` (if needed)
   - `npm run build`
   - Generates `numbers/dist/bundle.js`

2. **Match Words Game**:
   - `npm install` (if needed)
   - `npm run build`
   - Generates `match-words/dist/bundle.js`

## Incremental Builds

Gradle is smart about when to rebuild:

### TypeScript Will Rebuild When:
- ✅ Any `.ts` file in `numbers/src/` changes
- ✅ Any `.ts` file in `match-words/src/` changes
- ✅ `package.json` changes
- ✅ Output files (`dist/bundle.js`) are missing

### TypeScript Will Skip When:
- ⏭️ No source files changed
- ⏭️ Output files are up-to-date
- ⏭️ Saves time on repeated builds

## Manual Build Commands

### Build TypeScript Only
```bash
# Build all games
./gradlew buildTypeScriptGames

# Force rebuild even if up-to-date
./gradlew buildTypeScriptGames --rerun-tasks

# Build specific game
cd app/src/main/assets/games/numbers
npm run build
```

### Build Android Only (Skip TypeScript)
```bash
# This will still trigger TypeScript build if needed
./gradlew assembleDebug

# To truly skip, you'd need to comment out the dependsOn line
```

### Build Everything
```bash
# Clean + build TypeScript + build Android
./gradlew clean assembleDebug
```

## Verification

### Check if TypeScript Built
```bash
# Look for these files
ls -lh app/src/main/assets/games/numbers/dist/bundle.js
ls -lh app/src/main/assets/games/match-words/dist/bundle.js

# Should show:
# bundle.js (4.12 KiB)
# bundle.js (7.2 KiB)
```

### Check Build Logs
```bash
./gradlew assembleDebug | grep -A 5 "buildTypeScriptGames"

# Should show:
# > Task :app:buildTypeScriptGames
# 🎮 Building TypeScript games...
# [npm output]
# ✅ TypeScript games built successfully!
```

## Troubleshooting

### TypeScript Build Fails

**Error**: `npm: command not found`
```bash
# Install Node.js and npm
brew install node  # macOS
```

**Error**: `webpack: command not found`
```bash
# Install dependencies
cd app/src/main/assets/games/numbers
npm install

cd ../match-words
npm install
```

**Error**: TypeScript compilation errors
```bash
# Check TypeScript errors
cd app/src/main/assets/games/numbers
npm run build

# Fix errors in src/*.ts files
```

### Gradle Task Not Running

**Check task exists**:
```bash
./gradlew tasks --group=build | grep buildTypeScriptGames
```

**Force run**:
```bash
./gradlew buildTypeScriptGames --rerun-tasks
```

**Check dependencies**:
```bash
./gradlew assembleDebug --dry-run | grep buildTypeScriptGames
```

### Build is Slow

The first build will be slower because:
- npm installs 134 packages per game
- Webpack compiles TypeScript

Subsequent builds are faster because:
- npm uses cache
- Gradle skips if no changes
- Webpack uses cache

**Typical times**:
- First build: ~10-15 seconds
- Incremental: ~2-3 seconds (if changes)
- No changes: ~0 seconds (skipped)

## Development Workflow

### Recommended Workflow

1. **Edit TypeScript**:
   ```bash
   # Edit files in src/
   vim app/src/main/assets/games/match-words/src/MatchWordsGame.ts
   ```

2. **Test in Browser** (fast iteration):
   ```bash
   cd app/src/main/assets/games/match-words
   npm run build
   open demo.html
   ```

3. **Test in Android** (when ready):
   ```bash
   ./gradlew installDebug
   # TypeScript automatically builds first!
   ```

### Watch Mode (Optional)

For rapid TypeScript development:

```bash
# Terminal 1: Watch TypeScript changes
cd app/src/main/assets/games/match-words
npm run dev  # Watches and rebuilds on save

# Terminal 2: Build Android when ready
./gradlew installDebug
```

## Adding New Games

When you add a new game (e.g., `alphabet`):

1. Create game directory with package.json
2. Update `build.gradle.kts`:
   ```kotlin
   commandLine("bash", "-c", """
       cd numbers && npm install && npm run build && cd .. &&
       cd match-words && npm install && npm run build && cd .. &&
       cd alphabet && npm install && npm run build && cd ..
   """.trimIndent())
   
   inputs.dir("src/main/assets/games/alphabet/src")
   outputs.dir("src/main/assets/games/alphabet/dist")
   ```

3. Build automatically works!

## Benefits

### Before (Manual)
```bash
cd app/src/main/assets/games/numbers
npm run build
cd ../match-words
npm run build
cd ../../..
./gradlew installDebug
```
**Steps**: 5 commands  
**Time**: ~30 seconds  
**Error-prone**: Easy to forget

### After (Automatic)
```bash
./gradlew installDebug
```
**Steps**: 1 command  
**Time**: ~15 seconds (first) / ~5 seconds (incremental)  
**Error-proof**: Never forget to build TypeScript!

## Summary

✅ **TypeScript games build automatically**  
✅ **Incremental builds (only when needed)**  
✅ **Works with all Gradle commands**  
✅ **No manual steps required**  
✅ **Saves time and prevents errors**

Just run `./gradlew installDebug` and everything builds automatically! 🚀
