package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SmartKeyboardApp
import com.example.settings.KeyboardFont
import com.example.settings.KeyboardLanguage
import com.example.settings.KeyboardMode
import com.example.settings.KeyboardSettingsState
import com.example.settings.KeyboardTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: KeyboardSettingsState,
    onNavigateBack: () -> Unit,
    onOpenOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val prefs = SmartKeyboardApp.instance.preferences

    var isShareSheetVisible by remember { mutableStateOf(false) }
    var isPersonalDictionaryOpen by remember { mutableStateOf(false) }

    if (isPersonalDictionaryOpen) {
        PersonalDictionaryScreen(
            initialLanguage = settings.currentLanguage,
            onNavigateBack = { isPersonalDictionaryOpen = false }
        )
        return
    }

    if (isShareSheetVisible) {
        com.example.ui.common.ShareAppBottomSheet(
            onDismissRequest = { isShareSheetVisible = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات کیبورد حماسی", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    com.example.ui.common.DistinctiveShareButton(
                        isCompact = false,
                        text = "اشتراک برنامه",
                        onClick = { isShareSheetVisible = true },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 0: Welcome / Onboarding Wizard Launcher Card
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenOnboarding() },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                border = CardDefaults.outlinedCardBorder(true)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "جادوگر راه‌اندازی و راهنما (۵ مرحله)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "تنظیم جهت، زبان، ردیف‌ها و آموزش فعال‌سازی مرحله‌به‌مرحله",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Section: Keyboard Orientation & Direction (RTL / LTR)
            SettingsCategoryCard(
                title = "چیدمان و جهت کیبورد (RTL و LTR)",
                icon = Icons.Default.Keyboard
            ) {
                Text(
                    text = "در گوشی‌هایی با زبان فارسی/عربی، جهت سیستم راست‌به‌چپ است. حالت استاندارد جهانی از چیدمان فیزیکی استاندارد کیبوردها (شروع از چپ) محافظت می‌کند:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                SwitchSettingRow(
                    title = "چیدمان استاندارد جهانی (چپ‌به‌راست LTR)",
                    subtitle = if (settings.forceLtrKeyboardLayout) {
                        "فعال (توصیه‌شده): چیدمان کلیدها طبق استاندارد استاندارد ملی و جهانی چیده می‌شود"
                    } else {
                        "غیرفعال: کلیدها بر اساس زبان سیستم قرینه می‌شوند (راست‌به‌چپ RTL)"
                    },
                    checked = settings.forceLtrKeyboardLayout,
                    onCheckedChange = {
                        prefs.updateForceLtrKeyboardLayout(it)
                    }
                )
            }

            // Section 1: Keyboard Mode Selection (4 Modes)
            SettingsCategoryCard(
                title = "حالت‌های نمایش کیبورد (۴ حالت)",
                icon = Icons.Default.Keyboard
            ) {
                KeyboardMode.entries.forEach { mode ->
                    val isSelected = settings.mode == mode
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { prefs.updateMode(mode) },
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = CardDefaults.outlinedCardBorder(isSelected)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { prefs.updateMode(mode) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.titlePersian,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = mode.descriptionPersian,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Configuration for One-Handed Compression (10% / 15% / 20% / 30%)
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "میزان فشرده‌سازی و فضای خالی در حالت یک‌دست:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "کیبورد فشرده شده و مابقی فضا به عنوان حاشیه خالی در سمت چپ یا راست قرار می‌گیرد.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )

                        val compressionOptions = listOf(10, 15, 20, 30)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            compressionOptions.forEach { pct ->
                                val isChosen = settings.oneHandedCompressionPercent == pct
                                FilterChip(
                                    selected = isChosen,
                                    onClick = { prefs.updateOneHandedCompression(pct) },
                                    label = {
                                        Text(
                                            text = "$pct درصد",
                                            fontSize = 12.sp,
                                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Fonts Selection per Language (5 Fonts)
            SettingsCategoryCard(
                title = "فونت اختصاصی هر زبان (۵ فونت مدرن)",
                icon = Icons.Default.FontDownload
            ) {
                Text(
                    text = "شامل فونت‌های سنس مدرن، نوتو سنس، نوتو نسخ، پاپینز و سنس پیش‌فرض سیستم:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Select Language to configure font
                var selectedLanguageForFont by remember { mutableStateOf(KeyboardLanguage.PERSIAN) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    KeyboardLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = selectedLanguageForFont == lang,
                            onClick = { selectedLanguageForFont = lang },
                            label = { Text(lang.nativeName, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val currentFontForSelectedLang = when (selectedLanguageForFont) {
                    KeyboardLanguage.PERSIAN -> settings.fontForPersian
                    KeyboardLanguage.ENGLISH -> settings.fontForEnglish
                    KeyboardLanguage.TURKISH -> settings.fontForTurkish
                    KeyboardLanguage.AZERBAIJANI_LATIN -> settings.fontForAzLatin
                    KeyboardLanguage.AZERBAIJANI_ARABIC -> settings.fontForAzArabic
                    KeyboardLanguage.ARABIC -> settings.fontForArabic
                }

                KeyboardFont.entries.forEach { font ->
                    val isFontSelected = currentFontForSelectedLang == font
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                prefs.updateFontForLanguage(selectedLanguageForFont, font)
                            },
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isFontSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = CardDefaults.outlinedCardBorder(isFontSelected)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = font.displayNamePersian,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = font.toFontFamily(),
                                    color = if (isFontSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = font.displayNameEnglish,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isFontSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: 4 Independent Font Size Sliders
            SettingsCategoryCard(
                title = "اندازه فونت اجزای کیبورد (۴ بخش مجزا)",
                icon = Icons.Default.FormatSize
            ) {
                // 1. Harakat Size (14sp to 40sp)
                SliderSettingRow(
                    title = "۱. اندازه حرکه‌ها و اعراب بالای کیبورد",
                    valueText = "${settings.fontSizeHarakatSp.roundToInt()} sp",
                    value = settings.fontSizeHarakatSp,
                    range = 14f..40f,
                    onValueChange = {
                        prefs.updateFontSizes(
                            lettersSp = settings.fontSizeLettersSp,
                            numbersSp = settings.fontSizeNumbersSp,
                            harakatSp = it,
                            toolbarSp = settings.fontSizeToolbarSp
                        )
                    }
                )

                // 2. Letters Size (14sp to 40sp)
                SliderSettingRow(
                    title = "۲. اندازه حروف روی کلیدهای داخل کیبورد",
                    valueText = "${settings.fontSizeLettersSp.roundToInt()} sp",
                    value = settings.fontSizeLettersSp,
                    range = 14f..40f,
                    onValueChange = {
                        prefs.updateFontSizes(
                            lettersSp = it,
                            numbersSp = settings.fontSizeNumbersSp,
                            harakatSp = settings.fontSizeHarakatSp,
                            toolbarSp = settings.fontSizeToolbarSp
                        )
                    }
                )

                // 3. Toolbar Keys Size (10sp to 35sp)
                SliderSettingRow(
                    title = "۳. اندازه کلیدهای عملکردی بالای کیبورد (نوار ابزار)",
                    valueText = "${settings.fontSizeToolbarSp.roundToInt()} sp",
                    value = settings.fontSizeToolbarSp,
                    range = 10f..35f,
                    onValueChange = {
                        prefs.updateFontSizes(
                            lettersSp = settings.fontSizeLettersSp,
                            numbersSp = settings.fontSizeNumbersSp,
                            harakatSp = settings.fontSizeHarakatSp,
                            toolbarSp = it
                        )
                    }
                )

                // 4. Numbers Row Size (14sp to 40sp)
                SliderSettingRow(
                    title = "۴. اندازه کلیدهای اعداد بالای کیبورد",
                    valueText = "${settings.fontSizeNumbersSp.roundToInt()} sp",
                    value = settings.fontSizeNumbersSp,
                    range = 14f..40f,
                    onValueChange = {
                        prefs.updateFontSizes(
                            lettersSp = settings.fontSizeLettersSp,
                            numbersSp = it,
                            harakatSp = settings.fontSizeHarakatSp,
                            toolbarSp = settings.fontSizeToolbarSp
                        )
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Dimensions
                SliderSettingRow(
                    title = "ارتفاع کل صفحه کلید",
                    valueText = "${(settings.keyboardHeightScale * 100).roundToInt()}%",
                    value = settings.keyboardHeightScale,
                    range = 0.75f..1.4f,
                    onValueChange = {
                        prefs.updateKeyboardDimensions(it, settings.bottomOffsetDp)
                    }
                )

                SliderSettingRow(
                    title = "فاصله از پایین صفحه",
                    valueText = "${settings.bottomOffsetDp} dp",
                    value = settings.bottomOffsetDp.toFloat(),
                    range = 0f..64f,
                    onValueChange = {
                        prefs.updateKeyboardDimensions(settings.keyboardHeightScale, it.roundToInt())
                    }
                )
            }

            // Section 4: Themes (3 Dark, 3 Light, 1 Custom Palette)
            SettingsCategoryCard(
                title = "تم‌های کیبورد (۳ تم تاریک، ۳ تم روشن، ۱ تم پالت رنگی)",
                icon = Icons.Default.ColorLens
            ) {
                Text(
                    text = "۳ تم روشن:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )

                val lightThemes = listOf(KeyboardTheme.LIGHT_CLEAN, KeyboardTheme.LIGHT_SKY, KeyboardTheme.LIGHT_WARM)
                lightThemes.forEach { th ->
                    ThemeSelectionRow(
                        theme = th,
                        isSelected = settings.theme == th,
                        onSelect = { prefs.updateTheme(th) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "۳ تم تاریک:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                val darkThemes = listOf(KeyboardTheme.DARK_EPIC_AMOLED, KeyboardTheme.DARK_CYBER_INDIGO, KeyboardTheme.DARK_CHARCOAL)
                darkThemes.forEach { th ->
                    ThemeSelectionRow(
                        theme = th,
                        isSelected = settings.theme == th,
                        onSelect = { prefs.updateTheme(th) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "تم سفارشی با پالت رنگی اختصاصی:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                ThemeSelectionRow(
                    theme = KeyboardTheme.CUSTOM,
                    isSelected = settings.theme == KeyboardTheme.CUSTOM,
                    onSelect = { prefs.updateTheme(KeyboardTheme.CUSTOM) }
                )

                // Custom Color Palette Swatches when Custom Theme is selected
                if (settings.theme == KeyboardTheme.CUSTOM) {
                    CustomPaletteEditor(
                        settings = settings,
                        onApplyPalette = { bg, key, special, text, accent ->
                            prefs.updateCustomColors(bg, key, special, text, accent)
                        }
                    )
                }
            }

            // Section 5: Diacritics (Harakat)
            SettingsCategoryCard(
                title = "حرکه‌ها و اعراب بالای کیبورد",
                icon = Icons.Default.TextFields
            ) {
                Text(
                    text = "ترتیب ردیف پایین معکوس شده است: (ٍ / ٌ / ً / ْ / ّ / ِ / ُ / َ)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                SwitchSettingRow(
                    title = "ردیف بالای حرکه‌ها (ٓ ٔ ـ ٕ ٰ « » ٫)",
                    subtitle = "نمایش ردیف ۸ کلیدی بالایی حرکه‌ها",
                    checked = settings.harakatTopRowEnabled,
                    onCheckedChange = {
                        prefs.updateFeatureToggle("harakat_top_row_enabled", it) { copy(harakatTopRowEnabled = it) }
                    }
                )

                SwitchSettingRow(
                    title = "ردیف پایین حرکه‌ها (ٍ ٌ ً ْ ّ ِ ُ َ)",
                    subtitle = "نمایش ردیف ۸ کلیدی پایینی حرکه‌ها با ترتیب جدید",
                    checked = settings.harakatBottomRowEnabled,
                    onCheckedChange = {
                        prefs.updateFeatureToggle("harakat_bottom_row_enabled", it) { copy(harakatBottomRowEnabled = it) }
                    }
                )
            }

            // Section 6: Action Toolbar Keys Toggles
            SettingsCategoryCard(
                title = "کلیدهای بالای کیبورد (مدیریت تک‌به‌تک)",
                icon = Icons.Default.TouchApp
            ) {
                SwitchSettingRow(
                    title = "ردیف اعداد در بالای کیبورد (۱۲۳۴۵۶۷۸۹۰)",
                    subtitle = "دسترسی سریع به شماره‌ها",
                    checked = settings.numberRowEnabled,
                    onCheckedChange = {
                        prefs.updateFeatureToggle("number_row_enabled", it) { copy(numberRowEnabled = it) }
                    }
                )

                SwitchSettingRow(
                    title = "کلید بازگشت (Undo)",
                    subtitle = "لغو آخرین تغییرات متن در حال تایپ",
                    checked = settings.undoEnabled,
                    onCheckedChange = {
                        prefs.updateFeatureToggle("undo_enabled", it) { copy(undoEnabled = it) }
                    }
                )

                SwitchSettingRow(
                    title = "کلید کپی (Copy)",
                    subtitle = "کپی کردن متن انتخاب شده",
                    checked = settings.copyKeyEnabled,
                    onCheckedChange = {
                        prefs.updateFeatureToggle("copy_key_enabled", it) { copy(copyKeyEnabled = it) }
                    }
                )

                SwitchSettingRow(
                    title = "کلید کپی و سنجاق (Copy & Pin)",
                    subtitle = "کپی همزمان متن و سنجاق کردن دائمی در کلیپ‌بورد",
                    checked = settings.copyPinKeyEnabled,
                    onCheckedChange = {
                        prefs.updateFeatureToggle("copy_pin_key_enabled", it) { copy(copyPinKeyEnabled = it) }
                    }
                )

                SwitchSettingRow(
                    title = "کلید جایگذاری (Paste)",
                    subtitle = "درج متن کپی شده",
                    checked = settings.pasteKeyEnabled,
                    onCheckedChange = {
                        prefs.updateFeatureToggle("paste_key_enabled", it) { copy(pasteKeyEnabled = it) }
                    }
                )

                SwitchSettingRow(
                    title = "کلید برش (Cut)",
                    subtitle = "حذف و کپی متن انتخاب شده",
                    checked = settings.cutKeyEnabled,
                    onCheckedChange = {
                        prefs.updateFeatureToggle("cut_key_enabled", it) { copy(cutKeyEnabled = it) }
                    }
                )

                SwitchSettingRow(
                    title = "کلید انتخاب همه (Select All)",
                    subtitle = "انتخاب تمام متن موجود",
                    checked = settings.selectAllKeyEnabled,
                    onCheckedChange = {
                        prefs.updateFeatureToggle("select_all_key_enabled", it) { copy(selectAllKeyEnabled = it) }
                    }
                )

                SwitchSettingRow(
                    title = "کلید حذف جلو (Del)",
                    subtitle = "پاک کردن کاراکتر بعد از مکان‌نما",
                    checked = settings.delKeyEnabled,
                    onCheckedChange = {
                        prefs.updateFeatureToggle("del_key_enabled", it) { copy(delKeyEnabled = it) }
                    }
                )

                SwitchSettingRow(
                    title = "کلیدهای جهت‌نما Home و End و HomeAll و EndAll",
                    subtitle = "جابجایی سریع مکان‌نما به اول و آخر سطر و کل متن",
                    checked = settings.homeEndKeysEnabled,
                    onCheckedChange = {
                        prefs.updateFeatureToggle("home_end_keys_enabled", it) { copy(homeEndKeysEnabled = it) }
                    }
                )

                SwitchSettingRow(
                    title = "کلید صفحه کلید ماشین‌حسابی (NumPad)",
                    subtitle = "صفحه اختصاصی اعداد با Del و Enter",
                    checked = settings.numPadKeyEnabled,
                    onCheckedChange = {
                        prefs.updateFeatureToggle("numpad_key_enabled", it) { copy(numPadKeyEnabled = it) }
                    }
                )
            }

            // Section 7: Predictions, Gboard Touch Intelligence & Personal Dictionary
            SettingsCategoryCard(
                title = "پیشنهاد هوشمند، تشخیص لمس و دیکشنری شخصی",
                icon = Icons.Default.Psychology
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isPersonalDictionaryOpen = true },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                    ),
                    border = CardDefaults.outlinedCardBorder(true)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "مدیریت دیکشنری شخصی و فایل CSV",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "افزودن، اصلاح و حذف کلمات برای هر ۶ زبان و ورود لیست CSV همراه با فایل نمونه",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                SwitchSettingRow(
                    title = "تشخیص هوشمند لمس و مدیریت انگشت بزرگ (Gboard)",
                    subtitle = "تصحیح خودکار لمس‌های لبه‌ای نزدیک به کلید صحیح و تشخیص کلید مقصود در حدفاصل بین دو کلید بر اساس احتمال کلمات",
                    checked = settings.smartTouchCorrectionEnabled,
                    onCheckedChange = {
                        prefs.updateFeatureToggle("smart_touch_correction_enabled", it) { copy(smartTouchCorrectionEnabled = it) }
                    }
                )

                SwitchSettingRow(
                    title = "نوار پیشنهاد ۴ کلمه (مشابه Gboard)",
                    subtitle = "پیش‌بینی کلمه بعدی در جملات بلند و حدس آنی با هر کلید بدون فاصله",
                    checked = settings.suggestionsEnabled,
                    onCheckedChange = {
                        prefs.updateFeatureToggle("suggestions_enabled", it) { copy(suggestionsEnabled = it) }
                    }
                )

                SwitchSettingRow(
                    title = "وارونه‌سازی پرانتزها در زبان‌های راست‌به‌چپ",
                    subtitle = "نمایش صحیح پرانتزها () و علامت‌ها در فارسی و عربی",
                    checked = settings.rtlMirrorBrackets,
                    onCheckedChange = {
                        prefs.updateFeatureToggle("rtl_mirror_brackets", it) { copy(rtlMirrorBrackets = it) }
                    }
                )
            }

            // Section 8: Vibration & Sound
            SettingsCategoryCard(
                title = "لرزش و صدای کلیدها",
                icon = Icons.Default.Vibration
            ) {
                SwitchSettingRow(
                    title = "بازخورد لمسی (Haptic Feedback)",
                    subtitle = "لرزش کوتاه هنگام فشردن کلیدها",
                    checked = settings.hapticEnabled,
                    onCheckedChange = {
                        prefs.updateHaptic(it, settings.hapticDurationMs)
                    }
                )

                if (settings.hapticEnabled) {
                    SliderSettingRow(
                        title = "شدت / مدت لرزش",
                        valueText = "${settings.hapticDurationMs} ms",
                        value = settings.hapticDurationMs.toFloat(),
                        range = 5f..60f,
                        onValueChange = {
                            prefs.updateHaptic(settings.hapticEnabled, it.toLong())
                        }
                    )
                }

                SwitchSettingRow(
                    title = "صدای فشردن کلیدها",
                    subtitle = "پخش صدای ملایم کلیک با هر لمس",
                    checked = settings.soundEnabled,
                    onCheckedChange = {
                        prefs.updateSound(it)
                    }
                )
            }

            // Section 9: Key Repeat Timing
            SettingsCategoryCard(
                title = "زمان مکث و سرعت تکرار کلیدها (Key Repeat)",
                icon = Icons.Default.TouchApp
            ) {
                Text(
                    text = "تنظیم میزان تاخیر اولیه و فاصله زمانی تکرار در هنگام نگه داشتن دکمه‌ها (مانند BackSpace):",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                SliderSettingRow(
                    title = "زمان مکث اولیه تا شروع تکرار (مکث کلید)",
                    valueText = "${settings.repeatInitialDelayMs} ms",
                    value = settings.repeatInitialDelayMs.toFloat(),
                    range = 150f..600f,
                    onValueChange = {
                        prefs.updateKeyRepeat(
                            initialDelayMs = it.toLong(),
                            intervalMs = settings.repeatIntervalMs
                        )
                    }
                )

                SliderSettingRow(
                    title = "فاصله زمانی تکرار کلیدها (سرعت تکرار)",
                    valueText = "${settings.repeatIntervalMs} ms",
                    value = settings.repeatIntervalMs.toFloat(),
                    range = 25f..100f,
                    onValueChange = {
                        prefs.updateKeyRepeat(
                            initialDelayMs = settings.repeatInitialDelayMs,
                            intervalMs = it.toLong()
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeSelectionRow(
    theme: KeyboardTheme,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder(isSelected)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color preview dots
                Row(modifier = Modifier.padding(end = 10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(theme.defaultBg))
                            .border(1.dp, Color.Gray, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(theme.defaultKeyBg))
                            .border(1.dp, Color.Gray, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(theme.defaultAccent))
                    )
                }

                Column {
                    Text(
                        text = theme.displayNamePersian,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = theme.displayNameEnglish,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CustomPaletteEditor(
    settings: KeyboardSettingsState,
    onApplyPalette: (bg: Long, key: Long, special: Long, text: Long, accent: Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Text(
            text = "پالت‌های رنگی آماده برای انتخاب سریع:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        val palettes = listOf(
            "نئون سایبر" to listOf(0xFF0D0F18L, 0xFF171B26L, 0xFF232A3BL, 0xFFE2E8F0L, 0xFF38BDF8L),
            "زمردی سلطنتی" to listOf(0xFF06281EL, 0xFF0B3B2EL, 0xFF145242L, 0xFFECFDF5L, 0xFF10B981L),
            "زرشکی تیره" to listOf(0xFF200914L, 0xFF3B1225L, 0xFF541C36L, 0xFFFFF1F2L, 0xFFF43F5EL),
            "طلایی مشکی" to listOf(0xFF121212L, 0xFF1F1F1FL, 0xFF2E2E2EL, 0xFFFEF3C7L, 0xFFF59E0BL),
            "رزگلد پاستلی" to listOf(0xFFFAF5F0L, 0xFFFFFFFFL, 0xFFF3E8E2L, 0xFF4A342FL, 0xFFD97706L),
            "بنفش کهکشانی" to listOf(0xFF130924L, 0xFF241442L, 0xFF35205EL, 0xFFF5F3FFL, 0xFFA855F7L)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            palettes.forEach { (name, colors) ->
                OutlinedCard(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onApplyPalette(colors[0], colors[1], colors[2], colors[3], colors[4])
                        },
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(colors[0]))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(colors[1])))
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(colors[4])))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = name, fontSize = 10.sp, color = Color(colors[3]), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCategoryCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            content()
        }
    }
}

@Composable
private fun SwitchSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SliderSettingRow(
    title: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(
                text = valueText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
