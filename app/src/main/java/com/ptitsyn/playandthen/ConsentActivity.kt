package com.ptitsyn.playandthen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ConsentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consent)

        val checkbox = findViewById<CheckBox>(R.id.consentCheckbox)
        val acceptButton = findViewById<Button>(R.id.acceptButton)
        val declineButton = findViewById<Button>(R.id.declineButton)

        checkbox.setOnCheckedChangeListener { _, isChecked ->
            acceptButton.isEnabled = isChecked
        }

        acceptButton.setOnClickListener {
            setConsent(this, true)
            Toast.makeText(this, "✅ PlayAndThen is now active!", Toast.LENGTH_SHORT).show()
            finish()
        }

        declineButton.setOnClickListener {
            Toast.makeText(this, "Consent is required. Accessibility service will be disabled.", Toast.LENGTH_LONG).show()
            setConsent(this, false)
            finish()
        }
    }
    
    companion object {
        private const val PREFS_NAME = "playandthen_consent"
        private const val KEY_CONSENT_GIVEN = "accessibility_consent_given"
        const val KEY_CONSENT_DECLINED = "accessibility_consent_declined"

        fun hasConsent(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_CONSENT_GIVEN, false)
        }
        
        fun wasDeclined(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_CONSENT_DECLINED, false)
        }

        fun setConsent(context: Context, granted: Boolean) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_CONSENT_GIVEN, granted)
                .putBoolean(KEY_CONSENT_DECLINED, !granted)
                .apply()
        }
    }
}