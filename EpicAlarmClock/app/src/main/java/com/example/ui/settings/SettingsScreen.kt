package com.example.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.AlarmOff
import com.example.alarm.AlarmBackupManager
import com.example.data.model.AlarmEntity
import com.example.sensor.GyroscopeMotionTracker
import com.example.guardian.GuardianAlertManager
import com.example.util.ApkShareHelper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.security.AppLockManager
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundAndVibrationManager
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import com.example.ui.MainViewModel
import com.example.ui.components.AnalogClockDial
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.EpicTheme
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
import com.example.widget.AnalogClockWidgetProvider
import com.example.widget.TripleDateWidgetProvider
import com.example.ui.components.ColorPickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.mutableLongStateOf

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onOpenOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.appSettings.collectAsState()
    val alarms by viewModel.alarms.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val themeMode by viewModel.currentThemeMode.collectAsState()
    val currentTimeMillis by viewModel.currentTimeMillis.collectAsState()
    val theme = EpicTheme.colors
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var isSharingApk by remember { mutableStateOf(false) }

    var activeSoundTarget by remember { mutableStateOf<String?>(null) } // "alarm", "timer", "stopwatch"
    var showSoundPickerDialog by remember { mutableStateOf(false) }
    var isPreviewingSound by remember { mutableStateOf(false) }

    // State for 16 Million + 32 Presets Full Color Picker Dialog
    var fullColorPickerTarget by remember { mutableStateOf<String?>(null) }
    var fullColorPickerTitle by remember { mutableStateOf("") }
    var fullColorPickerInitialColor by remember { mutableLongStateOf(0L) }

    // State for In-App PIN Configuration Dialog
    var showAppPinSetupDialog by remember { mutableStateOf(false) }
    var pinDialogInput by remember { mutableStateOf("") }
    var pinDialogConfirmInput by remember { mutableStateOf("") }
    var pinDialogError by remember { mutableStateOf<String?>(null) }

    // Launcher for picking user audio from device storage
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { pickedUri ->
            val (savedUri, title) = com.example.audio.AudioFileManager.saveAudioLocally(context, pickedUri)
            when (activeSoundTarget) {
                "alarm" -> viewModel.updateSettings(settings.copy(defaultAlarmSound = savedUri, defaultAlarmSoundTitle = title))
                "timer" -> viewModel.updateSettings(settings.copy(defaultTimerSound = savedUri, defaultTimerSoundTitle = title))
                "stopwatch" -> viewModel.updateSettings(settings.copy(defaultStopwatchSound = savedUri, defaultStopwatchSoundTitle = title))
            }
            showSoundPickerDialog = false
            AnalogClockWidgetProvider.updateAllWidgets(context)
        }
    }

    var testSmsNotice by remember { mutableStateOf<String?>(null) }
    var backupRestoreNotice by remember { mutableStateOf<String?>(null) }
    var backupRestoreIsError by remember { mutableStateOf(false) }
    var pendingRestoreAlarms by remember { mutableStateOf<List<AlarmEntity>?>(null) }

    val exportAlarmsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            val result = AlarmBackupManager.exportAlarmsToUri(context, uri, alarms)
            result.onSuccess { count ->
                backupRestoreIsError = false
                backupRestoreNotice = "پشتیبان‌گیری با موفقیت انجام شد ($count آلارم ذخیره شد)."
            }.onFailure {
                backupRestoreIsError = true
                backupRestoreNotice = "خطا در ذخیره فایل پشتیبان."
            }
        }
    }

    val importAlarmsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val result = AlarmBackupManager.importAlarmsFromUri(context, uri)
            result.onSuccess { parsed ->
                if (parsed.isEmpty()) {
                    backupRestoreIsError = true
                    backupRestoreNotice = "فایل پشتیبان هیچ آلارمی ندارد."
                } else {
                    pendingRestoreAlarms = parsed
                    backupRestoreNotice = null
                }
            }.onFailure {
                backupRestoreIsError = true
                backupRestoreNotice = "فایل انتخاب‌شده معتبر نیست یا ساختار پشتیبان آلارم را ندارد."
            }
        }
    }

    // Launcher for picking an emergency contact for Guardian Alert
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                        val hasPhoneIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                        val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        val name = if (nameIndex >= 0) it.getString(nameIndex) ?: "" else ""
                        val hasPhone = if (hasPhoneIndex >= 0) it.getInt(hasPhoneIndex) > 0 else false
                        if (hasPhone && idIndex >= 0) {
                            val id = it.getString(idIndex)
                            val phoneCursor = context.contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                arrayOf(id),
                                null
                            )
                            phoneCursor?.use { pc ->
                                if (pc.moveToFirst()) {
                                    val numIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    if (numIdx >= 0) {
                                        val number = pc.getString(numIdx) ?: ""
                                        viewModel.updateSettings(
                                            settings.copy(
                                                guardianPhoneNumber = number,
                                                guardianContactName = name
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (_: Throwable) {}
        }
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            contactPickerLauncher.launch(null)
        }
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.updateSettings(settings.copy(guardianAlertEnabled = true))
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "تنظیمات پیشرفته اپلیکیشن",
                color = theme.accentCyan,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "سفارشی‌سازی ظاهر ساعت آنالوگ، اصوات پیش‌فرض، نوتیفیکیشن‌ها و سیستم",
                color = theme.secondaryText,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Onboarding Re-run Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenOnboarding() }
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = NeonCyan)
                        }
                        Column {
                            Text(
                                text = "راهنمای راه‌اندازی و معرفی امکانات",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "مشاهده مجدد ویزارد راه‌اندازی ۶ مرحله‌ای و مجوزها",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Button(
                        onClick = { onOpenOnboarding() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("مشاهده", color = Color(0xFF0B0E14), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // ================= SECTION 1: ANALOG CLOCK CUSTOMIZATION =================
        item {
            SettingsCard(
                title = "ظاهر ساعت آنالوگ (صفحه اصلی و ویجت)",
                icon = Icons.Default.Palette,
                iconColor = NeonCyan
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Live Clock Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(theme.surfaceElevated)
                            .border(1.dp, theme.border, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val cal = java.util.Calendar.getInstance()
                        AnalogClockDial(
                            hours = cal.get(java.util.Calendar.HOUR),
                            minutes = cal.get(java.util.Calendar.MINUTE),
                            seconds = cal.get(java.util.Calendar.SECOND),
                            milliseconds = (currentTimeMillis % 1000).toInt(),
                            dialSize = (settings.clockSizePx * 0.85f).dp.coerceIn(140.dp, 240.dp),
                            faceColor = Color(settings.clockFaceColor),
                            hourHandColor = Color(settings.hourHandColor),
                            minuteHandColor = Color(settings.minuteHandColor),
                            secondHandColor = Color(settings.secondHandColor),
                            numberColor = Color(settings.numberColor),
                            showNumbers = settings.showNumbers
                        )
                    }

                    // Quick Full Color Palette Launcher Button
                    Button(
                        onClick = {
                            fullColorPickerTarget = "face"
                            fullColorPickerTitle = "پالت پیشرفته ظاهر ساعت آنالوگ (۱۶ میلیون رنگ + ۳۲ رنگ اصلی)"
                            fullColorPickerInitialColor = settings.clockFaceColor
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.accentCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "باز کردن پالت جامع رنگی (۱۶ میلیون رنگ + ۳۲ رنگ اصلی)",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 1. Clock Face Color (رنگ صفحه ساعت)
                    ColorPickerRow(
                        label = "رنگ صفحه ساعت (Face Color)",
                        selectedColor = settings.clockFaceColor,
                        colors = listOf(
                            0xFF0B0E14, // OLED Dark
                            0xFF141E30, // Navy Blue
                            0xFF1B1B2F, // Midnight
                            0xFF0F2027, // Dark Teal
                            0xFF1F1C2C, // Deep Violet
                            0xFF1C0A00, // Espresso
                            0xFFFFFFFF  // Crisp Pure White
                        ),
                        onSelectColor = { color ->
                            viewModel.updateSettings(settings.copy(clockFaceColor = color))
                            AnalogClockWidgetProvider.updateAllWidgets(context)
                        },
                        onOpenFullPalette = {
                            fullColorPickerTarget = "face"
                            fullColorPickerTitle = "انتخاب رنگ صفحه ساعت"
                            fullColorPickerInitialColor = settings.clockFaceColor
                        }
                    )

                    // 2. Hour Hand Color (رنگ عقربه ساعت‌شمار)
                    ColorPickerRow(
                        label = "رنگ عقربه ساعت‌شمار (Hour Hand)",
                        selectedColor = settings.hourHandColor,
                        colors = listOf(0xFF00E5FF, 0xFFFFB300, 0xFFFF3366, 0xFF00E676, 0xFFD500F9, 0xFFFFFFFF, 0xFF0F172A),
                        onSelectColor = { color ->
                            viewModel.updateSettings(settings.copy(hourHandColor = color))
                            AnalogClockWidgetProvider.updateAllWidgets(context)
                        },
                        onOpenFullPalette = {
                            fullColorPickerTarget = "hour"
                            fullColorPickerTitle = "انتخاب رنگ عقربه ساعت‌شمار"
                            fullColorPickerInitialColor = settings.hourHandColor
                        }
                    )

                    // 3. Minute Hand Color (رنگ عقربه دقیقه‌شمار)
                    ColorPickerRow(
                        label = "رنگ عقربه دقیقه‌شمار (Minute Hand)",
                        selectedColor = settings.minuteHandColor,
                        colors = listOf(0xFFD500F9, 0xFF00E5FF, 0xFFFFB300, 0xFF00E676, 0xFFFF3366, 0xFFFFFFFF, 0xFF0284C7),
                        onSelectColor = { color ->
                            viewModel.updateSettings(settings.copy(minuteHandColor = color))
                            AnalogClockWidgetProvider.updateAllWidgets(context)
                        },
                        onOpenFullPalette = {
                            fullColorPickerTarget = "minute"
                            fullColorPickerTitle = "انتخاب رنگ عقربه دقیقه‌شمار"
                            fullColorPickerInitialColor = settings.minuteHandColor
                        }
                    )

                    // 4. Second Hand Color (رنگ عقربه ثانیه‌شمار)
                    ColorPickerRow(
                        label = "رنگ عقربه ثانیه‌شمار (Second Hand)",
                        selectedColor = settings.secondHandColor,
                        colors = listOf(0xFFFF3366, 0xFFFF9100, 0xFF00E5FF, 0xFF00E676, 0xFFFFFF00, 0xFFFFFFFF, 0xFFDC2626),
                        onSelectColor = { color ->
                            viewModel.updateSettings(settings.copy(secondHandColor = color))
                            AnalogClockWidgetProvider.updateAllWidgets(context)
                        },
                        onOpenFullPalette = {
                            fullColorPickerTarget = "second"
                            fullColorPickerTitle = "انتخاب رنگ عقربه ثانیه‌شمار"
                            fullColorPickerInitialColor = settings.secondHandColor
                        }
                    )

                    // 5. Number Color (رنگ شماره‌ها)
                    ColorPickerRow(
                        label = "رنگ شماره‌های ۱ تا ۱۲",
                        selectedColor = settings.numberColor,
                        colors = listOf(0xFF0F172A, 0xFFFFFFFF, 0xFF00E5FF, 0xFFFFB300, 0xFF00E676, 0xFFD500F9, 0xFFFF3366),
                        onSelectColor = { color ->
                            viewModel.updateSettings(settings.copy(numberColor = color))
                            AnalogClockWidgetProvider.updateAllWidgets(context)
                        },
                        onOpenFullPalette = {
                            fullColorPickerTarget = "number"
                            fullColorPickerTitle = "انتخاب رنگ شماره‌های صفحه ساعت"
                            fullColorPickerInitialColor = settings.numberColor
                        }
                    )

                    // 6. Show / Hide Numbers (نمایش شماره‌ها)
                    SettingToggleRow(
                        title = "نمایش شماره‌ها روی صفحه ساعت",
                        subtitle = "اعداد ۱ تا ۱۲ دور صفحه ساعت نمایش داده شوند",
                        checked = settings.showNumbers,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(settings.copy(showNumbers = checked))
                            AnalogClockWidgetProvider.updateAllWidgets(context)
                        }
                    )

                    // 7. Clock Size Slider (اندازه هر ضلع ساعت به پیکسل)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "اندازه ساعت: ${settings.clockSizePx} پیکسل", color = theme.primaryText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "۱۶۰ تا ۳۲۰ px", color = theme.mutedText, fontSize = 11.sp)
                        }
                        Slider(
                            value = settings.clockSizePx.toFloat(),
                            onValueChange = { value ->
                                viewModel.updateSettings(settings.copy(clockSizePx = value.toInt()))
                            },
                            valueRange = 160f..320f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = theme.surfaceElevated
                            )
                        )
                    }

                    // 8. Toggles for In-App Calendar (تنظیمات تاریخ‌های داخل اپلیکیشن)
                    Divider(color = theme.border, thickness = 0.5.dp)
                    Text(text = "تنظیمات تاریخ‌های داخل اپلیکیشن:", color = theme.accentAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    SettingToggleRow(
                        title = "نمایش تاریخ شمسی داخل برنامه",
                        subtitle = "فعال بودن در صفحه اصلی و صفحه ساعت جهانی",
                        checked = settings.inAppShowSolarDate,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(settings.copy(inAppShowSolarDate = checked))
                        }
                    )

                    SettingToggleRow(
                        title = "نمایش تاریخ میلادی داخل برنامه",
                        subtitle = "فعال بودن در صفحه اصلی و صفحه ساعت جهانی",
                        checked = settings.inAppShowGregorianDate,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(settings.copy(inAppShowGregorianDate = checked))
                        }
                    )

                    SettingToggleRow(
                        title = "نمایش تاریخ قمری داخل برنامه",
                        subtitle = "فعال بودن در صفحه اصلی و صفحه ساعت جهانی",
                        checked = settings.inAppShowLunarDate,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(settings.copy(inAppShowLunarDate = checked))
                        }
                    )
                }
            }
        }

        // ================= SECTION: HOME SCREEN WIDGETS SETTINGS =================
        item {
            SettingsCard(
                title = "تنظیمات ویجت‌های صفحه Home",
                icon = Icons.Default.Schedule,
                iconColor = NeonCyan
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "شخصی‌سازی محتوا و تاریخ‌های ویجت‌های صفحه اصلی دستگاه (Home Screen):",
                        color = theme.secondaryText,
                        fontSize = 12.sp
                    )

                    Text(text = "ویجت تقویم و ساعت سه‌گانه:", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    // Live preview box of Triple Date Widget
                    val triplePreview = com.example.calendar.TripleCalendarHelper.getTodayTripleDate(language)
                    val bgPreviewColor = Color(settings.widgetTripleBgColor).copy(alpha = settings.widgetTripleBgOpacity)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(bgPreviewColor)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (settings.widgetTripleShowDigitalClock) {
                                val cal = java.util.Calendar.getInstance()
                                val timeStr = "%02d:%02d".format(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
                                Text(
                                    text = timeStr,
                                    color = Color(settings.widgetTripleClockColor),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = triplePreview.weekdayName,
                                    color = Color(settings.widgetTripleClockColor),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "پیش‌نمایش زنده ویجت",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 10.sp
                                )
                            }
                            Divider(color = Color.White.copy(alpha = 0.15f), thickness = 0.5.dp)
                            if (settings.widgetTripleShowSolar) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "شمسی:", color = Color(settings.widgetTripleSolarColor), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = triplePreview.solarFormatted, color = Color.White, fontSize = 11.sp)
                                }
                            }
                            if (settings.widgetTripleShowGregorian) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "میلادی:", color = Color(settings.widgetTripleGregorianColor), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = triplePreview.gregorianFormatted, color = Color.White, fontSize = 11.sp)
                                }
                            }
                            if (settings.widgetTripleShowLunar) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "قمری:", color = Color(settings.widgetTripleLunarColor), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = triplePreview.lunarFormatted, color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Background Opacity Slider (0% to 100%)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "میزان شفافیت پس‌زمینه ویجت:", color = theme.primaryText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "${(settings.widgetTripleBgOpacity * 100).toInt()}% (شفاف تا مات)", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.widgetTripleBgOpacity,
                            onValueChange = { value ->
                                viewModel.updateSettings(settings.copy(widgetTripleBgOpacity = value))
                                TripleDateWidgetProvider.updateAllWidgets(context)
                            },
                            valueRange = 0.0f..1.0f,
                            steps = 20,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = theme.surfaceElevated
                            )
                        )
                    }

                    // Background Color Picker
                    ColorPickerRow(
                        label = "رنگ پس‌زمینه ویجت تقویم و ساعت",
                        selectedColor = settings.widgetTripleBgColor,
                        colors = listOf(0xFF0F172A, 0xFF0B0E14, 0xFF1E293B, 0xFF141E30, 0xFF1B1B2F, 0xFF1F1C2C, 0xFF000000),
                        onSelectColor = { color ->
                            viewModel.updateSettings(settings.copy(widgetTripleBgColor = color))
                            TripleDateWidgetProvider.updateAllWidgets(context)
                        },
                        onOpenFullPalette = {
                            fullColorPickerTarget = "widget_triple_bg"
                            fullColorPickerTitle = "انتخاب رنگ پس‌زمینه ویجت تقویم"
                            fullColorPickerInitialColor = settings.widgetTripleBgColor
                        }
                    )

                    // Big Clock Color Picker
                    ColorPickerRow(
                        label = "رنگ ساعت دیجیتال ویجت",
                        selectedColor = settings.widgetTripleClockColor,
                        colors = listOf(0xFF00F2FE, 0xFFFFFFFF, 0xFFFFB703, 0xFF00E676, 0xFFD500F9, 0xFFFF3366, 0xFF38BDF8),
                        onSelectColor = { color ->
                            viewModel.updateSettings(settings.copy(widgetTripleClockColor = color))
                            TripleDateWidgetProvider.updateAllWidgets(context)
                        },
                        onOpenFullPalette = {
                            fullColorPickerTarget = "widget_triple_clock"
                            fullColorPickerTitle = "انتخاب رنگ ساعت دیجیتال ویجت"
                            fullColorPickerInitialColor = settings.widgetTripleClockColor
                        }
                    )

                    // Solar Date Color Picker
                    ColorPickerRow(
                        label = "رنگ تاریخ شمسی در ویجت",
                        selectedColor = settings.widgetTripleSolarColor,
                        colors = listOf(0xFF00F2FE, 0xFF00E676, 0xFFFFB703, 0xFFFFFFFF, 0xFF38BDF8, 0xFFD500F9, 0xFFFF5252),
                        onSelectColor = { color ->
                            viewModel.updateSettings(settings.copy(widgetTripleSolarColor = color))
                            TripleDateWidgetProvider.updateAllWidgets(context)
                        },
                        onOpenFullPalette = {
                            fullColorPickerTarget = "widget_triple_solar"
                            fullColorPickerTitle = "انتخاب رنگ تاریخ شمسی ویجت"
                            fullColorPickerInitialColor = settings.widgetTripleSolarColor
                        }
                    )

                    // Gregorian Date Color Picker
                    ColorPickerRow(
                        label = "رنگ تاریخ میلادی در ویجت",
                        selectedColor = settings.widgetTripleGregorianColor,
                        colors = listOf(0xFFFFB703, 0xFFFF9100, 0xFF00E5FF, 0xFFFFFFFF, 0xFF00E676, 0xFFD500F9, 0xFFFF3366),
                        onSelectColor = { color ->
                            viewModel.updateSettings(settings.copy(widgetTripleGregorianColor = color))
                            TripleDateWidgetProvider.updateAllWidgets(context)
                        },
                        onOpenFullPalette = {
                            fullColorPickerTarget = "widget_triple_greg"
                            fullColorPickerTitle = "انتخاب رنگ تاریخ میلادی ویجت"
                            fullColorPickerInitialColor = settings.widgetTripleGregorianColor
                        }
                    )

                    // Lunar Date Color Picker
                    ColorPickerRow(
                        label = "رنگ تاریخ قمری در ویجت",
                        selectedColor = settings.widgetTripleLunarColor,
                        colors = listOf(0xFFB388FF, 0xFFD500F9, 0xFF00E5FF, 0xFFFFB703, 0xFF00E676, 0xFFFFFFFF, 0xFFFF5252),
                        onSelectColor = { color ->
                            viewModel.updateSettings(settings.copy(widgetTripleLunarColor = color))
                            TripleDateWidgetProvider.updateAllWidgets(context)
                        },
                        onOpenFullPalette = {
                            fullColorPickerTarget = "widget_triple_lunar"
                            fullColorPickerTitle = "انتخاب رنگ تاریخ قمری ویجت"
                            fullColorPickerInitialColor = settings.widgetTripleLunarColor
                        }
                    )

                    SettingToggleRow(
                        title = "نمایش ساعت دستگاه به صورت دیجیتال بزرگ",
                        subtitle = "نمایش ساعت دیجیتال با سایز بزرگ و خوانا در بالای ویجت",
                        checked = settings.widgetTripleShowDigitalClock,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(settings.copy(widgetTripleShowDigitalClock = checked))
                            TripleDateWidgetProvider.updateAllWidgets(context)
                        }
                    )

                    SettingToggleRow(
                        title = "نمایش تاریخ شمسی در ویجت تقویم",
                        subtitle = "نمایش روز، ماه و سال شمسی در ویجت صفحه اصلی",
                        checked = settings.widgetTripleShowSolar,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(settings.copy(widgetTripleShowSolar = checked))
                            TripleDateWidgetProvider.updateAllWidgets(context)
                        }
                    )

                    SettingToggleRow(
                        title = "نمایش تاریخ میلادی در ویجت تقویم",
                        subtitle = "نمایش روز، ماه و سال میلادی در ویجت صفحه اصلی",
                        checked = settings.widgetTripleShowGregorian,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(settings.copy(widgetTripleShowGregorian = checked))
                            TripleDateWidgetProvider.updateAllWidgets(context)
                        }
                    )

                    SettingToggleRow(
                        title = "نمایش تاریخ قمری در ویجت تقویم",
                        subtitle = "نمایش روز، ماه و سال قمری در ویجت صفحه اصلی",
                        checked = settings.widgetTripleShowLunar,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(settings.copy(widgetTripleShowLunar = checked))
                            TripleDateWidgetProvider.updateAllWidgets(context)
                        }
                    )

                    Divider(color = theme.border, thickness = 0.5.dp)
                    Text(text = "ویجت ساعت عقربه‌ای آنالوگ (صفحه Home):", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    SettingToggleRow(
                        title = "نمایش روز هفته در ساعت آنالوگ (مثلاً جمعه یا چهارشنبه)",
                        subtitle = "درج نام روز هفته روی صفحه ساعت عقربه‌ای در صفحه Home",
                        checked = settings.widgetAnalogShowWeekday,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(settings.copy(widgetAnalogShowWeekday = checked))
                            AnalogClockWidgetProvider.updateAllWidgets(context)
                        }
                    )

                    SettingToggleRow(
                        title = "نمایش تاریخ شمسی در ساعت آنالوگ",
                        subtitle = "درج تاریخ روز شمسی بر روی صفحه ساعت آنالوگ",
                        checked = settings.widgetAnalogShowSolar,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(settings.copy(widgetAnalogShowSolar = checked))
                            AnalogClockWidgetProvider.updateAllWidgets(context)
                        }
                    )

                    SettingToggleRow(
                        title = "نمایش تاریخ میلادی در ساعت آنالوگ",
                        subtitle = "درج تاریخ میلادی بر روی صفحه ساعت آنالوگ",
                        checked = settings.widgetAnalogShowGregorian,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(settings.copy(widgetAnalogShowGregorian = checked))
                            AnalogClockWidgetProvider.updateAllWidgets(context)
                        }
                    )

                    SettingToggleRow(
                        title = "نمایش تاریخ قمری در ساعت آنالوگ",
                        subtitle = "درج تاریخ قمری بر روی صفحه ساعت آنالوگ",
                        checked = settings.widgetAnalogShowLunar,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(settings.copy(widgetAnalogShowLunar = checked))
                            AnalogClockWidgetProvider.updateAllWidgets(context)
                        }
                    )

                    Divider(color = theme.border, thickness = 0.5.dp)
                    Text(text = "ویجت قطع سریع آلارم (1x1 Quick Stop):", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "ویجت جمع‌وجور یک در یک (1x1) برای صفحه اصلی. در هنگام به صدا درآمدن هرگونه آلارم یا تایمر، به رنگ قرمز روشن درآمده و با یک لمس فوری آلارم را خاموش می‌کند.",
                        color = theme.secondaryText,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    // Compact preview of Quick Stop Widget
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, NeonRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonRed)
                                .border(2.dp, Color.White, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.AlarmOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Text(text = "قطع", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Column {
                            Text(text = "ویجت آماده در لیست ویجت‌های دستگاه", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "از لانچر گوشی گزینه Widgets را انتخاب کرده و «ویجت قطع سریع آلارم (1x1)» را روی صفحه Home قرار دهید.", color = theme.secondaryText, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            TripleDateWidgetProvider.updateAllWidgets(context)
                            AnalogClockWidgetProvider.updateAllWidgets(context)
                            com.example.widget.QuickStopWidgetProvider.updateAllWidgets(context)
                            android.widget.Toast.makeText(context, "ویجت‌های تقویم، ساعت و قطع سریع با موفقیت بروزرسانی شدند", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceElevated),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "بروزرسانی فوری تمام ویجت‌های صفحه Home", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ================= SECTION: TOP PERSISTENT NOTIFICATION SHADE =================
        item {
            SettingsCard(
                title = "اعلان دائمی وضعیت در بالای پرده اعلان‌ها",
                icon = Icons.Default.NotificationsActive,
                iconColor = NeonCyan
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "کارت وضعیت پیوسته در بالاترین بخش پرده نوتیفیکیشن اندروید شامل تاریخ‌های سه‌گانه، زمان مانده تا آلارم بعدی، تایمر و کرنومتر:",
                        color = theme.secondaryText,
                        fontSize = 12.sp
                    )

                    SettingToggleRow(
                        title = "فعالسازی اعلان دائمی در پرده نوتیفیکیشن",
                        subtitle = "نمایش کارت خلاصه وضعیت همیشه در دسترس در نوار اعلان‌ها",
                        checked = settings.showPersistentNotification,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(settings.copy(showPersistentNotification = checked))
                            if (checked) {
                                com.example.service.PersistentStatusNotificationHelper.updateStatusNotification(context)
                            } else {
                                com.example.service.PersistentStatusNotificationHelper.cancelStatusNotification(context)
                            }
                        }
                    )

                    AnimatedVisibility(visible = settings.showPersistentNotification) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Divider(color = theme.border, thickness = 0.5.dp)

                            Text(text = "تنظیم نمایش تاریخ‌های سه‌گانه در اعلان دائمی:", color = NeonAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                            SettingToggleRow(
                                title = "نمایش تاریخ شمسی در اعلان",
                                subtitle = "درج تاریخ روز و ماه شمسی در نوار اعلان",
                                checked = settings.persistentShowSolarDate,
                                onCheckedChange = { checked ->
                                    viewModel.updateSettings(settings.copy(persistentShowSolarDate = checked))
                                    com.example.service.PersistentStatusNotificationHelper.updateStatusNotification(context)
                                }
                            )

                            SettingToggleRow(
                                title = "نمایش تاریخ میلادی در اعلان",
                                subtitle = "درج تاریخ میلادی در نوار اعلان",
                                checked = settings.persistentShowGregorianDate,
                                onCheckedChange = { checked ->
                                    viewModel.updateSettings(settings.copy(persistentShowGregorianDate = checked))
                                    com.example.service.PersistentStatusNotificationHelper.updateStatusNotification(context)
                                }
                            )

                            SettingToggleRow(
                                title = "نمایش تاریخ قمری در اعلان",
                                subtitle = "درج تاریخ قمری در نوار اعلان",
                                checked = settings.persistentShowLunarDate,
                                onCheckedChange = { checked ->
                                    viewModel.updateSettings(settings.copy(persistentShowLunarDate = checked))
                                    com.example.service.PersistentStatusNotificationHelper.updateStatusNotification(context)
                                }
                            )

                            Divider(color = theme.border, thickness = 0.5.dp)

                            SettingToggleRow(
                                title = "نمایش زمان مانده تا آلارم بعدی",
                                subtitle = "محاسبه و نمایش دقیق ساعت و دقیقه مانده تا زنگ هشدار بعدی",
                                checked = settings.persistentShowNextAlarm,
                                onCheckedChange = { checked ->
                                    viewModel.updateSettings(settings.copy(persistentShowNextAlarm = checked))
                                }
                            )

                            SettingToggleRow(
                                title = "نمایش وضعیت تایمر فعال",
                                subtitle = "نمایش وضعیت و زمان باقی‌مانده تایمر در حال اجرا",
                                checked = settings.persistentShowTimer,
                                onCheckedChange = { checked ->
                                    viewModel.updateSettings(settings.copy(persistentShowTimer = checked))
                                }
                            )

                            SettingToggleRow(
                                title = "نمایش وضعیت کرنومتر فعال",
                                subtitle = "نمایش وضعیت و زمان‌سنج در صورت فعال بودن",
                                checked = settings.persistentShowStopwatch,
                                onCheckedChange = { checked ->
                                    viewModel.updateSettings(settings.copy(persistentShowStopwatch = checked))
                                }
                            )

                            // Button to open Android System Notification Settings for this channel
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                            putExtra(Settings.EXTRA_CHANNEL_ID, com.example.service.PersistentStatusNotificationHelper.CHANNEL_ID)
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Throwable) {
                                        try {
                                            val appIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.fromParts("package", context.packageName, null)
                                            }
                                            context.startActivity(appIntent)
                                        } catch (_: Throwable) {}
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonAmber),
                                border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "تنظیم صدا، ویبره و اعلان در بخش Apps دستگاه", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    com.example.service.PersistentStatusNotificationHelper.updateStatusNotification(context)
                                    android.widget.Toast.makeText(context, "اعلان وضعیت با موفقیت بروزرسانی شد", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceElevated),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "بروزرسانی فوری اعلان دائمی", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ================= SECTION 2: AUDIO & RINGTONES =================
        item {
            SettingsCard(
                title = "آهنگ‌ها و اصوات پیش‌فرض",
                icon = Icons.Default.MusicNote,
                iconColor = NeonPurple
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Default Alarm Ringtone
                    RingtoneSelectorRow(
                        label = "آهنگ پیش‌فرض آلارم",
                        currentTitle = settings.defaultAlarmSoundTitle,
                        onClick = {
                            activeSoundTarget = "alarm"
                            showSoundPickerDialog = true
                        }
                    )

                    // Default Timer Ringtone
                    RingtoneSelectorRow(
                        label = "آهنگ پیش‌فرض تایمر",
                        currentTitle = settings.defaultTimerSoundTitle,
                        onClick = {
                            activeSoundTarget = "timer"
                            showSoundPickerDialog = true
                        }
                    )

                    // Default Stopwatch Sound
                    RingtoneSelectorRow(
                        label = "آهنگ پیش‌فرض کرنومتر",
                        currentTitle = settings.defaultStopwatchSoundTitle,
                        onClick = {
                            activeSoundTarget = "stopwatch"
                            showSoundPickerDialog = true
                        }
                    )

                    Divider(color = theme.border, thickness = 0.5.dp)

                    // Volume Slider: Alarm
                    VolumeSliderRow(
                        label = "بلندی صدای هشدار آلارم (Volume)",
                        volume = settings.alarmVolume,
                        onVolumeChange = { v ->
                            viewModel.updateSettings(settings.copy(alarmVolume = v))
                        },
                        onTestSound = {
                            SoundAndVibrationManager.previewSound(context, settings.defaultAlarmSound, settings.alarmVolume)
                        }
                    )

                    // Volume Slider: Timer
                    VolumeSliderRow(
                        label = "بلندی صدای تایمر (Volume)",
                        volume = settings.timerVolume,
                        onVolumeChange = { v ->
                            viewModel.updateSettings(settings.copy(timerVolume = v))
                        },
                        onTestSound = {
                            SoundAndVibrationManager.previewSound(context, settings.defaultTimerSound, settings.timerVolume)
                        }
                    )

                    Divider(color = theme.border, thickness = 0.5.dp)

                    // Auto-Silence Duration
                    OptionDropdownRow(
                        label = "توقف خودکار زنگ هنگام عدم پاسخ",
                        subtitle = "اگر پاسخی داده نشود، پس از چه مدتی آلارم/تایمر خودبه‌خود خاموش شود",
                        currentValueText = if (settings.autoSilenceMinutes == 0) "هرگز (خاموش نشود)" else "${settings.autoSilenceMinutes} دقیقه",
                        options = listOf(1 to "۱ دقیقه", 2 to "۲ دقیقه", 5 to "۵ دقیقه", 10 to "۱۰ دقیقه", 15 to "۱۵ دقیقه", 0 to "هرگز"),
                        onSelect = { min ->
                            viewModel.updateSettings(settings.copy(autoSilenceMinutes = min))
                        }
                    )

                    // Snooze Duration
                    OptionDropdownRow(
                        label = "مدت زمان به تعویق انداختن (Snooze)",
                        subtitle = "فاصله زمانی میان به صدا درآمدن مجدد زنگ پس از زدن دکمه Snooze",
                        currentValueText = "${settings.snoozeDurationMinutes} دقیقه",
                        options = listOf(2 to "۲ دقیقه", 5 to "۵ دقیقه", 10 to "۱۰ دقیقه", 15 to "۱۵ دقیقه"),
                        onSelect = { min ->
                            viewModel.updateSettings(settings.copy(snoozeDurationMinutes = min))
                        }
                    )

                    Divider(color = theme.border, thickness = 0.5.dp)

                    // Hardware & Motion Dismiss Section
                    Text(
                        text = "روش‌های نوین و سخت‌افزاری قطع زنگ (Dismiss Controls):",
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "گزینه‌های فیزیکی و حرکتی جهت قطع یا آرام کردن هشدار در مواقع مختلف:",
                        color = theme.secondaryText,
                        fontSize = 11.sp
                    )

                    // 1. Power Button
                    SettingToggleRow(
                        title = "قطع زنگ با دکمه پاور (Power Button)",
                        subtitle = "فشردن متوالی دکمه پاور گوشی جهت قطع هشدار",
                        checked = settings.dismissPowerEnabled,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(settings.copy(dismissPowerEnabled = checked))
                        }
                    )
                    if (settings.dismissPowerEnabled) {
                        OptionDropdownRow(
                            label = "تعداد دفعات فشار دادن دکمه Power",
                            subtitle = "تعداد کلیک متوالی مورد نیاز برای قطع زنگ",
                            currentValueText = "${settings.dismissPowerPressCount} مرتبه",
                            options = listOf(1 to "۱ مرتبه", 2 to "۲ مرتبه", 3 to "۳ مرتبه", 4 to "۴ مرتبه", 5 to "۵ مرتبه", 6 to "۶ مرتبه"),
                            onSelect = { count ->
                                viewModel.updateSettings(settings.copy(dismissPowerPressCount = count))
                            }
                        )
                    }

                    Divider(color = theme.border.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // 2. Shake to Stop (تکان دادن گوشی)
                    SettingToggleRow(
                        title = "قطع زنگ با تکان دادن گوشی (Shake to Stop)",
                        subtitle = "تکان دادن تلفن همراه جهت خاموش کردن آلارم؛ ایده‌آل برای کم‌بینایان و در تاریکی شب",
                        checked = settings.shakeToStopEnabled,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(settings.copy(shakeToStopEnabled = checked))
                        }
                    )
                    if (settings.shakeToStopEnabled) {
                        val sensLabel = when (settings.shakeSensitivity) {
                            "LOW" -> "حساسیت کم (نیاز به تکان شدید)"
                            "HIGH" -> "حساسیت بالا (تکان آرام، مناسب تاریکی)"
                            else -> "حساسیت متوسط (استاندارد)"
                        }
                        StringOptionDropdownRow(
                            label = "میزان حساسیت تکان دادن (Sensitivity)",
                            subtitle = "شدت لرزش مورد نیاز جهت قطع آلارم",
                            currentValueText = sensLabel,
                            options = listOf(
                                "LOW" to "حساسیت کم (نیاز به تکان شدید)",
                                "MEDIUM" to "حساسیت متوسط (استاندارد)",
                                "HIGH" to "حساسیت بالا (تکان آرام - مناسب تاریکی)"
                            ),
                            onSelect = { sens ->
                                viewModel.updateSettings(settings.copy(shakeSensitivity = sens))
                            }
                        )
                    }

                    Divider(color = theme.border.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // 3. Presence Detection (کاهش ۷۵٪ صدا با نزدیک شدن یا برداشتن گوشی)
                    SettingToggleRow(
                        title = "کاهش بلندی صدا با برداشتن یا نزدیک شدن (Presence Detection)",
                        subtitle = "کاهش ۷۵ درصدی صدا (باقی ماندن ۲۵٪) با حسگر مجاورت یا شتاب‌سنج تا زمان قطع کامل",
                        checked = settings.presenceAutoLowerEnabled,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(settings.copy(presenceAutoLowerEnabled = checked))
                        }
                    )

                    Divider(color = theme.border.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // 4. Flip-to-Snooze (به تعویق انداختن با برگرداندن گوشی صفحه به سمت زمین)
                    SettingToggleRow(
                        title = "به تعویق انداختن با برگرداندن گوشی (Flip to Snooze)",
                        subtitle = "با چرخاندن گوشی به‌طوری‌که صفحه نمایش رو به زمین قرار گیرد، زنگ به تعویق (Snooze) می‌افتد",
                        checked = settings.flipToSnoozeEnabled,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(settings.copy(flipToSnoozeEnabled = checked))
                        },
                        modifier = Modifier.testTag("flip_to_snooze_toggle")
                    )
                }
            }
        }

        // ================= SECTION: GUARDIAN ALERT (هشدار ثانویه نگهبان) =================
        item {
            SettingsCard(
                title = "سامانه نگهبان و هشدار اضطراری (Guardian Alert)",
                icon = Icons.Default.Shield,
                iconColor = NeonRed
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "اگر آلارم پس از مدت زمان مشخصی قطع نشود، برنامه به طور خودکار به مخاطب اضطراری شما پیامک ارسال می‌کند:",
                        color = theme.secondaryText,
                        fontSize = 12.sp
                    )

                    SettingToggleRow(
                        title = "فعالسازی هشدار ثانویه (Guardian Alert)",
                        subtitle = "ارسال پیامک اضطراری به مخاطب در صورت عدم قطع زنگ هشدار",
                        checked = settings.guardianAlertEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                val hasSmsPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.SEND_SMS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasSmsPerm) {
                                    smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                                } else {
                                    viewModel.updateSettings(settings.copy(guardianAlertEnabled = true))
                                }
                            } else {
                                viewModel.updateSettings(settings.copy(guardianAlertEnabled = false))
                            }
                        }
                    )

                    if (settings.guardianAlertEnabled) {
                        // Timeout duration dropdown
                        OptionDropdownRow(
                            label = "مدت زمان انتظار پیش از ارسال پیامک",
                            subtitle = "چند دقیقه پس از شروع زنگ بدون قطع پیامک ارسال شود",
                            currentValueText = "${settings.guardianTimeoutMinutes} دقیقه",
                            options = listOf(
                                1 to "۱ دقیقه (تست سریع)",
                                2 to "۲ دقیقه",
                                3 to "۳ دقیقه (پیش‌فرض)",
                                5 to "۵ دقیقه",
                                10 to "۱۰ دقیقه"
                            ),
                            onSelect = { minutes ->
                                viewModel.updateSettings(settings.copy(guardianTimeoutMinutes = minutes))
                            }
                        )

                        // Emergency Contact Input with Contact Picker
                        Text(
                            text = "شماره تلفن مخاطب اضطراری:",
                            color = theme.primaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = settings.guardianPhoneNumber,
                                onValueChange = { newNumber ->
                                    viewModel.updateSettings(settings.copy(guardianPhoneNumber = newNumber))
                                },
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(
                                        text = if (settings.guardianContactName.isNotBlank()) "${settings.guardianContactName} (مثلا 0912...)" else "شماره تلفن (مثلا 0912...)",
                                        fontSize = 12.sp,
                                        color = theme.secondaryText
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = theme.border,
                                    focusedTextColor = theme.primaryText,
                                    unfocusedTextColor = theme.primaryText
                                )
                            )
                            Button(
                                onClick = {
                                    val hasContactsPerm = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.READ_CONTACTS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!hasContactsPerm) {
                                        contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                    } else {
                                        contactPickerLauncher.launch(null)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContactPhone, contentDescription = "انتخاب از مخاطبین", tint = NeonCyan)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "مخاطبین", fontSize = 11.sp, color = NeonCyan)
                            }
                        }

                        // Custom Message Text Box
                        Text(
                            text = "متن پیامک اضطراری:",
                            color = theme.primaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = settings.guardianCustomMessage,
                            onValueChange = { newMsg ->
                                viewModel.updateSettings(settings.copy(guardianCustomMessage = newMsg))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2,
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = theme.border,
                                focusedTextColor = theme.primaryText,
                                unfocusedTextColor = theme.primaryText
                            )
                        )

                        // Toggle: Include missed alarms count
                        val missedAlarms24h = GyroscopeMotionTracker.getMissedAlarmsLast24Hours(context)
                        SettingToggleRow(
                            title = "افزودن تعداد هشدارهای بی‌پاسخ ۲۴ ساعت گذشته به پیامک",
                            subtitle = "تعداد ثبت‌شده فعلی: $missedAlarms24h زنگ بدون پاسخ",
                            checked = settings.guardianIncludeMissedAlarms,
                            onCheckedChange = { checked ->
                                viewModel.updateSettings(settings.copy(guardianIncludeMissedAlarms = checked))
                            }
                        )

                        // Toggle: Include gyroscope motion data
                        val lastMotionStr = GyroscopeMotionTracker.getLastMotionTimeFormatted(context)
                        val motionlessStr = GyroscopeMotionTracker.getMotionlessDurationSummary(context)
                        SettingToggleRow(
                            title = "افزودن وضعیت حسگر ژیروسکوپ و مدت بی‌حرکتی به پیامک",
                            subtitle = "آخرین حرکت: $lastMotionStr | مدت بی‌حرکتی: $motionlessStr",
                            checked = settings.guardianIncludeGyroscopeData,
                            onCheckedChange = { checked ->
                                viewModel.updateSettings(settings.copy(guardianIncludeGyroscopeData = checked))
                            }
                        )

                        // Toggle: Continuous gyroscope logger
                        SettingToggleRow(
                            title = "فعال بودن ثبت مداوم سوابق حسگر ژیروسکوپ",
                            subtitle = "ثبت هوشمند زمان‌های حرکت و جابجایی تلفن همراه در پس‌زمینه",
                            checked = settings.gyroscopeLoggerEnabled,
                            onCheckedChange = { checked ->
                                viewModel.updateSettings(settings.copy(gyroscopeLoggerEnabled = checked))
                                if (checked) {
                                    GyroscopeMotionTracker.startTracking(context)
                                } else {
                                    GyroscopeMotionTracker.stopTracking()
                                }
                            }
                        )

                        // Live SMS Preview Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF070B14))
                                .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "پیش‌نمایش پیامک ارسالی به مخاطب:",
                                    color = NeonAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val previewMsg = GuardianAlertManager.buildEmergencyMessage(context, settings)
                                Text(
                                    text = previewMsg,
                                    color = theme.primaryText,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // Test SMS Send Button
                        Button(
                            onClick = {
                                if (settings.guardianPhoneNumber.isBlank()) {
                                    testSmsNotice = "لطفا ابتدا شماره تلفن مخاطب را وارد کنید."
                                } else {
                                    val sent = GuardianAlertManager.sendEmergencyAlert(context, settings)
                                    testSmsNotice = if (sent) {
                                        "پیامک اضطراری نگهبان با موفقیت ارسال شد!"
                                    } else {
                                        "دسترسی مستقیم پیامک فعال نیست؛ صفحه ارسال پیامک گوشی باز شد."
                                    }
                                    if (!sent) {
                                        GuardianAlertManager.openSmsAppFallback(
                                            context,
                                            settings.guardianPhoneNumber,
                                            GuardianAlertManager.buildEmergencyMessage(context, settings)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRed.copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "ارسال آزمایشی پیامک اضطراری نگهبان", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        testSmsNotice?.let { notice ->
                            Text(
                                text = notice,
                                color = if (notice.contains("موفقیت")) NeonCyan else NeonAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // ================= SECTION: ALARMS BACKUP & RESTORE =================
        item {
            SettingsCard(
                title = "پشتیبان‌گیری و بازگرداندن لیست آلارم‌ها",
                icon = Icons.Default.FileDownload,
                iconColor = NeonCyan
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "از تمامی آلارم‌ها، شیفت‌ها و رویدادهای تقویم یک فایل پشتیبان (JSON) تهیه کنید و در صورت نیاز در همین دستگاه یا گوشی دیگر بازگردانی نمایید. (تعداد آلارم‌های فعلی: ${alarms.size})",
                        color = theme.secondaryText,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (alarms.isEmpty()) {
                                    backupRestoreIsError = true
                                    backupRestoreNotice = "هیچ آلارمی برای پشتیبان‌گیری وجود ندارد."
                                } else {
                                    val cal = java.util.Calendar.getInstance()
                                    val stamp = String.format(
                                        java.util.Locale.US,
                                        "%04d%02d%02d_%02d%02d",
                                        cal.get(java.util.Calendar.YEAR),
                                        cal.get(java.util.Calendar.MONTH) + 1,
                                        cal.get(java.util.Calendar.DAY_OF_MONTH),
                                        cal.get(java.util.Calendar.HOUR_OF_DAY),
                                        cal.get(java.util.Calendar.MINUTE)
                                    )
                                    exportAlarmsLauncher.launch("epic_alarms_backup_$stamp.json")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("backup_alarms_button")
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "تهیه پشتیبان", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                importAlarmsLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonAmber),
                            border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("restore_alarms_button")
                        ) {
                            Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "بازگرداندن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    backupRestoreNotice?.let { notice ->
                        Text(
                            text = notice,
                            color = if (backupRestoreIsError) NeonRed else NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }


        // ================= SECTION 3: THEME, LANGUAGE & FONT SCALE =================
        item {
            SettingsCard(
                title = "تم، زبان و اندازه فونت (Display & Fonts)",
                icon = Icons.Default.BrightnessAuto,
                iconColor = NeonAmber
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Theme Mode Selector
                    Text(text = "پوسته نرم‌افزار:", color = theme.primaryText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeButton(
                            title = "تاریک (OLED)",
                            icon = Icons.Default.DarkMode,
                            isSelected = themeMode == AppThemeMode.DARK,
                            color = NeonCyan,
                            onClick = { viewModel.setThemeMode(AppThemeMode.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeButton(
                            title = "روشن (Light)",
                            icon = Icons.Default.LightMode,
                            isSelected = themeMode == AppThemeMode.LIGHT,
                            color = NeonAmber,
                            onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeButton(
                            title = "سیستم",
                            icon = Icons.Default.BrightnessAuto,
                            isSelected = themeMode == AppThemeMode.SYSTEM,
                            color = NeonPurple,
                            onClick = { viewModel.setThemeMode(AppThemeMode.SYSTEM) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Language Selector
                    Text(text = "زبان برنامه (Language):", color = theme.primaryText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AppLanguage.values().forEach { lang ->
                            val isSelected = language == lang
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) theme.accentCyan else theme.surfaceElevated)
                                    .border(1.dp, if (isSelected) theme.accentCyan else theme.border, RoundedCornerShape(10.dp))
                                    .clickable { viewModel.setLanguage(lang) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lang.title,
                                    color = if (isSelected) Color.Black else theme.primaryText,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Font Size Scaling Selector (کوچک / عادی / بزرگ / بزرگتر)
                    Text(
                        text = "${Strings.get("font_scale_title", language)}:",
                        color = theme.primaryText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = Strings.get("font_scale_desc", language),
                        color = theme.secondaryText,
                        fontSize = 11.sp
                    )

                    val fontScales = listOf(
                        "SMALL" to Strings.get("font_scale_small", language),
                        "NORMAL" to Strings.get("font_scale_normal", language),
                        "LARGE" to Strings.get("font_scale_large", language),
                        "LARGER" to Strings.get("font_scale_larger", language)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        fontScales.forEach { (scaleKey, scaleTitle) ->
                            val isSelected = settings.fontSizeScale == scaleKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) NeonCyan else theme.surfaceElevated)
                                    .border(1.dp, if (isSelected) NeonCyan else theme.border, RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.updateSettings(settings.copy(fontSizeScale = scaleKey))
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = scaleTitle,
                                    color = if (isSelected) Color.Black else theme.primaryText,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Live sample preview box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.surfaceElevated)
                            .border(1.dp, theme.border, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "پیش‌نمایش اندازه نوشته: ساعت هشدار اپیک ۰۷:۳۰",
                            color = NeonAmber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ================= SECTION: DEVICE & APP SECURITY LOCK =================
        item {
            SettingsCard(
                title = "ورود امن و قفل اپلیکیشن",
                icon = Icons.Default.Security,
                iconColor = NeonCyan
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "محافظت از برنامه با استفاده از امنیت بیومتریک گوشی اندروید (اثر انگشت، چهره، پین دستگاه) یا رمز اختصاصی داخل برنامه:",
                        color = theme.secondaryText,
                        fontSize = 12.sp
                    )

                    // Master App Lock Switch
                    SettingToggleRow(
                        title = "فعالسازی قفل و ورود امن",
                        subtitle = "در هنگام باز شدن اپلیکیشن، هویت کاربر تأیید گردد",
                        checked = settings.appLockEnabled,
                        onCheckedChange = { checked ->
                            if (checked && !settings.appLockUseAppPin && !AppLockManager.isDeviceSecure(context)) {
                                // If device has no lock and no app pin, open setup dialog
                                showAppPinSetupDialog = true
                            } else {
                                viewModel.updateSettings(settings.copy(appLockEnabled = checked))
                            }
                        }
                    )

                    AnimatedVisibility(visible = settings.appLockEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Divider(color = theme.border, thickness = 0.5.dp)

                            Text(
                                text = "روش‌های مجاز احراز هویت:",
                                color = NeonAmber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // 1. Device Fingerprint
                            SettingToggleRow(
                                title = "استفاده از اثر انگشت ثبت شده در دستگاه",
                                subtitle = "ورود سریع با سنسور حسگر اثر انگشت گوشی (Fingerprint)",
                                checked = settings.appLockUseFingerprint,
                                onCheckedChange = { checked ->
                                    viewModel.updateSettings(settings.copy(appLockUseFingerprint = checked))
                                }
                            )

                            // 2. Device Face Recognition
                            SettingToggleRow(
                                title = "استفاده از چهره ثبت شده در دستگاه",
                                subtitle = "ورود با تشخیص چهره تعریف شده در اندروید (Face Unlock)",
                                checked = settings.appLockUseFace,
                                onCheckedChange = { checked ->
                                    viewModel.updateSettings(settings.copy(appLockUseFace = checked))
                                }
                            )

                            // 3. Device Screen Lock PIN / Pattern
                            SettingToggleRow(
                                title = "استفاده از پین کد یا الگوی ثبت شده در دستگاه",
                                subtitle = "ورود با استفاده از PIN، الگو یا گذرواژه امنیتی گوشی",
                                checked = settings.appLockUseDevicePin,
                                onCheckedChange = { checked ->
                                    viewModel.updateSettings(settings.copy(appLockUseDevicePin = checked))
                                }
                            )

                            Divider(color = theme.border, thickness = 0.5.dp)

                            // 4. In-App PIN Code
                            SettingToggleRow(
                                title = "استفاده از پین کد اختصاصی داخل برنامه",
                                subtitle = "رمز عددی ۴ تا ۶ رقمی مستقل تعریف شده در «ساعت هشدار اپیک»",
                                checked = settings.appLockUseAppPin,
                                onCheckedChange = { checked ->
                                    if (checked && settings.appLockAppPinHash.isBlank()) {
                                        showAppPinSetupDialog = true
                                    } else {
                                        viewModel.updateSettings(settings.copy(appLockUseAppPin = checked))
                                    }
                                }
                            )

                            // Button to set or change In-App PIN
                            OutlinedButton(
                                onClick = { showAppPinSetupDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (settings.appLockAppPinHash.isBlank()) "تعریف و تنظیم پین کد اختصاصی داخل برنامه" else "تغییر پین کد اختصاصی برنامه",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Permanent Security Notice
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(theme.surfaceElevated)
                                    .border(1.dp, NeonAmber.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(20.dp))
                                    Text(
                                        text = "توجه: پین کد اختصاصی برنامه به صورت رمزنگاری‌شده در حافظه دائمی امن ذخیره می‌شود و به هیچ وجه حذف نخواهد شد مگر اینکه کاربر از بخش تنظیمات گوشی اقدام به Clear Data یا حذف و نصب مجدد برنامه نماید.",
                                        color = theme.secondaryText,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= SECTION 4: SYSTEM & POWER TECHNICAL GUIDE =================
        item {
            SettingsCard(
                title = "راهنمای کارکرد در خاموشی و فورس‌استاپ",
                icon = Icons.Default.BatteryAlert,
                iconColor = NeonPink
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "پاسخ به سوالات فنی و نحوه عملکرد سیستم اندروید:",
                        color = theme.accentAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Point 1: Powered-off device reality
                    SystemExplainerItem(
                        icon = Icons.Default.PowerSettingsNew,
                        title = "آیا هنگام خاموش بودن کامل گوشی زنگ می‌زند؟",
                        description = "در سیستم‌عامل اندروید، وقتی گوشی کامل خاموش (Power Off) می‌شود، لینوکس و سیستم‌عامل متوقف شده و پردازنده خاموش است. زنگ زدن در حالت خاموشی کامل (Power-off Alarm) منحصراً نیازمند فرم‌ور سطح سخت‌افزار تراشه (مانند Qualcomm PMIC) است که فقط رام‌های کارخانه‌ای برخی برندها دارند و طبق استاندارد امنیتی اندروید، هیچ اپلیکیشن نصبی اجازه روشن کردن خودکار سخت‌افزار خاموش را ندارد.\n\nاما در برنامه ما، بالاترین سطح استاندارد اندروید یعنی AlarmManager.setAlarmClock() و معافیت از Doze Mode پیاده شده است؛ به این معنی که حتی اگر صفحه گوشی خاموش باشد و در عمیق‌ترین خواب باتری قرار داشته باشد، دستگاه سر ثانیه دقیق روشن شده و زنگ تمام‌صفحه به صدا درمی‌آید."
                    )

                    // Point 2: Force Stop & Background Reliability
                    SystemExplainerItem(
                        icon = Icons.Default.Schedule,
                        title = "عملکرد در پس‌زمینه و بعد از روشن شدن گوشی (Boot)",
                        description = "این برنامه مجهز به گیرنده خودکار راه‌اندازی (BootReceiver) است. به محض روشن شدن یا ری‌استارت گوشی، تمام آلارم‌ها و تایمرها دوباره فوراً در تقویم سیستم زمان‌بندی می‌شوند. همچنین با استفاده از سرویس شناور Foreground Service، پایداری اجرای آلارم در بالاترین سطح تضمین شده است."
                    )

                    // Battery optimization whitelist button
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            } catch (_: Throwable) {
                                try {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Throwable) {}
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceElevated),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.BatteryAlert, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "بررسی تنظیمات بهینه‌سازی باتری در اندروید", color = NeonCyan, fontSize = 12.sp)
                    }
                }
            }
        }

        // ================= SECTION 5: SHARE & PUBLISH APK FILE =================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 2.dp,
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(NeonCyan, NeonPurple, NeonAmber, NeonCyan)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .testTag("share_apk_card"),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(NeonCyan, NeonPurple)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "انتشار و اشتراک فایل نصبی برنامه (APK)",
                                color = theme.primaryText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ارسال مستقیم فایل نصبی ساعت هشدار اپیک به سایر افراد",
                                color = theme.secondaryText,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = "با لمس دکمه زیر، فایل APK کامل و آماده نصب «ساعت هشدار اپیک» از روی دستگاه شما آماده شده و می‌توانید آن را مستقیماً از طریق بلوتوث، تلگرام، واتساپ، ایتا، بله، روبیکا، جیمیل و سایر پیام‌رسان‌ها یا ابزارهای اشتراک فایل به دیگران ارسال نمایید.",
                        color = theme.mutedText,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    // Distinctive Gradient Share Button
                    Button(
                        onClick = {
                            if (!isSharingApk) {
                                isSharingApk = true
                                coroutineScope.launch {
                                    ApkShareHelper.shareAppApk(context)
                                    isSharingApk = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("share_apk_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF00C6FF),
                                            Color(0xFF0072FF),
                                            Color(0xFF8A2387)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSharingApk) Icons.Default.Refresh else Icons.Default.Send,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isSharingApk) "در حال آماده‌سازی فایل APK..." else "اشتراک‌گذاری و ارسال فایل APK اپلیکیشن",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ================= SOUND PICKER DIALOG =================
    if (showSoundPickerDialog && activeSoundTarget != null) {
        val targetName = when (activeSoundTarget) {
            "alarm" -> "آلارم"
            "timer" -> "تایمر"
            else -> "کرنومتر"
        }
        val currentSoundId = when (activeSoundTarget) {
            "alarm" -> settings.defaultAlarmSound
            "timer" -> settings.defaultTimerSound
            else -> settings.defaultStopwatchSound
        }

        AlertDialog(
            onDismissRequest = {
                SoundAndVibrationManager.stopAlarm(context)
                showSoundPickerDialog = false
            },
            containerColor = theme.surface,
            title = {
                Text(text = "انتخاب آهنگ پیش‌فرض $targetName", color = theme.primaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "آهنگ‌های اختصاصی اپلیکیشن:", color = theme.accentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    SoundAndVibrationManager.builtInSounds.forEach { opt ->
                        val isSelected = currentSoundId == opt.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) theme.surfaceElevated else Color.Transparent)
                                .clickable {
                                    SoundAndVibrationManager.previewSound(context, opt.id, 0.9f)
                                    when (activeSoundTarget) {
                                        "alarm" -> viewModel.updateSettings(settings.copy(defaultAlarmSound = opt.id, defaultAlarmSoundTitle = opt.nameFa))
                                        "timer" -> viewModel.updateSettings(settings.copy(defaultTimerSound = opt.id, defaultTimerSoundTitle = opt.nameFa))
                                        "stopwatch" -> viewModel.updateSettings(settings.copy(defaultStopwatchSound = opt.id, defaultStopwatchSoundTitle = opt.nameFa))
                                    }
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                                )
                                Text(text = opt.nameFa, color = theme.primaryText, fontSize = 13.sp)
                            }
                            IconButton(onClick = { SoundAndVibrationManager.previewSound(context, opt.id, 0.9f) }) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = NeonAmber, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Divider(color = theme.border, modifier = Modifier.padding(vertical = 4.dp))

                    // Choose from Device Audio Files
                    Button(
                        onClick = {
                            audioPickerLauncher.launch("audio/*")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.accentPurple),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "انتخاب فایل صوتی از حافظه دستگاه", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    SoundAndVibrationManager.stopAlarm(context)
                    showSoundPickerDialog = false
                }) {
                    Text(text = "تأیید و بستن", color = theme.accentCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Full 16 Million + 32 Presets Color Picker Dialog
    if (fullColorPickerTarget != null) {
        ColorPickerDialog(
            initialColorLong = fullColorPickerInitialColor,
            title = fullColorPickerTitle,
            onDismiss = { fullColorPickerTarget = null },
            onColorSelected = { pickedColor ->
                when (fullColorPickerTarget) {
                    "face" -> {
                        viewModel.updateSettings(settings.copy(clockFaceColor = pickedColor))
                        AnalogClockWidgetProvider.updateAllWidgets(context)
                    }
                    "hour" -> {
                        viewModel.updateSettings(settings.copy(hourHandColor = pickedColor))
                        AnalogClockWidgetProvider.updateAllWidgets(context)
                    }
                    "minute" -> {
                        viewModel.updateSettings(settings.copy(minuteHandColor = pickedColor))
                        AnalogClockWidgetProvider.updateAllWidgets(context)
                    }
                    "second" -> {
                        viewModel.updateSettings(settings.copy(secondHandColor = pickedColor))
                        AnalogClockWidgetProvider.updateAllWidgets(context)
                    }
                    "number" -> {
                        viewModel.updateSettings(settings.copy(numberColor = pickedColor))
                        AnalogClockWidgetProvider.updateAllWidgets(context)
                    }
                    "widget_triple_bg" -> {
                        viewModel.updateSettings(settings.copy(widgetTripleBgColor = pickedColor))
                        TripleDateWidgetProvider.updateAllWidgets(context)
                    }
                    "widget_triple_clock" -> {
                        viewModel.updateSettings(settings.copy(widgetTripleClockColor = pickedColor))
                        TripleDateWidgetProvider.updateAllWidgets(context)
                    }
                    "widget_triple_solar" -> {
                        viewModel.updateSettings(settings.copy(widgetTripleSolarColor = pickedColor))
                        TripleDateWidgetProvider.updateAllWidgets(context)
                    }
                    "widget_triple_greg" -> {
                        viewModel.updateSettings(settings.copy(widgetTripleGregorianColor = pickedColor))
                        TripleDateWidgetProvider.updateAllWidgets(context)
                    }
                    "widget_triple_lunar" -> {
                        viewModel.updateSettings(settings.copy(widgetTripleLunarColor = pickedColor))
                        TripleDateWidgetProvider.updateAllWidgets(context)
                    }
                }
                fullColorPickerTarget = null
            }
        )
    }

    // In-App PIN Setup / Change Dialog
    if (showAppPinSetupDialog) {
        AlertDialog(
            onDismissRequest = {
                showAppPinSetupDialog = false
                pinDialogInput = ""
                pinDialogConfirmInput = ""
                pinDialogError = null
            },
            containerColor = theme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, tint = theme.accentCyan)
                    Text(
                        text = "تنظیم پین کد اختصاصی برنامه",
                        color = theme.primaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "یک رمز عددی ۴ تا ۶ رقمی برای برنامه «ساعت هشدار اپیک» تعیین نمایید. این رمز به صورت دائمی در حافظه امن دستگاه ذخیره شده و غیرقابل حذف است مگر با پاک کردن داده‌ها (Clear Data) یا حذف و نصب مجدد برنامه.",
                        color = theme.secondaryText,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = pinDialogInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pinDialogInput = it },
                        label = { Text("پین کد جدید (۴ تا ۶ رقم)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.accentCyan,
                            unfocusedBorderColor = theme.border,
                            focusedTextColor = theme.primaryText,
                            unfocusedTextColor = theme.primaryText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pinDialogConfirmInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pinDialogConfirmInput = it },
                        label = { Text("تکرار پین کد") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.accentCyan,
                            unfocusedBorderColor = theme.border,
                            focusedTextColor = theme.primaryText,
                            unfocusedTextColor = theme.primaryText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (pinDialogError != null) {
                        Text(
                            text = pinDialogError ?: "",
                            color = NeonRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinDialogInput.length < 4) {
                            pinDialogError = "طول رمز باید حداقل ۴ رقم باشد"
                        } else if (pinDialogInput != pinDialogConfirmInput) {
                            pinDialogError = "رمز وارد شده با تکرار آن یکسان نیست"
                        } else {
                            val hash = AppLockManager.hashPin(pinDialogInput)
                            viewModel.updateSettings(
                                settings.copy(
                                    appLockEnabled = true,
                                    appLockUseAppPin = true,
                                    appLockAppPinHash = hash
                                )
                            )
                            showAppPinSetupDialog = false
                            pinDialogInput = ""
                            pinDialogConfirmInput = ""
                            pinDialogError = null
                            android.widget.Toast.makeText(context, "پین کد با موفقیت ذخیره و فعال گردید", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentCyan)
                ) {
                    Text(text = "ذخیره و فعالسازی", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAppPinSetupDialog = false
                    pinDialogInput = ""
                    pinDialogConfirmInput = ""
                    pinDialogError = null
                }) {
                    Text(text = "انصراف", color = theme.mutedText)
                }
            }
        )
    }

    // Alarms Backup Restore Mode Dialog
    pendingRestoreAlarms?.let { importedList ->
        AlertDialog(
            onDismissRequest = { pendingRestoreAlarms = null },
            containerColor = theme.surface,
            title = {
                Text(
                    text = "بازگرداندن لیست آلارم‌ها",
                    color = theme.accentCyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "تعداد ${importedList.size} آلارم در فایل پشتیبان یافت شد. نحوه بازگرداندن را انتخاب کنید:",
                        color = theme.primaryText,
                        fontSize = 13.sp
                    )
                    Button(
                        onClick = {
                            viewModel.restoreAlarms(importedList, replaceExisting = false) { count ->
                                backupRestoreIsError = false
                                backupRestoreNotice = "$count آلارم با موفقیت به لیست فعلی اضافه و زمان‌بندی شد."
                            }
                            pendingRestoreAlarms = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("restore_merge_button")
                    ) {
                        Text(
                            text = "افزودن به لیست فعلی (ادغام)",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = {
                            viewModel.restoreAlarms(importedList, replaceExisting = true) { count ->
                                backupRestoreIsError = false
                                backupRestoreNotice = "لیست قبلی جایگزین شد و $count آلارم بازگردانی گردید."
                            }
                            pendingRestoreAlarms = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("restore_replace_button")
                    ) {
                        Text(
                            text = "جایگزینی کامل لیست فعلی",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { pendingRestoreAlarms = null }) {
                    Text(text = "انصراف", color = theme.mutedText)
                }
            }
        )
    }
}

// Helpers
@Composable
fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    val theme = EpicTheme.colors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Text(text = title, color = theme.primaryText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

@Composable
fun ColorPickerRow(
    label: String,
    selectedColor: Long,
    colors: List<Long>,
    onSelectColor: (Long) -> Unit,
    onOpenFullPalette: (() -> Unit)? = null
) {
    val theme = EpicTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = theme.primaryText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            if (onOpenFullPalette != null) {
                OutlinedButton(
                    onClick = onOpenFullPalette,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, theme.accentCyan.copy(alpha = 0.6f)),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = theme.accentCyan, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "پالت ۱۶M + ۳۲ رنگ", color = theme.accentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            items(colors) { colorLong ->
                val isSelected = selectedColor == colorLong
                val colorObj = Color(colorLong)
                val isBright = (colorObj.red * 0.299f + colorObj.green * 0.587f + colorObj.blue * 0.114f) > 0.6f

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colorObj)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) (if (theme.isDark) Color.White else Color.Black) else theme.border,
                            shape = CircleShape
                        )
                        .clickable { onSelectColor(colorLong) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isBright) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = EpicTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = theme.primaryText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = theme.mutedText, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonCyan,
                checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                uncheckedThumbColor = theme.mutedText,
                uncheckedTrackColor = theme.surfaceElevated
            )
        )
    }
}

@Composable
fun RingtoneSelectorRow(
    label: String,
    currentTitle: String,
    onClick: () -> Unit
) {
    val theme = EpicTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(theme.surfaceElevated)
            .border(1.dp, theme.border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = theme.mutedText, fontSize = 11.sp)
            Text(text = currentTitle, color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        Icon(imageVector = Icons.Default.Audiotrack, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun VolumeSliderRow(
    label: String,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onTestSound: () -> Unit
) {
    val theme = EpicTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = theme.primaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "${(volume * 100).toInt()}%", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onTestSound, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Test", tint = NeonAmber, modifier = Modifier.size(18.dp))
                }
            }
        }
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            valueRange = 0.05f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = NeonCyan,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = theme.surfaceElevated
            )
        )
    }
}

@Composable
fun OptionDropdownRow(
    label: String,
    subtitle: String,
    currentValueText: String,
    options: List<Pair<Int, String>>,
    onSelect: (Int) -> Unit
) {
    val theme = EpicTheme.colors
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(theme.surfaceElevated)
            .border(1.dp, theme.border, RoundedCornerShape(10.dp))
            .clickable { showDialog = true }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = theme.primaryText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = theme.mutedText, fontSize = 11.sp)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(theme.surfaceVariant)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(text = currentValueText, color = NeonAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = theme.surface,
            title = { Text(text = label, color = theme.primaryText, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    options.forEach { (value, title) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onSelect(value)
                                    showDialog = false
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = title, color = theme.primaryText, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun StringOptionDropdownRow(
    label: String,
    subtitle: String,
    currentValueText: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit
) {
    val theme = EpicTheme.colors
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(theme.surfaceElevated)
            .border(1.dp, theme.border, RoundedCornerShape(10.dp))
            .clickable { showDialog = true }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = theme.primaryText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = theme.mutedText, fontSize = 11.sp)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(theme.surfaceVariant)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(text = currentValueText, color = NeonAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = theme.surface,
            title = { Text(text = label, color = theme.primaryText, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    options.forEach { (value, title) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onSelect(value)
                                    showDialog = false
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = title, color = theme.primaryText, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun ThemeButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = EpicTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else theme.surfaceElevated)
            .border(1.dp, if (isSelected) color else theme.border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) color else theme.mutedText, modifier = Modifier.size(18.dp))
            Text(text = title, color = if (isSelected) color else theme.secondaryText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SystemExplainerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    val theme = EpicTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(theme.surfaceElevated)
            .border(1.dp, theme.border, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
            Text(text = title, color = theme.primaryText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Text(text = description, color = theme.secondaryText, fontSize = 11.sp, lineHeight = 18.sp)
    }
}

private fun queryAudioTitle(context: Context, uri: Uri): String {
    try {
        val ringtone = RingtoneManager.getRingtone(context, uri)
        val title = ringtone?.getTitle(context)
        if (!title.isNullOrBlank()) return title
    } catch (_: Throwable) {}
    return uri.lastPathSegment ?: "آهنگ انتخابی کاربر"
}
