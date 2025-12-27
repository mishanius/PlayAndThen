package com.ptitsyn.playandthen

import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

/**
 * ButtonExtractor provides static functionality to find specific buttons
 * in the YouTube Kids accessibility tree using language-independent identifiers.
 */
object ButtonExtractor {
    
    private const val TAG = "ButtonExtractor"
    
    /**
     * Finds a button by its resource ID in the accessibility tree.
     * Uses language-independent identifiers (className, resource ID, clickable status).
     * 
     * @param root The root accessibility node to search from
     * @param buttonId The resource ID of the button to find (e.g., "com.google.android.apps.youtube.kids:id/pause")
     * @return The AccessibilityNodeInfo for the button if found, null otherwise
     */
    fun findButtonById(root: AccessibilityNodeInfo, buttonId: String): AccessibilityNodeInfo? {
        fun searchNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (node == null) return null
            
            // Check if this node is the target button (only using stable identifiers)
            val className = node.className?.toString()
            val id = node.viewIdResourceName
            
            if (className == "android.widget.ImageView" &&
                id == buttonId &&
                node.isClickable) {
                Log.d(TAG, "Found button: className=$className, id=$id")
                return node
            }
            
            // Recursively search children
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                val result = searchNode(child)
                if (result != null) {
                    return result
                }
            }
            
            return null
        }
        
        return searchNode(root)
    }
    
    /**
     * Finds the pause button in the accessibility tree.
     * 
     * @param root The root accessibility node to search from
     * @return The AccessibilityNodeInfo for the pause button if found, null otherwise
     */
    fun findPauseButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        return findButtonById(root, "com.google.android.apps.youtube.kids:id/pause")
    }
    
    /**
     * Finds the next button in the accessibility tree.
     * 
     * @param root The root accessibility node to search from
     * @return The AccessibilityNodeInfo for the next button if found, null otherwise
     */
    fun findNextButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        return findButtonById(root, "com.google.android.apps.youtube.kids:id/next")
    }
    
    /**
     * Finds the previous button in the accessibility tree.
     * 
     * @param root The root accessibility node to search from
     * @return The AccessibilityNodeInfo for the previous button if found, null otherwise
     */
    fun findPreviousButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        return findButtonById(root, "com.google.android.apps.youtube.kids:id/previous")
    }
    
    /**
     * Finds the back button (returns to main screen) in the accessibility tree.
     * 
     * @param root The root accessibility node to search from
     * @return The AccessibilityNodeInfo for the back button if found, null otherwise
     */
    fun findBackButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        return findButtonById(root, "com.google.android.apps.youtube.kids:id/back_button")
    }
}
