// package com.ptitsyn.playandthen

// import android.content.Context
// import android.graphics.drawable.AnimationDrawable
// import android.media.AudioAttributes
// import android.media.AudioFocusRequest
// import android.media.AudioManager
// import android.media.MediaPlayer
// import android.media.audiofx.LoudnessEnhancer
// import android.os.Build
// import android.util.AttributeSet
// import android.util.Log
// import android.view.LayoutInflater
// import android.widget.*
// import androidx.constraintlayout.widget.ConstraintLayout
// import kotlin.random.Random

// class GameView @JvmOverloads constructor(
//     context: Context,
//     attrs: AttributeSet? = null,
//     defStyleAttr: Int = 0
// ) : ConstraintLayout(context, attrs, defStyleAttr) {

//     private lateinit var gameMessage: TextView
// //    private lateinit var speechBubbleText: TextView
//     private lateinit var hugeText: TextView
//     private lateinit var balloonContainers: List<LinearLayout>
//     private lateinit var cells: List<FrameLayout>
//     private lateinit var speakerButton: ImageView
//     private lateinit var characterImage: ImageView
    
//     private var targetNumber = 0
//     private var correctCellIndex = 0
//     private var gameActive = true
//     private var isNumberMode = false  // New: determines if we show numbers or balloons
//     private var mediaPlayer: MediaPlayer? = null
//     private var audioManager: AudioManager? = null
//     private var audioFocusRequest: AudioFocusRequest? = null
//     private var loudnessEnhancer: LoudnessEnhancer? = null
//     private var originalSystemVolume: Int = -1  // Store original volume to restore later
    
//     // Game completion callback
//     var onGameCompleted: (() -> Unit)? = null
    
//     // Balloon colors for variety
//     private val balloonColors = listOf(
//         "#FF6B6B", // Red
//         "#4ECDC4", // Teal  
//         "#45B7D1", // Blue
//         "#96CEB4", // Green
//         "#FFEAA7", // Yellow
//         "#DDA0DD", // Plum
//         "#98D8C8", // Mint
//         "#F7DC6F"  // Light Yellow
//     )
    
//     // Childish, bright colors for numbers
//     private val numberColors = listOf(
//         "#FF69B4", // Hot Pink
//         "#FF4500", // Orange Red
//         "#32CD32", // Lime Green
//         "#FF1493", // Deep Pink
//         "#00BFFF", // Deep Sky Blue
//         "#FFD700", // Gold
//         "#FF6347", // Tomato
//         "#9370DB", // Medium Purple
//         "#00CED1", // Dark Turquoise
//         "#FFA500"  // Orange
//     )
    
//     init {
//         // Initialize audio manager
//         audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//         initView()
//         setupGame()
//         setupClickListeners()
//     }
    
//     private fun initView() {
//         // Inflate the game layout
//         LayoutInflater.from(context).inflate(R.layout.game_view_layout, this, true)

//         gameMessage = findViewById(R.id.gameMessage)
// //        speechBubbleText = findViewById(R.id.speechBubbleText)
//         hugeText = findViewById(R.id.hugeNumberTextText)
        
//         // Initialize balloon containers
//         balloonContainers = listOf(
//             findViewById(R.id.balloonContainer0),
//             findViewById(R.id.balloonContainer1),
//             findViewById(R.id.balloonContainer2),
//             findViewById(R.id.balloonContainer3),
//             findViewById(R.id.balloonContainer4),
//             findViewById(R.id.balloonContainer5)
//         )
        
//         // Initialize cells
//         cells = listOf(
//             findViewById(R.id.cell0),
//             findViewById(R.id.cell1),
//             findViewById(R.id.cell2),
//             findViewById(R.id.cell3),
//             findViewById(R.id.cell4),
//             findViewById(R.id.cell5)
//         )
        
//         // Initialize speaker button and start glowing animation
//         speakerButton = findViewById(R.id.speakerButton)
//         startSpeakerGlowAnimation()
        
//         // Initialize father character image
//         characterImage = findViewById(R.id.characterImage)
//     }
    
//     fun setupGame() {
//         // Generate random target number (0-10)
//         targetNumber = Random.nextInt(0, 11)
        
