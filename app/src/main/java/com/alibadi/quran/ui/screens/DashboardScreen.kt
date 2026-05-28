package com.alibadi.quran.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alibadi.quran.R
import com.alibadi.quran.core.data.HijriCalendarHelper
import com.alibadi.quran.core.data.entities.PrayerLog
import com.alibadi.quran.feature.calendar.CalendarViewModel
import com.alibadi.quran.feature.calendar.IslamicEvent
import com.alibadi.quran.feature.hadith.HadithViewModel
import com.alibadi.quran.feature.prayer.NewsItem
import com.alibadi.quran.feature.prayer.PrayerViewModel
import com.alibadi.quran.feature.quran.QuranViewModel
import com.alibadi.quran.feature.settings.SettingsViewModel
import com.alibadi.quran.feature.settings.UpdateState
import com.alibadi.quran.ui.theme.accentGold
import com.alibadi.quran.ui.theme.primaryEmerald
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    prayerViewModel: PrayerViewModel,
    quranViewModel: QuranViewModel,
    hadithViewModel: HadithViewModel,
    settingsViewModel: SettingsViewModel,
    calendarViewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    val liveCal by prayerViewModel.liveTimeFlow.collectAsState()
    val prayerTimes by prayerViewModel.prayerTimesState.collectAsState()
    val prayerLog by prayerViewModel.todayPrayerLog.collectAsState()
    val recentPrayerLogs by prayerViewModel.recentPrayerLogs.collectAsState()
    val palestineUpdates by prayerViewModel.palestineUpdates.collectAsState()
    val liveMuslimNews by prayerViewModel.liveMuslimNews.collectAsState()
    val isRefreshing by prayerViewModel.isRefreshing.collectAsState()

    val hijriDate by calendarViewModel.hijriDate.collectAsState()
    val daysToRamadan by calendarViewModel.daysToRamadan.collectAsState()
    val islamicEvents by calendarViewModel.islamicEvents.collectAsState()

    val quranHistory by quranViewModel.quranHistory.collectAsState()
    val surahs by quranViewModel.surahs.collectAsState()

    val filteredHadiths by hadithViewModel.filteredHadiths.collectAsState()

    val amoled by settingsViewModel.amoled.collectAsState()
    val showPalestineUpdates by settingsViewModel.showPalestineUpdates.collectAsState()
    val updateState by settingsViewModel.updateState.collectAsState()
    val locationName by settingsViewModel.locationName.collectAsState(initial = "مسقط، عُمان")

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (amoled) Color.Black else MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Update Check Banner
            AnimatedVisibility(
                visible = updateState is UpdateState.UpdateAvailable,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                (updateState as? UpdateState.UpdateAvailable)?.let { update ->
                    var dismissed by remember { mutableStateOf(false) }
                    if (!dismissed) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Celebration,
                                            contentDescription = null,
                                            tint = accentGold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "تحديث جديد متوفر: ${update.latestVersion}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    IconButton(
                                        onClick = { dismissed = true },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = update.changelog,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = android.content.Intent(
                                                    android.content.Intent.ACTION_VIEW,
                                                    android.net.Uri.parse(update.downloadUrl)
                                                )
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "فشل في فتح الرابط", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = primaryEmerald,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("تنزيل التحديث")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                val intent = android.content.Intent(
                                                    android.content.Intent.ACTION_VIEW,
                                                    android.net.Uri.parse(update.htmlUrl)
                                                )
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "فشل في فتح الرابط", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("عرض على GitHub")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Welcome Header & Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = accentGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = locationName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    hijriDate?.let { date ->
                        Text(
                            text = "${date.day} ${date.monthNameAr} ${date.year} هـ",
                            style = MaterialTheme.typography.titleLarge,
                            color = accentGold,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }

                // Digital Clock
                Column(horizontalAlignment = Alignment.End) {
                    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
                    val dayFormat = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()) }
                    Text(
                        text = timeFormat.format(liveCal.time),
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dayFormat.format(liveCal.time),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Countdown Panel
            prayerTimes?.let { times ->
                val countdownData = remember(times, liveCal) {
                    val calculated = calculateCountdown(times, liveCal)
                    val arabicName = when (calculated.prayerName) {
                        "Fajr" -> "الفجر"
                        "Sunrise" -> "الشروق"
                        "Dhuhr" -> "الظهر"
                        "Asr" -> "العصر"
                        "Maghrib" -> "المغرب"
                        "Isha" -> "العشاء"
                        else -> calculated.prayerName
                    }
                    calculated.copy(prayerName = arabicName)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(accentGold.copy(alpha = 0.12f))
                                .border(1.dp, accentGold.copy(0.4f), CircleShape)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "الصَّلَاةُ القَادِمَةُ لِلْمُسْلِمِ",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                color = accentGold,
                                fontFamily = FontFamily.Serif
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = countdownData.remainingStr,
                            style = MaterialTheme.typography.displayMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = primaryEmerald,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "متبقي لرفع نداء صلاة ${countdownData.prayerName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentGold,
                            fontFamily = FontFamily.Serif
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = null,
                                tint = accentGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "موعد الأذان: ${countdownData.athanTime}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Daily Progress and Ramadan Countdown
                val todayCompletedProgress = remember(prayerLog) {
                    var count = 0
                    if (prayerLog.fajrCompleted) count++
                    if (prayerLog.dhuhrCompleted) count++
                    if (prayerLog.asrCompleted) count++
                    if (prayerLog.maghribCompleted) count++
                    if (prayerLog.ishaCompleted) count++
                    count
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(primaryEmerald.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = primaryEmerald,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "الفروض المنجزة",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accentGold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "$todayCompletedProgress / 5",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(accentGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NightsStay,
                                        contentDescription = null,
                                        tint = accentGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "رمضان المبارك",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accentGold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "$daysToRamadan يوم",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                }

                // Weekly tracker chart
                WeeklyPrayerBarChart(
                    recentPrayerLogs = recentPrayerLogs,
                    amoled = amoled
                )

                // Checklist of prayers
                TodayPrayersChecklist(
                    log = prayerLog,
                    prayerTimes = times,
                    onToggle = { prayerViewModel.togglePrayerCompleted(it) }
                )
            }

            // Action Cards for Hadith & Qibla
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { navController.navigate("hadith") },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = accentGold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.hadith_library),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.read_hadith),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { navController.navigate("qibla") },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.CompassCalibration, contentDescription = null, tint = accentGold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.qibla_compass),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.read_hadith),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Palestine Updates (Collapsible & Opt-In)
            if (showPalestineUpdates) {
                var palestineExpanded by remember { mutableStateOf(false) }
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "نصرة القضية الفلسطينية",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            IconButton(onClick = { palestineExpanded = !palestineExpanded }) {
                                Icon(
                                    imageVector = if (palestineExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Toggle"
                                )
                            }
                        }
                        if (palestineExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = palestineUpdates,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Bulletins Board
            if (islamicEvents.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "التوجيهات والفعاليات الإسلامية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentGold
                    )
                    islamicEvents.forEach { event ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = event.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(MaterialTheme.shapes.small)
                                            .background(Color(android.graphics.Color.parseColor(event.color)).copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = event.tag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(android.graphics.Color.parseColor(event.color))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = event.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // News Ticker
            if (liveMuslimNews.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "أخبار العالم الإسلامي المباشرة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentGold
                    )
                    liveMuslimNews.take(3).forEach { news ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        val intent = android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse(news.link)
                                        )
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = news.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = news.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Quran Resume Card
            quranHistory?.let { history ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("quran/${history.surahNumber}") },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "متابعة تلاوة القرآن الكريم",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "سورة ${history.surahName} • آية ${history.ayahNumber}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Random Highlight Hadith Card
            if (filteredHadiths.isNotEmpty()) {
                val randomHadith = remember(filteredHadiths) { filteredHadiths.random() }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "حديث شريف مختار",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = accentGold
                            )
                            TextButton(onClick = { navController.navigate("hadith") }) {
                                Text("المزيد", color = accentGold)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = randomHadith.textAr,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyPrayerBarChart(
    recentPrayerLogs: List<PrayerLog>,
    amoled: Boolean,
    modifier: Modifier = Modifier
) {
    val weeklyData = remember(recentPrayerLogs) {
        val df = SimpleDateFormat("E", Locale("ar"))
        val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val list = mutableListOf<Pair<String, Int>>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0 until 7) {
            val dateStr = keyFormat.format(cal.time)
            val dayLabel = df.format(cal.time)
            val matchingLog = recentPrayerLogs.find { it.dateKey == dateStr }
            var completedCount = 0
            if (matchingLog != null) {
                if (matchingLog.fajrCompleted) completedCount++
                if (matchingLog.dhuhrCompleted) completedCount++
                if (matchingLog.asrCompleted) completedCount++
                if (matchingLog.maghribCompleted) completedCount++
                if (matchingLog.ishaCompleted) completedCount++
            }
            list.add(Pair(dayLabel, completedCount))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "النشاط الأسبوعي",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentGold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { (day, count) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .height(80.dp)
                                .width(12.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            val fraction = count / 5f
                            if (fraction > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight(fraction)
                                        .fillMaxWidth()
                                        .clip(CircleShape)
                                        .background(primaryEmerald)
                                )
                            }
                        }
                        Text(text = day, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun TodayPrayersChecklist(
    log: PrayerLog,
    prayerTimes: com.alibadi.quran.core.data.PrayerTimeCalculator.PrayerTimes,
    onToggle: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "صلوات الفريضة اليومية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentGold
            )
            Spacer(modifier = Modifier.height(12.dp))

            val prayers = listOf(
                Triple("الفجر (Fajr)", log.fajrCompleted, prayerTimes.fajr),
                Triple("الظهر (Dhuhr)", log.dhuhrCompleted, prayerTimes.dhuhr),
                Triple("العصر (Asr)", log.asrCompleted, prayerTimes.asr),
                Triple("المغرب (Maghrib)", log.maghribCompleted, prayerTimes.maghrib),
                Triple("العشاء (Isha)", log.ishaCompleted, prayerTimes.isha)
            )

            prayers.forEachIndexed { index, (name, completed, time) ->
                val englishKeyName = name.substringAfter("(").substringBefore(")")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(englishKeyName) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = completed,
                            onCheckedChange = { onToggle(englishKeyName) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = primaryEmerald,
                                checkmarkColor = accentGold
                            )
                        )
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = accentGold
                    )
                }
                if (index < prayers.size - 1) {
                    HorizontalDivider()
                }
            }
        }
    }
}

// Helper structures
data class CountdownData(
    val prayerName: String,
    val remainingStr: String,
    val athanTime: String
)

fun calculateCountdown(
    times: com.alibadi.quran.core.data.PrayerTimeCalculator.PrayerTimes,
    liveCal: Calendar
): CountdownData {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    val todayPrefix = times.dateString

    val prayersList = listOf(
        "Fajr" to times.fajr,
        "Sunrise" to times.sunrise,
        "Dhuhr" to times.dhuhr,
        "Asr" to times.asr,
        "Maghrib" to times.maghrib,
        "Isha" to times.isha
    )

    val currentMillis = liveCal.timeInMillis
    var targetName = "Fajr"
    var targetTimeStr = times.fajr
    var targetMillis = 0L

    for ((name, time) in prayersList) {
        try {
            val date = sdf.parse("$todayPrefix $time") ?: continue
            if (date.time > currentMillis) {
                targetName = name
                targetTimeStr = time
                targetMillis = date.time
                break
            }
        } catch (e: Exception) {}
    }

    if (targetMillis == 0L) {
        try {
            val tomCal = (liveCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
            val tomPrefix = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(tomCal.time)
            val date = sdf.parse("$tomPrefix ${times.fajr}")
            if (date != null) {
                targetName = "Fajr"
                targetTimeStr = times.fajr
                targetMillis = date.time
            }
        } catch (e: Exception) {}
    }

    val diff = targetMillis - currentMillis
    val hours = diff / (3600 * 1000)
    val minutes = (diff / (60 * 1000)) % 60
    val seconds = (diff / 1000) % 60

    val remainingString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    return CountdownData(targetName, remainingString, targetTimeStr)
}
