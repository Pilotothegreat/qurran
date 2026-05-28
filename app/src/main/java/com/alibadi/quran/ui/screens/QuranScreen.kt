package com.alibadi.quran.ui.screens

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.VolumeOff
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibadi.quran.R
import com.alibadi.quran.core.data.entities.CachedVerse
import com.alibadi.quran.core.data.entities.Surah
import com.alibadi.quran.feature.quran.QuranLoadResult
import com.alibadi.quran.feature.quran.QuranViewModel
import com.alibadi.quran.ui.theme.accentGold
import com.alibadi.quran.ui.theme.primaryEmerald
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranIndexScreen(
    viewModel: QuranViewModel,
    onSurahClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val surahs by viewModel.surahs.collectAsState()
    val quranHistory by viewModel.quranHistory.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val lastReadSurahNum = quranHistory?.surahNumber ?: 0
    val lastReadPageNum = quranHistory?.ayahNumber ?: 1 // defaults page count or index
    val lastReadSurahObj = remember(lastReadSurahNum, surahs) {
        if (lastReadSurahNum in 1..114) {
            surahs.firstOrNull { it.number == lastReadSurahNum }
        } else null
    }

    val filteredSurahs = remember(searchQuery, surahs) {
        if (searchQuery.isBlank()) {
            surahs
        } else {
            surahs.filter {
                it.nameEn.contains(searchQuery, ignoreCase = true) ||
                        it.englishMeaning.contains(searchQuery, ignoreCase = true) ||
                        it.nameAr.contains(searchQuery)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Last Read Card
        if (lastReadSurahObj != null) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable {
                        onSurahClick(lastReadSurahObj.number)
                    },
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(primaryEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = accentGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.last_read_label, lastReadSurahObj.nameAr),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = accentGold
                            )
                            Text(
                                text = stringResource(R.string.last_read_page_desc, lastReadPageNum),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = accentGold
                    )
                }
            }
        }

        // Header Label
        Text(
            text = stringResource(R.string.quran_index_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = accentGold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(stringResource(R.string.search_surah_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = accentGold) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentGold,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredSurahs, key = { it.number }) { surah ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSurahClick(surah.number) },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = surah.number.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = accentGold
                                )
                            }

                            Column {
                                Text(
                                    text = surah.nameEn,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${surah.englishMeaning} • ${surah.versesCount} verses",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = surah.nameAr,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = accentGold
                            )
                            Text(
                                text = if (surah.type == "Meccan") "مكية" else "مدنية",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (surah.type == "Meccan") accentGold else primaryEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    surahNumber: Int,
    viewModel: QuranViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surahs by viewModel.surahs.collectAsState()
    val loadedVersesState by viewModel.loadedVersesState.collectAsState()
    val context = LocalContext.current

    val surah = remember(surahNumber, surahs) {
        surahs.firstOrNull { it.number == surahNumber }
    }

    LaunchedEffect(surahNumber) {
        viewModel.loadQuranVerses(surahNumber)
    }

    if (surah == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = accentGold)
        }
        return
    }

    when (val state = loadedVersesState) {
        is QuranLoadResult.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = accentGold)
                    Text(
                        text = stringResource(R.string.loading_verses),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentGold
                    )
                }
            }
        }
        is QuranLoadResult.Success -> {
            QuranReaderContent(
                surah = surah,
                verses = state.verses,
                viewModel = viewModel,
                onBack = onBackClick,
                modifier = modifier
            )
        }
        is QuranLoadResult.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = accentGold,
                        modifier = Modifier.size(64.dp)
                    )

                    Text(
                        text = stringResource(R.string.offline_need_internet_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentGold
                    )

                    Text(
                        text = stringResource(R.string.offline_need_internet_desc, surah.nameAr),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { viewModel.loadQuranVerses(surah.number) },
                        colors = ButtonDefaults.buttonColors(containerColor = accentGold),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(stringResource(R.string.retry_button), color = MaterialTheme.colorScheme.onTertiary)
                    }

                    TextButton(onClick = onBackClick) {
                        Text(stringResource(R.string.back_index_button), color = accentGold)
                    }
                }
            }
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accentGold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderContent(
    surah: Surah,
    verses: List<CachedVerse>,
    viewModel: QuranViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fontSize by remember { mutableFloatStateOf(24f) }
    var bookMode by remember { mutableStateOf(false) }
    var translationLang by remember { mutableStateOf("EN") }

    val lastPageBookmark by viewModel.lastReadPage.collectAsState(initial = 0)

    val versesPerPage = 10
    val totalPages = remember(verses) { ((verses.size + versesPerPage - 1) / versesPerPage).coerceAtLeast(1) }

    var currentPage by remember(surah) {
        mutableIntStateOf(lastPageBookmark.coerceIn(0, totalPages - 1))
    }

    // Save position
    LaunchedEffect(currentPage) {
        viewModel.saveLastReadPage(currentPage)
    }

    val context = LocalContext.current
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var activeSpeakingIndex by remember { mutableIntStateOf(-1) }

    DisposableEffect(Unit) {
        val speech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Setup
            }
        }
        tts = speech
        onDispose {
            speech.stop()
            speech.shutdown()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Quick Tool Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentGold)
                }
                Column {
                    Text(
                        text = "سورة ${surah.nameAr}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentGold
                    )
                    Text(
                        text = "${surah.nameEn} • ${surah.versesCount} verses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                // Translation Toggle
                IconButton(
                    onClick = {
                        translationLang = when (translationLang) {
                            "EN" -> "NONE"
                            else -> "EN"
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Translate,
                        contentDescription = "Translate",
                        tint = if (translationLang == "NONE") MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else accentGold
                    )
                }

                // Layout Mode Toggle
                IconButton(
                    onClick = { bookMode = !bookMode }
                ) {
                    Icon(
                        imageVector = if (bookMode) Icons.Default.MenuBook else Icons.Default.FormatListNumbered,
                        contentDescription = "Toggle View Mode",
                        tint = accentGold
                    )
                }

                // Font adjustment
                IconButton(onClick = { if (fontSize > 16f) fontSize -= 2f }) {
                    Text("أ-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accentGold)
                }
                IconButton(onClick = { if (fontSize < 36f) fontSize += 2f }) {
                    Text("أ+", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accentGold)
                }
            }
        }

        if (bookMode) {
            // Book Mode Pagination Slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.quick_jump_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = currentPage.toFloat(),
                    onValueChange = { currentPage = it.toInt().coerceIn(0, totalPages - 1) },
                    valueRange = 0f..(totalPages - 1).coerceAtLeast(1).toFloat(),
                    steps = if (totalPages > 2) totalPages - 2 else 0,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = accentGold,
                        activeTrackColor = primaryEmerald
                    )
                )
                Text(
                    text = "ص ${currentPage + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentGold
                )
            }

            val pageVerses = remember(verses, currentPage) {
                val start = currentPage * versesPerPage
                val end = minOf(start + versesPerPage, verses.size)
                if (start < verses.size) verses.subList(start, end) else emptyList()
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = BorderStroke(2.dp, accentGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Basmala
                        if (currentPage == 0 && surah.number != 1 && surah.number != 9) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
                                    fontSize = (fontSize - 2).coerceAtLeast(18f).sp,
                                    fontFamily = FontFamily.Serif,
                                    color = accentGold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Text continuous format
                        val continuousArabic = buildString {
                            pageVerses.forEach { verse ->
                                append(verse.textAr)
                                append(" ﴿")
                                append(verse.ayahNumber)
                                append("﴾ ")
                            }
                        }

                        Text(
                            text = continuousArabic,
                            fontSize = fontSize.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium,
                            color = primaryEmerald,
                            textAlign = TextAlign.Right,
                            lineHeight = (fontSize * 1.8).sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }

                    // Flip controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { if (currentPage > 0) currentPage-- },
                            enabled = currentPage > 0
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.prev_page_button), style = MaterialTheme.typography.labelMedium)
                        }

                        Text(
                            text = stringResource(R.string.page_num_label, currentPage + 1, totalPages),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = { if (currentPage < totalPages - 1) currentPage++ },
                            enabled = currentPage < totalPages - 1
                        ) {
                            Text(stringResource(R.string.next_page_button), style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        } else {
            // List view
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (surah.number != 1 && surah.number != 9) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
                                fontSize = fontSize.coerceAtLeast(20f).sp,
                                fontFamily = FontFamily.Serif,
                                color = accentGold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                items(verses) { verse ->
                    val isSpeaking = activeSpeakingIndex == verse.ayahNumber
                    val isBookmarkedFlow = viewModel.isBookmarked(surah.number, verse.ayahNumber)
                    val isBookmarked by isBookmarkedFlow.collectAsState(initial = false)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSpeaking) accentGold.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceContainer
                        )
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
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(primaryEmerald.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = verse.ayahNumber.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = accentGold
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (isSpeaking) {
                                                tts?.stop()
                                                activeSpeakingIndex = -1
                                            } else {
                                                activeSpeakingIndex = verse.ayahNumber
                                                val speakText = if (translationLang == "NONE") verse.textAr else verse.textEn
                                                val speakLocale = if (translationLang == "NONE") Locale("ar") else Locale.US
                                                try {
                                                    tts?.setLanguage(speakLocale)
                                                } catch (e: Exception) {}
                                                tts?.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "QuranRecite")
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSpeaking) Icons.Outlined.VolumeOff else Icons.Outlined.VolumeUp,
                                            contentDescription = "TTS Recite",
                                            tint = if (isSpeaking) primaryEmerald else accentGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.toggleBookmark(
                                                surahNum = surah.number,
                                                surahName = surah.nameEn,
                                                ayahNum = verse.ayahNumber,
                                                verseText = verse.textAr
                                            )
                                            Toast.makeText(context, context.getString(R.string.bookmark_update_toast), Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = "Bookmark",
                                            tint = accentGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = verse.textAr,
                                fontSize = fontSize.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = primaryEmerald,
                                lineHeight = (fontSize * 1.6).sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (translationLang != "NONE") {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = accentGold.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = verse.textEn,
                                    fontSize = (fontSize - 9).coerceAtLeast(13f).sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = (fontSize - 5).coerceAtLeast(18f).sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
