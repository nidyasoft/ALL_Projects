package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.playground.PlaygroundScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    ONBOARDING,
    PLAYGROUND,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs = SmartKeyboardApp.instance.preferences
            val settings by prefs.settingsState.collectAsStateWithLifecycle()
            val useDark = settings.darkTheme ?: isSystemInDarkTheme()

            MyApplicationTheme(darkTheme = useDark) {
                // If onboarding not completed, start with ONBOARDING screen
                val initialScreen = remember {
                    if (prefs.isOnboardingCompleted()) AppScreen.PLAYGROUND else AppScreen.ONBOARDING
                }
                var currentScreen by remember { mutableStateOf(initialScreen) }

                BackHandler(enabled = currentScreen != AppScreen.PLAYGROUND) {
                    currentScreen = AppScreen.PLAYGROUND
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        AppScreen.ONBOARDING -> {
                            OnboardingScreen(
                                settings = settings,
                                onFinish = { currentScreen = AppScreen.PLAYGROUND },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.PLAYGROUND -> {
                            PlaygroundScreen(
                                settings = settings,
                                onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                                onOpenOnboarding = { currentScreen = AppScreen.ONBOARDING },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.SETTINGS -> {
                            SettingsScreen(
                                settings = settings,
                                onNavigateBack = { currentScreen = AppScreen.PLAYGROUND },
                                onOpenOnboarding = { currentScreen = AppScreen.ONBOARDING },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
