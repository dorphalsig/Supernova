package com.supernova.utils

import java.text.Normalizer
import java.util.Locale

object SearchUtils {
    /**
     * Normalizes text for FTS queries: lowercase, remove punctuation and diacritics,
     * collapse whitespace.
     */
    fun normalize(input: String): String {
        var text = Normalizer.normalize(input, Normalizer.Form.NFD)
        text = text.replace(Regex("""\p{InCombiningDiacriticalMarks}+"""), "")
        text = text.lowercase(Locale.US)
        text = text.replace(Regex("""[^\p{L}\p{Nd}\s]"""), " ")
        text = text.replace(Regex("""\s+"""), " ")
        return text.trim()
    }
}
