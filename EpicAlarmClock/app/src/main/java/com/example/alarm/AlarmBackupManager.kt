package com.example.alarm

import android.content.Context
import android.net.Uri
import com.example.data.model.AlarmEntity
import org.json.JSONArray
import org.json.JSONObject

object AlarmBackupManager {

    private const val BACKUP_FORMAT_TAG = "EpicAlarmClock"
    private const val BACKUP_VERSION = 1

    fun serializeAlarms(alarms: List<AlarmEntity>): String {
        val root = JSONObject().apply {
            put("app", BACKUP_FORMAT_TAG)
            put("version", BACKUP_VERSION)
            put("exportedAt", System.currentTimeMillis())
        }
        val array = JSONArray()
        alarms.forEach { alarm ->
            val obj = JSONObject().apply {
                put("hour", alarm.hour)
                put("minute", alarm.minute)
                put("isEnabled", alarm.isEnabled)
                put("label", alarm.label)
                put("soundId", alarm.soundId)
                put("vibrationPattern", alarm.vibrationPattern)
                put("autoDeleteAfterUse", alarm.autoDeleteAfterUse)
                put("repeatType", alarm.repeatType)
                put("customDays", alarm.customDays)
                put("shiftCycleDays", alarm.shiftCycleDays)
                put("shiftIntervalValue", alarm.shiftIntervalValue)
                put("shiftIntervalUnit", alarm.shiftIntervalUnit)
                put("shiftStartHour", alarm.shiftStartHour)
                put("shiftStartMinute", alarm.shiftStartMinute)
                put("shiftAnchorCalendar", alarm.shiftAnchorCalendar)
                put("shiftAnchorYear", alarm.shiftAnchorYear)
                put("shiftAnchorMonth", alarm.shiftAnchorMonth)
                put("shiftAnchorDay", alarm.shiftAnchorDay)
                put("shiftTimesJson", alarm.shiftTimesJson)
                put("eventCalendar", alarm.eventCalendar)
                put("eventYear", alarm.eventYear)
                put("eventMonth", alarm.eventMonth)
                put("eventDay", alarm.eventDay)
                put("isMeeting", alarm.isMeeting)
                put("eventNote", alarm.eventNote)
                if (alarm.customSoundTitle != null) {
                    put("customSoundTitle", alarm.customSoundTitle)
                }
                put("orderIndex", alarm.orderIndex)
                put("skippedUntilMillis", alarm.skippedUntilMillis)
            }
            array.put(obj)
        }
        root.put("alarms", array)
        return root.toString(2)
    }

    fun deserializeAlarms(jsonString: String): List<AlarmEntity> {
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) return emptyList()

        val array: JSONArray = if (trimmed.startsWith("[")) {
            JSONArray(trimmed)
        } else {
            val root = JSONObject(trimmed)
            if (!root.has("alarms")) {
                throw IllegalArgumentException("Invalid backup file: missing alarms array")
            }
            root.getJSONArray("alarms")
        }

        val result = mutableListOf<AlarmEntity>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val hour = obj.optInt("hour", 7).coerceIn(0, 23)
            val minute = obj.optInt("minute", 0).coerceIn(0, 59)
            val customTitle = if (obj.isNull("customSoundTitle")) null else obj.optString("customSoundTitle", "").takeIf { it.isNotBlank() }

            result.add(
                AlarmEntity(
                    id = 0L, // Reset ID so Room auto-generates cleanly
                    hour = hour,
                    minute = minute,
                    isEnabled = obj.optBoolean("isEnabled", true),
                    label = obj.optString("label", ""),
                    soundId = obj.optString("soundId", "cyber_pulse"),
                    vibrationPattern = obj.optString("vibrationPattern", "INTERMITTENT"),
                    autoDeleteAfterUse = obj.optBoolean("autoDeleteAfterUse", false),
                    repeatType = obj.optString("repeatType", "DAILY"),
                    customDays = obj.optString("customDays", "SAT,SUN,MON,TUE,WED,THU,FRI"),
                    shiftCycleDays = obj.optInt("shiftCycleDays", 3),
                    shiftIntervalValue = obj.optInt("shiftIntervalValue", 1).coerceAtLeast(1),
                    shiftIntervalUnit = obj.optString("shiftIntervalUnit", "DAYS"),
                    shiftStartHour = obj.optInt("shiftStartHour", hour).coerceIn(0, 23),
                    shiftStartMinute = obj.optInt("shiftStartMinute", minute).coerceIn(0, 59),
                    shiftAnchorCalendar = obj.optString("shiftAnchorCalendar", "SOLAR"),
                    shiftAnchorYear = obj.optInt("shiftAnchorYear", 1405),
                    shiftAnchorMonth = obj.optInt("shiftAnchorMonth", 1),
                    shiftAnchorDay = obj.optInt("shiftAnchorDay", 1),
                    shiftTimesJson = obj.optString("shiftTimesJson", "07:00,15:00,OFF"),
                    eventCalendar = obj.optString("eventCalendar", "SOLAR"),
                    eventYear = obj.optInt("eventYear", 0),
                    eventMonth = obj.optInt("eventMonth", 0),
                    eventDay = obj.optInt("eventDay", 0),
                    isMeeting = obj.optBoolean("isMeeting", false),
                    eventNote = obj.optString("eventNote", ""),
                    customSoundTitle = customTitle,
                    orderIndex = obj.optInt("orderIndex", i),
                    skippedUntilMillis = obj.optLong("skippedUntilMillis", 0L)
                )
            )
        }
        return result
    }

    fun exportAlarmsToUri(context: Context, uri: Uri, alarms: List<AlarmEntity>): Result<Int> {
        return try {
            val json = serializeAlarms(alarms)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(json.toByteArray(Charsets.UTF_8))
                out.flush()
            } ?: return Result.failure(IllegalStateException("Unable to open output stream"))
            Result.success(alarms.size)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    fun importAlarmsFromUri(context: Context, uri: Uri): Result<List<AlarmEntity>> {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader(Charsets.UTF_8).readText()
            } ?: return Result.failure(IllegalStateException("Unable to read backup file"))
            val parsed = deserializeAlarms(content)
            Result.success(parsed)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
