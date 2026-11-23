package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.example.data.model.Song
import com.example.ui.viewmodel.MusicViewModel

class PlaybackService : Service() {

    private var mediaSession: MediaSession? = null

    companion object {
        const val CHANNEL_ID = "epic_music_channel"
        const val NOTIFICATION_ID = 101

        const val ACTION_PLAY = "com.example.action.PLAY"
        const val ACTION_PAUSE = "com.example.action.PAUSE"
        const val ACTION_PREVIOUS = "com.example.action.PREVIOUS"
        const val ACTION_NEXT = "com.example.action.NEXT"
        const val ACTION_STOP = "com.example.action.STOP"

        fun startService(context: Context) {
            val intent = Intent(context, PlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, PlaybackService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupMediaSession()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                ACTION_PLAY -> {
                    if (MusicViewModel.instance?.isPlaybackServiceAllowedToPlay() == true) {
                        MusicViewModel.instance?.resumeSong()
                    }
                }
                ACTION_PAUSE -> MusicViewModel.instance?.pauseSong()
                ACTION_NEXT -> MusicViewModel.instance?.nextSong()
                ACTION_PREVIOUS -> MusicViewModel.instance?.prevSong()
                ACTION_STOP -> {
                    MusicViewModel.instance?.pauseSong()
                    stopSelf()
                }
            }
        }
        updateNotificationAndSession()
        return START_STICKY
    }

    private fun setupMediaSession() {
        mediaSession = MediaSession(this, "EpicMusicPlayerSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                    val keyEvent = mediaButtonIntent.getParcelableExtra<android.view.KeyEvent>(Intent.EXTRA_KEY_EVENT)
                    if (keyEvent != null && keyEvent.action == android.view.KeyEvent.ACTION_DOWN) {
                        when (keyEvent.keyCode) {
                            android.view.KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                if (MusicViewModel.instance?.isPlaybackServiceAllowedToPlay() == true) {
                                    MusicViewModel.instance?.resumeSong()
                                    updateNotificationAndSession()
                                }
                                return true
                            }
                            android.view.KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                                MusicViewModel.instance?.pauseSong()
                                updateNotificationAndSession()
                                return true
                            }
                            android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, android.view.KeyEvent.KEYCODE_HEADSETHOOK -> {
                                val vm = MusicViewModel.instance
                                if (vm != null) {
                                    if (vm.isPlaying.value) {
                                        vm.pauseSong()
                                        updateNotificationAndSession()
                                    } else if (vm.isPlaybackServiceAllowedToPlay()) {
                                        vm.resumeSong()
                                        updateNotificationAndSession()
                                    }
                                }
                                return true
                            }
                            android.view.KeyEvent.KEYCODE_MEDIA_NEXT -> {
                                MusicViewModel.instance?.nextSong()
                                updateNotificationAndSession()
                                return true
                            }
                            android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                                MusicViewModel.instance?.prevSong()
                                updateNotificationAndSession()
                                return true
                            }
                        }
                    }
                    return super.onMediaButtonEvent(mediaButtonIntent)
                }

                override fun onPlay() {
                    if (MusicViewModel.instance?.isPlaybackServiceAllowedToPlay() == true) {
                        MusicViewModel.instance?.resumeSong()
                        updateNotificationAndSession()
                    }
                }

                override fun onPause() {
                    MusicViewModel.instance?.pauseSong()
                    updateNotificationAndSession()
                }

                override fun onSkipToNext() {
                    MusicViewModel.instance?.nextSong()
                    updateNotificationAndSession()
                }

                override fun onSkipToPrevious() {
                    MusicViewModel.instance?.prevSong()
                    updateNotificationAndSession()
                }

                override fun onStop() {
                    MusicViewModel.instance?.pauseSong()
                    stopSelf()
                }
            })
            isActive = true
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Epic Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls and displays playing background music"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun updateNotificationAndSession() {
        val vm = MusicViewModel.instance
        if (vm == null) {
            stopSelf()
            return
        }

        val currentSong = vm.currentQueue.value.getOrNull(vm.currentSongIndex.value)
        val isPlaying = vm.isPlaying.value

        if (currentSong == null) {
            stopSelf()
            return
        }

        // Update Media Session Playback State
        val speed = vm.playbackSpeed.value
        val progress = vm.playbackProgress.value
        val stateBuilder = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PAUSE or
                PlaybackState.ACTION_PLAY_PAUSE or
                PlaybackState.ACTION_SKIP_TO_NEXT or
                PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                PlaybackState.ACTION_STOP
            )
            .setState(
                if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
                progress,
                speed
            )
        mediaSession?.setPlaybackState(stateBuilder.build())

        // Update Metadata
        val metadataBuilder = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, currentSong.title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, currentSong.artist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, currentSong.album)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, currentSong.duration)

        val coverBitmap = getSongCoverBitmap(this, currentSong)
        if (coverBitmap != null) {
            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, coverBitmap)
        }
        mediaSession?.setMetadata(metadataBuilder.build())

        // Build Notification
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPauseIntent = Intent(this, PlaybackService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getService(
            this, 1, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = Intent(this, PlaybackService::class.java).apply { action = ACTION_PREVIOUS }
        val prevPendingIntent = PendingIntent.getService(
            this, 2, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = Intent(this, PlaybackService::class.java).apply { action = ACTION_NEXT }
        val nextPendingIntent = PendingIntent.getService(
            this, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, PlaybackService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this, 4, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }

        val playIconId = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }

        notificationBuilder
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(currentSong.title)
            .setContentText(currentSong.artist)
            .setSubText(currentSong.album)
            .setContentIntent(pendingIntent)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .addAction(
                Notification.Action.Builder(
                    android.R.drawable.ic_media_previous,
                    "Previous",
                    prevPendingIntent
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    playIconId,
                    if (isPlaying) "Pause" else "Play",
                    playPausePendingIntent
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    android.R.drawable.ic_media_next,
                    "Next",
                    nextPendingIntent
                ).build()
            )

        if (coverBitmap != null) {
            notificationBuilder.setLargeIcon(coverBitmap)
        }

        val notification = notificationBuilder.build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getSongCoverBitmap(context: Context, song: Song): Bitmap? {
        try {
            if (song.path.startsWith("PRELOADED_")) {
                val resourceName = song.customCover ?: "ic_launcher_background"
                val resId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
                if (resId != 0) {
                    return BitmapFactory.decodeResource(context.resources, resId)
                }
            } else if (song.customCover != null) {
                val f = java.io.File(song.customCover)
                if (f.exists()) {
                    return BitmapFactory.decodeFile(f.absolutePath)
                }
            } else if (song.path.startsWith("/")) {
                // Try reading embedded picture from physical file
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(song.path)
                val picture = retriever.embeddedPicture
                retriever.release()
                if (picture != null) {
                    return BitmapFactory.decodeByteArray(picture, 0, picture.size)
                }
            }
        } catch (e: Exception) {
            Log.e("PlaybackService", "Error decoding cover bitmap: ${e.message}")
        }
        return null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.release()
        mediaSession = null
    }
}
