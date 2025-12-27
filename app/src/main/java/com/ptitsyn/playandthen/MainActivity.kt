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
        checkOverlayPermission()
    }
    
    private fun setupClickListeners() {
        findViewById<Button>(R.id.requestPermissionButton)?.setOnClickListener {
            requestOverlayPermission()
        }
        
        findViewById<Button>(R.id.testOverlayButton)?.setOnClickListener {
            testOverlay()
        }
    }
    
    private fun testOverlay() {
        if (Settings.canDrawOverlays(this)) {
            GameOverlayService.startGameOverlay(this)
            Toast.makeText(this, "🎮 Game overlay started! Complete the counting game to dismiss it.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Overlay permission required to test the game", Toast.LENGTH_SHORT).show()
            requestOverlayPermission()
        }
    }
    
    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            // Show explanation dialog
            AlertDialog.Builder(this)
                .setTitle("Overlay Permission Required")
                .setMessage("PlayAndThen needs overlay permission to show the counting game on top of YouTube Kids. This ensures your child completes the educational game before watching videos.")
                .setPositiveButton("Grant Permission") { _, _ ->
                    requestOverlayPermission()
                }
                .setNegativeButton("Cancel") { _, _ ->
                    Toast.makeText(this, "Overlay permission is required for PlayAndThen to function properly", Toast.LENGTH_LONG).show()
                }
                .setCancelable(false)
                .show()
        } else {
            // Permission already granted, show success message
            showPermissionGrantedMessage()
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
            if (Settings.canDrawOverlays(this)) {
                showPermissionGrantedMessage()
            } else {
                Toast.makeText(this, "Overlay permission is required for PlayAndThen to work", Toast.LENGTH_LONG).show()
                // Ask again after a delay
                findViewById<Button>(R.id.requestPermissionButton)?.visibility = android.view.View.VISIBLE
            }
        }
    }
    
    private fun showPermissionGrantedMessage() {
        Toast.makeText(this, "✅ PlayAndThen is ready! The counting game will appear when new YouTube Kids videos start.", Toast.LENGTH_LONG).show()
        
        // Hide permission button if visible
        findViewById<Button>(R.id.requestPermissionButton)?.visibility = android.view.View.GONE
        
        // Show test button
        findViewById<Button>(R.id.testOverlayButton)?.visibility = android.view.View.VISIBLE
    }
}
