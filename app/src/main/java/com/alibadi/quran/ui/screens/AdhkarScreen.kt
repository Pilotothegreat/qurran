package com.alibadi.quran.ui.screens

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
import com.alibadi.quran.core.data.entities.AdhkarItem
import com.alibadi.quran.feature.adhkar.AdhkarViewModel
import com.alibadi.quran.feature.settings.SettingsViewModel
import com.alibadi.quran.ui.theme.accentGold
import com.alibadi.quran.ui.theme.primaryEmerald
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AdhkarScreen(
    viewModel: AdhkarViewModel,
    modifier: Modifier = Modifier
) {
    val settingsViewModel: SettingsViewModel = koinViewModel()

    val currentCategory by viewModel.selectedAdhkarCategory.collectAsState()
    val counts by viewModel.adhkarCounts.collectAsState()
    val amoled by settingsViewModel.amoled.collectAsState()
    val translationLang by settingsViewModel.quranLanguage.collectAsState()

    var activeSubTab by remember { mutableStateOf("ADH_LIST") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (translationLang == "NONE") "الأذكار والأدعية اليومية" else "Daily Adhkar & Remembrance",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = accentGold,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = if (translationLang == "NONE") "ألا بذكر الله تطمئن القلوب" else "Keep your tongue moist with remembrance",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
            fontFamily = FontFamily.Serif
        )

        // Sub tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "ADH_LIST" to (if (translationLang == "NONE") "قوائم الأذكار" else "Adhkar Lists"),
                "STAND_TASBIH" to (if (translationLang == "NONE") "مسبحة الذكر" else "Tasbih Ring"),
                "POSTURE_GUIDE" to (if (translationLang == "NONE") "صفة الصلاة" else "Prayer Style")
            ).forEach { (tabKey, tabLabel) ->
                Button(
                    onClick = { activeSubTab = tabKey },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubTab == tabKey) primaryEmerald else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (activeSubTab == tabKey) accentGold else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(tabLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        AnimatedContent(
            targetState = activeSubTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "AdhkarSubFlow",
            modifier = Modifier.weight(1f)
        ) { subTab ->
            when (subTab) {
                "ADH_LIST" -> AdhkarListsContent(viewModel = viewModel, currentCategory = currentCategory, counts = counts, translationLang = translationLang)
                "STAND_TASBIH" -> StandaloneTasbihRing(viewModel = viewModel, amoled = amoled)
                "POSTURE_GUIDE" -> OmaniPostureInstructions()
            }
        }
    }
}

@Composable
fun AdhkarListsContent(
    viewModel: AdhkarViewModel,
    currentCategory: String,
    counts: Map<Int, Int>,
    translationLang: String
) {
    val items by viewModel.filteredAdhkarItems.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
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
                        containerColor = if (currentCategory == catId) accentGold else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (currentCategory == catId) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items, key = { it.id }) { item ->
                val currentC = counts[item.id] ?: 0
                AdhkarCountRow(
                    item = item,
                    currentC = currentC,
                    onTap = { viewModel.incrementAdhkar(item) },
                    onReset = { viewModel.resetAdhkar(item.id) },
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
    translationLang: String
) {
    val context = LocalContext.current
    val isComplete = currentC >= item.countTarget

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                triggerHapticFeedback(context)
                onTap()
            },
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isComplete) primaryEmerald else primaryEmerald.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$currentC/${item.countTarget}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isComplete) accentGold else MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onReset, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = accentGold, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.textAr,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = if (isComplete) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else accentGold,
                textAlign = TextAlign.Right,
                lineHeight = 28.sp,
                modifier = Modifier.fillMaxWidth()
            )

            if (translationLang != "NONE") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.textEn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isComplete) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            val virtueToShow = if (translationLang == "NONE") item.virtueAr else item.virtueEn
            if (virtueToShow.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = virtueToShow,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = if (translationLang == "NONE") TextAlign.Right else TextAlign.Left,
                    fontFamily = if (translationLang == "NONE") FontFamily.Serif else FontFamily.Default,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun StandaloneTasbihRing(viewModel: AdhkarViewModel, amoled: Boolean) {
    val counter by viewModel.standaloneTasbihCount.collectAsState()
    val phraseIdx by viewModel.standaloneDhikrNameIdx.collectAsState()
    val phrases = viewModel.standaloneDhikrPhrases

    val activePhrase = phrases[phraseIdx]
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.cycleStandaloneDhikr() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Prev", tint = accentGold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = activePhrase.first,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = accentGold
                )
                Text(
                    text = activePhrase.second,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { viewModel.cycleStandaloneDhikr() }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = accentGold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(primaryEmerald.copy(alpha = 0.15f))
                .border(2.dp, accentGold, CircleShape)
                .clickable {
                    triggerHapticFeedback(context)
                    viewModel.incrementStandaloneTasbih()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = counter.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "TAP HERE TO COUNT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentGold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { viewModel.resetStandaloneTasbih() },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Reset Count", color = accentGold)
        }
    }
}

@Composable
fun OmaniPostureInstructions() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "IBADI FAITH & POSTURE GUIDE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = accentGold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "The Ibadi school of Islamic jurisprudence maintains established postures and details in alignment with canonical early compilations like Jami' al-Sahih of Imam al-Rabi' bin Habib.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    lineHeight = 18.sp
                )
            }
        }

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
                    Icon(Icons.Default.AccessibilityNew, contentDescription = null, tint = accentGold, modifier = Modifier.size(18.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "1. Prayer Arms Position (Al-Sadl)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Keeping the arms straight down at the sides (*sadl*) during the standing posture of prayer, rather than folding or clasping them. This is the authentic practice of early companions and the established position of Ibadi legal rulings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }
        }

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
                    Icon(Icons.Default.DoNotDisturb, contentDescription = null, tint = accentGold, modifier = Modifier.size(18.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "2. No Qunoot Supplication",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Recitations by default do not include any loud qunoot (invocational) supplications during Sunrise/Fajr or Witr prayers, preserving quietude and individual contemplation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

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
