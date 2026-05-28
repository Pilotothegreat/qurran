package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.data.HijriCalendarHelper
import com.example.core.data.PrayerTimeCalculator
import com.example.ui.MainViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHighlightCard
import java.text.SimpleDateFormat
import java.util.*

private val AccentGold: Color @Composable get() = MaterialTheme.colorScheme.tertiary
private val PrimaryEmerald: Color @Composable get() = MaterialTheme.colorScheme.primary

@Composable
fun CalendarScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val amoled = viewModel.prefs.amoledMode
    val liveCal by viewModel.liveTimeFlow.collectAsState()

    var activeSubView by remember { mutableStateOf("EVENTS") } // "EVENTS" or "TIMETABLE"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Upper Headers
        Text(
            text = "CALENDAR & TIMETABLE",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = AccentGold
        )
        Text(
            text = "Hijri / Gregorian coordinate schedules",
            fontSize = 12.sp,
            color = if (amoled) Color(0xFF90A49F) else Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Toggle Buttons sub navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { activeSubView = "EVENTS" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubView == "EVENTS") PrimaryEmerald else Color(0x1F129676),
                    contentColor = if (activeSubView == "EVENTS") AccentGold else Color.LightGray
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lunar Events")
            }

            Button(
                onClick = { activeSubView = "TIMETABLE" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubView == "TIMETABLE") PrimaryEmerald else Color(0x1F129676),
                    contentColor = if (activeSubView == "TIMETABLE") AccentGold else Color.LightGray
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Monthly Times")
            }
        }

        if (activeSubView == "EVENTS") {
            LunarEventsListView(liveCal = liveCal, amoled = amoled)
        } else {
            MonthlyPrayerCalculationsTimetable(viewModel = viewModel, liveCal = liveCal, amoled = amoled)
        }
    }
}

data class IslamicEvent(
    val hijriDate: String,
    val gregorianDate: String,
    val titleAr: String,
    val titleEn: String,
    val info: String
)

@Composable
fun LunarEventsListView(liveCal: Calendar, amoled: Boolean) {
    val events = remember(liveCal) {
        listOf(
            IslamicEvent("1 Ramadan", "March / April Approx", "بداية شهر رمضان المبارك", "1st Ramadan", "Start of the holy month of fasting."),
            IslamicEvent("17 Ramadan", "Ramadan Middle", "معركة بدر الكبرى", "Battle of Badr", "Major victory in early Islamic history."),
            IslamicEvent("1 Shawwal", "End of Ramadan", "عيد الفطر السعيد", "Eid al-Fitr", "Blessed festival marking completion of Ramadan."),
            IslamicEvent("10 Dhu al-Hijjah", "Kurban festival", "عيد الأضحى المبارك", "Eid al-Adha", "Feast of sacrifice coinciding with Hajj."),
            IslamicEvent("1 Muharram", "New Islamic Year", "رأس السنة الهجرية", "Islamic New Year", "Commemoration of the Prophet’s Migration (Hijrah)."),
            IslamicEvent("12 Rabi' al-Awwal", "Mawlid al-Nabi", "المولد النبوي الشريف", "Prophetic Mawlid", "Observed quietly in contemplation in Oman."),
            IslamicEvent("27 Rajab", "Isra' and Mi'raj", "الإسراء والمعراج", "Isra' and Mi'raj", "Night journey commemorating the Prophet's ascension.")
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(events) { ev ->
            GlassHighlightCard(
                modifier = Modifier.fillMaxWidth(),
                isDark = amoled
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryEmerald),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ev.titleEn,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold
                            )
                            Text(
                                text = ev.hijriDate,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (amoled) Color.White else Color.Black
                            )
                        }
                        Text(
                            text = ev.titleAr,
                            fontSize = 14.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            color = AccentGold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = ev.info,
                            fontSize = 12.sp,
                            color = if (amoled) Color.LightGray else Color.DarkGray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyPrayerCalculationsTimetable(
    viewModel: MainViewModel,
    liveCal: Calendar,
    amoled: Boolean
) {
    val currentMonth = remember { liveCal.get(Calendar.MONTH) }
    val currentYear = remember { liveCal.get(Calendar.YEAR) }

    val daysInMonth = remember(currentMonth, currentYear) {
        val tempCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
        }
        tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    // Calculated schedules lists
    val monthSchedules = remember(daysInMonth, viewModel.prefs.latitude, viewModel.prefs.longitude) {
        val itemsList = mutableListOf<PrayerTimeCalculator.PrayerTimes>()
        val tempCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
        }
        val tz = TimeZone.getDefault()
        for (day in 1..daysInMonth) {
            tempCal.set(Calendar.DAY_OF_MONTH, day)
            val tzOffsetHours = tz.getOffset(tempCal.timeInMillis) / 3600000.0
            val calculated = PrayerTimeCalculator.calculateTimes(
                calendar = tempCal,
                latitude = viewModel.prefs.latitude,
                longitude = viewModel.prefs.longitude,
                timezone = tzOffsetHours,
                fajrAngle = viewModel.prefs.fajrAngle,
                ishaAngle = viewModel.prefs.ishaAngle,
                asrSchool = viewModel.prefs.asrSchool
            )
            itemsList.add(calculated)
        }
        itemsList
    }

    val displayMonthName = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(liveCal.time)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Monthly Timetable: $displayMonthName",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Column Headings Layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (amoled) Color(0xFF101715) else Color(0xFFE2E9E6))
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val headers = listOf("Day", "Fajr", "Dhuhr", "Asr", "Mag.", "Isha")
            headers.forEach { label ->
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Table List Scroll
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(monthSchedules) { calc ->
                    val dayNum = calc.dateString.substringAfterLast("-")
                    val isToday = dayNum.toInt() == liveCal.get(Calendar.DAY_OF_MONTH)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isToday) Color(0x3B129676) else Color.Transparent
                            )
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dayNum,
                            fontSize = 12.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) AccentGold else (if (amoled) Color.White else Color.Black),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        listOf(calc.fajr, calc.dhuhr, calc.asr, calc.maghrib, calc.isha).forEach { valStr ->
                            Text(
                                text = valStr,
                                fontSize = 12.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = if (isToday) (if (amoled) Color.White else Color.Black) else (if (amoled) Color.LightGray else Color.DarkGray),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    HorizontalDivider(color = if (amoled) Color(0xFF101715) else Color(0xFFE2E9E6))
                }
            }
        }
    }
}
