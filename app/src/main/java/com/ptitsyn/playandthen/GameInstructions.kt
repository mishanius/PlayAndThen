package com.ptitsyn.playandthen

/**
 * Instructions for a game mode.
 * @param audioResourceId Resource ID for the instruction audio file
 * @param displayText Optional text to display (empty = no text display)
 */
data class GameInstructions(
    val audioResourceId: Int,
    val displayText: String = ""
)