//         // Randomly choose between number mode and balloon mode
//         isNumberMode = true//Random.nextBoolean()
        
//         // Control hugeText visibility - hide it in number mode
//         if (isNumberMode) {
//             hugeText.visibility = android.view.View.GONE
//         } else {
//             hugeText.visibility = android.view.View.VISIBLE
//             updateHugeText(targetNumber.toString())
//         }
        
//         // Choose random cell for correct answer
//         correctCellIndex = Random.nextInt(6)
        
//         // Clear all containers
//         balloonContainers.forEach { it.removeAllViews() }
        
//         // Reset cell backgrounds
//         cells.forEach { cell ->
//             cell.setBackgroundResource(R.drawable.cell_background)
//         }
        
//         // Generate counts for each cell
//         val counts = generateCounts()
        
//         // Place content (balloons or numbers) in each cell
//         counts.forEachIndexed { index, count ->
//             placeContent(index, count)
//         }
        
//         gameActive = true
        
//         // Play instructions based on the current mode after a short delay
//         postDelayed({
//             playInstructionSound()
//         }, 500)
//     }
    
//     private fun generateCounts(): List<Int> {
//         val counts = mutableListOf<Int>()
        
//         // Place target number in correct cell
//         repeat(6) { index ->
//             if (index == correctCellIndex) {
//                 counts.add(targetNumber)
//             } else {
//                 // Generate random count that's not equal to target number
//                 var randomCount: Int
//                 do {
//                     randomCount = Random.nextInt(0, 11)
//                 } while (randomCount == targetNumber || counts.contains(randomCount))
//                 counts.add(randomCount)
//             }
//         }
        
//         return counts
//     }
    
//     private fun placeContent(cellIndex: Int, count: Int) {
//         val container = balloonContainers[cellIndex]
        
//         if (isNumberMode) {
//             // Show large, colorful number
//             val numberTextView = createNumberTextView(count)
//             container.addView(numberTextView)
//         } else {
//             // Show balloons (existing logic)
//             placeBalloons(cellIndex, count)
//         }
//     }
    
//     private fun placeBalloons(cellIndex: Int, balloonCount: Int) {
//         val container = balloonContainers[cellIndex]
        
//         if (balloonCount == 0) {
//             // Show "0" text for zero balloons
//             val zeroText = TextView(context).apply {
//                 text = "0"
//                 textSize = 24f
//                 setTextColor(resources.getColor(android.R.color.darker_gray, context.theme))
//                 gravity = android.view.Gravity.CENTER
//             }
//             container.addView(zeroText)
//             return
//         }
        
//         // Create balloons based on count
//         when {
//             balloonCount <= 3 -> {
//                 // Single row for 1-3 balloons
//                 val row = LinearLayout(context).apply {
//                     orientation = LinearLayout.HORIZONTAL
//                     gravity = android.view.Gravity.CENTER
//                 }
//                 repeat(balloonCount) {
//                     row.addView(createBalloonImageView())
//                 }
//                 container.addView(row)
//             }
//             balloonCount <= 6 -> {
//                 // Two rows for 4-6 balloons
//                 val topRow = LinearLayout(context).apply {
//                     orientation = LinearLayout.HORIZONTAL
//                     gravity = android.view.Gravity.CENTER
//                 }
//                 val bottomRow = LinearLayout(context).apply {
//                     orientation = LinearLayout.HORIZONTAL
//                     gravity = android.view.Gravity.CENTER
//                 }
                
//                 val topCount = balloonCount / 2
//                 val bottomCount = balloonCount - topCount
                
//                 repeat(topCount) { topRow.addView(createBalloonImageView()) }
//                 repeat(bottomCount) { bottomRow.addView(createBalloonImageView()) }
                
//                 container.addView(topRow)
//                 container.addView(bottomRow)
//             }
//             else -> {
//                 // Three rows for 7+ balloons
//                 val rows = listOf(
//                     LinearLayout(context).apply {
//                         orientation = LinearLayout.HORIZONTAL
//                         gravity = android.view.Gravity.CENTER
//                     },
//                     LinearLayout(context).apply {
//                         orientation = LinearLayout.HORIZONTAL
//                         gravity = android.view.Gravity.CENTER
//                     },
//                     LinearLayout(context).apply {
//                         orientation = LinearLayout.HORIZONTAL
//                         gravity = android.view.Gravity.CENTER
//                     }
//                 )
                
