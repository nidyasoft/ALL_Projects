package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

data class SoundOption(val id: String, val nameFa: String, val nameEn: String)

object SoundAndVibrationManager {

    val builtInSounds = listOf(
        SoundOption("cyber_pulse", "پالس سایبر (Cyber Pulse)", "Cyber Pulse"),
        SoundOption("cosmic_chime", "زنگ کیهانی (Cosmic Chime)", "Cosmic Chime"),
        SoundOption("gentle_sunrise", "طلوع آرامش‌بخش (Gentle Sunrise)", "Gentle Sunrise"),
        SoundOption("digital_beep", "بیپ دیجیتال اپیک (Digital Beep)", "Digital Beep"),
        SoundOption("epic_fanfare", "ملودی بیدارباش (Epic Fanfare)", "Epic Fanfare")
    )

    val soundOptions = builtInSounds

    private var audioTrack: AudioTrack? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    fun isAlarmPlaying(): Boolean = isPlaying

    private var initialVolume: Float = 0.85f
    private var activeVolume: Float = 0.85f
    private var isVolumeReduced: Boolean = false
    fun isVolumeReduced(): Boolean = isVolumeReduced

    fun reduceVolumeToQuarter() {
        if (!isPlaying || isVolumeReduced) return
        isVolumeReduced = true
        val reduced = (initialVolume * 0.25f).coerceAtLeast(0.05f)
        activeVolume = reduced
        try {
            mediaPlayer?.setVolume(reduced, reduced)
        } catch (_: Throwable) {}
    }

    private var playbackJob: Job? = null
    private var autoSilenceJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playAlarm(
        context: Context,
        soundIdOrUri: String,
        vibrationPattern: String,
        volume: Float = 0.85f,
        autoSilenceMinutes: Int = 5,
        onAutoSilenced: (() -> Unit)? = null
    ) {
        stopAlarm(context)
        isPlaying = true
        initialVolume = volume.coerceIn(0.05f, 1f)
        activeVolume = initialVolume
        isVolumeReduced = false
        startVibration(context, vibrationPattern, repeat = true)

        // Setup auto-silence timer if configured
        if (autoSilenceMinutes > 0) {
            autoSilenceJob = scope.launch {
                delay(autoSilenceMinutes * 60 * 1000L)
                if (isPlaying) {
                    stopAlarm(context)
                    onAutoSilenced?.invoke()
                }
            }
        }

        if (soundIdOrUri.startsWith("content://") || soundIdOrUri.startsWith("file://") || soundIdOrUri.startsWith("/") || soundIdOrUri.startsWith("android.resource://")) {
            // Play through MediaPlayer for custom audio files or ringtones
            playMediaUri(context, soundIdOrUri, volume)
        } else {
            // Synthesized tone
            playbackJob = scope.launch {
                while (isActive && isPlaying) {
                    generateAndPlayTone(soundIdOrUri, activeVolume)
                    delay(700)
                }
            }
        }
    }

