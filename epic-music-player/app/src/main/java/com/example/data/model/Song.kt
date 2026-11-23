package com.example.data.model

data class Song(
    val path: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val duration: Long,
    val size: Long,
    val year: String,
    val folder: String,
    val dateAdded: Long,
    val lyrics: String? = null,
    val customCover: String? = null,
    val playCount: Int = 0,
    val isHidden: Boolean = false,
    val isFavorite: Boolean = false
) {
    val durationText: String
        get() {
            val seconds = (duration / 1000) % 60
            val minutes = (duration / (1000 * 60)) % 60
            val hours = (duration / (1000 * 60 * 60))
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

    val sizeText: String
        get() {
            val kb = size / 1024.0
            val mb = kb / 1024.0
            return if (mb > 1) {
                String.format("%.2f MB", mb)
            } else {
                String.format("%.2f KB", kb)
            }
        }
}
