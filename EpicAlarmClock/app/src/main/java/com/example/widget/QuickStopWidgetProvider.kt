package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.receiver.AlarmActionReceiver
import com.example.service.AlarmAlertService

class QuickStopWidgetProvider : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        updateAllWidgets(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == ACTION_UPDATE_QUICK_STOP ||
            action == AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
            action == Intent.ACTION_TIME_TICK ||
            action == Intent.ACTION_TIME_CHANGED
        ) {
            updateAllWidgets(context)
        }
    }

    companion object {
        const val ACTION_UPDATE_QUICK_STOP = "com.example.widget.ACTION_UPDATE_QUICK_STOP"

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, QuickStopWidgetProvider::class.java))
                for (id in ids) {
                    updateWidget(context, appWidgetManager, id)
                }
            } catch (_: Throwable) {}
        }

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_stop)
            val activeAlert = AlarmAlertService.activeAlertFlow.value

            val isRinging = activeAlert != null

            // Generate customized dynamic background card
            val bgBitmap = createCardBackground(
                width = 240,
                height = 240,
                isRinging = isRinging
            )
            views.setImageViewBitmap(R.id.widget_quick_stop_bg, bgBitmap)

            if (isRinging) {
                views.setTextViewText(R.id.widget_quick_stop_text, "🚨 قطع آلارم\n(لمس کنید)")
                views.setTextColor(R.id.widget_quick_stop_text, 0xFFFFFFFF.toInt())
                views.setImageViewResource(R.id.widget_quick_stop_icon, R.drawable.ic_quick_stop_bell)

                // PendingIntent to Dismiss active alert immediately
                val dismissIntent = Intent(context, AlarmActionReceiver::class.java).apply {
                    action = AlarmActionReceiver.ACTION_DISMISS
                    putExtra("EXTRA_ALARM_ID", activeAlert.alarmId)
                    putExtra("EXTRA_IS_TIMER", activeAlert.isTimer)
                    putExtra("EXTRA_ALARM_LABEL", activeAlert.title)
                    putExtra("EXTRA_SOUND_ID", activeAlert.soundId)
                    putExtra("EXTRA_VIBRATION", activeAlert.vibration)
                }
                val pi = PendingIntent.getBroadcast(
                    context,
                    9911,
                    dismissIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_quick_stop_container, pi)
            } else {
                views.setTextViewText(R.id.widget_quick_stop_text, "ساعت اپیک\n(آماده)")
                views.setTextColor(R.id.widget_quick_stop_text, 0xFF94A3B8.toInt())
                views.setImageViewResource(R.id.widget_quick_stop_icon, R.drawable.ic_quick_stop_bell)

                // PendingIntent to open app
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pi = PendingIntent.getActivity(
                    context,
                    9912,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_quick_stop_container, pi)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun createCardBackground(width: Int, height: Int, isRinging: Boolean): Bitmap {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val rect = RectF(6f, 6f, width - 6f, height - 6f)
            val cornerRadius = 36f

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = if (isRinging) 0xFFFF1744.toInt() else 0xFF0F172A.toInt()
            }
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = if (isRinging) 6f else 3f
                color = if (isRinging) 0xFFFFFFFF.toInt() else 0xFF00F2FE.toInt()
            }
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, strokePaint)

            return bitmap
        }
    }
}
