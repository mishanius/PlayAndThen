package com.ptitsyn.playandthen

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Data class representing element criteria for order checking
 */
data class ElementCriteria(
    val className: String,
    val hasDescription: Boolean,
    val isClickable: Boolean,
    val isFocusable: Boolean,
    val id: String
)

/**
 * Simple order-based matcher that checks if elements appear in specified order
 * (allowing additional elements in between)
 */
object FullScreenVideoMatcher : NodeMatcher() {
    
    override fun isMatch(node: AccessibilityNodeInfo): Boolean {
        // Define the expected order of elements
        val expectedOrder = listOf(
            ElementCriteria("ViewGroup", hasDescription = true, isClickable = false, isFocusable = true, id="com.google.android.apps.youtube.kids:id/watch_player"),
            ElementCriteria("ImageView", hasDescription = true, isClickable = true, isFocusable = true, id="com.google.android.apps.youtube.kids:id/back_button"),
            ElementCriteria("TextView", hasDescription = false, isClickable = false, isFocusable = false, id="com.google.android.apps.youtube.kids:id/header_title"),
            ElementCriteria("ImageView", hasDescription = true, isClickable = true, isFocusable = true, id="com.google.android.apps.youtube.kids:id/next"),
            ElementCriteria("ImageView", hasDescription = true, isClickable = true, isFocusable = true, id="com.google.android.apps.youtube.kids:id/pause")
        )
        
        return checkElementsInOrder(node, expectedOrder)
    }
    
    /**
     * Checks if elements appear in the specified order within the node's children.
     * Additional elements in between are allowed, but the order must be maintained.
     * 
     * @param node The parent node to check
     * @param expectedOrder List of element criteria in expected order
     * @return true if all elements are found in the correct order
     */
    private fun checkElementsInOrder(node: AccessibilityNodeInfo, expectedOrder: List<ElementCriteria>): Boolean {
        var patternIndex = 0
        
        // Iterate through all children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            
            // If we've matched all expected elements, we're done
            if (patternIndex >= expectedOrder.size) break
            
            // Check if current child matches the next expected element
            if (matchesElementCriteria(child, expectedOrder[patternIndex])) {
                patternIndex++
            }
        }
        
        // Return true if we found all expected elements in order
        return patternIndex == expectedOrder.size
    }
    
    /**
     * Checks if a node matches the specified element criteria
     */
    private fun matchesElementCriteria(node: AccessibilityNodeInfo, criteria: ElementCriteria): Boolean {
        // Check class name
        val nodeClassName = node.className?.toString()?.substringAfterLast('.') ?: return false
        if (nodeClassName != criteria.className) return false
        
        // Check description presence
        if (criteria.hasDescription) {
            if (node.contentDescription?.toString().isNullOrBlank()) return false
        }
        
        // Check clickable state
        if (criteria.isClickable != node.isClickable) return false
        
        // Check focusable state
        if (criteria.isFocusable != node.isFocusable) return false
        // 
        if (criteria.id != node.viewIdResourceName) return false
        
        return true
    }
}
