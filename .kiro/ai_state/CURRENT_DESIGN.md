# CURRENT_DESIGN: PlayAndThen

## 1. Overview

PlayAndThen is an Android parental control application for YouTube Kids that intercepts video playback to display educational games while monitoring content language and enforcing viewing policies through rate limiting.

### Scope
- Accessibility service integration for YouTube Kids monitoring
- Educational game overlays (Numbers, Hebrew Alphabet, Balloons)
- ML-powered language detection (English, Hebrew, Russian)
- Rate limiting for content skipping
- Watch time tracking with multi-round game support
- Configurable parental controls

### Non-Goals
- Support for other video apps beyond YouTube Kids
- Cloud sync or multi-device support
- User accounts or authentication
- Content recommendation or curation

### Assumptions
- Single device, single child usage model
- Parent configures settings before child use
- YouTube Kids is the only monitored application
- Audio files provided by user (not bundled)

---

## 2. Requirements (Architectural)

### Functional
- Monitor YouTube Kids via Android Accessibility Service
- Detect video playback states (mini-player, full-screen, strict mode)
- Extract video titles from accessibility tree
- Detect video language using ML Kit
- Skip non-compliant videos (non-Hebrew/English/Russian)
- Display game overlays before compliant videos
- Track watch time to determine game frequency
- Rate limit skipping to prevent excessive filtering
- Navigate back to home after violation threshold
- Support configurable thresholds via settings UI

---

## 3. Current Architecture

### System Context
```
┌─────────────────────────────────────────────────────────────┐
│                      Android Device                          │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────┐         ┌──────────────────┐          │
│  │   YouTube Kids   │◄───────►│   PlayAndThen    │          │
│  │   (Monitored)    │ A11y    │   (Controller)   │          │
│  └──────────────────┘ Events  └──────────────────┘          │
│                                        │                     │
│                                        ▼                     │
│                               ┌──────────────────┐          │
│                               │   Google ML Kit  │          │
│                               │ (Language ID)    │          │
│                               └──────────────────┘          │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

### Component Diagram
```
┌─────────────────────────────────────────────────────────────┐
│                      PlayAndThen App                         │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │ MainActivity │    │SettingsActivity│   │GameOverlay   │  │
│  │              │    │              │    │  Service     │  │
│  │ - Permissions│    │ - Config UI  │    │ - Game Views │  │
│  │ - Test Games │    │ - SharedPrefs│    │ - Foreground │  │
│  └──────────────┘    └──────────────┘    └──────────────┘  │
│                                                               │
│  ┌─────────────────────────────────────────────────────┐    │
│  │           GuardService (Accessibility)               │    │
│  │  ┌────────────────────────────────────────────┐     │    │
│  │  │         GuardManager (Core Logic)          │     │    │
│  │  │  - State Management                        │     │    │
│  │  │  - Video Detection                         │     │    │
│  │  │  - Rate Limiting                           │     │    │
│  │  │  - Watch Time Tracking                     │     │    │
│  │  └────────────────────────────────────────────┘     │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                               │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │MatchingEngine│    │LanguageDetect│    │ButtonExtractor│ │
│  │ - Tree Walk  │    │  - ML Kit    │    │ - UI Actions │  │
│  └──────────────┘    └──────────────┘    └──────────────┘  │
│                                                               │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │ NumbersGame  │    │ AlphabetGame │    │  BloonsGame  │  │
│  └──────────────┘    └──────────────┘    └──────────────┘  │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

### Data Flows

**Video Monitoring Flow:**
```
YouTube Kids Event → GuardService → GuardManager → MatchingEngine
                                         │
                                         ▼
                                  State Detection
                                         │
                    ┌────────────────────┼────────────────────┐
                    ▼                    ▼                    ▼
              Title Extract      Language Check         Rate Limit
                    │                    │                    │
                    └────────────────────┼────────────────────┘
                                         ▼
                                   Action Decision
                                         │
                    ┌────────────────────┼────────────────────┐
                    ▼                    ▼                    ▼
              Show Game            Skip Video           Hit Back
```

**Game Overlay Flow:**
```
Watch Time Threshold → Pause Video → GameOverlayService → Game Selection
                                                               │
                                                               ▼
                                                         Display Game
                                                               │
                                                               ▼
                                                         User Input
                                                               │
                                          ┌────────────────────┼────────────────────┐
                                          ▼                                         ▼
                                       Correct                                   Incorrect
                                          │                                         │
                                          ▼                                         ▼
                                    Next Round?                               Try Again
                                          │
                              ┌───────────┼───────────┐
                              ▼                       ▼
                           Yes                       No
                              │                       │
                              ▼                       ▼
                        Show Next Game         Hide Overlay
```

---

## 4. Interfaces & Contracts

