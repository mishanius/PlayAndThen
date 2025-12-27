package com.ptitsyn.playandthen

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for FullScreenVideoMatcher.
 * Tests the complex order-based matching logic using ElementCriteria.
 */
class FullScreenVideoMatcherTest {

    @Test
    fun `should match exact valid sequence in order with correct IDs`() {
        // Arrange - Create exact sequence matching expected order with correct IDs
        val children = TestNodeHelper.createValidFullScreenSequence()
        val parentNode = createParentNodeWithChildren(*children.toTypedArray())

        // Act
        val result = FullScreenVideoMatcher.isMatch(parentNode)

        // Assert
        assertTrue("Should match exact valid sequence with correct IDs", result)
    }

    @Test
    fun `should match valid sequence with extra elements in between`() {
        // Arrange - Add extra elements between expected ones, but with correct IDs for matching elements
        val validSequence = TestNodeHelper.createValidFullScreenSequence()
        val children = listOf(
            validSequence[0], // ViewGroup with correct ID
            TestNodeHelper.createMockNode(className = "android.widget.Button"), // Extra element
            validSequence[1], // ImageView with correct ID
            TestNodeHelper.createMockNode(className = "android.widget.LinearLayout"), // Extra element
            validSequence[2], // TextView with correct ID
            TestNodeHelper.createMockNode(className = "android.widget.Space"), // Extra element
            validSequence[3], // ImageView with correct ID
            TestNodeHelper.createMockNode(className = "android.widget.View"), // Extra element
            validSequence[4] // ImageView with correct ID
        )
        val parentNode = createParentNodeWithChildren(*children.toTypedArray())

        // Act
        val result = FullScreenVideoMatcher.isMatch(parentNode)

        // Assert
        assertTrue("Should match valid sequence with extra elements in between", result)
    }

    @Test
    fun `should not match when elements are in wrong order`() {
        // Arrange - Swap order of ImageView and TextView
        val children = listOf(
            TestNodeHelper.createViewGroup(contentDescription = "video player", isClickable = false, isFocusable = true),
            TestNodeHelper.createTextView(text = "video title"), // Should come after ImageView
            TestNodeHelper.createImageView(contentDescription = "back button", isClickable = true, isFocusable = true), // Should come before TextView
            TestNodeHelper.createImageView(contentDescription = "volume control", isClickable = true, isFocusable = true),
            TestNodeHelper.createImageView(contentDescription = "more options", isClickable = true, isFocusable = true)
        )
        val parentNode = createParentNodeWithChildren(*children.toTypedArray())

        // Act
        val result = FullScreenVideoMatcher.isMatch(parentNode)

        // Assert
        assertFalse("Should not match when elements are in wrong order", result)
    }

    @Test
    fun `should not match when missing required elements`() {
        // Test cases with different missing elements
        val testCases = listOf(
            // Missing ViewGroup (first element)
            listOf(
                TestNodeHelper.createImageView(contentDescription = "back button", isClickable = true, isFocusable = true),
                TestNodeHelper.createTextView(text = "video title"),
                TestNodeHelper.createImageView(contentDescription = "volume control", isClickable = true, isFocusable = true),
                TestNodeHelper.createImageView(contentDescription = "more options", isClickable = true, isFocusable = true)
            ),
            // Missing first ImageView
            listOf(
                TestNodeHelper.createViewGroup(contentDescription = "video player", isClickable = false, isFocusable = true),
                TestNodeHelper.createTextView(text = "video title"),
                TestNodeHelper.createImageView(contentDescription = "volume control", isClickable = true, isFocusable = true),
                TestNodeHelper.createImageView(contentDescription = "more options", isClickable = true, isFocusable = true)
            ),
            // Missing TextView
            listOf(
                TestNodeHelper.createViewGroup(contentDescription = "video player", isClickable = false, isFocusable = true),
                TestNodeHelper.createImageView(contentDescription = "back button", isClickable = true, isFocusable = true),
                TestNodeHelper.createImageView(contentDescription = "volume control", isClickable = true, isFocusable = true),
                TestNodeHelper.createImageView(contentDescription = "more options", isClickable = true, isFocusable = true)
            ),
            // Missing last ImageView
            listOf(
                TestNodeHelper.createViewGroup(contentDescription = "video player", isClickable = false, isFocusable = true),
                TestNodeHelper.createImageView(contentDescription = "back button", isClickable = true, isFocusable = true),
                TestNodeHelper.createTextView(text = "video title"),
                TestNodeHelper.createImageView(contentDescription = "volume control", isClickable = true, isFocusable = true)
            )
        )

        testCases.forEachIndexed { index, children ->
            // Arrange
            val parentNode = createParentNodeWithChildren(*children.toTypedArray())

            // Act
            val result = FullScreenVideoMatcher.isMatch(parentNode)

            // Assert
            assertFalse("Should not match when missing elements - test case $index", result)
        }
    }

