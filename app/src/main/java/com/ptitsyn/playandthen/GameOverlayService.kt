package com.ptitsyn.playandthen

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.NotificationCompat

class GameOverlayService : Service() {

    companion object {
        private const val TAG = "GameOverlayService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "game_overlay_channel"
        
        const val ACTION_SHOW_GAME = "com.ptitsyn.playandthen.SHOW_GAME"
        const val ACTION_HIDE_GAME = "com.ptitsyn.playandthen.HIDE_GAME"
        
        const val EXTRA_NUMBER_OF_ROUNDS = "extra_number_of_rounds"
        const val EXTRA_GAME_TYPE = "extra_game_type"
        const val EXTRA_FORCE_SINGLE_ROUND = "extra_force_single_round"
        
        // Game type constants
        const val GAME_TYPE_NUMBERS_KT = "numbers_kt"
        const val GAME_TYPE_ALPHABET_KT = "alphabet_kt"
        const val GAME_TYPE_BALLOONS_KT = "balloons_kt"
        const val GAME_TYPE_NUMBERS_TS = "numbers_ts"
        const val GAME_TYPE_MATCH_WORDS_TS = "match_words_ts"
        const val GAME_TYPE_OPPOSITES_TS = "opposites_ts"
        const val GAME_TYPE_ALPHABET_TS = "alphabet_ts"
        const val GAME_TYPE_LOGIC_ADD_TS = "logic_add_ts"
        
        fun startGameOverlay(context: Context, gameParams: GameParams) {
            val intent = Intent(context, GameOverlayService::class.java).apply {
                action = ACTION_SHOW_GAME
                putExtra(EXTRA_NUMBER_OF_ROUNDS, gameParams.numberOfRounds)
            }
            context.startForegroundService(intent)
        }
        
        fun hideGameOverlay(context: Context) {
            val intent = Intent(context, GameOverlayService::class.java).apply {
                action = ACTION_HIDE_GAME
            }
            context.startService(intent)
        }
    }

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var gameView: GridGame? = null
    private var isOverlayShowing = false
    
    // Round management
    private var numberOfRounds: Int = 1
    private var currentRound: Int = 0
    
