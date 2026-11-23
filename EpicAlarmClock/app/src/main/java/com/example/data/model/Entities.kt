package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val label: String = "",
    val soundId: String = "cyber_pulse",
    val vibrationPattern: String = "INTERMITTENT",
    val autoDeleteAfterUse: Boolean = false,
    val repeatType: String = "DAILY", // ONCE, DAILY, SAT_THU, MON_FRI, CUSTOM, SHIFT, EVENT
    val customDays: String = "SAT,SUN,MON,TUE,WED,THU,FRI",
    val shiftCycleDays: Int = 3, // Legacy compatibility
    val shiftIntervalValue: Int = 1, // e.g. every N days / hours / minutes
    val shiftIntervalUnit: String = "DAYS", // "DAYS", "HOURS", "MINUTES"
    val shiftStartHour: Int = 8,
    val shiftStartMinute: Int = 0,
    val shiftAnchorCalendar: String = "SOLAR", // SOLAR, GREGORIAN, LUNAR
    val shiftAnchorYear: Int = 1405,
    val shiftAnchorMonth: Int = 1,
    val shiftAnchorDay: Int = 1,
    val shiftTimesJson: String = "07:00,15:00,OFF", // Legacy compatibility
    val eventCalendar: String = "SOLAR", // SOLAR, GREGORIAN, LUNAR
    val eventYear: Int = 0,
    val eventMonth: Int = 0,
    val eventDay: Int = 0,
    val isMeeting: Boolean = false,
    val eventNote: String = "",
    val customSoundTitle: String? = null,
    val orderIndex: Int = 0,
    val skippedUntilMillis: Long = 0L
)

@Entity(tableName = "timers")
data class TimerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val totalSeconds: Long,
    val remainingSeconds: Long,
    val isRunning: Boolean = false,
    val targetEndTimeMillis: Long = 0L,
    val label: String = "",
    val soundId: String = "digital_beep",
    val customSoundTitle: String? = null,
    val vibrationPattern: String = "CONTINUOUS",
    val autoDeleteAfterUse: Boolean = false,
    val repeatType: String = "ONCE",
    val orderIndex: Int = 0
)

@Entity(tableName = "world_cities")
data class WorldCityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cityNameFa: String,
    val cityNameEn: String,
    val cityNameAr: String,
    val cityNameAz: String,
    val countryName: String,
    val timezoneId: String,
    val orderIndex: Int = 0
)
