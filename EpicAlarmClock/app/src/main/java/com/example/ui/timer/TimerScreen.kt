package com.example.ui.timer

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TimerEntity
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import com.example.ui.MainViewModel
import com.example.ui.alarm.MetadataChip
import com.example.ui.components.CountdownBanner
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
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
fun TimerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val timers by viewModel.timers.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val isSelectionMode by viewModel.isTimerSelectionMode.collectAsState()
    val selectedIds by viewModel.selectedTimerIds.collectAsState()
    val nextTimerCountdown = viewModel.getNextTimerCountdown()

    val settings by viewModel.appSettings.collectAsState()
    val sortBy = settings.timerSortBy
    val sortAscending = settings.timerSortAscending

    val sortedTimers = remember(timers, sortBy, sortAscending) {
        val list = timers.toList()
        if (sortBy == "TIME") {
            if (sortAscending) {
                list.sortedWith(compareBy({ it.totalSeconds }, { it.id }))
            } else {
                list.sortedWith(compareByDescending<TimerEntity> { it.totalSeconds }.thenByDescending { it.id })
            }
        } else {
            if (sortAscending) {
                list.sortedBy { it.id }
            } else {
                list.sortedByDescending { it.id }
            }
        }
    }

    var isAddingNew by remember { mutableStateOf(false) }
    var editingTimer by remember { mutableStateOf<TimerEntity?>(null) }

    Scaffold(
        containerColor = OledBackground,
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { isAddingNew = true },
                    containerColor = NeonAmber,
                    contentColor = OledBackground,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .testTag("add_timer_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = Strings.get("add_timer", language))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // In-App Notification Banner for Active Ringing Timer
            val activeServiceAlert by com.example.service.AlarmAlertService.activeAlertFlow.collectAsState()
            val firedAlarm by viewModel.activeFiredAlarm.collectAsState()
            com.example.ui.components.ActiveRingingInAppBanner(
                activeAlert = activeServiceAlert,
                firedAlarm = firedAlarm,
                forTimerTab = true,
                onDismiss = { viewModel.dismissFiredAlarm() },
                onSnooze = { viewModel.snoozeFiredAlarm() }
            )

            // Next Timer Countdown Banner (Displayed only if running timers exist!)
            if (nextTimerCountdown != null) {
                CountdownBanner(
                    text = nextTimerCountdown,
                    isTimer = true
                )
            }

            var showDeleteSelectedConfirmDialog by remember { mutableStateOf(false) }
            var showDeleteAllConfirmDialog by remember { mutableStateOf(false) }

            // Timer Quick Management Action Bar
            if (timers.isNotEmpty()) {
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
                        Text(
                            text = "تعداد تایمرها: ${timers.size}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (!isSelectionMode) {
                            Surface(
                                color = OledSurfaceElevated,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OledBorder),
                                modifier = Modifier
                                    .clickable { viewModel.enterTimerSelectionMode(null) }
                                    .testTag("enter_timer_selection_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SelectAll,
                                        contentDescription = "انتخاب چندتایی",
                                        tint = NeonAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "انتخاب و مدیریت",
                                        color = NeonAmber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        if (selectedIds.size == timers.size) {
                                            viewModel.exitTimerSelectionMode()
                                        } else {
                                            viewModel.selectAllTimers()
                                        }
                                    }
                                ) {
                                    Text(
                                        text = if (selectedIds.size == timers.size) "لغو همه" else "انتخاب همه",
                                        color = NeonAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                TextButton(
                                    onClick = { showDeleteAllConfirmDialog = true }
                                ) {
                                    Text(
                                        text = "حذف کل تایمرها",
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
                                if (selectedIds.size == timers.size) {
                                    viewModel.exitTimerSelectionMode()
                                } else {
                                    viewModel.selectAllTimers()
                                }
                            }) {
                                Icon(
                                    imageVector = if (selectedIds.size == timers.size) Icons.Default.CheckCircle else Icons.Default.SelectAll,
                                    contentDescription = Strings.get("select_all", language),
                                    tint = NeonAmber
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
                                enabled = selectedIds.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = Strings.get("delete", language),
                                    tint = if (selectedIds.isNotEmpty()) NeonRed else TextMuted
                                )
                            }
                            IconButton(onClick = { viewModel.exitTimerSelectionMode() }) {
                                Text(text = Strings.get("cancel", language), color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Confirm Dialog for deleting selected timers
            if (showDeleteSelectedConfirmDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showDeleteSelectedConfirmDialog = false },
                    title = { Text(text = "حذف تایمرهای انتخاب‌شده", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    text = {
                        Text(
                            text = "آیا مطمئن هستید می‌خواهید ${selectedIds.size} تایمر انتخاب‌شده را حذف کنید؟",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        androidx.compose.material3.Button(
                            onClick = {
                                viewModel.deleteSelectedTimers()
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

            // Confirm Dialog for deleting ALL timers
            if (showDeleteAllConfirmDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showDeleteAllConfirmDialog = false },
                    title = { Text(text = "حذف همه تایمرها", color = NeonRed, fontWeight = FontWeight.Bold) },
                    text = {
                        Text(
                            text = "هشدار: تمامی تایمرهای تعریف‌شده (تعداد ${timers.size}) حذف خواهند شد. این عملیات قابل بازگشت نیست. آیا ادامه می‌دهید؟",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        androidx.compose.material3.Button(
                            onClick = {
                                viewModel.deleteAllTimers()
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

            // Sort Controls Bar (مرتب‌سازی صعودی/نزولی بر اساس زمان تایمر یا ترتیب افزودن)
            if (timers.isNotEmpty() && !isSelectionMode) {
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
                            color = if (sortBy == "TIME") NeonAmber.copy(alpha = 0.2f) else OledSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (sortBy == "TIME") NeonAmber else OledBorder),
                            modifier = Modifier.clickable {
                                viewModel.updateSettings(settings.copy(timerSortBy = "TIME"))
                            }
                        ) {
                            Text(
                                text = "زمان تایمر",
                                color = if (sortBy == "TIME") NeonAmber else TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = if (sortBy == "TIME") FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        // By Added Order Chip
                        Surface(
                            color = if (sortBy == "ADDED") NeonAmber.copy(alpha = 0.2f) else OledSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (sortBy == "ADDED") NeonAmber else OledBorder),
                            modifier = Modifier.clickable {
                                viewModel.updateSettings(settings.copy(timerSortBy = "ADDED"))
                            }
                        ) {
                            Text(
                                text = "ترتیب افزودن",
                                color = if (sortBy == "ADDED") NeonAmber else TextSecondary,
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
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable {
                            viewModel.updateSettings(settings.copy(timerSortAscending = !sortAscending))
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
                                tint = NeonAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (sortAscending) "صعودی" else "نزولی",
                                color = NeonAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Timers List or Empty State
            if (timers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "هیچ تایمری ثبت نشده است",
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
                    itemsIndexed(sortedTimers, key = { index, timer -> if (timer.id != 0L) "${timer.id}" else "timer_$index" }) { index, timer ->
                        TimerItemCard(
                            timer = timer,
                            index = index,
                            totalCount = timers.size,
                            language = language,
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedIds.contains(timer.id),
                            onToggleSelect = { viewModel.toggleTimerSelection(timer.id) },
                            onLongClick = { viewModel.enterTimerSelectionMode(timer.id) },
                            onClick = {
                                if (isSelectionMode) {
                                    viewModel.toggleTimerSelection(timer.id)
                                } else {
                                    editingTimer = timer
                                }
                            },
                            onToggleRun = { viewModel.toggleTimerRunning(timer) },
                            onReset = { viewModel.resetTimer(timer) },
                            onMoveUp = { viewModel.moveTimerUp(timer) },
                            onMoveDown = { viewModel.moveTimerDown(timer) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (isAddingNew || editingTimer != null) {
        AddTimerSheet(
            existingTimer = editingTimer,
            language = language,
            onDismiss = {
                isAddingNew = false
                editingTimer = null
            },
            onSave = { timer ->
                viewModel.saveTimer(timer)
                isAddingNew = false
                editingTimer = null
            }
        )
    }
}

@Composable
fun TimerItemCard(
    timer: TimerEntity,
    index: Int,
    totalCount: Int,
    language: AppLanguage,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onToggleRun: () -> Unit,
    onReset: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val remSecs = timer.remainingSeconds
    val days = remSecs / (24 * 3600)
    val hours = (remSecs % (24 * 3600)) / 3600
    val minutes = (remSecs % 3600) / 60
    val seconds = remSecs % 60

    val timeString = if (days > 0) {
        "${days}d ${String.format("%02d:%02d:%02d", hours, minutes, seconds)}"
    } else {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    val progress = if (timer.totalSeconds > 0) {
        (timer.remainingSeconds.toFloat() / timer.totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                1.dp,
                if (isSelected) NeonAmber else if (timer.isRunning) NeonAmber.copy(alpha = 0.4f) else OledBorder,
                RoundedCornerShape(18.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (timer.isRunning) OledSurfaceElevated else OledSurface
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time & Selection
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelectionMode) {
                        IconButton(onClick = onToggleSelect, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSelected) NeonAmber else TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Column {
                        Text(
                            text = Strings.formatNumber(timeString, language),
                            color = if (timer.isRunning) NeonAmber else TextPrimary,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (timer.label.isNotBlank()) {
                            Text(
                                text = timer.label,
                                color = NeonCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Control Buttons: Play/Pause, Reset, Reorder
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelectionMode) {
                        IconButton(onClick = onMoveUp, enabled = index > 0, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, tint = if (index > 0) NeonAmber else TextMuted)
                        }
                        IconButton(onClick = onMoveDown, enabled = index < totalCount - 1, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, tint = if (index < totalCount - 1) NeonAmber else TextMuted)
                        }
                    } else {
                        // Reset Button
                        IconButton(
                            onClick = onReset,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = Strings.get("reset", language), tint = TextSecondary)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Start/Pause Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (timer.isRunning) NeonRed else NeonAmber)
                                .clickable { onToggleRun() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (timer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = OledBackground,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = if (timer.isRunning) NeonAmber else NeonPurple,
                trackColor = OledSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata row: Repeat, Vibration, Sound
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetadataChip(
                    icon = Icons.Default.Repeat,
                    text = if (timer.repeatType == "DAILY") Strings.get("repeat_daily", language) else Strings.get("repeat_once", language),
                    tint = NeonPurple
                )

                if (timer.vibrationPattern != "OFF") {
                    MetadataChip(
                        icon = Icons.Default.Vibration,
                        text = when (timer.vibrationPattern) {
                            "CONTINUOUS" -> Strings.get("vibe_continuous", language)
                            "INTERMITTENT" -> Strings.get("vibe_intermittent", language)
                            "HEARTBEAT" -> Strings.get("vibe_heartbeat", language)
                            "STROBE" -> Strings.get("vibe_strobe", language)
                            else -> Strings.get("vibration", language)
                        },
                        tint = NeonAmber
                    )
                }

                val timerSoundDisplayName = if (!timer.customSoundTitle.isNullOrBlank()) {
                    timer.customSoundTitle
                } else {
                    SoundAndVibrationManager.soundOptions.find { it.id == timer.soundId }?.let {
                        if (language == AppLanguage.ENGLISH) it.nameEn else it.nameFa
                    } ?: if (timer.soundId.startsWith("content://") || timer.soundId.startsWith("file://")) {
                        Strings.get("custom_device_sound", language)
                    } else {
                        timer.soundId.replace("_", " ")
                    }
                }
                MetadataChip(
                    icon = Icons.Default.MusicNote,
                    text = timerSoundDisplayName,
                    tint = NeonCyan
                )
            }
        }
    }
}
