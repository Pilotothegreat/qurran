package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
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
import com.example.feature.hadith.Hadith
import com.example.ui.HadithLoadResult
import com.example.ui.MainViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHighlightCard

private val AccentGold: Color @Composable get() = MaterialTheme.colorScheme.tertiary
private val PrimaryEmerald: Color @Composable get() = MaterialTheme.colorScheme.primary


@Composable
fun HadithScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val searchInput by viewModel.hadithSearchQuery.collectAsState()
    val hadiths by viewModel.filteredHadiths.collectAsState()
    val favoritedIds by viewModel.favoritedHadithIds.collectAsState()
    val selectedBook by viewModel.selectedHadithBook.collectAsState()
    val hadithsState by viewModel.hadithsState.collectAsState()
    val amoled = viewModel.prefs.amoledMode
    val translationLang = viewModel.prefs.quranLanguage

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App header intro
        Text(
            text = "مَوْسُوعَةُ الأَحَادِيثِ الشَّرِيفَةِ 📚",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = AccentGold,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = "الموسوعة المفتوحة والحديثة للتحقق من أسانيد السنة النبوية المطهرة",
            fontSize = 11.sp,
            color = if (amoled) Color(0xFF90A49F) else Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Multiple collection library books filter tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Triple("ibadi", "موطأ الإمام الربيع", "الجامع الصحيح"),
                Triple("bukhari", "صحيح البخاري", "الشريف الكامل"),
                Triple("muslim", "صحيح مسلم", "الشريف الكامل")
            ).forEach { (id, titleAr, subAr) ->
                val active = selectedBook == id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (active) PrimaryEmerald else (if (amoled) Color(0xFF111D1B) else Color(0xFFE5ECEB)))
                        .border(1.dp, if (active) AccentGold else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { viewModel.loadHadithsFromBook(id) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = titleAr,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) AccentGold else (if (amoled) Color.White else Color.Black),
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = subAr,
                            fontSize = 9.sp,
                            color = if (active) AccentGold.copy(0.8f) else Color.Gray,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }
        }

        // Text input Search Bar
        OutlinedTextField(
            value = searchInput,
            onValueChange = { viewModel.setHadithSearchQuery(it) },
            placeholder = { Text("ابحث بالراوي، الباب، أو الكلمة...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGold) },
            trailingIcon = {
                if (searchInput.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setHadithSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGold,
                unfocusedBorderColor = if (amoled) Color(0xFF223A33) else Color.LightGray
            )
        )

        // Loading and result lists
        when (hadithsState) {
            is HadithLoadResult.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(color = AccentGold)
                        Text("جاري جلب أسانيد السنة وتدقيق الصحاح...", fontSize = 12.sp, color = AccentGold, fontFamily = FontFamily.Serif)
                    }
                }
            }
            is HadithLoadResult.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("عفواً، فشل الاتصال بخوادم الحديث الأكاديمية.", fontSize = 13.sp, color = Color.Red, fontFamily = FontFamily.Serif)
                        Button(onClick = { viewModel.loadHadithsFromBook(selectedBook) }, colors = ButtonDefaults.buttonColors(containerColor = AccentGold)) {
                            Text("إعادة المحاولة", color = Color.White)
                        }
                    }
                }
            }
            is HadithLoadResult.Success -> {
                if (hadiths.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "لم يتم العثور على أي حديث يطابق كلمات البحث.",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(hadiths) { hadith ->
                            val isFavorite = favoritedIds.contains(hadith.id)
                            HadithCardItem(
                                hadith = hadith,
                                isFav = isFavorite,
                                onFavToggle = { viewModel.toggleHadithFavorite(hadith.id) },
                                onShare = {
                                    try {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "حديث من ${hadith.bookName}")
                                            putExtra(Intent.EXTRA_TEXT, "${hadith.narrator}\n\n${hadith.textAr}\n\n${hadith.textEn}")
                                        }
                                        val chooser = Intent.createChooser(shareIntent, "مشاركة الحديث الشريف").apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(chooser)
                                    } catch (e: Exception) {
                                        // Gracefully handle missing share activity handlers in restricted vm environments
                                    }
                                },
                                amoled = amoled,
                                translationLang = translationLang
                            )
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun HadithCardItem(
    hadith: Hadith,
    isFav: Boolean,
    onFavToggle: () -> Unit,
    onShare: () -> Unit,
    amoled: Boolean,
    translationLang: String
) {
    GlassHighlightCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = amoled
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Upper Header (Book, Chapter and Number)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (translationLang == "NONE") {
                        Text(
                            text = hadith.chapterAr,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "الحديث رقم ${hadith.number} • ${hadith.bookName}",
                            fontSize = 11.sp,
                            color = if (amoled) Color(0xFF90A49F) else Color.Gray,
                            fontFamily = FontFamily.Serif
                        )
                    } else {
                        Text(
                            text = hadith.chapterEn.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Hadith #${hadith.number} • Book ${hadith.bookNumber}",
                            fontSize = 11.sp,
                            color = if (amoled) Color(0xFF90A49F) else Color.Gray
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Favorite Toggle Button
                    IconButton(onClick = onFavToggle, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) Color.Red else AccentGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Share Button
                    IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = AccentGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = if (amoled) Color(0xFF1B2825) else Color(0xFFE5ECEB))
            Spacer(modifier = Modifier.height(12.dp))

            // Narrator Line
            Text(
                text = hadith.narrator,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (amoled) Color(0xFF86A39B) else PrimaryEmerald,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Arabic text
            Text(
                text = hadith.textAr,
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = AccentGold,
                textAlign = TextAlign.Right,
                lineHeight = 28.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // English meaning
            if (translationLang != "NONE") {
                Text(
                    text = hadith.textEn,
                    fontSize = 13.sp,
                    color = if (amoled) Color.LightGray else Color.DarkGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
