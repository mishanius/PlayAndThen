package com.ptitsyn.playandthen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val targetActivity = if (ConsentActivity.hasConsent(this)) {
            MainActivity::class.java
        } else {
            ConsentActivity::class.java
        }
        
        startActivity(Intent(this, targetActivity))
        finish()
    }
}
