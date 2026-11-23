package com.example.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object ApkShareHelper {

    /**
     * Extracts and shares the installed application APK file through standard Android share sheet.
     */
    suspend fun shareAppApk(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val packageManager = context.packageManager
                val appInfo = packageManager.getApplicationInfo(context.packageName, 0)
                val originalApk = File(appInfo.publicSourceDir)

                if (!originalApk.exists() || !originalApk.canRead()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "فایل بسته نصبی برنامه در دسترس نیست", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }

                // Prepare clean destination in app cache directory
                val shareDir = File(context.cacheDir, "apk_share")
                if (!shareDir.exists()) {
                    shareDir.mkdirs()
                }

                val sharedApk = File(shareDir, "EpicAlarmClock.apk")
                if (sharedApk.exists()) {
                    sharedApk.delete()
                }

                // Copy source APK to shareable cache location
                FileInputStream(originalApk).use { input ->
                    FileOutputStream(sharedApk).use { output ->
                        input.copyTo(output)
                    }
                }

                val authority = "${context.packageName}.fileprovider"
                val apkUri = FileProvider.getUriForFile(context, authority, sharedApk)

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.android.package-archive"
                    putExtra(Intent.EXTRA_STREAM, apkUri)
                    putExtra(Intent.EXTRA_SUBJECT, "ساعت هشدار اپیک - بسته نصبی (APK)")
                    putExtra(Intent.EXTRA_TEXT, "اپلیکیشن پیشرفته «ساعت هشدار اپیک» با امکانات تقویم‌های سه‌گانه، ساعت جهانی، شیفت کاری و قفل امنیتی")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(shareIntent, "ارسال و اشتراک‌گذاری فایل APK ساعت هشدار اپیک با:")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                withContext(Dispatchers.Main) {
                    context.startActivity(chooser)
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "خطا در آماده‌سازی فایل APK: ${e.localizedMessage ?: "نامشخص"}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
