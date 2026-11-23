package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.activity.compose.BackHandler
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.rememberDrawerState
import com.example.ui.components.FastScrollbar
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.TabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PlaylistEntity
import com.example.data.model.Song
import com.example.ui.utils.LanguageUtils
import com.example.ui.viewmodel.MusicViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MusicViewModel,
    onNavigateToPlayer: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToCutter: () -> Unit = {},
    songsScrollState: LazyListState,
    favoritesScrollState: LazyListState,
    playlistsScrollState: LazyListState,
    albumsScrollState: LazyListState,
    artistsScrollState: LazyListState,
    genresScrollState: LazyListState,
    foldersScrollState: LazyListState,
    hiddenScrollState: LazyListState,
    initialTabIdx: Int,
    onTabChanged: (Int) -> Unit
) {
    val lang by viewModel.selectedLanguage.collectAsState()
    val songsList by viewModel.songs.collectAsState()
    val playlistsList by viewModel.playlists.collectAsState()
    val currentSongIndex by viewModel.currentSongIndex.collectAsState()
    val currentQueue by viewModel.currentQueue.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val activeThemeIdx by viewModel.appThemeIndex.collectAsState()
    val activeSubThemeIdx by viewModel.subThemeIndex.collectAsState()

    // Multi select state
    val multiSelectMode by viewModel.multiSelectMode.collectAsState()
    val selectedSongPaths by viewModel.selectedSongPaths.collectAsState()
    var selectedGroupPlaylistAddition by remember { mutableStateOf(false) }

    // Search & Sort states
    val sortType by viewModel.sortType.collectAsState()
    val sortAscending by viewModel.sortAscending.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Slide/Swipe Tabs State (8 tabs!)
    val pagerState = rememberPagerState(initialPage = initialTabIdx, pageCount = { 8 })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(pagerState.currentPage) {
        onTabChanged(pagerState.currentPage)
    }

    // Categorized Columns Titles in Farsi & English
    val tabTitles = listOf(
        LanguageUtils.get("songs", lang),
        LanguageUtils.get("favorites", lang),
        LanguageUtils.get("playlists", lang),
        LanguageUtils.get("albums", lang),
        LanguageUtils.get("artists", lang),
        LanguageUtils.get("genres", lang),
        LanguageUtils.get("folders", lang),
        LanguageUtils.get("hidden_songs", lang)
    )



    // Dialog state controllers
    var activeSongForMenu by remember { mutableStateOf<Song?>(null) }
    var renamePlaylistTarget by remember { mutableStateOf<PlaylistEntity?>(null) }
    var playlistToExportTarget by remember { mutableStateOf<PlaylistEntity?>(null) }
    var activeCategoryForCoverEdit by remember { mutableStateOf<Pair<String, Int>?>(null) } // categoryType, itemId

    // Builders Forms
    var showCreatePlaylistForm by remember { mutableStateOf(false) }
    var playlistFormText by remember { mutableStateOf("") }
    var showPlaylistSongsManager by remember { mutableStateOf<PlaylistEntity?>(null) }
    var showSongTagEditForm by remember { mutableStateOf<Song?>(null) }
    var showSongDetailsForm by remember { mutableStateOf<Song?>(null) }
    
    // Sort & Filters Menu
    var showSortMenu by remember { mutableStateOf(false) }
    var showLanguageSettings by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Merged Scanned Songs (Excluding hidden songs)
    val displaySongs = remember(songsList, searchQuery, sortType, sortAscending) {
        songsList.filter { !it.isHidden && (it.title.contains(searchQuery, true) || it.artist.contains(searchQuery, true)) }
            .sortedWith { s1, s2 ->
                val result = when (sortType) {
                    "DURATION" -> s1.duration.compareTo(s2.duration)
                    "ALBUM" -> s1.album.compareTo(s2.album, true)
                    "FILE" -> s1.path.substringAfterLast("/").compareTo(s2.path.substringAfterLast("/"), true)
                    "ARTIST" -> s1.artist.compareTo(s2.artist, true)
                    "PLAY_COUNT" -> s1.playCount.compareTo(s2.playCount)
                    "SIZE" -> s1.size.compareTo(s2.size)
                    "YEAR" -> s1.year.compareTo(s2.year, true)
                    "FOLDER" -> s1.folder.compareTo(s2.folder, true)
                    else -> s1.dateAdded.compareTo(s2.dateAdded) // DATE_ADDED
                }
                if (sortAscending) result else -result
            }
    }

    // Classifications derived dynamically from scanned songs list
    val favoritesSongs = remember(displaySongs) {
        displaySongs.filter { it.isFavorite }
    }
    val albumsMap = remember(displaySongs) { displaySongs.groupBy { it.album } }
    val artistsMap = remember(displaySongs) { displaySongs.groupBy { it.artist } }
    val genresMap = remember(displaySongs) { displaySongs.groupBy { it.genre } }
    val foldersMap = remember(displaySongs) { displaySongs.groupBy { it.folder } }

    // Auto-scroll (focus/center) the playing song in the "Songs" tab
    LaunchedEffect(pagerState.currentPage, currentSongIndex, currentQueue, displaySongs) {
        if (pagerState.currentPage == 0 && currentQueue.isNotEmpty() && currentSongIndex >= 0) {
            val currentPlaySong = currentQueue.getOrNull(currentSongIndex)
            if (currentPlaySong != null) {
                val idx = displaySongs.indexOfFirst { it.path == currentPlaySong.path }
                if (idx >= 0) {
                    val layoutInfo = songsScrollState.layoutInfo
                    val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
                    if (viewportHeight > 0) {
                        val itemSize = layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
                        val offset = - (viewportHeight / 2 - itemSize / 2)
                        songsScrollState.animateScrollToItem(idx, offset)
                    } else {
                        songsScrollState.animateScrollToItem(idx)
                    }
                }
            }
        }
    }

    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !multiSelectMode,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF0F172A).copy(alpha = 0.96f),
                drawerContentColor = Color.White,
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                // Aurora Glassmorphic Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0284C7).copy(alpha = 0.45f),
                                    Color(0xFF6366F1).copy(alpha = 0.35f),
                                    Color(0xFFC026D3).copy(alpha = 0.25f)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 26.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF0284C7), Color(0xFF818CF8))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Epic Music Player",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Glassmorphic Aurora Edition",
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val drawerScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(drawerScrollState)
                        .padding(horizontal = 12.dp)
                ) {
                    // 1. راهنما (Guide)
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = if (lang == "fa") "راهنمای جامع اپلیکیشن" else "App Interactive Guide",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.Help,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8)
                            )
                        },
                        badge = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0284C7).copy(alpha = 0.3f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (lang == "fa") "راهنما" else "Help",
                                    fontSize = 10.sp,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            showGuideDialog = true
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            unselectedTextColor = Color.White
                        )
                    )

                    // 2. زبان (Language)
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = LanguageUtils.get("language", lang),
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = null,
                                tint = Color(0xFF818CF8)
                            )
                        },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            showLanguageSettings = true
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            unselectedTextColor = Color.White
                        )
                    )

                    // 3. تم‌ها (Themes)
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = LanguageUtils.get("themes", lang),
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color(0xFFC026D3)
                            )
                        },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            showThemeMenu = true
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            unselectedTextColor = Color.White
                        )
                    )

                    // 4. اشتراک‌گذاری (Share)
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = LanguageUtils.get("share_app", lang),
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                tint = Color(0xFF34D399)
                            )
                        },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            com.example.ui.utils.FileShareUtils.shareAppApk(context)
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            unselectedTextColor = Color.White
                        )
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Color.White.copy(alpha = 0.12f)
                    )

                    // 5. Epic Music Cutter
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = LanguageUtils.get("epic_music_cutter", lang),
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.ContentCut,
                                contentDescription = null,
                                tint = Color(0xFFF43F5E)
                            )
                        },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            onNavigateToCutter()
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            unselectedTextColor = Color.White
                        )
                    )

                    // 6. Equalizer
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = LanguageUtils.get("equalizer", lang),
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.Equalizer,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24)
                            )
                        },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            onNavigateToEqualizer()
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            unselectedTextColor = Color.White
                        )
                    )

                    // 7. Sort
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = LanguageUtils.get("sort_by", lang),
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.Sort,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8)
                            )
                        },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            showSortMenu = true
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            unselectedTextColor = Color.White
                        )
                    )
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            com.example.ui.components.FluidBackground(themeIndex = activeThemeIdx, subThemeIndex = activeSubThemeIdx, isPlaying = isPlaying)

            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
            // --- 1. Top Section (Normal search header or contextual multi-select top bar) ---
            AnimatedVisibility(
                visible = multiSelectMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                // Bulk Contextual Top Action Bar requested by the user
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { viewModel.toggleMultiSelect() }) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Text(
                        text = "${selectedSongPaths.size} selected",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Bulk play next
                        IconButton(onClick = {
                            viewModel.bulkPlayNext()
                            Toast.makeText(context, "Added to Play Next", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play Next", tint = MaterialTheme.colorScheme.onPrimary)
                        }

                        // Bulk enqueue
                        IconButton(onClick = {
                            viewModel.bulkAddToQueue()
                            Toast.makeText(context, "Added to Queue", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.LibraryMusic, contentDescription = "Add Queue", tint = MaterialTheme.colorScheme.onPrimary)
                        }

                        // Bulk Add to Playlist
                        IconButton(onClick = {
                            if (selectedSongPaths.isNotEmpty()) {
                                selectedGroupPlaylistAddition = true
                            } else {
                                Toast.makeText(context, "No songs selected", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = "Add to Playlist", tint = MaterialTheme.colorScheme.onPrimary)
                        }

                        // Bulk share
                        IconButton(onClick = {
                            val selectedSongs = songsList.filter { it.path in selectedSongPaths }
                            if (selectedSongs.isNotEmpty()) {
                                com.example.ui.utils.FileShareUtils.shareSongs(context, selectedSongs)
                                viewModel.toggleMultiSelect()
                            } else {
                                Toast.makeText(context, "No songs selected to share", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onPrimary)
                        }

                        // Bulk delete
                        IconButton(onClick = {
                            viewModel.bulkDelete()
                            Toast.makeText(context, "Deleted selected files", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }

            // Top Action Header when not in multi-select mode
            if (!multiSelectMode) {
                AnimatedVisibility(
                    visible = isSearchExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    // Expanded Search Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { isSearchExpanded = false }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Close Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(26.dp),
                            placeholder = { Text(text = LanguageUtils.get("search_placeholder", lang), fontSize = 13.sp) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear Search",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )

                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                if (!isSearchExpanded) {
                    // Normal Top Bar with Sandwich Menu on Left, Small Guide Button, Title, and Search/Sort on Right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Sandwich Drawer Menu Button & Small Guide Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { coroutineScope.launch { drawerState.open() } }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Sandwich Menu",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Small Guide Button with glowing indicator
                            IconButton(
                                onClick = { showGuideDialog = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    Icon(
                                        imageVector = Icons.Default.HelpOutline,
                                        contentDescription = "Help Guide",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(23.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .offset(x = 1.dp, y = (-1).dp)
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF38BDF8))
                                    )
                                }
                            }
                        }

                        // Center: App title
                        Text(
                            text = "Epic Music",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Right: Search Button & Sort Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = { isSearchExpanded = true }) {
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    if (searchQuery.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF38BDF8))
                                                .align(Alignment.TopEnd)
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }

        // --- 2. Slidable Pager and category headings row ---
        val isCyberTheme = MaterialTheme.colorScheme.primary == Color(0xFFFF007F)
        val maxTitleLength = remember(tabTitles) {
            tabTitles.maxOfOrNull { it.length } ?: 15
        }
        val maxUniformTabWidth = remember(maxTitleLength) {
            (maxTitleLength * 11 + 32).dp.coerceAtLeast(130.dp)
        }

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = if (isCyberTheme) Color(0xFFFF007F) else MaterialTheme.colorScheme.primary,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                if (pagerState.currentPage < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = if (isCyberTheme) Color(0xFF00F3FF) else MaterialTheme.colorScheme.primary,
                        height = if (isCyberTheme) 3.5.dp else 2.dp
                    )
                }
            }
        ) {
            tabTitles.forEachIndexed { idx, title ->
                Tab(
                    selected = pagerState.currentPage == idx,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(idx) }
                    },
                    modifier = Modifier.width(maxUniformTabWidth),
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (pagerState.currentPage == idx) {
                                if (isCyberTheme) Color(0xFF00F3FF) else MaterialTheme.colorScheme.primary
                            } else {
                                if (isCyberTheme) Color(0xFFE9FBFD).copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Visible
                        )
                    }
                )
            }
        }

        // --- 3. Sub pages sliding Horizontal Pager (8 distinct pages) ---
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            when (pageIndex) {
                0 -> {
                    // ---- Page 1: Songs ----
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = songsScrollState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            items(displaySongs) { song ->
                                val isSelected = selectedSongPaths.contains(song.path)
                                SongRowItem(
                                    song = song,
                                    isPlaying = isPlaying && currentQueue.getOrNull(currentSongIndex)?.path == song.path,
                                    isMultiSelectMode = multiSelectMode,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (multiSelectMode) {
                                            viewModel.selectSongPathToggle(song.path)
                                        } else {
                                            // Play immediately
                                            viewModel.playSongAtIndex(displaySongs.indexOf(song), displaySongs)
                                            onNavigateToPlayer()
                                        }
                                    },
                                    onLongClick = {
                                        if (!multiSelectMode) {
                                            viewModel.toggleMultiSelect()
                                            viewModel.selectSongPathToggle(song.path)
                                        }
                                    },
                                    onMenuClick = { activeSongForMenu = song }
                                )
                            }
                        }

                        FastScrollbar(
                            state = songsScrollState,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }
                1 -> {
                    // ---- Page 2: Favorites ----
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = favoritesScrollState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            if (favoritesSongs.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillParentMaxSize()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (lang == "fa") "آهنگی در علاقه‌مندی‌ها وجود ندارد" else if (lang == "az") "Sevimlilərdə mahnı yoxdur" else "No favorite songs yet",
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            } else {
                                items(favoritesSongs) { song ->
                                    val isSelected = selectedSongPaths.contains(song.path)
                                    SongRowItem(
                                        song = song,
                                        isPlaying = isPlaying && currentQueue.getOrNull(currentSongIndex)?.path == song.path,
                                        isMultiSelectMode = multiSelectMode,
                                        isSelected = isSelected,
                                        onClick = {
                                            if (multiSelectMode) {
                                                viewModel.selectSongPathToggle(song.path)
                                            } else {
                                                viewModel.playSongAtIndex(favoritesSongs.indexOf(song), favoritesSongs, playlistName = "favorites")
                                                onNavigateToPlayer()
                                            }
                                        },
                                        onLongClick = {
                                            if (!multiSelectMode) {
                                                viewModel.toggleMultiSelect()
                                                viewModel.selectSongPathToggle(song.path)
                                            }
                                        },
                                        onMenuClick = { activeSongForMenu = song }
                                    )
                                }
                            }
                        }

                        FastScrollbar(
                            state = favoritesScrollState,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }
                2 -> {
                    // ---- Page 3: Playlists ----
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = { showCreatePlaylistForm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(LanguageUtils.get("create_playlist", lang), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            LazyColumn(
                                state = playlistsScrollState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(playlistsList) { playlist ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .clickable {
                                                showPlaylistSongsManager = playlist
                                            },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = playlist.name,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 15.sp
                                                )
                                            }

                                            Row {
                                                // Play playlist trigger
                                                val playlistSongs by remember(playlist) { viewModel.getPlaylistSongsFlow(playlist.id) }.collectAsState(initial = emptyList())
                                                IconButton(onClick = {
                                                    if (playlistSongs.isNotEmpty()) {
                                                        viewModel.playPlaylist(playlist.id, playlist.name, playlistSongs)
                                                        onNavigateToPlayer()
                                                    } else {
                                                        Toast.makeText(context, "No songs in this playlist (لیست خالی است)", Toast.LENGTH_SHORT).show()
                                                    }
                                                }) {
                                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play Playlist", tint = MaterialTheme.colorScheme.primary)
                                                }

                                                // Covers selection trigger
                                                IconButton(onClick = { activeCategoryForCoverEdit = "playlist" to playlist.id }) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit cover")
                                                }
                                                // Export JSON
                                                Button(
                                                    onClick = { playlistToExportTarget = playlist },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                                                ) {
                                                    Text("Export", fontSize = 11.sp)
                                                }
                                                // Delete Playlist
                                                IconButton(onClick = { viewModel.deletePlaylist(playlist.id) }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            FastScrollbar(
                                state = playlistsScrollState,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                }
                3 -> {
                    // ---- Page 4: Albums ----
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = albumsScrollState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            items(albumsMap.keys.toList()) { albumName ->
                                val albumSongs = albumsMap[albumName] ?: emptyList()
                                CategoryRowCard(
                                    title = albumName,
                                    subtitle = "${albumSongs.size} Songs",
                                    icon = Icons.Default.LibraryMusic,
                                    onPlayAll = {
                                        viewModel.playSongAtIndex(0, albumSongs, playlistName = "album:$albumName")
                                        onNavigateToPlayer()
                                    },
                                    onCoverEdit = { activeCategoryForCoverEdit = "album" to 0 } // index placeholder
                                )
                            }
                        }

                        FastScrollbar(
                            state = albumsScrollState,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }
                4 -> {
                    // ---- Page 5: Artists ----
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = artistsScrollState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            items(artistsMap.keys.toList()) { artistName ->
                                val artistSongs = artistsMap[artistName] ?: emptyList()
                                CategoryRowCard(
                                    title = artistName,
                                    subtitle = "${artistSongs.size} Songs",
                                    icon = Icons.Default.MusicNote,
                                    onPlayAll = {
                                        viewModel.playSongAtIndex(0, artistSongs, playlistName = "artist:$artistName")
                                        onNavigateToPlayer()
                                    },
                                    onCoverEdit = { activeCategoryForCoverEdit = "artist" to 0 }
                                )
                            }
                        }

                        FastScrollbar(
                            state = artistsScrollState,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }
                5 -> {
                    // ---- Page 6: Genres ----
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = genresScrollState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            items(genresMap.keys.toList()) { genreName ->
                                val genreSongs = genresMap[genreName] ?: emptyList()
                                CategoryRowCard(
                                    title = genreName,
                                    subtitle = "${genreSongs.size} Songs",
                                    icon = Icons.Default.Radio,
                                    onPlayAll = {
                                        viewModel.playSongAtIndex(0, genreSongs, playlistName = "genre:$genreName")
                                        onNavigateToPlayer()
                                    },
                                    onCoverEdit = null
                                )
                            }
                        }

                        FastScrollbar(
                            state = genresScrollState,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }
                6 -> {
                    // ---- Page 7: Folders ----
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = foldersScrollState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            items(foldersMap.keys.toList()) { folderName ->
                                val folderSongs = foldersMap[folderName] ?: emptyList()
                                CategoryRowCard(
                                    title = folderName,
                                    subtitle = "${folderSongs.size} Songs",
                                    icon = Icons.Default.Folder,
                                    onPlayAll = {
                                        viewModel.playSongAtIndex(0, folderSongs, playlistName = "folder:$folderName")
                                        onNavigateToPlayer()
                                    },
                                    onCoverEdit = null
                                )
                            }
                        }

                        FastScrollbar(
                            state = foldersScrollState,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }
                7 -> {
                    // ---- Page 8: Hidden Songs ----
                    val hiddenSongs = remember(songsList) { songsList.filter { it.isHidden } }
                    val isUnlocked by viewModel.isHiddenUnlocked.collectAsState()
                    
                    HiddenSongsPage(
                        hiddenSongs = hiddenSongs,
                        isUnlocked = isUnlocked,
                        scrollState = hiddenScrollState,
                        lang = lang,
                        context = context,
                        viewModel = viewModel,
                        onNavigateToPlayer = onNavigateToPlayer,
                        onSongMenuClick = { activeSongForMenu = it }
                    )
                }
            }
        }

        // --- 4. Micro Player Control bar at bottom of the main list ---
        val currentPlaySong = currentQueue.getOrNull(currentSongIndex)
        if (currentPlaySong != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onNavigateToPlayer() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Album art thumbnail
                        Card(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentPlaySong.title,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentPlaySong.artist,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.togglePlayPause() }) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            IconButton(onClick = { viewModel.nextSong() }) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Progress bar indicator along bottom edge
                    val progress by viewModel.playbackProgress.collectAsState()
                    val totalDuration = currentPlaySong.duration.coerceAtLeast(1L)
                    val progressRatio = (progress.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressRatio)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
    }

    // --- DIALOGS & OVERRUN MENUS ---

    // Song Popup 3-Dot Options Sheet
    activeSongForMenu?.let { song ->
        var showSinglePlaylistSelector by remember { mutableStateOf(false) }
        Dialog(onDismissRequest = { activeSongForMenu = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = song.title,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Epic Music Cutter option
                    MenuOptionItem(
                        text = LanguageUtils.get("epic_music_cutter", lang),
                        icon = Icons.Default.ContentCut,
                        onClick = {
                            viewModel.setCutterTargetSong(song)
                            onNavigateToCutter()
                            activeSongForMenu = null
                        }
                    )

                    // Set as ringtone
                    MenuOptionItem(
                        text = LanguageUtils.get("set_ringtone", lang),
                        onClick = {
                            Toast.makeText(context, LanguageUtils.get("ringtone_simulated", lang), Toast.LENGTH_LONG).show()
                            activeSongForMenu = null
                        }
                    )

                    // Change Cover Art
                    MenuOptionItem(
                        text = LanguageUtils.get("change_cover", lang),
                        onClick = {
                            activeCategoryForCoverEdit = "song" to 0
                            activeSongForMenu = null
                        }
                    )

                    // Edit Tags
                    MenuOptionItem(
                        text = LanguageUtils.get("edit_tags", lang),
                        onClick = {
                            showSongTagEditForm = song
                            activeSongForMenu = null
                        }
                    )

                    // Go to Album
                    MenuOptionItem(
                        text = LanguageUtils.get("go_to_album", lang),
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(3) } // album page
                            activeSongForMenu = null
                        }
                    )

                    // Hide song
                    if (song.isHidden) {
                        MenuOptionItem(
                            text = LanguageUtils.get("unhide_song", lang),
                            onClick = {
                                viewModel.unhideSong(song)
                                Toast.makeText(context, "Song is visible", Toast.LENGTH_SHORT).show()
                                activeSongForMenu = null
                            }
                        )
                    } else {
                        MenuOptionItem(
                            text = LanguageUtils.get("hide_song", lang),
                            onClick = {
                                viewModel.hideSong(song)
                                Toast.makeText(context, "Song hidden", Toast.LENGTH_SHORT).show()
                                activeSongForMenu = null
                            }
                        )
                    }

                    // Delete song
                    MenuOptionItem(
                        text = LanguageUtils.get("delete", lang),
                        onClick = {
                            viewModel.deleteSong(song)
                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                            activeSongForMenu = null
                        }
                    )

                    // Share song
                    MenuOptionItem(
                        text = LanguageUtils.get("share", lang),
                        onClick = {
                            com.example.ui.utils.FileShareUtils.shareSongs(context, listOf(song))
                            activeSongForMenu = null
                        }
                    )

                    // Song Details
                    MenuOptionItem(
                        text = "Metadata " + LanguageUtils.get("song_details", lang),
                        onClick = {
                            showSongDetailsForm = song
                            activeSongForMenu = null
                        }
                    )
                }
            }
        }
    }

    // Custom Cover Art Selector dialog (containing 5 visual gradients)
    activeCategoryForCoverEdit?.let { target ->
        Dialog(onDismissRequest = { activeCategoryForCoverEdit = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Select Beautiful Cover Accent", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    val gradients = listOf(
                        "gradient_starlight" to "Midnight Magenta Space",
                        "gradient_deepwater" to "Submarine Teal Oasis",
                        "gradient_technogrid" to "Cyberpunk Cyber Beat",
                        "gradient_persianrose" to "Shiraz Rose Classic",
                        "gradient_azeribreeze" to "Caspian Gold Breeze"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        gradients.forEach { (key, display) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (target.first == "song" && activeSongForMenu != null) {
                                            viewModel.updateSongCover(activeSongForMenu!!.path, key)
                                        } else if (target.first == "playlist") {
                                            viewModel.updatePlaylistCover(target.second, key)
                                        }
                                        Toast.makeText(context, "Cover applied successfully", Toast.LENGTH_SHORT).show()
                                        activeCategoryForCoverEdit = null
                                    }
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.Gray)) // placeholder color indicator
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(display, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }

    // Playlist exporter Dialog Form
    playlistToExportTarget?.let { playlist ->
        Dialog(onDismissRequest = { playlistToExportTarget = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Export Options (${playlist.name})", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val txt = viewModel.exportPlaylistToString(playlist.id, playlist.name, "json", displaySongs)
                            Toast.makeText(context, "Exported as JSON (Check logs/save)", Toast.LENGTH_LONG).show()
                            playlistToExportTarget = null
                        }
                    ) {
                        Text("Export as JSON (قالب متنی جی‌سان)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val txt = viewModel.exportPlaylistToString(playlist.id, playlist.name, "csv", displaySongs)
                            Toast.makeText(context, "Exported as CSV File (قالب متنی سی‌اس‌وی)", Toast.LENGTH_LONG).show()
                            playlistToExportTarget = null
                        }
                    ) {
                        Text("Export as CSV Excel")
                    }
                }
            }
        }
    }

    // Tag Editor dialog
    showSongTagEditForm?.let { song ->
        var editTitle by remember { mutableStateOf(song.title) }
        var editArtist by remember { mutableStateOf(song.artist) }
        var editAlbum by remember { mutableStateOf(song.album) }
        var editGenre by remember { mutableStateOf(song.genre) }
        var editYear by remember { mutableStateOf(song.year) }

        Dialog(onDismissRequest = { showSongTagEditForm = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Tag Metadata Editor", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(value = editTitle, onValueChange = { editTitle = it }, label = { Text("Title") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editArtist, onValueChange = { editArtist = it }, label = { Text("Artist") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editAlbum, onValueChange = { editAlbum = it }, label = { Text("Album") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editGenre, onValueChange = { editGenre = it }, label = { Text("Genre") })
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = {
                            viewModel.updateSongMetadata(song.path, editTitle, editArtist, editAlbum, editGenre, editYear)
                            Toast.makeText(context, "MetaData Updated offline", Toast.LENGTH_SHORT).show()
                            showSongTagEditForm = null
                        }) {
                            Text("Save Tags")
                        }
                    }
                }
            }
        }
    }

    // Playlist generator Dialog Builder
    if (showCreatePlaylistForm) {
        Dialog(onDismissRequest = { showCreatePlaylistForm = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(LanguageUtils.get("create_playlist", lang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = playlistFormText,
                        onValueChange = { playlistFormText = it },
                        placeholder = { Text(LanguageUtils.get("playlist_name", lang)) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (playlistFormText.trim().isNotEmpty()) {
                                viewModel.createAndAddPlaylist(playlistFormText.trim())
                                playlistFormText = ""
                                showCreatePlaylistForm = false
                            }
                        }
                    ) {
                        Text(LanguageUtils.get("save", lang))
                    }
                }
            }
        }
    }

    // Playlist Manager Dialog (for editing/removing/adding songs within a playlist)
    showPlaylistSongsManager?.let { playlist ->
        // Retrieve local songs of this playlist reactively
        val playlistSongs by remember(playlist) { viewModel.getPlaylistSongsFlow(playlist.id) }.collectAsState(initial = emptyList())
        var isAddingSongsMode by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showPlaylistSongsManager = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAddingSongsMode) "Add Songs (آهنگ جدید)" else playlist.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${playlistSongs.size} tracks",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Toggle Mode
                            IconButton(onClick = { isAddingSongsMode = !isAddingSongsMode }) {
                                Icon(
                                    imageVector = if (isAddingSongsMode) Icons.Default.ArrowBack else Icons.Default.Add,
                                    contentDescription = "Toggle add",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Play All Flow
                            if (!isAddingSongsMode && playlistSongs.isNotEmpty()) {
                                IconButton(onClick = {
                                    viewModel.playSongAtIndex(0, playlistSongs, playlist.id, playlist.name)
                                    onNavigateToPlayer()
                                    showPlaylistSongsManager = null
                                }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.primary)
                                }
                            }

                            // Close Dialog
                            IconButton(onClick = { showPlaylistSongsManager = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    )

                    if (isAddingSongsMode) {
                        // Gather remaining songs
                        val availableSongsToAdd = songsList.filter { libSong -> 
                            !playlistSongs.any { it.path == libSong.path } 
                        }
                        if (availableSongsToAdd.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No more songs available to add.", fontSize = 14.sp)
                            }
                        } else {
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(availableSongsToAdd) { song ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val customCover = song.customCover
                                            if (customCover != null) {
                                                if (customCover.startsWith("gradient_")) {
                                                    val brush = when (customCover) {
                                                        "gradient_starlight" -> Brush.radialGradient(colors = listOf(Color(0xFF8E24AA), Color(0xFF0F0B1E)))
                                                        "gradient_deepwater" -> Brush.radialGradient(colors = listOf(Color(0xFF00ACC1), Color(0xFF003040)))
                                                        "gradient_technogrid" -> Brush.radialGradient(colors = listOf(Color(0xFFFF1744), Color(0xFF1B0000)))
                                                        "gradient_persianrose" -> Brush.radialGradient(colors = listOf(Color(0xFFFF4081), Color(0xFF4A148C)))
                                                        "gradient_azeribreeze" -> Brush.radialGradient(colors = listOf(Color(0xFFD4AF37), Color(0xFF112233)))
                                                        else -> Brush.verticalGradient(colors = listOf(Color(0xFFFF5722), Color(0xFF2E3137)))
                                                    }
                                                    Box(modifier = Modifier.fillMaxSize().background(brush), contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.6f))
                                                    }
                                                } else {
                                                    val context = LocalContext.current
                                                    val model = remember(customCover) {
                                                        if (customCover.startsWith("/")) java.io.File(customCover)
                                                        else {
                                                            val resId = context.resources.getIdentifier(customCover, "drawable", context.packageName)
                                                            if (resId != 0) resId else customCover
                                                        }
                                                    }
                                                    coil.compose.AsyncImage(
                                                        model = model,
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                    )
                                                }
                                            } else if (!song.path.startsWith("PRELOADED_") && song.path.startsWith("/")) {
                                                val context = LocalContext.current
                                                val model = remember(song.path) {
                                                    try {
                                                        val retriever = android.media.MediaMetadataRetriever()
                                                        retriever.setDataSource(song.path)
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
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(song.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(song.artist, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                        }
                                        IconButton(onClick = {
                                            viewModel.addSingleSongToPlaylist(playlist.id, song.path)
                                            Toast.makeText(context, "${song.title} added!", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Show current playlist songs with edit controls
                        if (playlistSongs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                                Text("No songs. Click '+' to add songs!", fontSize = 14.sp, textAlign = TextAlign.Center)
                            }
                        } else {
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                itemsIndexed(playlistSongs) { index, song ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                            .clickable {
                                                viewModel.playSongAtIndex(index, playlistSongs, playlist.id, playlist.name)
                                                onNavigateToPlayer()
                                                showPlaylistSongsManager = null
                                            }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val customCover = song.customCover
                                            if (customCover != null) {
                                                if (customCover.startsWith("gradient_")) {
                                                    val brush = when (customCover) {
                                                        "gradient_starlight" -> Brush.radialGradient(colors = listOf(Color(0xFF8E24AA), Color(0xFF0F0B1E)))
                                                        "gradient_deepwater" -> Brush.radialGradient(colors = listOf(Color(0xFF00ACC1), Color(0xFF003040)))
                                                        "gradient_technogrid" -> Brush.radialGradient(colors = listOf(Color(0xFFFF1744), Color(0xFF1B0000)))
                                                        "gradient_persianrose" -> Brush.radialGradient(colors = listOf(Color(0xFFFF4081), Color(0xFF4A148C)))
                                                        "gradient_azeribreeze" -> Brush.radialGradient(colors = listOf(Color(0xFFD4AF37), Color(0xFF112233)))
                                                        else -> Brush.verticalGradient(colors = listOf(Color(0xFFFF5722), Color(0xFF2E3137)))
                                                    }
                                                    Box(modifier = Modifier.fillMaxSize().background(brush), contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.6f))
                                                    }
                                                } else {
                                                    val context = LocalContext.current
                                                    val model = remember(customCover) {
                                                        if (customCover.startsWith("/")) java.io.File(customCover)
                                                        else {
                                                            val resId = context.resources.getIdentifier(customCover, "drawable", context.packageName)
                                                            if (resId != 0) resId else customCover
                                                        }
                                                    }
                                                    coil.compose.AsyncImage(
                                                        model = model,
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                    )
                                                }
                                            } else if (!song.path.startsWith("PRELOADED_") && song.path.startsWith("/")) {
                                                val context = LocalContext.current
                                                val model = remember(song.path) {
                                                    try {
                                                        val retriever = android.media.MediaMetadataRetriever()
                                                        retriever.setDataSource(song.path)
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
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(song.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(song.artist, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Reorder Up
                                            IconButton(
                                                onClick = {
                                                    if (index > 0) {
                                                        val reordered = playlistSongs.map { it.path }.toMutableList()
                                                        val temp = reordered[index]
                                                        reordered[index] = reordered[index - 1]
                                                        reordered[index - 1] = temp
                                                        viewModel.reorderPlaylistSongs(playlist.id, reordered)
                                                    }
                                                },
                                                enabled = index > 0
                                            ) {
                                                Icon(Icons.Default.ArrowUpward, contentDescription = "Up", modifier = Modifier.size(16.dp))
                                            }

                                            // Reorder Down
                                            IconButton(
                                                onClick = {
                                                    if (index < playlistSongs.lastIndex) {
                                                        val reordered = playlistSongs.map { it.path }.toMutableList()
                                                        val temp = reordered[index]
                                                        reordered[index] = reordered[index + 1]
                                                        reordered[index + 1] = temp
                                                        viewModel.reorderPlaylistSongs(playlist.id, reordered)
                                                    }
                                                },
                                                enabled = index < playlistSongs.lastIndex
                                            ) {
                                                Icon(Icons.Default.ArrowDownward, contentDescription = "Down", modifier = Modifier.size(16.dp))
                                            }

                                            // Remove
                                            IconButton(onClick = {
                                                viewModel.removeSingleSongFromPlaylist(playlist.id, song.path)
                                                Toast.makeText(context, "${song.title} removed!", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
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
    }

    // Multi select add songs to playlist picker
    if (selectedGroupPlaylistAddition) {
        Dialog(onDismissRequest = { selectedGroupPlaylistAddition = false }) {
            var showNewPlaylistInput by remember { mutableStateOf(false) }
            var newPlaylistName by remember { mutableStateOf("") }
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
                        Text("Select Playlist (انتخاب لیست پخش)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                                                viewModel.bulkAddToPlaylist(newId.toInt())
                                                newPlaylistName = ""
                                                showNewPlaylistInput = false
                                                selectedGroupPlaylistAddition = false
                                                Toast.makeText(context, "Playlist created and tracks added", Toast.LENGTH_SHORT).show()
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
                            items(playlistsList) { p ->
                                Text(
                                    text = p.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.bulkAddToPlaylist(p.id)
                                            selectedGroupPlaylistAddition = false
                                            Toast.makeText(context, "Selected tracks added", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 12.dp),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Sort configurations selection Modal
    if (showSortMenu) {
        Dialog(onDismissRequest = { showSortMenu = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(LanguageUtils.get("sort_by", lang), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    val sortOptions = listOf(
                        "DATE_ADDED" to LanguageUtils.get("sort_date_added", lang),
                        "DURATION" to LanguageUtils.get("sort_duration", lang),
                        "ALBUM" to LanguageUtils.get("sort_album", lang),
                        "FILE" to LanguageUtils.get("sort_file", lang),
                        "ARTIST" to LanguageUtils.get("sort_artist", lang),
                        "PLAY_COUNT" to LanguageUtils.get("sort_play_count", lang),
                        "SIZE" to LanguageUtils.get("sort_size", lang),
                        "YEAR" to LanguageUtils.get("sort_year", lang),
                        "FOLDER" to LanguageUtils.get("sort_folder", lang)
                    )

                    sortOptions.forEach { (typeKey, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateSortOrder(typeKey, sortAscending)
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = sortType == typeKey,
                                onCheckedChange = { viewModel.updateSortOrder(typeKey, sortAscending) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ascending / Descending Order", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Switch(
                            checked = sortAscending,
                            onCheckedChange = { viewModel.updateSortOrder(sortType, it) }
                        )
                    }
                }
            }
        }
    }

    // Theme Picker selector
    if (showThemeMenu) {
        Dialog(onDismissRequest = { showThemeMenu = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(LanguageUtils.get("themes", lang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Light / Dark Mode Toggle Selector
                    val isDarkModeOverride by viewModel.isDarkModeOverride.collectAsState()
                    Text(
                        text = if (lang == "fa") "حالت تاریک / روشن:" else "Dark / Light Mode:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val modes = listOf(
                            null to if (lang == "fa") "سیستم" else "System",
                            false to if (lang == "fa") "روشن" else "Light",
                            true to if (lang == "fa") "تاریک" else "Dark"
                        )
                        modes.forEach { (modeVal, modeLabel) ->
                            val isSelected = isDarkModeOverride == modeVal
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { viewModel.updateDarkModeOverride(modeVal) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = modeLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val activeThemeIdx by viewModel.appThemeIndex.collectAsState()
                    val templates = listOf(
                        0 to if (lang == "fa") "الگوی ۱: میراث ماندگار (Legacy Fusion)" else "Template 1: Legacy Fusion",
                        1 to if (lang == "fa") "الگوی ۲: آرورای شیشه‌ای (Aurora)" else "Template 2: Glassmorphic Aurora",
                        2 to if (lang == "fa") "الگوی ۳: کاست نوستالژی (Tape Deck)" else "Template 3: Retro Tape Deck",
                        3 to if (lang == "fa") "الگوی ۴: ماتریکس سایبر (Cyber Grid)" else "Template 4: Cyber Grid Neon",
                        4 to if (lang == "fa") "الگوی ۵: امواج آرامش (Zen Ripple)" else "Template 5: Zen Nature Ripple",
                        5 to if (lang == "fa") "الگوی ۶: کهکشان بی‌پایان (Starfield)" else "Template 6: Cosmic Starfield",
                        6 to if (lang == "fa") "الگوی ۷: گرامافون کلاسیک (Vintage Gramophone)" else "Template 7: Vintage Gramophone"
                    )

                    templates.forEach { (index, label) ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.updateThemeIndex(index)
                                    }
                                    .background(
                                        if (activeThemeIdx == index) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else Color.Transparent
                                    )
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }

                            if (index == 0 && activeThemeIdx == 0) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, top = 8.dp, bottom = 12.dp)
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = if (lang == "fa") "پالت رنگی پوسته میراث:" else "Legacy Color Palette:",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val activeSubThemeIdx by viewModel.subThemeIndex.collectAsState()
                                    val subThemes = listOf(
                                        0 to LanguageUtils.get("theme_phoenix", lang),
                                        1 to LanguageUtils.get("theme_nebula", lang),
                                        2 to LanguageUtils.get("theme_mint", lang),
                                        3 to LanguageUtils.get("theme_coral", lang),
                                        4 to LanguageUtils.get("theme_cyberpunk", lang),
                                        5 to LanguageUtils.get("theme_system", lang)
                                    )
                                    subThemes.forEach { (subIdx, subLabel) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    viewModel.updateSubThemeIndex(subIdx)
                                                }
                                                .background(
                                                    if (activeSubThemeIdx == subIdx) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                    else Color.Transparent
                                                )
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(
                                                        color = when (subIdx) {
                                                            0 -> GoldPrimary
                                                            1 -> CosmicPrimary
                                                            2 -> MintPrimary
                                                            3 -> CoralPrimary
                                                            4 -> CyberPrimary
                                                            else -> MaterialTheme.colorScheme.primary
                                                        },
                                                        shape = CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(subLabel, fontSize = 13.sp, fontWeight = FontWeight.Normal)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }

    // Language configurations selector dialog (15 global languages)
    if (showLanguageSettings) {
        Dialog(onDismissRequest = { showLanguageSettings = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().height(480.dp).padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(LanguageUtils.get("language", lang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(LanguageUtils.supportedLanguages) { item ->
                            val isSelected = lang == item.code
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.updateLanguage(item.code)
                                    }
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else Color.Transparent
                                    )
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // App Interactive Schematic Guide Dialog
    if (showGuideDialog) {
        com.example.ui.components.AppGuideDialog(
            lang = lang,
            onDismiss = { showGuideDialog = false }
        )
    }
}
}

// Individual Song List Row Item
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongRowItem(
    song: Song,
    isPlaying: Boolean,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
            } else if (isPlaying) {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.40f)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = androidx.compose.foundation.LocalIndication.current,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
        // Thumbnail card containing song cover image/gradient or fallback icon
        Card(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val customCover = song.customCover
                if (customCover != null) {
                    if (customCover.startsWith("gradient_")) {
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
                                colors = listOf(Color(0xFFFF5722), Color(0xFF2E3137))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(brush),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
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
                            contentDescription = "Cover Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            alignment = Alignment.Center
                        )
                    }
                } else if (!song.path.startsWith("PRELOADED_") && song.path.startsWith("/")) {
                    val context = LocalContext.current
                    val model = remember(song.path) {
                        try {
                            val retriever = android.media.MediaMetadataRetriever()
                            retriever.setDataSource(song.path)
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
                            contentDescription = "Cover Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            alignment = Alignment.Center
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPlaying) {
                    AnimatedEqualizerIndicator()
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = song.title,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = song.artist,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        if (isMultiSelectMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = song.durationText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                IconButton(onClick = onMenuClick) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                }
            }
        }
    }
}
}

// Category lists details layout helper (album/artist/genre folders)
@Composable
fun CategoryRowCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onPlayAll: () -> Unit,
    onCoverEdit: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onPlayAll() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                Text(text = subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 12.sp)
            }

            Row {
                if (onCoverEdit != null) {
                    IconButton(onClick = onCoverEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Change Cover")
                    }
                }
                IconButton(onClick = onPlayAll) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play Category", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// Dialog single menu option helper
@Composable
fun MenuOptionItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AnimatedEqualizerIndicator() {
    Row(
        modifier = androidx.compose.ui.Modifier.size(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
        
        val bar1Height by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 400, delayMillis = 50, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar1"
        )
        val bar2Height by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 350, delayMillis = 100, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar2"
        )
        val bar3Height by infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 450, delayMillis = 0, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar3"
        )

        val primaryColor = MaterialTheme.colorScheme.primary

        Box(
            modifier = androidx.compose.ui.Modifier
                .width(2.5.dp)
                .fillMaxHeight(bar1Height)
                .background(primaryColor, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = androidx.compose.ui.Modifier
                .width(2.5.dp)
                .fillMaxHeight(bar2Height)
                .background(primaryColor, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = androidx.compose.ui.Modifier
                .width(2.5.dp)
                .fillMaxHeight(bar3Height)
                .background(primaryColor, RoundedCornerShape(1.dp))
        )
    }
}

@Composable
fun HiddenSongsPage(
    hiddenSongs: List<Song>,
    isUnlocked: Boolean,
    scrollState: LazyListState,
    lang: String,
    context: android.content.Context,
    viewModel: MusicViewModel,
    onNavigateToPlayer: () -> Unit,
    onSongMenuClick: (Song) -> Unit
) {
    val prefs = remember { context.getSharedPreferences("hidden_settings_prefs", android.content.Context.MODE_PRIVATE) }
    var isPasswordSet by remember { mutableStateOf(!prefs.getString("hidden_password", "").isNullOrEmpty()) }
    val currentSongIndex by viewModel.currentSongIndex.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentQueue by viewModel.currentQueue.collectAsState()
    val keyguardManager = remember { context.getSystemService(android.content.Context.KEYGUARD_SERVICE) as android.app.KeyguardManager }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.setHiddenUnlocked(true)
            android.widget.Toast.makeText(context, LanguageUtils.get("password_saved", lang), android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val resetLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            prefs.edit().remove("hidden_password").apply()
            isPasswordSet = false
            viewModel.setHiddenUnlocked(false)
            android.widget.Toast.makeText(context, LanguageUtils.get("set_password", lang), android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    if (!isPasswordSet) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = LanguageUtils.get("set_password", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            var newPassword by remember { mutableStateOf("") }
            androidx.compose.material3.OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text(if (lang == "fa") "رمز عبور جدید" else "New password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            androidx.compose.material3.Button(
                onClick = {
                    if (newPassword.isNotEmpty()) {
                        prefs.edit().putString("hidden_password", newPassword).apply()
                        isPasswordSet = true
                        viewModel.setHiddenUnlocked(true)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(LanguageUtils.get("save", lang))
            }
        }
    } else if (!isUnlocked) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = LanguageUtils.get("enter_password", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            var enteredPassword by remember { mutableStateOf("") }
            androidx.compose.material3.OutlinedTextField(
                value = enteredPassword,
                onValueChange = { enteredPassword = it },
                label = { Text(if (lang == "fa") "رمز عبور" else "Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.Button(
                onClick = {
                    val correctPassword = prefs.getString("hidden_password", "")
                    if (enteredPassword == correctPassword) {
                        viewModel.setHiddenUnlocked(true)
                    } else {
                        android.widget.Toast.makeText(context, LanguageUtils.get("password_incorrect", lang), android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (lang == "fa") "ورود" else "Unlock")
            }
            
            if (keyguardManager.isDeviceSecure) {
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                            LanguageUtils.get("confirm_device", lang),
                            LanguageUtils.get("auth_required", lang)
                        )
                        if (intent != null) {
                            launcher.launch(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp).padding(end = 4.dp)
                    )
                    Text(LanguageUtils.get("bypass_biometric", lang))
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                                    LanguageUtils.get("confirm_device", lang),
                                    LanguageUtils.get("confirm_to_reset", lang)
                                )
                                if (intent != null) {
                                    resetLauncher.launch(intent)
                                } else {
                                    prefs.edit().remove("hidden_password").apply()
                                    isPasswordSet = false
                                    viewModel.setHiddenUnlocked(false)
                                    android.widget.Toast.makeText(context, "Password cleared", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(LanguageUtils.get("reset_password_menu", lang), fontSize = 12.sp)
                        }

                        androidx.compose.material3.TextButton(
                            onClick = { viewModel.setHiddenUnlocked(false) }
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (lang == "fa") "قفل مجدد" else "Lock", fontSize = 12.sp)
                        }
                    }
                }

                if (hiddenSongs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (lang == "fa") "آهنگی پنهان نشده است" else "No hidden songs yet",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        }
                    }
                } else {
                    items(hiddenSongs) { song ->
                        SongRowItem(
                            song = song,
                            isPlaying = isPlaying && currentQueue.getOrNull(currentSongIndex)?.path == song.path,
                            isMultiSelectMode = false,
                            isSelected = false,
                            onClick = {
                                viewModel.playSongAtIndex(hiddenSongs.indexOf(song), hiddenSongs, playlistName = "hidden_songs")
                                onNavigateToPlayer()
                            },
                            onLongClick = {},
                            onMenuClick = { onSongMenuClick(song) }
                        )
                    }
                }
            }

            FastScrollbar(
                state = scrollState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}
