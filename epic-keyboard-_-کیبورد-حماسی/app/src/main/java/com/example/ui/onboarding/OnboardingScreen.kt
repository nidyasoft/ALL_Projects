package com.example.ui.onboarding

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.SmartKeyboardApp
import com.example.settings.KeyboardLanguage
import com.example.settings.KeyboardSettingsState
import java.util.Locale

@Composable
fun OnboardingScreen(
    settings: KeyboardSettingsState,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs = SmartKeyboardApp.instance.preferences
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 5

    // Detect device system language
    val systemLocale = Locale.getDefault()
    val isSystemRtl = remember {
        val lang = systemLocale.language.lowercase()
        lang == "fa" || lang == "ar" || lang == "ur" || lang == "he"
    }

    var isImeEnabled by remember { mutableStateOf(false) }
    var isImeSelected by remember { mutableStateOf(false) }

    fun refreshImeStatus() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val enabledMethods = imm?.enabledInputMethodList ?: emptyList()
        val pkg = context.packageName
        isImeEnabled = enabledMethods.any { it.packageName == pkg }

        val currentIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) ?: ""
        isImeSelected = currentIme.contains(pkg)
    }

    LaunchedEffect(currentStep) {
        refreshImeStatus()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Navigation footer with progress & sleek buttons
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back or Skip
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مرحله قبل")
                        }
                    } else {
                        TextButton(
                            onClick = {
                                prefs.setOnboardingCompleted(true)
                                onFinish()
                            }
                        ) {
                            Text("رد کردن و ورود", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Modern Step Indicator Dots with animated size
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..totalSteps) {
                            val isActive = i == currentStep
                            val dotWidth by animateFloatAsState(
                                targetValue = if (isActive) 24f else 8f,
                                animationSpec = tween(300, easing = FastOutSlowInEasing),
                                label = "dotWidth"
                            )
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(dotWidth.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isActive) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                            )
                        }
                    }

                    // Next or Finish
                    if (currentStep < totalSteps) {
                        Button(
                            onClick = { currentStep++ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("مرحله بعد")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                prefs.setOnboardingCompleted(true)
                                onFinish()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("شروع استفاده", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with App Logo & Step Counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.epic_keyboard_logo),
                    contentDescription = "Epic Keyboard Logo",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Epic Keyboard / کیبورد حماسی",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "اینفوگرافیک و راهنمای راه‌اندازی • گام $currentStep از $totalSteps",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Step Content with Modern Animated Slide + Fade
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width / 3 } + fadeIn(tween(350)))
                            .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut(tween(250)))
                    } else {
                        (slideInHorizontally { width -> -width / 3 } + fadeIn(tween(350)))
                            .togetherWith(slideOutHorizontally { width -> width / 3 } + fadeOut(tween(250)))
                    }
                },
                label = "step_infographic_transition"
            ) { step ->
                when (step) {
                    1 -> Step1PhoneLanguageAndAlignment(
                        settings = settings,
                        systemLocale = systemLocale,
                        isSystemRtl = isSystemRtl,
                        onUpdateForceLtr = { prefs.updateForceLtrKeyboardLayout(it) }
                    )
                    2 -> Step2PrimaryLanguage(
                        settings = settings,
                        onSelectLanguage = { prefs.updateLanguage(it) }
                    )
                    3 -> Step3KeyboardRowsCustomization(
                        settings = settings,
                        onUpdateTopHarakat = { prefs.updateFeatureToggle("harakat_top_row_enabled", it) { copy(harakatTopRowEnabled = it) } },
                        onUpdateBottomHarakat = { prefs.updateFeatureToggle("harakat_bottom_row_enabled", it) { copy(harakatBottomRowEnabled = it) } },
                        onUpdateNumberRow = { prefs.updateFeatureToggle("number_row_enabled", it) { copy(numberRowEnabled = it) } },
                        onUpdateSuggestions = { prefs.updateFeatureToggle("suggestions_enabled", it) { copy(suggestionsEnabled = it) } }
                    )
                    4 -> Step4ActivationStepOne(
                        isImeEnabled = isImeEnabled,
                        onOpenSettings = {
                            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        },
                        onRefresh = { refreshImeStatus() }
                    )
                    5 -> Step5ActivationStepTwo(
                        isImeSelected = isImeSelected,
                        onOpenPicker = {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.showInputMethodPicker()
                        },
                        onRefresh = { refreshImeStatus() },
                        onFinish = {
                            prefs.setOnboardingCompleted(true)
                            onFinish()
                        }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 1: Phone Language & Alignment (Animated Infographic)
// -------------------------------------------------------------
@Composable
private fun Step1PhoneLanguageAndAlignment(
    settings: KeyboardSettingsState,
    systemLocale: Locale,
    isSystemRtl: Boolean,
    onUpdateForceLtr: (Boolean) -> Unit
) {
    // Pulse animation for alignment schematic
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Modern Infographic Hero Card with Gradient
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "۱. هماهنگی زبان دستگاه و چیدمان استاندارد",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "زبان سیستم: ${systemLocale.displayLanguage} (${systemLocale.language})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (isSystemRtl) {
                        Text(
                            text = "جهت زبان سیستم دستگاه راست‌به‌چپ (RTL) است.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "در گوشی‌هایی با زبان راست‌به‌چپ (فارسی، عربی و ...)، سیستم‌عامل تمایل دارد ردیف‌ها را قرینه کند. برای جلوگیری از برعکس شدن حروف و تطابق کامل با استاندارد جهانی ISIRI، کیبورد به صورت هوشمند استاندارد LTR را حفظ می‌کند:",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp,
            textAlign = TextAlign.Right
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Infographic Comparison Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "اینفوگرافیک چیدمان فیزیکی حروف بر روی صفحه:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (settings.forceLtrKeyboardLayout) "استاندارد LTR ✓" else "قرینه RTL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Keyboard Mock Visualization
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val sampleKeys = if (settings.forceLtrKeyboardLayout) {
                        listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "چ")
                    } else {
                        listOf("چ", "ج", "ح", "خ", "ه", "ع", "غ", "ف", "ق", "ث", "ص", "ض")
                    }
                    sampleKeys.forEach { k ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = k, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (settings.forceLtrKeyboardLayout)
                        "استاندارد ملی و جهانی: شروع ردیف از «ض/ش/ظ» در سمت چپ"
                    else
                        "حالت معکوس: شروع ردیف از «چ/گ/» در سمت راست",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Switch to enforce LTR Standard vs RTL Mirrored
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "چیدمان استاندارد جهانی (توصیه‌شده)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "جلوگیری از برعکس شدن چینش حروف در زبان فارسی گوشی",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.forceLtrKeyboardLayout,
                    onCheckedChange = { onUpdateForceLtr(it) }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 2: Primary Active Language Selection (Infographic Cards)
// -------------------------------------------------------------
@Composable
private fun Step2PrimaryLanguage(
    settings: KeyboardSettingsState,
    onSelectLanguage: (KeyboardLanguage) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "۲. انتخاب زبان اصلی و فعال کیبورد",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "زبان مورد نظر خود برای شروع کار با کیبورد را برگزینید:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        KeyboardLanguage.entries.forEach { lang ->
            val isSelected = settings.currentLanguage == lang
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelectLanguage(lang) },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder(isSelected)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = lang.id.take(2).uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lang.nativeName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${lang.displayName} • ${if (lang.isRtl) "راست‌به‌چپ (RTL)" else "چپ‌به‌راست (LTR)"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "انتخاب شده",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 3: Keyboard Rows Customization (Infographic Toggles)
// -------------------------------------------------------------
@Composable
private fun Step3KeyboardRowsCustomization(
    settings: KeyboardSettingsState,
    onUpdateTopHarakat: (Boolean) -> Unit,
    onUpdateBottomHarakat: (Boolean) -> Unit,
    onUpdateNumberRow: (Boolean) -> Unit,
    onUpdateSuggestions: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ViewAgenda,
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "۳. سفارشی‌سازی ردیف‌های عملکردی کیبورد",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "ردیف‌های بالای صفحه کیبورد را بر اساس نیاز خود فعال یا غیرفعال کنید:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Top Harakat Row
        RowToggleCard(
            title = "ردیف بالای حرکه‌ها و نشانه‌ها",
            subtitle = "شامل ۸ نشانه: ـٓ / ـٔ / ـ / ـٕ / ـٰ / « / » / ٫",
            badge = "بالا",
            checked = settings.harakatTopRowEnabled,
            onCheckedChange = onUpdateTopHarakat
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Harakat Row
        RowToggleCard(
            title = "ردیف پایین حرکه‌ها و اعراب معکوس",
            subtitle = "شامل اعراب با چیدمان جدید: ٍ / ٌ / ً / ْ / ّ / ِ / ُ / َ",
            badge = "پایین",
            checked = settings.harakatBottomRowEnabled,
            onCheckedChange = onUpdateBottomHarakat
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Number Row
        RowToggleCard(
            title = "ردیف اختصاصی اعداد بالای کیبورد",
            subtitle = "دسترسی مستقیم و سریع به ارقام (۱۲۳۴۵۶۷۸۹۰)",
            badge = "اعداد",
            checked = settings.numberRowEnabled,
            onCheckedChange = onUpdateNumberRow
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Suggestion Bar
        RowToggleCard(
            title = "نوار پیشنهاد هوشمند ۴ کلمه",
            subtitle = "پیشنهاد پیش‌بینانه، غلط‌یاب و سرعت تایپ بالا",
            badge = "پیشنهادات",
            checked = settings.suggestionsEnabled,
            onCheckedChange = onUpdateSuggestions
        )
    }
}

@Composable
private fun RowToggleCard(
    title: String,
    subtitle: String,
    badge: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (checked) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

// -------------------------------------------------------------
// STEP 4: Activation Guide - Step 1 (Animated Infographic)
// -------------------------------------------------------------
@Composable
private fun Step4ActivationStepOne(
    isImeEnabled: Boolean,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseSwitch")
    val toggleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "toggleScale"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "۴. فعال‌سازی کیبورد در سیستم اندروید (گام اول)",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "برای امکان استفاده از Epic Keyboard در تمام اپ‌ها باید آن را در تنظیمات دستگاه فعال کنید:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Infographic Schematic Guide
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "شماتیک راهنما در صفحه تنظیمات ورودی گوشی:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Schematic Box with animated highlighted switch
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SchematicSettingItem(name = "Gboard", enabled = true)
                    SchematicSettingItem(
                        name = "Epic Keyboard (کیبورد حماسی)",
                        enabled = isImeEnabled,
                        isHighlighted = true,
                        pulseScale = if (!isImeEnabled) toggleScale else 1f
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "دکمه روبروی «Epic Keyboard» را در وضعیت روشن قرار دهید.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isImeEnabled) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isImeEnabled) Icons.Default.CheckCircle else Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = if (isImeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isImeEnabled) "کیبورد حماسی در تنظیمات دستگاه فعال شد ✓"
                    else "کیبورد هنوز در تنظیمات فعال نشده است",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isImeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onOpenSettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("ورود مستقیم به تنظیمات کیبوردهای گوشی", fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------
// STEP 5: Activation Guide - Step 2 (Select as Default Keyboard)
// -------------------------------------------------------------
@Composable
private fun Step5ActivationStepTwo(
    isImeSelected: Boolean,
    onOpenPicker: () -> Unit,
    onRefresh: () -> Unit,
    onFinish: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseRadio")
    val radioScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radioScale"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Keyboard,
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "۵. انتخاب به عنوان کیبورد پیش‌فرض (گام دوم)",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "کیبورد حماسی را در پنجره انتخاب ورودی، به عنوان کیبورد پیش‌فرض انتخاب کنید:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Infographic Schematic Dialog Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "شماتیک پنجره انتخاب کیبورد (Input Method Picker):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SchematicPickerItem(name = "Gboard", selected = !isImeSelected)
                    SchematicPickerItem(
                        name = "Epic Keyboard (کیبورد حماسی)",
                        selected = isImeSelected,
                        isHighlighted = true,
                        pulseScale = if (!isImeSelected) radioScale else 1f
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "روی گزینه «Epic Keyboard» لمس کنید تا انتخاب شود.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isImeSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isImeSelected) Icons.Default.CheckCircle else Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = if (isImeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isImeSelected) "کیبورد حماسی هم‌اکنون کیبورد فعال پیش‌فرض است ✓"
                    else "کیبورد حماسی هنوز به عنوان کیبورد پیش‌فرض انتخاب نشده است",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isImeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onOpenPicker,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Keyboard, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("باز کردن پنجره انتخاب کیبورد پیش‌فرض", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SchematicSettingItem(
    name: String,
    enabled: Boolean,
    isHighlighted: Boolean = false,
    pulseScale: Float = 1f
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isHighlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Keyboard,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
        Box(modifier = Modifier.scale(pulseScale)) {
            Switch(
                checked = enabled,
                onCheckedChange = null,
                modifier = Modifier.size(scale = 0.8f, default = 36.dp)
            )
        }
    }
}

@Composable
private fun SchematicPickerItem(
    name: String,
    selected: Boolean,
    isHighlighted: Boolean = false,
    pulseScale: Float = 1f
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isHighlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .scale(pulseScale)
                .size(20.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun Modifier.size(scale: Float, default: Dp): Modifier {
    return this.size(default * scale)
}