    // Forced game type (from debug button)
    private var forcedGameType: String? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "GameOverlayService created")
        
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_GAME -> {
                numberOfRounds = intent.getIntExtra(EXTRA_NUMBER_OF_ROUNDS, 1)
                currentRound = 0
                forcedGameType = null
                Log.d(TAG, "Starting game overlay with $numberOfRounds rounds")
                showGameOverlay()
            }
            ACTION_HIDE_GAME -> hideGameOverlay()
            else -> {
                // Handle debug button launch (no action, just extras)
                val gameType = intent?.getStringExtra(EXTRA_GAME_TYPE)
                val forceSingleRound = intent?.getBooleanExtra(EXTRA_FORCE_SINGLE_ROUND, false) ?: false
                
                if (gameType != null) {
                    forcedGameType = gameType
                    numberOfRounds = if (forceSingleRound) 1 else intent.getIntExtra(EXTRA_NUMBER_OF_ROUNDS, 1)
                    currentRound = 0
                    Log.d(TAG, "Starting forced game: $gameType with $numberOfRounds rounds")
                    showGameOverlay()
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Game Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls the counting game overlay"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("PlayAndThen Game Active")
        .setContentText("Complete the counting game to continue watching")
        .setSmallIcon(R.drawable.balloon)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .build()

    private fun showGameOverlay() {
        // Check if we have overlay permission
        if (!Settings.canDrawOverlays(this)) {
            Log.e(TAG, "No overlay permission granted")
            Toast.makeText(this, "Overlay permission required", Toast.LENGTH_LONG).show()
            return
        }

        if (isOverlayShowing) {
            Log.d(TAG, "Overlay already showing")
            return
        }

        try {
            // Create the overlay view
            overlayView = createOverlayView()
            
            // Add the view to window manager
            windowManager?.addView(overlayView, createLayoutParams())
            isOverlayShowing = true
            
            Log.d(TAG, "Game overlay shown successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show game overlay", e)
        }
    }

    private fun createOverlayView(): View {
        // Create a fullscreen container
        val container = object : ViewGroup(this) {
            init {
                setBackgroundColor(0xE0000000.toInt()) // Semi-transparent dark background
                isFocusableInTouchMode = true
                requestFocus()
            }

            override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
                getChildAt(0)?.layout(0, 0, r - l, b - t)
            }

            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)

                // Measure child
                val child = getChildAt(0)
                child?.measure(
                    MeasureSpec.makeMeasureSpec(measuredWidth,  MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(measuredHeight,  MeasureSpec.EXACTLY)
                )
            }

            override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
                // Block back button and other navigation keys
                return when (keyCode) {
                    KeyEvent.KEYCODE_BACK,
                    KeyEvent.KEYCODE_HOME,
                    KeyEvent.KEYCODE_MENU,
                    KeyEvent.KEYCODE_APP_SWITCH -> {
                        Log.d(TAG, "Blocked key: $keyCode")
                        true // Consume the event
                    }
                    else -> super.onKeyDown(keyCode, event)
                }
            }

            override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
                // Block all key events to prevent leaving the game
                return if (event?.action == KeyEvent.ACTION_DOWN) {
                    onKeyDown(event.keyCode, event)
                } else {
                    true
                }
            }
        }

        // Select game with variety logic - avoid recently played games
        val gameIndex = selectNextGame()
        currentRound++
        
        val selectedGame: View = when (gameIndex) {
            0 -> {
                Log.d(TAG, "Selected NumbersGame (round $currentRound/$numberOfRounds)")
                NumbersGame(this).also { game ->
                    gameView = game
                    game.onGameCompleted = {
                        Log.d(TAG, "Round $currentRound/$numberOfRounds completed")
                        onRoundCompleted()
                    }
                }
            }
            1 -> {
                Log.d(TAG, "Selected BloonsGame (round $currentRound/$numberOfRounds)")
                BloonsGame(this).also { game ->
                    gameView = game
                    game.onGameCompleted = {
                        Log.d(TAG, "Round $currentRound/$numberOfRounds completed")
                        onRoundCompleted()
                    }
                }
            }
            2 -> {
                Log.d(TAG, "Selected AlphabetGame (round $currentRound/$numberOfRounds)")
                AlphabetGame(this).also { game ->
                    gameView = game
                    game.onGameCompleted = {
                        Log.d(TAG, "Round $currentRound/$numberOfRounds completed")
                        onRoundCompleted()
                    }
                }
            }
            3 -> {
                Log.d(TAG, "Selected NumbersGameTS (JS/TS) (round $currentRound/$numberOfRounds)")
                GridGameJs(
                    context = this,
                    currentRound = currentRound,
                    totalRounds = numberOfRounds,
                    gameType = "numbers"
                ).also { game ->
                    game.onGameCompleted = {
                        Log.d(TAG, "Round $currentRound/$numberOfRounds completed")
                        onRoundCompleted()
                    }
                }
            }
            4 -> {
                Log.d(TAG, "Selected MatchWordsGame (JS/TS) (round $currentRound/$numberOfRounds)")
                GridGameJs(
                    context = this,
                    currentRound = currentRound,
                    totalRounds = numberOfRounds,
                    gameType = "match-words"
                ).also { game ->
                    game.onGameCompleted = {
                        Log.d(TAG, "Round $currentRound/$numberOfRounds completed")
                        onRoundCompleted()
                    }
                }
            }
            5 -> {
                Log.d(TAG, "Selected OppositesGame (JS/TS) (round $currentRound/$numberOfRounds)")
                GridGameJs(
                    context = this,
                    currentRound = currentRound,
                    totalRounds = numberOfRounds,
                    gameType = "opposites"
                ).also { game ->
                    game.onGameCompleted = {
                        Log.d(TAG, "Round $currentRound/$numberOfRounds completed")
                        onRoundCompleted()
                    }
                }
            }
            6 -> {
                Log.d(TAG, "Selected AlphabetGame (JS/TS) (round $currentRound/$numberOfRounds)")
                GridGameJs(
                    context = this,
                    currentRound = currentRound,
                    totalRounds = numberOfRounds,
                    gameType = "alphabet"
                ).also { game ->
                    game.onGameCompleted = {
                        Log.d(TAG, "Round $currentRound/$numberOfRounds completed")
                        onRoundCompleted()
                    }
                }
            }
            7 -> {
                Log.d(TAG, "Selected LogicAddGame (JS/TS) (round $currentRound/$numberOfRounds)")
                GridGameJs(
                    context = this,
                    currentRound = currentRound,
                    totalRounds = numberOfRounds,
                    gameType = "logic-add"
                ).also { game ->
                    game.onGameCompleted = {
                        Log.d(TAG, "Round $currentRound/$numberOfRounds completed")
                        onRoundCompleted()
                    }
                }
            }
            else -> {
                Log.d(TAG, "Selected NumbersGame (Kotlin fallback) (round $currentRound/$numberOfRounds)")
                NumbersGame(this).also { game ->
                    gameView = game
                    game.onGameCompleted = {
                        Log.d(TAG, "Round $currentRound/$numberOfRounds completed")
                        onRoundCompleted()
                    }
                }
            }
        }

        // Add game view to container
        container.addView(selectedGame)

        return container
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }
    }

    /**
     * Selects a random game from enabled games or returns forced game type.
     * 3 = NumbersGameTS (JavaScript/TypeScript)
     * 4 = MatchWordsGame (JavaScript/TypeScript)
     * 6 = AlphabetGame (JavaScript/TypeScript)
     */
    private fun selectNextGame(): Int {
        // If forced game type is set, use it
        forcedGameType?.let { type ->
            return when (type) {
                GAME_TYPE_NUMBERS_KT -> 0
                GAME_TYPE_BALLOONS_KT -> 1
                GAME_TYPE_ALPHABET_KT -> 2
                GAME_TYPE_NUMBERS_TS -> 3
                GAME_TYPE_MATCH_WORDS_TS -> 4
                GAME_TYPE_OPPOSITES_TS -> 5
                GAME_TYPE_ALPHABET_TS -> 6
                GAME_TYPE_LOGIC_ADD_TS -> 7
                else -> 3
            }
        }
        
        // Get enabled games from settings
        val prefs = SettingsActivity.getPreferences(this)
        val enabledGames = mutableListOf<Int>()
        
        if (prefs.getBoolean(SettingsActivity.KEY_GAME_NUMBERS_ENABLED, true)) {
            enabledGames.add(3) // Numbers TS
        }
        if (prefs.getBoolean(SettingsActivity.KEY_GAME_ALPHABET_ENABLED, true)) {
            enabledGames.add(6) // Alphabet TS
        }
        if (prefs.getBoolean(SettingsActivity.KEY_GAME_MATCH_WORDS_ENABLED, true)) {
            enabledGames.add(4) // Match Words TS
        }
        if (prefs.getBoolean(SettingsActivity.KEY_GAME_BALLOONS_ENABLED, true)) {
            enabledGames.add(1) // Balloons Kotlin
        }
        if (prefs.getBoolean(SettingsActivity.KEY_GAME_OPPOSITES_ENABLED, true)) {
            enabledGames.add(5) // Opposites TS
        }
        if (prefs.getBoolean(SettingsActivity.KEY_GAME_LOGIC_ADD_ENABLED, true)) {
            enabledGames.add(7) // Logic Add TS
        }
        
        return enabledGames.random()
    }
    
    /**
     * Called when a round is completed.
     * Either shows next round or hides overlay if all rounds are done.
     */
    private fun onRoundCompleted() {
        if (currentRound < numberOfRounds) {
            // More rounds to play - transition to next game
            Log.d(TAG, "Transitioning to next round ($currentRound/$numberOfRounds)")
            transitionToNextRound()
        } else {
            // All rounds complete - hide overlay
            Log.d(TAG, "All rounds completed! Hiding overlay...")
            hideGameOverlay()
        }
    }
    
    /**
     * Transitions from current game to the next round.
     */
    private fun transitionToNextRound() {
        try {
            // Remove current game
            if (overlayView != null && gameView != null) {
                (overlayView as? ViewGroup)?.removeView(gameView)
                gameView = null
            }
            
            // Create and add new game
            val newGameView = createOverlayView()
            if (overlayView != null) {
                windowManager?.removeView(overlayView)
            }
            
            overlayView = newGameView
            windowManager?.addView(overlayView, createLayoutParams())
            
            Log.d(TAG, "Transitioned to next round successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to transition to next round", e)
            hideGameOverlay()
        }
    }
    
    private fun hideGameOverlay() {
        try {
            if (isOverlayShowing && overlayView != null) {
                windowManager?.removeView(overlayView)
                overlayView = null
                gameView = null
                isOverlayShowing = false
                
                Log.d(TAG, "Game overlay hidden successfully")
            }
            
            // Stop the service after hiding overlay
            stopSelf()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to hide game overlay", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideGameOverlay()
        Log.d(TAG, "GameOverlayService destroyed")
    }
}
