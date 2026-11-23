package com.example.data

import kotlinx.coroutines.flow.Flow

class ClipboardRepository(private val dao: ClipboardDao) {

    companion object {
        const val MAX_PINNED_ITEMS = 20
        const val MAX_RECENT_ITEMS = 20
    }

    val allItems: Flow<List<ClipboardItem>> = dao.getAllFlow()
    val pinnedItems: Flow<List<ClipboardItem>> = dao.getPinnedFlow()
    val recentItems: Flow<List<ClipboardItem>> = dao.getRecentFlow()

    suspend fun addClip(text: String, pinImmediately: Boolean = false): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return false

        val existing = dao.findByText(trimmed)
        if (existing != null) {
            val shouldPin = if (pinImmediately) true else existing.isPinned
            if (shouldPin && !existing.isPinned) {
                val currentPinnedCount = dao.getPinnedCount()
                if (currentPinnedCount >= MAX_PINNED_ITEMS) return false
            }
            dao.insert(
                existing.copy(
                    timestamp = System.currentTimeMillis(),
                    isPinned = shouldPin
                )
            )
            dao.trimUnpinnedToTwenty()
            return true
        }

        if (pinImmediately) {
            val currentPinnedCount = dao.getPinnedCount()
            if (currentPinnedCount >= MAX_PINNED_ITEMS) return false
        }

        dao.insert(
            ClipboardItem(
                text = trimmed,
                timestamp = System.currentTimeMillis(),
                isPinned = pinImmediately
            )
        )
        dao.trimUnpinnedToTwenty()
        return true
    }

    suspend fun togglePin(item: ClipboardItem): Boolean {
        if (!item.isPinned) {
            val count = dao.getPinnedCount()
            if (count >= MAX_PINNED_ITEMS) return false // Max 20 pinned items allowed
            dao.setPinned(item.id, true)
            return true
        } else {
            dao.setPinned(item.id, false)
            dao.trimUnpinnedToTwenty()
            return true
        }
    }

    suspend fun delete(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearRecent() {
        dao.clearUnpinned()
    }
}
