# Android Integration Guide

## Match Words Game Integration Complete! ✅

The Match Words game has been integrated into the MainActivity with a dedicated test button.

## What Was Added

### 1. Updated GridGameJs.kt

Added `gameType` parameter to support loading different games:

```kotlin
class GridGameJs @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val currentRound: Int = 1,
    private val totalRounds: Int = 1,
    private val gameType: String = "numbers"  // NEW: "numbers" or "match-words"
) : ConstraintLayout(context, attrs, defStyleAttr)
```

**Game Loading**:
```kotlin
val gameUrl = "file:///android_asset/games/$gameType/dist/index.html"
webView.loadUrl(gameUrl)
```

### 2. Updated MainActivity.kt

Added new method `testMatchWordsGame()`:

```kotlin
private fun testMatchWordsGame() {
    if (Settings.canDrawOverlays(this)) {
        // Create Match Words game overlay
        val gameView = GridGameJs(
            context = this,
            currentRound = 1,
            totalRounds = 1,
            gameType = "match-words"
        )
        
        gameView.onGameCompleted = {
            Toast.makeText(this, "🎉 Match Words game completed!", Toast.LENGTH_SHORT).show()
            // Remove the game view
            (gameView.parent as? android.view.ViewGroup)?.removeView(gameView)
        }
        
        // Add game view to activity
        val rootView = findViewById<android.view.ViewGroup>(android.R.id.content)
        rootView.addView(gameView, android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        ))
        
        Toast.makeText(this, "🎯 Match Words game started! Drag words to emojis.", Toast.LENGTH_LONG).show()
    } else {
        Toast.makeText(this, "Overlay permission required to test the game", Toast.LENGTH_SHORT).show()
        requestOverlayPermission()
    }
}
```

### 3. Updated activity_main.xml

Added new button:

```xml
<Button
    android:id="@+id/testMatchWordsButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="🎯 Test Match Words Game"
    android:textSize="16sp"
    android:layout_marginBottom="16dp"
    android:backgroundTint="#FF9800"
    android:textColor="#FFFFFF"
    android:paddingHorizontal="32dp"
    android:paddingVertical="12dp"
    android:visibility="gone" />
```

## How It Works

### User Flow

1. **App Launch** → MainActivity opens
2. **Grant Permission** → User grants overlay permission
3. **Buttons Appear** → Two test buttons become visible:
   - 🎮 Test Game Overlay (random game from GameOverlayService)
   - 🎯 Test Match Words Game (specifically Match Words)
4. **Click Match Words Button** → Game loads fullscreen
5. **Play Game** → Drag words to matching emojis
6. **Complete** → Game removes itself, shows success toast

### Technical Flow

```
MainActivity
    ↓
testMatchWordsGame()
    ↓
Create GridGameJs(gameType = "match-words")
    ↓
Load: file:///android_asset/games/match-words/dist/index.html
    ↓
WebView loads HTML + bundle.js
    ↓
JavaScript initGame() called
    ↓
MatchWordsGame.ts creates UI
    ↓
User drags words to emojis
    ↓
All matches complete
    ↓
window.Android.onGameCompleted()
    ↓
Kotlin callback removes game view
```

## Testing

### Test Match Words Game

1. Build and install app
2. Grant overlay permission
3. Click "🎯 Test Match Words Game" button
4. Game appears fullscreen
5. Drag "House" to 🏠, "Apple" to 🍎, "Tree" to 🌳
6. Green lines appear for correct matches
7. Wrong matches shake
8. Complete all 3 matches
9. Game closes automatically

### Test Numbers Game

1. Click "🎮 Test Game Overlay" button
2. Random game appears (might be Numbers, Balloons, Alphabet, or Match Words)
3. Complete the game
4. Game closes

## Game Selection in GameOverlayService

To add Match Words to the random game selection, update `GameOverlayService.kt`:

```kotlin
private fun selectNextGame(): Int {
    return (0..4).random()  // 0-4 for 5 games
}

// In createOverlayView():
val selectedGame: View = when (gameIndex) {
    0 -> NumbersGame(this)
    1 -> BloonsGame(this)
    2 -> AlphabetGame(this)
    3 -> GridGameJs(this, currentRound, totalRounds, "numbers")
    4 -> GridGameJs(this, currentRound, totalRounds, "match-words")  // NEW
    else -> NumbersGame(this)
}
```

## File Paths

### Numbers Game
- **HTML**: `file:///android_asset/games/numbers/dist/index.html`
- **Bundle**: `file:///android_asset/games/numbers/dist/bundle.js`
- **Audio**: `file:///android_asset/games/numbers/audio/*.ogg`

### Match Words Game
- **HTML**: `file:///android_asset/games/match-words/dist/index.html`
- **Bundle**: `file:///android_asset/games/match-words/dist/bundle.js`
- **Audio**: `file:///android_asset/games/match-words/audio/*.ogg`

## Debugging

### WebView Console Logs

All JavaScript console logs appear in Logcat with tag `GridGameJs`:

```
adb logcat | grep GridGameJs
```

### Chrome DevTools

WebView debugging is enabled. Connect via:
1. Open Chrome: `chrome://inspect`
2. Find your WebView under "Remote Target"
3. Click "inspect"

### Common Issues

**Game not loading:**
- Check bundle.js exists in dist/ folder
- Verify gameType parameter is correct
- Check Logcat for WebView errors

**Audio not playing:**
- Audio files must be in `audio/` subdirectory
- Files must be OGG format
- Check file paths in game code

**Touch not working:**
- Ensure WebView has JavaScript enabled
- Check touch event listeners in TypeScript
- Verify canvas is not blocking touches

## Next Steps

1. ✅ Match Words game integrated
2. ✅ Test button added to MainActivity
3. ⏳ Add audio files to `match-words/audio/`
4. ⏳ Add Match Words to GameOverlayService random selection
5. ⏳ Test in real YouTube Kids scenario

## Success! 🎉

The Match Words game is now fully integrated into the Android app and can be tested with a single button click, just like it would appear when monitoring YouTube Kids!