//                 val balloonsPerRow = balloonCount / 3
//                 val remainder = balloonCount % 3
                
//                 rows.forEachIndexed { index, row ->
//                     val balloonsInThisRow = balloonsPerRow + if (index < remainder) 1 else 0
//                     repeat(balloonsInThisRow) { row.addView(createBalloonImageView()) }
//                     container.addView(row)
//                 }
//             }
//         }
//     }
    
//     private fun createBalloonImageView(): ImageView {
//         return ImageView(context).apply {
//             setImageResource(R.drawable.balloon)
//             layoutParams = LinearLayout.LayoutParams(32.dpToPx(), 42.dpToPx()).apply {
//                 setMargins(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
//             }
//             // Random color tint for variety
//             val randomColor = balloonColors.random()
//             setColorFilter(android.graphics.Color.parseColor(randomColor))
//         }
//     }
    
//     private fun createNumberTextView(number: Int): TextView {
//         return TextView(context).apply {
//             text = number.toString()
//             textSize = 56f  // Large, childish size
//             setTextColor(android.graphics.Color.parseColor(numberColors.random()))
//             gravity = android.view.Gravity.CENTER
//             typeface = android.graphics.Typeface.DEFAULT_BOLD  // Bold for playful look
            
//             // Add some padding for better visual appearance
//             setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
            
//             // Set layout parameters to center in container
//             layoutParams = LinearLayout.LayoutParams(
//                 LinearLayout.LayoutParams.MATCH_PARENT,
//                 LinearLayout.LayoutParams.MATCH_PARENT
//             ).apply {
//                 gravity = android.view.Gravity.CENTER
//             }
            
//             // Add a subtle shadow for depth (childish effect)
//             setShadowLayer(4f, 2f, 2f, android.graphics.Color.parseColor("#40000000"))
//         }
//     }
    
//     private fun setupClickListeners() {
//         // Father character click - play the target number sound for the child to hear

//         speakerButton.setOnClickListener {
//             if (gameActive) {
//                 playNumberSound(targetNumber)
//             }
//         }
        
//         // Cell clicks for answers
//         cells.forEachIndexed { index, cell ->
//             cell.setOnClickListener {
//                 if (gameActive) {
//                     handleCellClick(index)
//                 }
//             }
//         }
//     }
    
//     private fun handleCellClick(clickedIndex: Int) {
//         gameActive = false
        
//         if (clickedIndex == correctCellIndex) {
//             // Correct answer
//             cells[clickedIndex].setBackgroundColor(resources.getColor(android.R.color.holo_green_light, context.theme))
//             // Show success message on overlay
//             showGameMessage("🎉 Excellent! Well done!", android.R.color.holo_green_dark)
            
//             // Play well done sound
//             playFeedbackSound(true)
            
//             // Notify completion after a short delay
//             postDelayed({
//                 restoreOriginalSystemVolume()  // Restore volume before completing
//                 onGameCompleted?.invoke()
//             }, 2000) // 2 second delay to show success message
            
//         } else {
//             // Wrong answer
//             cells[clickedIndex].setBackgroundColor(resources.getColor(android.R.color.holo_red_light, context.theme))
//             cells[correctCellIndex].setBackgroundColor(resources.getColor(android.R.color.holo_green_light, context.theme))
//             // Show try again message on overlay
//             showGameMessage("Try again! The correct answer is highlighted in green.", android.R.color.holo_orange_dark)
            
//             // Play try again sound
//             playFeedbackSound(false)
            
//             // Reset game after showing feedback
//             postDelayed({
//                 setupGame()
//             }, 3000) // 3 second delay to show feedback
//         }
//     }
    
//     private fun showGameMessage(message: String, colorResId: Int) {
//         gameMessage.text = message
//         gameMessage.setBackgroundColor(resources.getColor(colorResId, context.theme))
//         gameMessage.visibility = android.view.View.VISIBLE
        
