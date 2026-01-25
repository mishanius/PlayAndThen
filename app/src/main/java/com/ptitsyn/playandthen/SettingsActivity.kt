package com.ptitsyn.playandthen

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Settings activity for configuring PlayAndThen accessibility service parameters.
 * Accessible through Android Settings > Accessibility > PlayAndThen > Settings
 */
class SettingsActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "SettingsActivity"
        const val PREFS_NAME = "playandthen_settings"
        
        // Setting keys
        const val KEY_MAX_SKIPS = "max_skips_per_window"
        const val KEY_TIME_WINDOW_MINUTES = "skip_time_window_minutes"
        const val KEY_LANGUAGE_THRESHOLD = "language_violation_threshold"
        const val KEY_GAME_OVERLAY_INTERVAL = "game_overlay_interval_minutes"
        const val KEY_GAME_NUMBERS_ENABLED = "game_numbers_enabled"
        const val KEY_GAME_ALPHABET_ENABLED = "game_alphabet_enabled"
        const val KEY_GAME_MATCH_WORDS_ENABLED = "game_match_words_enabled"
        const val KEY_GAME_BALLOONS_ENABLED = "game_balloons_enabled"
        const val KEY_GAME_OPPOSITES_ENABLED = "game_opposites_enabled"
        const val KEY_GAME_LOGIC_ADD_ENABLED = "game_logic_add_enabled"
        const val KEY_NUMBER_OF_ROUNDS = "number_of_rounds"
        const val KEY_LANGUAGE_FILTER_ENABLED = "language_filter_enabled"
        const val KEY_ALLOWED_LANGUAGE = "allowed_language"
        const val KEY_ALLOWED_LANGUAGES = "allowed_languages"
        
        // Default values
        const val DEFAULT_MAX_SKIPS = 5
        const val DEFAULT_TIME_WINDOW_MINUTES = 10L
        const val DEFAULT_LANGUAGE_THRESHOLD = 3
        const val DEFAULT_GAME_OVERLAY_INTERVAL = 15L
        
        /**
         * Utility method to get SharedPreferences instance
         */
        fun getPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
    
    private lateinit var prefs: SharedPreferences
    
    // UI Elements
    private lateinit var maxSkipsSeekBar: SeekBar
    private lateinit var maxSkipsValue: TextView
    private lateinit var timeWindowInput: EditText
    private lateinit var languageThresholdSeekBar: SeekBar
    private lateinit var languageThresholdValue: TextView
    private lateinit var gameOverlayInput: EditText
    private lateinit var gameNumbersCheckbox: CheckBox
    private lateinit var gameAlphabetCheckbox: CheckBox
    private lateinit var gameMatchWordsCheckbox: CheckBox
    private lateinit var gameBalloonsCheckbox: CheckBox
    private lateinit var gameOppositesCheckbox: CheckBox
    private lateinit var gameLogicAddCheckbox: CheckBox
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        prefs = getPreferences(this)
        
        initializeViews()
        setupListeners()
        loadCurrentSettings()
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "PlayAndThen Settings"
    }
    
    private fun initializeViews() {
        maxSkipsSeekBar = findViewById(R.id.maxSkipsSeekBar)
        maxSkipsValue = findViewById(R.id.maxSkipsValue)
        timeWindowInput = findViewById(R.id.timeWindowInput)
        languageThresholdSeekBar = findViewById(R.id.languageThresholdSeekBar)
        languageThresholdValue = findViewById(R.id.languageThresholdValue)
        gameOverlayInput = findViewById(R.id.gameOverlayInput)
        gameNumbersCheckbox = findViewById(R.id.gameNumbersCheckbox)
        gameAlphabetCheckbox = findViewById(R.id.gameAlphabetCheckbox)
        gameMatchWordsCheckbox = findViewById(R.id.gameMatchWordsCheckbox)
        gameBalloonsCheckbox = findViewById(R.id.gameBalloonsCheckbox)
        gameOppositesCheckbox = findViewById(R.id.gameOppositesCheckbox)
        gameLogicAddCheckbox = findViewById(R.id.gameLogicAddCheckbox)
    }
    
    private fun setupListeners() {
        // Max skips per time window (1-20)
        maxSkipsSeekBar.max = 19 // 0-19, represents 1-20
        maxSkipsSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress + 1 // Convert 0-19 to 1-20
                maxSkipsValue.text = value.toString()
                if (fromUser) {
                    prefs.edit().putInt(KEY_MAX_SKIPS, value).apply()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Time window input (1-60 minutes)
        timeWindowInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                if (text.isNotEmpty()) {
                    try {
                        val value = text.toLong().coerceIn(1, 60)
                        prefs.edit().putLong(KEY_TIME_WINDOW_MINUTES, value).apply()
                    } catch (e: NumberFormatException) {
                        // Invalid input, ignore
                    }
                }
            }
        })
        
        // Language violation threshold (1-10)
        languageThresholdSeekBar.max = 9 // 0-9, represents 1-10
        languageThresholdSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress + 1 // Convert 0-9 to 1-10
                languageThresholdValue.text = value.toString()
                if (fromUser) {
                    prefs.edit().putInt(KEY_LANGUAGE_THRESHOLD, value).apply()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Game overlay interval input (1-120 minutes)
        gameOverlayInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                if (text.isNotEmpty()) {
                    try {
                        val value = text.toLong().coerceIn(1, 120)
                        prefs.edit().putLong(KEY_GAME_OVERLAY_INTERVAL, value).apply()
                    } catch (e: NumberFormatException) {
                        // Invalid input, ignore
                    }
                }
            }
        })
        
        // Game checkboxes - enforce at least one enabled
        val gameCheckboxListener = { checkbox: CheckBox, key: String, isChecked: Boolean ->
            val otherGamesEnabled = listOf(
                gameNumbersCheckbox, gameAlphabetCheckbox, gameMatchWordsCheckbox, gameBalloonsCheckbox,
                gameOppositesCheckbox, gameLogicAddCheckbox
            ).filter { it != checkbox }.any { it.isChecked }
            
            if (!isChecked && !otherGamesEnabled) {
                checkbox.isChecked = true // Prevent unchecking last game
            } else {
                prefs.edit().putBoolean(key, isChecked).apply()
            }
        }
        
        gameNumbersCheckbox.setOnCheckedChangeListener { _, isChecked ->
            gameCheckboxListener(gameNumbersCheckbox, KEY_GAME_NUMBERS_ENABLED, isChecked)
        }
        gameAlphabetCheckbox.setOnCheckedChangeListener { _, isChecked ->
            gameCheckboxListener(gameAlphabetCheckbox, KEY_GAME_ALPHABET_ENABLED, isChecked)
        }
        gameMatchWordsCheckbox.setOnCheckedChangeListener { _, isChecked ->
            gameCheckboxListener(gameMatchWordsCheckbox, KEY_GAME_MATCH_WORDS_ENABLED, isChecked)
        }
        gameBalloonsCheckbox.setOnCheckedChangeListener { _, isChecked ->
            gameCheckboxListener(gameBalloonsCheckbox, KEY_GAME_BALLOONS_ENABLED, isChecked)
        }
        gameOppositesCheckbox.setOnCheckedChangeListener { _, isChecked ->
            gameCheckboxListener(gameOppositesCheckbox, KEY_GAME_OPPOSITES_ENABLED, isChecked)
        }
        gameLogicAddCheckbox.setOnCheckedChangeListener { _, isChecked ->
            gameCheckboxListener(gameLogicAddCheckbox, KEY_GAME_LOGIC_ADD_ENABLED, isChecked)
        }
    }
    
    private fun loadCurrentSettings() {
        // Load max skips (1-20, default 5)
        val maxSkips = prefs.getInt(KEY_MAX_SKIPS, DEFAULT_MAX_SKIPS)
        maxSkipsSeekBar.progress = maxSkips - 1 // Convert 1-20 to 0-19
        maxSkipsValue.text = maxSkips.toString()
        
        // Load time window (1-60 minutes, default 10)
        val timeWindow = prefs.getLong(KEY_TIME_WINDOW_MINUTES, DEFAULT_TIME_WINDOW_MINUTES)
        timeWindowInput.setText(timeWindow.toString())
        
        // Load language threshold (1-10, default 3)
        val languageThreshold = prefs.getInt(KEY_LANGUAGE_THRESHOLD, DEFAULT_LANGUAGE_THRESHOLD)
        languageThresholdSeekBar.progress = languageThreshold - 1 // Convert 1-10 to 0-9
        languageThresholdValue.text = languageThreshold.toString()
        
        // Load game overlay interval (1-120 minutes, default 15)
        val gameOverlayInterval = prefs.getLong(KEY_GAME_OVERLAY_INTERVAL, DEFAULT_GAME_OVERLAY_INTERVAL)
        gameOverlayInput.setText(gameOverlayInterval.toString())
        
        // Load game enabled states (all enabled by default)
        gameNumbersCheckbox.isChecked = prefs.getBoolean(KEY_GAME_NUMBERS_ENABLED, true)
        gameAlphabetCheckbox.isChecked = prefs.getBoolean(KEY_GAME_ALPHABET_ENABLED, true)
        gameMatchWordsCheckbox.isChecked = prefs.getBoolean(KEY_GAME_MATCH_WORDS_ENABLED, true)
        gameBalloonsCheckbox.isChecked = prefs.getBoolean(KEY_GAME_BALLOONS_ENABLED, true)
        gameOppositesCheckbox.isChecked = prefs.getBoolean(KEY_GAME_OPPOSITES_ENABLED, true)
        gameLogicAddCheckbox.isChecked = prefs.getBoolean(KEY_GAME_LOGIC_ADD_ENABLED, true)
    }
    
    private fun countEnabledGames(): Int {
        var count = 0
        if (gameNumbersCheckbox.isChecked) count++
        if (gameAlphabetCheckbox.isChecked) count++
        if (gameMatchWordsCheckbox.isChecked) count++
        if (gameBalloonsCheckbox.isChecked) count++
        if (gameOppositesCheckbox.isChecked) count++
        if (gameLogicAddCheckbox.isChecked) count++
        return count
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