    @Test
    fun `should not match when ViewGroup has wrong properties`() {
        // Test cases with ViewGroup having wrong properties
        val testCases = listOf(
            // ViewGroup without description
            TestNodeHelper.createViewGroup(contentDescription = null, isClickable = false, isFocusable = true),
            // ViewGroup with empty description
            TestNodeHelper.createViewGroup(contentDescription = "", isClickable = false, isFocusable = true),
            // ViewGroup that's clickable (should be false)
            TestNodeHelper.createViewGroup(contentDescription = "video player", isClickable = true, isFocusable = true),
            // ViewGroup that's not focusable (should be true)
            TestNodeHelper.createViewGroup(contentDescription = "video player", isClickable = false, isFocusable = false)
        )

        testCases.forEachIndexed { index, wrongViewGroup ->
            // Arrange
            val children = listOf(
                wrongViewGroup, // Wrong ViewGroup
                TestNodeHelper.createImageView(contentDescription = "back button", isClickable = true, isFocusable = true),
                TestNodeHelper.createTextView(text = "video title"),
                TestNodeHelper.createImageView(contentDescription = "volume control", isClickable = true, isFocusable = true),
                TestNodeHelper.createImageView(contentDescription = "more options", isClickable = true, isFocusable = true)
            )
            val parentNode = createParentNodeWithChildren(*children.toTypedArray())

            // Act
            val result = FullScreenVideoMatcher.isMatch(parentNode)

            // Assert
            assertFalse("Should not match when ViewGroup has wrong properties - test case $index", result)
        }
    }

    @Test
    fun `should not match when ImageView has wrong properties`() {
        // Test cases with ImageView having wrong properties
        val testCases = listOf(
            // ImageView without description
            TestNodeHelper.createImageView(contentDescription = null, isClickable = true, isFocusable = true),
            // ImageView with empty description
            TestNodeHelper.createImageView(contentDescription = "", isClickable = true, isFocusable = true),
            // ImageView that's not clickable (should be true)
            TestNodeHelper.createImageView(contentDescription = "back button", isClickable = false, isFocusable = true),
            // ImageView that's not focusable (should be true)
            TestNodeHelper.createImageView(contentDescription = "back button", isClickable = true, isFocusable = false)
        )

        testCases.forEachIndexed { index, wrongImageView ->
            // Arrange
            val children = listOf(
                TestNodeHelper.createViewGroup(contentDescription = "video player", isClickable = false, isFocusable = true),
                wrongImageView, // Wrong ImageView
                TestNodeHelper.createTextView(text = "video title"),
                TestNodeHelper.createImageView(contentDescription = "volume control", isClickable = true, isFocusable = true),
                TestNodeHelper.createImageView(contentDescription = "more options", isClickable = true, isFocusable = true)
            )
            val parentNode = createParentNodeWithChildren(*children.toTypedArray())

            // Act
            val result = FullScreenVideoMatcher.isMatch(parentNode)

            // Assert
            assertFalse("Should not match when ImageView has wrong properties - test case $index", result)
        }
    }

