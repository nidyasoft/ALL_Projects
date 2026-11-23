package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FluidBackground
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun AdvancedFeaturesScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit
) {
    val lang by viewModel.selectedLanguage.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val activeThemeIdx by viewModel.appThemeIndex.collectAsState()
    val activeSubThemeIdx by viewModel.subThemeIndex.collectAsState()

    val screenTitle = when (lang) {
        "fa" -> "امکانات پیشرفته و هوشمند"
        "az" -> "قابلیت‌لر و گوجلو ایمکانات"
        else -> "Advanced AI Features"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FluidBackground(
            themeIndex = activeThemeIdx,
            subThemeIndex = activeSubThemeIdx,
            isPlaying = isPlaying
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = screenTitle,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    BiometricSleepTimerView(viewModel, lang)
                }
            }
        }
    }
}

@Composable
fun BiometricSleepTimerView(viewModel: MusicViewModel, lang: String) {
    val context = LocalContext.current
    val biometricSleepEnabled by viewModel.biometricSleepEnabled.collectAsState()
    val isUserAsleep by viewModel.isUserAsleep.collectAsState()
    val accelData by viewModel.accelerometerData.collectAsState()
    val sensitivity by viewModel.biometricSensitivity.collectAsState()
    val remainingTimeMs by viewModel.biometricRemainingTimeMs.collectAsState()

    // Translations
    val titleText = when (lang) {
        "fa" -> "تایمر خواب بیومتریک هوشمند"
        "az" -> "آغیللی بیومتریک یوخو تایمری"
        else -> "Smart Biometric Sleep Timer"
    }

    val subtitleText = when (lang) {
        "fa" -> "تشخیص هوشمند خواب با سنسور شتاب‌سنج دستگاه"
        "az" -> "جهازین شتاب‌سنج سنسوری ایله آغیللی یوخو تایمری"
        else -> "Intelligent sleep detection via device accelerometer"
    }

    val liveGraphTitle = when (lang) {
        "fa" -> "نمودار زنده شتاب‌سنج (حرکات بدن):"
        "az" -> "شتاب‌سنجین دیری نموداری (بدن حرکت‌لری):"
        else -> "Live Accelerometer Telemetry Graph:"
    }

    val activateMsg = when (lang) {
        "fa" -> "جهت پایش زنده، تایمر خواب را فعال کنید"
        "az" -> "دیری پایش اوچون، یوخو تایمرینی فعال ائدین"
        else -> "Activate sleep timer to start live monitoring"
    }

    val sensitivityLabel = when (lang) {
        "fa" -> "میزان حساسیت تشخیص حرکت:"
        "az" -> "حرکتی تشخیص ائتمک حساسیتی:"
        else -> "Movement Detection Sensitivity:"
    }

    val specsTitle = when (lang) {
        "fa" -> "خلاصه مشخصات و نحوه عملکرد:"
        "az" -> "اؤزللیک‌لرین خلاصه‌سی و چالیشما طرزی:"
        else -> "How it works & Specifications:"
    }

    val specLow = when (lang) {
        "fa" -> "حالت کم: ۲۰ دقیقه بی‌حرکتی کامل ➔ ۱ دقیقه کاهش تدریجی صدا تا صفر"
        "az" -> "آشاغی حالت: ۲۰ دقیقه تام حرکتیز قالماق ➔ ۱ دقیقه سسین یاواشلاشیب دایانماسی"
        else -> "Low Mode: 20 minutes stillness ➔ 1 minute fade-out to zero volume"
    }

    val specMedium = when (lang) {
        "fa" -> "حالت متوسط: ۱۰ دقیقه بی‌حرکتی کامل ➔ ۱ دقیقه کاهش تدریجی صدا تا صفر"
        "az" -> "اورتا حالت: ۱۰ دقیقه تام حرکتیز قالماق ➔ ۱ دقیقه سسین یاواشلاشیب دایانماسی"
        else -> "Medium Mode: 10 minutes stillness ➔ 1 minute fade-out to zero volume"
    }

    val specHigh = when (lang) {
        "fa" -> "حالت زیاد: ۵ دقیقه بی‌حرکتی کامل ➔ ۱ دقیقه کاهش تدریجی صدا تا صفر"
        "az" -> "یوخاری حالت: ۵ دقیقه تام حرکتیز قالماق ➔ ۱ دقیقه سسین یاواشلاشیب دایانماسی"
        else -> "High Mode: 5 minutes stillness ➔ 1 minute fade-out to zero volume"
    }

    val statusSystemReady = when (lang) {
        "fa" -> "سیستم آماده پایش حرکت بدنی"
        "az" -> "سیستم بدن حرکتی پایش ائتمه‌یه حاضردیر"
        else -> "System ready to detect sleep stillness"
    }

    val statusUserAsleep = when (lang) {
        "fa" -> "خواب شناسایی شد! موسیقی با ملایمت متوقف گردید."
        "az" -> "یوخو تشخیص وئریلدی! موسیقی آرامشله دایاندی."
        else -> "Sleep detected! Music faded out and paused."
    }

    val statusDesc = when (lang) {
        "fa" -> "با قرار دادن گوشی در نزدیکی خود، پس از خواب رفتن و توقف حرکات طبیعی بدن، موسیقی به صورت خودکار با افکت Fade-out به صفر رسیده و متوقف خواهد شد."
        "az" -> "تلفونو یانیزدا قویمایلا، یوخویا گئدندن و طبیعی حرکت‌لر دایاناندان سونرا، موسیقی اؤزوآلیشیندان Fade-out افکتی ایله دایاناجاقدیر."
        else -> "By placing your phone nearby, the app will auto-detect sleep stillness, softly fading out your music to zero over 1 minute."
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Main Switch Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitleText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = biometricSleepEnabled,
                onCheckedChange = { viewModel.toggleBiometricSleep(context, it) },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Countdown Timer Overlay
        if (biometricSleepEnabled && !isUserAsleep) {
            val totalSecs = remainingTimeMs / 1000
            val minutes = totalSecs / 60
            val seconds = totalSecs % 60
            val remainingLabel = when (lang) {
                "fa" -> "زمان باقی‌مانده تا آغاز توقف موسیقی:"
                "az" -> "چالغینین کسمه‌سینه قالان زمان:"
                else -> "Remaining stillness countdown:"
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = remainingLabel,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Live Accelerometer Graph
        Text(
            text = liveGraphTitle,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Live movement values array
        val linePointsX = remember { mutableStateListOf<Float>() }
        val linePointsY = remember { mutableStateListOf<Float>() }
        val linePointsZ = remember { mutableStateListOf<Float>() }

        LaunchedEffect(accelData, biometricSleepEnabled) {
            if (biometricSleepEnabled) {
                linePointsX.add(accelData[0])
                linePointsY.add(accelData[1])
                linePointsZ.add(accelData[2])
                if (linePointsX.size > 50) {
                    linePointsX.removeAt(0)
                    linePointsY.removeAt(0)
                    linePointsZ.removeAt(0)
                }
            } else {
                linePointsX.clear()
                linePointsY.clear()
                linePointsZ.clear()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val midY = height / 2

                drawLine(
                    color = Color.Gray.copy(alpha = 0.15f),
                    start = Offset(0f, midY),
                    end = Offset(width, midY),
                    strokeWidth = 2f
                )

                if (linePointsX.isNotEmpty()) {
                    val stepX = width / 50f
                    val scaleY = height / 25f

                    val pathX = Path()
                    val pathY = Path()
                    val pathZ = Path()

                    linePointsX.forEachIndexed { i, xVal ->
                        val plotX = i * stepX
                        val plotY = midY - (xVal * scaleY)
                        if (i == 0) pathX.moveTo(plotX, plotY) else pathX.lineTo(plotX, plotY)
                    }

                    linePointsY.forEachIndexed { i, yVal ->
                        val plotX = i * stepX
                        val plotY = midY - (yVal * scaleY)
                        if (i == 0) pathY.moveTo(plotX, plotY) else pathY.lineTo(plotX, plotY)
                    }

                    linePointsZ.forEachIndexed { i, zVal ->
                        val plotX = i * stepX
                        val plotY = midY - ((zVal - 9.8f) * scaleY)
                        if (i == 0) pathZ.moveTo(plotX, plotY) else pathZ.lineTo(plotX, plotY)
                    }

                    drawPath(pathX, Color(0xFFE91E63), style = Stroke(width = 3f, cap = StrokeCap.Round))
                    drawPath(pathY, Color(0xFF00BCD4), style = Stroke(width = 3f, cap = StrokeCap.Round))
                    drawPath(pathZ, Color(0xFF4CAF50), style = Stroke(width = 3f, cap = StrokeCap.Round))
                }
            }

            if (!biometricSleepEnabled) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = activateMsg,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sensors badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val labelX = if (lang == "fa") "افقی (X)" else if (lang == "az") "افقی (X)" else "Horizontal X"
            val labelY = if (lang == "fa") "عمودی (Y)" else if (lang == "az") "عمودی (Y)" else "Vertical Y"
            val labelZ = if (lang == "fa") "جاذبه (Z)" else if (lang == "az") "جاذبه (Z)" else "Gravity Z"

            SensorValueBadge(label = labelX, value = String.format("%.1f", accelData[0]), color = Color(0xFFE91E63))
            SensorValueBadge(label = labelY, value = String.format("%.1f", accelData[1]), color = Color(0xFF00BCD4))
            SensorValueBadge(label = labelZ, value = String.format("%.1f", accelData[2]), color = Color(0xFF4CAF50))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sensitivity Switch
        Text(
            text = sensitivityLabel,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sensList = listOf(
                "low" to (if (lang == "fa") "کم" else if (lang == "az") "آشاغی" else "Low"),
                "medium" to (if (lang == "fa") "متوسط" else if (lang == "az") "اورتا" else "Medium"),
                "high" to (if (lang == "fa") "زیاد" else if (lang == "az") "یوخاری" else "High")
            )
            sensList.forEach { (id, name) ->
                val active = sensitivity == id
                OutlinedButton(
                    onClick = { viewModel.setBiometricSensitivity(id) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    ),
                    border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = name,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // HOW IT WORKS & SPECS CARD (Requirement 1)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = specsTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                BulletSpecRow(text = specLow)
                Spacer(modifier = Modifier.height(6.dp))
                BulletSpecRow(text = specMedium)
                Spacer(modifier = Modifier.height(6.dp))
                BulletSpecRow(text = specHigh)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sleep status description
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isUserAsleep) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isUserAsleep) Icons.Default.Bedtime else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (isUserAsleep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (isUserAsleep) statusUserAsleep else statusSystemReady,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = statusDesc,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BulletSpecRow(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "• ",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 14.sp
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun SensorValueBadge(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "$label: $value", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
