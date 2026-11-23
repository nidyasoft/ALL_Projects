package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.data.db.AppDatabase
import com.example.data.repository.MusicRepository
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.MusicCutterScreen
import com.example.ui.screens.AdvancedFeaturesScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var repository: MusicRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request Notification and Phone State Permissions
        val permissionsToRequest = mutableListOf<String>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsToRequest.add(android.Manifest.permission.READ_PHONE_STATE)
        requestPermissions(permissionsToRequest.toTypedArray(), 102)

        // Initialize local SQLite Room Database
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "epic_music_player.db"
        ).fallbackToDestructiveMigration().build()

        repository = MusicRepository(applicationContext, database.musicDao)

        setContent {
            // Instantiate the ViewModel bound to standard architecture lifecycle to prevent duplication/leaks
            val musicViewModel: MusicViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return MusicViewModel(applicationContext, repository) as T
                    }
                }
            )

            // Sync structural visual dynamic themes index directly from local database settings Flow
            val activeThemeIdx by musicViewModel.appThemeIndex.collectAsState()
            val activeSubThemeIdx by musicViewModel.subThemeIndex.collectAsState()
            val isDarkModeOverride by musicViewModel.isDarkModeOverride.collectAsState()
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDarkTheme = isDarkModeOverride ?: isSystemDark

            MyApplicationTheme(
                themeIndex = activeThemeIdx, 
                subThemeIndex = activeSubThemeIdx,
                darkTheme = isDarkTheme
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 900.dp)
                        ) {
                            AppNavigationController(
                                viewModel = musicViewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MusicViewModel.instance?.markAppActive()
    }
}

@Composable
fun AppNavigationController(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    // Elegant, zero-latency state nav mapping: "splash", "home", "player", "equalizer"
    var currentScreen by remember { mutableStateOf("splash") }

    val songsScrollState = rememberLazyListState()
    val favoritesScrollState = rememberLazyListState()
    val playlistsScrollState = rememberLazyListState()
    val albumsScrollState = rememberLazyListState()
    val artistsScrollState = rememberLazyListState()
    val genresScrollState = rememberLazyListState()
    val foldersScrollState = rememberLazyListState()
    val hiddenScrollState = rememberLazyListState()

    var lastSelectedTabIdx by rememberSaveable { mutableStateOf(0) }
    var cutterReturnScreen by rememberSaveable { mutableStateOf("player") }

    val currentSongIndex by viewModel.currentSongIndex.collectAsState()
    val currentQueue by viewModel.currentQueue.collectAsState()
    var hasAutoNavigated by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(currentQueue, currentSongIndex, currentScreen) {
        if (currentScreen != "splash" && !hasAutoNavigated && currentQueue.isNotEmpty() && currentSongIndex >= 0) {
            hasAutoNavigated = true
            currentScreen = "player"
        }
    }

    // Multi-select cancel state on back handling
    val multiSelectMode by viewModel.multiSelectMode.collectAsState()

    // Handle System Hardware physical Back key actions
    BackHandler {
        if (multiSelectMode) {
            viewModel.toggleMultiSelect()
        } else {
            when (currentScreen) {
                "equalizer" -> currentScreen = "player"
                "music_cutter" -> currentScreen = cutterReturnScreen
                "advanced_features" -> currentScreen = "player"
                "player" -> currentScreen = "home"
                else -> {
                    // Quit activity or standard system callback
                }
            }
        }
    }

    // Modern animation transition between view hierarchies
    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (initialState == "splash") {
                fadeIn() togetherWith fadeOut()
            } else if (targetState == "equalizer") {
                slideInHorizontally { w -> w } + fadeIn() togetherWith
                        slideOutHorizontally { w -> -w } + fadeOut()
            } else if (initialState == "equalizer" && targetState == "player") {
                slideInHorizontally { w -> -w } + fadeIn() togetherWith
                        slideOutHorizontally { w -> w } + fadeOut()
            } else if (targetState == "player" && initialState == "home") {
                (slideInVertically(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { h -> h } + scaleIn(initialScale = 0.90f) + fadeIn()) togetherWith 
                        (fadeOut() + scaleOut(targetScale = 0.90f))
            } else if (targetState == "home" && initialState == "player") {
                (fadeIn() + scaleIn(initialScale = 0.90f)) togetherWith 
                        (slideOutVertically(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { h -> h } + scaleOut(targetScale = 0.90f) + fadeOut())
            } else {
                slideInHorizontally { w -> w } + fadeIn() togetherWith
                        slideOutHorizontally { w -> -w } + fadeOut()
            }.using(
                SizeTransform(clip = false)
            )
        },
        label = "ScreenNavigation",
        modifier = modifier.fillMaxSize()
    ) { screen ->
        when (screen) {
            "splash" -> {
                SplashScreen(
                    onTimeout = {
                        currentScreen = if (!hasAutoNavigated && currentQueue.isNotEmpty() && currentSongIndex >= 0) {
                            hasAutoNavigated = true
                            "player"
                        } else {
                            "home"
                        }
                    }
                )
            }
            "home" -> {
                MainScreen(
                    viewModel = viewModel,
                    onNavigateToPlayer = { currentScreen = "player" },
                    onNavigateToEqualizer = { currentScreen = "equalizer" },
                    onNavigateToCutter = {
                        cutterReturnScreen = "home"
                        currentScreen = "music_cutter"
                    },
                    songsScrollState = songsScrollState,
                    favoritesScrollState = favoritesScrollState,
                    playlistsScrollState = playlistsScrollState,
                    albumsScrollState = albumsScrollState,
                    artistsScrollState = artistsScrollState,
                    genresScrollState = genresScrollState,
                    foldersScrollState = foldersScrollState,
                    hiddenScrollState = hiddenScrollState,
                    initialTabIdx = lastSelectedTabIdx,
                    onTabChanged = { lastSelectedTabIdx = it }
                )
            }
            "player" -> {
                PlayerScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "home" },
                    onNavigateToEqualizer = { currentScreen = "equalizer" },
                    onNavigateToCutter = {
                        cutterReturnScreen = "player"
                        currentScreen = "music_cutter"
                    },
                    onNavigateToAdvancedFeatures = { currentScreen = "advanced_features" }
                )
            }
            "music_cutter" -> {
                MusicCutterScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = cutterReturnScreen }
                )
            }
            "equalizer" -> {
                EqualizerScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "player" }
                )
            }
            "advanced_features" -> {
                AdvancedFeaturesScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "player" }
                )
            }
        }
    }
}
