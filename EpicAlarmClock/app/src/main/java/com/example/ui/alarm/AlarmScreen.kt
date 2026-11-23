package com.example.ui.alarm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.audio.SoundAndVibrationManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlarmEntity
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import com.example.ui.MainViewModel
import com.example.ui.components.CountdownBanner
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OledBackground
import com.example.ui.theme.OledBorder
import com.example.ui.theme.OledSurface
import com.example.ui.theme.OledSurfaceElevated
import com.example.ui.theme.OledSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AlarmScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val alarms by viewModel.alarms.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val isSelectionMode by viewModel.isAlarmSelectionMode.collectAsState()
    val selectedIds by viewModel.selectedAlarmIds.collectAsState()
    val nextAlarmCountdown = viewModel.getNextAlarmCountdown()

    val settings by viewModel.appSettings.collectAsState()
    val sortBy = settings.alarmSortBy
    val sortAscending = settings.alarmSortAscending

    val sortedAlarms = remember(alarms, sortBy, sortAscending) {
        val list = alarms.toList()
        if (sortBy == "TIME") {
            if (sortAscending) {
                list.sortedWith(compareBy({ it.hour * 60 + it.minute }, { it.id }))
            } else {
                list.sortedWith(compareByDescending<AlarmEntity> { it.hour * 60 + it.minute }.thenByDescending { it.id })
            }
        } else {
            if (sortAscending) {
                list.sortedBy { it.id }
            } else {
                list.sortedByDescending { it.id }
            }
        }
    }

    var editingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }
    var pendingDisableAlarm by remember { mutableStateOf<Pair<AlarmEntity, Long>?>(null) }

    Scaffold(
        containerColor = OledBackground,
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { isAddingNew = true },
                    containerColor = NeonCyan,
                    contentColor = OledBackground,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .testTag("add_alarm_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = Strings.get("add_alarm", language))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // In-App Notification Banner for Active Ringing Alarm
            val activeServiceAlert by com.example.service.AlarmAlertService.activeAlertFlow.collectAsState()
            val firedAlarm by viewModel.activeFiredAlarm.collectAsState()
            com.example.ui.components.ActiveRingingInAppBanner(
                activeAlert = activeServiceAlert,
                firedAlarm = firedAlarm,
                forTimerTab = false,
                onDismiss = { viewModel.dismissFiredAlarm() },
                onSnooze = { viewModel.snoozeFiredAlarm() }
            )

            // Next Alarm Countdown Banner (Displayed only if active alarms exist!)
            if (nextAlarmCountdown != null) {
                CountdownBanner(
                    text = nextAlarmCountdown,
                    isTimer = false
                )
            }

            var showDeleteSelectedConfirmDialog by remember { mutableStateOf(false) }
            var showDeleteAllConfirmDialog by remember { mutableStateOf(false) }

            // Calendar & Quick Management Action Bar
            Surface(
                color = OledSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OledBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Open Calendar Button
                    Surface(
                        color = NeonCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable { viewModel.openCalendarScreen() }
                            .testTag("open_calendar_screen_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "تقویم",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "تقویم و قرارها",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val meetingCount = alarms.count { it.repeatType == "EVENT" || it.isMeeting }
                            if (meetingCount > 0) {
                                Surface(
                                    color = NeonCyan,
                                    shape = CircleShape,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$meetingCount",
                                            color = OledBackground,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Selection / Batch Management Toggle
                    if (alarms.isNotEmpty()) {
                        if (!isSelectionMode) {
                            Surface(
                                color = OledSurfaceElevated,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OledBorder),
                                modifier = Modifier
                                    .clickable { viewModel.enterAlarmSelectionMode(null) }
                                    .testTag("enter_alarm_selection_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SelectAll,
                                        contentDescription = "انتخاب چندتایی",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "انتخاب و مدیریت",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            // In selection mode: button to select all or deselect all
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        if (selectedIds.size == alarms.size) {
                                            viewModel.exitAlarmSelectionMode()
                                        } else {
                                            viewModel.selectAllAlarms()
                                        }
                                    }
                                ) {
                                    Text(
                                        text = if (selectedIds.size == alarms.size) "لغو همه" else "انتخاب همه",
                                        color = NeonCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                TextButton(
                                    onClick = { showDeleteAllConfirmDialog = true }
                                ) {
                                    Text(
                                        text = "حذف کل آلارم‌ها",
                                        color = NeonRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Selection Mode Bar
            AnimatedVisibility(visible = isSelectionMode) {
                Surface(
                    color = OledSurfaceElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                if (selectedIds.size == alarms.size) {
                                    viewModel.exitAlarmSelectionMode()
                                } else {
                                    viewModel.selectAllAlarms()
                                }
                            }) {
                                Icon(
                                    imageVector = if (selectedIds.size == alarms.size) Icons.Default.CheckCircle else Icons.Default.SelectAll,
                                    contentDescription = Strings.get("select_all", language),
                                    tint = NeonCyan
                                )
                            }
                            Text(
                                text = String.format(Strings.get("selected_count", language), selectedIds.size),
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showDeleteSelectedConfirmDialog = true },
                                enabled = selectedIds.isNotEmpty(),
                                modifier = Modifier.testTag("delete_selected_alarms_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = Strings.get("delete", language),
                                    tint = if (selectedIds.isNotEmpty()) NeonRed else TextMuted
                                )
                            }
                            IconButton(onClick = { viewModel.exitAlarmSelectionMode() }) {
                                Text(text = Strings.get("cancel", language), color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Confirm Dialog for deleting selected alarms
            if (showDeleteSelectedConfirmDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showDeleteSelectedConfirmDialog = false },
                    title = { Text(text = "حذف آلارم‌های انتخاب‌شده", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    text = {
                        Text(
                            text = "آیا مطمئن هستید می‌خواهید ${selectedIds.size} آلارم انتخاب‌شده را حذف کنید؟",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        androidx.compose.material3.Button(
                            onClick = {
                                viewModel.deleteSelectedAlarms()
                                showDeleteSelectedConfirmDialog = false
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = NeonRed)
                        ) {
                            Text(text = "حذف کن", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteSelectedConfirmDialog = false }) {
                            Text(text = "انصراف", color = TextSecondary)
                        }
                    },
                    containerColor = OledSurfaceElevated
                )
            }

            // Confirm Dialog for deleting ALL alarms
            if (showDeleteAllConfirmDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showDeleteAllConfirmDialog = false },
                    title = { Text(text = "حذف همه آلارم‌ها", color = NeonRed, fontWeight = FontWeight.Bold) },
                    text = {
                        Text(
                            text = "هشدار: تمامی آلارم‌های تعریف‌شده (تعداد ${alarms.size}) حذف خواهند شد. این عملیات قابل بازگشت نیست. آیا ادامه می‌دهید؟",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        androidx.compose.material3.Button(
                            onClick = {
                                viewModel.deleteAllAlarms()
                                showDeleteAllConfirmDialog = false
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = NeonRed)
                        ) {
                            Text(text = "بله، حذف همه", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteAllConfirmDialog = false }) {
                            Text(text = "انصراف", color = TextSecondary)
                        }
                    },
                    containerColor = OledSurfaceElevated
                )
            }

            // Sort Controls Bar (مرتب‌سازی صعودی/نزولی بر اساس زمان رسیدن هشدار یا ترتیب افزودن)
            if (alarms.isNotEmpty() && !isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "مرتب‌سازی:",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        // By Time Chip
                        Surface(
                            color = if (sortBy == "TIME") NeonCyan.copy(alpha = 0.2f) else OledSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (sortBy == "TIME") NeonCyan else OledBorder),
                            modifier = Modifier.clickable {
                                viewModel.updateSettings(settings.copy(alarmSortBy = "TIME"))
                            }
                        ) {
                            Text(
                                text = "زمان هشدار (ساعت و دقیقه)",
                                color = if (sortBy == "TIME") NeonCyan else TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = if (sortBy == "TIME") FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        // By Added Order Chip
                        Surface(
                            color = if (sortBy == "ADDED") NeonCyan.copy(alpha = 0.2f) else OledSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (sortBy == "ADDED") NeonCyan else OledBorder),
                            modifier = Modifier.clickable {
                                viewModel.updateSettings(settings.copy(alarmSortBy = "ADDED"))
                            }
                        ) {
                            Text(
                                text = "ترتیب افزودن",
                                color = if (sortBy == "ADDED") NeonCyan else TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = if (sortBy == "ADDED") FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // Ascending / Descending Toggle
                    Surface(
                        color = OledSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable {
                            viewModel.updateSettings(settings.copy(alarmSortAscending = !sortAscending))
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (sortAscending) "صعودی" else "نزولی",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Alarms List or Empty state
            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = Strings.get("no_active_alarms", language),
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(sortedAlarms, key = { index, alarm -> if (alarm.id != 0L) "${alarm.id}" else "alarm_$index" }) { index, alarm ->
                        AlarmItemCard(
                            alarm = alarm,
                            index = index,
                            totalCount = alarms.size,
                            language = language,
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedIds.contains(alarm.id),
                            onToggleSelect = { viewModel.toggleAlarmSelection(alarm.id) },
                            onLongClick = { viewModel.enterAlarmSelectionMode(alarm.id) },
                            onClick = {
                                if (isSelectionMode) {
                                    viewModel.toggleAlarmSelection(alarm.id)
                                } else {
                                    editingAlarm = alarm
                                }
                            },
                            onToggleEnable = {
                                if (alarm.isEnabled && alarm.repeatType != "ONCE") {
                                    val nextMillis = com.example.alarm.AlarmScheduler.calculateNextTriggerMillis(alarm)
                                    if (nextMillis > System.currentTimeMillis()) {
                                        pendingDisableAlarm = alarm to nextMillis
                                    } else {
                                        viewModel.disableAlarmCompletely(alarm)
                                    }
                                } else {
                                    viewModel.toggleAlarm(alarm)
                                }
                            },
                            onClearSkip = { viewModel.clearAlarmSkip(alarm) },
                            onMoveUp = { viewModel.moveAlarmUp(alarm) },
                            onMoveDown = { viewModel.moveAlarmDown(alarm) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Smart Disable Choice Dialog (Skip Next Occurrence vs Full Disable)
    pendingDisableAlarm?.let { (alarmToDisable, nextTriggerMillis) ->
        DisableAlarmChoiceDialog(
            alarm = alarmToDisable,
            nextTriggerMillis = nextTriggerMillis,
            language = language,
            onSkipNext = {
                viewModel.skipNextAlarmOccurrence(alarmToDisable, nextTriggerMillis)
                pendingDisableAlarm = null
            },
            onDisableCompletely = {
                viewModel.disableAlarmCompletely(alarmToDisable)
                pendingDisableAlarm = null
            },
            onDismiss = { pendingDisableAlarm = null }
        )
    }

    // Modal Sheet for Adding New or Editing Alarm
    if (isAddingNew || editingAlarm != null) {
        AddEditAlarmSheet(
            existingAlarm = editingAlarm,
            language = language,
            onDismiss = {
                isAddingNew = false
                editingAlarm = null
            },
            onSave = { alarm ->
                viewModel.saveAlarm(alarm)
                isAddingNew = false
                editingAlarm = null
            }
        )
    }
}

@Composable
fun AlarmItemCard(
    alarm: AlarmEntity,
    index: Int,
    totalCount: Int,
    language: AppLanguage,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onToggleEnable: () -> Unit,
    onClearSkip: () -> Unit = {},
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val hour12 = if (alarm.hour == 0) 12 else if (alarm.hour > 12) alarm.hour - 12 else alarm.hour
    val amPm = if (alarm.hour >= 12) "PM" else "AM"
    val timeFormatted = "${String.format("%02d", hour12)}:${String.format("%02d", alarm.minute)}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                1.dp,
                if (isSelected) NeonCyan else if (alarm.isEnabled) NeonCyan.copy(alpha = 0.3f) else OledBorder,
                RoundedCornerShape(18.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) OledSurfaceElevated else OledSurface
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time & Selection checkbox
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelectionMode) {
                        IconButton(onClick = onToggleSelect, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSelected) NeonCyan else TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = Strings.formatNumber(timeFormatted, language),
                                color = if (alarm.isEnabled) TextPrimary else TextMuted,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = amPm,
                                color = if (alarm.isEnabled) NeonCyan else TextMuted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        if (alarm.label.isNotBlank()) {
                            Text(
                                text = alarm.label,
                                color = if (alarm.isEnabled) NeonAmber else TextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Switch and Reorder buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelectionMode) {
                        IconButton(
                            onClick = onMoveUp,
                            enabled = index > 0,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = Strings.get("move_up", language),
                                tint = if (index > 0) NeonCyan else TextMuted
                            )
                        }
                        IconButton(
                            onClick = onMoveDown,
                            enabled = index < totalCount - 1,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = Strings.get("move_down", language),
                                tint = if (index < totalCount - 1) NeonCyan else TextMuted
                            )
                        }
                    } else {
                        Switch(
                            checked = alarm.isEnabled,
                            onCheckedChange = { onToggleEnable() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OledBackground,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = OledSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata row: Repeat cycle, Vibration status, Sound
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Repeat Badge or Event Date Badge
                val isEvent = alarm.repeatType == "EVENT" || alarm.isMeeting
                if (isEvent) {
                    val calName = when (alarm.eventCalendar) {
                        "GREGORIAN" -> "میلادی"
                        "LUNAR" -> "قمری"
                        else -> "شمسی"
                    }
                    MetadataChip(
                        icon = Icons.Default.Event,
                        text = "📅 $calName: ${alarm.eventYear}/${alarm.eventMonth}/${alarm.eventDay}" + (if (!alarm.eventNote.isNullOrBlank()) " (${alarm.eventNote})" else ""),
                        tint = NeonCyan
                    )
                } else {
                    val repeatLabel = when (alarm.repeatType) {
                        "ONCE" -> Strings.get("repeat_once", language)
                        "DAILY" -> Strings.get("repeat_daily", language)
                        "SAT_THU" -> Strings.get("repeat_sat_thu", language)
                        "MON_FRI" -> Strings.get("repeat_mon_fri", language)
                        "CUSTOM" -> Strings.get("repeat_custom", language)
                        "SHIFT" -> {
                            val unitStr = when (alarm.shiftIntervalUnit) {
                                "MINUTES" -> "دقیقه"
                                "HOURS" -> "ساعت"
                                else -> "روز"
                            }
                            "شیفت هر ${alarm.shiftIntervalValue} $unitStr"
                        }
                        else -> alarm.repeatType
                    }
                    MetadataChip(
                        icon = Icons.Default.Repeat,
                        text = repeatLabel,
                        tint = if (alarm.repeatType == "SHIFT") NeonPink else NeonPurple
                    )
                }

                // Vibration Indicator
                if (alarm.vibrationPattern != "OFF") {
                    MetadataChip(
                        icon = Icons.Default.Vibration,
                        text = when (alarm.vibrationPattern) {
                            "CONTINUOUS" -> Strings.get("vibe_continuous", language)
                            "INTERMITTENT" -> Strings.get("vibe_intermittent", language)
                            "HEARTBEAT" -> Strings.get("vibe_heartbeat", language)
                            "STROBE" -> Strings.get("vibe_strobe", language)
                            else -> Strings.get("vibration", language)
                        },
                        tint = NeonAmber
                    )
                }

                // Sound Indicator
                val soundDisplayName = if (!alarm.customSoundTitle.isNullOrBlank()) {
                    alarm.customSoundTitle
                } else {
                    SoundAndVibrationManager.soundOptions.find { it.id == alarm.soundId }?.let {
                        if (language == AppLanguage.ENGLISH) it.nameEn else it.nameFa
                    } ?: if (alarm.soundId.startsWith("content://") || alarm.soundId.startsWith("file://")) {
                        Strings.get("custom_device_sound", language)
                    } else {
                        alarm.soundId.replace("_", " ")
                    }
                }
                MetadataChip(
                    icon = Icons.Default.MusicNote,
                    text = soundDisplayName,
                    tint = NeonCyan
                )
            }

            // Skipped next occurrence indicator badge
            val isSkippedNext = alarm.isEnabled && alarm.skippedUntilMillis > System.currentTimeMillis()
            if (isSkippedNext) {
                Spacer(modifier = Modifier.height(8.dp))
                val nextActiveMillis = remember(alarm.skippedUntilMillis, alarm.hour, alarm.minute, alarm.repeatType) {
                    com.example.alarm.AlarmScheduler.calculateNextTriggerMillis(alarm)
                }
                val skipTriple = remember(alarm.skippedUntilMillis, language) {
                    com.example.calendar.TripleCalendarHelper.getTripleDateForMillis(
                        alarm.skippedUntilMillis,
                        java.util.TimeZone.getDefault(),
                        language
                    )
                }
                val nextCalText = if (nextActiveMillis > System.currentTimeMillis()) {
                    val nextTriple = com.example.calendar.TripleCalendarHelper.getTripleDateForMillis(
                        nextActiveMillis,
                        java.util.TimeZone.getDefault(),
                        language
                    )
                    " | هشدار بعدی: ${nextTriple.weekdayName} ${nextTriple.solarFormatted}"
                } else ""
                Surface(
                    color = NeonAmber.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClearSkip() }
                        .testTag("clear_skip_badge_${alarm.id}")
                ) {
                    Text(
                        text = "نوبت ${skipTriple.weekdayName} ${skipTriple.solarFormatted} رد شد$nextCalText (لمس برای لغو رد)",
                        color = NeonAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DisableAlarmChoiceDialog(
    alarm: AlarmEntity,
    nextTriggerMillis: Long,
    language: AppLanguage,
    onSkipNext: () -> Unit,
    onDisableCompletely: () -> Unit,
    onDismiss: () -> Unit
) {
    val triple = remember(nextTriggerMillis, language) {
        com.example.calendar.TripleCalendarHelper.getTripleDateForMillis(
            nextTriggerMillis,
            java.util.TimeZone.getDefault(),
            language
        )
    }
    val triggerCal = remember(nextTriggerMillis) {
        java.util.Calendar.getInstance().apply { timeInMillis = nextTriggerMillis }
    }
    val timeText = String.format(
        "%02d:%02d",
        triggerCal.get(java.util.Calendar.HOUR_OF_DAY),
        triggerCal.get(java.util.Calendar.MINUTE)
    )
    val dateLine = "${triple.weekdayName} ${triple.solarFormatted} / ${triple.gregorianFormatted} / ${triple.lunarFormatted}"

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = OledSurfaceElevated,
        title = {
            Text(
                text = "غیرفعال‌سازی هشدار${if (alarm.label.isNotBlank()) " «${alarm.label}»" else ""}",
                color = NeonCyan,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "نزدیک‌ترین زمان فعال شدن هشدار این آلارم در تاریخ زیر می‌باشد:",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Surface(
                    color = OledSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = dateLine,
                            color = NeonAmber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ساعت $timeText",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    text = "آیا فقط همین نزدیک‌ترین هشدار غیرفعال (رد) شود یا این آلارم به صورت کامل غیرفعال گردد؟",
                    color = TextPrimary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                androidx.compose.material3.Button(
                    onClick = onSkipNext,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = NeonAmber,
                        contentColor = OledBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("skip_next_occurrence_button")
                ) {
                    Text(
                        text = "فقط غیرفعال کردن نزدیک‌ترین هشدار",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                androidx.compose.material3.Button(
                    onClick = onDisableCompletely,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = NeonRed,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("disable_alarm_completely_button")
                ) {
                    Text(
                        text = "غیرفعال‌سازی کامل آلارم",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_disable_dialog_button")
            ) {
                Text(text = Strings.get("cancel", language), color = TextSecondary)
            }
        }
    )
}

@Composable
fun MetadataChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(OledSurfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(12.dp))
        Text(text = text, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
    }
}