### Internal APIs

**GuardManager Interface:**
```kotlin
interface GuardManager {
    fun updateRoot(root: AccessibilityNodeInfo?)
    fun skipVideo()
    fun pauseVideo()
    fun hitBackButton()
    fun clickMiddle()
}
```

**MatchingEngine Interface:**
```kotlin
interface MatchingEngine {
    fun walkTreeAndMatch(root: AccessibilityNodeInfo, matchers: List<NodeMatcher>): Array<NodeMatcher?>
}
```

**LanguageDetectionUtil Interface:**
```kotlin
interface LanguageDetectionUtil {
    fun detectAndValidateLanguage(text: String, callback: (String?, Boolean) -> Unit)
}
```

### Configuration (SharedPreferences)
| Key | Type | Default |
|-----|------|---------|
| `max_skips` | Int | 5 |
| `time_window_minutes` | Long | 10 |
| `language_violation_threshold` | Int | 3 |
| `game_overlay_interval_minutes` | Long | 15 |

### External Dependencies
- **Google ML Kit Language ID**: `com.google.mlkit:language-id:17.0.6`
- **Android Accessibility Service API**
- **Android System Alert Window**

---

## 5. Reliability & Failure Strategy

### Failure Modes
| Failure | Impact | Mitigation |
|---------|--------|------------|
| Accessibility service disabled | No monitoring | UI prompt to re-enable |
| ML Kit unavailable | No language detection | Default to compliant |
| Overlay permission revoked | No games shown | UI prompt to re-enable |
| YouTube Kids UI changes | State detection fails | Graceful degradation |

### Rate Limiting
- Skip history tracked with timestamps
- Old entries cleaned outside time window
- Prevents excessive content filtering

### Data Consistency
- In-memory state only (no persistence beyond SharedPreferences)
- Daily reset of video counts
- Watch time resets after 1-hour gap

---

## 6. Security & Privacy

### Permissions
- `SYSTEM_ALERT_WINDOW`: Display overlays
- `FOREGROUND_SERVICE`: Game overlay service
- `WAKE_LOCK`: Keep screen on during games
- `POST_NOTIFICATIONS`: Service notification

### Data Handling
- No user data collected or transmitted
- No network calls except ML Kit model download
- All processing local to device
- No PII stored

---

## 7. Observability

### Logging
- Accessibility tree dumps (debug)
- State transitions
- Language detection results
- Skip/violation events

### Metrics (In-App)
- Daily video count
- Accumulated watch time
- Skip history

---

## 8. Technical Specifications

### Performance
| Metric | Value |
|--------|-------|
| Event debounce | 5ms |
| State change debounce | 2000ms |
| Skip delay | 2000ms |
| Center click timeout | 3000ms |

### Android Requirements
- Min SDK: 33 (Android 13)
- Target SDK: 33
- Compile SDK: 33

### Language Support
- UI: English
- Content detection: English, Hebrew, Russian
- ML Kit confidence threshold: 50%

---

## 9. Risks & Open Questions

### Risks
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| YouTube Kids UI changes | Medium | High | Flexible matchers, logging |
| ML Kit accuracy issues | Low | Medium | Configurable threshold |
| Battery drain | Medium | Medium | Debouncing, efficient tree walk |

### Open Questions
- Should game types be configurable?
- Should language whitelist be user-configurable?
- Need analytics/reporting for parents?
- Support for additional video apps?

---

## 10. File Structure

```
app/src/main/
├── java/com/example/playandthen/
│   ├── MainActivity.kt
│   ├── SettingsActivity.kt
│   ├── GuardService.kt
│   ├── GuardManager.kt
│   ├── MatchingEngine.kt
│   ├── ButtonExtractor.kt
│   ├── LanguageDetectionUtil.kt
│   ├── GameOverlayService.kt
│   ├── games/
│   │   ├── NumbersGame.kt
│   │   ├── AlphabetGame.kt
│   │   ├── BloonsGame.kt
│   │   └── GridGameJs.kt
│   └── matchers/
│       ├── NodeMatcher.kt
│       ├── MiniPlayerMatcher.kt
│       ├── FullScreenVideoMatcher.kt
│       └── StrictFullScreenVideoMatcher.kt
├── res/
│   ├── layout/
│   ├── values/
│   └── raw/ (user-provided audio files)
└── AndroidManifest.xml
```

---

## 11. Dependencies

```kotlin
// Core Android
implementation("androidx.core:core-ktx")
implementation("androidx.appcompat:appcompat")
implementation("com.google.android.material:material")

// ML Kit Language Detection
implementation("com.google.mlkit:language-id:17.0.6")

// Testing
testImplementation("junit:junit")
testImplementation("org.mockito:mockito-core:5.1.1")
testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
```
