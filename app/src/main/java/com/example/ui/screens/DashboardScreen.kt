package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.data.HijriCalendarHelper
import com.example.core.data.entities.PrayerLog
import com.example.feature.hadith.HadithData
import com.example.feature.quran.QuranData
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.IslamicEvent
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHighlightCard
import java.text.SimpleDateFormat
import java.util.*

private val AccentGold: Color @Composable get() = MaterialTheme.colorScheme.tertiary
private val PrimaryEmerald: Color @Composable get() = MaterialTheme.colorScheme.primary


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val liveCal by viewModel.liveTimeFlow.collectAsState()
    val prayerTimes by viewModel.prayerTimesState.collectAsState()
    val prayerLog by viewModel.todayPrayerLog.collectAsState()
    val amoled by viewModel.amoledMode.collectAsState()
    val simpleMode by viewModel.simpleMode.collectAsState()
    val dynamicEvents by viewModel.dynamicEvents.collectAsState()
    val palestineUpdates by viewModel.palestineUpdates.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val recentPrayerLogs by viewModel.recentPrayerLogs.collectAsState()
    val translationLang = viewModel.prefs.quranLanguage

    val hijriDate = remember(liveCal, viewModel.prefs.hijriOffset) {
        HijriCalendarHelper.getHijriDate(liveCal, viewModel.prefs.hijriOffset)
    }

    val daysToRamadan = remember(liveCal, viewModel.prefs.hijriOffset) {
        HijriCalendarHelper.getDaysToRamadan(liveCal, viewModel.prefs.hijriOffset)
    }

    val scrollState = rememberScrollState()
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                viewModel.recalculateTimes()
                viewModel.syncOnlinePrayerTimes { _, _ -> }
                viewModel.fetchDynamicEvents()
                viewModel.fetchPalestineLiveUpdates()
                viewModel.fetchLiveMuslimNews()
                viewModel.checkForUpdates()
                kotlinx.coroutines.delay(1200)
                isRefreshing = false
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (amoled) Color.Black else MaterialTheme.colorScheme.background)
                .drawBehind {
                    val size = this.size
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x1F0F5B46),
                                Color(0x000F5B46)
                            ),
                            center = Offset(size.width * 0.95f, size.height * 0.08f),
                            radius = size.width * 0.75f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x1ADB9728),
                                Color(0x00DB9728)
                            ),
                            center = Offset(size.width * 0.05f, size.height * 0.45f),
                            radius = size.width * 0.85f
                        )
                    )
                }
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Material You Auto Update Notification Banner
            androidx.compose.animation.AnimatedVisibility(
                visible = updateState is com.example.ui.UpdateState.UpdateAvailable,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                (updateState as? com.example.ui.UpdateState.UpdateAvailable)?.let { update ->
                    var dismissed by remember { mutableStateOf(false) }
                    if (!dismissed) {
                        GlassHighlightCard(
                            modifier = Modifier.fillMaxWidth(),
                            isDark = amoled
                        ) {
                            Column(modifier = Modifier.padding(4.dp)) {
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
                                            tint = com.example.ui.theme.AccentGold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "تحديث جديد متوفر: ${update.latestVersion} 😍",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (simpleMode) 17.sp else 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    IconButton(
                                        onClick = { dismissed = true },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss",
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = update.changelog,
                                    fontSize = if (simpleMode) 13.sp else 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val context = LocalContext.current
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = android.content.Intent(
                                                    android.content.Intent.ACTION_VIEW,
                                                    android.net.Uri.parse(update.downloadUrl ?: update.htmlUrl)
                                                )
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "فشل في فتح الرابط", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = com.example.ui.theme.PrimaryEmerald,
                                            contentColor = com.example.ui.theme.AccentGold
                                        ),
                                        modifier = Modifier.height(38.dp).weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = "تنزيل التحديث 🚀",
                                            fontSize = if (simpleMode) 13.sp else 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
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
                                                android.widget.Toast.makeText(context, "فشل في فتح الرابط", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.PrimaryEmerald),
                                        modifier = Modifier.height(38.dp).weight(1.0f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = "عرض على GitHub 🐙",
                                            color = com.example.ui.theme.PrimaryEmerald,
                                            fontSize = if (simpleMode) 13.sp else 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Welcome Header & Location Section
            HeaderSection(
                locationName = viewModel.prefs.locationName,
                hijriDateStr = "${hijriDate.day} ${hijriDate.monthNameAr} ${hijriDate.year} هـ",
                hijriEnglish = "${hijriDate.day} ${hijriDate.monthNameEn}, ${hijriDate.year} AH",
                amoled = amoled,
                simpleMode = simpleMode,
                liveCal = liveCal,
                translationLang = translationLang
            )

            // Countdowns of Next Prayer
            prayerTimes?.let { times ->
                CountdownPanel(times = times, liveCal = liveCal, amoled = amoled, simpleMode = simpleMode)
                
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
                    // Card 1: Completed Prayers
                    GlassHighlightCard(
                        modifier = Modifier.weight(1f),
                        isDark = amoled
                    ) {
                        Column(modifier = Modifier.padding(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryEmerald.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = PrimaryEmerald,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "الفروض المنجزة",
                                    fontSize = if (simpleMode) 13.sp else 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "$todayCompletedProgress / 5",
                                fontSize = if (simpleMode) 26.sp else 22.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Serif
                            )
                            Text(
                                text = "صلوات مؤداة اليوم",
                                fontSize = if (simpleMode) 11.sp else 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Card 2: Days to Ramadan
                    GlassHighlightCard(
                        modifier = Modifier.weight(1f),
                        isDark = amoled
                    ) {
                        Column(modifier = Modifier.padding(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(AccentGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NightsStay,
                                        contentDescription = null,
                                        tint = AccentGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "رمضان المبارك",
                                    fontSize = if (simpleMode) 13.sp else 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "$daysToRamadan يوم",
                                fontSize = if (simpleMode) 26.sp else 22.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Serif
                            )
                            Text(
                                text = "متبقٍ للشهر الفضيل",
                                fontSize = if (simpleMode) 11.sp else 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Weekly tracker bar chart reflecting actual logs
                WeeklyPrayerBarChart(
                    recentPrayerLogs = recentPrayerLogs,
                    amoled = amoled,
                    simpleMode = simpleMode
                )
            }

            // Daily Prayers with Custom Iqama times calculations
            TodayPrayersChecklistWithIqama(
                log = prayerLog,
                prayerTimes = prayerTimes,
                onToggle = { viewModel.togglePrayerCompleted(it) },
                prefs = viewModel.prefs,
                amoled = amoled,
                simpleMode = simpleMode
            )

            // Palestine Solidarity Section (Sacred Al-Aqsa support)
            PalestineSolidarityCard(palestineText = palestineUpdates, amoled = amoled)

            // Multi-Tab Educational Material Portal
            EducationalPortalSection(amoled = amoled)

            // Dynamic Bulletin Events board (updates without app update)
            DynamicBulletinsBoard(events = dynamicEvents, amoled = amoled)

            // Active Live Muslim News Ticker from Global Feeds (Real-time events)
            LiveMuslimNewsFeed(viewModel = viewModel, amoled = amoled)

            // Fasting preparation countdown
            FastingCountdownCard(daysToRamadan = daysToRamadan, pTimes = prayerTimes, amoled = amoled)

            // Recent read Quran Resume Link and Stats
            QuranResumeCard(viewModel = viewModel)

            // Jami' Al-Sahih collection Highlight Hadith
            HadithHighlightCard(viewModel = viewModel)
        }
    }
}

@Composable
fun HeaderSection(
    locationName: String,
    hijriDateStr: String,
    hijriEnglish: String,
    amoled: Boolean,
    simpleMode: Boolean,
    liveCal: Calendar,
    translationLang: String
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()) }

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
                    tint = AccentGold,
                    modifier = Modifier.size(if (simpleMode) 22.dp else 16.dp)
                )
                Text(
                    text = locationName,
                    fontSize = if (simpleMode) 18.sp else 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = hijriDateStr,
                fontSize = if (simpleMode) 26.sp else 20.sp,
                color = AccentGold,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif
            )
            if (translationLang != "NONE") {
                Text(
                    text = hijriEnglish,
                    fontSize = if (simpleMode) 15.sp else 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Digital interactive clock
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = timeFormat.format(liveCal.time),
                fontSize = if (simpleMode) 24.sp else 18.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dayFormat.format(liveCal.time),
                fontSize = if (simpleMode) 14.sp else 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun CountdownPanel(
    times: com.example.core.data.PrayerTimeCalculator.PrayerTimes,
    liveCal: Calendar,
    amoled: Boolean,
    simpleMode: Boolean
) {
    val countdownData = remember(times, liveCal) {
        val calculated = calculateCountdown(times, liveCal)
        // Translate upcoming prayers to elegant Arabic
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

    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    GlassHighlightCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = amoled
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (simpleMode) 16.dp else 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, AccentGold.copy(0.4f), RoundedCornerShape(20.dp))
                    .background(AccentGold.copy(0.12f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "الصَّلَاةُ القَادِمَةُ لِلْمُسْلِمِ 🕋",
                    fontSize = if (simpleMode) 15.sp else 12.sp,
                    fontWeight = FontWeight.Black,
                    color = AccentGold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Serif
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Massive Counter (Replicating traffic-light speed tracker)
            Text(
                text = countdownData.remainingStr,
                fontSize = if (simpleMode) 54.sp else 46.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = if (amoled) Color.White else PrimaryEmerald,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Current state tag / label
            Text(
                text = "متبقي لرفع نداء صلاة ${countdownData.prayerName}",
                fontSize = if (simpleMode) 18.sp else 15.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGold,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Information tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(if (simpleMode) 22.dp else 16.dp)
                )
                Text(
                    text = "موعد الأذان: ${countdownData.athanTime}",
                    fontSize = if (simpleMode) 15.sp else 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = subTextColor
                )
            }
        }
    }
}

@Composable
fun WeeklyPrayerBarChart(
    recentPrayerLogs: List<PrayerLog>,
    amoled: Boolean,
    simpleMode: Boolean,
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

    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        isDark = amoled
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "متابعة النشاط الأسبوعي 📈",
                    fontSize = if (simpleMode) 18.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "عدد الفروض المؤداة بالسبتمر في آخر 7 أيام",
                    fontSize = if (simpleMode) 13.sp else 11.sp,
                    color = subTextColor
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, PrimaryEmerald.copy(0.3f), RoundedCornerShape(12.dp))
                    .background(PrimaryEmerald.copy(0.1f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "الصلوات",
                    fontSize = if (simpleMode) 12.sp else 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryEmerald
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            weeklyData.forEach { (day, count) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .height(100.dp)
                            .width(if (simpleMode) 18.dp else 12.dp)
                            .clip(CircleShape)
                            .background(if (amoled) Color(0xFF1B2825) else Color(0xFFE5ECEB)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        val fraction = count / 5.0f
                        if (fraction > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(fraction)
                                    .fillMaxWidth()
                                    .clip(CircleShape)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                AccentGold,
                                                PrimaryEmerald
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    Text(
                        text = day,
                        fontSize = if (simpleMode) 14.sp else 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun TodayPrayersChecklistWithIqama(
    log: PrayerLog,
    prayerTimes: com.example.core.data.PrayerTimeCalculator.PrayerTimes?,
    onToggle: (String) -> Unit,
    prefs: com.example.core.data.UserPreferencesRepository,
    amoled: Boolean,
    simpleMode: Boolean
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = amoled
    ) {
        Text(
            text = "تتبع صلوات الفريضة والإقامة اليومية 🕋",
            fontSize = if (simpleMode) 18.sp else 14.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGold,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = "احرص على أداء صلاتك في وقتها جماعة. سنّة سدل اليدين مأثورة بالأثر.",
            fontSize = if (simpleMode) 14.sp else 11.sp,
            color = subTextColor,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Compute Iqamah times
        val fAthan = prayerTimes?.fajr ?: "--:--"
        val dAthan = prayerTimes?.dhuhr ?: "--:--"
        val aAthan = prayerTimes?.asr ?: "--:--"
        val mAthan = prayerTimes?.maghrib ?: "--:--"
        val iAthan = prayerTimes?.isha ?: "--:--"

        val prayers = listOf(
            Triple("الفجر (Fajr)", log.fajrCompleted, Pair(fAthan, calculateIqamaTime(fAthan, prefs.iqamaFajr))),
            Triple("الظهر (Dhuhr)", log.dhuhrCompleted, Pair(dAthan, calculateIqamaTime(dAthan, prefs.iqamaDhuhr))),
            Triple("العصر (Asr)", log.asrCompleted, Pair(aAthan, calculateIqamaTime(aAthan, prefs.iqamaAsr))),
            Triple("المغرب (Maghrib)", log.maghribCompleted, Pair(mAthan, calculateIqamaTime(mAthan, prefs.iqamaMaghrib))),
            Triple("العشاء (Isha)", log.ishaCompleted, Pair(iAthan, calculateIqamaTime(iAthan, prefs.iqamaIsha)))
        )

        prayers.forEachIndexed { index, (name, completed, times) ->
            val englishKeyName = when {
                name.contains("Fajr") -> "Fajr"
                name.contains("Dhuhr") -> "Dhuhr"
                name.contains("Asr") -> "Asr"
                name.contains("Maghrib") -> "Maghrib"
                else -> "Isha"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggle(englishKeyName) }
                    .padding(vertical = if (simpleMode) 12.dp else 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Checkbox(
                        checked = completed,
                        onCheckedChange = { onToggle(englishKeyName) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PrimaryEmerald,
                            checkmarkColor = AccentGold
                        ),
                        modifier = Modifier.scale(if (simpleMode) 1.3f else 1.0f)
                    )
                    Column {
                        Text(
                            text = name,
                            fontSize = if (simpleMode) 20.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (completed) subTextColor.copy(alpha = 0.5f) else textColor,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1B2825))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "الأذان: ${times.first}",
                                    fontSize = if (simpleMode) 13.sp else 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = AccentGold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PrimaryEmerald.copy(0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "الإقامة: ${times.second}",
                                    fontSize = if (simpleMode) 13.sp else 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (amoled) Color.White else PrimaryEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Icon(
                    imageVector = if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (completed) PrimaryEmerald else AccentGold.copy(0.6f),
                    modifier = Modifier.size(if (simpleMode) 28.dp else 20.dp)
                )
            }
            if (index < prayers.size - 1) {
                HorizontalDivider(color = if (amoled) Color(0xFF1B2825) else Color(0xFFE5ECEB))
            }
        }
    }
}

// QoL support for Palestinian Cause awareness
@Composable
fun PalestineSolidarityCard(palestineText: String, amoled: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (amoled) Color(0xFF1C0A0A) else Color(0xFFFFF1F1)
        ),
        border = BorderStroke(1.5.dp, Color(0xFFE53935))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Visual Palestinian Flag Ribbon/Motif
                    Box(
                        modifier = Modifier
                            .size(width = 32.dp, height = 20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Gray)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.Black))
                            }
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.White))
                            }
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF138808)))
                            }
                        }
                        // Triangle
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(0f, 0f)
                                lineTo(size.width * 0.45f, size.height / 2f)
                                lineTo(0f, size.height)
                                close()
                            }
                            drawPath(path, Color(0xFFE53935))
                        }
                    }

                    Text(
                        text = "نُصرَةُ القَضِيَّةِ الفِلَسْطِينِيَّةِ 🇵🇸",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935),
                        fontFamily = FontFamily.Serif
                    )
                }

                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFE53935)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = palestineText,
                fontSize = 12.sp,
                color = if (amoled) Color(0xFFD3C2C2) else Color.DarkGray,
                lineHeight = 18.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFE53935).copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "دعاء مرابط: \"اللَّهُمَّ حَرِّرِ المَسْجِدَ الأَقْصَى، وَانْصُرْ إِخْوَانَنَا فِي غَزَّةَ وَجَنِّبْهُمُ الظُّلْمَ وَأَيِّدْهُمْ بِقُوَّتِكَ يَا ذَا الجَلَالِ وَالإِكْرَامِ.\"",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 18.sp
            )
        }
    }
}

