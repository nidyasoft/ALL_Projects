package com.example.nlp

import android.content.Context
import com.example.data.PersonalDictionaryRepository
import com.example.settings.KeyboardLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

class PredictionEngine(
    context: Context,
    private val personalDictionaryRepo: PersonalDictionaryRepository? = null
) {
    private val appContext = context.applicationContext
    private val engineScope = CoroutineScope(Dispatchers.IO)

    // Per-language personal dictionary word -> frequency
    private val personalWordsByLanguage = ConcurrentHashMap<KeyboardLanguage, ConcurrentHashMap<String, Int>>()

    // Per-language N-gram models extracted from corpora + runtime learning
    private val unigramCounts = ConcurrentHashMap<KeyboardLanguage, ConcurrentHashMap<String, Int>>()
    private val bigramNext = ConcurrentHashMap<KeyboardLanguage, ConcurrentHashMap<String, ConcurrentHashMap<String, Int>>>()
    private val trigramNext = ConcurrentHashMap<KeyboardLanguage, ConcurrentHashMap<String, ConcurrentHashMap<String, Int>>>()
    private val fourgramNext = ConcurrentHashMap<KeyboardLanguage, ConcurrentHashMap<String, ConcurrentHashMap<String, Int>>>()

    private val baseVocabSets = ConcurrentHashMap<KeyboardLanguage, Set<String>>()

    init {
        KeyboardLanguage.entries.forEach { lang ->
            personalWordsByLanguage[lang] = ConcurrentHashMap()
            unigramCounts[lang] = ConcurrentHashMap()
            bigramNext[lang] = ConcurrentHashMap()
            trigramNext[lang] = ConcurrentHashMap()
            fourgramNext[lang] = ConcurrentHashMap()
            initLanguageCorpus(lang)
        }

        // Legacy migration of old SharedPreferences learned words to current storage if needed
        val legacyPrefs = appContext.getSharedPreferences("user_dictionary_prefs", Context.MODE_PRIVATE)
        val legacyWords = legacyPrefs.all.mapNotNull { (k, v) -> if (v is Int && k.trim().length >= 2) k.trim() to v else null }

        personalDictionaryRepo?.onDictionaryChanged = {
            reloadPersonalDictionaryAsync()
        }

        engineScope.launch {
            if (legacyWords.isNotEmpty() && personalDictionaryRepo != null) {
                legacyWords.forEach { (w, freq) ->
                    val guessLang = if (w.any { it in '\u0600'..'\u06FF' }) KeyboardLanguage.PERSIAN else KeyboardLanguage.ENGLISH
                    personalDictionaryRepo.addOrBoostWord(guessLang, w, freq)
                }
                legacyPrefs.edit().clear().apply()
            }
            reloadPersonalDictionarySync()
        }
    }

    private fun initLanguageCorpus(lang: KeyboardLanguage) {
        val baseWords = getBaseVocabulary(lang)
        baseVocabSets[lang] = baseWords.map { it.lowercase() }.toSet()

        val uni = unigramCounts[lang]!!
        // Earlier words in list represent higher base popularity
        baseWords.forEachIndexed { idx, w ->
            val clean = w.trim()
            if (clean.isNotEmpty()) {
                val weight = ((baseWords.size - idx) / 15).coerceIn(1, 15)
                uni[clean] = (uni[clean] ?: 0) + weight
            }
        }

        val phrases = when (lang) {
            KeyboardLanguage.PERSIAN -> WordDictionaries.PERSIAN_PHRASES
            KeyboardLanguage.ENGLISH -> WordDictionaries.ENGLISH_PHRASES
            KeyboardLanguage.TURKISH -> WordDictionaries.TURKISH_PHRASES
            KeyboardLanguage.AZERBAIJANI_LATIN -> WordDictionaries.AZERBAIJANI_LATIN_PHRASES
            KeyboardLanguage.AZERBAIJANI_ARABIC -> WordDictionaries.AZERBAIJANI_ARABIC_PHRASES
            KeyboardLanguage.ARABIC -> WordDictionaries.ARABIC_PHRASES
        }

        for (phrase in phrases) {
            val tokens = tokenize(phrase)
            trainNgramsFromTokens(lang, tokens, boost = 5)
        }
    }

    fun reloadPersonalDictionaryAsync() {
        engineScope.launch {
            reloadPersonalDictionarySync()
        }
    }

    suspend fun reloadPersonalDictionarySync() {
        val repo = personalDictionaryRepo ?: return
        val entities = repo.getAllWordsSync()
        val grouped = KeyboardLanguage.entries.associateWith { ConcurrentHashMap<String, Int>() }
        for (entity in entities) {
            val lang = KeyboardLanguage.fromId(entity.languageId)
            grouped[lang]?.put(entity.word, entity.frequency)
        }
        grouped.forEach { (lang, map) ->
            personalWordsByLanguage[lang] = map
        }
    }

    /**
     * Records committed word usage AND learns N-gram transitions from previous context words.
     */
    fun learnWord(
        context: Context,
        word: String,
        language: KeyboardLanguage = KeyboardLanguage.PERSIAN,
        previousContextText: String = "",
        saveToPersonalIfNew: Boolean = false
    ) {
        val clean = word.trim()
        if (clean.length < 2) return

        // Always update runtime unigram + N-gram transitions in memory
        val uni = unigramCounts[language] ?: return
        uni[clean] = (uni[clean] ?: 0) + 4

        val prevTokens = tokenize(previousContextText).takeLast(3)
        if (prevTokens.isNotEmpty()) {
            trainNgramsFromTokens(language, prevTokens + clean, boost = 4)
        }

        // Boost frequency if already in personal dictionary, or add explicitly if requested
        val personalMap = personalWordsByLanguage[language]
        if (saveToPersonalIfNew || (personalMap?.containsKey(clean) == true)) {
            personalMap?.put(clean, (personalMap[clean] ?: 0) + 3)
            engineScope.launch {
                personalDictionaryRepo?.addOrBoostWord(language, clean, frequencyIncrement = 3)
            }
        }
    }

    private fun trainNgramsFromTokens(lang: KeyboardLanguage, tokens: List<String>, boost: Int) {
        val bi = bigramNext[lang] ?: return
        val tri = trigramNext[lang] ?: return
        val four = fourgramNext[lang] ?: return

        for (i in tokens.indices) {
            val w = tokens[i]
            if (i >= 1) {
                val key1 = tokens[i - 1].lowercase()
                val nextMap = bi.getOrPut(key1) { ConcurrentHashMap() }
                nextMap[w] = (nextMap[w] ?: 0) + boost
            }
            if (i >= 2) {
                val key2 = "${tokens[i - 2].lowercase()} ${tokens[i - 1].lowercase()}"
                val nextMap = tri.getOrPut(key2) { ConcurrentHashMap() }
                nextMap[w] = (nextMap[w] ?: 0) + boost * 2
            }
            if (i >= 3) {
                val key3 = "${tokens[i - 3].lowercase()} ${tokens[i - 2].lowercase()} ${tokens[i - 1].lowercase()}"
                val nextMap = four.getOrPut(key3) { ConcurrentHashMap() }
                nextMap[w] = (nextMap[w] ?: 0) + boost * 3
            }
        }
    }

    fun getBaseVocabulary(language: KeyboardLanguage): List<String> {
        return when (language) {
            KeyboardLanguage.PERSIAN -> WordDictionaries.PERSIAN_WORDS
            KeyboardLanguage.ENGLISH -> WordDictionaries.ENGLISH_WORDS
            KeyboardLanguage.TURKISH -> WordDictionaries.TURKISH_WORDS
            KeyboardLanguage.AZERBAIJANI_LATIN -> WordDictionaries.AZERBAIJANI_LATIN_WORDS
            KeyboardLanguage.AZERBAIJANI_ARABIC -> WordDictionaries.AZERBAIJANI_ARABIC_WORDS
            KeyboardLanguage.ARABIC -> WordDictionaries.ARABIC_WORDS
        }
    }

    fun getVocabulary(language: KeyboardLanguage): List<String> {
        val base = getBaseVocabulary(language)
        val personal = personalWordsByLanguage[language]?.keys?.toList() ?: emptyList()
        return if (personal.isEmpty()) base else (personal + base).distinct()
    }

    /**
     * Checks if a typed word already exists in the base OR personal dictionary for the active language.
     * Returns true for short (<= 1 char) tokens, numbers, or punctuation so only real new words trigger "+ Add".
     */
    fun isKnownWord(word: String, language: KeyboardLanguage): Boolean {
        val clean = word.trim()
        if (clean.length < 2) return true
        // Ignore pure digits / symbols
        if (!clean.any { it.isLetter() }) return true

        val lower = clean.lowercase()
        if (baseVocabSets[language]?.contains(lower) == true) return true
        val personalMap = personalWordsByLanguage[language]
        if (personalMap != null && personalMap.keys.any { it.lowercase() == lower }) return true
        return false
    }

    /**
     * Computes context score map (word -> score) using 4-gram -> 3-gram -> 2-gram hierarchy
     * over the preceding sentence tokens.
     */
    private fun getContextPredictionsScoreMap(
        previousTextBeforeWord: String,
        language: KeyboardLanguage
    ): Map<String, Double> {
        val tokens = tokenize(previousTextBeforeWord)
        if (tokens.isEmpty()) return emptyMap()

        val scores = HashMap<String, Double>()
        val n = tokens.size

        // 4-gram (strongest multi-word phrase signal)
        if (n >= 3) {
            val k3 = "${tokens[n - 3].lowercase()} ${tokens[n - 2].lowercase()} ${tokens[n - 1].lowercase()}"
            fourgramNext[language]?.get(k3)?.forEach { (cand, count) ->
                scores[cand] = (scores[cand] ?: 0.0) + 18.0 + count * 0.8
            }
        }

        // 3-gram
        if (n >= 2) {
            val k2 = "${tokens[n - 2].lowercase()} ${tokens[n - 1].lowercase()}"
            trigramNext[language]?.get(k2)?.forEach { (cand, count) ->
                scores[cand] = (scores[cand] ?: 0.0) + 11.0 + count * 0.6
            }
        }

        // 2-gram
        val k1 = tokens[n - 1].lowercase()
        bigramNext[language]?.get(k1)?.forEach { (cand, count) ->
            scores[cand] = (scores[cand] ?: 0.0) + 5.5 + count * 0.4
        }

        return scores
    }

    /**
     * Generates top 4 Gboard-style predictions:
     * 1. Empty currentWord -> Multi-word context (4-gram / 3-gram / 2-gram) next-word prediction.
     * 2. Active currentWord (on EVERY keystroke without space!) -> Fuses context N-gram scores with
     *    prefix completions, personal dictionary entries, and geometric fat-finger typo tolerance.
     */
    fun getSuggestions(
        currentWord: String,
        previousText: String,
        language: KeyboardLanguage
    ): List<String> {
        val trimmed = currentWord.trim()
        val trimmedLower = trimmed.lowercase()
        val vocab = getVocabulary(language)
        val personalMap = personalWordsByLanguage[language] ?: emptyMap()
        val uniMap = unigramCounts[language] ?: emptyMap()
        val contextScores = getContextPredictionsScoreMap(previousText, language)

        // 1. Next-word prediction when user has not started typing a new word yet
        if (trimmedLower.isEmpty()) {
            val contextSorted = contextScores.entries
                .sortedByDescending { it.value + (personalMap[it.key] ?: 0) * 0.3 }
                .map { it.key }

            if (contextSorted.size >= 4) {
                return contextSorted.take(4)
            }

            val starterCandidates = (contextSorted + personalMap.entries.sortedByDescending { it.value }.map { it.key } + vocab)
                .distinct()
            return starterCandidates.take(4)
        }

        // 2. Instant Mid-Word Context + Prefix + Typo Scoring (on EVERY character typed, without needing space!)
        val scoredCandidates = HashMap<String, Double>()

        // 2a. First check all context-predicted words that match the typed prefix (or are close typos!)
        for ((word, ctxScore) in contextScores) {
            val lowerWord = word.lowercase()
            if (lowerWord.startsWith(trimmedLower)) {
                val completenessBonus = (trimmedLower.length.toDouble() / lowerWord.length.coerceAtLeast(1)) * 4.0
                val personalBonus = if (personalMap.containsKey(word)) 3.5 else 0.0
                scoredCandidates[word] = ctxScore + 12.0 + completenessBonus + personalBonus
            } else if (abs(lowerWord.length - trimmedLower.length) <= 2 && lowerWord.length >= 2) {
                val dist = ProximityModel.weightedDistance(trimmedLower, lowerWord, language)
                if (dist <= 1.55) {
                    scoredCandidates[word] = ctxScore + 6.0 - dist * 4.0
                }
            }
        }

        // 2b. Direct prefix matches across Personal Dictionary + Base Vocabulary
        for (word in vocab) {
            val lowerWord = word.lowercase()
            if (lowerWord.startsWith(trimmedLower)) {
                val ctxScore = contextScores[word] ?: 0.0
                val freqScore = ((uniMap[word] ?: 1) * 0.22).coerceAtMost(6.0)
                val personalScore = personalMap[word]?.let { 4.5 + (it * 0.25).coerceAtMost(6.0) } ?: 0.0
                val exactBonus = if (lowerWord == trimmedLower) 5.0 else 0.0
                // Prefer tighter prefix completions when context is neutral
                val lengthPenalty = (lowerWord.length - trimmedLower.length) * 0.12
                val total = 10.0 + ctxScore + freqScore + personalScore + exactBonus - lengthPenalty
                val prevBest = scoredCandidates[word]
                if (prevBest == null || total > prevBest) {
                    scoredCandidates[word] = total
                }
            }
        }

        // 2c. Geometric Fat-Finger / Proximity Correction across vocabulary
        if (trimmedLower.length >= 2) {
            for (word in vocab) {
                if (scoredCandidates.containsKey(word)) continue
                val lowerWord = word.lowercase()
                if (abs(lowerWord.length - trimmedLower.length) > 2) continue

                val dist = ProximityModel.weightedDistance(trimmedLower, lowerWord, language)
                if (dist <= 1.75) {
                    val ctxScore = (contextScores[word] ?: 0.0) * 0.75
                    val personalScore = personalMap[word]?.let { 2.5 + (it * 0.15).coerceAtMost(4.0) } ?: 0.0
                    val freqScore = ((uniMap[word] ?: 1) * 0.12).coerceAtMost(3.0)
                    val score = 7.2 - dist * 4.2 + ctxScore + personalScore + freqScore
                    if (score > 0.5) {
                        scoredCandidates[word] = score
                    }
                }
            }
        }

        val ranked = scoredCandidates.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .distinct()
            .toMutableList()

        if (ranked.size < 4) {
            val fallback = vocab.filter {
                it.lowercase().contains(trimmedLower) && !ranked.contains(it)
            }.take(4 - ranked.size)
            ranked.addAll(fallback)
        }

        return if (ranked.isEmpty()) listOf(currentWord) else ranked.take(4)
    }

    /**
     * Produces a normalized next-character probability map [0f..1f] for the Gboard SpatialTapDecoder,
     * dynamically fusing multi-word sentence context + active word prefix + personal dictionary.
     */
    fun getNextCharacterProbabilities(
        currentWord: String,
        previousText: String,
        language: KeyboardLanguage
    ): Map<Char, Float> {
        val trimmed = currentWord.trim().lowercase()
        val contextScores = getContextPredictionsScoreMap(previousText, language)
        val vocab = getVocabulary(language)
        val personalMap = personalWordsByLanguage[language] ?: emptyMap()

        val rawWeights = HashMap<Char, Double>()

        if (trimmed.isEmpty()) {
            // Next-word start character probabilities from sentence context + unigrams
            for ((word, ctxScore) in contextScores) {
                val ch = word.firstOrNull()?.lowercaseChar() ?: continue
                rawWeights[ch] = (rawWeights[ch] ?: 0.0) + ctxScore + 4.0
            }
            for ((word, freq) in personalMap) {
                val ch = word.firstOrNull()?.lowercaseChar() ?: continue
                rawWeights[ch] = (rawWeights[ch] ?: 0.0) + 1.5 + freq * 0.1
            }
            // Light starter prior from top 40 words
            vocab.take(40).forEach { w ->
                val ch = w.firstOrNull()?.lowercaseChar() ?: return@forEach
                rawWeights[ch] = (rawWeights[ch] ?: 0.0) + 0.6
            }
        } else {
            val nextIdx = trimmed.length
            // Prefix matches in context first
            for ((word, ctxScore) in contextScores) {
                val lower = word.lowercase()
                if (lower.startsWith(trimmed) && lower.length > nextIdx) {
                    val ch = lower[nextIdx]
                    rawWeights[ch] = (rawWeights[ch] ?: 0.0) + 8.0 + ctxScore
                }
            }
            // Prefix matches in personal dictionary + base vocab
            for (word in vocab) {
                val lower = word.lowercase()
                if (lower.startsWith(trimmed) && lower.length > nextIdx) {
                    val ch = lower[nextIdx]
                    val personalBoost = if (personalMap.containsKey(word)) 3.5 else 1.2
                    rawWeights[ch] = (rawWeights[ch] ?: 0.0) + personalBoost
                }
            }
        }

        val total = rawWeights.values.sum()
        if (total <= 0.0) return emptyMap()

        return rawWeights.mapValues { (_, v) -> (v / total).toFloat().coerceIn(0f, 1f) }
    }

    fun getLikelyNextCharacters(currentWord: String, language: KeyboardLanguage): Set<Char> {
        return getNextCharacterProbabilities(currentWord, "", language).keys
    }

    private fun tokenize(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        return text.trim()
            .split(Regex("[\\s.,،؛;:!?؟()\\[\\]{}«»\"']+"))
            .filter { it.isNotEmpty() }
    }
}