    private fun playMediaUri(context: Context, soundUriStr: String, volume: Float) {
        try {
            val uri = Uri.parse(soundUriStr)
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                if (uri.scheme == "file" || soundUriStr.startsWith("/")) {
                    val filePath = if (uri.scheme == "file") uri.path ?: "" else soundUriStr
                    val file = java.io.File(filePath)
                    if (file.exists()) {
                        val fis = java.io.FileInputStream(file)
                        setDataSource(fis.fd)
                        fis.close()
                    } else {
                        setDataSource(context, uri)
                    }
                } else {
                    setDataSource(context, uri)
                }
                isLooping = true
                setVolume(volume.coerceIn(0f, 1f), volume.coerceIn(0f, 1f))
                prepare()
                start()
            }
            mediaPlayer = mp
        } catch (_: Throwable) {
            // Fallback to cyber_pulse synthesized tone if Uri fails
            playbackJob = scope.launch {
                while (isActive && isPlaying) {
                    generateAndPlayTone("cyber_pulse", activeVolume)
                    delay(700)
                }
            }
        }
    }

    fun stopAlarm(context: Context) {
        isPlaying = false
        autoSilenceJob?.cancel()
        autoSilenceJob = null
        playbackJob?.cancel()
        playbackJob = null

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Throwable) {}
        mediaPlayer = null

        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.release()
        } catch (_: Throwable) {}
        audioTrack = null

        stopVibration(context)
    }

    fun previewSound(soundId: String) {
        scope.launch {
            generateAndPlayTone(soundId, 0.85f)
        }
    }

    fun previewSound(context: Context, soundIdOrUri: String, volume: Float = 0.85f) {
        scope.launch {
            if (soundIdOrUri.startsWith("content://") || soundIdOrUri.startsWith("file://") || soundIdOrUri.startsWith("/") || soundIdOrUri.startsWith("android.resource://")) {
                try {
                    val uri = Uri.parse(soundIdOrUri)
                    val mp = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        if (uri.scheme == "file" || soundIdOrUri.startsWith("/")) {
                            val filePath = if (uri.scheme == "file") uri.path ?: "" else soundIdOrUri
                            val file = java.io.File(filePath)
                            if (file.exists()) {
                                val fis = java.io.FileInputStream(file)
                                setDataSource(fis.fd)
                                fis.close()
                            } else {
                                setDataSource(context, uri)
                            }
                        } else {
                            setDataSource(context, uri)
                        }
                        setVolume(volume.coerceIn(0f, 1f), volume.coerceIn(0f, 1f))
                        prepare()
                        start()
                    }
                    delay(3000)
                    try {
                        mp.stop()
                        mp.release()
                    } catch (_: Throwable) {}
                } catch (_: Throwable) {
                    generateAndPlayTone("cyber_pulse", volume)
                }
            } else {
                generateAndPlayTone(soundIdOrUri, volume)
            }
        }
    }

    private fun generateAndPlayTone(soundId: String, volume: Float = 0.85f) {
        try {
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (_: Throwable) {}
            audioTrack = null

            val sampleRate = 44100
            val frequencies = when (soundId) {
                "cyber_pulse" -> doubleArrayOf(587.33, 783.99, 880.00, 1174.66)
                "cosmic_chime" -> doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
                "gentle_sunrise" -> doubleArrayOf(440.0, 554.37, 659.25, 830.61)
                "digital_beep" -> doubleArrayOf(1000.0, 1200.0, 1000.0, 1200.0)
                else -> doubleArrayOf(440.0, 523.25, 659.25, 880.0)
            }

            val noteDuration = 0.18
            val totalSamples = (sampleRate * noteDuration * frequencies.size).toInt()
            val generatedSnd = ShortArray(totalSamples)

            val safeVolume = volume.coerceIn(0.05f, 1f)
            var idx = 0
            for (freq in frequencies) {
                val samplesPerNote = (sampleRate * noteDuration).toInt()
                for (i in 0 until samplesPerNote) {
                    val time = i.toDouble() / sampleRate
                    val envelope = kotlin.math.exp(-3.0 * (i.toDouble() / samplesPerNote))
                    val wave = sin(2.0 * Math.PI * freq * time)
                    val harmonic = 0.3 * sin(4.0 * Math.PI * freq * time)
                    val sample = ((wave + harmonic) * envelope * 0.7 * safeVolume * Short.MAX_VALUE).toInt()
                    if (idx < totalSamples) {
                        generatedSnd[idx++] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                }
            }

            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(totalSamples * 2)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(generatedSnd, 0, generatedSnd.size)
            track.play()
            audioTrack = track
        } catch (_: Throwable) {}
    }

    data class VibrationOption(val id: String, val nameFa: String, val nameEn: String, val descFa: String)

    val vibrationOptions = listOf(
        VibrationOption("CONTINUOUS", "مداوم (Continuous)", "Continuous", "لرزش یکنواخت و پیوسته"),
        VibrationOption("INTERMITTENT", "متناوب (Intermittent)", "Intermittent", "پالس‌های مقطع و تکرارشونده"),
        VibrationOption("HEARTBEAT", "ضربان قلب (Heartbeat)", "Heartbeat", "ریتم دوضربه مشابه طپش قلب"),
        VibrationOption("STROBE", "استروب تند (Strobe)", "Strobe", "پالس‌های بسیار سریع و هشداردهنده"),
        VibrationOption("OFF", "بدون لرزش (خاموش)", "Off", "فقط پخش صدا بدون ویبره")
    )

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Throwable) {
            try {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } catch (_: Throwable) {
                null
            }
        }
    }

    fun startVibration(context: Context, patternType: String, repeat: Boolean = true) {
        try {
            val vibrator = getVibrator(context) ?: return
            vibrator.cancel()
            if (patternType == "OFF") return

            val timings = when (patternType) {
                "CONTINUOUS" -> longArrayOf(0, 1000, 300)
                "INTERMITTENT" -> longArrayOf(0, 350, 200, 350, 200, 350, 600)
                "HEARTBEAT" -> longArrayOf(0, 160, 140, 340, 650)
                "STROBE" -> longArrayOf(0, 100, 80, 100, 80, 100, 80, 100, 80, 100, 400)
                else -> longArrayOf(0, 500, 250)
            }

            val repeatIndex = if (repeat) 0 else -1

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(timings, repeatIndex)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val attrs = android.os.VibrationAttributes.Builder()
                        .setUsage(android.os.VibrationAttributes.USAGE_ALARM)
                        .build()
                    vibrator.vibrate(effect, attrs)
                } else {
                    val audioAttrs = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    vibrator.vibrate(effect, audioAttrs)
                }
            } else {
                val audioAttrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, repeatIndex, audioAttrs)
            }
        } catch (_: Throwable) {
            try {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                v?.vibrate(longArrayOf(0, 500, 300), if (repeat) 0 else -1)
            } catch (_: Throwable) {}
        }
    }

    fun previewVibration(context: Context, patternType: String) {
        try {
            val vibrator = getVibrator(context) ?: return
            vibrator.cancel()
            if (patternType == "OFF") return

            val timings = when (patternType) {
                "CONTINUOUS" -> longArrayOf(0, 750)
                "INTERMITTENT" -> longArrayOf(0, 300, 150, 300, 150, 300)
                "HEARTBEAT" -> longArrayOf(0, 150, 120, 320, 450, 150, 120, 320)
                "STROBE" -> longArrayOf(0, 90, 70, 90, 70, 90, 70, 90, 70, 90)
                else -> longArrayOf(0, 500)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(timings, -1)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val attrs = android.os.VibrationAttributes.Builder()
                        .setUsage(android.os.VibrationAttributes.USAGE_ALARM)
                        .build()
                    vibrator.vibrate(effect, attrs)
                } else {
                    val audioAttrs = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    vibrator.vibrate(effect, audioAttrs)
                }
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, -1)
            }
        } catch (_: Throwable) {
            try {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                v?.vibrate(400)
            } catch (_: Throwable) {}
        }
    }

    fun stopVibration(context: Context) {
        try {
            val vibrator = getVibrator(context)
            vibrator?.cancel()
        } catch (_: Throwable) {}
    }
}
