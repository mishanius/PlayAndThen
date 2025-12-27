package com.ptitsyn.playandthen

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for StrictFullScreenVideoMatcher.
 * Tests the exact matching logic requiring FrameLayout with single ViewGroup child.
 */
class StrictFullScreenVideoMatcherTest {

    @Test
    fun `should match valid FrameLayout with single focusable ViewGroup child with description and correct ID`() {
        // Arrange
        val viewGroupChild = TestNodeHelper.createViewGroup(
            contentDescription = "video player",
            isFocusable = true,
            viewIdResourceName = "com.google.android.apps.youtube.kids:id/watch_player"
        )
        val frameLayout = TestNodeHelper.createFrameLayoutWithChildren(viewGroupChild)

        // Act
        val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

        // Assert
        assertTrue("Should match valid FrameLayout with single ViewGroup child with correct ID", result)
    }

    @Test
    fun `should not match when root node is not FrameLayout`() {
        // Arrange
        val testCases = listOf(
            "android.widget.LinearLayout",
            "android.widget.RelativeLayout",
            "android.view.ViewGroup",
            "android.widget.Button",
            null
        )

        testCases.forEach { className ->
            // Arrange
            val viewGroupChild = TestNodeHelper.createViewGroup(
                contentDescription = "video player",
                isFocusable = true
            )
            val rootNode = TestNodeHelper.createMockNode(
                className = className,
                childCount = 1
            )
            // Mock the getChild call
            org.mockito.kotlin.whenever(rootNode.getChild(0)).thenReturn(viewGroupChild)

            // Act
            val result = StrictFullScreenVideoMatcher.isMatch(rootNode)

            // Assert
            assertFalse("Should not match when root is not FrameLayout: $className", result)
        }
    }

    @Test
    fun `should not match when FrameLayout has no children`() {
        // Arrange
        val frameLayout = TestNodeHelper.createFrameLayoutWithChildren()

        // Act
        val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

        // Assert
        assertFalse("Should not match when FrameLayout has no children", result)
    }

    @Test
    fun `should not match when FrameLayout has multiple children`() {
        // Arrange
        val child1 = TestNodeHelper.createViewGroup(
            contentDescription = "video player",
            isFocusable = true
        )
        val child2 = TestNodeHelper.createImageView()
        val frameLayout = TestNodeHelper.createFrameLayoutWithChildren(child1, child2)

        // Act
        val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

        // Assert
        assertFalse("Should not match when FrameLayout has multiple children", result)
    }

    @Test
    fun `should not match when single child is null`() {
        // Arrange
        val frameLayout = TestNodeHelper.createMockNode(
            className = "android.widget.FrameLayout",
            childCount = 1
        )
        // Mock getChild to return null
        org.mockito.kotlin.whenever(frameLayout.getChild(0)).thenReturn(null)

        // Act
        val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

        // Assert
        assertFalse("Should not match when single child is null", result)
    }

    @Test
    fun `should not match when child is not ViewGroup`() {
        // Arrange
        val testCases = listOf(
            "android.widget.Button",
            "android.widget.TextView",
            "android.widget.ImageView",
            "android.widget.FrameLayout",
            null
        )

        testCases.forEach { className ->
            // Arrange
            val child = TestNodeHelper.createMockNode(
                className = className,
                contentDescription = "video player",
                isFocusable = true
            )
            val frameLayout = TestNodeHelper.createFrameLayoutWithChildren(child)

            // Act
            val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

            // Assert
            assertFalse("Should not match when child is not ViewGroup: $className", result)
        }
    }

    @Test
    fun `should not match when ViewGroup child is not focusable`() {
        // Arrange
        val viewGroupChild = TestNodeHelper.createViewGroup(
            contentDescription = "video player",
            isFocusable = false  // Not focusable
        )
        val frameLayout = TestNodeHelper.createFrameLayoutWithChildren(viewGroupChild)

        // Act
        val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

        // Assert
        assertFalse("Should not match when ViewGroup child is not focusable", result)
    }

