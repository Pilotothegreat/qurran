package com.alibadi.quran.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibadi.quran.R
import com.alibadi.quran.core.data.PrayerTimeCalculator
import com.alibadi.quran.feature.calendar.CalendarViewModel
import com.alibadi.quran.feature.settings.SettingsViewModel
import com.alibadi.quran.ui.theme.accentGold
import com.alibadi.quran.ui.theme.primaryEmerald
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val amoled by settingsViewModel.amoled.collectAsState()

    var activeSubView by remember { mutableStateOf("EVENTS") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "التقويم الهجري والشهري",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = accentGold
        )
        Text(
            text = "Hijri / Gregorian coordinate schedules",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { activeSubView = "EVENTS" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubView == "EVENTS") primaryEmerald else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (activeSubView == "EVENTS") accentGold else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lunar Events", style = MaterialTheme.typography.labelMedium)
            }

            Button(
                onClick = { activeSubView = "TIMETABLE" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubView == "TIMETABLE") primaryEmerald else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (activeSubView == "TIMETABLE") accentGold else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Monthly Times", style = MaterialTheme.typography.labelMedium)
            }
        }

        if (activeSubView == "EVENTS") {
            LunarEventsListView()
        } else {
            MonthlyPrayerCalculationsTimetable(settingsViewModel = settingsViewModel)
        }
    }
}

data class CalendarEventItem(
    val hijriDate: String,
    val gregorianDate: String,
    val titleAr: String,
    val titleEn: String,
    val info: String
)

@Composable
fun LunarEventsListView() {
    val events = remember {
        listOf(
            CalendarEventItem("1 Ramadan", "March / April Approx", "بداية شهر رمضان المبارك", "1st Ramadan", "Start of the holy month of fasting."),
            CalendarEventItem("17 Ramadan", "Ramadan Middle", "معركة بدر الكبرى", "Battle of Badr", "Major victory in early Islamic history."),
            CalendarEventItem("1 Shawwal", "End of Ramadan", "عيد الفطر السعيد", "Eid al-Fitr", "Blessed festival marking completion of Ramadan."),
            CalendarEventItem("10 Dhu al-Hijjah", "Kurban festival", "عيد الأضحى المبارك", "Eid al-Adha", "Feast of sacrifice coinciding with Hajj."),
            CalendarEventItem("1 Muharram", "New Islamic Year", "رأس السنة الهجرية", "Islamic New Year", "Commemoration of the Prophet’s Migration (Hijrah)."),
            CalendarEventItem("12 Rabi' al-Awwal", "Mawlid al-Nabi", "المولد النبوي الشريف", "Prophetic Mawlid", "Observed quietly in contemplation in Oman."),
            CalendarEventItem("27 Rajab", "Isra' and Mi'raj", "الإسراء والمعراج", "Isra' and Mi'raj", "Night journey commemorating the Prophet's ascension.")
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(events) { ev ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(primaryEmerald),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, tint = accentGold, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ev.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = accentGold
                            )
                            Text(
                                text = ev.hijriDate,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = ev.titleAr,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            color = accentGold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = ev.info,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    settingsViewModel: SettingsViewModel
) {
    val liveCal = Calendar.getInstance()
    val currentMonth = remember { liveCal.get(Calendar.MONTH) }
    val currentYear = remember { liveCal.get(Calendar.YEAR) }

    val userLat by settingsViewModel.latitude.collectAsState(initial = 23.5880)
    val userLon by settingsViewModel.longitude.collectAsState(initial = 58.3829)
    val asrSchool by settingsViewModel.asrSchool.collectAsState()

    val daysInMonth = remember(currentMonth, currentYear) {
        val tempCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
        }
        tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val monthSchedules = remember(daysInMonth, userLat, userLon, asrSchool) {
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
                latitude = userLat,
                longitude = userLon,
                timezone = tzOffsetHours,
                fajrAngle = 18.0,
                ishaAngle = 18.0,
                asrSchool = asrSchool
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accentGold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val headers = listOf("Day", "Fajr", "Dhuhr", "Asr", "Mag.", "Isha")
            headers.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentGold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

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
                                if (isToday) primaryEmerald.copy(alpha = 0.15f) else Color.Transparent
                            )
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dayNum,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) accentGold else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        listOf(calc.fajr, calc.dhuhr, calc.asr, calc.maghrib, calc.isha).forEach { valStr ->
                            Text(
                                text = valStr,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                color = if (isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
