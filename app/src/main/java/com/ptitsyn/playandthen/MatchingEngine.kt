package com.ptitsyn.playandthen

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Engine for efficiently matching multiple NodeMatchers against an accessibility tree.
 * Performs a single tree traversal and tests all matchers against each node.
 */
class MatchingEngine {
    

    fun walkTreeAndMatch(root: AccessibilityNodeInfo, matchers: List<NodeMatcher>): List<NodeMatcher?> {
        val matched = Array<NodeMatcher?>(matchers.size) { null }
        
        fun walkTree(node: AccessibilityNodeInfo?): Boolean {
            if (node == null) return false
            
            // Test each matcher against this node
            for (i in matchers.indices) {
                if (matched[i] == null && matchers[i].isMatch(node)) {
                    matched[i] = matchers[i]
                }
            }
            
            // Check if all matchers are now matched
            val allMatched = matched.all { it != null }
            if (allMatched) return true
            
            // Walk children
            for (i in 0 until node.childCount) {
                if (walkTree(node.getChild(i))) return true
            }
            
            return false
        }
        
        walkTree(root)
        return matched.toList()
    }
}