//         // Hide message after delay
//         postDelayed({
//             gameMessage.visibility = android.view.View.GONE
//         }, 1500)
//     }
    
// //    private fun updateSpeechBubble(message: String) {
// //        speechBubbleText.text = message
// //    }

//     private fun updateHugeText(message: String) {
//         hugeText.text = message
//     }
    
//     private fun startSpeakerGlowAnimation() {
//         // Apply the glow animation as background while keeping the speaker image as src
//         speakerButton.setBackgroundResource(R.drawable.speaker_glow)
//         val animationDrawable = speakerButton.background as AnimationDrawable
//         animationDrawable.start()
//     }
    
//     private fun playNumberSound(number: Int) {
//         Log.d("GameView", "playNumberSound called with number: $number")
        
//         try {
//             // Release any existing MediaPlayer
//             mediaPlayer?.release()
//             mediaPlayer = null
            
//             // Only play sound for numbers 1-10 (we don't have a sound file for 0)
//             if (number in 1..10) {
//                 val soundResourceId = when (number) {
//                     1 -> R.raw.one
//                     2 -> R.raw.two
//                     3 -> R.raw.three
//                     4 -> R.raw.four
//                     5 -> R.raw.five
//                     6 -> R.raw.six
//                     7 -> R.raw.seven
//                     8 -> R.raw.eight
//                     9 -> R.raw.nine
//                     10 -> R.raw.ten
//                     else -> {
//                         Log.w("GameView", "No sound file available for number: $number")
//                         return
//                     }
//                 }
                
//                 Log.d("GameView", "Attempting to play sound resource: $soundResourceId")
                
//                 // Request audio focus before playing
//                 if (!requestAudioFocus()) {
//                     Log.w("GameView", "Failed to gain audio focus")
//                     return
//                 }
                
//                 // Create MediaPlayer with proper audio attributes
//                 mediaPlayer = MediaPlayer().apply {
//                     // Set audio attributes for proper stream type
//                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                         setAudioAttributes(
//                             AudioAttributes.Builder()
//                                 .setUsage(AudioAttributes.USAGE_MEDIA)
//                                 .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                                 .build()
//                         )
//                     } else {
//                         @Suppress("DEPRECATION")
//                         setAudioStreamType(AudioManager.STREAM_MUSIC)
//                     }
                    
//                     // Set up completion listener
//                     setOnCompletionListener { mp ->
//                         Log.d("GameView", "Sound playback completed")
//                         cleanupAudioEnhancements()
//                         abandonAudioFocus()
//                         mp.release()
//                         mediaPlayer = null
//                     }
                    
//                     // Set up error listener
//                     setOnErrorListener { mp, what, extra ->
//                         Log.e("GameView", "MediaPlayer error: what=$what, extra=$extra")
//                         cleanupAudioEnhancements()
//                         abandonAudioFocus()
//                         mp.release()
//                         mediaPlayer = null
//                         true // return true to indicate error was handled
//                     }
                    
//                     // Set data source and prepare
//                     try {
//                         val afd = context.resources.openRawResourceFd(soundResourceId)
//                         setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
//                         afd.close()
                        
//                         prepareAsync()
//                         setOnPreparedListener { mp ->
//                             Log.d("GameView", "MediaPlayer prepared, boosting volume and starting playback")
                            
                            
//                             // Boost system volume to maximum for media stream
//                             boostSystemVolume()
                            
//                             // Apply loudness enhancement for additional volume boost
//                             applyLoudnessEnhancement(mp)
                            
//                             mp.start()
//                         }
//                     } catch (e: Exception) {
//                         Log.e("GameView", "Error preparing MediaPlayer", e)
//                         abandonAudioFocus()
//                         release()
//                         mediaPlayer = null
//                     }
//                 }
//             } else {
//                 Log.d("GameView", "Number $number is out of range (1-10), no sound to play")
//             }
//         } catch (e: Exception) {
//             Log.e("GameView", "Error in playNumberSound for number $number", e)
//             abandonAudioFocus()
//             mediaPlayer?.release()
//             mediaPlayer = null
//         }
//     }
    
