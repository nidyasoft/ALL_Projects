package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Aurora Glassmorphic Interactive Application Guide Dialog
 * Inspired by Template 2: Glassmorphic Aurora
 * Comprehensive 2026 Schematic Overview of All Player Features
 */
@Composable
fun AppGuideDialog(
    lang: String,
    onDismiss: () -> Unit
) {
    var selectedSectionIndex by remember { mutableIntStateOf(0) }
    val guideScrollState = rememberLazyListState()

    val sections = listOf(
        GuideSection(
            id = "overview",
            titleFa = "۱. منوی ساندویچی و جستجو",
            titleEn = "1. Sandwich Menu & Search",
            icon = Icons.Default.Menu,
            badgeFa = "منو و نویگیشن",
            badgeEn = "Drawer"
        ),
        GuideSection(
            id = "tabs",
            titleFa = "۲. ستون‌های ۸گانه اصلی",
            titleEn = "2. 8 Core Screen Columns",
            icon = Icons.Default.ViewColumn,
            badgeFa = "۸ ستون",
            badgeEn = "8 Columns"
        ),
        GuideSection(
            id = "batch",
            titleFa = "۳. انتخاب چندتایی فایل‌ها",
            titleEn = "3. Multi-Select Actions",
            icon = Icons.Default.Checklist,
            badgeFa = "عملیات گروهی",
            badgeEn = "Bulk"
        ),
        GuideSection(
            id = "player",
            titleFa = "۴. صفحه پخش و منوی ۳نقطه",
            titleEn = "4. Player & 3-Dot Menu",
            icon = Icons.Default.PlayCircle,
            badgeFa = "صفحه پخش",
            badgeEn = "Now Playing"
        ),
        GuideSection(
            id = "cutter",
            titleFa = "۵. برش پیشرفته موسیقی",
            titleEn = "5. Epic Music Cutter",
            icon = Icons.Default.ContentCut,
            badgeFa = "ویرایش صدا",
            badgeEn = "Audio Trim"
        ),
        GuideSection(
            id = "scrollbar",
            titleFa = "۶. اسکرول‌بار هوشمند سریع",
            titleEn = "6. Smart Fast Scrollbar",
            icon = Icons.Default.SwapVert,
            badgeFa = "جدید ۲۰۲۶",
            badgeEn = "New 2026"
        ),
        GuideSection(
            id = "themes",
            titleFa = "۷. تم‌های ۷گانه و زبان‌ها",
            titleEn = "7. Themes & Languages",
            icon = Icons.Default.Palette,
            badgeFa = "شخصی‌سازی",
            badgeEn = "Styling"
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.78f))
                .padding(horizontal = 12.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glassmorphic Aurora Card Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.94f)
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF38BDF8).copy(alpha = 0.85f),
                                Color(0xFF818CF8).copy(alpha = 0.55f),
                                Color(0xFFC026D3).copy(alpha = 0.65f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F172A).copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Aurora glowing backdrop circles
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF0284C7).copy(alpha = 0.30f), Color.Transparent),
                                center = Offset(size.width * 0.15f, size.height * 0.12f),
                                radius = size.width * 0.65f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF7C3AED).copy(alpha = 0.28f), Color.Transparent),
                                center = Offset(size.width * 0.85f, size.height * 0.88f),
                                radius = size.width * 0.75f
                            )
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Header Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(Color(0xFF0284C7), Color(0xFF818CF8))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (lang == "fa") "راهنمای جامع شماتیک اپلیکیشن" else "Schematic App Guide",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (lang == "fa") "امکانات، منوها، ستون‌ها و ابزارهای صوتی ۲۰۲۶" else "All Features, Columns, Tools & 2026 Engine",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Category Chips Horizontal Scrollable Row (Glass Aurora Tabs)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(sections) { idx, section ->
                                val isSelected = selectedSectionIndex == idx
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isSelected) {
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFF0284C7), Color(0xFF6366F1))
                                                )
                                            } else {
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color.White.copy(alpha = 0.08f),
                                                        Color.White.copy(alpha = 0.04f)
                                                    )
                                                )
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable { selectedSectionIndex = idx }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = section.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (lang == "fa") section.titleFa else section.titleEn,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Content Body for Selected Section with fast scrollbar
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1E293B).copy(alpha = 0.72f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.12f)
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(
                                    state = guideScrollState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    item {
                                        when (selectedSectionIndex) {
                                            0 -> SectionSandwichAndSearch(lang)
                                            1 -> SectionEightColumns(lang)
                                            2 -> SectionBatchSelectActions(lang)
                                            3 -> SectionNowPlayingAnd3Dot(lang)
                                            4 -> SectionEpicMusicCutterGuide(lang)
                                            5 -> SectionFastScrollbarGuide(lang)
                                            6 -> SectionThemesAndLanguagesGuide(lang)
                                        }
                                    }
                                }

                                FastScrollbar(
                                    state = guideScrollState,
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bottom Dismiss Button
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0284C7)
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "fa") "متوجه شدم و آماده‌ام" else "Got it! Back to App",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class GuideSection(
    val id: String,
    val titleFa: String,
    val titleEn: String,
    val icon: ImageVector,
    val badgeFa: String,
    val badgeEn: String
)

