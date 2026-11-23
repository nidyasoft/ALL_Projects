package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalDictionaryDao {
    @Query("SELECT * FROM personal_dictionary WHERE languageId = :languageId ORDER BY updatedAt DESC, frequency DESC")
    fun getWordsByLanguageFlow(languageId: String): Flow<List<PersonalWordEntity>>

    @Query("SELECT * FROM personal_dictionary ORDER BY updatedAt DESC")
    fun getAllWordsFlow(): Flow<List<PersonalWordEntity>>

    @Query("SELECT * FROM personal_dictionary")
    suspend fun getAllWordsSync(): List<PersonalWordEntity>

    @Query("SELECT * FROM personal_dictionary WHERE languageId = :languageId AND word = :word LIMIT 1")
    suspend fun findWord(languageId: String, word: String): PersonalWordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: PersonalWordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PersonalWordEntity>)

    @Query("UPDATE personal_dictionary SET word = :newWord, languageId = :newLanguageId, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateWord(id: Long, newWord: String, newLanguageId: String, updatedAt: Long)

    @Query("DELETE FROM personal_dictionary WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM personal_dictionary WHERE languageId = :languageId")
    suspend fun deleteByLanguage(languageId: String)
}
