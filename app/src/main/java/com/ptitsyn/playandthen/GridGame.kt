package com.ptitsyn.playandthen

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.LoudnessEnhancer
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * Abstract base class for grid-based educational games.
 * 
 * Parent: UI orchestration, audio playback, game flow
 * Child: All game logic and content
 */
abstract class GridGame @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    // UI Components
    private lateinit var gameMessage: android.widget.TextView
    private lateinit var hugeText: android.widget.TextView
    private lateinit var cellContainers: List<LinearLayout>
    private lateinit var cells: List<android.widget.FrameLayout>
    private lateinit var speakerButton: android.widget.ImageView
    private lateinit var characterImage: android.widget.ImageView
    
    // Game State
    private var gameActive = true
    
    // Audio Management
    private var audioManager: AudioManager? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    
    // Game completion callback
    var onGameCompleted: (() -> Unit)? = null
    
    /**
     * Child must implement these methods
     */
    
    abstract fun populateCells(containers: List<LinearLayout>)
    abstract fun isCorrect(clickedCellIndex: Int): Boolean
    abstract fun getInstructions(): GameInstructions
    abstract fun getHugeTextContent(): String?
    abstract fun getTargetSoundResourceId(): Int
    
    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        initView()
        // Note: startGame() must be called by child classes after their properties are initialized
    }
    
    private fun initView() {
        android.view.LayoutInflater.from(context).inflate(R.layout.game_view_layout, this, true)

        gameMessage = findViewById(R.id.gameMessage)
        hugeText = findViewById(R.id.hugeNumberTextText)
        
        cellContainers = listOf(
            findViewById(R.id.cellContainer0),
            findViewById(R.id.cellContainer1),
            findViewById(R.id.cellContainer2),
            findViewById(R.id.cellContainer3),
            findViewById(R.id.cellContainer4),
            findViewById(R.id.cellContainer5)
        )
        
        cells = listOf(
            findViewById(R.id.cell0),
            findViewById(R.id.cell1),
            findViewById(R.id.cell2),
            findViewById(R.id.cell3),
            findViewById(R.id.cell4),
            findViewById(R.id.cell5)
        )
        
        speakerButton = findViewById(R.id.speakerButton)
        startSpeakerGlowAnimation()
        
        characterImage = findViewById(R.id.characterImage)
        
        setupClickListeners()
    }
    
    /**
     * Initializes and starts the game.
     * Must be called by child classes in their init block after all properties are initialized.
     */
    protected fun startGame() {
        setupGame()
        
        // Play instructions after a short delay
        postDelayed({
            playInstructionSound()
        }, 500)
    }
    
    fun setupGame() {
        // Clear all cell containers
        cellContainers.forEach { it.removeAllViews() }
        
        // Reset cell backgrounds to default
        cells.forEach { cell ->
            cell.setBackgroundResource(R.drawable.cell_background)
        }
        
        // Let child game populate the cells with its content
        populateCells(cellContainers)


        // Update huge text visibility and content based on child's needs
        val hugeTextContent = getHugeTextContent()
        if (hugeTextContent != null) {
            hugeText.visibility = android.view.View.VISIBLE
            hugeText.text = hugeTextContent
        } else {
            hugeText.visibility = android.view.View.GONE
        }


        gameActive = true
    }
    
    private fun setupClickListeners() {
        // Speaker button - play target sound
        speakerButton.setOnClickListener {
            if (gameActive) {
                playTargetSound()
            }
        }
        
        // Cell clicks for answers
        cells.forEachIndexed { index, cell ->
            cell.setOnClickListener {
                if (gameActive) {
                    handleCellClick(index)
                }
            }
        }
    }
    
    private fun handleCellClick(clickedIndex: Int) {
        gameActive = false
        
        if (isCorrect(clickedIndex)) {
            // Correct answer
            cells[clickedIndex].setBackgroundColor(
                resources.getColor(android.R.color.holo_green_light, context.theme)
            )
//            showGameMessage("🎉 Excellent! Well done!", android.R.color.holo_green_dark)
            
            // Play well done sound
            playFeedbackSound(true)
            
            // Notify completion after a short delay
            postDelayed({
                onGameCompleted?.invoke()
            }, 2000)
            
        } else {
            // Wrong answer - find correct cell and highlight both
            cells[clickedIndex].setBackgroundColor(
                resources.getColor(android.R.color.holo_red_light, context.theme)
            )
            
            // Find and highlight correct cell
            cells.forEachIndexed { index, cell ->
                if (isCorrect(index)) {
                    cell.setBackgroundColor(
                        resources.getColor(android.R.color.holo_green_light, context.theme)
                    )
                }
            }
            
//            showGameMessage(
//                "Try again! The correct answer is highlighted in green.",
//                android.R.color.holo_orange_dark
//            )
            
            // Play try again sound
            playFeedbackSound(false)
            
            // Reset game after showing feedback
            postDelayed({
                setupGame()
                playTargetSound()
            }, 3000)
        }
    }
    
    private fun playTargetSound() {
        playSound(getTargetSoundResourceId(), "target") {
            Log.d("GridGame", "Target sound playback completed")
        }
    }
    
    private fun playInstructionSound() {
        val instructions = getInstructions()
        
        playSound(instructions.audioResourceId, "instruction") {
            Log.d("GridGame", "Instruction sound playback completed")
            
            // Play target sound after instructions
            postDelayed({
                if (gameActive) {
                    playTargetSound()
                }
            }, 50)
        }
    }
    
    private fun playFeedbackSound(isCorrect: Boolean) {
        val soundResourceId = if (isCorrect) {
            R.raw.well_done
        } else {
            R.raw.try_again
        }
        
        playSound(soundResourceId, "feedback") {
            Log.d("GridGame", "Feedback sound playback completed")
        }
    }
    
    private fun playSound(resourceId: Int, soundType: String, onComplete: () -> Unit) {
        Log.d("GridGame", "playSound called with resourceId: $resourceId, type: $soundType")
        
        try {
            // Release any existing MediaPlayer
            mediaPlayer?.release()
            mediaPlayer = null
            
            // Request audio focus before playing
            if (!requestAudioFocus()) {
                Log.w("GridGame", "Failed to gain audio focus")
                return
            }
            
            // Create MediaPlayer with proper audio attributes
            mediaPlayer = MediaPlayer().apply {
                // Set audio attributes for proper stream type
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(
                                if (soundType == "target") AudioAttributes.CONTENT_TYPE_MUSIC
                                else AudioAttributes.CONTENT_TYPE_SPEECH
                            )
                            .build()
                    )
                } else {
                    @Suppress("DEPRECATION")
                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                }
                
                // Set up completion listener
                setOnCompletionListener { mp ->
                    Log.d("GridGame", "Sound playback completed")
                    cleanupAudioEnhancements()
                    abandonAudioFocus()
                    mp.release()
                    mediaPlayer = null
                    onComplete()
                }
                
                // Set up error listener
                setOnErrorListener { mp, what, extra ->
                    Log.e("GridGame", "MediaPlayer error: what=$what, extra=$extra")
                    cleanupAudioEnhancements()
                    abandonAudioFocus()
                    mp.release()
                    mediaPlayer = null
                    onComplete()
                    true
                }
                
                // Set data source and prepare
                try {
                    val afd = context.resources.openRawResourceFd(resourceId)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    
                    prepareAsync()
                    setOnPreparedListener { mp ->
                        Log.d("GridGame", "MediaPlayer prepared, boosting volume and starting playback")
                        mp.start()
                    }
                } catch (e: Exception) {
                    Log.e("GridGame", "Error preparing MediaPlayer", e)
                    abandonAudioFocus()
                    release()
                    mediaPlayer = null
                }
            }
        } catch (e: Exception) {
            Log.e("GridGame", "Error in playSound", e)
            abandonAudioFocus()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
    
    private fun requestAudioFocus(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (audioFocusRequest == null) {
                    audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setOnAudioFocusChangeListener { focusChange ->
                            handleAudioFocusChange(focusChange)
                        }
                        .build()
                }
                val result = audioManager?.requestAudioFocus(audioFocusRequest!!)
                result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                @Suppress("DEPRECATION")
                val result = audioManager?.requestAudioFocus(
                    { focusChange -> handleAudioFocusChange(focusChange) },
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
                result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        } catch (e: Exception) {
            Log.e("GridGame", "Error requesting audio focus", e)
            false
        }
    }
    
    private fun abandonAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { request ->
                    audioManager?.abandonAudioFocusRequest(request)
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(null)
            }
        } catch (e: Exception) {
            Log.e("GridGame", "Error abandoning audio focus", e)
        }
    }
    
    private fun boostSystemVolume() {
        try {
            audioManager?.let { am ->
                val currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                
                if (currentVolume < maxVolume) {
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
                    Log.d("GridGame", "System volume boosted to maximum: $maxVolume")
                }
            }
        } catch (e: Exception) {
            Log.e("GridGame", "Error boosting system volume", e)
        }
    }

    
    private fun cleanupAudioEnhancements() {
        try {
            loudnessEnhancer?.release()
            loudnessEnhancer = null
            Log.d("GridGame", "Audio enhancements cleaned up")
        } catch (e: Exception) {
            Log.e("GridGame", "Error cleaning up audio enhancements", e)
        }
    }
    
    private fun handleAudioFocusChange(focusChange: Int) {
        Log.d("GridGame", "Audio focus changed: $focusChange")
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mediaPlayer?.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.setVolume(1.0f, 1.0f)
                boostSystemVolume()
                mediaPlayer?.start()
            }
        }
    }
    
    private fun showGameMessage(message: String, colorResId: Int) {
        gameMessage.text = message
        gameMessage.setBackgroundColor(resources.getColor(colorResId, context.theme))
        gameMessage.visibility = android.view.View.VISIBLE
        
        postDelayed({
            gameMessage.visibility = android.view.View.GONE
        }, 1500)
    }
    
    private fun startSpeakerGlowAnimation() {
        speakerButton.setBackgroundResource(R.drawable.speaker_glow)
        val animationDrawable = speakerButton.background as AnimationDrawable
        animationDrawable.start()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cleanupAudioEnhancements()
        abandonAudioFocus()
        mediaPlayer?.release()
        mediaPlayer = null
    }
    
    protected fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