@Composable
private fun SectionSandwichAndSearch(lang: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SchematicCardHeader(
            title = if (lang == "fa") "منوی ساندویچی (سمت چپ) و دکمه جستجوی بازشونده" else "Left Sandwich Drawer & Collapsible Search",
            icon = Icons.Default.Menu
        )

        SchematicStepItem(
            step = "☰",
            title = if (lang == "fa") "منوی ساندویچی کشویی (سمت چپ صفحه اصلی)" else "Sandwich Navigation Drawer (Left Side)",
            desc = if (lang == "fa") "آیکون ساندویچی در بالای صفحه سمت چپ، کشوی شیشه‌ای Aurora را باز می‌کند و شامل گزینه‌های «راهنمای جامع»، «تغییر زبان»، «پوسته‌ها و تم‌ها»، «اشتراک‌گذاری فایل APK»، «برش موسیقی» و «اکولایزر» است."
            else "The sandwich hamburger icon on top-left opens the Aurora glass drawer with Quick Access to Guide, Languages, Themes, App Sharing, Music Cutter, and Equalizer.",
            icon = Icons.Default.MenuOpen
        )

        SchematicStepItem(
            step = "🔍",
            title = if (lang == "fa") "دکمه جستجوی بازشونده هوشمند (Collapsible Search)" else "Collapsible Expandable Search Button",
            desc = if (lang == "fa") "به جای اشغال دائمی بالای صفحه، جستجو به صورت یک دکمه زیبا طراحی شده است؛ با لمس آن نوار جستجوی اختصاصی با افکت حرکتی در بالا باز شده و امکان فیلتر آنی آهنگ‌ها و خواننده‌ها را فراهم می‌کند."
            else "Tapping the Search icon smoothly expands a top search bar with auto-focus, real-time filtering and a quick clear/close button.",
            icon = Icons.Default.Search
        )

        SchematicStepItem(
            step = "❓",
            title = if (lang == "fa") "دکمه کوچک راهنما در بالای صفحه اصلی" else "Dedicated Quick Guide Button",
            desc = if (lang == "fa") "یک دکمه کوچک با نشانگر درخشان در بالای صفحه تعبیه شده تا با یک کلیک همین راهنمای مصور را در اختیارتان قرار دهد."
            else "A compact guide button with a glowing badge sits at the top for instant one-touch access to this manual.",
            icon = Icons.Default.HelpOutline
        )

        SchematicStepItem(
            step = "📶",
            title = if (lang == "fa") "دکمه مرتب‌سازی و فیلتر پیشرفته" else "Sort & Re-ordering Engine",
            desc = if (lang == "fa") "امکان مرتب‌سازی صعودی و نزولی بر پایه زمان اضافه شدن، نام، خواننده، آلبوم، مدت زمان، تعداد دفعات پخش و حجم فایل."
            else "Ascending/Descending sorting by Date Added, Title, Artist, Album, Duration, Play Counts and File Size.",
            icon = Icons.Default.Sort
        )
    }
}

