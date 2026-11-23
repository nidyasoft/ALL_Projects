package com.example.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object AudioFileManager {

    /**
     * Resolves the real display title of an audio URI and saves a permanent copy
     * into the app's internal private storage directory so that it can be played
     * at any time across reboots, app restarts, and background services without SAF permission expiration.
     *
     * @return Pair of (permanent file Uri string, clean display title)
     */
    fun saveAudioLocally(context: Context, sourceUri: Uri): Pair<String, String> {
        val detectedTitle = queryDisplayTitle(context, sourceUri)
        val cleanTitle = cleanFileName(detectedTitle)

        return try {
            val audioDir = File(context.filesDir, "custom_audio")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }

            // Create a safe unique filename preserving extension if available
            val safeBaseName = cleanTitle.replace("[^a-zA-Z0-9._\\- \u0600-\u06FF]".toRegex(), "_")
            val destFile = File(audioDir, "audio_${System.currentTimeMillis()}_$safeBaseName")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            val localUri = Uri.fromFile(destFile).toString()
            Pair(localUri, cleanTitle)
        } catch (e: Throwable) {
            // Fallback to original URI string if file copy encounters an issue
            Pair(sourceUri.toString(), cleanTitle)
        }
    }

    /**
     * Queries the best possible user-facing title for an audio URI.
     */
    fun queryDisplayTitle(context: Context, uri: Uri): String {
        // 1. Try querying OpenableColumns.DISPLAY_NAME from ContentResolver
        try {
            if (uri.scheme == "content") {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            val name = cursor.getString(nameIndex)
                            if (!name.isNullOrBlank()) {
                                return cleanFileName(name)
                            }
                        }
                    }
                }
            }
        } catch (_: Throwable) {}

        // 2. Try MediaMetadataRetriever
        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(context, uri)
            val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            mmr.release()
            if (!title.isNullOrBlank()) {
                return if (!artist.isNullOrBlank()) "$artist - $title" else title
            }
        } catch (_: Throwable) {}

        // 3. Try file path if it's a file URI
        if (uri.scheme == "file") {
            val file = File(uri.path ?: "")
            if (file.exists() && file.name.isNotBlank()) {
                return cleanFileName(file.name)
            }
        }

        // 4. Last path segment or default
        val lastSegment = uri.lastPathSegment
        if (!lastSegment.isNullOrBlank() && !lastSegment.contains(":")) {
            return cleanFileName(lastSegment)
        }

        return "آهنگ انتخابی"
    }

    /**
     * Cleans up common file extensions for display if desired, or returns trimmed name.
     */
    private fun cleanFileName(name: String): String {
        var clean = name.trim()
        val extensions = listOf(".mp3", ".m4a", ".ogg", ".wav", ".aac", ".flac", ".opus", ".wma")
        for (ext in extensions) {
            if (clean.endsWith(ext, ignoreCase = true)) {
                clean = clean.substring(0, clean.length - ext.length)
                break
            }
        }
        return if (clean.isBlank()) "آهنگ انتخابی" else clean
    }
}
