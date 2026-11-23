package com.example.ui.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundAndVibrationManager
import com.example.data.local.SettingsManager
import com.example.i18n.AppLanguage
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OledBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private val OledCard = Color(0xFF0F172A)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    val settings by settingsManager.settings.collectAsState()
    val scope = rememberCoroutineScope()

    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 6

    val completeOnboarding: () -> Unit = {
        scope.launch {
            settingsManager.updateSettings(settings.copy(onboardingCompleted = true))
            onFinish()
        }
    }

    Scaffold(
        containerColor = OledBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    IconButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.testTag("onboarding_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "مرحله قبل",
                            tint = NeonCyan
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                // Step indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until totalSteps) {
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (i == currentStep) 24.dp else 8.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (i == currentStep) NeonCyan else Color.White.copy(alpha = 0.2f))
                        )
                    }
                }

                TextButton(
                    onClick = completeOnboarding,
                    modifier = Modifier.testTag("onboarding_skip_all_button")
                ) {
                    Text(text = "رد شدن", color = TextSecondary, fontSize = 14.sp)
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep < totalSteps - 1) {
                    Text(
                        text = "مرحله ${currentStep + 1} از $totalSteps",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Button(
                        onClick = { currentStep++ },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.testTag("onboarding_next_step_button")
                    ) {
                        Text(text = "مرحله بعد", color = Color(0xFF0B0E14), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF0B0E14),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = completeOnboarding,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_finish_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF0B0E14)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ورود به برنامه ساعت هشدار اپیک",
                            color = Color(0xFF0B0E14),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding_step_transition"
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    when (step) {
                        0 -> Step1Language(settingsManager = settingsManager)
                        1 -> Step2ThemeAndFont(settingsManager = settingsManager)
                        2 -> Step3InfographicFeatures()
                        3 -> Step4Permissions()
                        4 -> Step5SoundPreview(settingsManager = settingsManager)
                        5 -> Step6GuardianAlert(settingsManager = settingsManager)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// Step 1: Language Selection
@Composable
private fun Step1Language(settingsManager: SettingsManager) {
    val currentLang = settingsManager.getLanguage()
    val scope = rememberCoroutineScope()

    StepHeader(
        icon = Icons.Default.Language,
        title = "انتخاب زبان برنامه",
        subtitle = "زبان مورد نظر خود را برای محیط کاربری و متون انتخاب نمایید."
    )

    val languages = listOf(
        Pair(AppLanguage.PERSIAN, "فارسی (Persian)"),
        Pair(AppLanguage.ENGLISH, "English (انگلیسی)"),
        Pair(AppLanguage.ARABIC, "العربية (Arabic)"),
        Pair(AppLanguage.AZERBAIJANI, "آذربایجان دیلی (Azərbaycanca)")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        languages.forEach { (lang, label) ->
            val isSelected = currentLang == lang
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch { settingsManager.setLanguage(lang) }
                    }
                    .border(
                        1.dp,
                        if (isSelected) NeonCyan else Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = if (isSelected) NeonCyan.copy(alpha = 0.15f) else OledCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = label, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyan)
                    }
                }
            }
        }
    }
}