@Composable
private fun SectionEightColumns(lang: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SchematicCardHeader(
            title = if (lang == "fa") "ستون‌های ۸گانه بالای صفحه اصلی و مفاهیم آن‌ها" else "The 8 Core Screen Columns Explained",
            icon = Icons.Default.ViewColumn
        )

        Text(
            text = if (lang == "fa") "نوار لغزان بالای صفحه شامل ۸ ستون هماهنگ با قابلیت کشیدن (Swipe) و اسکرول سریع است:"
            else "Top scrollable tab bar hosts 8 dedicated columns with smooth gesture paging:",
            fontSize = 12.sp,
            color = Color(0xFFCBD5E1)
        )

        val columns = listOf(
            Triple(if (lang == "fa") "۱. آهنگ‌ها (Tracks)" else "1. Tracks", if (lang == "fa") "فهرست کامل تمام موزیک‌های شناسایی شده از حافظه دستگاه با امکان جستجو و مرتب‌سازی دقیق." else "Complete library of all indexed songs with sorting & quick play.", Icons.Default.MusicNote),
            Triple(if (lang == "fa") "۲. علاقه‌مندی‌ها (Favorites)" else "2. Favorites", if (lang == "fa") "آهنگ‌هایی که با آیکون قلب لایک شده‌اند؛ برای دسترسی سریع به برترین آهنگ‌های دلخواه شما." else "Loved songs marked with a heart icon for instant personal access.", Icons.Default.Favorite),
            Triple(if (lang == "fa") "۳. لیست‌های پخش (Playlists)" else "3. Playlists", if (lang == "fa") "ایجاد و نام‌گذاری پلی‌لیست‌های دلخواه، خروجی گرفتن متنی/M3U و مدیریت قطعات هر پلی‌لیست." else "Create custom playlists, export playlists and re-order tracks.", Icons.Default.PlaylistPlay),
            Triple(if (lang == "fa") "۴. آلبوم‌ها (Albums)" else "4. Albums", if (lang == "fa") "دسته‌بندی خودکار موزیک‌ها بر اساس آلبوم با امکان پخش یکباره و تغییر کاور دلخواه." else "Auto-grouped by album metadata with custom artwork replacement.", Icons.Default.Album),
            Triple(if (lang == "fa") "۵. هنرمندان (Artists)" else "5. Artists", if (lang == "fa") "تفکیک آثار بر اساس نام هر خواننده به همراه تعداد آهنگ‌های موجود از آن خواننده." else "Grouped by singer/artist with individual song counts.", Icons.Default.Person),
            Triple(if (lang == "fa") "۶. سبک‌ها (Genres)" else "6. Genres", if (lang == "fa") "تفکیک بر پایه سبک موسیقی (پاپ، راک، کلاسیک، سنتی، الکترونیک و...)." else "Music genres categorization (Pop, Rock, Classical, Traditional, etc.).", Icons.Default.Radio),
            Triple(if (lang == "fa") "۷. پوشه‌ها (Folders)" else "7. Folders", if (lang == "fa") "مرور مستقیم پوشه‌های واقعی دستگاه (دانلودها، ضبط صدا، تلگرام، واتساپ، موزیک)." else "Direct filesystem browsing of device folders (Music, Downloads, etc.).", Icons.Default.Folder),
            Triple(if (lang == "fa") "۸. آهنگ‌های مخفی (Hidden Songs)" else "8. Hidden Songs", if (lang == "fa") "صندوق امنیتی قفل‌شده با رمز عبور و سنسور اثر انگشت بیومتریک برای مخفی‌سازی موزیک‌های شخصی." else "Secure password & biometric fingerprint locked vault for hidden files.", Icons.Default.Lock)
        )

        columns.forEach { (title, desc, icon) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7).copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(17.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = desc, fontSize = 11.sp, color = Color(0xFFCBD5E1), lineHeight = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun SectionBatchSelectActions(lang: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SchematicCardHeader(
            title = if (lang == "fa") "انتخاب چندتایی فایل‌ها و امکانات عملیات گروهی" else "Multi-Selection Mode & Batch Operations",
            icon = Icons.Default.Checklist
        )

        SchematicStepItem(
            step = "👆",
            title = if (lang == "fa") "نحوه فعال‌سازی انتخاب چندتایی" else "Activation Trigger",
            desc = if (lang == "fa") "با لمس طولانی (Long Press) روی هر آهنگ در هر یک از ستون‌ها، حالت انتخاب گروهی فعال شده و نوار ابزار مخصوص در بالای صفحه ظاهر می‌شود."
            else "Long-press any song item across any tab to activate Multi-Select mode and reveal the batch action bar.",
            icon = Icons.Default.TouchApp
        )

        Text(
            text = if (lang == "fa") "امکانات موجود در نوار عملیات گروهی:" else "Batch Action Bar Capabilities:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8)
        )

        val batchItems = listOf(
            Pair(if (lang == "fa") "⏩ پخش بعدی (Bulk Play Next)" else "Play Next", if (lang == "fa") "افزودن تمام فایل‌های انتخاب شده به عنوان آهنگ‌های بعدی صف پخش جاری." else "Queues selected files to play immediately after the current song."),
            Pair(if (lang == "fa") "➕ افزودن به صف (Bulk Add to Queue)" else "Add to Queue", if (lang == "fa") "اضافه کردن همزمان به انتهای صف آهنگ‌ها بدون قطع موسیقی فعلی." else "Appends all selected songs to the bottom of the active queue."),
            Pair(if (lang == "fa") "📁 افزودن به پلی‌لیست (Bulk Add Playlist)" else "Add to Playlist", if (lang == "fa") "انتقال یکباره تمام آهنگ‌های نشان شده به یکی از لیست‌های پخش دلخواه." else "Adds all selected tracks at once into any chosen playlist."),
            Pair(if (lang == "fa") "📤 اشتراک‌گذاری دسته‌ای (Bulk Share)" else "Batch Share", if (lang == "fa") "ارسال و اشتراک‌گذاری همزمان چندین فایل صوتی به تلگرام، واتساپ یا بلوتوث." else "Shares multiple audio files concurrently via system share sheet."),
            Pair(if (lang == "fa") "🗑️ حذف گروهی (Bulk Delete)" else "Batch Delete", if (lang == "fa") "پاکسازی همزمان فایل‌های انتخاب شده از دیسک و دیتابیس." else "Deletes selected audio files from storage in one click.")
        )

        batchItems.forEach { (title, desc) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = desc, fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }
        }
    }
}

