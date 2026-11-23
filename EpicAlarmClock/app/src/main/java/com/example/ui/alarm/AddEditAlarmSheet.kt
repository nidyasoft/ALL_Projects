package com.example.ui.alarm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.verticalScroll
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import java.util.Calendar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioFileManager
import com.example.audio.SoundAndVibrationManager
import com.example.calendar.TripleCalendarHelper
import com.example.data.model.AlarmEntity
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.OledBackground
import com.example.ui.theme.OledBorder
import com.example.ui.theme.OledSurface
import com.example.ui.theme.OledSurfaceElevated
import com.example.ui.theme.OledSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditAlarmSheet(
    existingAlarm: AlarmEntity?,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (AlarmEntity) -> Unit,
    initialTripleDate: com.example.calendar.TripleDate? = null,
    initialIsMeeting: Boolean = false,
    initialCalendarType: String = "SOLAR"
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentCal = remember { java.util.Calendar.getInstance() }
    var hour by remember { mutableIntStateOf(existingAlarm?.hour ?: currentCal.get(java.util.Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(existingAlarm?.minute ?: currentCal.get(java.util.Calendar.MINUTE)) }
    var isPm by remember { mutableStateOf(hour >= 12) }
    var hour12 by remember { mutableIntStateOf(if (hour == 0) 12 else if (hour > 12) hour - 12 else hour) }

    var label by remember { mutableStateOf(existingAlarm?.label ?: "") }
    val defaultAlarmSound = remember { com.example.data.local.SettingsManager.getInstance(context).settings.value.defaultAlarmSound }
    var soundId by remember { mutableStateOf(existingAlarm?.soundId ?: defaultAlarmSound) }
    var customSoundTitle by remember { mutableStateOf(existingAlarm?.customSoundTitle) }
    var isPreviewingCustomSound by remember { mutableStateOf(false) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { pickedUri ->
            val (savedUri, title) = AudioFileManager.saveAudioLocally(context, pickedUri)
            soundId = savedUri
            customSoundTitle = title
            isPreviewingCustomSound = true
            SoundAndVibrationManager.previewSound(context, savedUri, 0.85f)
        }
    }

    var vibrationPattern by remember { mutableStateOf(existingAlarm?.vibrationPattern ?: "INTERMITTENT") }
    var autoDelete by remember { mutableStateOf(existingAlarm?.autoDeleteAfterUse ?: false) }

    val defaultRepeat = if (initialIsMeeting) "EVENT" else (existingAlarm?.repeatType ?: "DAILY")
    var repeatType by remember { mutableStateOf(defaultRepeat) }

    // Custom days
    val allWeekDays = listOf("SAT", "SUN", "MON", "TUE", "WED", "THU", "FRI")
    val allWeekDaysNamesFa = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
    val initialCustomDays = existingAlarm?.customDays?.split(",")?.toSet() ?: allWeekDays.toSet()
    var selectedDays by remember { mutableStateOf(initialCustomDays) }

    // Shift alarm configuration
    val todayTriple = remember { TripleCalendarHelper.getTodayTripleDate(language) }
    val baseTriple = initialTripleDate ?: todayTriple

    var shiftIntervalValue by remember { mutableIntStateOf(existingAlarm?.shiftIntervalValue ?: 1) }
    var shiftIntervalUnit by remember { mutableStateOf(existingAlarm?.shiftIntervalUnit ?: "DAYS") } // "DAYS", "HOURS", "MINUTES"
    var shiftCalendarType by remember { mutableStateOf(existingAlarm?.shiftAnchorCalendar ?: "SOLAR") }
    var anchorYear by remember { mutableIntStateOf(existingAlarm?.shiftAnchorYear ?: baseTriple.solarYear) }
    var anchorMonth by remember { mutableIntStateOf(existingAlarm?.shiftAnchorMonth ?: baseTriple.solarMonth) }
    var anchorDay by remember { mutableIntStateOf(existingAlarm?.shiftAnchorDay ?: baseTriple.solarDay) }

    // Meeting / Event Alarm configuration
    var isMeeting by remember { mutableStateOf(existingAlarm?.isMeeting ?: initialIsMeeting) }
    val initEvCal = existingAlarm?.eventCalendar ?: initialCalendarType
    var eventCalendar by remember { mutableStateOf(initEvCal) }
    var eventYear by remember {
        mutableIntStateOf(
            existingAlarm?.eventYear?.takeIf { it > 0 } ?: when (initEvCal) {
                "GREGORIAN" -> baseTriple.gregorianYear
                "LUNAR" -> baseTriple.lunarYear
                else -> baseTriple.solarYear
            }
        )
    }
    var eventMonth by remember {
        mutableIntStateOf(
            existingAlarm?.eventMonth?.takeIf { it > 0 } ?: when (initEvCal) {
                "GREGORIAN" -> baseTriple.gregorianMonth
                "LUNAR" -> baseTriple.lunarMonth
                else -> baseTriple.solarMonth
            }
        )
    }
    var eventDay by remember {
        mutableIntStateOf(
            existingAlarm?.eventDay?.takeIf { it > 0 } ?: when (initEvCal) {
                "GREGORIAN" -> baseTriple.gregorianDay
                "LUNAR" -> baseTriple.lunarDay
                else -> baseTriple.solarDay
            }
        )
    }
    var eventNote by remember { mutableStateOf(existingAlarm?.eventNote ?: "") }


    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = OledSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingAlarm == null) Strings.get("add_alarm", language) else Strings.get("edit_alarm", language),
                    color = NeonCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onDismiss() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = Strings.get("cancel", language), tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Selector (Hour, Minute, AM/PM)
            Card(
                colors = CardDefaults.cardColors(containerColor = OledSurfaceElevated),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Hour Picker Box
                            TimeNumberPicker(
                                value = hour12,
                                range = 1..12,
                                language = language,
                                onValueChange = {
                                    hour12 = it
                                    hour = if (isPm) {
                                        if (it == 12) 12 else it + 12
                                    } else {
                                        if (it == 12) 0 else it
                                    }
                                }
                            )

                            Text(
                                text = ":",
                                color = NeonCyan,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            // Minute Picker Box
                            TimeNumberPicker(
                                value = minute,
                                range = 0..59,
                                language = language,
                                onValueChange = { minute = it }
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // AM/PM Toggle
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                AmPmButton(
                                    text = "AM",
                                    isSelected = !isPm,
                                    onClick = {
                                        isPm = false
                                        hour = if (hour12 == 12) 0 else hour12
                                    }
                                )
                                AmPmButton(
                                    text = "PM",
                                    isSelected = isPm,
                                    onClick = {
                                        isPm = true
                                        hour = if (hour12 == 12) 12 else hour12 + 12
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اسکرول با کشیدن انگشت (Scroll / Drag) یا لمس برای تایپ عدد",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Label Input
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text(Strings.get("label", language)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alarm_label_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = OledBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = NeonCyan,
                    unfocusedLabelColor = TextSecondary,
                    focusedContainerColor = OledSurfaceElevated,
                    unfocusedContainerColor = OledSurfaceElevated
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ringtone Selector
            SectionTitle(icon = Icons.Default.MusicNote, title = Strings.get("ringtone", language))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SoundAndVibrationManager.soundOptions.forEach { sound ->
                    val isSelected = soundId == sound.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            soundId = sound.id
                            customSoundTitle = null
                            SoundAndVibrationManager.previewSound(sound.id)
                        },
                        label = {
                            Text(
                                text = if (language == AppLanguage.ENGLISH) sound.nameEn else sound.nameFa,
                                fontSize = 12.sp,
                                color = if (isSelected) OledBackground else TextPrimary
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isSelected) OledBackground else NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            containerColor = OledSurfaceElevated
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) NeonCyan else OledBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Custom Audio File Card (Choose from device storage)
            val isCustomSound = soundId.startsWith("content://") || soundId.startsWith("file://")
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isCustomSound) NeonCyan.copy(alpha = 0.12f) else OledSurfaceElevated
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isCustomSound) NeonCyan else OledBorder,
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isCustomSound) NeonCyan else OledSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LibraryMusic,
                                contentDescription = null,
                                tint = if (isCustomSound) OledBackground else NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (isCustomSound) (customSoundTitle ?: Strings.get("custom_device_sound", language)) else Strings.get("pick_from_device", language),
                                color = if (isCustomSound) NeonCyan else TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isCustomSound) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isCustomSound) {
                                Text(
                                    text = Strings.get("custom_device_sound", language),
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (isCustomSound) {
                            IconButton(
                                onClick = {
                                    SoundAndVibrationManager.previewSound(context, soundId, 0.85f)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Preview",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Button(
                            onClick = { audioPickerLauncher.launch("audio/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCustomSound) NeonCyan else OledSurfaceVariant,
                                contentColor = if (isCustomSound) OledBackground else NeonCyan
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCustomSound) Strings.get("change_audio_file", language) else Strings.get("pick_from_device", language),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vibration Mode Selector
            SectionTitle(icon = Icons.Default.Vibration, title = Strings.get("vibration", language))
            Card(
                colors = CardDefaults.cardColors(containerColor = OledSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonPurple.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SoundAndVibrationManager.vibrationOptions.forEach { opt ->
                            val isSelected = vibrationPattern == opt.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    vibrationPattern = opt.id
                                    SoundAndVibrationManager.previewVibration(context, opt.id)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (opt.id == "OFF") Icons.Default.Close else Icons.Default.Vibration,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) OledBackground else NeonPurple
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (language == AppLanguage.ENGLISH) opt.nameEn else opt.nameFa,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) OledBackground else TextPrimary
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonPurple,
                                    containerColor = OledSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) NeonPurple else OledBorder
                                )
                            )
                        }
                    }

                    // Test vibration button with active pattern description
                    val currentDesc = SoundAndVibrationManager.vibrationOptions.firstOrNull { it.id == vibrationPattern }?.descFa ?: ""
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentDesc,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = {
                                SoundAndVibrationManager.previewVibration(context, vibrationPattern)
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPurple),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تست ویبره", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-delete after 1 use
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(OledSurfaceElevated)
                    .border(1.dp, OledBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = Strings.get("auto_delete", language),
                    color = TextPrimary,
                    fontSize = 14.sp
                )
                Switch(
                    checked = autoDelete,
                    onCheckedChange = { autoDelete = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = OledBackground,
                        checkedTrackColor = NeonAmber,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = OledSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Repeat Mode Selector
            SectionTitle(icon = Icons.Default.Repeat, title = Strings.get("repeat", language))
            val repeatModes = listOf(
                "ONCE" to Strings.get("repeat_once", language),
                "DAILY" to Strings.get("repeat_daily", language),
                "SAT_THU" to Strings.get("repeat_sat_thu", language),
                "MON_FRI" to Strings.get("repeat_mon_fri", language),
                "CUSTOM" to Strings.get("repeat_custom", language),
                "SHIFT" to "چرخه شیفت (Shift Cycle)",
                "EVENT" to "قرار ملاقات / رویداد (Meeting/Event)"
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeatModes.forEach { (type, labelText) ->
                    val isSelected = repeatType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { repeatType = type },
                        label = {
                            Text(
                                text = labelText,
                                fontSize = 12.sp,
                                color = if (isSelected) OledBackground else TextPrimary
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (type) {
                                "SHIFT" -> NeonPink
                                "EVENT" -> NeonAmber
                                else -> NeonCyan
                            },
                            containerColor = OledSurfaceElevated
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) NeonCyan else OledBorder
                        )
                    )
                }
            }

            // Custom Days selection chips
            AnimatedVisibility(visible = repeatType == "CUSTOM") {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = Strings.get("repeat_custom", language),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        allWeekDays.forEachIndexed { i, dayKey ->
                            val isDaySelected = selectedDays.contains(dayKey)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isDaySelected) NeonCyan else OledSurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (isDaySelected) NeonCyan else OledBorder,
                                        CircleShape
                                    )
                                    .clickable {
                                        selectedDays = if (isDaySelected) {
                                            selectedDays - dayKey
                                        } else {
                                            selectedDays + dayKey
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = allWeekDaysNamesFa[i],
                                    color = if (isDaySelected) OledBackground else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Shift Cycle Alarm Configuration Section (فواصل زمانی + تاریخ و ساعت شروع چرخه)
            AnimatedVisibility(visible = repeatType == "SHIFT") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = OledSurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .border(1.dp, NeonPink.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Repeat, contentDescription = null, tint = NeonPink, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تنظیمات چرخه شیفت (Shift Cycle)",
                                color = NeonPink,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "۱. آلارم در چه فواصل زمانی فعال شود؟",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Interval Unit selector (DAYS, HOURS, MINUTES)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "DAYS" to "هر چند روز",
                                "HOURS" to "هر چند ساعت",
                                "MINUTES" to "هر چند دقیقه"
                            ).forEach { (unitKey, unitLabel) ->
                                val isUnitSelected = shiftIntervalUnit == unitKey
                                FilterChip(
                                    selected = isUnitSelected,
                                    onClick = { shiftIntervalUnit = unitKey },
                                    label = {
                                        Text(
                                            text = unitLabel,
                                            fontSize = 11.sp,
                                            color = if (isUnitSelected) OledBackground else TextPrimary
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonPink,
                                        containerColor = OledSurfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Interval Value Stepper and Presets
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "فاصله تکرار:",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            IconButton(
                                onClick = { if (shiftIntervalValue > 1) shiftIntervalValue-- },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(OledSurfaceVariant)
                            ) {
                                Text("-", color = NeonPink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedTextField(
                                value = shiftIntervalValue.toString(),
                                onValueChange = {
                                    val v = it.toIntOrNull()
                                    if (v != null && v in 1..9999) shiftIntervalValue = v
                                },
                                modifier = Modifier.width(80.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonPink,
                                    unfocusedBorderColor = OledBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            IconButton(
                                onClick = { shiftIntervalValue++ },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(OledSurfaceVariant)
                            ) {
                                Text("+", color = NeonPink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = when (shiftIntervalUnit) {
                                    "MINUTES" -> "دقیقه یکبار"
                                    "HOURS" -> "ساعت یکبار"
                                    else -> "روز یکبار"
                                },
                                color = NeonPink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Quick presets chips based on unit
                        val presets = when (shiftIntervalUnit) {
                            "MINUTES" -> listOf(15, 30, 45, 60, 90)
                            "HOURS" -> listOf(4, 6, 8, 12, 24, 36)
                            else -> listOf(1, 2, 3, 4, 7, 14)
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "گزینه‌های پرکاربرد:", color = TextMuted, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterVertically))
                            presets.forEach { p ->
                                val isP = shiftIntervalValue == p
                                Surface(
                                    color = if (isP) NeonPink else OledSurfaceVariant,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable { shiftIntervalValue = p }
                                ) {
                                    Text(
                                        text = "$p",
                                        color = if (isP) Color.White else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = OledBorder, thickness = 0.5.dp)

                        Text(
                            text = "۲. تاریخ و تقویم شروع چرخه (Cycle Start Date):",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Anchor Calendar Type (Solar, Gregorian, Lunar)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "SOLAR" to "تقویم شمسی",
                                "GREGORIAN" to "تقویم میلادی",
                                "LUNAR" to "تقویم قمری"
                            ).forEach { (calKey, calTitle) ->
                                val isSelected = shiftCalendarType == calKey
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        shiftCalendarType = calKey
                                        val today = TripleCalendarHelper.getTodayTripleDate(language)
                                        when (calKey) {
                                            "SOLAR" -> { anchorYear = today.solarYear; anchorMonth = today.solarMonth; anchorDay = today.solarDay }
                                            "GREGORIAN" -> { anchorYear = today.gregorianYear; anchorMonth = today.gregorianMonth; anchorDay = today.gregorianDay }
                                            "LUNAR" -> { anchorYear = today.lunarYear; anchorMonth = today.lunarMonth; anchorDay = today.lunarDay }
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = calTitle,
                                            fontSize = 11.sp,
                                            color = if (isSelected) OledBackground else TextPrimary
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan,
                                        containerColor = OledSurfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Shift start date pickers (Year, Month, Day)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = anchorYear.toString(),
                                onValueChange = { anchorYear = it.toIntOrNull() ?: anchorYear },
                                label = { Text("سال") },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonPink,
                                    unfocusedBorderColor = OledBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            OutlinedTextField(
                                value = anchorMonth.toString(),
                                onValueChange = { anchorMonth = (it.toIntOrNull() ?: anchorMonth).coerceIn(1, 12) },
                                label = { Text("ماه") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonPink,
                                    unfocusedBorderColor = OledBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            OutlinedTextField(
                                value = anchorDay.toString(),
                                onValueChange = { anchorDay = (it.toIntOrNull() ?: anchorDay).coerceIn(1, 31) },
                                label = { Text("روز") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonPink,
                                    unfocusedBorderColor = OledBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        // Live smart preview summary
                        val unitTitleFa = when (shiftIntervalUnit) {
                            "MINUTES" -> "دقیقه"
                            "HOURS" -> "ساعت"
                            else -> "روز"
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonPink.copy(alpha = 0.12f))
                                .border(1.dp, NeonPink.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "⏰ آلارم بر اساس تنظیمات فوق از تاریخ $anchorYear/$anchorMonth/$anchorDay ساعت ${String.format("%02d:%02d", hour, minute)} شروع شده و در فواصل منظم هر $shiftIntervalValue $unitTitleFa فعال خواهد شد.",
                                color = NeonPink,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Meeting / Event Alarm Configuration Section
            AnimatedVisibility(visible = repeatType == "EVENT") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = OledSurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "قرار ملاقات و رویداد تقویم (Meeting / Event)",
                                color = NeonCyan,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "نوع تقویم رویداد:",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "SOLAR" to "تقویم خورشیدی (شمسی)",
                                "GREGORIAN" to "تقویم میلادی",
                                "LUNAR" to "تقویم قمری"
                            ).forEach { (calKey, calTitle) ->
                                val isSelected = eventCalendar == calKey
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        eventCalendar = calKey
                                        val today = TripleCalendarHelper.getTodayTripleDate(language)
                                        when (calKey) {
                                            "SOLAR" -> { eventYear = today.solarYear; eventMonth = today.solarMonth; eventDay = today.solarDay }
                                            "GREGORIAN" -> { eventYear = today.gregorianYear; eventMonth = today.gregorianMonth; eventDay = today.gregorianDay }
                                            "LUNAR" -> { eventYear = today.lunarYear; eventMonth = today.lunarMonth; eventDay = today.lunarDay }
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = calTitle,
                                            fontSize = 11.sp,
                                            color = if (isSelected) OledBackground else TextPrimary
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan,
                                        containerColor = OledSurfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Date Pickers (Year, Month, Day)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = eventYear.toString(),
                                onValueChange = { eventYear = it.toIntOrNull() ?: eventYear },
                                label = { Text("سال") },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = OledBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            OutlinedTextField(
                                value = eventMonth.toString(),
                                onValueChange = { eventMonth = (it.toIntOrNull() ?: eventMonth).coerceIn(1, 12) },
                                label = { Text("ماه") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = OledBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            OutlinedTextField(
                                value = eventDay.toString(),
                                onValueChange = { eventDay = (it.toIntOrNull() ?: eventDay).coerceIn(1, 31) },
                                label = { Text("روز") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = OledBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        // Quick buttons: Today, Tomorrow, Next Week
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val t = TripleCalendarHelper.getTodayTripleDate(language)
                                    when (eventCalendar) {
                                        "SOLAR" -> { eventYear = t.solarYear; eventMonth = t.solarMonth; eventDay = t.solarDay }
                                        "GREGORIAN" -> { eventYear = t.gregorianYear; eventMonth = t.gregorianMonth; eventDay = t.gregorianDay }
                                        "LUNAR" -> { eventYear = t.lunarYear; eventMonth = t.lunarMonth; eventDay = t.lunarDay }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("امروز", fontSize = 11.sp, color = NeonCyan)
                            }
                            OutlinedButton(
                                onClick = {
                                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                                    val t = TripleCalendarHelper.getTripleDateForMillis(cal.timeInMillis, java.util.TimeZone.getDefault(), language)
                                    when (eventCalendar) {
                                        "SOLAR" -> { eventYear = t.solarYear; eventMonth = t.solarMonth; eventDay = t.solarDay }
                                        "GREGORIAN" -> { eventYear = t.gregorianYear; eventMonth = t.gregorianMonth; eventDay = t.gregorianDay }
                                        "LUNAR" -> { eventYear = t.lunarYear; eventMonth = t.lunarMonth; eventDay = t.lunarDay }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("فردا", fontSize = 11.sp, color = NeonCyan)
                            }
                            OutlinedButton(
                                onClick = {
                                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }
                                    val t = TripleCalendarHelper.getTripleDateForMillis(cal.timeInMillis, java.util.TimeZone.getDefault(), language)
                                    when (eventCalendar) {
                                        "SOLAR" -> { eventYear = t.solarYear; eventMonth = t.solarMonth; eventDay = t.solarDay }
                                        "GREGORIAN" -> { eventYear = t.gregorianYear; eventMonth = t.gregorianMonth; eventDay = t.gregorianDay }
                                        "LUNAR" -> { eventYear = t.lunarYear; eventMonth = t.lunarMonth; eventDay = t.lunarDay }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("هفته بعد", fontSize = 11.sp, color = NeonCyan)
                            }
                        }

                        // Event note / details
                        OutlinedTextField(
                            value = eventNote,
                            onValueChange = { eventNote = it },
                            label = { Text("توضیحات و یادداشت قرار ملاقات (اختیاری)") },
                            placeholder = { Text("محل جلسه، موضوع گفتگو یا نکات مهم...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = OledBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        // Smart Summary
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonCyan.copy(alpha = 0.12f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "📅 این قرار ملاقات در تقویم رویدادها ثبت شده و آلارم در تاریخ $eventYear/$eventMonth/$eventDay ساعت ${String.format("%02d:%02d", hour, minute)} زنگ خواهد زد.",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: Cancel and Save
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("cancel_alarm_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextSecondary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(OledBorder, OledBorder)))
                ) {
                    Text(text = Strings.get("cancel", language), fontSize = 15.sp)
                }

                Button(
                    onClick = {
                        val alarm = (existingAlarm ?: AlarmEntity(hour = hour, minute = minute)).copy(
                            hour = hour,
                            minute = minute,
                            label = label,
                            soundId = soundId,
                            vibrationPattern = vibrationPattern,
                            autoDeleteAfterUse = autoDelete,
                            repeatType = repeatType,
                            customDays = selectedDays.joinToString(","),
                            shiftCycleDays = shiftIntervalValue,
                            shiftIntervalValue = shiftIntervalValue,
                            shiftIntervalUnit = shiftIntervalUnit,
                            shiftStartHour = hour,
                            shiftStartMinute = minute,
                            shiftAnchorCalendar = shiftCalendarType,
                            shiftAnchorYear = anchorYear,
                            shiftAnchorMonth = anchorMonth,
                            shiftAnchorDay = anchorDay,
                            shiftTimesJson = "$hour:$minute",
                            eventCalendar = eventCalendar,
                            eventYear = eventYear,
                            eventMonth = eventMonth,
                            eventDay = eventDay,
                            isMeeting = (repeatType == "EVENT"),
                            eventNote = eventNote,
                            customSoundTitle = customSoundTitle,
                            isEnabled = true,
                            skippedUntilMillis = 0L
                        )
                        onSave(alarm)
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                        .testTag("save_alarm_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = OledBackground
                    )
                ) {
                    Text(
                        text = Strings.get("save", language),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

        }
    }
}

@Composable
fun SectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
        Text(text = title, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TimeNumberPicker(
    value: Int,
    range: IntRange,
    language: AppLanguage,
    accentColor: Color = NeonCyan,
    onValueChange: (Int) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var inputText by remember(value) { mutableStateOf(String.format("%02d", value)) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val dragThreshold = 22f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        IconButton(
            onClick = {
                val next = if (value + 1 > range.last) range.first else value + 1
                onValueChange(next)
            },
            modifier = Modifier.size(32.dp)
        ) {
            Text("▲", color = accentColor, fontSize = 16.sp)
        }

        Box(
            modifier = Modifier
                .size(width = 68.dp, height = 56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(OledSurfaceVariant)
                .border(1.5.dp, if (isEditing) NeonAmber else accentColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .pointerInput(range, value) {
                    detectVerticalDragGestures(
                        onDragEnd = { dragAccumulator = 0f },
                        onDragCancel = { dragAccumulator = 0f }
                    ) { change, dragAmount ->
                        change.consume()
                        dragAccumulator += dragAmount
                        if (dragAccumulator <= -dragThreshold) {
                            // Drag up -> increment
                            val next = if (value + 1 > range.last) range.first else value + 1
                            onValueChange(next)
                            dragAccumulator = 0f
                        } else if (dragAccumulator >= dragThreshold) {
                            // Drag down -> decrement
                            val prev = if (value - 1 < range.first) range.last else value - 1
                            onValueChange(prev)
                            dragAccumulator = 0f
                        }
                    }
                }
                .clickable { isEditing = true },
            contentAlignment = Alignment.Center
        ) {
            if (isEditing) {
                androidx.compose.foundation.text.BasicTextField(
                    value = inputText,
                    onValueChange = { newStr ->
                        val filtered = newStr.filter { it.isDigit() }.take(2)
                        inputText = filtered
                        val parsed = filtered.toIntOrNull()
                        if (parsed != null && parsed in range) {
                            onValueChange(parsed)
                        }
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = {
                            val parsed = inputText.toIntOrNull()
                            if (parsed != null) {
                                onValueChange(parsed.coerceIn(range.first, range.last))
                            }
                            isEditing = false
                        }
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    singleLine = true
                )
            } else {
                Text(
                    text = Strings.formatNumber(String.format("%02d", value), language),
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        IconButton(
            onClick = {
                val prev = if (value - 1 < range.first) range.last else value - 1
                onValueChange(prev)
            },
            modifier = Modifier.size(32.dp)
        ) {
            Text("▼", color = accentColor, fontSize = 16.sp)
        }
    }
}

@Composable
fun AmPmButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NeonCyan else OledSurfaceVariant)
            .border(1.dp, if (isSelected) NeonCyan else OledBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) OledBackground else TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun queryAudioTitle(context: Context, uri: Uri): String {
    try {
        val ringtone = RingtoneManager.getRingtone(context, uri)
        val title = ringtone?.getTitle(context)
        if (!title.isNullOrBlank()) return title
    } catch (_: Throwable) {}
    return uri.lastPathSegment ?: "آهنگ انتخابی"
}

