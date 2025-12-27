package com.ptitsyn.playandthen

import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Extracts video titles from accessibility trees by walking the tree and analyzing nodes.
 */
object TitleExtractor {
    
    /**
     * Extracts video title from the accessibility tree starting from the root node.
     * @param root The root AccessibilityNodeInfo to start searching from
     * @return The extracted video title or null if no title is found
     */
    fun extractTitle(root: AccessibilityNodeInfo): String? {
        return walkTreeForTitle(root)
    }
    
    /**
     * Recursively walks the accessibility tree looking for video title patterns.
     */
    private fun walkTreeForTitle(node: AccessibilityNodeInfo?): String? {
        if (node == null) return null
        
        // Try to extract title from current node
        val title = extractVideoTitleFromNode(node)
        if (title != null) return title
        
        // Walk children recursively
        for (i in 0 until node.childCount) {
            val childTitle = walkTreeForTitle(node.getChild(i))
            if (childTitle != null) return childTitle
        }
        
        return null
    }
    
    /**
     * Attempts to extract video title from a specific node using the RecyclerView pattern.
     * Based on the logic from GuardService.extractVideoTitleFromNode.
     */
    fun extractVideoTitleFromNode(node: AccessibilityNodeInfo?): String? {
        if (node == null) return null
        // Check if this is a RecyclerView
        val className = node.className?.toString() ?: return null
        var title: String? = null
        if (!className.contains("FrameLayout", ignoreCase = true)) return null
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val childClass = child?.className?.toString()
            if (child?.className?.toString()?.contains("TextView", ignoreCase = true) == true && child?.viewIdResourceName == "com.google.android.apps.youtube.kids:id/header_title") {
                title = child.text?.toString()?.trim()
                Log.d("titleExtract", "child text: $title")
                return title
            }
        }
        return null
    }
}