@Composable
private fun SectionNowPlayingAnd3Dot(lang: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SchematicCardHeader(
            title = if (lang == "fa") "المان‌های صفحه پخش موسیقی، تنظیمات و منوی ۳نقطه" else "Now Playing Elements, Controls & 3-Dot Menu",
            icon = Icons.Default.PlayCircle
        )

        SchematicStepItem(
            step = "💿",
            title = if (lang == "fa") "صفحه گرامافون دوار سه‌بعدی و کاور" else "Spinning Vinyl Record & High-Res Art",
            desc = if (lang == "fa") "نمایش گرامافون کلاسیک چرخنده با سوزن واقعی، افکت لرزش و ویژوالایزر موجی دینامیک که همگام با بیس و فرکانس آهنگ می‌تپد."
            else "Animated 3D vinyl record with realistic tonearm, soundwave visualizers and high-res cover art.",
            icon = Icons.Default.GraphicEq
        )

        SchematicStepItem(
            step = "🎛️",
            title = if (lang == "fa") "کنترل‌های پخش و تایمر خواب" else "Playback Controls & Sleep Timer",
            desc = if (lang == "fa") "نوار پیشرفت Seekbar دقیق، دکمه‌های پرش ۱۰ ثانیه‌ای به جلو و عقب، حالت‌های تکرار (تک آهنگ/همه)، پخش تصادفی (Shuffle) و تایمر خواب هوشمند."
            else "Precise seekbar, 10s skip buttons, Repeat modes, Shuffle, and Sleep Timer with song-end auto-off.",
            icon = Icons.Default.Tune
        )

        SchematicStepItem(
            step = "⋮",
            title = if (lang == "fa") "منوی ۳نقطه بالای صفحه پخش موسیقی" else "3-Dot Overflow Menu (Now Playing)",
            desc = if (lang == "fa") "شامل: «برش پیشرفته موسیقی (Epic Cutter)»، «اکولایزر ۱۰ بانده»، «تغییر کاور با ۵ گرادیان خیره‌کننده»، «ویرایش تگ‌ها (Title, Artist, Album)»، «تنظیم مستقیم به عنوان زنگ گوشی» و «نمایش متادیتا و جزئیات حجم و بیت‌ریت»."
            else "Includes: Epic Music Cutter, 10-Band Equalizer, Custom Cover Picker, ID3 Tag Editor, Set as Ringtone, and File Metadata Specs.",
            icon = Icons.Default.MoreVert
        )

        SchematicStepItem(
            step = "📝",
            title = if (lang == "fa") "شعر و متن ترانه (Interactive Lyrics)" else "Lyrics Editor & Synchronizer",
            desc = if (lang == "fa") "امکان مشاهده متن ترانه با اسکرول نرم و امکان ویرایش یا افزودن ترانه دلخواه برای هر آهنگ با ذخیره خودکار."
            else "Interactive scrolling lyrics view with in-app text editor and automatic persistence.",
            icon = Icons.Default.Description
        )
    }
}