// Step 2: Theme & Font Preview
@Composable
private fun Step2ThemeAndFont(settingsManager: SettingsManager) {
    val currentTheme = settingsManager.getThemeMode()
    val scope = rememberCoroutineScope()
    var fontScale by remember { mutableFloatStateOf(1.0f) }

    StepHeader(
        icon = Icons.Default.Palette,
        title = "پوسته ظاهری و اندازه فونت",
        subtitle = "تم تاریک OLED برای صرفه‌جویی باتری یا تم روشن به دلخواه شما."
    )

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        ThemeChoiceCard(
            title = "OLED تاریک",
            selected = currentTheme == AppThemeMode.DARK,
            color = Color(0xFF05070B),
            modifier = Modifier.weight(1f)
        ) {
            scope.launch { settingsManager.setThemeMode(AppThemeMode.DARK) }
        }

        ThemeChoiceCard(
            title = "پوسته روشن",
            selected = currentTheme == AppThemeMode.LIGHT,
            color = Color(0xFFF8FAFC),
            modifier = Modifier.weight(1f)
        ) {
            scope.launch { settingsManager.setThemeMode(AppThemeMode.LIGHT) }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "پیش‌نمایش اندازه قلم متون",
        style = MaterialTheme.typography.titleSmall,
        color = NeonCyan
    )

    Slider(
        value = fontScale,
        onValueChange = { fontScale = it },
        valueRange = 0.85f..1.35f,
        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OledCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "ساعت هشدار ۰۷:۳۰ صبح",
                fontSize = (18 * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "امروز جمعه ۱۸ آبان - تقویم سه‌گانه فعال",
                fontSize = (13 * fontScale).sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ThemeChoiceCard(
    title: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .border(2.dp, if (selected) NeonCyan else Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = OledCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, Color.Gray, CircleShape)
            )
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

// Step 3: Infographic Feature Overview
@Composable
private fun Step3InfographicFeatures() {
    StepHeader(
        icon = Icons.Default.Info,
        title = "امکانات حرفه‌ای ساعت هشدار اپیک",
        subtitle = "یک ابزار قدرتمند، دقیق و چندمنظوره برای شروع پرانرژی روز شما."
    )

    val features = listOf(
        Pair("⏰ دقت ثانیه‌ای با بیدارباش تضمینی", "بدون به خواب رفتن پروسه آلارم توسط سیستم‌عامل اندروید"),
        Pair("📅 ویجت‌های تقویم سه‌گانه و ساعت آنالوگ", "نمایش همزمان تاریخ خورشیدی، میلادی و قمری روی هوم‌اسکرین"),
        Pair("📳 قطع زنگ با تکان دادن (Shake to Stop)", "خاموش کردن آلارم در تاریکی تنها با تکان دادن گوشی"),
        Pair("🔘 قطع با کلید فیزیکی پاور (Power Button)", "تنظیم تعداد مرتبه فشردن کلید پاور برای خاموشی مطمئن"),
        Pair("🛡️ هشدار ثانویه نگهبان (Guardian Alert)", "ارسال خودکار پیامک اضطراری به مخاطب در صورت عدم قطع زنگ"),
        Pair("🔄 تعویق با برگرداندن گوشی (Flip to Snooze)", "با برگرداندن گوشی به سمت زمین، زنگ به صورت خودکار به تعویق می‌افتد")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        features.forEach { (title, desc) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = OledCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = title, style = MaterialTheme.typography.bodyMedium, color = NeonCyan, fontWeight = FontWeight.Bold)
                    Text(text = desc, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}

// Step 4: Notifications & Lock Screen Permissions
@Composable
private fun Step4Permissions() {
    var isGranted by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        isGranted = granted
    }

    StepHeader(
        icon = Icons.Default.NotificationsActive,
        title = "مجوز نوتیفیکیشن و صفحه قفل",
        subtitle = "جهت نمایش تمام‌صفحه زنگ هشدار روی قفل صفحه (Lock Screen) و سامسونگ One UI."
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OledCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "مجوز ارسال اعلان و تمام‌صفحه برای عملکرد دقیق آلارم حیاتی است. لطفاً دسترسی را تأیید کنید.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Button(
                onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                colors = ButtonDefaults.buttonColors(containerColor = if (isGranted) NeonCyan else NeonAmber),
                modifier = Modifier.fillMaxWidth().testTag("grant_notification_permission_btn")
            ) {
                Icon(if (isGranted) Icons.Default.Check else Icons.Default.NotificationsActive, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isGranted) "مجوز تأیید شد ✓" else "درخواست مجوز نوتیفیکیشن")
            }
        }
    }
}

// Step 5: Sound Previews
@Composable
private fun Step5SoundPreview(settingsManager: SettingsManager) {
    val context = LocalContext.current
    val settings by settingsManager.settings.collectAsState()
    val scope = rememberCoroutineScope()
    var playingSoundId by remember { mutableStateOf<String?>(null) }

    StepHeader(
        icon = Icons.Default.GraphicEq,
        title = "پیش‌شنوایی و انتخاب آهنگ پیش‌فرض",
        subtitle = "از بین صداهای ساخته‌شده اختصاصی نئونی و آرامش‌بخش انتخاب کنید."
    )

    val sounds = SoundAndVibrationManager.builtInSounds

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        sounds.take(4).forEach { sound ->
            val isSelected = settings.defaultAlarmSound == sound.id
            val isPlaying = playingSoundId == sound.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch {
                            settingsManager.updateSettings(
                                settings.copy(defaultAlarmSound = sound.id, defaultAlarmSoundTitle = sound.nameFa)
                            )
                        }
                    }
                    .border(1.dp, if (isSelected) NeonCyan else Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = if (isSelected) NeonCyan.copy(alpha = 0.15f) else OledCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = sound.nameFa, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(text = sound.nameEn, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }

                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                SoundAndVibrationManager.stopAlarm(context)
                                playingSoundId = null
                            } else {
                                SoundAndVibrationManager.playAlarm(
                                    context = context,
                                    soundIdOrUri = sound.id,
                                    vibrationPattern = "NONE",
                                    volume = 0.8f,
                                    autoSilenceMinutes = 1
                                )
                                playingSoundId = sound.id
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "پخش",
                            tint = if (isPlaying) NeonRed else NeonCyan
                        )
                    }
                }
            }
        }
    }
}

// Step 6: Guardian Alert Setup
@Composable
private fun Step6GuardianAlert(settingsManager: SettingsManager) {
    val settings by settingsManager.settings.collectAsState()
    val scope = rememberCoroutineScope()
    var phoneInput by remember { mutableStateOf(settings.guardianPhoneNumber) }
    var enabled by remember { mutableStateOf(settings.guardianAlertEnabled) }

    val smsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            enabled = true
            scope.launch {
                settingsManager.updateSettings(
                    settings.copy(guardianAlertEnabled = true, guardianPhoneNumber = phoneInput)
                )
            }
        }
    }

    StepHeader(
        icon = Icons.Default.Security,
        title = "تنظیم نگهبان اضطراری (Guardian Alert)",
        subtitle = "در صورت عدم بیداری پس از چند دقیقه، به مخاطب اضطراری شما پیامک ارسال می‌شود."
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OledCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "فعالسازی هشدار پیامکی", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Switch(
                    checked = enabled,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            smsLauncher.launch(Manifest.permission.SEND_SMS)
                        } else {
                            enabled = false
                            scope.launch {
                                settingsManager.updateSettings(settings.copy(guardianAlertEnabled = false))
                            }
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.5f))
                )
            }

            OutlinedTextField(
                value = phoneInput,
                onValueChange = {
                    phoneInput = it
                    scope.launch {
                        settingsManager.updateSettings(settings.copy(guardianPhoneNumber = it))
                    }
                },
                label = { Text("شماره همراه اضطراری (مثال: 0912...)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }
    }
}

@Composable
private fun StepHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.15f))
                .border(1.dp, NeonCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(32.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
