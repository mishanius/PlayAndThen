package com.ptitsyn.playandthen

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.constraintlayout.widget.ConstraintLayout
import org.json.JSONObject

/**
 * WebView-based grid game implementation using TypeScript/JavaScript.
 * Parallel to GridGame.kt but renders games using HTML/CSS/JS.
 */
class GridGameJs @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val currentRound: Int = 1,
    private val totalRounds: Int = 1,
    private val gameType: String = "numbers"  // "numbers" or "match-words"
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "GridGameJs"
    }

    private lateinit var webView: WebView
    
    // Game completion callback
    var onGameCompleted: (() -> Unit)? = null

    init {
        initView()
    }

    private fun initView() {
        // Create and configure WebView
        webView = WebView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )

            // Enable JavaScript
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.mediaPlaybackRequiresUserGesture = false  // Allow autoplay

            // Enable remote debugging (useful for development)
            WebView.setWebContentsDebuggingEnabled(true)

            // Set WebView client
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d(TAG, "Page loaded: $url")
                    
                    // Initialize the game after page loads
                    initializeGame()
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    Log.e(TAG, "WebView error: $description")
                    super.onReceivedError(view, errorCode, description, failingUrl)
                }
            }

            // Set Chrome client for console logging
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    consoleMessage?.let {
                        Log.d(TAG, "JS Console [${it.messageLevel()}]: ${it.message()} " +
                                "(${it.sourceId()}:${it.lineNumber()})")
                    }
                    return true
                }
            }

            // Add JavaScript bridge
            addJavascriptInterface(AndroidBridge(), "Android")
        }

        addView(webView)

        // Load the game HTML based on game type
        val gameUrl = "file:///android_asset/games/$gameType/dist/index.html"
        Log.d(TAG, "Loading game: $gameType from $gameUrl")
        webView.loadUrl(gameUrl)
    }

    /**
     * Initialize the game by calling JavaScript initGame function
     */
    private fun initializeGame() {
        val config = JSONObject().apply {
            put("currentRound", currentRound)
            put("totalRounds", totalRounds)
        }

        val jsCode = "window.initGame($config);"
        Log.d(TAG, "Initializing game with config: $config")
        
        webView.evaluateJavascript(jsCode) { result ->
            Log.d(TAG, "initGame result: $result")
        }
    }

    /**
     * Reset the game by calling JavaScript resetGame function
     */
    fun resetGame() {
        Log.d(TAG, "Resetting game")
        webView.evaluateJavascript("window.resetGame();") { result ->
            Log.d(TAG, "resetGame result: $result")
        }
    }

    /**
     * JavaScript bridge interface exposed to TypeScript
     */
    inner class AndroidBridge {
        /**
         * Called from TypeScript when game is completed
         */
        @JavascriptInterface
        fun onGameCompleted() {
            Log.d(TAG, "Game completed callback from JavaScript")
            
            // Post to UI thread since this is called from JS thread
            post {
                onGameCompleted?.invoke()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        
        // Clean up WebView
        webView.apply {
            loadUrl("about:blank")
            removeJavascriptInterface("Android")
            destroy()
        }
    }
}
