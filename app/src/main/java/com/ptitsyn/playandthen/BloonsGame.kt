package com.ptitsyn.playandthen

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.random.Random

/**
 * Balloons game - shows colorful balloons in cells.
 * Child must find the cell with the target number of balloons.
 */
class BloonsGame @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GridGame(context, attrs, defStyleAttr) {

    private var targetNumber = 0
    private var correctCellIndex = 0
    
    // Balloon colors for variety
    private val balloonColors = listOf(
        "#FF6B6B", // Red
        "#4ECDC4", // Teal  
        "#45B7D1", // Blue
        "#96CEB4", // Green
        "#FFEAA7", // Yellow
        "#DDA0DD", // Plum
        "#98D8C8", // Mint
        "#F7DC6F"  // Light Yellow
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
        
        // Place balloons in each cell
        counts.forEachIndexed { index, count ->
            placeBalloons(containers[index], count)
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
    
    private fun placeBalloons(container: LinearLayout, balloonCount: Int) {
        if (balloonCount == 0) {
            // Show "0" text for zero balloons
            val zeroText = TextView(context).apply {
                text = "0"
                textSize = 24f
                setTextColor(resources.getColor(android.R.color.darker_gray, context.theme))
                gravity = android.view.Gravity.CENTER
            }
            container.addView(zeroText)
            return
        }
        
        // Create balloons based on count
        when {
            balloonCount <= 3 -> {
                // Single row for 1-3 balloons
                val row = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER
                }
                repeat(balloonCount) {
                    row.addView(createBalloonImageView())
                }
                container.addView(row)
            }
            balloonCount <= 6 -> {
                // Two rows for 4-6 balloons
                val topRow = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER
                }
                val bottomRow = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER
                }
                
                val topCount = balloonCount / 2
                val bottomCount = balloonCount - topCount
                
                repeat(topCount) { topRow.addView(createBalloonImageView()) }
                repeat(bottomCount) { bottomRow.addView(createBalloonImageView()) }
                
                container.addView(topRow)
                container.addView(bottomRow)
            }
            else -> {
                // Three rows for 7+ balloons
                val rows = listOf(
                    LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = android.view.Gravity.CENTER
                    },
                    LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = android.view.Gravity.CENTER
                    },
                    LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = android.view.Gravity.CENTER
                    }
                )
                
                val balloonsPerRow = balloonCount / 3
                val remainder = balloonCount % 3
                
                rows.forEachIndexed { index, row ->
                    val balloonsInThisRow = balloonsPerRow + if (index < remainder) 1 else 0
                    repeat(balloonsInThisRow) { row.addView(createBalloonImageView()) }
                    container.addView(row)
                }
            }
        }
    }
    
    private fun createBalloonImageView(): ImageView {
        return ImageView(context).apply {
            setImageResource(R.drawable.balloon)
            layoutParams = LinearLayout.LayoutParams(32.dpToPx(), 42.dpToPx()).apply {
                setMargins(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
            }
            // Random color tint for variety
            val randomColor = balloonColors.random()
            setColorFilter(android.graphics.Color.parseColor(randomColor))
        }
    }
    
    override fun isCorrect(clickedCellIndex: Int): Boolean {
        return clickedCellIndex == correctCellIndex
    }
    
    override fun getInstructions(): GameInstructions {
        return GameInstructions(
            audioResourceId = R.raw.instructions_baloons_mode,
            displayText = "Find $targetNumber balloons"
        )
    }
    
    override fun getHugeTextContent(): String? {
        // Balloons mode shows the target number as huge text
        return targetNumber.toString()
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
            else -> R.raw.instructions_baloons_mode // Fallback
        }
    }
}
