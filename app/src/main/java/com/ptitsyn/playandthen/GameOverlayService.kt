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
        
        fun startGameOverlay(context: Context) {
            val intent = Intent(context, GameOverlayService::class.java).apply {
                action = ACTION_SHOW_GAME
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

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "GameOverlayService created")
        
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_GAME -> showGameOverlay()
            ACTION_HIDE_GAME -> hideGameOverlay()
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

        // Randomly select and create one of the three game modes
        val selectedGame = when ((0..2).random()) {
            0 -> {
                Log.d(TAG, "Selected NumbersGame")
                NumbersGame(this)
            }
            1 -> {
                Log.d(TAG, "Selected BloonsGame")
                BloonsGame(this)
            }
            else -> {
                Log.d(TAG, "Selected AlphabetGame")
                AlphabetGame(this)
            }
        }
        
        // Configure game completion callback
        gameView = selectedGame.apply {
            onGameCompleted = {
                Log.d(TAG, "Game completed! Hiding overlay...")
                hideGameOverlay()
            }
        }

        // Add game view to container
        container.addView(gameView)

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
