package com.ptitsyn.playandthen

import android.view.accessibility.AccessibilityNodeInfo
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Helper class for creating mock AccessibilityNodeInfo objects for testing.
 */
object TestNodeHelper {
    
    /**
     * Creates a mock AccessibilityNodeInfo with specified properties.
     */
    fun createMockNode(
        className: String? = null,
        contentDescription: String? = null,
        text: String? = null,
        isClickable: Boolean = false,
        isFocusable: Boolean = false,
        childCount: Int = 0,
        viewIdResourceName: String? = null
    ): AccessibilityNodeInfo {
        val mockNode = mock<AccessibilityNodeInfo>()
        
        // Set up className
        whenever(mockNode.className).thenReturn(className)
        
        // Set up contentDescription
        whenever(mockNode.contentDescription).thenReturn(contentDescription)
        
        // Set up text
        whenever(mockNode.text).thenReturn(text)
        
        // Set up clickable state
        whenever(mockNode.isClickable).thenReturn(isClickable)
        
        // Set up focusable state
        whenever(mockNode.isFocusable).thenReturn(isFocusable)
        
        // Set up child count
        whenever(mockNode.childCount).thenReturn(childCount)
        
        // Set up view ID resource name
        whenever(mockNode.viewIdResourceName).thenReturn(viewIdResourceName)
        
        return mockNode
    }
    
    /**
     * Creates a FrameLayout mock node with specified children.
     */
    fun createFrameLayoutWithChildren(vararg children: AccessibilityNodeInfo): AccessibilityNodeInfo {
        val frameLayout = createMockNode(
            className = "android.widget.FrameLayout",
            childCount = children.size
        )
        
        // Set up children
        children.forEachIndexed { index, child ->
            whenever(frameLayout.getChild(index)).thenReturn(child)
        }
        
        return frameLayout
    }
    
    /**
     * Creates a ViewGroup mock node with specified properties.
     */
    fun createViewGroup(
        contentDescription: String? = "test description",
        isClickable: Boolean = false,
        isFocusable: Boolean = true,
        viewIdResourceName: String? = null
    ): AccessibilityNodeInfo {
        return createMockNode(
            className = "android.view.ViewGroup",
            contentDescription = contentDescription,
            isClickable = isClickable,
            isFocusable = isFocusable,
            viewIdResourceName = viewIdResourceName
        )
    }
    
    /**
     * Creates an ImageView mock node with specified properties.
     */
    fun createImageView(
        contentDescription: String? = "test image",
        isClickable: Boolean = true,
        isFocusable: Boolean = true,
        viewIdResourceName: String? = null
    ): AccessibilityNodeInfo {
        return createMockNode(
            className = "android.widget.ImageView",
            contentDescription = contentDescription,
            isClickable = isClickable,
            isFocusable = isFocusable,
            viewIdResourceName = viewIdResourceName
        )
    }
    
    /**
     * Creates a TextView mock node with specified properties.
     */
    fun createTextView(
        text: String? = "test text",
        isClickable: Boolean = false,
        isFocusable: Boolean = false,
        viewIdResourceName: String? = null
    ): AccessibilityNodeInfo {
        return createMockNode(
            className = "android.widget.TextView",
            text = text,
            isClickable = isClickable,
            isFocusable = isFocusable,
            viewIdResourceName = viewIdResourceName
        )
    }
    
    /**
     * Creates a sequence of nodes matching the FullScreenVideoMatcher expected order with correct IDs.
     */
    fun createValidFullScreenSequence(): List<AccessibilityNodeInfo> {
        return listOf(
            createViewGroup(
                contentDescription = "video player", 
                isFocusable = true, 
                isClickable = false,
                viewIdResourceName = "com.google.android.apps.youtube.kids:id/watch_player"
            ),
            createImageView(
                contentDescription = "back button", 
                isClickable = true, 
                isFocusable = true,
                viewIdResourceName = "com.google.android.apps.youtube.kids:id/back_button"
            ),
            createTextView(
                text = "video title",
                isClickable = false,
                isFocusable = false,
                viewIdResourceName = "com.google.android.apps.youtube.kids:id/header_title"
            ),
            createImageView(
                contentDescription = "volume control", 
                isClickable = true, 
                isFocusable = true,
                viewIdResourceName = "com.google.android.apps.youtube.kids:id/next"
            ),
            createImageView(
                contentDescription = "more options", 
                isClickable = true, 
                isFocusable = true,
                viewIdResourceName = "com.google.android.apps.youtube.kids:id/pause"
            )
        )
    }
}
