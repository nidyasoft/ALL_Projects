package com.example.ui.timer

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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundAndVibrationManager
import com.example.data.model.TimerEntity
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import com.example.ui.alarm.SectionTitle
import com.example.ui.alarm.TimeNumberPicker
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
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
fun AddTimerSheet(
    existingTimer: TimerEntity?,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (TimerEntity) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val existingSecs = existingTimer?.totalSeconds ?: 300L
    var days by remember { mutableIntStateOf((existingSecs / (24 * 3600)).toInt()) }
    var hours by remember { mutableIntStateOf(((existingSecs % (24 * 3600)) / 3600).toInt()) }
    var minutes by remember { mutableIntStateOf(((existingSecs % 3600) / 60).toInt()) }
    var seconds by remember { mutableIntStateOf((existingSecs % 60).toInt()) }

    var label by remember { mutableStateOf(existingTimer?.label ?: "") }
    val defaultTimerSound = remember { com.example.data.local.SettingsManager.getInstance(context).settings.value.defaultTimerSound }
    var soundId by remember { mutableStateOf(existingTimer?.soundId ?: defaultTimerSound) }
    var customSoundTitle by remember { mutableStateOf(existingTimer?.customSoundTitle) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { pickedUri ->
            val (savedUri, title) = com.example.audio.AudioFileManager.saveAudioLocally(context, pickedUri)
            soundId = savedUri
            customSoundTitle = title
            SoundAndVibrationManager.previewSound(context, savedUri, 0.85f)
        }
    }

    var vibrationPattern by remember { mutableStateOf(existingTimer?.vibrationPattern ?: "CONTINUOUS") }
    var autoDelete by remember { mutableStateOf(existingTimer?.autoDeleteAfterUse ?: false) }
    var repeatType by remember { mutableStateOf(existingTimer?.repeatType ?: "ONCE") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = OledSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(NeonAmber.copy(alpha = 0.5f))
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
                    text = Strings.get("add_timer", language),
                    color = NeonAmber,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = Strings.get("cancel", language), tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Dial Picker: Days, Hours, Minutes, Seconds
            Card(
                colors = CardDefaults.cardColors(containerColor = OledSurfaceElevated),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonAmber.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Days
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = Strings.get("days", language), color = TextSecondary, fontSize = 11.sp)
                                TimeNumberPicker(value = days, range = 0..30, language = language, accentColor = NeonAmber, onValueChange = { days = it })
                            }

                            Text(":", color = NeonAmber, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                            // Hours
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = Strings.get("hours", language), color = TextSecondary, fontSize = 11.sp)
                                TimeNumberPicker(value = hours, range = 0..23, language = language, accentColor = NeonAmber, onValueChange = { hours = it })
                            }

                            Text(":", color = NeonAmber, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                            // Minutes
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = Strings.get("minutes", language), color = TextSecondary, fontSize = 11.sp)
                                TimeNumberPicker(value = minutes, range = 0..59, language = language, accentColor = NeonAmber, onValueChange = { minutes = it })
                            }

                            Text(":", color = NeonAmber, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                            // Seconds
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = Strings.get("seconds", language), color = TextSecondary, fontSize = 11.sp)
                                TimeNumberPicker(value = seconds, range = 0..59, language = language, accentColor = NeonAmber, onValueChange = { seconds = it })
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

            // Label
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text(Strings.get("label", language)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("timer_label_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonAmber,
                    unfocusedBorderColor = OledBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = NeonAmber,
                    unfocusedLabelColor = TextSecondary,
                    focusedContainerColor = OledSurfaceElevated,
                    unfocusedContainerColor = OledSurfaceElevated
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ringtone
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
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = if (isSelected) OledBackground else NeonAmber, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonAmber,
                            containerColor = OledSurfaceElevated
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Custom Audio File Card (Choose from device storage for Timer)
            val isCustomSound = soundId.startsWith("content://") || soundId.startsWith("file://")
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isCustomSound) NeonAmber.copy(alpha = 0.12f) else OledSurfaceElevated
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isCustomSound) NeonAmber else OledBorder,
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
                                .background(if (isCustomSound) NeonAmber else OledSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LibraryMusic,
                                contentDescription = null,
                                tint = if (isCustomSound) OledBackground else NeonAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (isCustomSound) (customSoundTitle ?: Strings.get("custom_device_sound", language)) else Strings.get("pick_from_device", language),
                                color = if (isCustomSound) NeonAmber else TextPrimary,
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
                                    tint = NeonAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Button(
                            onClick = { audioPickerLauncher.launch("audio/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCustomSound) NeonAmber else OledSurfaceVariant,
                                contentColor = if (isCustomSound) OledBackground else NeonAmber
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

            // Vibration Mode
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

            // Auto-delete on finish
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

            // Repeat Mode
            SectionTitle(icon = Icons.Default.Repeat, title = Strings.get("repeat", language))
            val timerRepeats = listOf(
                "ONCE" to Strings.get("repeat_once", language),
                "DAILY" to Strings.get("repeat_daily", language)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                timerRepeats.forEach { (type, title) ->
                    val isSelected = repeatType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { repeatType = type },
                        label = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                color = if (isSelected) OledBackground else TextPrimary
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonAmber,
                            containerColor = OledSurfaceElevated
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: Cancel and Save
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("cancel_timer_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text(text = Strings.get("cancel", language), fontSize = 15.sp)
                }

                Button(
                    onClick = {
                        val total = (days * 24L * 3600) + (hours * 3600L) + (minutes * 60L) + seconds
                        val validTotal = if (total <= 0) 60L else total
                        val timer = (existingTimer ?: TimerEntity(totalSeconds = validTotal, remainingSeconds = validTotal)).copy(
                            totalSeconds = validTotal,
                            remainingSeconds = validTotal,
                            label = label,
                            soundId = soundId,
                            customSoundTitle = customSoundTitle,
                            vibrationPattern = vibrationPattern,
                            autoDeleteAfterUse = autoDelete,
                            repeatType = repeatType,
                            isRunning = false
                        )
                        onSave(timer)
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                        .testTag("save_timer_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonAmber,
                        contentColor = OledBackground
                    )
                ) {
                    Text(text = Strings.get("save", language), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
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

