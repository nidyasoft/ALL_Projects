package com.example.ui

import android.content.Context
import android.content.Intent
import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.AlarmScheduler
import com.example.audio.SoundAndVibrationManager
import com.example.calendar.TripleCalendarHelper
import com.example.data.local.AppDatabase
import com.example.data.local.AppSettings
import com.example.data.local.SettingsManager
import com.example.data.model.AlarmEntity
import com.example.data.model.TimerEntity
import com.example.data.model.WorldCityEntity
import com.example.data.repository.AppRepository
import com.example.i18n.AppLanguage
import com.example.ui.theme.AppThemeMode
import com.example.i18n.Strings
import com.example.service.AlarmAlertService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class NavigationTab {
    ALARM, WORLD_CLOCK, STOPWATCH, TIMER, SETTINGS
}

data class LapItem(
    val lapNumber: Int,
    val lapTimeMillis: Long,
    val totalTimeMillis: Long,
    val splitDiffMillis: Long // diff from previous lap
)

data class FiredAlarm(
    val title: String,
    val soundId: String,
    val vibrationPattern: String,
    val isTimer: Boolean = false,
    val entityId: Long = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("epic_alarm_clock_prefs", Context.MODE_PRIVATE)
    private val settingsManager = SettingsManager.getInstance(application)
    val appSettings: StateFlow<AppSettings> = settingsManager.settings

    fun updateSettings(newSettings: AppSettings) {
        settingsManager.updateSettings(newSettings)
        com.example.service.PersistentStatusNotificationHelper.updateStatusNotification(getApplication())
        com.example.widget.TripleDateWidgetProvider.updateAllWidgets(getApplication())
        com.example.widget.AnalogClockWidgetProvider.updateAllWidgets(getApplication())
    }

    private val repository: AppRepository = AppRepository(
        AppDatabase.getDatabase(application).alarmDao(),
        AppDatabase.getDatabase(application).timerDao(),
        AppDatabase.getDatabase(application).worldCityDao()
    )

    val alarms: StateFlow<List<AlarmEntity>> = repository.alarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timers: StateFlow<List<TimerEntity>> = repository.timers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val worldCities: StateFlow<List<WorldCityEntity>> = repository.worldCities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation and Language
    private val _currentTab = MutableStateFlow(NavigationTab.ALARM)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _currentLanguage = MutableStateFlow(
        try {
            AppLanguage.valueOf(prefs.getString("pref_language", AppLanguage.PERSIAN.name) ?: AppLanguage.PERSIAN.name)
        } catch (_: Exception) {
            AppLanguage.PERSIAN
        }
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _currentThemeMode = MutableStateFlow(
        try {
            AppThemeMode.valueOf(prefs.getString("pref_theme_mode", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name)
        } catch (_: Exception) {
            AppThemeMode.DARK
        }
    )
    val currentThemeMode: StateFlow<AppThemeMode> = _currentThemeMode.asStateFlow()

    // Theme Selection Dialog State
    private val _isThemeDialogOpen = MutableStateFlow(false)
    val isThemeDialogOpen = _isThemeDialogOpen.asStateFlow()

    fun openThemeDialog() {
        _isThemeDialogOpen.value = true
    }

    fun closeThemeDialog() {
        _isThemeDialogOpen.value = false
    }

    // Calendar Screen State
    private val _isCalendarScreenOpen = MutableStateFlow(false)
    val isCalendarScreenOpen: StateFlow<Boolean> = _isCalendarScreenOpen.asStateFlow()

    fun openCalendarScreen() {
        _isCalendarScreenOpen.value = true
    }

    fun closeCalendarScreen() {
        _isCalendarScreenOpen.value = false
    }

    // Real-time clock state (hours, minutes, seconds, millis)
    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    // Selection mode for Alarms
    private val _isAlarmSelectionMode = MutableStateFlow(false)
    val isAlarmSelectionMode = _isAlarmSelectionMode.asStateFlow()
    private val _selectedAlarmIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedAlarmIds = _selectedAlarmIds.asStateFlow()

    // Selection mode for Timers
    private val _isTimerSelectionMode = MutableStateFlow(false)
    val isTimerSelectionMode = _isTimerSelectionMode.asStateFlow()
    private val _selectedTimerIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedTimerIds = _selectedTimerIds.asStateFlow()

    // Selection mode for World Clock
    private val _isCitySelectionMode = MutableStateFlow(false)
    val isCitySelectionMode = _isCitySelectionMode.asStateFlow()
    private val _selectedCityIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedCityIds = _selectedCityIds.asStateFlow()

    // Stopwatch State
    private val _stopwatchRunning = MutableStateFlow(false)
    val stopwatchRunning = _stopwatchRunning.asStateFlow()
    private val _stopwatchElapsed = MutableStateFlow(0L)
    val stopwatchElapsed = _stopwatchElapsed.asStateFlow()
    private val _stopwatchLaps = MutableStateFlow<List<LapItem>>(emptyList())
    val stopwatchLaps = _stopwatchLaps.asStateFlow()
    private var stopwatchJob: Job? = null

    // Active Waking Modal
    private val _activeFiredAlarm = MutableStateFlow<FiredAlarm?>(null)
    val activeFiredAlarm = _activeFiredAlarm.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialCitiesIfEmpty()
        }
        startTimeTicker()
        startTimerCountdownTicker()
        observeServiceAlerts()
    }

    private fun observeServiceAlerts() {
        viewModelScope.launch {
            AlarmAlertService.activeAlertFlow.collect { serviceAlert ->
                if (serviceAlert != null) {
                    _activeFiredAlarm.value = FiredAlarm(
                        title = serviceAlert.title,
                        soundId = serviceAlert.soundId,
                        vibrationPattern = serviceAlert.vibration,
                        isTimer = serviceAlert.isTimer,
                        entityId = serviceAlert.alarmId
                    )
                } else if (_activeFiredAlarm.value != null && !SoundAndVibrationManager.isAlarmPlaying()) {
                    _activeFiredAlarm.value = null
                }
            }
        }
    }

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
        prefs.edit().putString("pref_language", lang.name).apply()
    }

    fun setThemeMode(mode: AppThemeMode) {
        _currentThemeMode.value = mode
        prefs.edit().putString("pref_theme_mode", mode.name).apply()
    }

    private fun startTimeTicker() {
        viewModelScope.launch {
            while (isActive) {
                _currentTimeMillis.value = System.currentTimeMillis()
                checkAlarmTriggers()
                delay(100)
            }
        }
    }

    // Check if an alarm matches current time
    private var lastCheckedMinute = -1
    private fun checkAlarmTriggers() {
        val cal = Calendar.getInstance()
        val currentMin = cal.get(Calendar.MINUTE)
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentSec = cal.get(Calendar.SECOND)

        if (currentMin != lastCheckedMinute && currentSec == 0) {
            lastCheckedMinute = currentMin
            val activeAlarms = alarms.value.filter { it.isEnabled }
            for (alarm in activeAlarms) {
                if (shouldAlarmRingNow(alarm, cal)) {
                    triggerAlarm(alarm)
                    break
                }
            }
        }
    }

    private fun shouldAlarmRingNow(alarm: AlarmEntity, cal: Calendar): Boolean {
        if (alarm.skippedUntilMillis > 0L && cal.timeInMillis <= alarm.skippedUntilMillis) {
            return false
        }
        if (alarm.hour != cal.get(Calendar.HOUR_OF_DAY) || alarm.minute != cal.get(Calendar.MINUTE)) {
            return false
        }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon, ..., 7=Sat
        return when (alarm.repeatType) {
            "ONCE", "DAILY" -> true
            "SAT_THU" -> dayOfWeek != Calendar.FRIDAY
            "MON_FRI" -> dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY
            "CUSTOM" -> {
                val daysMap = mapOf(
                    Calendar.SATURDAY to "SAT",
                    Calendar.SUNDAY to "SUN",
                    Calendar.MONDAY to "MON",
                    Calendar.TUESDAY to "TUE",
                    Calendar.WEDNESDAY to "WED",
                    Calendar.THURSDAY to "THU",
                    Calendar.FRIDAY to "FRI"
                )
                val dayStr = daysMap[dayOfWeek] ?: ""
                alarm.customDays.contains(dayStr)
            }
            "SHIFT" -> {
                val startMillis = TripleCalendarHelper.getMillisFromCalendar(
                    calType = alarm.shiftAnchorCalendar,
                    year = alarm.shiftAnchorYear,
                    month = alarm.shiftAnchorMonth,
                    day = alarm.shiftAnchorDay,
                    hour = alarm.hour,
                    minute = alarm.minute
                )
                val intervalVal = alarm.shiftIntervalValue.coerceAtLeast(1)
                val intervalMs: Long = when (alarm.shiftIntervalUnit) {
                    "MINUTES" -> intervalVal * 60 * 1000L
                    "HOURS" -> intervalVal * 3600 * 1000L
                    else -> intervalVal * 24L * 3600 * 1000L // "DAYS"
                }
                val checkMillis = cal.timeInMillis
                if (checkMillis < startMillis) {
                    false
                } else {
                    val diff = checkMillis - startMillis
                    val remainder = diff % intervalMs
                    remainder < 60_000L || (intervalMs - remainder) < 60_000L
                }
            }
            "EVENT", "MEETING" -> {
                val eventYear = if (alarm.eventYear > 0) alarm.eventYear else alarm.shiftAnchorYear
                val eventMonth = if (alarm.eventMonth > 0) alarm.eventMonth else alarm.shiftAnchorMonth
                val eventDay = if (alarm.eventDay > 0) alarm.eventDay else alarm.shiftAnchorDay
                val eventMillis = TripleCalendarHelper.getMillisFromCalendar(
                    calType = alarm.eventCalendar,
                    year = eventYear,
                    month = eventMonth,
                    day = eventDay,
                    hour = alarm.hour,
                    minute = alarm.minute
                )
                Math.abs(cal.timeInMillis - eventMillis) < 60_000L
            }
            else -> true
        }
    }

    private fun triggerAlarm(alarm: AlarmEntity) {
        val fired = FiredAlarm(
            title = alarm.label.ifBlank { Strings.get("tab_alarm", currentLanguage.value) },
            soundId = alarm.soundId,
            vibrationPattern = alarm.vibrationPattern,
            isTimer = false,
            entityId = alarm.id
        )
        _activeFiredAlarm.value = fired

        // Start Foreground Service with Heads-up and Fullscreen notification
        val context = getApplication<Application>()
        val serviceIntent = Intent(context, AlarmAlertService::class.java).apply {
            action = AlarmAlertService.ACTION_START
            putExtra("EXTRA_ALARM_ID", alarm.id)
            putExtra("EXTRA_ALARM_LABEL", fired.title)
            putExtra("EXTRA_SOUND_ID", alarm.soundId)
            putExtra("EXTRA_VIBRATION", alarm.vibrationPattern)
            putExtra("EXTRA_IS_TIMER", false)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (_: Throwable) {
            context.startService(serviceIntent)
        }

    }

    fun dismissFiredAlarm() {
        val context = getApplication<Application>()
        SoundAndVibrationManager.stopAlarm(context)
        val stopIntent = Intent(context, AlarmAlertService::class.java).apply {
            action = AlarmAlertService.ACTION_STOP
        }
        context.stopService(stopIntent)

        val fired = _activeFiredAlarm.value
        if (fired != null) {
            viewModelScope.launch {
                if (fired.isTimer) {
                    val timer = repository.getTimerById(fired.entityId)
                    if (timer != null && timer.autoDeleteAfterUse) {
                        repository.deleteTimer(timer)
                    }
                } else {
                    val alarm = repository.getAlarmById(fired.entityId)
                    if (alarm != null && alarm.autoDeleteAfterUse) {
                        repository.deleteAlarm(alarm)
                    }
                }
            }
        }
        _activeFiredAlarm.value = null
    }

    fun snoozeFiredAlarm(minutes: Int = 5) {
        val context = getApplication<Application>()
        SoundAndVibrationManager.stopAlarm(context)
        val stopIntent = Intent(context, AlarmAlertService::class.java).apply {
            action = AlarmAlertService.ACTION_STOP
        }
        context.stopService(stopIntent)

        val fired = _activeFiredAlarm.value
        _activeFiredAlarm.value = null

        val snoozeMinutes = if (minutes > 0) minutes else appSettings.value.snoozeDurationMinutes
        AlarmScheduler.scheduleSnooze(
            context = context,
            title = fired?.title ?: Strings.get("snooze", currentLanguage.value),
            soundId = fired?.soundId ?: appSettings.value.defaultAlarmSound,
            vibration = fired?.vibrationPattern ?: "INTERMITTENT",
            snoozeMinutes = snoozeMinutes,
            isTimer = fired?.isTimer ?: false
        )
    }

    // --- Timers Countdown Engine ---
    private fun startTimerCountdownTicker() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val currentTimers = timers.value
                val updated = mutableListOf<TimerEntity>()
                var changed = false
                val now = System.currentTimeMillis()

                for (t in currentTimers) {
                    if (t.isRunning) {
                        val computedRemaining = if (t.targetEndTimeMillis > 0) {
                            ((t.targetEndTimeMillis - now + 999) / 1000).coerceAtLeast(0)
                        } else {
                            (t.remainingSeconds - 1).coerceAtLeast(0)
                        }

                        if (computedRemaining > 0) {
                            if (computedRemaining != t.remainingSeconds) {
                                updated.add(t.copy(remainingSeconds = computedRemaining))
                                changed = true
                            } else {
                                updated.add(t)
                            }
                        } else {
                            // Timer reached 0!
                            updated.add(t.copy(remainingSeconds = 0, isRunning = false, targetEndTimeMillis = 0L))
                            changed = true
                            triggerTimerFired(t)
                        }
                    } else {
                        updated.add(t)
                    }
                }

                if (changed) {
                    repository.updateAllTimers(updated)
                }
            }
        }
    }

    private fun triggerTimerFired(timer: TimerEntity) {
        val context = getApplication<Application>()
        AlarmScheduler.cancelTimer(context, timer.id)
        val fired = FiredAlarm(
            title = timer.label.ifBlank { Strings.get("tab_timer", currentLanguage.value) },
            soundId = timer.soundId,
            vibrationPattern = timer.vibrationPattern,
            isTimer = true,
            entityId = timer.id
        )
        _activeFiredAlarm.value = fired

        // Start Foreground Service with Heads-up and Fullscreen notification for Timer too!
        val serviceIntent = Intent(context, AlarmAlertService::class.java).apply {
            action = AlarmAlertService.ACTION_START
            putExtra("EXTRA_ALARM_ID", timer.id)
            putExtra("EXTRA_ALARM_LABEL", fired.title)
            putExtra("EXTRA_SOUND_ID", timer.soundId)
            putExtra("EXTRA_VIBRATION", timer.vibrationPattern)
            putExtra("EXTRA_IS_TIMER", true)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (_: Throwable) {
            context.startService(serviceIntent)
        }
    }

    // --- Alarm Actions ---
    fun toggleAlarm(alarm: AlarmEntity) {
        val nextEnabled = !alarm.isEnabled
        val updated = alarm.copy(
            isEnabled = nextEnabled,
            skippedUntilMillis = 0L
        )
        viewModelScope.launch {
            repository.updateAlarm(updated)
            if (updated.isEnabled) {
                AlarmScheduler.scheduleAlarm(getApplication(), updated)
            } else {
                AlarmScheduler.cancelAlarm(getApplication(), updated.id)
            }
        }
    }

    fun skipNextAlarmOccurrence(alarm: AlarmEntity, skippedTriggerMillis: Long) {
        val updated = alarm.copy(
            isEnabled = true,
            skippedUntilMillis = skippedTriggerMillis + 30_000L
        )
        viewModelScope.launch {
            repository.updateAlarm(updated)
            AlarmScheduler.scheduleAlarm(getApplication(), updated)
        }
    }

    fun disableAlarmCompletely(alarm: AlarmEntity) {
        val updated = alarm.copy(
            isEnabled = false,
            skippedUntilMillis = 0L
        )
        viewModelScope.launch {
            repository.updateAlarm(updated)
            AlarmScheduler.cancelAlarm(getApplication(), updated.id)
        }
    }

    fun clearAlarmSkip(alarm: AlarmEntity) {
        val updated = alarm.copy(skippedUntilMillis = 0L)
        viewModelScope.launch {
            repository.updateAlarm(updated)
            if (updated.isEnabled) {
                AlarmScheduler.scheduleAlarm(getApplication(), updated)
            }
        }
    }

    fun restoreAlarms(
        importedAlarms: List<AlarmEntity>,
        replaceExisting: Boolean,
        onComplete: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            if (replaceExisting) {
                for (existing in alarms.value) {
                    AlarmScheduler.cancelAlarm(ctx, existing.id)
                }
                repository.deleteAllAlarms()
            }
            val baseOrderIndex = if (replaceExisting) 0 else alarms.value.size
            var count = 0
            importedAlarms.forEachIndexed { idx, alarm ->
                val clean = alarm.copy(
                    id = 0L,
                    orderIndex = baseOrderIndex + idx
                )
                val assignedId = repository.saveAlarm(clean)
                val saved = clean.copy(id = assignedId)
                if (saved.isEnabled) {
                    AlarmScheduler.scheduleAlarm(ctx, saved)
                }
                count++
            }
            onComplete(count)
        }
    }

    fun saveAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val assignedId = if (alarm.id == 0L) {
                repository.saveAlarm(alarm)
            } else {
                repository.updateAlarm(alarm)
                alarm.id
            }
            val scheduledAlarm = alarm.copy(id = assignedId)
            if (scheduledAlarm.isEnabled) {
                AlarmScheduler.scheduleAlarm(getApplication(), scheduledAlarm)
            } else {
                AlarmScheduler.cancelAlarm(getApplication(), scheduledAlarm.id)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarm(getApplication(), alarm.id)
            repository.deleteAlarm(alarm)
        }
    }

    fun toggleAlarmSelection(id: Long) {
        val current = _selectedAlarmIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _selectedAlarmIds.value = current
        if (current.isEmpty()) _isAlarmSelectionMode.value = false
    }

    fun enterAlarmSelectionMode(initialId: Long? = null) {
        _isAlarmSelectionMode.value = true
        _selectedAlarmIds.value = if (initialId != null) setOf(initialId) else emptySet()
    }

    fun exitAlarmSelectionMode() {
        _isAlarmSelectionMode.value = false
        _selectedAlarmIds.value = emptySet()
    }

    fun selectAllAlarms() {
        _selectedAlarmIds.value = alarms.value.map { it.id }.toSet()
    }

    fun deleteSelectedAlarms() {
        viewModelScope.launch {
            for (id in _selectedAlarmIds.value) {
                AlarmScheduler.cancelAlarm(getApplication(), id)
            }
            repository.deleteAlarms(_selectedAlarmIds.value.toList())
            exitAlarmSelectionMode()
        }
    }

    fun deleteAllAlarms() {
        viewModelScope.launch {
            for (alarm in alarms.value) {
                AlarmScheduler.cancelAlarm(getApplication(), alarm.id)
            }
            repository.deleteAllAlarms()
            exitAlarmSelectionMode()
        }
    }

    fun moveAlarmUp(alarm: AlarmEntity) {
        val list = alarms.value.toMutableList()
        val index = list.indexOfFirst { it.id == alarm.id }
        if (index > 0) {
            val prev = list[index - 1]
            list[index - 1] = alarm.copy(orderIndex = index - 1)
            list[index] = prev.copy(orderIndex = index)
            viewModelScope.launch { repository.updateAllAlarms(list) }
        }
    }

    fun moveAlarmDown(alarm: AlarmEntity) {
        val list = alarms.value.toMutableList()
        val index = list.indexOfFirst { it.id == alarm.id }
        if (index in 0 until list.size - 1) {
            val next = list[index + 1]
            list[index + 1] = alarm.copy(orderIndex = index + 1)
            list[index] = next.copy(orderIndex = index)
            viewModelScope.launch { repository.updateAllAlarms(list) }
        }
    }

    // Next alarm formatted countdown string
    fun getNextAlarmCountdown(): String? {
        return try {
            val activeAlarms = alarms.value.filter { it.isEnabled }
            if (activeAlarms.isEmpty()) return null

            val now = Calendar.getInstance()
            var minDiffMillis = Long.MAX_VALUE

            for (alarm in activeAlarms) {
                val nextCal = calculateNextAlarmCalendar(alarm, now)
                if (nextCal != null) {
                    val diff = nextCal.timeInMillis - now.timeInMillis
                    if (diff in 1 until minDiffMillis) {
                        minDiffMillis = diff
                    }
                }
            }

            if (minDiffMillis == Long.MAX_VALUE) return null

            val totalMinutes = minDiffMillis / (60 * 1000)
            val days = totalMinutes / (24 * 60)
            val hours = (totalMinutes % (24 * 60)) / 60
            val mins = totalMinutes % 60

            val lang = currentLanguage.value
            val parts = mutableListOf<String>()
            if (days > 0) parts.add("${Strings.formatNumber(days.toInt(), lang)} ${Strings.get("days", lang)}")
            if (hours > 0 || days > 0) parts.add("${Strings.formatNumber(hours.toInt(), lang)} ${Strings.get("hours", lang)}")
            parts.add("${Strings.formatNumber(mins.toInt(), lang)} ${Strings.get("minutes", lang)}")

            val formattedDiff = parts.joinToString(" و ")
            String.format(Strings.get("next_alarm_in", lang), formattedDiff)
        } catch (_: Throwable) {
            null
        }
    }

    private fun calculateNextAlarmCalendar(alarm: AlarmEntity, now: Calendar): Calendar? {
        return try {
            val nextMillis = AlarmScheduler.calculateNextTriggerMillis(alarm)
            if (nextMillis > now.timeInMillis) {
                Calendar.getInstance().apply { timeInMillis = nextMillis }
            } else null
        } catch (_: Throwable) {
            null
        }
    }

    // --- Timer Actions ---
    fun saveTimer(timer: TimerEntity) {
        viewModelScope.launch {
            val assignedId = if (timer.id == 0L) {
                repository.saveTimer(timer)
            } else {
                repository.updateTimer(timer)
                timer.id
            }
            val scheduledTimer = timer.copy(id = assignedId)
            if (scheduledTimer.isRunning && scheduledTimer.targetEndTimeMillis > System.currentTimeMillis()) {
                AlarmScheduler.scheduleTimer(getApplication(), scheduledTimer)
            } else {
                AlarmScheduler.cancelTimer(getApplication(), scheduledTimer.id)
            }
        }
    }

    fun deleteTimer(timer: TimerEntity) {
        viewModelScope.launch {
            AlarmScheduler.cancelTimer(getApplication(), timer.id)
            repository.deleteTimer(timer)
        }
    }

    fun toggleTimerRunning(timer: TimerEntity) {
        viewModelScope.launch {
            val newRunning = !timer.isRunning
            val resetTime = if (timer.remainingSeconds <= 0) timer.totalSeconds else timer.remainingSeconds
            val targetEndTime = if (newRunning) System.currentTimeMillis() + (resetTime * 1000L) else 0L
            val updated = timer.copy(
                isRunning = newRunning,
                remainingSeconds = resetTime,
                targetEndTimeMillis = targetEndTime
            )
            repository.updateTimer(updated)
            if (newRunning) {
                AlarmScheduler.scheduleTimer(getApplication(), updated)
            } else {
                AlarmScheduler.cancelTimer(getApplication(), updated.id)
            }
        }
    }

    fun resetTimer(timer: TimerEntity) {
        viewModelScope.launch {
            AlarmScheduler.cancelTimer(getApplication(), timer.id)
            repository.updateTimer(timer.copy(isRunning = false, remainingSeconds = timer.totalSeconds, targetEndTimeMillis = 0L))
        }
    }

    fun toggleTimerSelection(id: Long) {
        val current = _selectedTimerIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _selectedTimerIds.value = current
        if (current.isEmpty()) _isTimerSelectionMode.value = false
    }

    fun enterTimerSelectionMode(initialId: Long? = null) {
        _isTimerSelectionMode.value = true
        _selectedTimerIds.value = if (initialId != null) setOf(initialId) else emptySet()
    }

    fun exitTimerSelectionMode() {
        _isTimerSelectionMode.value = false
        _selectedTimerIds.value = emptySet()
    }

    fun selectAllTimers() {
        _selectedTimerIds.value = timers.value.map { it.id }.toSet()
    }

    fun deleteSelectedTimers() {
        viewModelScope.launch {
            for (id in _selectedTimerIds.value) {
                AlarmScheduler.cancelTimer(getApplication(), id)
            }
            repository.deleteTimers(_selectedTimerIds.value.toList())
            exitTimerSelectionMode()
        }
    }

    fun deleteAllTimers() {
        viewModelScope.launch {
            for (timer in timers.value) {
                AlarmScheduler.cancelTimer(getApplication(), timer.id)
            }
            repository.deleteAllTimers()
            exitTimerSelectionMode()
        }
    }

    fun moveTimerUp(timer: TimerEntity) {
        val list = timers.value.toMutableList()
        val index = list.indexOfFirst { it.id == timer.id }
        if (index > 0) {
            val prev = list[index - 1]
            list[index - 1] = timer.copy(orderIndex = index - 1)
            list[index] = prev.copy(orderIndex = index)
            viewModelScope.launch { repository.updateAllTimers(list) }
        }
    }

    fun moveTimerDown(timer: TimerEntity) {
        val list = timers.value.toMutableList()
        val index = list.indexOfFirst { it.id == timer.id }
        if (index in 0 until list.size - 1) {
            val next = list[index + 1]
            list[index + 1] = timer.copy(orderIndex = index + 1)
            list[index] = next.copy(orderIndex = index)
            viewModelScope.launch { repository.updateAllTimers(list) }
        }
    }

    fun getNextTimerCountdown(): String? {
        return try {
            val runningTimers = timers.value.filter { it.isRunning && it.remainingSeconds > 0 }
            if (runningTimers.isEmpty()) return null

            val minSeconds = runningTimers.minOf { it.remainingSeconds }
            val days = minSeconds / (24 * 3600)
            val hours = (minSeconds % (24 * 3600)) / 3600
            val mins = (minSeconds % 3600) / 60
            val secs = minSeconds % 60

            val lang = currentLanguage.value
            val parts = mutableListOf<String>()
            if (days > 0) parts.add("${Strings.formatNumber(days.toInt(), lang)} ${Strings.get("days", lang)}")
            if (hours > 0 || days > 0) parts.add("${Strings.formatNumber(hours.toInt(), lang)} ${Strings.get("hours", lang)}")
            if (mins > 0 || hours > 0) parts.add("${Strings.formatNumber(mins.toInt(), lang)} ${Strings.get("minutes", lang)}")
            parts.add("${Strings.formatNumber(secs.toInt(), lang)} ${Strings.get("seconds", lang)}")

            val formattedDiff = parts.joinToString(" و ")
            String.format(Strings.get("next_timer_in", lang), formattedDiff)
        } catch (_: Throwable) {
            null
        }
    }

    // --- World Clock Actions ---
    fun addCity(city: WorldCityEntity) {
        viewModelScope.launch {
            repository.addWorldCity(city)
        }
    }

    fun deleteCity(city: WorldCityEntity) {
        viewModelScope.launch {
            repository.deleteWorldCity(city)
        }
    }

    fun toggleCitySelection(id: Long) {
        val current = _selectedCityIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _selectedCityIds.value = current
        if (current.isEmpty()) _isCitySelectionMode.value = false
    }

    fun enterCitySelectionMode(initialId: Long? = null) {
        _isCitySelectionMode.value = true
        _selectedCityIds.value = if (initialId != null) setOf(initialId) else emptySet()
    }

    fun exitCitySelectionMode() {
        _isCitySelectionMode.value = false
        _selectedCityIds.value = emptySet()
    }

    fun selectAllCities() {
        _selectedCityIds.value = worldCities.value.map { it.id }.toSet()
    }

    fun deleteSelectedCities() {
        viewModelScope.launch {
            repository.deleteWorldCities(_selectedCityIds.value.toList())
            exitCitySelectionMode()
        }
    }

    fun moveCityUp(city: WorldCityEntity) {
        val list = worldCities.value.toMutableList()
        val index = list.indexOfFirst { it.id == city.id }
        if (index > 0) {
            val prev = list[index - 1]
            list[index - 1] = city.copy(orderIndex = index - 1)
            list[index] = prev.copy(orderIndex = index)
            viewModelScope.launch { repository.updateAllCities(list) }
        }
    }

    fun moveCityDown(city: WorldCityEntity) {
        val list = worldCities.value.toMutableList()
        val index = list.indexOfFirst { it.id == city.id }
        if (index in 0 until list.size - 1) {
            val next = list[index + 1]
            list[index + 1] = city.copy(orderIndex = index + 1)
            list[index] = next.copy(orderIndex = index)
            viewModelScope.launch { repository.updateAllCities(list) }
        }
    }

    // --- Stopwatch Actions ---
    fun startStopwatch() {
        if (_stopwatchRunning.value) return
        _stopwatchRunning.value = true
        val startBase = System.currentTimeMillis() - _stopwatchElapsed.value
        stopwatchJob = viewModelScope.launch {
            while (isActive && _stopwatchRunning.value) {
                _stopwatchElapsed.value = System.currentTimeMillis() - startBase
                delay(30)
            }
        }
    }

    fun pauseStopwatch() {
        _stopwatchRunning.value = false
        stopwatchJob?.cancel()
        stopwatchJob = null
    }

    fun resetStopwatch() {
        pauseStopwatch()
        _stopwatchElapsed.value = 0L
        _stopwatchLaps.value = emptyList()
    }

    fun recordLap() {
        val currentElapsed = _stopwatchElapsed.value
        val currentLaps = _stopwatchLaps.value
        val lapNum = currentLaps.size + 1
        val lastLapTotal = currentLaps.firstOrNull()?.totalTimeMillis ?: 0L
        val lapTime = currentElapsed - lastLapTotal
        val previousLapTime = currentLaps.firstOrNull()?.lapTimeMillis ?: lapTime
        val splitDiff = lapTime - previousLapTime

        val newLap = LapItem(
            lapNumber = lapNum,
            lapTimeMillis = lapTime,
            totalTimeMillis = currentElapsed,
            splitDiffMillis = splitDiff
        )
        _stopwatchLaps.value = listOf(newLap) + currentLaps
    }
}
