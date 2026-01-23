# HLD: Epic 0 - Transfer to JS

## 1. Overview

Migrate game UI rendering to TypeScript/HTML/CSS running in WebView, enabling browser-based testing and faster iteration. Kotlin games remain functional (backward compatible). TS games must be pixel-perfect matches of Kotlin originals.

**Scope:**
- TS game framework with browser testing support
- Mock framework for Android bridge simulation
- Debug button for on-demand game testing (debug builds only)
- NumbersGame TS implementation matching Kotlin exactly

**Non-Goals:**
- Replacing Kotlin games (kept for backward compatibility)
- New game types (future work)
- Changing game logic in GameOverlayService

**Assumptions:**
- Games are stateless UI components
- All game logic (rounds, timing) stays in GameOverlayService
- Audio files copied to assets for browser testing

---

## 2. Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    GameOverlayService                        │
│  (unchanged - controls rounds, timing, game selection)       │
├─────────────────────────────────────────────────────────────┤
│                           │                                  │
│              ┌────────────┴────────────┐                    │
│              ▼                         ▼                    │
│    ┌──────────────────┐     ┌──────────────────┐           │
│    │  Kotlin Games    │     │   TS Games       │           │
│    │  (NumbersGame,   │     │   (WebView)      │           │
│    │   AlphabetGame,  │     │                  │           │
│    │   BloonsGame)    │     │  GridGameJs.kt   │           │
│    └──────────────────┘     └────────┬─────────┘           │
│                                      │                      │
│                                      ▼                      │
│                             ┌──────────────────┐           │
│                             │  games/          │           │
│                             │  ├── numbers/    │           │
│                             │  │   ├── src/    │           │
│                             │  │   ├── dist/   │           │
│                             │  │   └── demo.html│          │
│                             │  └── audio/      │           │
│                             └──────────────────┘           │
└─────────────────────────────────────────────────────────────┘

Browser Testing:
┌─────────────────────────────────────────────────────────────┐
│  test-harness.html                                           │
│  ├── Mock Android Bridge (window.Android)                   │
│  ├── Game Picker UI                                         │
│  ├── Round Config                                           │
│  └── Loads game iframe                                      │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Component Design

### 3.1 TS Game Structure
```
games/
├── audio/                    # Shared audio (copied from res/raw)
│   ├── zero.ogg ... ten.ogg
│   ├── aleph.ogg ... taf.ogg
│   ├── well_done.ogg
│   ├── try_again.ogg
│   └── instructions_*.ogg
├── numbers/
│   ├── src/
│   │   ├── NumbersGame.ts    # Must match Kotlin exactly
│   │   ├── types.ts
│   │   ├── styles.css
│   │   └── index.ts
│   ├── dist/
│   │   ├── index.html
│   │   └── bundle.js
│   └── demo.html             # Browser test harness
├── mock/
│   └── android-bridge.js     # Mock window.Android for browser
└── test-harness.html         # Master test page with game picker
```

### 3.2 Android Bridge Interface
```typescript
interface AndroidBridge {
  onGameCompleted(): void;
}

declare global {
  interface Window {
    Android?: AndroidBridge;
    initGame?(config: { currentRound: number; totalRounds: number }): void;
    resetGame?(): void;
  }
}
```

### 3.3 Mock Framework (browser testing)
```javascript
// mock/android-bridge.js
window.Android = {
  onGameCompleted: function() {
    console.log('[MOCK] Game completed!');
    // Show completion UI or auto-restart
  }
};
```

### 3.4 Debug Button (Android)
- Visible only when `BuildConfig.SHOW_DEBUG_BUTTON == true`
- Floating button in top-right corner
- Click shows game picker dialog (Numbers, Alphabet, Balloons, TS Numbers)
- Launches selected game with single round
- Uses `TYPE_APPLICATION_OVERLAY` window

---

## 4. Build Configuration

```kotlin
// app/build.gradle.kts
android {
    buildTypes {
        debug {
            buildConfigField("boolean", "SHOW_DEBUG_BUTTON", "true")
        }
        release {
            buildConfigField("boolean", "SHOW_DEBUG_BUTTON", "false")
        }
    }
}
```

---

## 5. Data Flow

**Browser Testing:**
```
test-harness.html → Load game iframe → Game runs → Calls mock Android.onGameCompleted()
                                                           ↓
                                                 Mock shows "Completed!" UI
```

**Android Production:**
```
GameOverlayService → GridGameJs (WebView) → Load index.html → Game runs
                                                    ↓
                                          Calls real Android.onGameCompleted()
                                                    ↓
                                          GameOverlayService handles next round
```

---

## 6. NumbersGame Parity Checklist

Must match Kotlin NumbersGame exactly:
- [ ] 6 cells in 2x3 grid
- [ ] Numbers 0-10, unique per cell
- [ ] Random target number
- [ ] Colorful number text (same color palette)
- [ ] Cell states: default (white), correct (green), incorrect (red)
- [ ] On correct: green highlight, well_done.ogg, 2s delay, onGameCompleted
- [ ] On incorrect: red highlight, show correct cell green, try_again.ogg, 3s delay, restart
- [ ] Speaker button to replay target number
- [ ] Audio sequence: instructions → target number
- [ ] Same fonts/sizes as Kotlin version

---

## 7. Risks

| Risk | Mitigation |
|------|------------|
| WebView audio latency | Pre-load audio files |
| Visual differences | Side-by-side comparison testing |
| Debug button visible in prod | BuildConfig flag, code review |

---

## 8. Task Plan

| # | Task | Dependencies | Goal |
|---|------|--------------|------|
| 01 | ts_game_infrastructure | - | Shared audio, mock framework, test harness |
| 02 | numbers_game_parity | 01 | Rewrite NumbersGame.ts to match Kotlin |
| 03 | debug_button | - | Floating debug button (debug builds only) |
| 04 | game_service_launch_api | 03 | GameOverlayService API for on-demand launch |
| 05 | testing_and_docs | 01,02,03,04 | E2E testing, documentation |
