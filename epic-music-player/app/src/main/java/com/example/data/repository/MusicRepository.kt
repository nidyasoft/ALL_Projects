package com.example.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.data.db.MusicDao
import com.example.data.db.PlaylistEntity
import com.example.data.db.PlaylistSongEntity
import com.example.data.db.SongOverrideEntity
import com.example.data.db.PlayerSettingEntity
import com.example.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File

class MusicRepository(
    private val context: Context,
    private val musicDao: MusicDao
) {
    // Media extensions to scan
    private val extensionsToScan = setOf(
        "mp3", "wav", "flac", "aac", "ogg", "wma", "aiff", "alac", "m4a", "opus", 
        "mp4", "midi", "ape", "dsd", "dsf", "dff", "amr", "3gp", "au", "ra", "rm", 
        "caf", "ac3", "eac3", "dts", "mkv", "webm", "aax", "aif", "snd", "voc", "qcp"
    )

    // Flow of all playlists
    val playlists: Flow<List<PlaylistEntity>> = musicDao.getAllPlaylists()

    // Flow of song overrides
    private val songOverrides: Flow<List<SongOverrideEntity>> = musicDao.getAllSongOverrides()

    // Retrieve active player settings
    val playerSettingsFlow = musicDao.getPlayerSettingsFlow()

    // Quick access to preloaded tracks
    val preloadedTracks = listOf(
        Song(
            path = "PRELOADED_STARLIGHT_VOYAGE",
            title = "Starlight Voyage",
            artist = "Aether Horizon",
            album = "Nebula Dreams",
            genre = "Synthwave",
            duration = 145000,
            size = 3500000,
            year = "2024",
            folder = "Epic Player Preloads",
            dateAdded = 1690000000000L,
            lyrics = "Cruising through the cosmic skies...\nStarlight shining in your eyes...\nNebula clouds of green and blue...\nI am dreaming of the universe and you...\n\nEvery pulse within the sync\nCloser than we dare to think...",
            customCover = "gradient_starlight",
            playCount = 12,
            isHidden = false,
            isFavorite = true
        ),
        Song(
            path = "PRELOADED_DEEP_WATERS",
            title = "Deep Waters",
            artist = "Oceanic Whispers",
            album = "Abyssal Sounds",
            genre = "Ambient Chillout",
            duration = 182000,
            size = 4200000,
            year = "2023",
            folder = "Epic Player Preloads",
            dateAdded = 1691000000000L,
            lyrics = "Down where the warm coral glows...\nQuiet tide forever flows...\nSubmarine peace, no more noise...\nSinking deep, finding my poise...\n\nLet the silence heal the weight of the day.",
            customCover = "gradient_deepwater",
            playCount = 8,
            isHidden = false,
            isFavorite = false
        ),
        Song(
            path = "PRELOADED_TECHNO_GRID",
            title = "Techno Grid",
            artist = "Code Breaker",
            album = "Digital Era",
            genre = "Electronic",
            duration = 120000,
            size = 2800000,
            year = "2025",
            folder = "Epic Player Preloads",
            dateAdded = 1692000000000L,
            lyrics = "Zeros and ones on the computer screen...\nFastest beat you've ever seen...\nBass line running through your head...\nGrid is alive, paint it red...\n\nLooping the cycle, breaking the mold.",
            customCover = "gradient_technogrid",
            playCount = 19,
            isHidden = false,
            isFavorite = false
        ),
        Song(
            path = "PRELOADED_PERSIAN_ROSE",
            title = "Persian Rose (گل سرخ)",
            artist = "Farshad (فرشاد)",
            album = "Shiraz Gardens (باغ شیراز)",
            genre = "Fusion Traditional (سنتی)",
            duration = 210000,
            size = 5100000,
            year = "2024",
            folder = "Iranian Fusion",
            dateAdded = 1693000000000L,
            lyrics = "بوی گل سرخ و ترانه آب...\nعاشق خسته در حالت خواب...\nحافظ شیراز و عطر بهار نارنج...\nدل بردی از من به گوشه دنج...\n\nصدای تار و تنقل سنتور\nبرده هوشم به روز پر نور...",
            customCover = "gradient_persianrose",
            playCount = 25,
            isHidden = false,
            isFavorite = true
        ),
        Song(
            path = "PRELOADED_AZERI_BREEZE",
            title = "Azeri Breeze (Xəzər Küləyi)",
            artist = "Elnur (ائلنور)",
            album = "Caspian Shore (Xəzər Sahili)",
            genre = "Azerbaijani Folk Modern",
            duration = 165000,
            size = 3900000,
            year = "2023",
            folder = "Azerbaijani Folk",
            dateAdded = 1694000000000L,
            lyrics = "Xəzər dənizinin gözəl küləyi...\nSən oldun bu canımın tək diləyi...\nUlduzlar səmada sənə parlayır...\nMusiqi ruhumu sakit eyləyir...\n\nAzərbaycan diyarından səs gəlir\nGözəl nəğmələr könlümü fəth edir...",
            customCover = "gradient_azeribreeze",
            playCount = 15,
            isHidden = false,
            isFavorite = false
        )
    )

    /**
     * Scan storage for files and merge with database overrides
     */
    fun getSongsFlow(): Flow<List<Song>> {
        return songOverrides.map { overrides ->
            val localSongs = scanDeviceStorage()
            val overridesMap = overrides.associateBy { it.songPath }

            // Combine local files with overrides
            val mergedList = ArrayList<Song>()

            // Add local songs first (or merge if custom fields exist)
            for (local in localSongs) {
                val override = overridesMap[local.path]
                if (override != null) {
                    mergedList.add(
                        local.copy(
                            title = override.title ?: local.title,
                            artist = override.artist ?: local.artist,
                            album = override.album ?: local.album,
                            genre = override.genre ?: local.genre,
                            year = override.year ?: local.year,
                            lyrics = override.lyrics ?: local.lyrics,
                            customCover = override.customCover ?: local.customCover,
                            playCount = override.playCount,
                            isHidden = override.isHidden,
                            isFavorite = override.isFavorite
                        )
                    )
                } else {
                    mergedList.add(local)
                }
            }

            // Always bundle preloaded songs so that the player can be used and heard instantly
            for (preloaded in preloadedTracks) {
                // If the preloaded song has a local override in Room db, merge it!
                val override = overridesMap[preloaded.path]
                if (override != null) {
                    mergedList.add(
                        preloaded.copy(
                            title = override.title ?: preloaded.title,
                            artist = override.artist ?: preloaded.artist,
                            album = override.album ?: preloaded.album,
                            genre = override.genre ?: preloaded.genre,
                            year = override.year ?: preloaded.year,
                            lyrics = override.lyrics ?: preloaded.lyrics,
                            customCover = override.customCover ?: preloaded.customCover,
                            playCount = override.playCount,
                            isHidden = override.isHidden,
                            isFavorite = override.isFavorite
                        )
                    )
                } else {
                    mergedList.add(preloaded)
                }
            }

            mergedList
        }
    }

    private fun scanDeviceStorage(): List<Song> {
        val songList = mutableListOf<Song>()
        val resolver: ContentResolver = context.contentResolver
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED
        )

        // Query only files that are music or audio
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        
        try {
            val cursor = resolver.query(uri, projection, selection, null, "${MediaStore.Audio.Media.DATE_ADDED} DESC")
            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dateAddedCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (c.moveToNext()) {
                    val filePath = c.getString(dataCol) ?: ""
                    val file = File(filePath)
                    val ext = file.extension.lowercase()
                    
                    // Filter matching standard music player formats requested by the user
                    if (filePath.isNotEmpty() && (ext in extensionsToScan || ext.isEmpty())) {
                        val id = c.getLong(idCol)
                        val title = c.getString(titleCol) ?: file.nameWithoutExtension
                        val artist = c.getString(artistCol) ?: "<Unknown Artist>"
                        val album = c.getString(albumCol) ?: "<Unknown Album>"
                        val duration = c.getLong(durationCol)
                        val size = c.getLong(sizeCol)
                        val dateAdded = c.getLong(dateAddedCol) * 1000L // convert sec to ms
                        
                        val folder = file.parentFile?.name ?: "Root"

                        songList.add(
                            Song(
                                path = filePath,
                                title = title,
                                artist = artist,
                                album = album,
                                genre = "Local Audio",
                                duration = if (duration <= 0) 120000L else duration, // default 2 min if unavailable
                                size = size,
                                year = "Local",
                                folder = folder,
                                dateAdded = dateAdded,
                                lyrics = null,
                                customCover = null,
                                playCount = 0,
                                isHidden = false,
                                isFavorite = false
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error scanning device storage: ${e.message}")
        }
        return songList
    }

    // Settings
    suspend fun savePlayerSettings(settings: PlayerSettingEntity) = withContext(Dispatchers.IO) {
        musicDao.insertPlayerSettings(settings)
    }

    suspend fun getPlayerSettings(): PlayerSettingEntity = withContext(Dispatchers.IO) {
        musicDao.getPlayerSettings() ?: PlayerSettingEntity()
    }

    // Overrides (custom cover, tags, hidden, favorite, play count, lyrics)
    suspend fun saveSongOverride(override: SongOverrideEntity) = withContext(Dispatchers.IO) {
        musicDao.insertSongOverride(override)
    }

    suspend fun updateSongTags(
        songPath: String,
        title: String,
        artist: String,
        album: String,
        genre: String,
        year: String
    ) = withContext(Dispatchers.IO) {
        val existing = musicDao.getSongOverride(songPath) ?: SongOverrideEntity(songPath = songPath)
        musicDao.insertSongOverride(
            existing.copy(
                title = title,
                artist = artist,
                album = album,
                genre = genre,
                year = year
            )
        )
    }

    suspend fun updateSongCover(songPath: String, coverUri: String) = withContext(Dispatchers.IO) {
        val existing = musicDao.getSongOverride(songPath) ?: SongOverrideEntity(songPath = songPath)
        musicDao.insertSongOverride(existing.copy(customCover = coverUri))
    }

    suspend fun updateLyrics(songPath: String, lyrics: String) = withContext(Dispatchers.IO) {
        val existing = musicDao.getSongOverride(songPath) ?: SongOverrideEntity(songPath = songPath)
        musicDao.insertSongOverride(existing.copy(lyrics = lyrics))
    }

    suspend fun toggleFavorite(songPath: String, isFav: Boolean) = withContext(Dispatchers.IO) {
        val existing = musicDao.getSongOverride(songPath) ?: SongOverrideEntity(songPath = songPath)
        musicDao.insertSongOverride(existing.copy(isFavorite = isFav))
    }

    suspend fun toggleHideSong(songPath: String, isHidden: Boolean) = withContext(Dispatchers.IO) {
        val existing = musicDao.getSongOverride(songPath) ?: SongOverrideEntity(songPath = songPath)
        musicDao.insertSongOverride(existing.copy(isHidden = isHidden))
    }

    suspend fun incrementPlayCount(songPath: String) = withContext(Dispatchers.IO) {
        val existing = musicDao.getSongOverride(songPath) ?: SongOverrideEntity(songPath = songPath)
        musicDao.insertSongOverride(existing.copy(playCount = existing.playCount + 1))
    }

    // Playlists API
    suspend fun createPlaylist(name: String, coverPath: String? = null): Long = withContext(Dispatchers.IO) {
        musicDao.insertPlaylist(PlaylistEntity(name = name, customCover = coverPath))
    }

    suspend fun updatePlaylistCover(playlistId: Int, coverPath: String) = withContext(Dispatchers.IO) {
        // Since we need to update, scan existing
        val playlistsList = musicDao.getAllPlaylists().firstOrNull() ?: emptyList()
        val playlist = playlistsList.find { it.id == playlistId }
        if (playlist != null) {
            musicDao.updatePlaylist(playlist.copy(customCover = coverPath))
        }
    }

    suspend fun renamePlaylist(playlistId: Int, newName: String) = withContext(Dispatchers.IO) {
        val playlistsList = musicDao.getAllPlaylists().firstOrNull() ?: emptyList()
        val playlist = playlistsList.find { it.id == playlistId }
        if (playlist != null) {
            musicDao.updatePlaylist(playlist.copy(name = newName))
        }
    }

    suspend fun deletePlaylist(playlistId: Int) = withContext(Dispatchers.IO) {
        musicDao.deletePlaylist(playlistId)
        musicDao.clearSongsFromPlaylist(playlistId)
    }

    fun getPlaylistSongs(playlistId: Int): Flow<List<PlaylistSongEntity>> {
        return musicDao.getSongsForPlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Int, songPath: String) = withContext(Dispatchers.IO) {
        // Check list size
        val existing = musicDao.getSongsForPlaylist(playlistId).firstOrNull() ?: emptyList()
        val nextOrder = existing.size
        // Prevent duplicate songs in same playlist if preferred, but usually duplicates are allowed or skipped
        if (!existing.any { it.songPath == songPath }) {
            musicDao.insertPlaylistSong(
                PlaylistSongEntity(
                    playlistId = playlistId,
                    songPath = songPath,
                    displayOrder = nextOrder
                )
            )
        }
    }

    suspend fun removeSongFromPlaylist(playlistId: Int, songPath: String) = withContext(Dispatchers.IO) {
        musicDao.deleteSongFromPlaylist(playlistId, songPath)
    }

    suspend fun reorderPlaylistSongs(playlistId: Int, reorderedPaths: List<String>) = withContext(Dispatchers.IO) {
        musicDao.clearSongsFromPlaylist(playlistId)
        reorderedPaths.forEachIndexed { index, path ->
            musicDao.insertPlaylistSong(
                PlaylistSongEntity(
                    playlistId = playlistId,
                    songPath = path,
                    displayOrder = index
                )
            )
        }
    }
}
