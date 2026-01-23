package com.ptitsyn.playandthen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity: AppCompatActivity() {
    
    companion object {
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupClickListeners()
        checkPermissionsAndConsent()
    }
    
    override fun onResume() {
        super.onResume()
        updateUI()
    }
    
    private fun setupClickListeners() {
        findViewById<Button>(R.id.requestPermissionButton)?.setOnClickListener {
            requestOverlayPermission()
        }
        
        findViewById<Button>(R.id.testOverlayButton)?.setOnClickListener {
            testOverlay()
        }
        
        findViewById<Button>(R.id.testMatchWordsButton)?.setOnClickListener {
            testMatchWordsGame()
        }
    }
    
    private fun checkPermissionsAndConsent() {
        // First check consent
        if (!ConsentActivity.hasConsent(this)) {
            startActivity(Intent(this, ConsentActivity::class.java))
            return
        }
        
        // Then check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle("Overlay Permission Required")
                .setMessage("PlayAndThen needs overlay permission to show the counting game on top of YouTube Kids.")
                .setPositiveButton("Grant Permission") { _, _ ->
                    requestOverlayPermission()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        
        updateUI()
    }
    
    private fun updateUI() {
        val hasConsent = ConsentActivity.hasConsent(this)
        val hasOverlay = Settings.canDrawOverlays(this)
        
        findViewById<Button>(R.id.requestPermissionButton)?.visibility = 
            if (!hasOverlay) android.view.View.VISIBLE else android.view.View.GONE
        
        findViewById<Button>(R.id.testOverlayButton)?.visibility = 
            if (hasConsent && hasOverlay) android.view.View.VISIBLE else android.view.View.GONE
        
        findViewById<Button>(R.id.testMatchWordsButton)?.visibility = 
            if (hasConsent && hasOverlay) android.view.View.VISIBLE else android.view.View.GONE
        
        // Start debug button service (debug builds only)
        if (hasConsent && hasOverlay && BuildConfig.SHOW_DEBUG_BUTTON) {
            DebugButtonService.start(this)
        }
    }
    
    private fun testOverlay() {
        if (Settings.canDrawOverlays(this)) {
            GameOverlayService.startGameOverlay(this, GameParams(numberOfRounds = 1))
            Toast.makeText(this, "🎮 Game overlay started!", Toast.LENGTH_LONG).show()
        } else {
            requestOverlayPermission()
        }
    }
    
    private fun testMatchWordsGame() {
        if (Settings.canDrawOverlays(this)) {
            val gameView = GridGameJs(
                context = this,
                currentRound = 1,
                totalRounds = 1,
                gameType = "match-words"
            )
            
            gameView.onGameCompleted = {
                Toast.makeText(this, "🎉 Match Words game completed!", Toast.LENGTH_SHORT).show()
                (gameView.parent as? android.view.ViewGroup)?.removeView(gameView)
            }
            
            val rootView = findViewById<android.view.ViewGroup>(android.R.id.content)
            rootView.addView(gameView, android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            ))
            
            Toast.makeText(this, "🎯 Match Words game started!", Toast.LENGTH_LONG).show()
        } else {
            requestOverlayPermission()
        }
    }
    
    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            updateUI()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (BuildConfig.SHOW_DEBUG_BUTTON) {
            DebugButtonService.stop(this)
        }
    }
}
