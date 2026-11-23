package com.example.ui.screens

import com.example.ui.components.FastScrollbar
import com.example.ui.components.FastScrollbarForScrollState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.utils.LanguageUtils
import com.example.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.launch
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectTapGestures
import com.example.ui.components.bounceClick
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToCutter: () -> Unit,
    onNavigateToAdvancedFeatures: () -> Unit
) {
    val lang by viewModel.selectedLanguage.collectAsState()
    val songs by viewModel.songs.collectAsState()
    val queue by viewModel.currentQueue.collectAsState()
    val index by viewModel.currentSongIndex.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.playbackProgress.collectAsState()
    val speed by viewModel.playbackSpeed.collectAsState()
    val crossfadeEnabled by viewModel.crossfadeEnabled.collectAsState()
    val crossfadeDuration by viewModel.crossfadeDuration.collectAsState()
    val shuffleEnabled by viewModel.shuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val sleepTimerMode by viewModel.sleepTimerMode.collectAsState()
    val sleepTimeLeftMs by viewModel.sleepTimeLeftMs.collectAsState()
    val biometricSleepEnabled by viewModel.biometricSleepEnabled.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val headsetDisconnectPause by viewModel.headsetDisconnectPause.collectAsState()
    val activePlaylistName by viewModel.activePlaylistName.collectAsState()
    val activeThemeIdx by viewModel.appThemeIndex.collectAsState()
    val activeSubThemeIdx by viewModel.subThemeIndex.collectAsState()

    val currentSong = queue.getOrNull(index)
    val context = LocalContext.current

    // Modals control states
    var showQueueSheet by remember { mutableStateOf(false) }
    var showLyricsDialog by remember { mutableStateOf(false) }
    var showLyricsEditMode by remember { mutableStateOf(false) }
    var lyricsEditText by remember { mutableStateOf("") }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSleepTimerMenu by remember { mutableStateOf(false) }

    // Dropdown for append to playlist
    var showPlaylistSelectorForAdd by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Device metrics and clock state initialization (Requirement 1)
    val persian_digits = remember { charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹') }
    fun toPersianNumber(num: String): String {
        return num.map { c ->
            if (c in '0'..'9') persian_digits[c - '0'] else c
        }.joinToString("")
    }

    var batteryPct by remember { mutableStateOf(100) }
    
    androidx.compose.runtime.DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
                val level = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
                if (level != -1 && scale != -1) {
                    batteryPct = (level * 100 / scale.toFloat()).toInt()
                }
            }
        }
        val intentFilter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
        try {
            context.registerReceiver(receiver, intentFilter)
        } catch (e: Exception) {
            android.util.Log.e("PlayerScreen", "Error registering battery receiver: ${e.message}")
        }
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    var currentTimeText by remember { mutableStateOf("") }
    var currentShamsiDateText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val calendar = java.util.Calendar.getInstance()
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = calendar.get(java.util.Calendar.MINUTE)
            val second = calendar.get(java.util.Calendar.SECOND)
            
            val timeString = String.format("%02d:%02d:%02d", hour, minute, second)
            currentTimeText = toPersianNumber(timeString)
            
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH) + 1
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
            val weekdayStr = when (dayOfWeek) {
                java.util.Calendar.SATURDAY -> "شنبه"
                java.util.Calendar.SUNDAY -> "یکشنبه"
                java.util.Calendar.MONDAY -> "دوشنبه"
                java.util.Calendar.TUESDAY -> "سه‌شنبه"
                java.util.Calendar.WEDNESDAY -> "چهارشنبه"
                java.util.Calendar.THURSDAY -> "پنجشنبه"
                java.util.Calendar.FRIDAY -> "جمعه"
                else -> ""
            }
            val shamsiDate = getSolarHijriDate(year, month, day)
            currentShamsiDateText = if (weekdayStr.isNotEmpty()) "$weekdayStr - $shamsiDate" else shamsiDate
            
            delay(1000L)
        }
    }

    if (currentSong == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = LanguageUtils.get("no_songs_found", lang),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        return
    }

    ScaffoldWithKeepScreenOn(keepScreenOn = keepScreenOn) {
        var totalDragAmount by remember { mutableStateOf(0f) }
        val backgroundBg = MaterialTheme.colorScheme.background
        val isLightBg = (backgroundBg.red * 0.299f + backgroundBg.green * 0.587f + backgroundBg.blue * 0.114f) > 0.5f
        val topBarColor = if (isLightBg) Color.Black else Color.White
        val topBarColorSecondary = if (isLightBg) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.8f)

        Box(modifier = Modifier.fillMaxSize()) {
            com.example.ui.components.FluidBackground(themeIndex = activeThemeIdx, subThemeIndex = activeSubThemeIdx, isPlaying = isPlaying)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // Elegant Shamsi Date, Time, Battery, and Keep Screen On Bar (Requirement 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Battery State Indicator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(44.dp)
                ) {
                    Canvas(modifier = Modifier.size(36.dp)) {
                        drawArc(
                            color = topBarColor.copy(alpha = 0.15f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
                        )
                        drawArc(
                            color = if (batteryPct > 20) Color(0xFF4CAF50) else Color(0xFFF44336),
                            startAngle = -90f,
                            sweepAngle = (batteryPct * 3.6f),
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }
                    val batteryText = batteryPct.toString().map { c ->
                        if (c in '0'..'9') persian_digits[c - '0'] else c
                    }.joinToString("") + "٪"
                    Text(
                        text = batteryText,
                        color = topBarColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Clock and Solar Hijri Date Center Panel
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentTimeText,
                        color = topBarColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentShamsiDateText,
                        color = topBarColorSecondary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Keep Alive Toggle Spark Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (keepScreenOn) Color(0xFFFFEB3B).copy(alpha = 0.35f) else topBarColor.copy(alpha = 0.1f))
                        .clickable {
                            viewModel.updateKeepScreenOn(!keepScreenOn)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(18.dp)) {
                        val color = if (keepScreenOn) Color(0xFFFFEB3B) else topBarColor.copy(alpha = 0.6f)
                        if (keepScreenOn) {
                            drawCircle(color = color, radius = 4.5.dp.toPx())
                            for (i in 0 until 8) {
                                val angle = i * (Math.PI / 4)
                                val startX = (center.x + Math.cos(angle) * 5.5.dp.toPx()).toFloat()
                                val startY = (center.y + Math.sin(angle) * 5.5.dp.toPx()).toFloat()
                                val endX = (center.x + Math.cos(angle) * 8.5.dp.toPx()).toFloat()
                                val endY = (center.y + Math.sin(angle) * 8.5.dp.toPx()).toFloat()
                                drawLine(
                                    color = color,
                                    start = androidx.compose.ui.geometry.Offset(startX, startY),
                                    end = androidx.compose.ui.geometry.Offset(endX, endY),
                                    strokeWidth = 1.25.dp.toPx(),
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            }
                        } else {
                            val path = androidx.compose.ui.graphics.Path().apply {
                                val r = 6.5.dp.toPx()
                                addArc(
                                    oval = androidx.compose.ui.geometry.Rect(center.x - r, center.y - r, center.x + r, center.y + r),
                                    startAngleDegrees = -90f,
                                    sweepAngleDegrees = 180f
                                )
                                quadraticTo(
                                    x1 = center.x - 2.5.dp.toPx(),
                                    y1 = center.y,
                                    x2 = center.x,
                                    y2 = center.y - r
                                )
                            }
                            drawPath(path = path, color = color)
                        }
                    }
                }
            }

            // Player Top Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = LanguageUtils.get("app_title", lang),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Row {
                    // Advanced AI Features Shortcut
                    IconButton(onClick = onNavigateToAdvancedFeatures) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Smart Features",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Equalizer Shortcut
                    IconButton(onClick = onNavigateToEqualizer) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Equalizer",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Sleep Timer Quick Selection
                    Box {
                        IconButton(onClick = { showSleepTimerMenu = !showSleepTimerMenu }) {
                            Icon(
                                imageVector = Icons.Default.Snooze,
                                contentDescription = "Sleep",
                                tint = if (sleepTimerMode != -1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }

                        DropdownMenu(
                            expanded = showSleepTimerMenu,
                            onDismissRequest = { showSleepTimerMenu = false }
                        ) {
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.Close, contentDescription = null) },
                                text = { Text(LanguageUtils.get("sleep_timer_none", lang)) },
                                onClick = { viewModel.setSleepTimer(-1); viewModel.toggleBiometricSleep(context, false); showSleepTimerMenu = false }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.Snooze, contentDescription = null) },
                                text = { Text(LanguageUtils.get("sleep_timer_end_song", lang)) },
                                onClick = { viewModel.setSleepTimer(0); viewModel.toggleBiometricSleep(context, false); showSleepTimerMenu = false }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.Snooze, contentDescription = null) },
                                text = { Text(if (lang == "fa") "تایمر خواب بیومتریک هوشمند" else "Smart Biometric Sleep Timer") },
                                onClick = { 
                                    viewModel.setSleepTimer(-1)
                                    viewModel.toggleBiometricSleep(context, !biometricSleepEnabled)
                                    showSleepTimerMenu = false 
                                },
                                trailingIcon = {
                                    if (biometricSleepEnabled) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            )
                            listOf(10, 15, 30, 45, 60).forEach { mins ->
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.Snooze, contentDescription = null) },
                                    text = { Text(String.format(LanguageUtils.get("sleep_timer_mins", lang), mins)) },
                                    onClick = { viewModel.setSleepTimer(mins); viewModel.toggleBiometricSleep(context, false); showSleepTimerMenu = false }
                                )
                            }
                        }
                    }

                    // Settings shortcut
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sleep timer counting badge
            if (sleepTimerMode != -1 || biometricSleepEnabled) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    val countText = if (biometricSleepEnabled) {
                        if (lang == "fa") "خواب بیومتریک فعال" else "Bio Sleep Active"
                    } else if (sleepTimerMode == 0) {
                        LanguageUtils.get("sleep_timer_end_song", lang)
                    } else {
                        val totalSecs = sleepTimeLeftMs / 1000
                        val m = totalSecs / 60
                        val s = totalSecs % 60
                        String.format("%02d:%02d", m, s)
                    }
                    Text(
                        text = "⏱ $countText",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Beautiful Cover Album Artwork Card with layered shadows and gradients
            val dragOffset = remember { Animatable(0f) }
            var showHeartReaction by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { totalDragAmount = 0f },
                            onDragEnd = {
                                val swapDistance = 150f
                                if (totalDragAmount > swapDistance) {
                                    viewModel.prevSong()
                                } else if (totalDragAmount < -swapDistance) {
                                    viewModel.nextSong()
                                }
                                coroutineScope.launch {
                                    dragOffset.animateTo(
                                        0f,
                                        spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    dragOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                totalDragAmount += dragAmount
                                coroutineScope.launch {
                                    dragOffset.snapTo(totalDragAmount * 0.45f)
                                }
                            }
                        )
                    }
                    .pointerInput(currentSong.path) {
                        detectTapGestures(
                            onDoubleTap = {
                                viewModel.toggleFavorite(currentSong)
                                showHeartReaction = true
                            }
                        )
                    }
                    .graphicsLayer {
                        translationX = dragOffset.value
                        rotationZ = (dragOffset.value / 40f)
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (activeThemeIdx == 6) {
                    // Template 7: Vintage Gramophone Record & Tonearm ("صفحه گرامافون قدیمی")
                    com.example.ui.components.VintageGramophoneVisualizer(
                        isPlaying = isPlaying,
                        modifier = Modifier
                            .size(310.dp)
                            .clickable { viewModel.togglePlayPause() }
                    )
                } else {
                    // Wave/particle high fidelity canvas visualizer
                    com.example.ui.components.AdvancedAudioVisualizer(
                        isPlaying = isPlaying,
                        modifier = Modifier.size(340.dp)
                    )

                    // Stack layer for gorgeous look
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .offset(y = 10.dp)
                            .shadow(elevation = 16.dp, shape = RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    )
                    // Actual artwork visual card
                    Card(
                        shape = RoundedCornerShape(26.dp),
                        modifier = Modifier
                            .size(270.dp)
                            .clickable { viewModel.togglePlayPause() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val customCover = currentSong.customCover
                            if (customCover != null && !customCover.startsWith("gradient_")) {
                            // Render with AsyncImage
                            val context = LocalContext.current
                            val model = remember(customCover) {
                                if (customCover.startsWith("/")) {
                                    java.io.File(customCover)
                                } else {
                                    val resId = context.resources.getIdentifier(customCover, "drawable", context.packageName)
                                    if (resId != 0) resId else customCover
                                }
                            }
                            coil.compose.AsyncImage(
                                model = model,
                                contentDescription = "Album Cover",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_media_play),
                                placeholder = androidx.compose.ui.res.painterResource(android.R.drawable.ic_media_play)
                            )
                        } else if (customCover == null && !currentSong.path.startsWith("PRELOADED_") && currentSong.path.startsWith("/")) {
                            // Extract embedded picture if local file
                            val context = LocalContext.current
                            val model = remember(currentSong.path) {
                                try {
                                    val retriever = android.media.MediaMetadataRetriever()
                                    retriever.setDataSource(currentSong.path)
                                    val picture = retriever.embeddedPicture
                                    retriever.release()
                                    picture
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            if (model != null) {
                                coil.compose.AsyncImage(
                                    model = model,
                                    contentDescription = "Album Cover",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_media_play),
                                    placeholder = androidx.compose.ui.res.painterResource(android.R.drawable.ic_media_play)
                                )
                            } else {
                                // Draw default gradient fallback
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .drawBehind {
                                            val brush = Brush.verticalGradient(
                                                colors = listOf(Color(0xFFFF5722), Color(0xFF2E3137))
                                            )
                                            drawRect(brush)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.25f),
                                        modifier = Modifier.size(120.dp)
                                    )
                                }
                            }
                        } else {
                            // Fallback to gradient or simple placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .drawBehind {
                                        val brush = when (customCover) {
                                            "gradient_starlight" -> Brush.radialGradient(
                                                colors = listOf(Color(0xFF8E24AA), Color(0xFF0F0B1E))
                                            )
                                            "gradient_deepwater" -> Brush.radialGradient(
                                                colors = listOf(Color(0xFF00ACC1), Color(0xFF003040))
                                            )
                                            "gradient_technogrid" -> Brush.radialGradient(
                                                colors = listOf(Color(0xFFFF1744), Color(0xFF1B0000))
                                            )
                                            "gradient_persianrose" -> Brush.radialGradient(
                                                colors = listOf(Color(0xFFFF4081), Color(0xFF4A148C))
                                            )
                                            "gradient_azeribreeze" -> Brush.radialGradient(
                                                colors = listOf(Color(0xFFD4AF37), Color(0xFF112233))
                                            )
                                            else -> Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0xFFFF5722),
                                                    Color(0xFF2E3137)
                                                )
                                            )
                                        }
                                        drawRect(brush)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                // Ambient musical emblem
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.size(120.dp)
                                )
                            }
                        }

                        // Info text overlay at bottom
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(Color.Black.copy(alpha = 0.4f))
                                .align(Alignment.BottomCenter),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentSong.genre,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Floating heart reaction overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = showHeartReaction,
                    enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = scaleOut(animationSpec = tween(400)) + fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    LaunchedEffect(showHeartReaction) {
                        if (showHeartReaction) {
                            delay(1000)
                            showHeartReaction = false
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Liked",
                        tint = Color.Red,
                        modifier = Modifier
                            .size(100.dp)
                            .shadow(elevation = 20.dp, shape = CircleShape)
                            .background(Color.White.copy(alpha = 0.9f), shape = CircleShape)
                            .padding(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Music Titles metadata with scrollable/fade properties
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                com.example.ui.components.CharacterRevealText(
                    text = currentSong.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    key = currentSong.path
                )

                Spacer(modifier = Modifier.height(4.dp))

                com.example.ui.components.CharacterRevealText(
                    text = currentSong.artist,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    key = currentSong.path
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Audio Progress Seek and timer tags
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = progress.toFloat(),
                    onValueChange = { viewModel.seekTo(it.toLong()) },
                    valueRange = 0f..currentSong.duration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMills(progress),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatMills(currentSong.duration),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Functional Extra Action Buttons: Favorites, Playlists, Lyrics shortcut
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lyrics button
                IconButton(onClick = {
                    lyricsEditText = currentSong.lyrics ?: ""
                    showLyricsDialog = true
                }) {
                    Icon(
                        imageVector = Icons.Default.FormatAlignLeft,
                        contentDescription = "Lyrics",
                        tint = if (currentSong.lyrics != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }

                // Add to playlist
                IconButton(onClick = { showPlaylistSelectorForAdd = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add to Playlist"
                    )
                }

                // Favorite
                IconButton(onClick = { viewModel.toggleFavorite(currentSong) }) {
                    Icon(
                        imageVector = if (currentSong.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (currentSong.isFavorite) Color.Red else MaterialTheme.colorScheme.onBackground
                    )
                }

                // Share button
                IconButton(onClick = {
                    com.example.ui.utils.FileShareUtils.shareSongs(context, listOf(currentSong))
                }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Active queue sheet launcher
                IconButton(onClick = { showQueueSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Queue Music"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Media Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Toggler
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .bounceClick { viewModel.toggleShuffle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // Skip Left/Previous
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .bounceClick { viewModel.prevSong() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Prev",
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play / Pause Circle Action Button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .bounceClick { viewModel.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "PlayPause",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(42.dp)
                    )
                }

                // Skip Right/Next
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .bounceClick { viewModel.nextSong() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat cycling
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .bounceClick { viewModel.cycleRepeatMode() },
                    contentAlignment = Alignment.Center
                ) {
                    val repeatIcon = when (repeatMode) {
                        1 -> Icons.Default.Repeat
                        2 -> Icons.Default.RepeatOne
                        else -> Icons.Default.Loop
                    }
                    val iconTint = if (repeatMode > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repeat",
                        tint = iconTint
                    )
                }
            }
        }

        // --- Bottom Sheet for Active Playing Queue ---
        if (showQueueSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showQueueSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                        .padding(bottom = 24.dp)
                ) {
                    val sheetTitle = if (activePlaylistName != null) {
                        "Playlist: $activePlaylistName (لیست پخش: $activePlaylistName)"
                    } else {
                        "Music Queue (آهنگ‌های در حال پخش)"
                    }
                    Text(
                        text = sheetTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    val listState = rememberLazyListState()

                    // Automatically center container scroll list on the playing index
                    LaunchedEffect(index) {
                        if (index >= 0 && queue.isNotEmpty()) {
                            listState.animateScrollToItem((index - 1).coerceAtLeast(0))
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(queue) { i, song ->
                                val isCurrent = i == index
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            viewModel.playSongAtIndex(i, queue)
                                        }
                                        .padding(horizontal = 24.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Cover profile / thumbnail placeholder
                                    Card(
                                        modifier = Modifier.size(48.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = song.title,
                                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                            fontSize = 14.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = song.artist,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // If playing, render the custom 3-bar animated soundwave visualizer!
                                    if (isCurrent && isPlaying) {
                                        AnimatedMusicBars()
                                    } else {
                                        Text(
                                            text = song.durationText,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        FastScrollbar(
                            state = listState,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }
            }
        }

        // --- Lyrics Dialog (Read & Manual Edit) ---
        if (showLyricsDialog) {
            Dialog(onDismissRequest = { showLyricsDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = LanguageUtils.get("lyrics_title", lang),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (!showLyricsEditMode) {
                            // View Mode
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(16.dp)
                                        .verticalScroll(rememberScrollState()),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Text(
                                        text = currentSong.lyrics ?: LanguageUtils.get("lyrics_not_available", lang),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 26.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { showLyricsEditMode = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(LanguageUtils.get("edit_lyrics", lang))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { showLyricsDialog = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Text(LanguageUtils.get("cancel", lang), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        } else {
                            // Manual Edit Mode
                            Column(modifier = Modifier.fillMaxSize()) {
                                OutlinedTextField(
                                    value = lyricsEditText,
                                    onValueChange = { lyricsEditText = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    placeholder = { Text(LanguageUtils.get("lyrics_hint", lang)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.saveLyrics(currentSong.path, lyricsEditText)
                                            showLyricsEditMode = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text(LanguageUtils.get("save", lang))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { showLyricsEditMode = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Text(LanguageUtils.get("cancel", lang), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Now Playing Settings Dialog (Speed, Crossfade, KeepScreenOn, Headset Pause) ---
        if (showSettingsDialog) {
            Dialog(onDismissRequest = { showSettingsDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(490.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = LanguageUtils.get("settings", lang),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { showSettingsDialog = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Playback Speed Slider
                        Text(
                            text = "${LanguageUtils.get("speed", lang)}: ${(speed * 100).toInt()}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Slider(
                            value = (speed * 100),
                            onValueChange = { viewModel.updatePlaybackSpeed(it.toInt()) },
                            valueRange = 10f..200f,
                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Crossfade Controller
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(
                                    text = LanguageUtils.get("crossfade", lang),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${crossfadeDuration}s",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = crossfadeEnabled,
                                onCheckedChange = { viewModel.updateCrossfade(it, crossfadeDuration) },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer)
                            )
                        }

                        if (crossfadeEnabled) {
                            Slider(
                                value = crossfadeDuration.toFloat(),
                                onValueChange = { viewModel.updateCrossfade(crossfadeEnabled, it.toInt()) },
                                valueRange = 1f..15f,
                                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Keep Screen ON Switch
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = LanguageUtils.get("keep_screen_on", lang),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Prevent phone screen sleeping",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = keepScreenOn,
                                onCheckedChange = { viewModel.updateKeepScreenOn(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Headset Unplug Action Switch
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = LanguageUtils.get("headset_unplug_pause", lang),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Auto pause when headphones disconnected",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = headsetDisconnectPause,
                                onCheckedChange = { viewModel.updateHeadsetDisconnectAction(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Lock Screen Notice item
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SyncAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = LanguageUtils.get("lock_screen_playing", lang),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Integrated via device notification shade",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Epic Music Cutter Option Block
                        androidx.compose.material3.Card(
                            onClick = {
                                if (currentSong != null) {
                                    showSettingsDialog = false
                                    viewModel.setCutterTargetSong(currentSong)
                                    onNavigateToCutter()
                                } else {
                                    android.widget.Toast.makeText(
                                        context,
                                        if (lang == "fa") "ابتدا یک موسیقی پخش کنید" else "Please play a song first",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCut,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = LanguageUtils.get("epic_music_cutter", lang),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (lang == "fa") "برش، تغییر سرعت و خروجی صوتی پیشرفته" else "Trimming, speed adjustment & export tools",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Simple Single Song Appending to Playlist Selector ---
        if (showPlaylistSelectorForAdd) {
            val userPlaylists by viewModel.playlists.collectAsState()
            var showNewPlaylistInput by remember { mutableStateOf(false) }
            var newPlaylistName by remember { mutableStateOf("") }
            val context = LocalContext.current

            Dialog(onDismissRequest = { showPlaylistSelectorForAdd = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = LanguageUtils.get("add_to_playlist", lang),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (!showNewPlaylistInput) {
                                Button(
                                    onClick = { showNewPlaylistInput = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Create / ایجاد", fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (showNewPlaylistInput) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = newPlaylistName,
                                    onValueChange = { newPlaylistName = it },
                                    label = { Text("Playlist Name / نام لیست پخش") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { showNewPlaylistInput = false },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Text("Cancel / لغو")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (newPlaylistName.isNotBlank()) {
                                                coroutineScope.launch {
                                                    val newId = viewModel.createPlaylistAndGetId(newPlaylistName.trim())
                                                    viewModel.addSingleSongToPlaylist(newId.toInt(), currentSong.path)
                                                    newPlaylistName = ""
                                                    showNewPlaylistInput = false
                                                    showPlaylistSelectorForAdd = false
                                                    android.widget.Toast.makeText(context, "Playlist created and song added", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Save / ایجاد")
                                    }
                                }
                            }
                        } else {
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                itemsIndexed(userPlaylists) { _, p ->
                                    Text(
                                        text = p.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.addSingleSongToPlaylist(p.id, currentSong.path)
                                                showPlaylistSelectorForAdd = false
                                            }
                                            .padding(vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

// Custom 3-Bar Animated sound wave logo
@Composable
fun AnimatedMusicBars() {
    val infiniteTransition = rememberInfiniteTransition(label = "SoundWave")
    
    val h1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 50),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, delayMillis = 100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, delayMillis = 0),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar3"
    )

    Row(
        modifier = Modifier
            .width(22.dp)
            .height(20.dp)
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val activeColor = MaterialTheme.colorScheme.primary
        Box(modifier = Modifier.weight(1f).fillMaxHeight(h1).background(activeColor, RoundedCornerShape(1.dp)))
        Box(modifier = Modifier.weight(1f).fillMaxHeight(h2).background(activeColor, RoundedCornerShape(1.dp)))
        Box(modifier = Modifier.weight(1f).fillMaxHeight(h3).background(activeColor, RoundedCornerShape(1.dp)))
    }
}

// Helper formatting milliseconds into time strings
fun formatMills(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    val hours = (ms / (1000 * 60 * 60))
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

// Custom Solar Hijri (Shamsi) Date Converter (Requirement 1)
fun getSolarHijriDate(year: Int, month: Int, day: Int): String {
    val g_days_in_month = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    val j_days_in_month = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

    val gy = year - 1600
    val gm = month - 1
    val gd = day - 1

    var g_day_no = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
    for (i in 0 until gm) {
        g_day_no += g_days_in_month[i]
    }
    if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
        g_day_no++
    }
    g_day_no += gd

    var j_day_no = g_day_no - 79

    val j_np = j_day_no / 12053
    j_day_no %= 12053

    var jy = 979 + 33 * j_np + 4 * (j_day_no / 1461)
    j_day_no %= 1461

    if (j_day_no >= 366) {
        jy += (j_day_no - 1) / 365
        j_day_no = (j_day_no - 1) % 365
    }

    var jm = 0
    for (i in 0 until 12) {
        if (j_day_no < j_days_in_month[i]) {
            jm = i
            break
        }
        j_day_no -= j_days_in_month[i]
    }
    val jyVal = jy
    val jmVal = jm + 1
    val jdVal = j_day_no + 1

    val monthNames = arrayOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )
    
    val persian_digits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    fun toPersianNumber(num: String): String {
        return num.map { c ->
            if (c in '0'..'9') persian_digits[c - '0'] else c
        }.joinToString("")
    }

    val dateStr = "${jdVal} ${monthNames[jmVal - 1]} ماه سال ${jyVal}"
    return toPersianNumber(dateStr)
}

// Composition Local Provider to set Keep Screen ON
@Composable
fun ScaffoldWithKeepScreenOn(
    keepScreenOn: Boolean,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(keepScreenOn) {
        val activity = context as? android.app.Activity
        if (activity != null) {
            if (keepScreenOn) {
                activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
    content()
}