    @Test
    fun `should not match when TextView has wrong properties`() {
        // Test cases with TextView having wrong properties  
        val testCases = listOf(
            // TextView that's clickable (should be false)
            TestNodeHelper.createTextView(text = "video title", isClickable = true),
            // TextView that's focusable (should be false)  
            TestNodeHelper.createTextView(text = "video title", isFocusable = true)
        )

        testCases.forEachIndexed { index, wrongTextView ->
            // Arrange
            val children = listOf(
                TestNodeHelper.createViewGroup(contentDescription = "video player", isClickable = false, isFocusable = true),
                TestNodeHelper.createImageView(contentDescription = "back button", isClickable = true, isFocusable = true),
                wrongTextView, // Wrong TextView
                TestNodeHelper.createImageView(contentDescription = "volume control", isClickable = true, isFocusable = true),
                TestNodeHelper.createImageView(contentDescription = "more options", isClickable = true, isFocusable = true)
            )
            val parentNode = createParentNodeWithChildren(*children.toTypedArray())

            // Act
            val result = FullScreenVideoMatcher.isMatch(parentNode)

            // Assert
            assertFalse("Should not match when TextView has wrong properties - test case $index", result)
        }
    }

    @Test
    fun `should handle empty parent node`() {
        // Arrange
        val parentNode = createParentNodeWithChildren()

        // Act
        val result = FullScreenVideoMatcher.isMatch(parentNode)

        // Assert
        assertFalse("Should not match empty parent node", result)
    }

    @Test
    fun `should handle parent node with null children`() {
        // Arrange
        val parentNode = TestNodeHelper.createMockNode(childCount = 2)
        org.mockito.kotlin.whenever(parentNode.getChild(0)).thenReturn(null)
        org.mockito.kotlin.whenever(parentNode.getChild(1)).thenReturn(null)

        // Act
        val result = FullScreenVideoMatcher.isMatch(parentNode)

        // Assert
        assertFalse("Should not match when children are null", result)
    }

    @Test
    fun `should handle partial valid sequence followed by invalid elements`() {
        // Arrange - Start with valid sequence but then have invalid elements
        val children = listOf(
            TestNodeHelper.createViewGroup(contentDescription = "video player", isClickable = false, isFocusable = true),
            TestNodeHelper.createImageView(contentDescription = "back button", isClickable = true, isFocusable = true),
            TestNodeHelper.createTextView(text = "video title"),
            // Missing the last two ImageViews, should fail
            TestNodeHelper.createMockNode(className = "android.widget.Button")
        )
        val parentNode = createParentNodeWithChildren(*children.toTypedArray())

        // Act
        val result = FullScreenVideoMatcher.isMatch(parentNode)

        // Assert
        assertFalse("Should not match partial valid sequence", result)
    }

    @Test
    fun `should match when valid elements are at the end of many invalid elements`() {
        // Arrange - Start with many invalid elements, then valid sequence with correct IDs
        val validSequence = TestNodeHelper.createValidFullScreenSequence()
        val children = listOf(
            TestNodeHelper.createMockNode(className = "android.widget.Button"),
            TestNodeHelper.createMockNode(className = "android.widget.LinearLayout"),
            TestNodeHelper.createMockNode(className = "android.widget.Space")
        ) + validSequence // Add the valid sequence with correct IDs
        
        val parentNode = createParentNodeWithChildren(*children.toTypedArray())

        // Act
        val result = FullScreenVideoMatcher.isMatch(parentNode)

        // Assert
        assertTrue("Should match when valid elements come after invalid elements", result)
    }

