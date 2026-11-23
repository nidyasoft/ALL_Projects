package com.example.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Song
import java.io.File

object FileShareUtils {
    fun shareSongs(context: Context, songs: List<Song>) {
        if (songs.isEmpty()) {
            Toast.makeText(context, "No songs selected to share (لیستی انتخاب نشده)", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val providerAuthority = "${context.packageName}.fileprovider"
            val uris = ArrayList<Uri>()

            for (song in songs) {
                val file = if (song.path.startsWith("PRELOADED_")) {
                    // Create a dummy shareable audio asset file in cache for the mock preloaded tracks
                    val cacheFile = File(context.cacheDir, "shared_preloaded_${song.title.replace(" ", "_")}.mp3")
                    if (!cacheFile.exists()) {
                        // Create a valid dummy size so media processors consider it complete
                        cacheFile.writeText("Dummy MP3 synthetic signal bytes for ${song.title} - ${song.artist}")
                    }
                    cacheFile
                } else {
                    File(song.path)
                }

                if (file.exists()) {
                    val uri = FileProvider.getUriForFile(context, providerAuthority, file)
                    uris.add(uri)
                } else {
                    Toast.makeText(context, "File not found: ${song.title}", Toast.LENGTH_SHORT).show()
                }
            }

            if (uris.isEmpty()) return

            val intent = if (uris.size == 1) {
                Intent(Intent.ACTION_SEND).apply {
                    type = "audio/*"
                    putExtra(Intent.EXTRA_STREAM, uris[0])
                    putExtra(Intent.EXTRA_SUBJECT, songs[0].title)
                    putExtra(Intent.EXTRA_TEXT, "Shared via Music Player: ${songs[0].title} - ${songs[0].artist}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else {
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "audio/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                    putExtra(Intent.EXTRA_SUBJECT, "Shared Songs")
                    putExtra(Intent.EXTRA_TEXT, "Shared ${songs.size} songs from Music Player.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }

            context.startActivity(Intent.createChooser(intent, "Share audio file(s) via"))

        } catch (e: java.lang.Exception) {
            Toast.makeText(context, "Error sharing file: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    fun shareAppApk(context: Context) {
        try {
            val apkPath = context.applicationInfo.publicSourceDir
            val srcFile = File(apkPath)
            
            if (!srcFile.exists()) {
                Toast.makeText(context, "APK file not found (فایل برنامه یافت نشد)", Toast.LENGTH_SHORT).show()
                return
            }

            val sharedApkDir = File(context.cacheDir, "shared_apk")
            if (!sharedApkDir.exists()) {
                sharedApkDir.mkdirs()
            }
            val destFile = File(sharedApkDir, "EpicMusicPlayer.apk")

            srcFile.inputStream().use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val providerAuthority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, providerAuthority, destFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Epic Music Player - APK")
                putExtra(Intent.EXTRA_TEXT, "Here is the Epic Music Player app (APK). Install and enjoy!")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share APK via"))

        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing APK: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}