    @Test
    fun `should not match when ViewGroup child has no content description`() {
        // Arrange
        val viewGroupChild = TestNodeHelper.createViewGroup(
            contentDescription = null,  // No description
            isFocusable = true
        )
        val frameLayout = TestNodeHelper.createFrameLayoutWithChildren(viewGroupChild)

        // Act
        val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

        // Assert
        assertFalse("Should not match when ViewGroup child has no content description", result)
    }

    @Test
    fun `should not match when ViewGroup child has empty content description`() {
        // Arrange
        val viewGroupChild = TestNodeHelper.createViewGroup(
            contentDescription = "",  // Empty description
            isFocusable = true
        )
        val frameLayout = TestNodeHelper.createFrameLayoutWithChildren(viewGroupChild)

        // Act
        val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

        // Assert
        assertFalse("Should not match when ViewGroup child has empty content description", result)
    }

    @Test
    fun `should not match when ViewGroup child has blank content description`() {
        // Arrange
        val viewGroupChild = TestNodeHelper.createViewGroup(
            contentDescription = "   ",  // Blank description
            isFocusable = true
        )
        val frameLayout = TestNodeHelper.createFrameLayoutWithChildren(viewGroupChild)

        // Act
        val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

        // Assert
        assertFalse("Should not match when ViewGroup child has blank content description", result)
    }

    @Test
    fun `should match with any valid content description - language independent`() {
        // Arrange - Test different languages and descriptions
        val testCases = listOf(
            "video player",
            "regular video player",
            "reproducteur vidéo",  // French
            "reproductor de vídeo",  // Spanish
            "видео плеер",  // Russian
            "ビデオプレーヤー",  // Japanese
            "A",  // Single character
            "123",  // Numbers
            "!@#$%",  // Special characters
            "Very long description that contains many words and should still work"
        )

        testCases.forEach { description ->
            // Arrange
            val viewGroupChild = TestNodeHelper.createViewGroup(
                contentDescription = description,
                isFocusable = true,
                viewIdResourceName = "com.google.android.apps.youtube.kids:id/watch_player"  // Add correct ID
            )
            val frameLayout = TestNodeHelper.createFrameLayoutWithChildren(viewGroupChild)

            // Act
            val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

            // Assert
            assertTrue("Should match with any valid description: '$description'", result)
        }
    }

    @Test
    fun `should not be affected by ViewGroup child clickable state`() {
        // Arrange - Test that clickable state doesn't matter
        val testCases = listOf(true, false)

        testCases.forEach { isClickable ->
            // Arrange
            val viewGroupChild = TestNodeHelper.createViewGroup(
                contentDescription = "video player",
                isClickable = isClickable,
                isFocusable = true,
                viewIdResourceName = "com.google.android.apps.youtube.kids:id/watch_player"  // Add correct ID
            )
            val frameLayout = TestNodeHelper.createFrameLayoutWithChildren(viewGroupChild)

            // Act
            val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

            // Assert
            assertTrue("Should match regardless of clickable state: $isClickable", result)
        }
    }

    @Test
    fun `should not be affected by FrameLayout properties other than className and children`() {
        // Arrange
        val viewGroupChild = TestNodeHelper.createViewGroup(
            contentDescription = "video player",
            isFocusable = true,
            viewIdResourceName = "com.google.android.apps.youtube.kids:id/watch_player"  // Add correct ID
        )
        val frameLayout = TestNodeHelper.createMockNode(
            className = "android.widget.FrameLayout",
            contentDescription = "some description",  // This shouldn't matter
            text = "some text",  // This shouldn't matter
            isClickable = true,  // This shouldn't matter
            isFocusable = false,  // This shouldn't matter
            childCount = 1
        )
        // Mock the getChild call
        org.mockito.kotlin.whenever(frameLayout.getChild(0)).thenReturn(viewGroupChild)

        // Act
        val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

        // Assert
        assertTrue("Should match regardless of FrameLayout's other properties", result)
    }

