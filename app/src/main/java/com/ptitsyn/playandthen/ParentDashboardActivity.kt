package com.ptitsyn.playandthen

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ParentDashboardActivity : AppCompatActivity() {

    private lateinit var setupCard: CardView
    private lateinit var setupText: TextView
    private lateinit var enableAccessibilityButton: Button
    private lateinit var giveConsentButton: Button
    private lateinit var gamesRecyclerView: RecyclerView
    private lateinit var gameAdapter: GameTileAdapter
    
    // Settings
    private lateinit var intervalSeekBar: SeekBar
    private lateinit var intervalValue: TextView
    private lateinit var roundsSeekBar: SeekBar
    private lateinit var roundsValue: TextView
    private lateinit var languageFilterSwitch: Switch
    private lateinit var languageSection: View

    private val games = listOf(
        GameInfo("numbers", "Numbers", R.drawable.game_placeholder, SettingsActivity.KEY_GAME_NUMBERS_ENABLED),
        GameInfo("alphabet", "Alphabet", R.drawable.game_placeholder, SettingsActivity.KEY_GAME_ALPHABET_ENABLED),
        GameInfo("match_words", "Match Words", R.drawable.game_placeholder, SettingsActivity.KEY_GAME_MATCH_WORDS_ENABLED),
        GameInfo("opposites", "Opposites", R.drawable.game_placeholder, SettingsActivity.KEY_GAME_OPPOSITES_ENABLED),
        GameInfo("logic_add", "Logic Add", R.drawable.game_placeholder, SettingsActivity.KEY_GAME_LOGIC_ADD_ENABLED),
        GameInfo("balloons", "Balloons", R.drawable.game_placeholder, SettingsActivity.KEY_GAME_BALLOONS_ENABLED)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent_dashboard)
        
        initViews()
        setupGameTiles()
        setupSettings()
    }
    
    override fun onResume() {
        super.onResume()
        updateSetupStatus()
        gameAdapter.notifyDataSetChanged()
        loadSettings()
    }
    
    private fun initViews() {
        setupCard = findViewById(R.id.setupCard)
        setupText = findViewById(R.id.setupText)
        enableAccessibilityButton = findViewById(R.id.enableAccessibilityButton)
        giveConsentButton = findViewById(R.id.giveConsentButton)
        gamesRecyclerView = findViewById(R.id.gamesRecyclerView)
        
        intervalSeekBar = findViewById(R.id.intervalSeekBar)
        intervalValue = findViewById(R.id.intervalValue)
        roundsSeekBar = findViewById(R.id.roundsSeekBar)
        roundsValue = findViewById(R.id.roundsValue)
        languageFilterSwitch = findViewById(R.id.languageFilterSwitch)
        languageSection = findViewById(R.id.languageSection)
        
        enableAccessibilityButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        
        giveConsentButton.setOnClickListener {
            startActivity(Intent(this, ConsentActivity::class.java))
        }
    }
    
    private fun setupGameTiles() {
        gameAdapter = GameTileAdapter(games) { game ->
            toggleGame(game)
        }
        gamesRecyclerView.layoutManager = GridLayoutManager(this, 2)
        gamesRecyclerView.adapter = gameAdapter
    }
    
    private fun toggleGame(game: GameInfo) {
        val prefs = SettingsActivity.getPreferences(this)
        val currentState = prefs.getBoolean(game.prefKey, true)
        
        // Check if this is the last enabled game
        val enabledCount = games.count { prefs.getBoolean(it.prefKey, true) }
        if (currentState && enabledCount <= 1) {
            Toast.makeText(this, "At least one game must be enabled", Toast.LENGTH_SHORT).show()
            return
        }
        
        prefs.edit().putBoolean(game.prefKey, !currentState).apply()
        gameAdapter.notifyDataSetChanged()
    }
    
    private fun setupSettings() {
        val prefs = SettingsActivity.getPreferences(this)
        
        // Interval (1-60 minutes)
        intervalSeekBar.max = 59
        intervalSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress + 1
                intervalValue.text = "$value min"
                if (fromUser) {
                    prefs.edit().putLong(SettingsActivity.KEY_GAME_OVERLAY_INTERVAL, value.toLong()).apply()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Rounds (1-5)
        roundsSeekBar.max = 4
        roundsSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress + 1
                roundsValue.text = "$value"
                if (fromUser) {
                    prefs.edit().putInt(SettingsActivity.KEY_NUMBER_OF_ROUNDS, value).apply()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Language filter
        languageFilterSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(SettingsActivity.KEY_LANGUAGE_FILTER_ENABLED, isChecked).apply()
            languageSection.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        // Language multi-select button
        val languageButton = findViewById<Button>(R.id.languageSelectButton)
        languageButton.setOnClickListener {
            showLanguageMultiSelectDialog()
        }
        
        loadSettings()
    }
    
    private fun showLanguageMultiSelectDialog() {
        val languages = arrayOf("English", "Hebrew", "Russian", "Spanish", "French", "German")
        val languageCodes = arrayOf("en", "he", "ru", "es", "fr", "de")
        
        val prefs = SettingsActivity.getPreferences(this)
        val savedLanguages = prefs.getStringSet(SettingsActivity.KEY_ALLOWED_LANGUAGES, setOf("en")) ?: setOf("en")
        val checkedItems = languageCodes.map { savedLanguages.contains(it) }.toBooleanArray()
        
        val selectedLanguages = savedLanguages.toMutableSet()
        
        android.app.AlertDialog.Builder(this)
            .setTitle("Select Allowed Languages")
            .setMultiChoiceItems(languages, checkedItems) { _, which, isChecked ->
                if (isChecked) {
                    selectedLanguages.add(languageCodes[which])
                } else {
                    selectedLanguages.remove(languageCodes[which])
                }
            }
            .setPositiveButton("OK") { _, _ ->
                if (selectedLanguages.isEmpty()) {
                    selectedLanguages.add("en") // At least one language
                    Toast.makeText(this, "At least one language required", Toast.LENGTH_SHORT).show()
                }
                prefs.edit().putStringSet(SettingsActivity.KEY_ALLOWED_LANGUAGES, selectedLanguages).apply()
                updateLanguageButtonText()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun updateLanguageButtonText() {
        val prefs = SettingsActivity.getPreferences(this)
        val savedLanguages = prefs.getStringSet(SettingsActivity.KEY_ALLOWED_LANGUAGES, setOf("en")) ?: setOf("en")
        
        val languageNames = mapOf("en" to "English", "he" to "Hebrew", "ru" to "Russian", "es" to "Spanish", "fr" to "French", "de" to "German")
        val names = savedLanguages.mapNotNull { languageNames[it] }
        
        val languageButton = findViewById<Button>(R.id.languageSelectButton)
        languageButton.text = if (names.size <= 2) names.joinToString(", ") else "${names.take(2).joinToString(", ")} +${names.size - 2}"
    }
    
    private fun loadSettings() {
        val prefs = SettingsActivity.getPreferences(this)
        
        val interval = prefs.getLong(SettingsActivity.KEY_GAME_OVERLAY_INTERVAL, 15).toInt()
        intervalSeekBar.progress = interval - 1
        intervalValue.text = "$interval min"
        
        val rounds = prefs.getInt(SettingsActivity.KEY_NUMBER_OF_ROUNDS, 2)
        roundsSeekBar.progress = rounds - 1
        roundsValue.text = "$rounds"
        
        val filterEnabled = prefs.getBoolean(SettingsActivity.KEY_LANGUAGE_FILTER_ENABLED, false)
        languageFilterSwitch.isChecked = filterEnabled
        languageSection.visibility = if (filterEnabled) View.VISIBLE else View.GONE
        
        updateLanguageButtonText()
    }
    
    private fun updateSetupStatus() {
        val hasConsent = ConsentActivity.hasConsent(this)
        val hasAccessibility = isAccessibilityServiceEnabled()
        
        if (hasConsent && hasAccessibility) {
            setupCard.visibility = View.GONE
        } else {
            setupCard.visibility = View.VISIBLE
            
            val issues = mutableListOf<String>()
            if (!hasConsent) issues.add("consent")
            if (!hasAccessibility) issues.add("accessibility service")
            
            setupText.text = "Setup required: Enable ${issues.joinToString(" and ")}"
            giveConsentButton.visibility = if (hasConsent) View.GONE else View.VISIBLE
            enableAccessibilityButton.visibility = if (hasAccessibility) View.GONE else View.VISIBLE
        }
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabledServices.any { it.id.contains(packageName) }
    }
    
    // Inner adapter class
    inner class GameTileAdapter(
        private val games: List<GameInfo>,
        private val onGameClick: (GameInfo) -> Unit
    ) : RecyclerView.Adapter<GameTileAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val card: CardView = view.findViewById(R.id.gameCard)
            val image: ImageView = view.findViewById(R.id.gameImage)
            val name: TextView = view.findViewById(R.id.gameName)
            val overlay: View = view.findViewById(R.id.disabledOverlay)
            val checkmark: ImageView = view.findViewById(R.id.checkmark)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_game_tile, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val game = games[position]
            val prefs = SettingsActivity.getPreferences(this@ParentDashboardActivity)
            val isEnabled = prefs.getBoolean(game.prefKey, true)
            
            holder.name.text = game.displayName
            holder.image.setImageResource(game.imageRes)
            holder.overlay.visibility = if (isEnabled) View.GONE else View.VISIBLE
            holder.checkmark.visibility = if (isEnabled) View.VISIBLE else View.GONE
            
            holder.card.setOnClickListener { onGameClick(game) }
        }
        
        override fun getItemCount() = games.size
    }
}

data class GameInfo(
    val id: String,
    val displayName: String,
    val imageRes: Int,
    val prefKey: String
)
