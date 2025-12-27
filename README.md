# PlayAndThen

An Android parental control app for YouTube Kids that displays educational games as overlays before videos play, while monitoring content language and enforcing viewing policies.

## What It Does

- Monitors YouTube Kids activity via accessibility service
- Shows interactive games (numbers, Hebrew alphabet, balloons) before videos
- Detects and filters video language (Hebrew, Russian, English)
- Implements rate limiting to prevent excessive skipping
- Provides configurable parental controls

## Setup

### Prerequisites
- Android Studio
- Android SDK API 34+
- Gradle 8.7+

### Build & Install

```bash
# Clone the repository
git clone <repository-url>
cd PlayAndThen

# Build the app
./gradlew assembleDebug

# Install on device
./gradlew installDebug
```

## Permissions Setup

The app requires two critical Android permissions to function:

### 1. Accessibility Service

1. Go to **Settings** → **Accessibility**
2. Find **Accessibility apps** or **Downloaded services**
3. Locate **PlayAndThen** in the list
4. Toggle it **ON**
5. Confirm the permission prompt

**Purpose:** Allows the app to monitor YouTube Kids activity and detect video content.

### 2. Display Over Other Apps

1. Go to **Settings** → **Apps**
2. Select **Special app access** (or **Advanced**)
3. Find **Display over other apps**
4. Locate **PlayAndThen** in the list
5. Toggle **Allow permission** to **ON**

**Purpose:** Enables the app to show game overlays on top of YouTube Kids.

## Configuration

Access the Settings screen in the app to customize behavior:

### Maximum Skips
- **What it does:** Controls how many videos can be auto-skipped within the time window
- **Default:** 5 skips
- **Purpose:** Prevents excessive content filtering that might frustrate the child

### Time Window (minutes)
- **What it does:** Sets the duration for the skip rate limit
- **Default:** 10 minutes
- **Purpose:** Defines the period within which the skip limit applies (e.g., 5 skips per 10 minutes)

### Language Violation Threshold
- **What it does:** Number of non-compliant videos before returning to YouTube Kids home screen
- **Default:** 3 violations
- **Purpose:** If rate limit is reached and non-compliant videos continue, this forces navigation back to home

### Game Overlay Interval (minutes)
- **What it does:** Minimum time between game overlays
- **Default:** 15 minutes
- **Purpose:** Prevents games from appearing too frequently, maintaining a balance between education and viewing

## Usage

1. Complete the permissions setup (see above)
2. Open YouTube Kids - the app will monitor in the background
3. Games will appear automatically before videos play
4. Adjust settings as needed via the Settings button in the PlayAndThen app

## ⚠️ Important: Audio Recordings

**This repository does NOT include audio recordings for the games.**

Each user must provide their own recordings in the `app/src/main/res/raw/` directory:

### Required Audio Files (OGG format):

**Numbers (0-10):**
`zero.ogg`, `one.ogg`, `two.ogg`, `three.ogg`, `four.ogg`, `five.ogg`, `six.ogg`, `seven.ogg`, `eight.ogg`, `nine.ogg`, `ten.ogg`

**Hebrew Alphabet (א-י):**
`aleph.ogg`, `bet.ogg`, `gimel.ogg`, `dalet.ogg`, `heh.ogg`, `vav.ogg`, `zayin.ogg`, `het.ogg`, `tet.ogg`, `yod.ogg`, 
`kaf.ogg`, `lamed.ogg`, `mem.ogg`, `nun.ogg`, `samekh.ogg`, `ayin.ogg`, `peh.ogg`, `tzadi.ogg`, `qof.ogg`, `resh.ogg`, `shin.ogg`, `taf.ogg`

**Game Instructions:**
- `instructions_numbers_mode.ogg` - what to do in numbers game i.e "choose a number..." 
- `instructions_alphabet_mode.ogg`- what to do in alphabet game i.e "choose a letter..." 
- `instructions_baloons_mode.ogg` - what to do in baloons game i.e "choose a cell where number of baloons is..." 

**Feedback:**
- `well_done.ogg` - Success message
- `try_again.ogg` - Retry message

Without these recordings, the games will not have audio instructions or feedback.

## Testing

```bash
./gradlew test
```


## Author

Created by Mishanous