@Composable
private fun SectionEpicMusicCutterGuide(lang: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SchematicCardHeader(
            title = if (lang == "fa") "استودیو برش پیشرفته موسیقی (Epic Music Cutter)" else "Epic Music Cutter Studio & Trimming Engine",
            icon = Icons.Default.ContentCut
        )

        SchematicStepItem(
            step = "📍",
            title = if (lang == "fa") "دسترسی آسان از منوی هر آهنگ در تمام ستون‌ها" else "Available on Every Song Menu & Player Screen",
            desc = if (lang == "fa") "گزینه «برش پیشرفته موسیقی» هم در منوی ۳نقطه کنار هر آهنگ در لیست‌های صفحه اصلی و هم در منوی تنظیمات صفحه پخش قرار دارد و آهنگ انتخابی را فوراً برای برش آماده می‌کند."
            else "Accessible from both the 3-dot menu of any song in home columns and inside the player screen, loading the exact chosen song instantly.",
            icon = Icons.Default.ContentCut
        )

        SchematicStepItem(
            step = "〰️",
            title = if (lang == "fa") "نمودار شکل موج صوتی (Waveform) با قابلیت زوم" else "Interactive Waveform with Zoom Multipliers",
            desc = if (lang == "fa") "امکان زوم تا ۴ برابر روی نمودار موج صدا برای پیدا کردن بهترین بیت و برش میلی‌ثانیه‌ای با دقت بسیار بالا."
            else "Multi-level waveform zoom up to 4x for pinpoint beat alignment and millisecond-accurate audio clipping.",
            icon = Icons.Default.GraphicEq
        )

        SchematicStepItem(
            step = "⏱️",
            title = if (lang == "fa") "فیلدهای عددی دقیق (ساعت، دقیقه، ثانیه، میلی‌ثانیه)" else "Precise Numerical Input Fields",
            desc = if (lang == "fa") "علاوه بر اسلایدرهای لمسی، ۴ فیلد عددی برای تایپ دستی نقطه شروع و پایان تعبیه شده است."
            else "Full numerical inputs for Hour, Minute, Second, and Millisecond alongside drag sliders.",
            icon = Icons.Default.Timer
        )

        SchematicStepItem(
            step = "🎚️",
            title = if (lang == "fa") "افکت‌های Fade In/Out و ضریب سرعت" else "Fade In/Out DSP & Speed Adjuster",
            desc = if (lang == "fa") "اعمال شیب محو شدن صوتی در ابتدای برش (ورود ملایم) و انتهای برش، به همراه تغییر تمپو و سرعت از ۰.۵ تا ۲ برابر."
            else "Smooth audio slope blending for fade-in/out transitions and variable tempo multiplier from 0.5x to 2.0x.",
            icon = Icons.Default.Speed
        )

        SchematicStepItem(
            step = "💾",
            title = if (lang == "fa") "خروجی با کیفیت و ذخیره در پوشه Music دستگاه" else "High-Quality Export to Device Storage",
            desc = if (lang == "fa") "پشتیبانی از فرمت‌های MP3 و AAC/M4A با نام‌گذاری دلخواه و اسکن خودکار مدیا تا فایل جدید بلافاصله در لیست آهنگ‌ها نمایش داده شود."
            else "Supports MP3 and AAC exports with custom naming and instant MediaScanner refresh so the new clip appears right away.",
            icon = Icons.Default.SaveAlt
        )
    }
}

