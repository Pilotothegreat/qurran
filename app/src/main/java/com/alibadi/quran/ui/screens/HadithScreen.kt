package com.alibadi.quran.ui.screens

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.alibadi.quran.core.data.entities.Hadith
import com.alibadi.quran.feature.hadith.HadithLoadResult
import com.alibadi.quran.feature.hadith.HadithViewModel
import com.alibadi.quran.feature.settings.SettingsViewModel
import com.alibadi.quran.ui.theme.accentGold
import com.alibadi.quran.ui.theme.primaryEmerald
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithScreen(
    viewModel: HadithViewModel,
    modifier: Modifier = Modifier
) {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    
    val searchInput by viewModel.hadithSearchQuery.collectAsState()
    val hadiths by viewModel.filteredHadiths.collectAsState()
    val favoritedIds by viewModel.favoritedHadithIds.collectAsState()
    val selectedBook by viewModel.selectedHadithBook.collectAsState()
    val hadithsState by viewModel.hadithsState.collectAsState()
    
    val amoled by settingsViewModel.amoled.collectAsState()
    val translationLang by settingsViewModel.quranLanguage.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "موسوعة الأحاديث الشريفة",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = accentGold,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = "الموسوعة المفتوحة والحديثة للتحقق من أسانيد السنة النبوية المطهرة",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Filter tabs
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
                        .clip(MaterialTheme.shapes.small)
                        .background(if (active) primaryEmerald else MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, if (active) accentGold else Color.Transparent, MaterialTheme.shapes.small)
                        .clickable { viewModel.loadHadithsFromBook(id) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = titleAr,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (active) accentGold else MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = subAr,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (active) accentGold.copy(0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchInput,
            onValueChange = { viewModel.setHadithSearchQuery(it) },
            placeholder = { Text("ابحث بالراوي، الباب، أو الكلمة...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = accentGold) },
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
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentGold,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Loading and Results
        when (hadithsState) {
            is HadithLoadResult.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(color = accentGold)
                        Text("جاري جلب أسانيد السنة وتدقيق الصحاح...", style = MaterialTheme.typography.bodyMedium, color = accentGold, fontFamily = FontFamily.Serif)
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
                        Text("عفواً، فشل الاتصال بخوادم الحديث الأكاديمية.", style = MaterialTheme.typography.bodyMedium, color = Color.Red, fontFamily = FontFamily.Serif)
                        Button(onClick = { viewModel.loadHadithsFromBook(selectedBook) }, colors = ButtonDefaults.buttonColors(containerColor = accentGold)) {
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
                                tint = accentGold,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "لم يتم العثور على أي حديث يطابق كلمات البحث.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    } catch (e: Exception) {}
                                },
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
    translationLang: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (translationLang == "NONE") {
                        Text(
                            text = hadith.chapterAr,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentGold,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "الحديث رقم ${hadith.number} • ${hadith.bookName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Serif
                        )
                    } else {
                        Text(
                            text = hadith.chapterEn.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentGold
                        )
                        Text(
                            text = "Hadith #${hadith.number} • Book ${hadith.bookNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onFavToggle, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) Color.Red else accentGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = accentGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = hadith.narrator,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = primaryEmerald,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = hadith.textAr,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = accentGold,
                textAlign = TextAlign.Right,
                lineHeight = 28.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            if (translationLang != "NONE") {
                Text(
                    text = hadith.textEn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
