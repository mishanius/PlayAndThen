package com.ptitsyn.playandthen

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class GuardService : AccessibilityService() {

    companion object {
        private const val TAG = "PlayAndThenGuard"
        private const val YT_PKG = "com.google.android.apps.youtube.kids"
        private const val SCAN_COOLDOWN_MS = 5L
    }

    private var lastScanAtMs = 0L
    
    // GuardManager handles all state management and interactions
    private lateinit var guardManager: GuardManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        
        // Initialize GuardManager with gesture dispatcher
        guardManager = GuardManager(this) { gesture, callback ->
            dispatchGesture(gesture, callback, null)
        }
        
        Log.d(TAG, "GuardService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg != YT_PKG) return

        // Only handle events that can affect UI state
        val t = event.eventType
        val relevant = (t == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                t == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                t == AccessibilityEvent.TYPE_VIEW_SCROLLED)
        if (!relevant) return

        // Debounce because YouTube is extremely noisy
        val now = System.currentTimeMillis()
        if (now - lastScanAtMs < SCAN_COOLDOWN_MS) return
        lastScanAtMs = now

        val root = rootInActiveWindow ?: return
        maybeDumpTree(root, "YT event=${event.eventType}")
        
        // Delegate all state management and logic to GuardManager
        guardManager.updateRoot(root)
    }

    override fun onInterrupt() {
        Log.d(TAG, "GuardService interrupted")
    }

    private fun dumpAccessibilityTree(
        root: AccessibilityNodeInfo,
        maxDepth: Int = 14,
        maxNodes: Int = 800,
        onlyInteresting: Boolean = false
    ): String {
        val sb = StringBuilder()
        var nodes = 0

        fun esc(s: String): String =
            s.replace("\n", "\\n").replace("\r", "\\r").take(220)

        fun isInteresting(n: AccessibilityNodeInfo): Boolean {
            val hasText = !n.text?.toString().isNullOrBlank()
            val hasDesc = !n.contentDescription?.toString().isNullOrBlank()
            val hasId = !n.viewIdResourceName.isNullOrBlank()
            return hasText || hasDesc || hasId
        }

        fun walk(n: AccessibilityNodeInfo?, depth: Int) {
            if (n == null) return
            if (depth > maxDepth) return
            if (nodes >= maxNodes) return
            nodes++
            if (!onlyInteresting || isInteresting(n)) {
                val indent = "  ".repeat(depth)

                val cls = (n.className?.toString() ?: "?").substringAfterLast('.')
                val id = n.viewIdResourceName ?: ""
                val text = n.text?.toString()?.trim().orEmpty()
                val desc = n.contentDescription?.toString()?.trim().orEmpty()
                Log.d("mishapttest", n.toString())
                sb.append(indent).append("• ").append(cls)

                if (id.isNotBlank()) sb.append("  id=").append(id)
                if (text.isNotBlank()) sb.append("  text=\"").append(esc(text)).append("\"")
                if (desc.isNotBlank()) sb.append("  desc=\"").append(esc(desc)).append("\"")

                if (n.isClickable) sb.append("  [clickable]")
                if (n.isScrollable) sb.append("  [scrollable]")
                if (n.isFocusable) sb.append("  [focusable]")
                if (n.isFocused) sb.append("  [focused]")
                if (n.isSelected) sb.append("  [selected]")
                if (n.isEnabled.not()) sb.append("  [disabled]")

                sb.append("\n")
            }

            for (i in 0 until n.childCount) {
                walk(n.getChild(i), depth + 1)
                if (nodes >= maxNodes) return
            }
        }

        walk(root, 0)
        sb.append("---- nodes dumped: ").append(nodes)
            .append(" (maxDepth=").append(maxDepth)
            .append(", maxNodes=").append(maxNodes)
            .append(", onlyInteresting=").append(onlyInteresting)
            .append(")\n")

        return sb.toString()
    }

    private fun logBig(tag: String, message: String) {
        val chunkSize = 3500
        var i = 0
        while (i < message.length) {
            val end = minOf(i + chunkSize, message.length)
            Log.d(tag, message.substring(i, end))
            Log.d(tag, "***************")
            i = end
        }
    }

    private var lastDumpAtMs = 0L

    private fun maybeDumpTree(root: AccessibilityNodeInfo, reason: String) {
        val now = System.currentTimeMillis()
        if (now - lastDumpAtMs < 1000) return // avoid spam
        lastDumpAtMs = now

        val dump = dumpAccessibilityTree(
            root = root,
            maxDepth = 160,
            maxNodes = 10000,
            onlyInteresting = false // start with true; set false for full dump
        )

        logBig("TreeDump", "Reason=$reason\n$dump")
    }


    override fun onDestroy() {
        super.onDestroy()
        // Clean up GuardManager when service is destroyed
        if (::guardManager.isInitialized) {
            guardManager.cleanup()
        }
        Log.d(TAG, "GuardService destroyed")
    }

}
