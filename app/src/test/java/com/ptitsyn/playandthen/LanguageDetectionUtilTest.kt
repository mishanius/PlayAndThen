package com.ptitsyn.playandthen

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for LanguageDetectionUtil.
 */
class LanguageDetectionUtilTest {

    @Test
    fun `test allowed languages contains only English Hebrew and Russian`() {
        val allowedLanguages = LanguageDetectionUtil.getAllowedLanguages()
        
        // Test exact set of allowed languages
        assertEquals("Should contain exactly 3 languages", 3, allowedLanguages.size)
        assertTrue("English should be allowed", allowedLanguages.contains("en"))
        assertTrue("Hebrew should be allowed", allowedLanguages.contains("he"))
        assertTrue("Russian should be allowed", allowedLanguages.contains("ru"))
    }
    
    @Test
    fun `test isLanguageAllowed works correctly`() {
        // Test allowed languages
        assertTrue("English should be allowed", LanguageDetectionUtil.isLanguageAllowed("en"))
        assertTrue("Hebrew should be allowed", LanguageDetectionUtil.isLanguageAllowed("he"))
        assertTrue("Russian should be allowed", LanguageDetectionUtil.isLanguageAllowed("ru"))
        
        // Test disallowed languages
        assertFalse("Spanish should not be allowed", LanguageDetectionUtil.isLanguageAllowed("es"))
        assertFalse("French should not be allowed", LanguageDetectionUtil.isLanguageAllowed("fr"))
        assertFalse("German should not be allowed", LanguageDetectionUtil.isLanguageAllowed("de"))
        assertFalse("Chinese should not be allowed", LanguageDetectionUtil.isLanguageAllowed("zh"))
        assertFalse("Random code should not be allowed", LanguageDetectionUtil.isLanguageAllowed("xx"))
        assertFalse("Empty string should not be allowed", LanguageDetectionUtil.isLanguageAllowed(""))
        assertFalse("Undefined should not be allowed", LanguageDetectionUtil.isLanguageAllowed("und"))
    }
    
    @Test
    fun `test allowed languages are valid ISO codes`() {
        val allowedLanguages = LanguageDetectionUtil.getAllowedLanguages()
        
        for (languageCode in allowedLanguages) {
            // ISO 639-1 codes should be exactly 2 characters long
            assertEquals("Language code '$languageCode' should be 2 characters long", 2, languageCode.length)
            
            // Should contain only lowercase letters
            assertTrue("Language code '$languageCode' should contain only lowercase letters", 
                languageCode.matches(Regex("[a-z]{2}")))
        }
    }
    
    @Test
    fun `test getAllowedLanguages returns immutable copy`() {
        val allowedLanguages1 = LanguageDetectionUtil.getAllowedLanguages()
        val allowedLanguages2 = LanguageDetectionUtil.getAllowedLanguages()
        
        // Should be equal but not the same reference
        assertEquals("Both calls should return the same languages", allowedLanguages1, allowedLanguages2)
        
        // Modifying one should not affect the original or other calls
        val modifiableSet = allowedLanguages1.toMutableSet()
        modifiableSet.add("xx")
        
        val allowedLanguages3 = LanguageDetectionUtil.getAllowedLanguages()
        assertEquals("Original should not be affected", 3, allowedLanguages3.size)
        assertFalse("Should not contain added language", allowedLanguages3.contains("xx"))
    }
}
