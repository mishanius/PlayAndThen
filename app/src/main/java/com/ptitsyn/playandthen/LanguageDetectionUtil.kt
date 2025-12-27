package com.ptitsyn.playandthen

import android.util.Log
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions

/**
 * Utility class for detecting and validating languages using ML Kit Language ID.
 */
object LanguageDetectionUtil {
    
    private const val TAG = "LanguageDetection"
    
    // Static list of allowed languages (ISO 639-1 codes)
    private val ALLOWED_LANGUAGES = setOf(
        "en", // English
        "he", // Hebrew
        "ru"  // Russian
    )
    
    // Language identifier with high confidence threshold (lazy initialization)
    private val languageIdentifier by lazy {
        LanguageIdentification.getClient(
            LanguageIdentificationOptions.Builder()
                .setConfidenceThreshold(0.5f)
                .build()
        )
    }
    
    /**
     * Detects the language of the given title and validates it against allowed languages.
     * @param title The title text to analyze
     * @param onResult Callback with detection result (detected language, is compliant)
     */
    fun detectAndValidateLanguage(title: String, onResult: (String?, Boolean) -> Unit) {
        if (title.isBlank()) {
            Log.w(TAG, "Cannot detect language: title is blank")
            onResult(null, false)
            return
        }
        
        languageIdentifier.identifyLanguage(title)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
                    // Undetermined language
                    Log.w(TAG, "Could not determine language for title: '$title'")
                    onResult(null, false)
                } else {
                    val isCompliant = ALLOWED_LANGUAGES.contains(languageCode)
                    
                    if (isCompliant) {
                        Log.d(TAG, "Language compliance: COMPLIANT - Detected language '$languageCode' for title: '$title'")
                    } else {
                        Log.e(TAG, "Language compliance: NON-COMPLIANT - Detected language '$languageCode' is not in allowed languages for title: '$title'")
                    }
                    
                    onResult(languageCode, isCompliant)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Language detection failed for title: '$title'", exception)
                onResult(null, false)
            }
    }
    
    /**
     * Gets the set of allowed language codes.
     * @return Set of allowed ISO 639-1 language codes
     */
    fun getAllowedLanguages(): Set<String> = ALLOWED_LANGUAGES.toSet()
    
    /**
     * Checks if a language code is in the allowed list.
     * @param languageCode The ISO 639-1 language code to check
     * @return True if the language is allowed, false otherwise
     */
    fun isLanguageAllowed(languageCode: String): Boolean = ALLOWED_LANGUAGES.contains(languageCode)
}
