package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ui.onboarding.OnboardingScreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import androidx.compose.material.icons.filled.Settings
import com.example.ui.alarm.ActiveAlarmOverlay
import com.example.ui.alarm.AlarmScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.stopwatch.StopwatchScreen
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.EpicTheme
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
import com.example.ui.timer.TimerScreen
import com.example.ui.worldclock.WorldClockScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val firedAlarm by viewModel.activeFiredAlarm.collectAsState()
    val themeMode by viewModel.currentThemeMode.collectAsState()
    val isThemeDialogOpen by viewModel.isThemeDialogOpen.collectAsState()
    val theme = EpicTheme.colors

    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var showOnboardingFromSettings by rememberSaveable { mutableStateOf(false) }
    val isCalendarScreenOpen by viewModel.isCalendarScreenOpen.collectAsState()

    Scaffold(
        containerColor = theme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Logo and App Name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.openThemeDialog() }
                                .padding(4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "App Logo",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, theme.border, RoundedCornerShape(8.dp))
                            )
                            Text(
                                text = Strings.get("app_name", language),
                                color = theme.accentCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        // Right action: Open dedicated Settings Screen
                        IconButton(
                            onClick = { isSettingsOpen = true },
                            modifier = Modifier
                                .testTag("top_bar_settings_button")
                                .padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = Strings.get("tab_settings", language),
                                tint = theme.accentCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = theme.surface,
                    titleContentColor = theme.accentCyan
                )
            )
        },
        bottomBar = {
            val navBg = theme.surface
            val navBorder = theme.border
            val selectedIconTint = if (theme.isDark) Color(0xFF05070B) else Color.White

            NavigationBar(
                containerColor = navBg,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(1.dp, navBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                // Tab 1: Alarm
                NavigationBarItem(
                    selected = currentTab == NavigationTab.ALARM,
                    onClick = { viewModel.setTab(NavigationTab.ALARM) },
                    icon = {
                        Icon(imageVector = Icons.Default.Alarm, contentDescription = Strings.get("tab_alarm", language))
                    },
                    label = {
                        Text(text = Strings.get("tab_alarm", language), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedIconTint,
                        selectedTextColor = theme.accentCyan,
                        indicatorColor = theme.accentCyan,
                        unselectedIconColor = theme.mutedText,
                        unselectedTextColor = theme.mutedText
                    ),
                    modifier = Modifier.testTag("tab_alarm")
                )

                // Tab 2: World Clock
                NavigationBarItem(
                    selected = currentTab == NavigationTab.WORLD_CLOCK,
                    onClick = { viewModel.setTab(NavigationTab.WORLD_CLOCK) },
                    icon = {
                        Icon(imageVector = Icons.Default.Public, contentDescription = Strings.get("tab_world_clock", language))
                    },
                    label = {
                        Text(text = Strings.get("tab_world_clock", language), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedIconTint,
                        selectedTextColor = theme.accentCyan,
                        indicatorColor = theme.accentCyan,
                        unselectedIconColor = theme.mutedText,
                        unselectedTextColor = theme.mutedText
                    ),
                    modifier = Modifier.testTag("tab_world_clock")
                )

                // Tab 3: Stopwatch
                NavigationBarItem(
                    selected = currentTab == NavigationTab.STOPWATCH,
                    onClick = { viewModel.setTab(NavigationTab.STOPWATCH) },
                    icon = {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = Strings.get("tab_stopwatch", language))
                    },
                    label = {
                        Text(text = Strings.get("tab_stopwatch", language), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedIconTint,
                        selectedTextColor = theme.accentPurple,
                        indicatorColor = theme.accentPurple,
                        unselectedIconColor = theme.mutedText,
                        unselectedTextColor = theme.mutedText
                    ),
                    modifier = Modifier.testTag("tab_stopwatch")
                )

                // Tab 4: Timer
                NavigationBarItem(
                    selected = currentTab == NavigationTab.TIMER,
                    onClick = { viewModel.setTab(NavigationTab.TIMER) },
                    icon = {
                        Icon(imageVector = Icons.Default.HourglassTop, contentDescription = Strings.get("tab_timer", language))
                    },
                    label = {
                        Text(text = Strings.get("tab_timer", language), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedIconTint,
                        selectedTextColor = theme.accentAmber,
                        indicatorColor = theme.accentAmber,
                        unselectedIconColor = theme.mutedText,
                        unselectedTextColor = theme.mutedText
                    ),
                    modifier = Modifier.testTag("tab_timer")
                )
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentTab,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "tab_crossfade"
        ) { tab ->
            when (tab) {
                NavigationTab.ALARM -> AlarmScreen(viewModel = viewModel)
                NavigationTab.WORLD_CLOCK -> WorldClockScreen(viewModel = viewModel)
                NavigationTab.STOPWATCH -> StopwatchScreen(viewModel = viewModel)
                NavigationTab.TIMER -> TimerScreen(viewModel = viewModel)
                NavigationTab.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onOpenOnboarding = { showOnboardingFromSettings = true }
                )
            }
        }
    }

    // Dedicated Full-Screen Settings View (accessed via gear button in TopAppBar)
    if (isSettingsOpen) {
        BackHandler {
            isSettingsOpen = false
        }
        Scaffold(
            containerColor = theme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "تنظیمات اپلیکیشن",
                            color = theme.accentCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { isSettingsOpen = false },
                            modifier = Modifier.testTag("settings_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت",
                                tint = theme.accentCyan
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = theme.surface,
                        titleContentColor = theme.accentCyan
                    )
                )
            }
        ) { innerPad ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPad)
            ) {
                SettingsScreen(
                    viewModel = viewModel,
                    onOpenOnboarding = { showOnboardingFromSettings = true }
                )
            }
        }
    }

    // Dedicated Full-Screen Calendar, Meetings & Events Screen
    if (isCalendarScreenOpen) {
        BackHandler {
            viewModel.closeCalendarScreen()
        }
        com.example.ui.calendar.CalendarEventsScreen(
            viewModel = viewModel,
            onBack = { viewModel.closeCalendarScreen() }
        )
    }

    // Onboarding Wizard re-opened from Settings
    if (showOnboardingFromSettings) {
        BackHandler {
            showOnboardingFromSettings = false
        }
        OnboardingScreen(
            onFinish = { showOnboardingFromSettings = false }
        )
    }

    // Theme Selection Dialog
    if (isThemeDialogOpen) {
        ThemeSelectionDialog(
            currentTheme = themeMode,
            language = language,
            onSelectTheme = { selectedMode ->
                viewModel.setThemeMode(selectedMode)
            },
            onDismiss = { viewModel.closeThemeDialog() }
        )
    }

    // Active Waking Overlay when alarm/timer is ringing
    firedAlarm?.let { alarm ->
        ActiveAlarmOverlay(
            firedAlarm = alarm,
            language = language,
            onDismiss = { viewModel.dismissFiredAlarm() },
            onSnooze = { minutes -> viewModel.snoozeFiredAlarm(minutes) }
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: AppThemeMode,
    language: AppLanguage,
    onSelectTheme: (AppThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val theme = EpicTheme.colors

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.surface,
        titleContentColor = theme.primaryText,
        textContentColor = theme.secondaryText,
        modifier = Modifier.testTag("theme_selection_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, theme.border, RoundedCornerShape(10.dp))
                )
                Column {
                    Text(
                        text = Strings.get("theme_dialog_title", language),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryText
                    )
                    Text(
                        text = Strings.get("app_name", language),
                        fontSize = 12.sp,
                        color = theme.mutedText
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Option 1: Dark (تاریک)
                ThemeOptionItem(
                    title = Strings.get("theme_dark", language),
                    description = Strings.get("theme_dark_desc", language),
                    icon = Icons.Default.DarkMode,
                    iconColor = NeonCyan,
                    isSelected = currentTheme == AppThemeMode.DARK,
                    onClick = { onSelectTheme(AppThemeMode.DARK) }
                )

                // Option 2: Light (روشن)
                ThemeOptionItem(
                    title = Strings.get("theme_light", language),
                    description = Strings.get("theme_light_desc", language),
                    icon = Icons.Default.LightMode,
                    iconColor = NeonAmber,
                    isSelected = currentTheme == AppThemeMode.LIGHT,
                    onClick = { onSelectTheme(AppThemeMode.LIGHT) }
                )

                // Option 3: Follow Android System Settings (مطابق تنظیمات دستگاه اندرویدی)
                ThemeOptionItem(
                    title = Strings.get("theme_system", language),
                    description = Strings.get("theme_system_desc", language),
                    icon = Icons.Default.BrightnessAuto,
                    iconColor = NeonPurple,
                    isSelected = currentTheme == AppThemeMode.SYSTEM,
                    onClick = { onSelectTheme(AppThemeMode.SYSTEM) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_theme_dialog_button")
            ) {
                Text(
                    text = Strings.get("theme_close", language),
                    color = theme.accentCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    )
}

@Composable
fun ThemeOptionItem(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val theme = EpicTheme.colors
    val backgroundColor = if (isSelected) {
        if (theme.isDark) theme.surfaceVariant else theme.surfaceElevated
    } else {
        theme.surface
    }
    val borderColor = if (isSelected) iconColor else theme.border

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = if (isSelected) 0.25f else 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = theme.primaryText,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                )
                Text(
                    text = description,
                    color = theme.secondaryText,
                    fontSize = 11.sp,
                    maxLines = 2
                )
            }
        }

        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = iconColor,
                unselectedColor = theme.mutedText
            )
        )
    }
}
