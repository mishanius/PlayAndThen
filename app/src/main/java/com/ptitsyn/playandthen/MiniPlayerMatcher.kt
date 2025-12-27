package com.ptitsyn.playandthen

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Matcher for detecting YouTube Kids mini player pattern.
 * Looks for elements with content description containing "minimized player".
 */
object MiniPlayerMatcher : NodeMatcher() {
    
    override fun isMatch(node: AccessibilityNodeInfo): Boolean {
        val contentDesc = node.contentDescription?.toString()?.lowercase()
        return contentDesc?.contains("minimized player") == true
    }
}
