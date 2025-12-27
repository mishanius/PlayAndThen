package com.ptitsyn.playandthen

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.random.Random

/**
 * Numbers game - shows colorful numbers in cells.
 * Child must find the cell with the target number.
 */
class NumbersGame @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GridGame(context, attrs, defStyleAttr) {

    private var targetNumber = 0
    private var correctCellIndex = 0
    
    // Childish, bright colors for numbers
    private val numberColors = listOf(
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
        // Generate random target number (0-10)
        targetNumber = Random.nextInt(0, 11)
        
        // Choose random cell for correct answer
        correctCellIndex = Random.nextInt(6)
        
        // Generate counts for each cell
        val counts = generateCounts()
        
        // Place numbers in each cell
        counts.forEachIndexed { index, count ->
            val numberTextView = createNumberTextView(count)
            containers[index].addView(numberTextView)
        }
    }
    
    private fun generateCounts(): List<Int> {
        val counts = mutableListOf<Int>()
        
        // Place target number in correct cell
        repeat(6) { index ->
            if (index == correctCellIndex) {
                counts.add(targetNumber)
            } else {
                // Generate random count that's not equal to target number
                var randomCount: Int
                do {
                    randomCount = Random.nextInt(0, 11)
                } while (randomCount == targetNumber || counts.contains(randomCount))
                counts.add(randomCount)
            }
        }
        
        return counts
    }
    
    private fun createNumberTextView(number: Int): TextView {
        return TextView(context).apply {
            text = number.toString()
            textSize = 56f  // Large, childish size
            setTextColor(android.graphics.Color.parseColor(numberColors.random()))
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
            audioResourceId = R.raw.instructions_numbers_mode,
            displayText = "Find the number $targetNumber"
        )
    }
    
    override fun getHugeTextContent(): String? {
        // Numbers mode doesn't show huge text
        return null
    }
    
    override fun getTargetSoundResourceId(): Int {
        // Only play sound for numbers 0-10
        return when (targetNumber) {
            0 -> R.raw.zero
            1 -> R.raw.one
            2 -> R.raw.two
            3 -> R.raw.three
            4 -> R.raw.four
            5 -> R.raw.five
            6 -> R.raw.six
            7 -> R.raw.seven
            8 -> R.raw.eight
            9 -> R.raw.nine
            10 -> R.raw.ten
            else -> R.raw.instructions_numbers_mode // Fallback for 0
        }
    }
}