@Composable
private fun SectionFastScrollbarGuide(lang: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SchematicCardHeader(
            title = if (lang == "fa") "اسکرول‌بار هوشمند شناور با قابلیت اسکرول سریع (Fast Scroll)" else "Smart Auto-Hiding Fast Scrollbar (New 2026)",
            icon = Icons.Default.SwapVert
        )

        SchematicStepItem(
            step = "👁️",
            title = if (lang == "fa") "ظاهر شدن خودکار هنگام اسکرول" else "Auto-Appears Upon Scrolling",
            desc = if (lang == "fa") "در هر بخشی از برنامه که لیست قابل اسکرول وجود دارد، به محض حرکت دست کاربر، اسکرول‌بار شیشه‌ای با افکت درخشان ظاهر می‌شود."
            else "Across all scrollable screens, the glowing glass scrollbar capsule smoothly becomes visible as soon as you scroll.",
            icon = Icons.Default.Visibility
        )

        SchematicStepItem(
            step = "⏳",
            title = if (lang == "fa") "ماندگاری ۳ ثانیه‌ای و سپس محو شدن نرم" else "3-Second Fade Timeout",
            desc = if (lang == "fa") "پس از توقف اسکرول، دستگیره اسکرول بار دقیقاً تا ۳ ثانیه روی صفحه باقی می‌ماند تا کاربر بتواند از آن استفاده کند و سپس به آرامی محو می‌شود."
            else "Stays visible for exactly 3 seconds after scrolling stops, then performs a graceful fade-out to keep the interface clean.",
            icon = Icons.Default.HourglassBottom
        )

        SchematicStepItem(
            step = "🚀",
            title = if (lang == "fa") "جابجایی فوق‌سریع در لیست با کشیدن دستگیره" else "High-Speed List Scrubbing",
            desc = if (lang == "fa") "کاربر می‌تواند دستگیره اسکرول‌بار را لمس کرده و با کشیدن آن در چند ثانیه از بین صدها یا هزاران آهنگ از بالای لیست به انتهای آن بپرد."
            else "Touch and drag the capsule thumb to instantly scrub through hundreds of songs from top to bottom.",
            icon = Icons.Default.TouchApp
        )
    }
}

@Composable
private fun SectionThemesAndLanguagesGuide(lang: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SchematicCardHeader(
            title = if (lang == "fa") "تم‌های ۷گانه مدرن، حالت روز/شب و زبان‌ها" else "7 Design Themes, Dark/Light Mode & Languages",
            icon = Icons.Default.Palette
        )

        SchematicStepItem(
            step = "🎨",
            title = if (lang == "fa") "۷ پوسته گرافیکی اختصاصی (7 Modern Themes)" else "7 Distinct Visual Themes",
            desc = if (lang == "fa") "۱. Glassmorphic Aurora (شیشه‌ای درخشان قطبی)\n۲. Fusion Gold (لوکس طلایی مشکی)\n۳. Cyberpunk Neon (صورتی و فیروزه‌ای نئونی)\n۴. Midnight OLED (مشکی مطلق با کنتراست بالا)\n۵. Forest Emerald (سبز زمردی ارگانیک)\n۶. Deep Ocean (آبی ژرف اقیانوسی)\n۷. Royal Purple (بنفش سلطنتی کیهانی)"
            else "Includes Glassmorphic Aurora, Fusion Gold, Cyberpunk Neon, Midnight OLED, Forest Emerald, Deep Ocean, and Royal Purple.",
            icon = Icons.Default.ColorLens
        )

        SchematicStepItem(
            step = "🌗",
            title = if (lang == "fa") "حالت تاریک، روشن یا هماهنگ با سیستم" else "Dark, Light & System Sync",
            desc = if (lang == "fa") "امکان انتخاب دستی تم تیره یا روشن یا پیروی خودکار از تم فعال سیستم عامل اندروید."
            else "Manual Dark Mode, Light Mode, or automatic System Theme follow.",
            icon = Icons.Default.Brightness4
        )

        SchematicStepItem(
            step = "🌍",
            title = if (lang == "fa") "پشتیبانی از ۳ زبان کامل" else "Tri-Lingual Engine",
            desc = if (lang == "fa") "پشتیبانی بومی از زبان‌های فارسی (با چیدمان راست‌به‌چپ RTL)، ترکی آذربایجانی و انگلیسی در تمام بخش‌ها و پیام‌ها."
            else "Native Persian (RTL), Azerbaijani, and English translations across all menus and dialogs.",
            icon = Icons.Default.Language
        )
    }
}

@Composable
private fun SchematicCardHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0284C7).copy(alpha = 0.35f), Color(0xFF6366F1).copy(alpha = 0.15f))
                )
            )
            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}

@Composable
private fun SchematicStepItem(
    step: String,
    title: String,
    desc: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF0284C7), Color(0xFF6366F1))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = step, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = desc, fontSize = 11.sp, color = Color(0xFFCBD5E1), lineHeight = 16.sp)
        }
    }
}