//     private fun requestAudioFocus(): Boolean {
//         return try {
//             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                 // For API 26 and above
//                 if (audioFocusRequest == null) {
//                     audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
//                         .setAudioAttributes(
//                             AudioAttributes.Builder()
//                                 .setUsage(AudioAttributes.USAGE_MEDIA)
//                                 .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                                 .build()
//                         )
//                         .setOnAudioFocusChangeListener { focusChange ->
//                             Log.d("GameView", "Audio focus changed: $focusChange")
//                             when (focusChange) {
//                                 AudioManager.AUDIOFOCUS_LOSS,
//                                 AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
//                                     mediaPlayer?.pause()
//                                 }
//                                 AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
//                                     // Even when ducking, keep volume at maximum
//                                     mediaPlayer?.setVolume(1.0f, 1.0f)
//                                     // Re-apply loudness enhancement
//                                     mediaPlayer?.let { applyLoudnessEnhancement(it) }
//                                 }
//                                 AudioManager.AUDIOFOCUS_GAIN -> {
//                                     // Boost to maximum volume
//                                     mediaPlayer?.setVolume(1.0f, 1.0f)
//                                     boostSystemVolume()
//                                     mediaPlayer?.let { applyLoudnessEnhancement(it) }
//                                     mediaPlayer?.start()
//                                 }
//                             }
//                         }
//                         .build()
//                 }
//                 val result = audioManager?.requestAudioFocus(audioFocusRequest!!)
//                 result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
//             } else {
//                 // For older API versions
//                 @Suppress("DEPRECATION")
//                 val result = audioManager?.requestAudioFocus(
//                     { focusChange ->
//                         Log.d("GameView", "Audio focus changed: $focusChange")
//                         when (focusChange) {
//                             AudioManager.AUDIOFOCUS_LOSS,
//                             AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
//                                 mediaPlayer?.pause()
//                             }
//                             AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
//                                 // Even when ducking, keep volume at maximum
//                                 mediaPlayer?.setVolume(1.0f, 1.0f)
//                                 // Re-apply loudness enhancement
//                                 mediaPlayer?.let { applyLoudnessEnhancement(it) }
//                             }
//                             AudioManager.AUDIOFOCUS_GAIN -> {
//                                 // Boost to maximum volume
//                                 mediaPlayer?.setVolume(1.0f, 1.0f)
//                                 boostSystemVolume()
//                                 mediaPlayer?.let { applyLoudnessEnhancement(it) }
//                                 mediaPlayer?.start()
//                             }
//                         }
//                     },
//                     AudioManager.STREAM_MUSIC,
//                     AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
//                 )
//                 result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
//             }
//         } catch (e: Exception) {
//             Log.e("GameView", "Error requesting audio focus", e)
//             false
//         }
//     }
    
//     private fun abandonAudioFocus() {
//         try {
//             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                 audioFocusRequest?.let { request ->
//                     audioManager?.abandonAudioFocusRequest(request)
//                 }
//             } else {
//                 @Suppress("DEPRECATION")
//                 audioManager?.abandonAudioFocus(null)
//             }
//         } catch (e: Exception) {
//             Log.e("GameView", "Error abandoning audio focus", e)
//         }
//     }
    
//     private fun boostSystemVolume() {
//         try {
//             audioManager?.let { am ->
//                 // Get current and maximum volume for media stream
//                 val currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
//                 val maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                
//                 // Store original volume if not already stored
//                 if (originalSystemVolume == -1) {
//                     originalSystemVolume = currentVolume
//                     Log.d("GameView", "Stored original system volume: $originalSystemVolume")
//                 }
                
//                 Log.d("GameView", "Current volume: $currentVolume, Max volume: $maxVolume")
                
//                 // Set to maximum volume if not already at max
//                 if (currentVolume < maxVolume) {
//                     am.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
//                     Log.d("GameView", "System volume boosted to maximum: $maxVolume")
//                 }
//             }
//         } catch (e: Exception) {
//             Log.e("GameView", "Error boosting system volume", e)
//         }
//     }
    
//     private fun applyLoudnessEnhancement(mediaPlayer: MediaPlayer) {
//         try {
//             // Release previous loudness enhancer if exists
//             loudnessEnhancer?.release()
            
