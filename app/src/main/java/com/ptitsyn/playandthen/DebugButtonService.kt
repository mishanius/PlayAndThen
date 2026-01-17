package com.ptitsyn.playandthen

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

/**
 * Floating debug button service for on-demand game testing.
 * Only active when BuildConfig.SHOW_DEBUG_BUTTON is true (debug builds).
 */
class DebugButtonService : Service() {

    companion object {
        private const val TAG = "DebugButtonService"
        
        fun start(context: Context) {
            if (!BuildConfig.SHOW_DEBUG_BUTTON) {
                Log.d(TAG, "Debug button disabled in this build")
                return
            }
            
            if (!Settings.canDrawOverlays(context)) {
                Log.w(TAG, "Overlay permission not granted")
                return
            }
            
            context.startService(Intent(context, DebugButtonService::class.java))
        }
        
        fun stop(context: Context) {
            context.stopService(Intent(context, DebugButtonService::class.java))
        }
    }

    private var windowManager: WindowManager? = null
    private var floatingButton: View? = null
    private var gamePickerView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "DebugButtonService created")
        
        if (!BuildConfig.SHOW_DEBUG_BUTTON) {
            stopSelf()
            return
        }
        
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createFloatingButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFloatingButton()
        removeGamePicker()
        Log.d(TAG, "DebugButtonService destroyed")
    }

    private fun createFloatingButton() {
        val button = Button(this).apply {
            text = "🎮"
            textSize = 20f
            alpha = 0.7f
            setBackgroundColor(0xFF333333.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(24, 16, 24, 16)
            
            setOnClickListener {
                showGamePicker()
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 16
            y = 100
        }

        floatingButton = button
        windowManager?.addView(button, params)
        Log.d(TAG, "Floating button added")
    }

    private fun removeFloatingButton() {
        floatingButton?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing floating button", e)
            }
        }
        floatingButton = null
    }

    private fun showGamePicker() {
        removeGamePicker()
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xF0222222.toInt())
            setPadding(32, 32, 32, 32)
        }
        
        val title = TextView(this).apply {
            text = "Select Game"
            textSize = 18f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 0, 0, 24)
        }
        layout.addView(title)
        
        val games = listOf(
            "Numbers (Kotlin)" to "numbers_kt",
            "Alphabet (Kotlin)" to "alphabet_kt",
            "Balloons (Kotlin)" to "balloons_kt",
            "Numbers (TS)" to "numbers_ts"
        )
        
        games.forEach { (label, type) ->
            val btn = Button(this).apply {
                text = label
                setOnClickListener {
                    removeGamePicker()
                    launchGame(type)
                }
            }
            layout.addView(btn)
        }
        
        val cancelBtn = Button(this).apply {
            text = "Cancel"
            setOnClickListener {
                removeGamePicker()
            }
        }
        layout.addView(cancelBtn)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        gamePickerView = layout
        windowManager?.addView(layout, params)
        Log.d(TAG, "Game picker shown")
    }

    private fun removeGamePicker() {
        gamePickerView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing game picker", e)
            }
        }
        gamePickerView = null
    }

    private fun launchGame(gameType: String) {
        Log.d(TAG, "Launching game: $gameType")
        
        val intent = Intent(this, GameOverlayService::class.java).apply {
            putExtra(GameOverlayService.EXTRA_GAME_TYPE, gameType)
            putExtra(GameOverlayService.EXTRA_FORCE_SINGLE_ROUND, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