// Active Live Muslim News Ticker displaying real world updates on the fly
@Composable
fun LiveMuslimNewsFeed(viewModel: MainViewModel, amoled: Boolean) {
    val newsList by viewModel.liveMuslimNews.collectAsState()
    val context = LocalContext.current

    if (newsList.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "أَخْبَارُ العَالَمِ الـإِسْلَامِيِّ الـمُبَاشِرَةِ 📡",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGold,
                fontFamily = FontFamily.Serif
            )
            IconButton(
                onClick = { viewModel.fetchLiveMuslimNews() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "تحديث الأخبار",
                    tint = AccentGold,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        newsList.forEach { news ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(news.link)).apply {
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {}
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (amoled) Color(0xFF0F1413) else Color(0xFFF9F7F3)
                ),
                border = BorderStroke(1.dp, AccentGold.copy(0.15f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = news.source,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryEmerald
                        )
                        Text(
                            text = news.pubDate,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = news.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (amoled) Color.White else Color.Black,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = news.description,
                        fontSize = 11.sp,
                        color = if (amoled) Color.LightGray else Color.DarkGray,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// Educational Material System component
@Composable
fun EducationalPortalSection(amoled: Boolean) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("أركان الإسلام", "السنن وآداب", "بيت المقدس", "فقه الصلاة")

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = amoled
    ) {
        Text(
            text = "المَوْسُوعَةُ التَعْلِيمِيَّةُ لِلْمُسْلِمِ 📖",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGold,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = "تعلّم أهم مبادئ الشريعة والنهج القويم من مصادر موثوقة.",
            fontSize = 11.sp,
            color = if (amoled) Color(0xFF809892) else Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Tab selection pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tabs.forEachIndexed { idx, label ->
                val active = selectedTab == idx
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (active) PrimaryEmerald else (if (amoled) Color(0xFF1B2825) else Color(0xFFECEFF1)))
                        .clickable { selectedTab = idx }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (active) AccentGold else (if (amoled) Color.White else Color.Black),
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }

        // Tab Content Display Area inside Material 3 layouts
        Crossfade(targetState = selectedTab, label = "EduCrossfade") { tabIdx ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (amoled) Color(0xFF0D1412) else Color(0xFFFBF9F4))
                    .border(1.dp, AccentGold.copy(0.2f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                when (tabIdx) {
                    0 -> { // Pillars of Islam
                        Text(
                            text = "أركان الإسلام الخمسة:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AccentGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "١. شهادة أن لا إله إلا الله وأن محمداً رسول الله.\n" +
                                    "٢. إقام الصلاة مكتملة الطهارة فريضةً بالسكينة.\n" +
                                    "٣. إيتاء الزكاة الشرعية لتطهير المال وحفظ النفس.\n" +
                                    "٤. صيام شهر كرامة رمضان إخلاصاً واحتساباً.\n" +
                                    "٥. حج بيت الله الحرام لمن استطاع إليه سبيلاً.",
                            fontSize = 12.sp,
                            color = if (amoled) Color.LightGray else Color.DarkGray,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    1 -> { // Sunan and آداب
                        Text(
                            text = "سنن وآداب يومية مأثورة:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AccentGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• تسمية الله عند الأكل والحمد عند الفراغ للشكر.\n" +
                                    "• مسامحة الناس ومصافحتهم عند اللقاء تسليماً.\n" +
                                    "• التبكير لصلاة الفجر وقضاء الورد الصباحي.\n" +
                                    "• تبسمك في وجه أخيك صدقة تبسط المحبة.\n" +
                                    "• رعاية وإفشاء السلام حتى على من لم تعرِف.",
                            fontSize = 12.sp,
                            color = if (amoled) Color.LightGray else Color.DarkGray,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    2 -> { // Al Quds / Palestine Sacred Value
                        Text(
                            text = "عروبة وإسلامية القدس الشريف:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AccentGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• صلى المسلمون شطر بيت المقدس سبعة عشر شهراً قبل التحويل لـ مكة.\n" +
                                    "• أسري بالنبي ﷺ إليها وصلى بالأنبياء جميعاً إماماً دلالةً على القيادة الروحية.\n" +
                                    "• تشد إليها الرحال شرعاً: (لا تشد الرحال إلا إلى ثلاثة مساجد: المسجد الحرام، ومسجد الرسول، والمسجد الأقصى).\n" +
                                    "• تحرير بيت المقدس واجب عقدي وأخلاقي دائم على الأمة الإسلامية.",
                            fontSize = 12.sp,
                            color = if (amoled) Color.LightGray else Color.DarkGray,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    3 -> { // Purity & Salat Rules
                        Text(
                            text = "فقه الوضوء وشروط الصلاة الأساسية:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AccentGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• شروط الصلاة: طهارة البدن والثوب والمكان، وإسلام العبد، ودخول الوقت المحدد، واستقبال القبلة الكعبة، وستر العورة.\n" +
                                    "• أركان الصبح: النية الصادقة، تكبيرة الإحرام بالثبات، وقراءة الفاتحة وسورة، والركوع والارتفاع والسجود والطمأنينة الكاملة.\n" +
                                    "• السدل: إرسال اليدين في الصلاة سكينةً ووقاراً وهي السنة المأثورة الموروثة في عُمان.",
                            fontSize = 12.sp,
                            color = if (amoled) Color.LightGray else Color.DarkGray,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

// Developer independent REST/mock updates Events System
@Composable
fun DynamicBulletinsBoard(events: List<IslamicEvent>, amoled: Boolean) {
    if (events.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "لوحَةُ التَحْدِيثَاتِ وَأَحْدَاثُ الأُمَّةِ الـمُبَاشِرَةِ 📡",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGold,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        events.forEach { event ->
            // Parse custom color token
            val parsedColor = try {
                Color(android.graphics.Color.parseColor(event.color))
            } catch (e: Exception) {
                AccentGold
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (amoled) Color(0xFF0F1413) else Color(0xFFF4F6F5)
                ),
                border = BorderStroke(1.dp, parsedColor.copy(0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(parsedColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = event.tag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = parsedColor
                            )
                        }
                        Text(
                            text = event.date,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = event.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (amoled) Color.White else Color.Black,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.description,
                        fontSize = 12.sp,
                        color = if (amoled) Color.LightGray else Color.DarkGray,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun FastingCountdownCard(
    daysToRamadan: Int,
    pTimes: com.example.core.data.PrayerTimeCalculator.PrayerTimes?,
    amoled: Boolean
) {
    val imsakTime = remember(pTimes) {
        pTimes?.fajr?.let { adjustTimeStr(it, -10) } ?: "--:--"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = amoled
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "الاسْتِعْدَادُ لِشَهْرِ رَمَضَانَ المُعَظَّمِ 🌙",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Serif
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (daysToRamadan == 0) {
                    Text(
                        text = "رمضان مبارك كريم! 🌙",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = FontFamily.Serif
                    )
                } else {
                    Text(
                        text = "متبقي $daysToRamadan يوماً حتى رمضان المبارك",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (amoled) Color.White else Color.Black,
                        fontFamily = FontFamily.Serif
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "وقت الإمساك الاحتياطي: $imsakTime",
                    fontSize = 12.sp,
                    color = if (amoled) Color(0xFF90A49F) else Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0x1F0F5B46)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Brightness3,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun QuranResumeCard(viewModel: MainViewModel) {
    val amoled = viewModel.prefs.amoledMode
    val lastReadSurahNum = viewModel.prefs.lastReadSurah
    val lastReadPageNum = viewModel.prefs.lastReadPage
    val lastReadSurahObj = remember(lastReadSurahNum) {
        if (lastReadSurahNum in 1..114) {
            QuranData.fullSurahHeadersList.firstOrNull { it.number == lastReadSurahNum }
        } else null
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = amoled
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "مُتَابَعَة تِلاوَة الكِتَابِ المَقْدِسِ 📖",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Serif
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lastReadSurahObj?.let { "سورة ${it.nameAr}" } ?: "ابدأ قراءة القرآن الكريم",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (amoled) Color.White else Color.Black,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = lastReadSurahObj?.let { "الصفحة ${lastReadPageNum + 1} • اضغط للمتابعة الآن" } ?: "اختر السور والصفحات من المصحف المنظم",
                    fontSize = 11.sp,
                    color = if (amoled) Color(0xFF90A49F) else Color.Gray
                )
            }

            IconButton(
                onClick = {
                    if (lastReadSurahObj != null) {
                        viewModel.selectSurah(lastReadSurahObj)
                    }
                    viewModel.setScreen(Screen.QURAN)
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = PrimaryEmerald
                ),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Book,
                    contentDescription = "Resume",
                    tint = AccentGold
                )
            }
        }
    }
}

@Composable
fun HadithHighlightCard(viewModel: MainViewModel) {
    val randomHadith = remember { HadithData.rabiHadithsList[0] }
    val amoled = viewModel.prefs.amoledMode

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = amoled
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مُخْتَصَرُ مَتْنِ الجَامِعِ الصَّحِيحِ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "من مسند الإمام الربيع بن حبيب رحمه الله",
                        fontSize = 11.sp,
                        color = if (amoled) Color(0xFF90A49F) else Color.Gray,
                        fontFamily = FontFamily.Serif
                    )
                }

                Button(
                    onClick = { viewModel.setScreen(Screen.HADITH) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x3B129676),
                        contentColor = AccentGold
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("المزيد", fontSize = 11.sp, fontFamily = FontFamily.Serif)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = randomHadith.textAr,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGold,
                textAlign = TextAlign.Right,
                lineHeight = 24.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = randomHadith.textEn,
                fontSize = 12.sp,
                color = if (amoled) Color.LightGray else Color.DarkGray,
                lineHeight = 16.sp
            )
        }
    }
}

// Helper calculations & algorithms
data class CountdownData(
    val prayerName: String,
    val remainingStr: String,
    val athanTime: String
)

fun calculateCountdown(
    times: com.example.core.data.PrayerTimeCalculator.PrayerTimes,
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
        // All prayers passed, next is tomorrow's Fajr
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

fun adjustTimeStr(orig: String, deltaMin: Int): String {
    return try {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = format.parse(orig) ?: return orig
        val cal = Calendar.getInstance().apply {
            time = date
            add(Calendar.MINUTE, deltaMin)
        }
        format.format(cal.time)
    } catch (e: Exception) {
        orig
    }
}

fun calculateIqamaTime(athaTime: String, offsetMins: Int): String {
    return try {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = format.parse(athaTime) ?: return athaTime
        val cal = Calendar.getInstance().apply {
            time = date
            add(Calendar.MINUTE, offsetMins)
        }
        format.format(cal.time)
    } catch (e: Exception) {
        athaTime
    }
}
