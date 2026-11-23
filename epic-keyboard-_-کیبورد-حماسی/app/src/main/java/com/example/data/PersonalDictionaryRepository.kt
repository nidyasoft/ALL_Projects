package com.example.data

import com.example.settings.KeyboardLanguage
import kotlinx.coroutines.flow.Flow

data class CsvImportResult(
    val addedCount: Int,
    val updatedCount: Int,
    val skippedCount: Int
)

class PersonalDictionaryRepository(private val dao: PersonalDictionaryDao) {

    var onDictionaryChanged: (() -> Unit)? = null

    val allWordsFlow: Flow<List<PersonalWordEntity>> = dao.getAllWordsFlow()

    fun getWordsFlow(language: KeyboardLanguage): Flow<List<PersonalWordEntity>> {
        return dao.getWordsByLanguageFlow(language.id)
    }

    suspend fun getAllWordsSync(): List<PersonalWordEntity> = dao.getAllWordsSync()

    suspend fun addOrBoostWord(
        language: KeyboardLanguage,
        word: String,
        frequencyIncrement: Int = 5
    ): Boolean {
        val clean = word.trim()
        if (clean.length < 2) return false

        val existing = dao.findWord(language.id, clean)
        if (existing != null) {
            dao.insertOrReplace(
                existing.copy(
                    frequency = (existing.frequency + frequencyIncrement).coerceAtMost(1000),
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            dao.insertOrReplace(
                PersonalWordEntity(
                    languageId = language.id,
                    word = clean,
                    frequency = frequencyIncrement.coerceAtLeast(1),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        onDictionaryChanged?.invoke()
        return true
    }

    suspend fun updateWord(
        id: Long,
        newWord: String,
        newLanguage: KeyboardLanguage
    ): Boolean {
        val clean = newWord.trim()
        if (clean.length < 2) return false
        val conflict = dao.findWord(newLanguage.id, clean)
        if (conflict != null && conflict.id != id) {
            // Merge into existing and remove old duplicate row
            dao.deleteById(id)
        } else {
            dao.updateWord(id, clean, newLanguage.id, System.currentTimeMillis())
        }
        onDictionaryChanged?.invoke()
        return true
    }

    suspend fun deleteWord(id: Long) {
        dao.deleteById(id)
        onDictionaryChanged?.invoke()
    }

    suspend fun clearLanguage(language: KeyboardLanguage) {
        dao.deleteByLanguage(language.id)
        onDictionaryChanged?.invoke()
    }

    suspend fun importCsv(defaultLanguage: KeyboardLanguage, csvContent: String): CsvImportResult {
        val cleanedContent = csvContent.removePrefix("\uFEFF")
        val lines = cleanedContent.lines()

        var added = 0
        var updated = 0
        var skipped = 0

        for ((index, rawLine) in lines.withIndex()) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) continue

            val columns = parseCsvLine(line)
            if (columns.isEmpty()) {
                skipped++
                continue
            }

            val firstCol = columns[0].trim()
            if (firstCol.isEmpty()) {
                skipped++
                continue
            }

            // Skip header row if present
            if (index == 0 && (
                    firstCol.equals("word", ignoreCase = true) ||
                    firstCol == "کلمه" ||
                    firstCol == "واژه" ||
                    firstCol.equals("kelime", ignoreCase = true) ||
                    firstCol.equals("söz", ignoreCase = true)
                )
            ) {
                continue
            }

            if (firstCol.length < 2) {
                skipped++
                continue
            }

            val lang = if (columns.size >= 2 && columns[1].isNotBlank()) {
                resolveLanguage(columns[1].trim(), defaultLanguage)
            } else {
                defaultLanguage
            }

            val freq = if (columns.size >= 3) {
                columns[2].trim().toIntOrNull()?.coerceIn(1, 500) ?: 10
            } else {
                10
            }

            val existing = dao.findWord(lang.id, firstCol)
            if (existing != null) {
                dao.insertOrReplace(
                    existing.copy(
                        frequency = maxOf(existing.frequency, freq),
                        updatedAt = System.currentTimeMillis()
                    )
                )
                updated++
            } else {
                dao.insertOrReplace(
                    PersonalWordEntity(
                        languageId = lang.id,
                        word = firstCol,
                        frequency = freq,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                added++
            }
        }

        if (added > 0 || updated > 0) {
            onDictionaryChanged?.invoke()
        }
        return CsvImportResult(addedCount = added, updatedCount = updated, skippedCount = skipped)
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                (ch == ',' || ch == ';') && !inQuotes -> {
                    result.add(current.toString().trim())
                    current.clear()
                }
                else -> current.append(ch)
            }
            i++
        }
        result.add(current.toString().trim())
        return result
    }

    private fun resolveLanguage(token: String, fallback: KeyboardLanguage): KeyboardLanguage {
        return when (token.lowercase()) {
            "fa", "persian", "farsi", "فارسی" -> KeyboardLanguage.PERSIAN
            "en", "english", "انگلیسی" -> KeyboardLanguage.ENGLISH
            "tr", "turkish", "türkçe", "ترکی" -> KeyboardLanguage.TURKISH
            "az_latn", "az", "azerbaijani", "azərbaycanca", "آذربایجانی لاتین" -> KeyboardLanguage.AZERBAIJANI_LATIN
            "az_arab", "azb", "تورکجه", "ترکی آذربایجانی" -> KeyboardLanguage.AZERBAIJANI_ARABIC
            "ar", "arabic", "عربی", "العربية" -> KeyboardLanguage.ARABIC
            else -> fallback
        }
    }
}
