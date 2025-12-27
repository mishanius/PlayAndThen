package com.ptitsyn.playandthen

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for MiniPlayerMatcher.
 * Tests the simple content description matching logic.
 */
class MiniPlayerMatcherTest {

    @Test
    fun `should match when content description contains minimized player`() {
        // Arrange
        val node = TestNodeHelper.createMockNode(
            contentDescription = "minimized player"
        )

        // Act
        val result = MiniPlayerMatcher.isMatch(node)

        // Assert
        assertTrue("Should match when content description contains 'minimized player'", result)
    }

    @Test
    fun `should match when content description contains minimized player with extra text`() {
        // Arrange
        val node = TestNodeHelper.createMockNode(
            contentDescription = "YouTube minimized player controls"
        )

        // Act
        val result = MiniPlayerMatcher.isMatch(node)

        // Assert
        assertTrue("Should match when content description contains 'minimized player' with extra text", result)
    }

    @Test
    fun `should match case insensitive`() {
        // Arrange
        val testCases = listOf(
            "MINIMIZED PLAYER",
            "Minimized Player",
            "minimized PLAYER",
            "MiNiMiZeD pLaYeR"
        )

        testCases.forEach { description ->
            // Arrange
            val node = TestNodeHelper.createMockNode(
                contentDescription = description
            )

            // Act
            val result = MiniPlayerMatcher.isMatch(node)

            // Assert
            assertTrue("Should match case insensitive for: '$description'", result)
        }
    }

    @Test
    fun `should not match when content description is null`() {
        // Arrange
        val node = TestNodeHelper.createMockNode(
            contentDescription = null
        )

        // Act
        val result = MiniPlayerMatcher.isMatch(node)

        // Assert
        assertFalse("Should not match when content description is null", result)
    }

    @Test
    fun `should not match when content description is empty`() {
        // Arrange
        val node = TestNodeHelper.createMockNode(
            contentDescription = ""
        )

        // Act
        val result = MiniPlayerMatcher.isMatch(node)

        // Assert
        assertFalse("Should not match when content description is empty", result)
    }

    @Test
    fun `should not match when content description is blank`() {
        // Arrange
        val node = TestNodeHelper.createMockNode(
            contentDescription = "   "
        )

        // Act
        val result = MiniPlayerMatcher.isMatch(node)

        // Assert
        assertFalse("Should not match when content description is blank", result)
    }

    @Test
    fun `should not match when content description does not contain minimized player`() {
        // Arrange
        val testCases = listOf(
            "video player",
            "maximized player",
            "player controls",
            "minimized video",
            "player minimized",  // wrong order
            "mini player",       // partial match
            "full screen player"
        )

        testCases.forEach { description ->
            // Arrange
            val node = TestNodeHelper.createMockNode(
                contentDescription = description
            )

            // Act
            val result = MiniPlayerMatcher.isMatch(node)

            // Assert
            assertFalse("Should not match for non-matching description: '$description'", result)
        }
    }

    @Test
    fun `should match with different languages containing minimized player in English`() {
        // Arrange - Testing mixed language content where English phrase is present
        val testCases = listOf(
            "Jugador minimized player español",
            "French minimized player français",
            "minimized player русский текст"
        )

        testCases.forEach { description ->
            // Arrange
            val node = TestNodeHelper.createMockNode(
                contentDescription = description
            )

            // Act
            val result = MiniPlayerMatcher.isMatch(node)

            // Assert
            assertTrue("Should match when 'minimized player' is present in mixed language: '$description'", result)
        }
    }

    @Test
    fun `should not be affected by other node properties`() {
        // Arrange - Test that other properties don't affect the matching
        val node = TestNodeHelper.createMockNode(
            className = "android.widget.Button",
            contentDescription = "minimized player",
            text = "Some text",
            isClickable = true,
            isFocusable = false,
            childCount = 5
        )

        // Act
        val result = MiniPlayerMatcher.isMatch(node)

        // Assert
        assertTrue("Should match regardless of other node properties", result)
    }
}
