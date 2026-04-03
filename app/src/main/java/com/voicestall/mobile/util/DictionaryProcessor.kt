package com.voicestall.mobile.util

import com.voicestall.mobile.data.local.entity.DictionaryEntryEntity

object DictionaryProcessor {

    fun apply(text: String, entries: List<DictionaryEntryEntity>): String {
        var result = text
        for (entry in entries) {
            if (!entry.enabled) continue
            try {
                val regex = Regex(entry.pattern, RegexOption.IGNORE_CASE)
                result = regex.replace(result, entry.replacement)
            } catch (_: Exception) {
                // Skip invalid regex patterns
            }
        }
        return result
    }

    fun isValidRegex(pattern: String): Boolean {
        return try {
            Regex(pattern)
            true
        } catch (_: Exception) {
            false
        }
    }
}
