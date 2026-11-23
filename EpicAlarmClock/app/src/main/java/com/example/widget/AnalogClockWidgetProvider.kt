package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.Typeface
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.calendar.TripleCalendarHelper
import com.example.data.local.SettingsManager
import com.example.i18n.AppLanguage
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

class AnalogClockWidgetProvider : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        ClockWidgetUpdateService.start(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        ClockWidgetUpdateService.stop(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        if (appWidgetIds.isNotEmpty()) {
            ClockWidgetUpdateService.start(context)
        }
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_CLOCK || intent.action == Intent.ACTION_TIME_TICK || intent.action == Intent.ACTION_TIME_CHANGED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, AnalogClockWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_CLOCK = "com.example.widget.ACTION_UPDATE_CLOCK"

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, AnalogClockWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_CLOCK
            }
            context.sendBroadcast(intent)
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val settings = SettingsManager.getInstance(context).settings.value
            val activeAlert = com.example.service.AlarmAlertService.activeAlertFlow.value
            val isRinging = activeAlert != null

            val bitmap = createClockBitmap(context, settings, isRinging)

            val views = RemoteViews(context.packageName, R.layout.widget_analog_clock).apply {
                setImageViewBitmap(R.id.widget_clock_image, bitmap)

                if (isRinging) {
                    // Tap dismisses the active alarm directly
                    val dismissIntent = Intent(context, com.example.receiver.AlarmActionReceiver::class.java).apply {
                        action = com.example.receiver.AlarmActionReceiver.ACTION_DISMISS
                        putExtra("EXTRA_ALARM_ID", activeAlert.alarmId)
                        putExtra("EXTRA_IS_TIMER", activeAlert.isTimer)
                        putExtra("EXTRA_ALARM_LABEL", activeAlert.title)
                        putExtra("EXTRA_SOUND_ID", activeAlert.soundId)
                        putExtra("EXTRA_VIBRATION", activeAlert.vibration)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        8821,
                        dismissIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                } else {
                    // Launch MainActivity when tapped
                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        launchIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun createClockBitmap(context: Context, settings: com.example.data.local.AppSettings, isRinging: Boolean = false): Bitmap {
            val size = 520
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val centerX = size / 2f
            val centerY = size / 2f
            val radius = (size / 2f) - 24f

            val faceColorInt = settings.clockFaceColor.toInt()
            val hourColorInt = settings.hourHandColor.toInt()
            val minColorInt = settings.minuteHandColor.toInt()
            val secColorInt = settings.secondHandColor.toInt()
            val rawNumColor = settings.numberColor.toInt()

            val isBright = (Color.red(faceColorInt) * 0.299f + Color.green(faceColorInt) * 0.587f + Color.blue(faceColorInt) * 0.114f) > 150
            val edgeColor = if (isBright) {
                Color.rgb(
                    (Color.red(faceColorInt) * 0.88f).toInt(),
                    (Color.green(faceColorInt) * 0.88f).toInt(),
                    (Color.blue(faceColorInt) * 0.88f).toInt()
                )
            } else {
                Color.BLACK
            }

            val numColorInt = if (isBright && Color.red(rawNumColor) > 200 && Color.green(rawNumColor) > 200 && Color.blue(rawNumColor) > 200) {
                Color.rgb(15, 23, 42) // Slate-900 high contrast
            } else {
                rawNumColor
            }

            // 1. Dial Background with Gradient
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    centerX, centerY, radius,
                    intArrayOf(faceColorInt, faceColorInt, edgeColor),
                    floatArrayOf(0f, 0.75f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(centerX, centerY, radius, bgPaint)

            // 2. Outer Border Ring
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 6f
                shader = SweepGradient(
                    centerX, centerY,
                    intArrayOf(hourColorInt, minColorInt, secColorInt, hourColorInt),
                    null
                )
            }
            canvas.drawCircle(centerX, centerY, radius, borderPaint)

            // 3. Tick Marks
            val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }

            for (i in 0 until 60) {
                val angleRad = Math.toRadians((i * 6.0) - 90.0)
                val isMajor = i % 5 == 0
                val tickLength = if (isMajor) radius * 0.14f else radius * 0.06f

                tickPaint.strokeWidth = if (isMajor) 5f else 2.5f
                tickPaint.color = if (isMajor) hourColorInt else Color.argb(120, 200, 200, 200)

                val startX = (centerX + (radius - tickLength) * cos(angleRad)).toFloat()
                val startY = (centerY + (radius - tickLength) * sin(angleRad)).toFloat()
                val endX = (centerX + radius * cos(angleRad)).toFloat()
                val endY = (centerY + radius * sin(angleRad)).toFloat()

                canvas.drawLine(startX, startY, endX, endY, tickPaint)
            }

            // 4. Numbers (1 to 12) if enabled
            if (settings.showNumbers) {
                val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = numColorInt
                    textSize = radius * 0.16f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                val numRadius = radius * 0.70f
                for (hour in 1..12) {
                    val angle = Math.toRadians((hour * 30.0) - 90.0)
                    val x = (centerX + numRadius * cos(angle)).toFloat()
                    val y = (centerY + numRadius * sin(angle) + (numPaint.textSize / 3)).toFloat()
                    canvas.drawText(hour.toString(), x, y, numPaint)
                }
            }

            // 5. Current Time Hands
            val cal = Calendar.getInstance()
            val hours = cal.get(Calendar.HOUR)
            val minutes = cal.get(Calendar.MINUTE)
            val seconds = cal.get(Calendar.SECOND)

            // Hour Hand
            val hourAngle = ((hours % 12) + minutes / 60f) * 30.0 - 90.0
            val hourRad = Math.toRadians(hourAngle)
            val hourLen = radius * 0.50f
            val handHourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = hourColorInt
                strokeWidth = 12f
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawLine(
                (centerX - radius * 0.10f * cos(hourRad)).toFloat(),
                (centerY - radius * 0.10f * sin(hourRad)).toFloat(),
                (centerX + hourLen * cos(hourRad)).toFloat(),
                (centerY + hourLen * sin(hourRad)).toFloat(),
                handHourPaint
            )

            // Minute Hand
            val minAngle = (minutes + seconds / 60f) * 6.0 - 90.0
            val minRad = Math.toRadians(minAngle)
            val minLen = radius * 0.72f
            val handMinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = minColorInt
                strokeWidth = 8f
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawLine(
                (centerX - radius * 0.12f * cos(minRad)).toFloat(),
                (centerY - radius * 0.12f * sin(minRad)).toFloat(),
                (centerX + minLen * cos(minRad)).toFloat(),
                (centerY + minLen * sin(minRad)).toFloat(),
                handMinPaint
            )

            // Second Hand
            val secAngle = seconds * 6.0 - 90.0
            val secRad = Math.toRadians(secAngle)
            val secLen = radius * 0.85f
            val handSecPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = secColorInt
                strokeWidth = 4.5f
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawLine(
                (centerX - radius * 0.18f * cos(secRad)).toFloat(),
                (centerY - radius * 0.18f * sin(secRad)).toFloat(),
                (centerX + secLen * cos(secRad)).toFloat(),
                (centerY + secLen * sin(secRad)).toFloat(),
                handSecPaint
            )

            // Center Pin
            val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = secColorInt
            }
            canvas.drawCircle(centerX, centerY, 10f, pinPaint)

            val triple = TripleCalendarHelper.getTodayTripleDate(AppLanguage.PERSIAN)

            // 6. Ringing Alarm Indicator Badge if currently active
            if (isRinging) {
                val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFF1744.toInt()
                    style = Paint.Style.FILL
                }
                val badgeStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                }
                val badgeRect = android.graphics.RectF(centerX - 68f, centerY - (radius * 0.58f) - 18f, centerX + 68f, centerY - (radius * 0.58f) + 18f)
                canvas.drawRoundRect(badgeRect, 18f, 18f, badgePaint)
                canvas.drawRoundRect(badgeRect, 18f, 18f, badgeStroke)

                val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 17f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                canvas.drawText("🚨 قطع زنگ", centerX, centerY - (radius * 0.58f) + 6f, badgeTextPaint)
            }

            // 7. Weekday Display if enabled (e.g. جمعه / چهارشنبه)
            if (settings.widgetAnalogShowWeekday) {
                val weekdayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = hourColorInt
                    textSize = 22f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                val weekdayY = if (isRinging) centerY - (radius * 0.32f) else centerY - (radius * 0.42f)
                canvas.drawText(triple.weekdayName, centerX, weekdayY, weekdayPaint)
            }

            // 8. Date Display (Under center or above bottom) if enabled
            val datesToDisplay = mutableListOf<String>()
            if (settings.widgetAnalogShowSolar) {
                datesToDisplay.add(triple.solarFormatted)
            }
            if (settings.widgetAnalogShowGregorian) {
                datesToDisplay.add(triple.gregorianFormatted)
            }
            if (settings.widgetAnalogShowLunar) {
                datesToDisplay.add(triple.lunarFormatted)
            }

            if (datesToDisplay.isNotEmpty()) {
                val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = numColorInt
                    textSize = if (datesToDisplay.size > 1) 17f else 22f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                val startY = if (datesToDisplay.size == 1) {
                    centerY + (radius * 0.42f)
                } else if (datesToDisplay.size == 2) {
                    centerY + (radius * 0.36f)
                } else {
                    centerY + (radius * 0.30f)
                }
                val lineSpacing = 20f
                datesToDisplay.forEachIndexed { idx, dStr ->
                    canvas.drawText(dStr, centerX, startY + (idx * lineSpacing), datePaint)
                }
            }

            return bitmap
        }
    }
}
