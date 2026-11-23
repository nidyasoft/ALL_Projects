package com.example.ui.worldclock

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.data.repository.WorldCityPresets
import com.example.data.repository.WorldCityPreset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.TripleCalendarHelper
import com.example.data.model.WorldCityEntity
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import com.example.ui.MainViewModel
import com.example.ui.components.AnalogClockDial
import com.example.ui.components.ConfigurableAnalogClockSection
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OledBackground
import com.example.ui.theme.OledBorder
import com.example.ui.theme.OledSurface
import com.example.ui.theme.OledSurfaceElevated
import com.example.ui.theme.OledSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun WorldClockScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val worldCities by viewModel.worldCities.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val currentTimeMillis by viewModel.currentTimeMillis.collectAsState()
    val isSelectionMode by viewModel.isCitySelectionMode.collectAsState()
    val selectedIds by viewModel.selectedCityIds.collectAsState()
    val settings by viewModel.appSettings.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var activeSlotToChange by remember { mutableStateOf<Int?>(null) }

    // Current local clock decomposition
    val cal = remember(currentTimeMillis) {
        Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    }
    val hours = cal.get(Calendar.HOUR_OF_DAY)
    val minutes = cal.get(Calendar.MINUTE)
    val seconds = cal.get(Calendar.SECOND)
    val millis = cal.get(Calendar.MILLISECOND)

    val hour12 = if (hours == 0) 12 else if (hours > 12) hours - 12 else hours
    val amPm = if (hours >= 12) "PM" else "AM"
    val digitalTimeStr = "${String.format("%02d", hour12)}:${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"

    // Triple Calendar computed for current active time
    val tripleDate = remember(currentTimeMillis, language) {
        TripleCalendarHelper.getTripleDateForMillis(currentTimeMillis, TimeZone.getDefault(), language)
    }

    Scaffold(
        containerColor = OledBackground,
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = NeonCyan,
                    contentColor = OledBackground,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .testTag("add_city_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = Strings.get("add_city", language))
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Section: Customizable Analog Clock Dial
            item {
                Spacer(modifier = Modifier.height(14.dp))
                ConfigurableAnalogClockSection(
                    hours = hours,
                    minutes = minutes,
                    seconds = seconds,
                    milliseconds = millis,
                    settings = settings,
                    language = language
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 2. Digital Clock Display
            item {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = Strings.formatNumber(digitalTimeStr, language),
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = amPm,
                        color = NeonCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 3. Triple Calendar Cards (Solar Hijri, Gregorian, Lunar) - Filtered by User Settings
            val hasAnyDate = settings.showSolarDate || settings.showGregorianDate || settings.showLunarDate
            if (hasAnyDate) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Solar Hijri (شمسی)
                        if (settings.showSolarDate) {
                            TripleCalendarRow(
                                title = Strings.get("calendar_solar", language),
                                dateString = "${tripleDate.weekdayName}، ${Strings.formatNumber(tripleDate.solarDay, language)} ${tripleDate.solarMonthName} ${Strings.formatNumber(tripleDate.solarYear, language)}",
                                accentColor = NeonCyan
                            )
                        }

                        // Gregorian (میلادی)
                        if (settings.showGregorianDate) {
                            TripleCalendarRow(
                                title = Strings.get("calendar_gregorian", language),
                                dateString = "${Strings.formatNumber(tripleDate.gregorianDay, language)} ${tripleDate.gregorianMonthName} ${Strings.formatNumber(tripleDate.gregorianYear, language)}",
                                accentColor = NeonAmber
                            )
                        }

                        // Islamic Lunar (قمری)
                        if (settings.showLunarDate) {
                            TripleCalendarRow(
                                title = Strings.get("calendar_lunar", language),
                                dateString = "${Strings.formatNumber(tripleDate.lunarDay, language)} ${tripleDate.lunarMonthName} ${Strings.formatNumber(tripleDate.lunarYear, language)}",
                                accentColor = NeonPurple
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 4. Dedicated Section: 4 Official World Analog Clocks
            item {
                WorldClockFourAnalogClocksSection(
                    currentTimeMillis = currentTimeMillis,
                    analogCityIds = settings.worldClockAnalogCityIds,
                    settings = settings,
                    language = language,
                    onSelectCityForSlot = { slot ->
                        activeSlotToChange = slot
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 5. Batch Selection Toolbar for Cities
            item {
                AnimatedVisibility(visible = isSelectionMode) {
                    Surface(
                        color = OledSurfaceElevated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.selectAllCities() }) {
                                    Icon(imageVector = Icons.Default.SelectAll, contentDescription = Strings.get("select_all", language), tint = NeonCyan)
                                }
                                Text(
                                    text = String.format(Strings.get("selected_count", language), selectedIds.size),
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                            }
                            Row {
                                IconButton(
                                    onClick = { viewModel.deleteSelectedCities() },
                                    enabled = selectedIds.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = Strings.get("delete", language),
                                        tint = if (selectedIds.isNotEmpty()) NeonRed else TextMuted
                                    )
                                }
                                IconButton(onClick = { viewModel.exitCitySelectionMode() }) {
                                    Text(text = Strings.get("cancel", language), color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 5. Selected Cities List
            itemsIndexed(worldCities, key = { index, city -> if (city.id != 0L) "${city.id}" else "city_$index" }) { index, city ->
                WorldCityItem(
                    city = city,
                    currentTimeMillis = currentTimeMillis,
                    index = index,
                    totalCount = worldCities.size,
                    language = language,
                    isSelectionMode = isSelectionMode,
                    isSelected = selectedIds.contains(city.id),
                    onToggleSelect = { viewModel.toggleCitySelection(city.id) },
                    onLongClick = { viewModel.enterCitySelectionMode(city.id) },
                    onMoveUp = { viewModel.moveCityUp(city) },
                    onMoveDown = { viewModel.moveCityDown(city) }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        AddCityDialog(
            language = language,
            onDismiss = { showAddDialog = false },
            onCitySelected = { city ->
                viewModel.addCity(city.copy(orderIndex = worldCities.size))
                showAddDialog = false
            }
        )
    }

    activeSlotToChange?.let { slotIndex ->
        SelectAnalogCitySheet(
            language = language,
            slotNumber = slotIndex + 1,
            onDismiss = { activeSlotToChange = null },
            onCitySelected = { preset ->
                val currentList = settings.worldClockAnalogCityIds.split(",").toMutableList()
                while (currentList.size < 4) currentList.add("Asia/Tehran")
                currentList[slotIndex] = preset.timezoneId
                viewModel.updateSettings(settings.copy(worldClockAnalogCityIds = currentList.take(4).joinToString(",")))
                activeSlotToChange = null
            }
        )
    }
}

@Composable
fun TripleCalendarRow(
    title: String,
    dateString: String,
    accentColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(OledSurfaceElevated)
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Text(
                text = title,
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = dateString,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun WorldCityItem(
    city: WorldCityEntity,
    currentTimeMillis: Long,
    index: Int,
    totalCount: Int,
    language: AppLanguage,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onLongClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val cityTz = remember(city.timezoneId) { TimeZone.getTimeZone(city.timezoneId) }
    val localTz = remember { TimeZone.getDefault() }

    val cityCal = Calendar.getInstance(cityTz).apply { timeInMillis = currentTimeMillis }
    val cityHours = cityCal.get(Calendar.HOUR_OF_DAY)
    val cityMins = cityCal.get(Calendar.MINUTE)
    val citySecs = cityCal.get(Calendar.SECOND)

    val cityHour12 = if (cityHours == 0) 12 else if (cityHours > 12) cityHours - 12 else cityHours
    val cityAmPm = if (cityHours >= 12) "PM" else "AM"
    val cityTimeStr = "${String.format("%02d", cityHour12)}:${String.format("%02d", cityMins)}:${String.format("%02d", citySecs)}"

    // Calculate time difference between city and local
    val diffMillis = cityTz.getOffset(currentTimeMillis) - localTz.getOffset(currentTimeMillis)
    val diffHours = diffMillis / (1000 * 60 * 60)
    val diffMinutes = Math.abs((diffMillis / (1000 * 60)) % 60)
    val diffStr = if (diffMillis == 0) {
        "هم‌زمان"
    } else {
        val aheadBehind = if (diffMillis > 0) "جلوتر" else "عقب‌تر"
        val hPart = if (Math.abs(diffHours) > 0) "${Math.abs(diffHours)} ساعت" else ""
        val mPart = if (diffMinutes > 0) "$diffMinutes دقیقه" else ""
        "$hPart $mPart $aheadBehind".trim()
    }

    val localizedCityName = when (language) {
        AppLanguage.ENGLISH -> city.cityNameEn
        AppLanguage.ARABIC -> city.cityNameAr
        AppLanguage.AZERBAIJANI -> city.cityNameAz
        else -> city.cityNameFa
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = OledSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .border(
                1.dp,
                if (isSelected) NeonCyan else OledBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable {
                if (isSelectionMode) onToggleSelect() else onLongClick()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelectionMode) {
                    IconButton(onClick = onToggleSelect, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isSelected) NeonCyan else TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Column {
                    Text(
                        text = localizedCityName,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${city.countryName} • ${Strings.formatNumber(diffStr, language)}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = Strings.formatNumber(cityTimeStr, language),
                            color = NeonCyan,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = cityAmPm,
                            color = NeonAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                if (isSelectionMode) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onMoveUp, enabled = index > 0, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, tint = if (index > 0) NeonCyan else TextMuted)
                    }
                    IconButton(onClick = onMoveDown, enabled = index < totalCount - 1, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, tint = if (index < totalCount - 1) NeonCyan else TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun WorldClockFourAnalogClocksSection(
    currentTimeMillis: Long,
    analogCityIds: String,
    settings: com.example.data.local.AppSettings,
    language: AppLanguage,
    onSelectCityForSlot: (Int) -> Unit
) {
    val rawList = analogCityIds.split(",").map { it.trim() }.filter { it.isNotBlank() }
    val defaultList = listOf("Asia/Tehran", "Europe/London", "America/New_York", "Asia/Tokyo")
    val resolvedTzIds = (rawList + defaultList).take(4)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = OledSurface),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, OledBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ساعت‌های آنالوگ ۴گانه جهان",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "لمس ساعت جهت تغییر شهر",
                    color = NeonAmber,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2x2 Grid of Analog Clocks
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AnalogClockMiniCard(
                            slotIndex = 0,
                            timezoneId = resolvedTzIds[0],
                            currentTimeMillis = currentTimeMillis,
                            language = language,
                            settings = settings,
                            onClick = { onSelectCityForSlot(0) }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AnalogClockMiniCard(
                            slotIndex = 1,
                            timezoneId = resolvedTzIds[1],
                            currentTimeMillis = currentTimeMillis,
                            language = language,
                            settings = settings,
                            onClick = { onSelectCityForSlot(1) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AnalogClockMiniCard(
                            slotIndex = 2,
                            timezoneId = resolvedTzIds[2],
                            currentTimeMillis = currentTimeMillis,
                            language = language,
                            settings = settings,
                            onClick = { onSelectCityForSlot(2) }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AnalogClockMiniCard(
                            slotIndex = 3,
                            timezoneId = resolvedTzIds[3],
                            currentTimeMillis = currentTimeMillis,
                            language = language,
                            settings = settings,
                            onClick = { onSelectCityForSlot(3) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnalogClockMiniCard(
    slotIndex: Int,
    timezoneId: String,
    currentTimeMillis: Long,
    language: AppLanguage,
    settings: com.example.data.local.AppSettings,
    onClick: () -> Unit
) {
    val preset = remember(timezoneId) {
        WorldCityPresets.allPresets.find { it.timezoneId.equals(timezoneId, ignoreCase = true) }
            ?: WorldCityPreset(
                cityNameFa = timezoneId.substringAfterLast("/").replace("_", " "),
                cityNameEn = timezoneId.substringAfterLast("/").replace("_", " "),
                cityNameAr = timezoneId.substringAfterLast("/").replace("_", " "),
                cityNameAz = timezoneId.substringAfterLast("/").replace("_", " "),
                countryName = "",
                timezoneId = timezoneId
            )
    }

    val cityName = when (language) {
        AppLanguage.ENGLISH -> preset.cityNameEn
        AppLanguage.ARABIC -> preset.cityNameAr
        AppLanguage.AZERBAIJANI -> preset.cityNameAz
        else -> preset.cityNameFa
    }

    val cal = remember(currentTimeMillis, timezoneId) {
        Calendar.getInstance(TimeZone.getTimeZone(preset.timezoneId)).apply {
            timeInMillis = currentTimeMillis
        }
    }
    val hours = cal.get(Calendar.HOUR_OF_DAY)
    val minutes = cal.get(Calendar.MINUTE)
    val seconds = cal.get(Calendar.SECOND)
    val millis = cal.get(Calendar.MILLISECOND)

    val hour12 = if (hours == 0) 12 else if (hours > 12) hours - 12 else hours
    val amPm = if (hours >= 12) "PM" else "AM"
    val digitalStr = "${String.format("%02d", hour12)}:${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"

    // Time difference relative to device default timezone
    val localTz = TimeZone.getDefault()
    val cityTz = TimeZone.getTimeZone(preset.timezoneId)
    val diffMillis = cityTz.getOffset(currentTimeMillis) - localTz.getOffset(currentTimeMillis)
    val diffHours = diffMillis / (1000 * 3600)
    val diffMins = Math.abs((diffMillis / (1000 * 60)) % 60)
    val diffSign = if (diffMillis >= 0) "+" else "-"
    val diffText = if (diffMillis == 0) "هم‌زمان با محلی" else "$diffSign$diffHours:${String.format("%02d", diffMins)}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = OledSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, OledBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // City name and edit badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = cityName,
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Analog Clock Dial for this City
            AnalogClockDial(
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                milliseconds = millis,
                dialSize = 100.dp,
                faceColor = androidx.compose.ui.graphics.Color(settings.clockFaceColor),
                hourHandColor = androidx.compose.ui.graphics.Color(settings.hourHandColor),
                minuteHandColor = androidx.compose.ui.graphics.Color(settings.minuteHandColor),
                secondHandColor = androidx.compose.ui.graphics.Color(settings.secondHandColor),
                numberColor = androidx.compose.ui.graphics.Color(settings.numberColor),
                showNumbers = settings.showNumbers
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Digital time & AM/PM
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = Strings.formatNumber(digitalStr, language),
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = amPm,
                    color = NeonAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Offset text
            Text(
                text = Strings.formatNumber(diffText, language),
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectAnalogCitySheet(
    language: AppLanguage,
    slotNumber: Int,
    onDismiss: () -> Unit,
    onCitySelected: (WorldCityPreset) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filtered = remember(searchQuery, language) {
        if (searchQuery.isBlank()) {
            WorldCityPresets.allPresets
        } else {
            val q = searchQuery.trim().lowercase()
            WorldCityPresets.allPresets.filter {
                it.cityNameFa.lowercase().contains(q) ||
                it.cityNameEn.lowercase().contains(q) ||
                it.cityNameAr.lowercase().contains(q) ||
                it.countryName.lowercase().contains(q) ||
                it.timezoneId.lowercase().contains(q)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = OledSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "انتخاب شهر برای ساعت آنالوگ شماره $slotNumber",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("جستجوی نام شهر، پایتخت یا کشور...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = OledBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = OledSurfaceElevated,
                    unfocusedContainerColor = OledSurfaceElevated
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { "${it.cityNameEn}_${it.timezoneId}" }) { preset ->
                    val displayName = when (language) {
                        AppLanguage.ENGLISH -> preset.cityNameEn
                        AppLanguage.ARABIC -> preset.cityNameAr
                        AppLanguage.AZERBAIJANI -> preset.cityNameAz
                        else -> preset.cityNameFa
                    }
                    val cal = Calendar.getInstance(TimeZone.getTimeZone(preset.timezoneId))
                    val h = cal.get(Calendar.HOUR_OF_DAY)
                    val m = cal.get(Calendar.MINUTE)
                    val timeStr = String.format("%02d:%02d", h, m)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCitySelected(preset) },
                        colors = CardDefaults.cardColors(containerColor = OledSurfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OledBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = displayName,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${preset.cityNameEn} • ${preset.countryName} (${preset.timezoneId})",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = Strings.formatNumber(timeStr, language),
                                color = NeonAmber,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
