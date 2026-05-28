package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.adhkar.AdhkarData
import com.example.feature.adhkar.AdhkarItem
import com.example.ui.MainViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHighlightCard

private val AccentGold: Color @Composable get() = MaterialTheme.colorScheme.tertiary
private val PrimaryEmerald: Color @Composable get() = MaterialTheme.colorScheme.primary


@Composable
fun AdhkarScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentCategory by viewModel.selectedAdhkarCategory.collectAsState()
    val counts by viewModel.adhkarCounts.collectAsState()
    val amoled = viewModel.prefs.amoledMode
    val translationLang = viewModel.prefs.quranLanguage

    // Posture help state tab toggled
    var activeSubTab by remember { mutableStateOf("ADH_LIST") } // "ADH_LIST" or "STAND_TASBIH" or "POSTURE_GUIDE"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Core Selection Tab
        Text(
            text = if (translationLang == "NONE") "الْأَذْكَارُ وَالأَدْعِيَةُ اليَوْمِيَّةُ" else "REMEMBER ALLAH (DHIKR)",
            fontSize = if (translationLang == "NONE") 20.sp else 22.sp,
            fontWeight = FontWeight.Black,
            color = AccentGold,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = if (translationLang == "NONE") "«أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ»" else "Keep your tongue moist with remembrance",
            fontSize = 12.sp,
            color = if (amoled) Color(0xFF90A49F) else Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp),
            fontFamily = FontFamily.Serif
        )

        // Top sub-tabs: Adhkar Chapters, Tasbih Counter, Posture Detail
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "ADH_LIST" to if (translationLang == "NONE") "قوائم الأذكار" else "Adhkar Lists",
                "STAND_TASBIH" to if (translationLang == "NONE") "مسبحة الذكر" else "Tasbih Ring",
                "POSTURE_GUIDE" to if (translationLang == "NONE") "صفة الصلاة" else "Prayer Style"
            ).forEach { (tabKey, tabLabel) ->
                Button(
                    onClick = { activeSubTab = tabKey },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubTab == tabKey) PrimaryEmerald else Color(0x1F129676),
                        contentColor = if (activeSubTab == tabKey) AccentGold else Color.LightGray
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(tabLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Animated content based on selection
        AnimatedContent(
            targetState = activeSubTab,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "AdhkarSubFlow"
        ) { subTab ->
            when (subTab) {
                "ADH_LIST" -> AdhkarListsContent(viewModel = viewModel, currentCategory = currentCategory, counts = counts, amoled = amoled, translationLang = translationLang)
                "STAND_TASBIH" -> StandaloneTasbihRing(viewModel = viewModel, amoled = amoled)
                "POSTURE_GUIDE" -> OmaniPostureInstructions(amoled = amoled)
            }
        }
    }
}

@Composable
fun AdhkarListsContent(
    viewModel: MainViewModel,
    currentCategory: String,
    counts: Map<Int, Int>,
    amoled: Boolean,
    translationLang: String
) {
    val items = remember(currentCategory) {
        AdhkarData.adhkarList.filter { it.category == currentCategory }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Toggle category row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("Post-Prayer", "بعد الصلاة", "Post-Prayer"),
                Triple("Morning", "الصباح", "Morning"),
                Triple("Evening", "المساء", "Evening")
            ).forEach { (catId, catArName, catEnName) ->
                val label = if (translationLang == "NONE") catArName else catEnName
                Button(
                    onClick = { viewModel.setAdhkarCategory(catId) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentCategory == catId) AccentGold else Color(0x0EFFFFFF),
                        contentColor = if (currentCategory == catId) Color.Black else Color.LightGray
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Recycler values
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items) { item ->
                val currentC = counts[item.id] ?: 0
                AdhkarCountRow(
                    item = item,
                    currentC = currentC,
                    onTap = { viewModel.incrementAdhkar(item) },
                    onReset = { viewModel.resetAdhkar(item.id) },
                    amoled = amoled,
                    translationLang = translationLang
                )
            }
        }
    }
}

