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
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.calendar.TripleCalendarHelper
import com.example.data.local.SettingsManager
import com.example.i18n.AppLanguage
import com.example.i18n.Strings

class TripleDateWidgetProvider : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        updateAllWidgets(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == Intent.ACTION_DATE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_TIME_TICK ||
            action == ACTION_UPDATE_TRIPLE_DATE_WIDGET ||
            action == AppWidgetManager.ACTION_APPWIDGET_UPDATE
        ) {
            updateAllWidgets(context)
        }
    }

    companion object {
        const val ACTION_UPDATE_TRIPLE_DATE_WIDGET = "com.example.widget.UPDATE_TRIPLE_DATE_WIDGET"

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, TripleDateWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                if (appWidgetIds.isNotEmpty()) {
                    for (id in appWidgetIds) {
                        updateAppWidget(context, appWidgetManager, id)
                    }
                }
            } catch (_: Throwable) {}
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            try {
                val settingsManager = SettingsManager.getInstance(context)
                val settings = settingsManager.getSettings()
                val language = settingsManager.getLanguage()

                val triple = TripleCalendarHelper.getTodayTripleDate(language)
                val views = RemoteViews(context.packageName, R.layout.widget_triple_date)

                // Dynamic Background Image with opacity & color
                val bgBitmap = createWidgetBgBitmap(
                    bgColorLong = settings.widgetTripleBgColor,
                    opacity = settings.widgetTripleBgOpacity
                )
                views.setImageViewBitmap(R.id.widget_triple_bg, bgBitmap)

                // 0. Big Digital Clock toggle & color
                if (settings.widgetTripleShowDigitalClock) {
                    views.setViewVisibility(R.id.widget_digital_clock_container, View.VISIBLE)
                    views.setTextColor(R.id.widget_text_clock, settings.widgetTripleClockColor.toInt())
                } else {
                    views.setViewVisibility(R.id.widget_digital_clock_container, View.GONE)
                }

                // Set Weekday header & title
                views.setTextViewText(R.id.widget_weekday_text, triple.weekdayName)
                views.setTextColor(R.id.widget_weekday_text, settings.widgetTripleClockColor.toInt())
                val headerTitle = when (language) {
                    AppLanguage.ENGLISH -> "Triple Calendar"
                    AppLanguage.ARABIC -> "التقويم الثلاثي"
                    AppLanguage.AZERBAIJANI -> "Üçlü Təqvim"
                    else -> "تقویم سه‌گانه"
                }
                views.setTextViewText(R.id.widget_header_title, headerTitle)

                // Labels localized & colored
                val solarLabel = when (language) {
                    AppLanguage.ENGLISH -> "Solar:"
                    AppLanguage.ARABIC -> "شمسي:"
                    AppLanguage.AZERBAIJANI -> "Şəmsi:"
                    else -> "شمسی:"
                }
                val gregLabel = when (language) {
                    AppLanguage.ENGLISH -> "Gregorian:"
                    AppLanguage.ARABIC -> "ميلادي:"
                    AppLanguage.AZERBAIJANI -> "Miladi:"
                    else -> "میلادی:"
                }
                val lunarLabel = when (language) {
                    AppLanguage.ENGLISH -> "Lunar:"
                    AppLanguage.ARABIC -> "قمري:"
                    AppLanguage.AZERBAIJANI -> "Qəməri:"
                    else -> "قمری:"
                }

                views.setTextViewText(R.id.widget_solar_label, solarLabel)
                views.setTextColor(R.id.widget_solar_label, settings.widgetTripleSolarColor.toInt())
                views.setTextViewText(R.id.widget_gregorian_label, gregLabel)
                views.setTextColor(R.id.widget_gregorian_label, settings.widgetTripleGregorianColor.toInt())
                views.setTextViewText(R.id.widget_lunar_label, lunarLabel)
                views.setTextColor(R.id.widget_lunar_label, settings.widgetTripleLunarColor.toInt())

                // 1. Solar Hijri
                if (settings.widgetTripleShowSolar) {
                    views.setViewVisibility(R.id.widget_solar_row, View.VISIBLE)
                    val solarText = "${Strings.formatNumber(triple.solarDay, language)} ${triple.solarMonthName} ${Strings.formatNumber(triple.solarYear, language)}"
                    views.setTextViewText(R.id.widget_solar_text, solarText)
                    views.setTextColor(R.id.widget_solar_text, settings.widgetTripleSolarColor.toInt())
                } else {
                    views.setViewVisibility(R.id.widget_solar_row, View.GONE)
                }

                // 2. Gregorian
                if (settings.widgetTripleShowGregorian) {
                    views.setViewVisibility(R.id.widget_gregorian_row, View.VISIBLE)
                    val gregText = "${Strings.formatNumber(triple.gregorianDay, language)} ${triple.gregorianMonthName} ${Strings.formatNumber(triple.gregorianYear, language)}"
                    views.setTextViewText(R.id.widget_gregorian_text, gregText)
                    views.setTextColor(R.id.widget_gregorian_text, settings.widgetTripleGregorianColor.toInt())
                } else {
                    views.setViewVisibility(R.id.widget_gregorian_row, View.GONE)
                }

                // 3. Islamic Lunar
                if (settings.widgetTripleShowLunar) {
                    views.setViewVisibility(R.id.widget_lunar_row, View.VISIBLE)
                    val lunarText = "${Strings.formatNumber(triple.lunarDay, language)} ${triple.lunarMonthName} ${Strings.formatNumber(triple.lunarYear, language)}"
                    views.setTextViewText(R.id.widget_lunar_text, lunarText)
                    views.setTextColor(R.id.widget_lunar_text, settings.widgetTripleLunarColor.toInt())
                } else {
                    views.setViewVisibility(R.id.widget_lunar_row, View.GONE)
                }

                // Launch MainActivity on click
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                val pendingIntent = PendingIntent.getActivity(context, 102, intent, flags)
                views.setOnClickPendingIntent(R.id.widget_triple_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (_: Throwable) {}
        }

        private fun createWidgetBgBitmap(bgColorLong: Long, opacity: Float): Bitmap {
            val width = 480
            val height = 300
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val alphaInt = (opacity.coerceIn(0f, 1f) * 255).toInt()
            val rgb = (bgColorLong.toInt() and 0x00FFFFFF)
            val colorWithAlpha = (alphaInt shl 24) or rgb

            val radius = 32f
            if (alphaInt > 0) {
                val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = colorWithAlpha
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, fillPaint)

                // Draw subtle border
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb((alphaInt * 0.35f).toInt(), 255, 255, 255)
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                }
                canvas.drawRoundRect(1.5f, 1.5f, width - 1.5f, height - 1.5f, radius, radius, strokePaint)
            }

            return bitmap
        }
    }
}