//             // Create LoudnessEnhancer for significant volume boost
//             loudnessEnhancer = LoudnessEnhancer(mediaPlayer.audioSessionId).apply {
//                 // Set target gain in millibels (mB) - 2000mB = +20dB gain (very loud boost)
//                 // This provides significant amplification beyond normal volume levels
//                 setTargetGain(500) // 1000 mB = 5dB boost - very significant increase
//                 enabled = true
//                 Log.d("GameView", "LoudnessEnhancer applied with +20dB gain")
//             }
//         } catch (e: Exception) {
//             Log.e("GameView", "Error applying loudness enhancement", e)
//             // Continue without enhancement if not supported
//         }
//     }
    
//     private fun playInstructionSound() {
//         Log.d("GameView", "playInstructionSound called for mode: ${if (isNumberMode) "numbers" else "balloons"}")
        
//         try {
//             // Release any existing MediaPlayer
//             mediaPlayer?.release()
//             mediaPlayer = null
            
//             val soundResourceId = if (isNumberMode) {
//                 R.raw.instructions_numbers_mode
//             } else {
//                 R.raw.instructions_baloons_mode
//             }
            
//             Log.d("GameView", "Playing instruction sound resource: $soundResourceId")
            
//             // Request audio focus before playing
//             if (!requestAudioFocus()) {
//                 Log.w("GameView", "Failed to gain audio focus for instruction sound")
//                 return
//             }
            
//             // Create MediaPlayer with proper audio attributes
//             mediaPlayer = MediaPlayer().apply {
//                 // Set audio attributes for proper stream type
//                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                     setAudioAttributes(
//                         AudioAttributes.Builder()
//                             .setUsage(AudioAttributes.USAGE_MEDIA)
//                             .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                             .build()
//                     )
//                 } else {
//                     @Suppress("DEPRECATION")
//                     setAudioStreamType(AudioManager.STREAM_MUSIC)
//                 }
                
//                 // Set up completion listener
//                 setOnCompletionListener { mp ->
//                     Log.d("GameView", "Instruction sound playback completed")
//                     cleanupAudioEnhancements()
//                     abandonAudioFocus()
//                     mp.release()
//                     mediaPlayer = null
                    
//                     // Play the target number sound after instructions complete
//                     // Add a short delay before playing the number sound
//                     postDelayed({
//                         if (gameActive && targetNumber in 1..10) {
//                             playNumberSound(targetNumber)
//                         }
//                     }, 500) // 500ms delay between instruction and number sound
//                 }
                
//                 // Set up error listener
//                 setOnErrorListener { mp, what, extra ->
//                     Log.e("GameView", "MediaPlayer error for instruction sound: what=$what, extra=$extra")
//                     cleanupAudioEnhancements()
//                     abandonAudioFocus()
//                     mp.release()
//                     mediaPlayer = null
//                     true // return true to indicate error was handled
//                 }
                
//                 // Set data source and prepare
//                 try {
//                     val afd = context.resources.openRawResourceFd(soundResourceId)
//                     setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
//                     afd.close()
                    
//                     prepareAsync()
//                     setOnPreparedListener { mp ->
//                         Log.d("GameView", "Instruction MediaPlayer prepared, starting playback")
                        
                        
//                         // Boost system volume to maximum for media stream
//                         boostSystemVolume()
                        
//                         // Apply loudness enhancement for additional volume boost
//                         applyLoudnessEnhancement(mp)
                        
//                         mp.start()
//                     }
//                 } catch (e: Exception) {
//                     Log.e("GameView", "Error preparing MediaPlayer for instruction sound", e)
//                     abandonAudioFocus()
//                     release()
//                     mediaPlayer = null
//                 }
//             }
//         } catch (e: Exception) {
//             Log.e("GameView", "Error in playInstructionSound", e)
//             abandonAudioFocus()
//             mediaPlayer?.release()
//             mediaPlayer = null
//         }
//     }
    
//     private fun playFeedbackSound(isCorrect: Boolean) {
//         Log.d("GameView", "playFeedbackSound called for: ${if (isCorrect) "correct answer" else "incorrect answer"}")
        
