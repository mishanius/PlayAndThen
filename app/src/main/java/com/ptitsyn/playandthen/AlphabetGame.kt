package com.ptitsyn.playandthen

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.random.Random

/**
 * Hebrew Alphabet game - shows colorful Hebrew letters in cells.
 * Child must find the cell with the target letter.
 * Uses the first 10 letters of the Hebrew alphabet: א-י (Aleph-Yud)
 */
class AlphabetGame @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GridGame(context, attrs, defStyleAttr) {

    private var targetLetterIndex = 0
    private var correctCellIndex = 0
    
    // First 10 Hebrew letters (Aleph through Yud)
    private val hebrewLetters = listOf(
        "א", // Aleph
        "ב", // Bet
        "ג", // Gimel
        "ד", // Dalet
        "ה", // Heh
        "ו", // Vav
        "ז", // Zayin
        "ח", // Het
        "ט", // Tet
        "י"  // Yud
    )
    
    // Childish, bright colors for letters
    private val letterColors = listOf(
        "#FF69B4", // Hot Pink
        "#FF4500", // Orange Red
        "#32CD32", // Lime Green
        "#FF1493", // Deep Pink
        "#00BFFF", // Deep Sky Blue
        "#FFD700", // Gold
        "#FF6347", // Tomato
        "#9370DB", // Medium Purple
        "#00CED1", // Dark Turquoise
        "#FFA500"  // Orange
    )
    
    init {
        // Start game after all properties are initialized
        startGame()
    }
    
    override fun populateCells(containers: List<LinearLayout>) {
        // Generate random target letter index (0-9 for 10 letters)
        targetLetterIndex = Random.nextInt(0, 10)
        
        // Choose random cell for correct answer
        correctCellIndex = Random.nextInt(6)
        
        // Generate letter indices for each cell
        val letterIndices = generateLetterIndices()
        
        // Place letters in each cell
        letterIndices.forEachIndexed { index, letterIndex ->
            val letterTextView = createLetterTextView(hebrewLetters[letterIndex])
            containers[index].addView(letterTextView)
        }
    }
    
    private fun generateLetterIndices(): List<Int> {
        val indices = mutableListOf<Int>()
        
        // Place target letter in correct cell
        repeat(6) { index ->
            if (index == correctCellIndex) {
                indices.add(targetLetterIndex)
            } else {
                // Generate random letter index that's not equal to target
                var randomIndex: Int
                do {
                    randomIndex = Random.nextInt(0, 10)
                } while (randomIndex == targetLetterIndex || indices.contains(randomIndex))
                indices.add(randomIndex)
            }
        }
        
        return indices
    }
    
    private fun createLetterTextView(letter: String): TextView {
        return TextView(context).apply {
            text = letter
            textSize = 64f  // Large size for Hebrew letters
            setTextColor(android.graphics.Color.parseColor(letterColors.random()))
            gravity = android.view.Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            
            // Add some padding for better visual appearance
            setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
            
            // Set layout parameters to center in container
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
            
            // Add a subtle shadow for depth (childish effect)
            setShadowLayer(4f, 2f, 2f, android.graphics.Color.parseColor("#40000000"))
        }
    }
    
    override fun isCorrect(clickedCellIndex: Int): Boolean {
        return clickedCellIndex == correctCellIndex
    }
    
    override fun getInstructions(): GameInstructions {
        return GameInstructions(
            audioResourceId = R.raw.instructions_alphabet_mode,
            displayText = "Find the letter ${hebrewLetters[targetLetterIndex]}"
        )
    }
    
    override fun getHugeTextContent(): String? {
        // Alphabet mode doesn't show huge text (similar to numbers mode)
        return null
    }
    
    override fun getTargetSoundResourceId(): Int {
        // Map Hebrew letters to audio files
        // Audio files should be named: aleph.ogg, bet.ogg, gimel.ogg, etc.
        return when (targetLetterIndex) {
            0 -> R.raw.aleph
            1 -> R.raw.bet
            2 -> R.raw.gimel
            3 -> R.raw.dalet
            4 -> R.raw.heh
            5 -> R.raw.vav
            6 -> R.raw.zayin
            7 -> R.raw.het
            8 -> R.raw.tet
            9 -> R.raw.yud
            else -> R.raw.instructions_alphabet_mode // Fallback
        }
    }
}
