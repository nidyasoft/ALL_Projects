package com.example.nlp

import com.example.settings.KeyboardLanguage
import kotlin.math.hypot

object ProximityModel {

    // Physical row/col coordinates normalized for each keyboard layout
    private val PERSIAN_POSITIONS: Map<Char, Pair<Float, Float>> = buildPositionMap(
        listOf(
            listOf('ض', 'ص', 'ث', 'ق', 'ف', 'غ', 'ع', 'ه', 'خ', 'ح', 'ج', 'چ'),
            listOf('ش', 'س', 'ی', 'ب', 'ل', 'ا', 'ت', 'ن', 'م', 'ک', 'گ'),
            listOf('ظ', 'ط', 'ز', 'ر', 'ذ', 'د', 'پ', 'و')
        ),
        rowOffsets = floatArrayOf(0f, 0.35f, 0.7f)
    )

    private val AZ_ARAB_POSITIONS: Map<Char, Pair<Float, Float>> = PERSIAN_POSITIONS + mapOf(
        'ؤ' to (2f to 7.2f),
        'آ' to (1f to 5.1f)
    )

    private val ARABIC_POSITIONS: Map<Char, Pair<Float, Float>> = buildPositionMap(
        listOf(
            listOf('ض', 'ص', 'ث', 'ق', 'ف', 'غ', 'ع', 'ه', 'خ', 'ح', 'ج', 'د'),
            listOf('ش', 'س', 'ي', 'ب', 'ل', 'ا', 'ت', 'ن', 'م', 'ك', 'ط'),
            listOf('ئ', 'ء', 'ؤ', 'ر', 'ى', 'ة', 'و', 'ز', 'ظ')
        ),
        rowOffsets = floatArrayOf(0f, 0.3f, 0.5f)
    )

    private val ENGLISH_POSITIONS: Map<Char, Pair<Float, Float>> = buildPositionMap(
        listOf(
            listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'),
            listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l'),
            listOf('z', 'x', 'c', 'v', 'b', 'n', 'm')
        ),
        rowOffsets = floatArrayOf(0f, 0.5f, 1.35f)
    )

