package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM clipboard_items ORDER BY isPinned DESC, timestamp DESC")
    fun getAllFlow(): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items WHERE isPinned = 1 ORDER BY timestamp DESC LIMIT 20")
    fun getPinnedFlow(): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items WHERE isPinned = 0 ORDER BY timestamp DESC LIMIT 20")
    fun getRecentFlow(): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items WHERE text = :text LIMIT 1")
    suspend fun findByText(text: String): ClipboardItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ClipboardItem): Long

    @Query("DELETE FROM clipboard_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE clipboard_items SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean)

    @Query("SELECT COUNT(*) FROM clipboard_items WHERE isPinned = 1")
    suspend fun getPinnedCount(): Int

    @Query("SELECT COUNT(*) FROM clipboard_items WHERE isPinned = 0")
    suspend fun getRecentCount(): Int

    @Query("DELETE FROM clipboard_items WHERE isPinned = 0 AND id NOT IN (SELECT id FROM clipboard_items WHERE isPinned = 0 ORDER BY timestamp DESC LIMIT 20)")
    suspend fun trimUnpinnedToTwenty()

    @Query("DELETE FROM clipboard_items WHERE isPinned = 0")
    suspend fun clearUnpinned()

    @Query("DELETE FROM clipboard_items")
    suspend fun clearAll()
}
