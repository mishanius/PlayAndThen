package com.ptitsyn.playandthen

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import java.text.SimpleDateFormat
import java.util.*

/**
 * GuardManager represents all the relevant context of the current status of the user.
 * It holds the current PlayerState, video title, and language.
 * It provides methods that enable the service to interact with the app.
 */
class GuardManager(
    private val context: Context,
    private val gestureDispatcher: (GestureDescription, AccessibilityService.GestureResultCallback?) -> Unit
) {
    
    companion object {
        private const val TAG = "GuardManager"
        private const val CLICK_MIDDLE_TIMEOUT_MS = 3000L
        private const val STATE_DEBOUNCE_MS = 2000L
        private const val SKIP_DELAY_MS = 2000L  // Delay before skipping to allow video to load
        
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    // Current state properties
    private var currentRoot: AccessibilityNodeInfo? = null
    private var currentPlayerState: PlayerState = PlayerState.NONE
    private var currentVideoTitle: String? = null
    private var currentLanguage: String? = null
    
    // Matching engine for state detection
    private val matchingEngine = MatchingEngine()
    
    // For state change debouncing
    private val lastSeenStateMap = mutableMapOf<PlayerState, Long>()
    
    // Timer system for delayed actions
    private val handler = Handler(Looper.getMainLooper())
    private var delayedTouchRunnable: Runnable? = null
    private var delayedSkipRunnable: Runnable? = null
    
    // New tracking fields for enhanced features
    private var totalVideoCount = 0
    private var skipHistory = mutableListOf<Long>() // Timestamps of skips
    private var languageViolationCount = 0
    private var lastGameOverlayTime: Long = 0
    private var currentVideoStartTime: Long = 0
    private var wasAutoSkipped = false
    private var lastDailyResetDate: String = "" // Format: "yyyy-MM-dd"
    
    // Watch time tracking for multi-round games
    private var accumulatedWatchTimeMs: Long = 0
    private var lastWatchingUpdateTime: Long = 0
    private val MAX_GAP_BETWEEN_UPDATES_MS =  1 * 60 * 60 * 1000L // 1 hour
    private val MAX_GAP_BETWEEN_UPDATES_NON_WATCHING_MS =  10 * 60 * 1000L // 10 minutes

    /**
     * Gets configuration values from SharedPreferences with defaults.
     */
    private fun getMaxSkipsInTimeWindow(): Int {
        val prefs = SettingsActivity.getPreferences(context)
        return prefs.getInt(SettingsActivity.KEY_MAX_SKIPS, SettingsActivity.DEFAULT_MAX_SKIPS)
    }

    private fun getSkipTimeWindowMinutes(): Long {
        val prefs = SettingsActivity.getPreferences(context)
        return prefs.getLong(SettingsActivity.KEY_TIME_WINDOW_MINUTES, SettingsActivity.DEFAULT_TIME_WINDOW_MINUTES)
    }

    private fun getLanguageViolationThreshold(): Int {
        val prefs = SettingsActivity.getPreferences(context)
        return prefs.getInt(SettingsActivity.KEY_LANGUAGE_THRESHOLD, SettingsActivity.DEFAULT_LANGUAGE_THRESHOLD)
    }

    private fun getGameOverlayIntervalMinutes(): Long {
        val prefs = SettingsActivity.getPreferences(context)
        return prefs.getLong(SettingsActivity.KEY_GAME_OVERLAY_INTERVAL, SettingsActivity.DEFAULT_GAME_OVERLAY_INTERVAL)
    }

    /**
     * Updates the accessibility tree root and triggers state analysis.
     * This should be called whenever GuardService receives new accessibility events.
     */
    fun updateRoot(root: AccessibilityNodeInfo?) {
        currentRoot = root
        if (root != null) {
            analyzeCurrentState()
        }
    }

    /**
     * Clicks the middle of the screen with a timeout (currently 3 seconds).
     */
    fun clickMiddle() {
        // Cancel any previous delayed touch if it exists
        delayedTouchRunnable?.let { handler.removeCallbacks(it) }
        
        // Schedule center screen touch after timeout
        delayedTouchRunnable = Runnable {
            Log.d(TAG, "Executing delayed center screen touch after ${CLICK_MIDDLE_TIMEOUT_MS}ms")
            performCenterScreenTouch()
        }
        
        Log.d(TAG, "Scheduling center screen touch in ${CLICK_MIDDLE_TIMEOUT_MS}ms...")
        handler.postDelayed(delayedTouchRunnable!!, CLICK_MIDDLE_TIMEOUT_MS)
    }

    /**
     * Pauses the video by clicking the pause button.
     * Checks that we are in FULL_SCREEN_WITH_RECOMMENDATIONS state first.
     */
    fun pauseVideo() {
        Log.d(TAG, "pauseVideo() - Attempting to pause video")
        
        // Check if we are in the correct state
        if (currentPlayerState != PlayerState.FULL_SCREEN_WITH_RECOMMENDATIONS) {
            val error = "Cannot pause video - not in FULL_SCREEN_WITH_RECOMMENDATIONS state. Current state: $currentPlayerState"
            Log.e(TAG, error)
            throw IllegalStateException(error)
        }
        
        val root = currentRoot
        if (root == null) {
            val error = "Cannot pause video - root accessibility node is null"
            Log.e(TAG, error)
            throw IllegalStateException(error)
        }
        
        // Find the pause button
        val pauseButton = ButtonExtractor.findPauseButton(root)
        if (pauseButton == null) {
            val error = "Cannot pause video - pause button not found"
            Log.e(TAG, error)
            throw IllegalStateException(error)
        }
        
        // Click the pause button
        Log.d(TAG, "Found pause button, attempting to click it")
        val success = pauseButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        if (success) {
            Log.d(TAG, "Successfully clicked pause button")
        } else {
            Log.e(TAG, "Failed to click pause button")
            throw RuntimeException("Failed to click pause button")
        }
    }

    /**
     * Resumes/unpauses the video after pause.
     */
    fun unPauseVideo() {
        Log.d(TAG, "unPauseVideo() - Currently unimplemented")
        // TODO: Implement play button detection and clicking
    }

    /**
     * Clicks the next button to go to the next video.
     * Checks that we are in FULL_SCREEN_WITH_RECOMMENDATIONS state first.
     */
    fun nextVideo() {
        Log.d(TAG, "nextVideo() - Attempting to go to next video")
        
        // Check if we are in the correct state
        if (currentPlayerState != PlayerState.FULL_SCREEN_WITH_RECOMMENDATIONS) {
            val error = "Cannot go to next video - not in FULL_SCREEN_WITH_RECOMMENDATIONS state. Current state: $currentPlayerState"
            Log.e(TAG, error)
            throw IllegalStateException(error)
        }
        
        val root = currentRoot
        if (root == null) {
            val error = "Cannot go to next video - root accessibility node is null"
            Log.e(TAG, error)
            throw IllegalStateException(error)
        }
        
        // Find the next button
        val nextButton = ButtonExtractor.findNextButton(root)
        if (nextButton == null) {
            val error = "Cannot go to next video - next button not found"
            Log.e(TAG, error)
            throw IllegalStateException(error)
        }
        
        // Click the next button
        Log.d(TAG, "Found next button, attempting to click it")
        val success = nextButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        if (success) {
            Log.d(TAG, "Successfully clicked next button")
        } else {
            Log.e(TAG, "Failed to click next button")
            throw RuntimeException("Failed to click next button")
        }
    }

    // Getters for current state
    fun getCurrentPlayerState(): PlayerState = currentPlayerState
    fun getCurrentVideoTitle(): String? = currentVideoTitle
    fun getCurrentLanguage(): String? = currentLanguage
    fun getTotalVideoCount(): Int = totalVideoCount
    fun getSkipCount(): Int = skipHistory.size
    fun getLanguageViolationCount(): Int = languageViolationCount

    /**
     * Skips to the next video if rate limiting allows it.
     * Uses delayed execution to allow video to load properly before skipping.
     */
    fun skipVideo() {
        Log.d(TAG, "skipVideo() - Attempting to skip video")
        
        if (!shouldSkipVideo()) {
            Log.w(TAG, "Skip video blocked by rate limiting")
            return
        }
        
        // Cancel any previous delayed skip if it exists
        delayedSkipRunnable?.let { handler.removeCallbacks(it) }
        
        // Add current timestamp to skip history
        skipHistory.add(System.currentTimeMillis())
        Log.d(TAG, "Added skip to history. Total skips in window: ${skipHistory.size}")
        
        // Schedule delayed skip to allow video to load
        delayedSkipRunnable = Runnable {
            Log.d(TAG, "Executing delayed skip after ${SKIP_DELAY_MS}ms")
            performDelayedSkip()
        }
        
        Log.d(TAG, "Scheduling delayed skip in ${SKIP_DELAY_MS}ms...")
        handler.postDelayed(delayedSkipRunnable!!, SKIP_DELAY_MS)
        wasAutoSkipped = true
    }
    
    /**
     * Clicks the back button to return to main screen.
     * Resets language violation counter since user is leaving video context.
     */
    fun hitBackButton() {
        Log.d(TAG, "hitBackButton() - Attempting to go back to main screen")
        
        val root = currentRoot
        if (root == null) {
            val error = "Cannot hit back button - root accessibility node is null"
            Log.e(TAG, error)
            throw IllegalStateException(error)
        }
        
        // Find the back button
        val backButton = ButtonExtractor.findBackButton(root)
        if (backButton == null) {
            val error = "Cannot hit back button - back button not found"
            Log.e(TAG, error)
            throw IllegalStateException(error)
        }
        
        // Click the back button
        Log.d(TAG, "Found back button, attempting to click it")
        val success = backButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        if (success) {
            Log.d(TAG, "Successfully clicked back button")
            // Reset language violation count since we're leaving video context
            languageViolationCount = 0
            Log.d(TAG, "Reset language violation count after hitting back button")
        } else {
            Log.e(TAG, "Failed to click back button")
            throw RuntimeException("Failed to click back button")
        }
    }
    
    /**
     * Checks if we should skip video based on rate limiting.
     * Cleans old skip history and checks if we're under the limit.
     */
    private fun shouldSkipVideo(): Boolean {
        cleanOldSkipHistory()
        val maxSkips = getMaxSkipsInTimeWindow()
        val canSkip = skipHistory.size < maxSkips
        Log.d(TAG, "shouldSkipVideo(): ${skipHistory.size}/$maxSkips skips in window = $canSkip")
        return canSkip
    }
    
    /**
     * Updates accumulated watch time when user is actively watching.
     * Resets if gap between updates exceeds 2 hours.
     */
    private fun updateWatchTime() {
        val now = System.currentTimeMillis()
        if (!isCurrentlyWatching()) {
            if(now - lastWatchingUpdateTime > MAX_GAP_BETWEEN_UPDATES_NON_WATCHING_MS){
                accumulatedWatchTimeMs = 0;
            }
            return // Do nothing when not watching
        }
        

        
        if (lastWatchingUpdateTime > 0) {
            val timeSinceLastUpdate = now - lastWatchingUpdateTime
            
            if (timeSinceLastUpdate > MAX_GAP_BETWEEN_UPDATES_MS) {
                // Too long since last update - reset
                Log.d(TAG, "Gap too large (${timeSinceLastUpdate/1000}s), resetting watch time")
                accumulatedWatchTimeMs = 0
            } else {
                // Normal update - accumulate time
                accumulatedWatchTimeMs += timeSinceLastUpdate
                Log.d(TAG, "Accumulated +${timeSinceLastUpdate/1000}s, total: ${accumulatedWatchTimeMs/1000}s")
            }
        }
        
        lastWatchingUpdateTime = now
    }
    
    /**
     * Calculates number of game rounds based on accumulated watch time.
     * Returns a value between 1 and 5 rounds.
     */
    private fun calculateNumberOfRounds(): Int {
        val watchTimeMinutes = accumulatedWatchTimeMs / (60 * 1000L)
        val intervalMinutes = getGameOverlayIntervalMinutes()
        val rounds = (watchTimeMinutes / intervalMinutes).toInt()
        return rounds.coerceIn(1, 5)
    }
    
    /**
     * Determines if game overlay should be shown based on accumulated watch time.
     * Returns GameParams with calculated number of rounds, or null if games shouldn't be shown.
     */
    private fun shouldShowGameOverlay(): GameParams? {
        if (wasAutoSkipped) {
            Log.d(TAG, "shouldShowGameOverlay(): null - video was auto-skipped")
            return null
        }
        
        val watchTimeMinutes = accumulatedWatchTimeMs / (60 * 1000L)
        val overlayInterval = getGameOverlayIntervalMinutes()
        
        if (watchTimeMinutes < overlayInterval) {
            Log.d(TAG, "shouldShowGameOverlay(): null - insufficient watch time (${watchTimeMinutes}m < ${overlayInterval}m)")
            return null
        }
        
        val numberOfRounds = calculateNumberOfRounds()
        Log.d(TAG, "shouldShowGameOverlay(): GameParams(rounds=$numberOfRounds) - watch time: ${watchTimeMinutes}m")
        
        return GameParams(numberOfRounds)
    }
    
    /**
     * Checks if user is currently watching video.
     * Video is being watched when in FULL_SCREEN_STRICT or 
     * in FULL_SCREEN_WITH_RECOMMENDATIONS with pause button existing.
     */
    private fun isCurrentlyWatching(): Boolean {
        val root = currentRoot ?: return false
        
        return when (currentPlayerState) {
            PlayerState.FULL_SCREEN_STRICT -> true
            PlayerState.FULL_SCREEN_WITH_RECOMMENDATIONS -> {
                // Check if pause button exists (means video is playing)
                ButtonExtractor.findPauseButton(root) != null
            }
            else -> false
        }
    }
    
    /**
     * Removes old skip timestamps that are outside the time window.
     */
    private fun cleanOldSkipHistory() {
        val now = System.currentTimeMillis()
        val windowStart = now - (getSkipTimeWindowMinutes() * 60 * 1000L)
        
        val sizeBefore = skipHistory.size
        skipHistory.removeAll { it < windowStart }
        val sizeAfter = skipHistory.size
        
        if (sizeBefore != sizeAfter) {
            Log.d(TAG, "Cleaned skip history: $sizeBefore -> $sizeAfter entries")
        }
    }
    
    /**
     * Checks if a daily reset is needed and performs it.
     */
    private fun checkAndPerformDailyReset() {
        val today = dateFormat.format(Date())
        
        if (lastDailyResetDate != today) {
            Log.d(TAG, "Performing daily reset: $lastDailyResetDate -> $today")
            totalVideoCount = 0
            lastDailyResetDate = today
            Log.d(TAG, "Reset total video count to 0 for new day")
        }
    }

    /**
     * Analyzes the current accessibility tree to detect state changes and extract information.
     */
    private fun analyzeCurrentState() {
        val root = currentRoot ?: return
        
        // Update watch time tracking
        updateWatchTime()
        
        // Detect current player state
        val detectedState = detectPlayerState(root)
        
        // Check if we've seen this state recently (debouncing)
        val now = System.currentTimeMillis()
        val lastSeenTime = lastSeenStateMap[detectedState] ?: 0L
        if (now - lastSeenTime < STATE_DEBOUNCE_MS && detectedState != PlayerState.FULL_SCREEN_WITH_RECOMMENDATIONS) {
            Log.d(TAG, "Ignoring $detectedState - seen ${now - lastSeenTime}ms ago (less than ${STATE_DEBOUNCE_MS}ms)")
            return
        }
        
        // Update the map with current time for this state
        lastSeenStateMap[detectedState] = now

        // Handle state changes
        val previousState = currentPlayerState
        if (previousState != detectedState) {
            Log.d(TAG, "Player state changed: $previousState -> $detectedState")
            currentPlayerState = detectedState
            handleStateChange(previousState, detectedState)
        } else {
            Log.d(TAG, "We are in the same state: $currentPlayerState")
            // Handle same state scenarios (like new video in same state)
            handleSameState(detectedState)
        }

        // Extract title when in FULL_SCREEN_WITH_RECOMMENDATIONS state
        if (detectedState == PlayerState.FULL_SCREEN_WITH_RECOMMENDATIONS) {
            val detectedTitle = TitleExtractor.extractVideoTitleFromNode(root)
            Log.d(TAG, "THIS IS NEW!!! Current Video title: '$detectedTitle'")
            
            if (detectedTitle != null && detectedTitle != currentVideoTitle) {
                Log.d(TAG, "Video title changed: '$currentVideoTitle' -> '$detectedTitle'")
                currentVideoTitle = detectedTitle
                wasAutoSkipped = false // Reset auto-skip flag for new video
                
                // Increment total video count
                totalVideoCount++
                currentVideoStartTime = System.currentTimeMillis()
                Log.d(TAG, "Video count incremented to: $totalVideoCount")
                
                // Perform language detection on the new title
                LanguageDetectionUtil.detectAndValidateLanguage(detectedTitle) { detectedLanguage, isCompliant ->
                    currentLanguage = detectedLanguage
                    if (detectedLanguage != null) {
                        if (isCompliant) {
                            Log.d(TAG, "Language compliance: COMPLIANT - Title '$detectedTitle' is in allowed language '$detectedLanguage'")
                            
                            // Show game overlay if timing allows and not auto-skipped
                            val gameParams = shouldShowGameOverlay()
                            if (gameParams != null) {
                                Log.d(TAG, "Triggering game overlay with ${gameParams.numberOfRounds} rounds for compliant video: '$detectedTitle'")
                                pauseVideo()
                                GameOverlayService.startGameOverlay(context, gameParams)
                                lastGameOverlayTime = System.currentTimeMillis()
                                accumulatedWatchTimeMs = 0 // Reset watch time after showing games
                            } else {
                                Log.d(TAG, "Skipping game overlay - insufficient watch time or auto-skipped")
                            }
                            
                        } else {
                            Log.e(TAG, "Language compliance: NON-COMPLIANT - Title '$detectedTitle' detected as '$detectedLanguage' which is not in allowed languages")
                            
                            // Handle non-compliant language
                            if (shouldSkipVideo()) {
                                Log.d(TAG, "Auto-skipping non-compliant video")
                                skipVideo()
                            } else {
                                Log.d(TAG, "Cannot skip due to rate limiting, incrementing violation count")
                                languageViolationCount++
                                val threshold = getLanguageViolationThreshold()
                                Log.d(TAG, "Language violation count: $languageViolationCount/$threshold")
                                
                                if (languageViolationCount >= threshold) {
                                    Log.w(TAG, "Language violation threshold reached, hitting back button")
                                    hitBackButton()
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "Language compliance: ERROR - Could not detect language for title '$detectedTitle'")
                        // Treat unknown language as non-compliant
                        languageViolationCount++
                        val threshold = getLanguageViolationThreshold()
                        Log.d(TAG, "Language violation count (unknown language): $languageViolationCount/$threshold")
                        
                        if (languageViolationCount >= threshold) {
                            Log.w(TAG, "Language violation threshold reached (unknown language), hitting back button")
                            hitBackButton()
                        }
                    }
                }
            }
        }
    }

    /**
     * Detects the current player state from the accessibility tree.
     */
    private fun detectPlayerState(root: AccessibilityNodeInfo): PlayerState {
        val matchResults = matchingEngine.walkTreeAndMatch(root, listOf(
            MiniPlayerMatcher, 
            FullScreenVideoMatcher, 
            StrictFullScreenVideoMatcher
        ))
        
        val miniPlayerFound = matchResults[0] != null
        val fullScreenWithRecommendationsFound = matchResults[1] != null
        val fullScreenStrictFound = matchResults[2] != null

        return when {
            fullScreenStrictFound -> PlayerState.FULL_SCREEN_STRICT
            fullScreenWithRecommendationsFound -> PlayerState.FULL_SCREEN_WITH_RECOMMENDATIONS
            miniPlayerFound -> PlayerState.MINI_PLAYER
            else -> PlayerState.NONE
        }
    }

    /**
     * Handles state changes between different player states.
     */
    private fun handleStateChange(previousState: PlayerState, newState: PlayerState) {
        // Check for daily reset
        checkAndPerformDailyReset()
        
        // Reset language violations when leaving video states
        val wasInVideo = (previousState == PlayerState.FULL_SCREEN_WITH_RECOMMENDATIONS || 
                         previousState == PlayerState.FULL_SCREEN_STRICT)
        val nowInVideo = (newState == PlayerState.FULL_SCREEN_WITH_RECOMMENDATIONS || 
                         newState == PlayerState.FULL_SCREEN_STRICT)
        
        if (wasInVideo && !nowInVideo) {
            Log.d(TAG, "Left video context, resetting language violation count from $languageViolationCount to 0")
            languageViolationCount = 0
        }
        
        // Cancel any pending delayed touch when leaving FULL_SCREEN_STRICT state
        if (previousState == PlayerState.FULL_SCREEN_STRICT && newState != PlayerState.FULL_SCREEN_STRICT) {
            delayedTouchRunnable?.let { 
                handler.removeCallbacks(it)
                Log.d(TAG, "Cancelled pending delayed center screen touch")
            }
        }
        
        // Note: clickMiddle() is only called in handleSameState() when we detect
        // we're still in FULL_SCREEN_STRICT (indicating a new video auto-played)
    }

    /**
     * Handles scenarios where the state remains the same but content might have changed.
     */
    private fun handleSameState(state: PlayerState) {
        if (state == PlayerState.FULL_SCREEN_STRICT) {
            Log.d(TAG, "Still in FULL_SCREEN_STRICT probably new video")
            // Trigger auto-click for potentially new video
            clickMiddle()
        }
    }

    /**
     * Performs the actual center screen touch gesture.
     */
    private fun performCenterScreenTouch() {
        try {
            // Get screen dimensions
            val display = (context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager)
                .getDisplay(Display.DEFAULT_DISPLAY)
            val size = Point()
            display.getSize(size)
            
            // Calculate center coordinates
            val centerX = size.x / 2f
            val centerY = size.y / 2f
            
            Log.d(TAG, "Attempting to tap at center: ($centerX, $centerY) on ${size.x}x${size.y} screen")
            
            // Create tap gesture path
            val path = Path().apply {
                moveTo(centerX, centerY)
            }
            
            // Build gesture description
            val gestureBuilder = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 1)) // 1ms tap duration
            
            // Dispatch the gesture using the provided dispatcher
            gestureDispatcher(gestureBuilder.build(), object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    Log.d(TAG, "Center screen touch completed successfully")
                }
                
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    Log.w(TAG, "Center screen touch was cancelled")
                }
            })
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to perform center screen touch", e)
        }
    }

    /**
     * Performs the actual delayed skip operation.
     * This is called after the SKIP_DELAY_MS timeout to ensure video has loaded.
     */
    private fun performDelayedSkip() {
        try {
            Log.d(TAG, "Performing delayed skip operation")
            nextVideo()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to perform delayed skip", e)
        }
    }


    /**
     * Cleanup method to cancel any pending operations.
     */
    fun cleanup() {
        delayedTouchRunnable?.let { handler.removeCallbacks(it) }
        delayedSkipRunnable?.let { handler.removeCallbacks(it) }
        skipHistory.clear()
        Log.d(TAG, "GuardManager cleaned up - cleared skip history and cancelled pending operations")
    }
}