    private val TURKISH_POSITIONS: Map<Char, Pair<Float, Float>> = buildPositionMap(
        listOf(
            listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'ı', 'o', 'p', 'ğ', 'ü'),
            listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'ş', 'i'),
            listOf('z', 'x', 'c', 'v', 'b', 'n', 'm', 'ö', 'ç')
        ),
        rowOffsets = floatArrayOf(0f, 0.3f, 1.1f)
    )

    private val AZ_LATIN_POSITIONS: Map<Char, Pair<Float, Float>> = buildPositionMap(
        listOf(
            listOf('q', 'ü', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'ö', 'ğ'),
            listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'ı', 'ə'),
            listOf('z', 'x', 'c', 'v', 'b', 'n', 'm', 'ç', 'ş')
        ),
        rowOffsets = floatArrayOf(0f, 0.3f, 1.1f)
    )

    private fun buildPositionMap(
        rows: List<List<Char>>,
        rowOffsets: FloatArray
    ): Map<Char, Pair<Float, Float>> {
        val map = mutableMapOf<Char, Pair<Float, Float>>()
        rows.forEachIndexed { rowIdx, chars ->
            val offset = rowOffsets.getOrElse(rowIdx) { 0f }
            chars.forEachIndexed { colIdx, ch ->
                map[ch] = rowIdx.toFloat() to (colIdx + offset)
            }
        }
        return map
    }

    private fun positionsFor(language: KeyboardLanguage): Map<Char, Pair<Float, Float>> {
        return when (language) {
            KeyboardLanguage.PERSIAN -> PERSIAN_POSITIONS
            KeyboardLanguage.AZERBAIJANI_ARABIC -> AZ_ARAB_POSITIONS
            KeyboardLanguage.ARABIC -> ARABIC_POSITIONS
            KeyboardLanguage.ENGLISH -> ENGLISH_POSITIONS
            KeyboardLanguage.TURKISH -> TURKISH_POSITIONS
            KeyboardLanguage.AZERBAIJANI_LATIN -> AZ_LATIN_POSITIONS
        }
    }

    fun keyDistance(c1: Char, c2: Char, language: KeyboardLanguage): Double {
        val ch1 = normalizeArabicChar(c1.lowercaseChar())
        val ch2 = normalizeArabicChar(c2.lowercaseChar())
        if (ch1 == ch2) return 0.0

        val posMap = positionsFor(language)
        val p1 = posMap[ch1] ?: return 2.5
        val p2 = posMap[ch2] ?: return 2.5
        return hypot((p1.first - p2.first).toDouble(), (p1.second - p2.second).toDouble())
    }

    fun areAdjacent(c1: Char, c2: Char, language: KeyboardLanguage): Boolean {
        return keyDistance(c1, c2, language) <= 1.48
    }

    fun areAdjacent(c1: Char, c2: Char, isPersianOrArabic: Boolean): Boolean {
        val lang = if (isPersianOrArabic) KeyboardLanguage.PERSIAN else KeyboardLanguage.ENGLISH
        return areAdjacent(c1, c2, lang)
    }

    /**
     * Substitution cost proportional to geometric distance on the exact active keyboard layout.
     */
    fun substitutionCost(c1: Char, c2: Char, language: KeyboardLanguage): Double {
        val ch1 = c1.lowercaseChar()
        val ch2 = c2.lowercaseChar()
        if (ch1 == ch2) return 0.0

        val dist = keyDistance(ch1, ch2, language)
        return when {
            dist <= 1.05 -> 0.34 // Immediate horizontal/vertical neighbor
            dist <= 1.50 -> 0.48 // Diagonal neighbor
            dist <= 2.15 -> 0.78 // Two keys away
            else -> 1.0
        }
    }

    /**
     * Weighted Damerau-Levenshtein Distance (supports adjacent swaps, fat-finger substitutions,
     * and missed/extra letters across all 6 languages).
     */
    fun weightedDistance(
        s1: String,
        s2: String,
        language: KeyboardLanguage
    ): Double {
        val m = s1.length
        val n = s2.length
        if (m == 0) return n.toDouble()
        if (n == 0) return m.toDouble()

        val dp = Array(m + 1) { DoubleArray(n + 1) }
        for (i in 0..m) dp[i][0] = i.toDouble()
        for (j in 0..n) dp[0][j] = j.toDouble()

        for (i in 1..m) {
            for (j in 1..n) {
                val c1 = s1[i - 1]
                val c2 = s2[j - 1]
                val subCost = substitutionCost(c1, c2, language)

                var best = minOf(
                    dp[i - 1][j] + 0.95,       // deletion (accidentally double/extra typed letter)
                    dp[i][j - 1] + 0.95,       // insertion (skipped letter)
                    dp[i - 1][j - 1] + subCost // substitution
                )

                // Adjacent transposition (e.g. "سالم" -> "سلام") costs 0.45
                if (i > 1 && j > 1 &&
                    s1[i - 1].lowercaseChar() == s2[j - 2].lowercaseChar() &&
                    s1[i - 2].lowercaseChar() == s2[j - 1].lowercaseChar()
                ) {
                    best = minOf(best, dp[i - 2][j - 2] + 0.45)
                }

                dp[i][j] = best
            }
        }
        return dp[m][n]
    }

    fun weightedDistance(s1: String, s2: String, isRtl: Boolean): Double {
        val lang = if (isRtl) KeyboardLanguage.PERSIAN else KeyboardLanguage.ENGLISH
        return weightedDistance(s1, s2, lang)
    }

    private fun normalizeArabicChar(ch: Char): Char {
        return when (ch) {
            'آ', 'أ', 'إ', 'ٱ' -> 'ا'
            'ي', 'ئ' -> 'ی'
            'ك' -> 'ک'
            'ة', 'ۀ' -> 'ه'
            'ؤ' -> 'و'
            else -> ch
        }
    }
}
