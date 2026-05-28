package com.example.ui.screens

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.data.entities.QuranBookmark
import com.example.core.data.entities.CachedVerse
import com.example.ui.QuranLoadResult
import com.example.feature.quran.QuranData
import com.example.feature.quran.QuranVerse
import com.example.feature.quran.Surah
import com.example.ui.MainViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHighlightCard
import kotlinx.coroutines.launch
import java.util.Locale

private val AccentGold: Color @Composable get() = MaterialTheme.colorScheme.tertiary
private val PrimaryEmerald: Color @Composable get() = MaterialTheme.colorScheme.primary
private val AccentGoldLight: Color @Composable get() = MaterialTheme.colorScheme.tertiaryContainer

@Composable
fun QuranScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSurah by viewModel.selectedSurah.collectAsState()
    val amoled = viewModel.prefs.amoledMode

    if (selectedSurah != null) {
        androidx.activity.compose.BackHandler {
            viewModel.selectSurah(null)
        }
    }

    AnimatedContent(
        targetState = selectedSurah,
        transitionSpec = {
            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
        },
        label = "QuranFlow"
    ) { surah ->
        if (surah == null) {
            SurahIndexView(viewModel = viewModel, amoled = amoled)
        } else {
            QuranReaderView(
                surah = surah,
                viewModel = viewModel,
                onBack = { viewModel.selectSurah(null) },
                amoled = amoled
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahIndexView(viewModel: MainViewModel, amoled: Boolean) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Load last read Surah
    val lastReadSurahNum = viewModel.prefs.lastReadSurah
    val lastReadPageNum = viewModel.prefs.lastReadPage
    val lastReadSurahObj = remember(lastReadSurahNum) {
        if (lastReadSurahNum in 1..114) {
            QuranData.fullSurahHeadersList.firstOrNull { it.number == lastReadSurahNum }
        } else null
    }

    val filteredSurahs = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            QuranData.fullSurahHeadersList
        } else {
            QuranData.fullSurahHeadersList.filter {
                it.nameEn.contains(searchQuery, ignoreCase = true) ||
                        it.englishMeaning.contains(searchQuery, ignoreCase = true) ||
                        it.nameAr.contains(searchQuery)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Last Read Card (QoL shortcut)
        if (lastReadSurahObj != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable {
                        viewModel.selectSurah(lastReadSurahObj)
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (amoled) Color(0xFF0F201C) else Color(0xFFE8F3F0)
                ),
                border = BorderStroke(1.5.dp, AccentGold)
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
                                .background(PrimaryEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "آخر قراءة • سورة ${lastReadSurahObj.nameAr}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = AccentGold
                            )
                            Text(
                                text = "اضغط للمتابعة من الصفحة ${lastReadPageNum + 1}",
                                fontSize = 12.sp,
                                color = if (amoled) Color.LightGray else Color.DarkGray
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = AccentGold
                    )
                }
            }
        }

        // Header Label
        Text(
            text = "فهرس سور القرآن الكريم",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = AccentGold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Search Bar (Arabic prioritized)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث عن سورة بالاسم العربي أو الإنجليزي...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGold) },
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
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGold,
                unfocusedBorderColor = if (amoled) Color(0xFF223A33) else Color.LightGray
            )
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredSurahs) { surah ->
                SurahItemRow(surah = surah, onClick = { viewModel.selectSurah(surah) }, amoled = amoled)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SurahItemRow(surah: Surah, onClick: () -> Unit, amoled: Boolean) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        isDark = amoled
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circle Number
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (amoled) Color(0xFF1B2825) else Color(0xFFE2E9E6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = surah.number.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )
                }

                // Name Details
                Column {
                    Text(
                        text = surah.nameEn,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (amoled) Color.White else Color.Black
                    )
                    Text(
                        text = "${surah.englishMeaning} • ${surah.versesCount} verses",
                        fontSize = 12.sp,
                        color = if (amoled) Color(0xFF90A49F) else Color.Gray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = surah.nameAr,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                )
                Text(
                    text = if (surah.type == "Meccan") "مكية" else "مدنية",
                    fontSize = 10.sp,
                    color = if (surah.type == "Meccan") Color(0xFFD4AF37) else PrimaryEmerald,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderView(
    surah: Surah,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    amoled: Boolean
) {
    val loadedVersesState by viewModel.loadedVersesState.collectAsState()

    when (val state = loadedVersesState) {
        is QuranLoadResult.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (amoled) Color(0xFF070B0A) else Color(0xFFF4F6F5)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = AccentGold)
                    Text(
                        text = "جاري تحميل وتدقيق الآيات العثمانية الشريفة...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold,
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }
        is QuranLoadResult.Success -> {
            QuranReaderContent(
                surah = surah,
                verses = state.verses,
                viewModel = viewModel,
                onBack = onBack,
                amoled = amoled
            )
        }
        is QuranLoadResult.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (amoled) Color(0xFF070B0A) else Color(0xFFF4F6F5))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.message == "offline_need_internet") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(64.dp)
                        )
                        
                        Text(
                            text = "السورة غير متوفرة دون اتصال",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "تحميل سورة \"${surah.nameAr}\" لأول مرة يتطلب اتصالاً بنشاط بالإنترنت لحفظ المصحف الشريف في الذاكرة المحلية للجهاز.",
                            fontSize = 14.sp,
                            color = if (amoled) Color.LightGray else Color.DarkGray,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        
                        Text(
                            text = "السور التالية متوفرة دائماً دون اتصال بالإنترنت:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (amoled) Color(0xFF90A49F) else Color.Gray,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center
                        )
                        
                        // Row of Offline Surah Chips with clean auto-wrapping
                        val offlineIds = listOf(1, 67, 97, 103, 108, 110, 112, 113, 114)
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            offlineIds.forEach { id ->
                                val sObj = remember { QuranData.fullSurahHeadersList.firstOrNull { it.number == id } }
                                if (sObj != null) {
                                    Button(
                                        onClick = {
                                            viewModel.selectSurah(sObj)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PrimaryEmerald.copy(alpha = 0.2f),
                                            contentColor = AccentGold
                                        ),
                                        shape = RoundedCornerShape(20.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text(sObj.nameAr, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { viewModel.loadQuranVerses(surah.number) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إعادة المحاولة بعد الاتصال بالإنترنت", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        
                        TextButton(
                            onClick = onBack,
                            colors = ButtonDefaults.textButtonColors(contentColor = AccentGold)
                        ) {
                            Text("الرجوع لفهرس السور", fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "عفواً، فشل في تحميل الآيات: ${state.message}",
                            color = Color.Red,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.loadQuranVerses(surah.number) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("إعادة المحاولة", color = Color.White)
                        }
                        TextButton(onClick = onBack, colors = ButtonDefaults.textButtonColors(contentColor = AccentGold)) {
                            Text("رجوع", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        else -> {
            LaunchedEffect(surah) {
                viewModel.loadQuranVerses(surah.number)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderContent(
    surah: Surah,
    verses: List<CachedVerse>,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    amoled: Boolean
) {
    val coroutineScope = rememberCoroutineScope()

    // Config parameters
    var fontSize by remember { mutableStateOf(viewModel.prefs.quranFontSize) }
    var translationLang by remember { mutableStateOf(viewModel.prefs.quranLanguage) } // "EN", "SW", "NONE"
    var bookMode by remember { mutableStateOf(viewModel.prefs.quranBookMode) }

    // Book Mode Pagination Calculations (verses grouped per Page)
    val versesPerPage = 10
    val totalPages = remember(verses) { ((verses.size + versesPerPage - 1) / versesPerPage).coerceAtLeast(1) }
    
    // Set current page initially to saved bookmark if matching same Surah
    var currentPage by remember(surah) {
        val savedSurah = viewModel.prefs.lastReadSurah
        val savedPage = viewModel.prefs.lastReadPage
        if (savedSurah == surah.number && savedPage in 0 until totalPages) {
            mutableStateOf(savedPage)
        } else {
            mutableStateOf(0)
        }
    }

    // Save reading state when page flip occurs
    LaunchedEffect(currentPage) {
        viewModel.prefs.lastReadSurah = surah.number
        viewModel.prefs.lastReadPage = currentPage
    }

    // Text to speech Setup
    val context = LocalContext.current
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var activeSpeakingIndex by remember { mutableStateOf(-1) }

    DisposableEffect(Unit) {
        var speech: TextToSpeech? = null
        try {
            speech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        speech?.language = Locale.US
                    } catch (e: Exception) {}
                }
            }
            tts = speech
        } catch (e: Exception) {
            // TTS engine not available
        }
        
        onDispose {
            try {
                speech?.stop()
                speech?.shutdown()
            } catch (e: Exception) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (amoled) Color(0xFF070B0A) else Color(0xFFF4F6F5))
    ) {
        // Upper Navigation Reader Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AccentGold)
                }
                Column {
                    Text(
                        text = "سورة ${surah.nameAr}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )
                    Text(
                        text = "${surah.nameEn} • ${surah.versesCount} آية",
                        fontSize = 11.sp,
                        color = if (amoled) Color(0xFF90A49F) else Color.Gray
                    )
                }
            }

            // Quick Tool Controls inside Reader Top Bar
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                // Translator Toggle (Looping: Arabic and Translators)
                IconButton(
                    onClick = {
                        val nextLang = when (translationLang) {
                            "EN" -> "SW"
                            "SW" -> "NONE"
                            else -> "EN"
                        }
                        translationLang = nextLang
                        viewModel.prefs.quranLanguage = nextLang
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Translate,
                        contentDescription = "Translate",
                        tint = if (translationLang == "NONE") Color.Gray else AccentGold
                    )
                }

                // Toggle Display Mode
                IconButton(
                    onClick = {
                        val nextMode = !bookMode
                        bookMode = nextMode
                        viewModel.prefs.quranBookMode = nextMode
                    }
                ) {
                    Icon(
                        imageVector = if (bookMode) Icons.Default.MenuBook else Icons.Default.FormatListNumbered,
                        contentDescription = "Toggle View Mode",
                        tint = AccentGold
                    )
                }

                // Font Expansion Sizers
                IconButton(
                    onClick = {
                        if (fontSize > 16f) {
                            fontSize -= 2f
                            viewModel.prefs.quranFontSize = fontSize
                        }
                    }
                ) {
                    Text("أ-", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                }

                IconButton(
                    onClick = {
                        if (fontSize < 36f) {
                            fontSize += 2f
                            viewModel.prefs.quranFontSize = fontSize
                        }
                    }
                ) {
                    Text("أ+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                }
            }
        }

        if (bookMode) {
            // QoL Page Jumper slider to skip pages instantly (Mushaf exclusive)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "الانتقال السريع اليدوي:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (amoled) Color.LightGray else Color.DarkGray
                )
                Slider(
                    value = currentPage.toFloat(),
                    onValueChange = { currentPage = it.toInt().coerceIn(0, totalPages - 1) },
                    valueRange = 0f..(totalPages - 1).coerceAtLeast(1).toFloat(),
                    steps = if (totalPages > 2) totalPages - 2 else 0,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = AccentGold,
                        activeTrackColor = PrimaryEmerald
                    )
                )
                Text(
                    text = "ص ${currentPage + 1}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                )
            }

            // Beautiful Paper-like Mushaf Display Book Canvas
            val pageVerses = remember(verses, currentPage) {
                val start = currentPage * versesPerPage
                val end = minOf(start + versesPerPage, verses.size)
                if (start < verses.size) verses.subList(start, end) else emptyList()
            }

            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width - 50 } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width + 50 } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width + 50 } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width - 50 } + fadeOut()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = "BookPageFlip"
            ) { pageNum ->
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (amoled) Color(0xFF0D1412) else Color(0xFFFAF7EE)
                    ),
                    border = BorderStroke(2.dp, AccentGold),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Title header matching real pages
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الجزء المحفوظ الميسر",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGold
                                )
                                Text(
                                    text = "السورة: ${surah.nameAr}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGold,
                                    fontFamily = FontFamily.Serif
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = AccentGold.copy(alpha = 0.4f), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Basmala display on page 1 of surahs (except Surah Tawbah/Fatiha)
                            if (pageNum == 0 && surah.number != 1 && surah.number != 9) {
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
                                        color = AccentGold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Compiled Arabic Continuous Text (Classic Traditional Mushaf Format)
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
                                color = if (amoled) AccentGoldLight else PrimaryEmerald,
                                textAlign = TextAlign.Right,
                                lineHeight = (fontSize * 1.8).sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )

                            // Show translations only if enabled
                            if (translationLang != "NONE") {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = AccentGold.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(10.dp))

                                pageVerses.forEach { verse ->
                                    val isSpeaking = activeSpeakingIndex == verse.ayahNumber
                                    val isBookmarkedFlow = viewModel.isBookmarked(surah.number, verse.ayahNumber)
                                    val isBookmarked by isBookmarkedFlow.collectAsState(initial = false)

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSpeaking) AccentGold.copy(alpha = 0.1f) else Color.Transparent)
                                            .clickable {
                                                if (isSpeaking) {
                                                    tts?.stop()
                                                    activeSpeakingIndex = -1
                                                } else {
                                                    activeSpeakingIndex = verse.ayahNumber
                                                    val speakText = if (translationLang == "SW") verse.textSw else verse.textEn
                                                    val speakLocale = if (translationLang == "SW") Locale("sw") else Locale.US
                                                    try {
                                                        tts?.setLanguage(speakLocale)
                                                    } catch (e: Exception) {}
                                                    tts?.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "QuranRecite")
                                                }
                                            }
                                            .padding(4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "${verse.ayahNumber}.",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = AccentGold
                                            )

                                            val transText = if (translationLang == "SW") verse.textSw else verse.textEn
                                            Text(
                                                text = transText,
                                                fontSize = (fontSize - 10).coerceAtLeast(13f).sp,
                                                color = if (isSpeaking) PrimaryEmerald else (if (amoled) Color.LightGray else Color.DarkGray),
                                                lineHeight = (fontSize - 6).coerceAtLeast(18f).sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.toggleBookmark(
                                                        surahNum = surah.number,
                                                        surahName = surah.nameEn,
                                                        ayahNum = verse.ayahNumber,
                                                        verseText = verse.textAr
                                                    )
                                                    Toast.makeText(context, "تم تعديل حالة الإشارة الحفظ للمباركة", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                                    contentDescription = null,
                                                    tint = AccentGold,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "إضاءة: لحفظ آية مرجعية أو الاستماع للتلاوة آية بآية، جرب التبديل لنمط عرض قائمة الأسفار (القائمة المتسلسلة) من الأعلى.",
                                    fontSize = 11.sp,
                                    color = AccentGold.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                                )
                            }
                        }

                        // Bottom Navigation of physical pages
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            HorizontalDivider(color = AccentGold.copy(alpha = 0.2f))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { if (currentPage > 0) currentPage-- },
                                    enabled = currentPage > 0,
                                    colors = ButtonDefaults.textButtonColors(contentColor = AccentGold)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("الصفحة السابقة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Text(
                                    text = "صفحة ${currentPage + 1} من $totalPages",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (amoled) Color.White else Color.Black
                                )

                                TextButton(
                                    onClick = { if (currentPage < totalPages - 1) currentPage++ },
                                    enabled = currentPage < totalPages - 1,
                                    colors = ButtonDefaults.textButtonColors(contentColor = AccentGold)
                                ) {
                                    Text("الصفحة التالية", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Elegant continuous listing of all verses (List Mode)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Basmala header inside continuous scrolling list
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
                                fontSize = (fontSize).coerceAtLeast(20f).sp,
                                fontFamily = FontFamily.Serif,
                                color = AccentGold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                items(verses) { verse ->
                    val isSpeaking = activeSpeakingIndex == verse.ayahNumber
                    val isBookmarkedFlow = viewModel.isBookmarked(surah.number, verse.ayahNumber)
                    val isBookmarked by isBookmarkedFlow.collectAsState(initial = false)

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        isDark = amoled
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSpeaking) AccentGold.copy(alpha = 0.08f) else Color.Transparent)
                                .padding(16.dp)
                        ) {
                            // Header controls of verse
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryEmerald.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = verse.ayahNumber.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentGold
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Audio play button
                                    IconButton(
                                        onClick = {
                                            if (isSpeaking) {
                                                tts?.stop()
                                                activeSpeakingIndex = -1
                                            } else {
                                                activeSpeakingIndex = verse.ayahNumber
                                                val speakText: String
                                                if (translationLang == "NONE") {
                                                    speakText = verse.textAr
                                                    try {
                                                        tts?.setLanguage(Locale("ar"))
                                                    } catch (e: Exception) {}
                                                } else {
                                                    speakText = if (translationLang == "SW") verse.textSw else verse.textEn
                                                    val speakLocale = if (translationLang == "SW") Locale("sw") else Locale.US
                                                    try {
                                                        tts?.setLanguage(speakLocale)
                                                    } catch (e: Exception) {}
                                                }
                                                tts?.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "QuranRecite")
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Outlined.VolumeUp,
                                            contentDescription = "TTS Recite",
                                            tint = if (isSpeaking) PrimaryEmerald else AccentGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Bookmark button
                                    IconButton(
                                        onClick = {
                                            viewModel.toggleBookmark(
                                                surahNum = surah.number,
                                                surahName = surah.nameEn,
                                                ayahNum = verse.ayahNumber,
                                                verseText = verse.textAr
                                            )
                                            Toast.makeText(context, "تم تعديل حالة الإشارة المرجعية", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = "Bookmark",
                                            tint = AccentGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Arabic scripture paragraph
                            Text(
                                text = verse.textAr,
                                fontSize = fontSize.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = if (amoled) AccentGoldLight else PrimaryEmerald,
                                lineHeight = (fontSize * 1.6).sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Optional translation text
                            if (translationLang != "NONE") {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = AccentGold.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(10.dp))

                                val transText = if (translationLang == "SW") verse.textSw else verse.textEn
                                Text(
                                    text = transText,
                                    fontSize = (fontSize - 9).coerceAtLeast(13f).sp,
                                    color = if (amoled) Color.LightGray else Color.DarkGray,
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