//         try {
//             // Release any existing MediaPlayer
//             mediaPlayer?.release()
//             mediaPlayer = null
            
//             val soundResourceId = if (isCorrect) {
//                 R.raw.well_done
//             } else {
//                 R.raw.try_again
//             }
            
//             Log.d("GameView", "Playing feedback sound resource: $soundResourceId")
            
//             // Request audio focus before playing
//             if (!requestAudioFocus()) {
//                 Log.w("GameView", "Failed to gain audio focus for feedback sound")
//                 return
//             }
            
//             // Create MediaPlayer with proper audio attributes
//             mediaPlayer = MediaPlayer().apply {
//                 // Set audio attributes for proper stream type
//                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                     setAudioAttributes(
//                         AudioAttributes.Builder()
//                             .setUsage(AudioAttributes.USAGE_MEDIA)
//                             .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                             .build()
//                     )
//                 } else {
//                     @Suppress("DEPRECATION")
//                     setAudioStreamType(AudioManager.STREAM_MUSIC)
//                 }
                
//                 // Set up completion listener
//                 setOnCompletionListener { mp ->
//                     Log.d("GameView", "Feedback sound playback completed")
//                     cleanupAudioEnhancements()
//                     abandonAudioFocus()
//                     mp.release()
//                     mediaPlayer = null
//                 }
                
//                 // Set up error listener
//                 setOnErrorListener { mp, what, extra ->
//                     Log.e("GameView", "MediaPlayer error for feedback sound: what=$what, extra=$extra")
//                     cleanupAudioEnhancements()
//                     abandonAudioFocus()
//                     mp.release()
//                     mediaPlayer = null
//                     true // return true to indicate error was handled
//                 }
                
//                 // Set data source and prepare
//                 try {
//                     val afd = context.resources.openRawResourceFd(soundResourceId)
//                     setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
//                     afd.close()
                    
//                     prepareAsync()
//                     setOnPreparedListener { mp ->
//                         Log.d("GameView", "Feedback MediaPlayer prepared, starting playback")
                        
//                         // Boost system volume to maximum for media stream
//                         boostSystemVolume()
                        
//                         // Apply loudness enhancement for additional volume boost
//                         applyLoudnessEnhancement(mp)
                        
//                         mp.start()
//                     }
//                 } catch (e: Exception) {
//                     Log.e("GameView", "Error preparing MediaPlayer for feedback sound", e)
//                     abandonAudioFocus()
//                     release()
//                     mediaPlayer = null
//                 }
//             }
//         } catch (e: Exception) {
//             Log.e("GameView", "Error in playFeedbackSound", e)
//             abandonAudioFocus()
//             mediaPlayer?.release()
//             mediaPlayer = null
//         }
//     }
    
//     private fun cleanupAudioEnhancements() {
//         try {
//             loudnessEnhancer?.release()
//             loudnessEnhancer = null
//             Log.d("GameView", "Audio enhancements cleaned up")
//         } catch (e: Exception) {
//             Log.e("GameView", "Error cleaning up audio enhancements", e)
//         }
//     }
    
//     private fun restoreOriginalSystemVolume() {
//         try {
//             if (originalSystemVolume != -1) {
//                 audioManager?.let { am ->
//                     am.setStreamVolume(AudioManager.STREAM_MUSIC, originalSystemVolume, 0)
//                     Log.d("GameView", "System volume restored to original level: $originalSystemVolume")
//                     originalSystemVolume = -1  // Reset the stored volume
//                 }
//             }
//         } catch (e: Exception) {
//             Log.e("GameView", "Error restoring original system volume", e)
//         }
//     }
    
//     // Clean up MediaPlayer when view is destroyed
//     override fun onDetachedFromWindow() {
//         super.onDetachedFromWindow()
//         cleanupAudioEnhancements()
//         abandonAudioFocus()
//         restoreOriginalSystemVolume()  // Restore volume when view is destroyed
//         mediaPlayer?.release()
//         mediaPlayer = null
//     }
    
//     // Extension function to convert dp to pixels
//     private fun Int.dpToPx(): Int {
//         return (this * resources.displayMetrics.density).toInt()
//     }
// }