    @Test
    fun `should test ElementCriteria matching logic directly`() {
        // Arrange - Test individual element criteria
        val viewGroupNode = TestNodeHelper.createViewGroup(
            contentDescription = "test description",
            isClickable = false,
            isFocusable = true
        )
        val parentNode = createParentNodeWithChildren(viewGroupNode)

        // Act
        val result = FullScreenVideoMatcher.isMatch(parentNode)

        // Assert - Should fail because we only have 1 out of 5 required elements
        assertFalse("Should not match with only one matching element", result)
    }

    @Test
    fun `should handle nodes with wrong class names`() {
        // Arrange - Use correct properties but wrong class names
        val children = listOf(
            TestNodeHelper.createMockNode(className = "android.widget.Button", contentDescription = "video player", isClickable = false, isFocusable = true), // Should be ViewGroup
            TestNodeHelper.createMockNode(className = "android.widget.TextView", contentDescription = "back button", isClickable = true, isFocusable = true), // Should be ImageView
            TestNodeHelper.createMockNode(className = "android.widget.ImageView", text = "video title", isClickable = false, isFocusable = false), // Should be TextView
            TestNodeHelper.createMockNode(className = "android.widget.Button", contentDescription = "volume control", isClickable = true, isFocusable = true), // Should be ImageView
            TestNodeHelper.createMockNode(className = "android.widget.LinearLayout", contentDescription = "more options", isClickable = true, isFocusable = true) // Should be ImageView
        )
        val parentNode = createParentNodeWithChildren(*children.toTypedArray())

        // Act
        val result = FullScreenVideoMatcher.isMatch(parentNode)

        // Assert
        assertFalse("Should not match when class names are wrong", result)
    }

    @Test
    fun `should not match when elements have wrong IDs`() {
        // Test each element with wrong ID
        val testCases = listOf(
            // Wrong ViewGroup ID
            listOf(
                TestNodeHelper.createViewGroup(
                    contentDescription = "video player", 
                    isClickable = false, 
                    isFocusable = true,
                    viewIdResourceName = "wrong_player" // Wrong ID
                ),
                TestNodeHelper.createImageView(
                    contentDescription = "back button", 
                    isClickable = true, 
                    isFocusable = true,
                    viewIdResourceName = "back_button"
                ),
                TestNodeHelper.createTextView(
                    text = "video title",
                    isClickable = false,
                    isFocusable = false,
                    viewIdResourceName = "header_title"
                ),
                TestNodeHelper.createImageView(
                    contentDescription = "volume control", 
                    isClickable = true, 
                    isFocusable = true,
                    viewIdResourceName = "next"
                ),
                TestNodeHelper.createImageView(
                    contentDescription = "more options", 
                    isClickable = true, 
                    isFocusable = true,
                    viewIdResourceName = "pause"
                )
            ),
            // Wrong first ImageView ID
            listOf(
                TestNodeHelper.createViewGroup(
                    contentDescription = "video player", 
                    isClickable = false, 
                    isFocusable = true,
                    viewIdResourceName = "watch_player"
                ),
                TestNodeHelper.createImageView(
                    contentDescription = "back button", 
                    isClickable = true, 
                    isFocusable = true,
                    viewIdResourceName = "wrong_button" // Wrong ID
                ),
                TestNodeHelper.createTextView(
                    text = "video title",
                    isClickable = false,
                    isFocusable = false,
                    viewIdResourceName = "header_title"
                ),
                TestNodeHelper.createImageView(
                    contentDescription = "volume control", 
                    isClickable = true, 
                    isFocusable = true,
                    viewIdResourceName = "next"
                ),
                TestNodeHelper.createImageView(
                    contentDescription = "more options", 
                    isClickable = true, 
                    isFocusable = true,
                    viewIdResourceName = "pause"
                )
            ),
            // Wrong TextView ID
            listOf(
                TestNodeHelper.createViewGroup(
                    contentDescription = "video player", 
                    isClickable = false, 
                    isFocusable = true,
                    viewIdResourceName = "watch_player"
                ),
                TestNodeHelper.createImageView(
                    contentDescription = "back button", 
                    isClickable = true, 
                    isFocusable = true,
                    viewIdResourceName = "back_button"
                ),
                TestNodeHelper.createTextView(
                    text = "video title",
                    isClickable = false,
                    isFocusable = false,
                    viewIdResourceName = "wrong_title" // Wrong ID
                ),
                TestNodeHelper.createImageView(
                    contentDescription = "volume control", 
                    isClickable = true, 
                    isFocusable = true,
                    viewIdResourceName = "next"
                ),
                TestNodeHelper.createImageView(
                    contentDescription = "more options", 
                    isClickable = true, 
                    isFocusable = true,
                    viewIdResourceName = "pause"
                )
            )
        )

        testCases.forEachIndexed { index, children ->
            // Arrange
            val parentNode = createParentNodeWithChildren(*children.toTypedArray())

            // Act
            val result = FullScreenVideoMatcher.isMatch(parentNode)

            // Assert
            assertFalse("Should not match when element has wrong ID - test case $index", result)
        }
    }

