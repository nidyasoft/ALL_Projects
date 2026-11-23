package com.example.ui.calendar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.TripleCalendarHelper
import com.example.calendar.TripleDate
import com.example.data.model.AlarmEntity
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import com.example.ui.MainViewModel
import com.example.ui.alarm.AddEditAlarmSheet
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarEventsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val language by viewModel.currentLanguage.collectAsState()
    val alarms by viewModel.alarms.collectAsState()

    // Calendar system: "SOLAR" (شمسی), "GREGORIAN" (میلادی), "LUNAR" (قمری)
    var activeCalendar by remember { mutableStateOf("SOLAR") }

    val todayTriple = remember { TripleCalendarHelper.getTodayTripleDate(language) }

    // Active displayed Year and Month (preserved when switching tabs)
    var displayYear by remember { mutableIntStateOf(todayTriple.solarYear) }
    var displayMonth by remember { mutableIntStateOf(todayTriple.solarMonth) }

    // Selected Day in the active calendar
    var selectedDay by remember { mutableIntStateOf(todayTriple.solarDay) }

    // Modal Sheet to Add/Edit alarm or meeting on the selected date
    var isAddingEventOnDate by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }
    var pendingDisableAlarm by remember { mutableStateOf<Pair<AlarmEntity, Long>?>(null) }

    // Convert currently selected day into TripleDate
    val selectedDayTriple: TripleDate = remember(activeCalendar, displayYear, displayMonth, selectedDay) {
        val (gy, gm, gd) = when (activeCalendar) {
            "SOLAR" -> TripleCalendarHelper.solarToGregorian(displayYear, displayMonth, selectedDay)
            "LUNAR" -> TripleCalendarHelper.lunarToGregorian(displayYear, displayMonth, selectedDay)
            else -> Triple(displayYear, displayMonth, selectedDay)
        }
        val cal = Calendar.getInstance().apply {
            set(gy, gm - 1, gd)
        }
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        TripleCalendarHelper.calculateTripleDate(gy, gm, gd, dow, language)
    }

    // Month name display string
    val monthName = remember(activeCalendar, displayMonth, language) {
        when (activeCalendar) {
            "SOLAR" -> TripleCalendarHelper.solarMonthNamesFa.getOrElse(displayMonth - 1) { "" }
            "GREGORIAN" -> TripleCalendarHelper.gregorianMonthNamesFa.getOrElse(displayMonth - 1) { "" }
            else -> TripleCalendarHelper.lunarMonthNamesFa.getOrElse(displayMonth - 1) { "" }
        }
    }

    val daysInMonth = remember(activeCalendar, displayYear, displayMonth) {
        TripleCalendarHelper.getDaysInMonth(activeCalendar, displayYear, displayMonth)
    }

    // Calculate weekday of the 1st day of this month
    // In Solar/Lunar: Saturday is day 0, Sunday is 1, ... Friday is 6
    // In Gregorian: Sunday is day 0, ... Saturday is 6
    val leadingOffset = remember(activeCalendar, displayYear, displayMonth) {
        val (gy, gm, gd) = when (activeCalendar) {
            "SOLAR" -> TripleCalendarHelper.solarToGregorian(displayYear, displayMonth, 1)
            "LUNAR" -> TripleCalendarHelper.lunarToGregorian(displayYear, displayMonth, 1)
            else -> Triple(displayYear, displayMonth, 1)
        }
        val cal = Calendar.getInstance().apply { set(gy, gm - 1, gd) }
        val dow = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon, ..., 7=Sat
        if (activeCalendar == "GREGORIAN") {
            dow - 1
        } else {
            // Saturday is first column
            if (dow == Calendar.SATURDAY) 0 else dow
        }
    }

    // Helper to check if a specific day in this month has scheduled events or alarms
    fun getAlarmsForDay(day: Int): List<AlarmEntity> {
        val (gy, gm, gd) = when (activeCalendar) {
            "SOLAR" -> TripleCalendarHelper.solarToGregorian(displayYear, displayMonth, day)
            "LUNAR" -> TripleCalendarHelper.lunarToGregorian(displayYear, displayMonth, day)
            else -> Triple(displayYear, displayMonth, day)
        }
        val cal = Calendar.getInstance().apply {
            set(gy, gm - 1, gd, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayDow = cal.get(Calendar.DAY_OF_WEEK)

        val daysMap = mapOf(
            Calendar.SATURDAY to "SAT",
            Calendar.SUNDAY to "SUN",
            Calendar.MONDAY to "MON",
            Calendar.TUESDAY to "TUE",
            Calendar.WEDNESDAY to "WED",
            Calendar.THURSDAY to "THU",
            Calendar.FRIDAY to "FRI"
        )
        val dowCode = daysMap[dayDow] ?: ""

        return alarms.filter { alarm ->
            when (alarm.repeatType) {
                "EVENT", "MEETING" -> {
                    val (eGy, eGm, eGd) = when (alarm.eventCalendar.uppercase()) {
                        "SOLAR" -> TripleCalendarHelper.solarToGregorian(alarm.eventYear, alarm.eventMonth, alarm.eventDay)
                        "LUNAR" -> TripleCalendarHelper.lunarToGregorian(alarm.eventYear, alarm.eventMonth, alarm.eventDay)
                        else -> Triple(alarm.eventYear, alarm.eventMonth, alarm.eventDay)
                    }
                    eGy == gy && eGm == gm && eGd == gd
                }
                "DAILY" -> true
                "SAT_THU" -> dayDow != Calendar.FRIDAY
                "MON_FRI" -> dayDow in Calendar.MONDAY..Calendar.FRIDAY
                "CUSTOM" -> alarm.customDays.contains(dowCode)
                "SHIFT" -> {
                    // Check if shift lands on this day
                    val startMillis = TripleCalendarHelper.getMillisFromCalendar(
                        alarm.shiftAnchorCalendar,
                        alarm.shiftAnchorYear,
                        alarm.shiftAnchorMonth,
                        alarm.shiftAnchorDay,
                        alarm.hour,
                        alarm.minute
                    )
                    val checkDayStart = Calendar.getInstance().apply {
                        set(gy, gm - 1, gd, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val checkDayEnd = checkDayStart + (24L * 3600 * 1000L)
                    val intervalVal = alarm.shiftIntervalValue.coerceAtLeast(1)
                    val intervalMs: Long = when (alarm.shiftIntervalUnit) {
                        "MINUTES" -> intervalVal * 60 * 1000L
                        "HOURS" -> intervalVal * 3600 * 1000L
                        else -> intervalVal * 24L * 3600 * 1000L
                    }
                    if (checkDayEnd < startMillis) {
                        false
                    } else {
                        val diff = checkDayStart - startMillis
                        val remainder = if (diff >= 0) diff % intervalMs else (intervalMs - ((-diff) % intervalMs))
                        remainder < (24L * 3600 * 1000L)
                    }
                }
                else -> false
            }
        }
    }

    val selectedDayAlarms = remember(alarms, activeCalendar, displayYear, displayMonth, selectedDay) {
        getAlarmsForDay(selectedDay)
    }

    Scaffold(
        containerColor = OledBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "تقویم رویدادها و جلسات",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = NeonCyan
                        )
                    }
                },
                actions = {
                    // Quick Jump to Today
                    OutlinedButton(
                        onClick = {
                            when (activeCalendar) {
                                "SOLAR" -> {
                                    displayYear = todayTriple.solarYear
                                    displayMonth = todayTriple.solarMonth
                                    selectedDay = todayTriple.solarDay
                                }
                                "GREGORIAN" -> {
                                    displayYear = todayTriple.gregorianYear
                                    displayMonth = todayTriple.gregorianMonth
                                    selectedDay = todayTriple.gregorianDay
                                }
                                else -> {
                                    displayYear = todayTriple.lunarYear
                                    displayMonth = todayTriple.lunarMonth
                                    selectedDay = todayTriple.lunarDay
                                }
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(34.dp)
                    ) {
                        Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("امروز", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OledSurfaceElevated)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }

            // 1. Triple Calendar Selector Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "SOLAR" to "☀️ تقویم شمسی",
                        "GREGORIAN" to "🌍 تقویم میلادی",
                        "LUNAR" to "🌙 تقویم قمری"
                    ).forEach { (calKey, calLabel) ->
                        val isSelected = activeCalendar == calKey
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (activeCalendar != calKey) {
                                        val currentSelected = selectedDayTriple
                                        activeCalendar = calKey
                                        when (calKey) {
                                            "SOLAR" -> {
                                                displayYear = currentSelected.solarYear
                                                displayMonth = currentSelected.solarMonth
                                                selectedDay = currentSelected.solarDay
                                            }
                                            "GREGORIAN" -> {
                                                displayYear = currentSelected.gregorianYear
                                                displayMonth = currentSelected.gregorianMonth
                                                selectedDay = currentSelected.gregorianDay
                                            }
                                            else -> {
                                                displayYear = currentSelected.lunarYear
                                                displayMonth = currentSelected.lunarMonth
                                                selectedDay = currentSelected.lunarDay
                                            }
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else OledSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) NeonCyan else OledBorder
                            )
                        ) {
                            Text(
                                text = calLabel,
                                color = if (isSelected) NeonCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // 2. Month Navigation Header
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = OledSurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OledBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (displayMonth > 1) {
                                    displayMonth--
                                } else {
                                    displayMonth = 12
                                    displayYear--
                                }
                                selectedDay = 1
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "ماه قبل",
                                tint = NeonCyan
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$monthName $displayYear",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val calSubtitle = when (activeCalendar) {
                                "SOLAR" -> "سالنامه هجری خورشیدی"
                                "GREGORIAN" -> "Gregorian Calendar"
                                else -> "سالنامه هجری قمری"
                            }
                            Text(text = calSubtitle, color = TextMuted, fontSize = 10.sp)
                        }

                        IconButton(
                            onClick = {
                                if (displayMonth < 12) {
                                    displayMonth++
                                } else {
                                    displayMonth = 1
                                    displayYear++
                                }
                                selectedDay = 1
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "ماه بعد",
                                tint = NeonCyan
                            )
                        }
                    }
                }
            }

            // 3. Weekday Headers + Days Grid Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = OledSurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OledBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        LocalLayoutDirection provides if (activeCalendar == "GREGORIAN") LayoutDirection.Ltr else LayoutDirection.Rtl
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Weekday column headers
                            val weekdays = if (activeCalendar == "GREGORIAN") {
                                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                            } else {
                                listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                weekdays.forEachIndexed { i, wd ->
                                    val isHoliday = if (activeCalendar == "GREGORIAN") (i == 0) else (i == weekdays.size - 1)
                                    Text(
                                        text = wd,
                                        color = if (isHoliday) NeonRed else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Divider(color = OledBorder, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Grid cells: leading blanks + days
                            val totalCells = leadingOffset + daysInMonth
                            val rows = (totalCells + 6) / 7

                            for (r in 0 until rows) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    for (c in 0 until 7) {
                                        val cellIndex = r * 7 + c
                                        val dayNum = cellIndex - leadingOffset + 1
                                        if (dayNum in 1..daysInMonth) {
                                            val dayAlarms = remember(dayNum, alarms, activeCalendar, displayYear, displayMonth) {
                                                getAlarmsForDay(dayNum)
                                            }
                                            val hasMeeting = dayAlarms.any { it.repeatType == "EVENT" || it.isMeeting }
                                            val hasAlarm = dayAlarms.any { it.repeatType != "EVENT" && !it.isMeeting }
                                            val isSelected = selectedDay == dayNum

                                        val isToday = when (activeCalendar) {
                                            "SOLAR" -> todayTriple.solarYear == displayYear && todayTriple.solarMonth == displayMonth && todayTriple.solarDay == dayNum
                                            "GREGORIAN" -> todayTriple.gregorianYear == displayYear && todayTriple.gregorianMonth == displayMonth && todayTriple.gregorianDay == dayNum
                                            else -> todayTriple.lunarYear == displayYear && todayTriple.lunarMonth == displayMonth && todayTriple.lunarDay == dayNum
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    when {
                                                        isSelected -> NeonCyan.copy(alpha = 0.25f)
                                                        isToday -> NeonAmber.copy(alpha = 0.15f)
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .border(
                                                    1.dp,
                                                    when {
                                                        isSelected -> NeonCyan
                                                        isToday -> NeonAmber
                                                        else -> Color.Transparent
                                                    },
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .clickable {
                                                    selectedDay = dayNum
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "$dayNum",
                                                    color = when {
                                                        isSelected -> NeonCyan
                                                        isToday -> NeonAmber
                                                        else -> TextPrimary
                                                    },
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                                )

                                                // Event / Alarm Dots
                                                if (hasMeeting || hasAlarm) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        if (hasMeeting) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(NeonPink)
                                                            )
                                                        }
                                                        if (hasAlarm) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(NeonAmber)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Selected Day Details Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = OledSurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Triple Date Summary for the selected day
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val primaryDateText = when (activeCalendar) {
                                "GREGORIAN" -> selectedDayTriple.gregorianFormatted
                                "LUNAR" -> selectedDayTriple.lunarFormatted
                                else -> selectedDayTriple.solarFormatted
                            }
                            val secondaryDateText = when (activeCalendar) {
                                "GREGORIAN" -> "شمسی: ${selectedDayTriple.solarFormatted} | قمری: ${selectedDayTriple.lunarFormatted}"
                                "LUNAR" -> "شمسی: ${selectedDayTriple.solarFormatted} | میلادی: ${selectedDayTriple.gregorianFormatted}"
                                else -> "میلادی: ${selectedDayTriple.gregorianFormatted} | قمری: ${selectedDayTriple.lunarFormatted}"
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "روز ${selectedDayTriple.weekdayName}: $primaryDateText",
                                    color = NeonCyan,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = secondaryDateText,
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }

                            // Add Event Button for this specific day
                            Button(
                                onClick = { isAddingEventOnDate = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = OledBackground
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ثبت رویداد/آلارم", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(color = OledBorder, thickness = 0.5.dp)

                        // List of Alarms & Meetings on this Day
                        if (selectedDayAlarms.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "هیچ قرار ملاقات یا آلارمی برای این تاریخ تنظیم نشده است.",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                selectedDayAlarms.forEach { item ->
                                    val isEvent = item.repeatType == "EVENT" || item.isMeeting
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isEvent) NeonCyan.copy(alpha = 0.08f) else OledSurfaceVariant
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isEvent) NeonCyan.copy(alpha = 0.4f) else OledBorder
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { editingAlarm = item }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isEvent) NeonCyan.copy(alpha = 0.2f) else NeonAmber.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (isEvent) Icons.Default.Groups else Icons.Default.Alarm,
                                                        contentDescription = null,
                                                        tint = if (isEvent) NeonCyan else NeonAmber,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }

                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = String.format("%02d:%02d", item.hour, item.minute),
                                                            color = TextPrimary,
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = if (isEvent) NeonPink.copy(alpha = 0.2f) else NeonPurple.copy(alpha = 0.2f)
                                                        ) {
                                                            Text(
                                                                text = if (isEvent) "قرار ملاقات / جلسه" else "زنگ بیدارباش",
                                                                color = if (isEvent) NeonPink else NeonPurple,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }

                                                    val labelText = item.label.ifBlank { if (isEvent) "قرار ملاقات بدون عنوان" else "آلارم" }
                                                    Text(
                                                        text = labelText,
                                                        color = TextPrimary,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )

                                                    if (item.eventNote.isNotBlank()) {
                                                        Text(
                                                            text = "یادداشت: ${item.eventNote}",
                                                            color = TextSecondary,
                                                            fontSize = 11.sp,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Switch(
                                                    checked = item.isEnabled,
                                                    onCheckedChange = {
                                                        if (item.isEnabled && item.repeatType != "ONCE") {
                                                            val nextMillis = com.example.alarm.AlarmScheduler.calculateNextTriggerMillis(item)
                                                            if (nextMillis > System.currentTimeMillis()) {
                                                                pendingDisableAlarm = item to nextMillis
                                                            } else {
                                                                viewModel.disableAlarmCompletely(item)
                                                            }
                                                        } else {
                                                            viewModel.toggleAlarm(item)
                                                        }
                                                    },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = OledBackground,
                                                        checkedTrackColor = if (isEvent) NeonCyan else NeonAmber,
                                                        uncheckedThumbColor = TextMuted,
                                                        uncheckedTrackColor = OledSurfaceVariant
                                                    )
                                                )
                                                IconButton(
                                                    onClick = { viewModel.deleteAlarm(item) }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "حذف",
                                                        tint = NeonRed.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(20.dp)
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

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }

    // Modal Sheet to Add / Edit an event on the currently selected date
    if (isAddingEventOnDate || editingAlarm != null) {
        AddEditAlarmSheet(
            existingAlarm = editingAlarm,
            language = language,
            onDismiss = {
                isAddingEventOnDate = false
                editingAlarm = null
            },
            onSave = { saved ->
                viewModel.saveAlarm(saved)
                isAddingEventOnDate = false
                editingAlarm = null
            },
            initialTripleDate = selectedDayTriple,
            initialIsMeeting = true,
            initialCalendarType = activeCalendar
        )
    }

    pendingDisableAlarm?.let { (alarmToDisable, nextTriggerMillis) ->
        com.example.ui.alarm.DisableAlarmChoiceDialog(
            alarm = alarmToDisable,
            nextTriggerMillis = nextTriggerMillis,
            language = language,
            onSkipNext = {
                viewModel.skipNextAlarmOccurrence(alarmToDisable, nextTriggerMillis)
                pendingDisableAlarm = null
            },
            onDisableCompletely = {
                viewModel.disableAlarmCompletely(alarmToDisable)
                pendingDisableAlarm = null
            },
            onDismiss = { pendingDisableAlarm = null }
        )
    }
}
