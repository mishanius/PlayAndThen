/**
 * Mock Android Bridge for browser testing.
 * Simulates the window.Android interface provided by GridGameJs.kt in Android WebView.
 */
(function() {
    'use strict';

    // Only install mock if not running in Android WebView
    if (window.Android) {
        console.log('[AndroidBridge] Real Android bridge detected, skipping mock');
        return;
    }

    console.log('[AndroidBridge] Installing mock bridge for browser testing');

    // Create mock overlay for visual feedback
    function createMockOverlay(message, color) {
        const overlay = document.createElement('div');
        overlay.id = 'mock-overlay';
        overlay.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0, 0, 0, 0.8);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            z-index: 10000;
            font-family: Arial, sans-serif;
        `;
        
        const text = document.createElement('div');
        text.style.cssText = `
            color: ${color};
            font-size: 48px;
            font-weight: bold;
            text-align: center;
            margin-bottom: 20px;
        `;
        text.textContent = message;
        
        const subtext = document.createElement('div');
        subtext.style.cssText = `
            color: #888;
            font-size: 18px;
        `;
        subtext.textContent = 'Click anywhere to restart';
        
        overlay.appendChild(text);
        overlay.appendChild(subtext);
        
        overlay.addEventListener('click', function() {
            overlay.remove();
            if (window.resetGame) {
                window.resetGame();
            }
        });
        
        document.body.appendChild(overlay);
    }

    // Install mock Android bridge
    window.Android = {
        onGameCompleted: function() {
            console.log('[MOCK] onGameCompleted() called');
            createMockOverlay('🎉 Game Completed!', '#4CAF50');
        }
    };

    console.log('[AndroidBridge] Mock bridge installed successfully');
    
    // Auto-start game for browser testing
    window.addEventListener('DOMContentLoaded', function() {
        setTimeout(function() {
            if (window.initGame) {
                console.log('[AndroidBridge] Auto-starting game for browser testing');
                window.initGame({ currentRound: 1, totalRounds: 1 });
            }
        }, 100);
    });
})();
