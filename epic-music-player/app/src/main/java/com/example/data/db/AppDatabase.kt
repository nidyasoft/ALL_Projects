package com.example.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// 1. Entities
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val customCover: String? = null
)

@Entity(tableName = "playlist_songs")
data class PlaylistSongEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playlistId: Int,
    val songPath: String,
    val displayOrder: Int
)

@Entity(tableName = "song_overrides")
data class SongOverrideEntity(
    @PrimaryKey val songPath: String,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val genre: String? = null,
    val year: String? = null,
    val lyrics: String? = null,
    val customCover: String? = null,
    val playCount: Int = 0,
    val isHidden: Boolean = false,
    val isFavorite: Boolean = false
)

@Entity(tableName = "player_settings")
data class PlayerSettingEntity(
    @PrimaryKey val id: Int = 1,
    val themeIndex: Int = 4, // Theme 5 (System Auto) is index 4
    val languageCode: String = "fa", // default Persian
    val lastPlayedSongPath: String? = null,
    val lastPlayedPosition: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val crossfadeEnabled: Boolean = false,
    val crossfadeDurationSec: Int = 3,
    val keepScreenOn: Boolean = false,
    val headsetDisconnectPause: Boolean = true,
    val eqEnabled: Boolean = false,
    val eqBand0: Int = 0,
    val eqBand1: Int = 0,
    val eqBand2: Int = 0,
    val eqBand3: Int = 0,
    val eqBand4: Int = 0,
    val eqBassBoost: Int = 0,
    val eqVirtualizer: Int = 0,
    val sortBy: String = "DATE_ADDED",
    val sortAscending: Boolean = false,
    val subThemeIndex: Int = 5
)

// 2. DAOs
@Dao
interface MusicDao {
    // Playlists
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Int)

    // Playlist songs
    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId ORDER BY displayOrder ASC")
    fun getSongsForPlaylist(playlistId: Int): Flow<List<PlaylistSongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSong(playlistSong: PlaylistSongEntity): Long

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songPath = :songPath")
    suspend fun deleteSongFromPlaylist(playlistId: Int, songPath: String)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearSongsFromPlaylist(playlistId: Int)

    // Song Overrides (tag edits, covers, lyrics, favorite, hidden, play count)
    @Query("SELECT * FROM song_overrides")
    fun getAllSongOverrides(): Flow<List<SongOverrideEntity>>

    @Query("SELECT * FROM song_overrides WHERE songPath = :songPath LIMIT 1")
    suspend fun getSongOverride(songPath: String): SongOverrideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongOverride(override: SongOverrideEntity)

    // Player Settings
    @Query("SELECT * FROM player_settings WHERE id = 1 LIMIT 1")
    fun getPlayerSettingsFlow(): Flow<PlayerSettingEntity?>

    @Query("SELECT * FROM player_settings WHERE id = 1 LIMIT 1")
    suspend fun getPlayerSettings(): PlayerSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerSettings(settings: PlayerSettingEntity)
}

// 3. AppDatabase
@Database(
    entities = [PlaylistEntity::class, PlaylistSongEntity::class, SongOverrideEntity::class, PlayerSettingEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val musicDao: MusicDao
}
