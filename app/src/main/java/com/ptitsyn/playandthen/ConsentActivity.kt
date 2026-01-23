package com.ptitsyn.playandthen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ConsentActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "playandthen_consent"
        private const val KEY_CONSENT_GIVEN = "accessibility_consent_given"

        fun hasConsent(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_CONSENT_GIVEN, false)
        }

        fun setConsent(context: Context, granted: Boolean) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_CONSENT_GIVEN, granted)
                .apply()
        }
    }

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
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        declineButton.setOnClickListener {
            Toast.makeText(this, "Consent is required to use PlayAndThen", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