@Composable
fun AdhkarCountRow(
    item: AdhkarItem,
    currentC: Int,
    onTap: () -> Unit,
    onReset: () -> Unit,
    amoled: Boolean,
    translationLang: String
) {
    val context = LocalContext.current
    val isComplete = currentC >= item.countTarget

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                triggerHapticFeedback(context)
                onTap()
            },
        isDark = amoled
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Circle Badge Tracker
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isComplete) PrimaryEmerald else Color(0x3B129676)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$currentC/${item.countTarget}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isComplete) AccentGold else Color.White
                    )
                }

                // Reset capability button
                IconButton(onClick = onReset, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = AccentGold, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arabic text
            Text(
                text = item.textAr,
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = if (isComplete) Color.Gray else AccentGold,
                textAlign = TextAlign.Right,
                lineHeight = 28.sp,
                modifier = Modifier.fillMaxWidth()
            )

            if (translationLang != "NONE") {
                Spacer(modifier = Modifier.height(4.dp))

                // Translation text
                Text(
                    text = item.textEn,
                    fontSize = 12.sp,
                    color = if (isComplete) Color.Gray else Color.LightGray,
                    lineHeight = 16.sp
                )
            }

            val virtueToShow = if (translationLang == "NONE") item.virtueAr else item.virtueEn
            if (virtueToShow.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = virtueToShow,
                    fontSize = 11.sp,
                    color = if (amoled) Color(0xFF6B7A77) else Color.Gray,
                    fontStyle = if (translationLang == "NONE") androidx.compose.ui.text.font.FontStyle.Normal else androidx.compose.ui.text.font.FontStyle.Italic,
                    textAlign = if (translationLang == "NONE") TextAlign.Right else TextAlign.Left,
                    fontFamily = if (translationLang == "NONE") FontFamily.Serif else FontFamily.Default,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun StandaloneTasbihRing(viewModel: MainViewModel, amoled: Boolean) {
    val counter by viewModel.standaloneTasbihCount.collectAsState()
    val phraseIdx by viewModel.standaloneDhikrNameIdx.collectAsState()
    val phrases = viewModel.standaloneDhikrPhrases

    val activePhrase = phrases[phraseIdx]
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Selection of active dhikr
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.cycleStandaloneDhikr() }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Prev", tint = AccentGold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = activePhrase.first,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                )
                Text(
                    text = activePhrase.second,
                    fontSize = 11.sp,
                    color = if (amoled) Color.LightGray else Color.DarkGray
                )
            }
            IconButton(
                onClick = { viewModel.cycleStandaloneDhikr() }
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = AccentGold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large Ripple Tap circle button
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(androidx.compose.ui.graphics.Brush.radialGradient(listOf(Color(0x3B129676), Color(0x05129676))))
                .border(2.dp, AccentGold, CircleShape)
                .clickable {
                    triggerHapticFeedback(context)
                    viewModel.incrementStandaloneTasbih()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = counter.toString(),
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    color = if (amoled) Color.White else Color.Black
                )
                Text(
                    text = "TAP HERE TO COUNT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reset Standalone counts button
        OutlinedButton(
            onClick = { viewModel.resetStandaloneTasbih() },
            border = BorderStroke(1.dp, Color.Gray),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGold),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Reset Count")
        }
    }
}

@Composable
fun OmaniPostureInstructions(amoled: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassHighlightCard(
            modifier = Modifier.fillMaxWidth(),
            isDark = amoled
        ) {
            Text(
                text = "IBADI FAITH & POSTURE GUIDE",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = AccentGold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "The Ibadi school of Islamic jurisprudence, which is predominantly practiced in Oman, maintains established postures and details in alignment with canonical early compilations like Jami' al-Sahih of Imam al-Rabi' bin Habib.",
                fontSize = 12.sp,
                color = if (amoled) Color.LightGray else Color.DarkGray,
                lineHeight = 18.sp
            )
        }

        // Posture Card 1: Al-Sadl
        GlassCard(
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
                    Icon(Icons.Default.AccessibilityNew, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "1. Prayer Arms Position (Al-Sadl)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Keeping the arms straight down at the sides (*sadl*) during the standing posture of prayer, rather than folding or clasping them. This is the authentic practice of early companions and the established position of Ibadi legal rulings.",
                        fontSize = 12.sp,
                        color = if (amoled) Color.LightGray else Color.DarkGray,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Posture Card 2: No Qunoot
        GlassCard(
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
                    Icon(Icons.Default.DoNotDisturb, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "2. No Qunoot Supplication",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Recitations by default do not include any loud qunoot (invocational) supplications during Sunrise/Fajr or Witr prayers, preserving quietude and individual contemplation.",
                        fontSize = 12.sp,
                        color = if (amoled) Color.LightGray else Color.DarkGray,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

// Vibrator tool
private fun triggerHapticFeedback(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    } catch (e: Exception) {}
}