    @Test
    fun `should handle FrameLayout class name with full package path`() {
        // Arrange
        val viewGroupChild = TestNodeHelper.createViewGroup(
            contentDescription = "video player",
            isFocusable = true,
            viewIdResourceName = "com.google.android.apps.youtube.kids:id/watch_player"  // Add correct ID
        )
        val frameLayout = TestNodeHelper.createMockNode(
            className = "android.widget.FrameLayout",  // Full package name
            childCount = 1
        )
        org.mockito.kotlin.whenever(frameLayout.getChild(0)).thenReturn(viewGroupChild)

        // Act
        val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

        // Assert
        assertTrue("Should handle FrameLayout with full package path", result)
    }

    @Test
    fun `should handle ViewGroup class name with full package path`() {
        // Arrange
        val viewGroupChild = TestNodeHelper.createMockNode(
            className = "android.view.ViewGroup",  // Full package name
            contentDescription = "video player",
            isFocusable = true,
            viewIdResourceName = "com.google.android.apps.youtube.kids:id/watch_player"
        )
        val frameLayout = TestNodeHelper.createFrameLayoutWithChildren(viewGroupChild)

        // Act
        val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

        // Assert
        assertTrue("Should handle ViewGroup with full package path", result)
    }

    @Test
    fun `should not match when ViewGroup child has no ID`() {
        // Arrange
        val viewGroupChild = TestNodeHelper.createViewGroup(
            contentDescription = "video player",
            isFocusable = true,
            viewIdResourceName = null  // No ID
        )
        val frameLayout = TestNodeHelper.createFrameLayoutWithChildren(viewGroupChild)

        // Act
        val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

        // Assert
        assertFalse("Should not match when ViewGroup child has no ID", result)
    }

    @Test
    fun `should not match when ViewGroup child has empty ID`() {
        // Arrange
        val viewGroupChild = TestNodeHelper.createViewGroup(
            contentDescription = "video player",
            isFocusable = true,
            viewIdResourceName = ""  // Empty ID
        )
        val frameLayout = TestNodeHelper.createFrameLayoutWithChildren(viewGroupChild)

        // Act
        val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

        // Assert
        assertFalse("Should not match when ViewGroup child has empty ID", result)
    }

    @Test
    fun `should not match when ViewGroup child has blank ID`() {
        // Arrange
        val viewGroupChild = TestNodeHelper.createViewGroup(
            contentDescription = "video player",
            isFocusable = true,
            viewIdResourceName = "   "  // Blank ID
        )
        val frameLayout = TestNodeHelper.createFrameLayoutWithChildren(viewGroupChild)

        // Act
        val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

        // Assert
        assertFalse("Should not match when ViewGroup child has blank ID", result)
    }

    @Test
    fun `should not match when ViewGroup child has wrong ID`() {
        // Arrange - IDs that don't end with "watch_player"
        val testCases = listOf(
            "com.google.android.apps.youtube.kids:id/wrong_player",
            "com.google.android.apps.youtube.kids:id/video_player", 
            "com.google.android.apps.youtube.kids:id/player",
            "watch_player_wrong",
            "wrong_watch_player",
            "just_watch_player"
        )

        testCases.forEach { wrongId ->
            // Arrange
            val viewGroupChild = TestNodeHelper.createViewGroup(
                contentDescription = "video player",
                isFocusable = true,
                viewIdResourceName = wrongId
            )
            val frameLayout = TestNodeHelper.createFrameLayoutWithChildren(viewGroupChild)

            // Act
            val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

            // Assert
            assertFalse("Should not match when ViewGroup child has wrong ID: '$wrongId'", result)
        }
    }

    @Test
    fun `should match when ViewGroup child has correct ID`() {
        val testCases = listOf(
            "com.google.android.apps.youtube.kids:id/watch_player"
        )

        testCases.forEach { validId ->
            // Arrange
            val viewGroupChild = TestNodeHelper.createViewGroup(
                contentDescription = "video player",
                isFocusable = true,
                viewIdResourceName = validId
            )
            val frameLayout = TestNodeHelper.createFrameLayoutWithChildren(viewGroupChild)

            // Act
            val result = StrictFullScreenVideoMatcher.isMatch(frameLayout)

            // Assert
            assertTrue("Should match when ViewGroup child has correct ID format: '$validId'", result)
        }
    }
}
