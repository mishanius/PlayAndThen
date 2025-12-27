package com.ptitsyn.playandthen

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Strict matcher for detecting full screen video player pattern.
 * 
 * Expected pattern (exact match - no additional children allowed):
 * • FrameLayout
 *    • ViewGroup  desc="<any description>" [focusable]
 *
 * This matcher performs an EXACT match - if there are any additional children
 * beyond the single required ViewGroup, the match will fail.
 * The description content is ignored for language independence, but a description must be present.
 */
object StrictFullScreenVideoMatcher : NodeMatcher() {
    
    override fun isMatch(node: AccessibilityNodeInfo): Boolean {
        // Must be a FrameLayout root
        if (!isFrameLayout(node)) return false
        
        // Must have exactly one child
        if (node.childCount != 1) return false
        
        // The single child must match our strict criteria
        val child = node.getChild(0) ?: return false
        
        return isStrictVideoPlayerViewGroup(child)
    }
    
    private fun isFrameLayout(node: AccessibilityNodeInfo): Boolean {
        return node.className?.toString()?.substringAfterLast('.') == "FrameLayout"
    }
    
    private fun isStrictVideoPlayerViewGroup(node: AccessibilityNodeInfo): Boolean {
        // Must be a ViewGroup
        if (node.className?.toString()?.substringAfterLast('.') != "ViewGroup") {
            return false
        }
        
        // Must be focusable
        if (!node.isFocusable) {
            return false
        }
        
        // Must have a content description (any description, language-independent)
        val description = node.contentDescription?.toString()
        if (description.isNullOrBlank()) {
            return false
        }

        // Must havespecific id 
        val id = node.viewIdResourceName
        if (id.isNullOrBlank() || id != "com.google.android.apps.youtube.kids:id/watch_player") {
            return false
        }
        return true
    }
}