    @Test
    fun `should not match when elements have no IDs`() {
        // Arrange - Create elements without IDs
        val children = listOf(
            TestNodeHelper.createViewGroup(
                contentDescription = "video player", 
                isClickable = false, 
                isFocusable = true,
                viewIdResourceName = null // No ID
            ),
            TestNodeHelper.createImageView(
                contentDescription = "back button", 
                isClickable = true, 
                isFocusable = true,
                viewIdResourceName = null // No ID
            ),
            TestNodeHelper.createTextView(
                text = "video title",
                isClickable = false,
                isFocusable = false,
                viewIdResourceName = null // No ID
            ),
            TestNodeHelper.createImageView(
                contentDescription = "volume control", 
                isClickable = true, 
                isFocusable = true,
                viewIdResourceName = null // No ID
            ),
            TestNodeHelper.createImageView(
                contentDescription = "more options", 
                isClickable = true, 
                isFocusable = true,
                viewIdResourceName = null // No ID
            )
        )
        val parentNode = createParentNodeWithChildren(*children.toTypedArray())

        // Act
        val result = FullScreenVideoMatcher.isMatch(parentNode)

        // Assert
        assertFalse("Should not match when elements have no IDs", result)
    }

    @Test
    fun `should match valid sequence with extra elements that have correct IDs in between`() {
        // Arrange - Add extra elements between expected ones, but use valid IDs for matching elements
        val validSequence = TestNodeHelper.createValidFullScreenSequence()
        val children = listOf(
            validSequence[0], // ViewGroup with watch_player
            TestNodeHelper.createMockNode(className = "android.widget.Button"), // Extra element
            validSequence[1], // ImageView with back_button
            TestNodeHelper.createMockNode(className = "android.widget.LinearLayout"), // Extra element
            validSequence[2], // TextView with header_title
            TestNodeHelper.createMockNode(className = "android.widget.Space"), // Extra element
            validSequence[3], // ImageView with next
            TestNodeHelper.createMockNode(className = "android.widget.View"), // Extra element
            validSequence[4] // ImageView with pause
        )
        val parentNode = createParentNodeWithChildren(*children.toTypedArray())

        // Act
        val result = FullScreenVideoMatcher.isMatch(parentNode)

        // Assert
        assertTrue("Should match valid sequence with correct IDs and extra elements in between", result)
    }

    /**
     * Helper function to create a parent node with specified children.
     */
    private fun createParentNodeWithChildren(vararg children: android.view.accessibility.AccessibilityNodeInfo): android.view.accessibility.AccessibilityNodeInfo {
        val parentNode = TestNodeHelper.createMockNode(childCount = children.size)
        
        children.forEachIndexed { index, child ->
            org.mockito.kotlin.whenever(parentNode.getChild(index)).thenReturn(child)
        }
        
        return parentNode
    }
}
