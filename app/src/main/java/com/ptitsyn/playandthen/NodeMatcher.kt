package com.ptitsyn.playandthen

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Abstract base class for matching specific UI patterns in accessibility trees.
 * Each NodeMatcher implementation should focus on detecting a specific UI state or pattern.
 */
abstract class NodeMatcher {
    
    /**
     * Determines if the given node matches this matcher's pattern.
     * @param node The AccessibilityNodeInfo to test
     * @return true if the pattern is found, false otherwise
     */
    abstract fun isMatch(node: AccessibilityNodeInfo): Boolean
}
