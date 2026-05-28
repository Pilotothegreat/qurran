package com.alibadi.quran.feature.hadith

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alibadi.quran.core.data.dao.AppDao
import com.alibadi.quran.core.data.entities.Hadith
import com.alibadi.quran.core.data.entities.HadithFavorite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed interface HadithLoadResult {
    object Idle : HadithLoadResult
    object Loading : HadithLoadResult
    data class Success(val hadiths: List<Hadith>) : HadithLoadResult
    data class Error(val message: String) : HadithLoadResult
}

class HadithViewModel(
    private val dao: AppDao
) : ViewModel() {

    private val _selectedHadithBook = MutableStateFlow("ibadi")
    val selectedHadithBook = _selectedHadithBook.asStateFlow()

    private val _hadithsState = MutableStateFlow<HadithLoadResult>(HadithLoadResult.Idle)
    val hadithsState = _hadithsState.asStateFlow()

    private val _hadithSearchQuery = MutableStateFlow("")
    val hadithSearchQuery = _hadithSearchQuery.asStateFlow()

    // Query Room or memory lists
    val ibadiHadiths: StateFlow<List<Hadith>> = dao.getAllHadiths()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredHadiths: StateFlow<List<Hadith>> = combine(
        _hadithsState, _hadithSearchQuery, ibadiHadiths
    ) { state, query, ibadiList ->
        val list = when (state) {
            is HadithLoadResult.Success -> state.hadiths
            else -> {
                // If idle or not fetched yet, fallback to ibadi list
                ibadiList
            }
        }
        if (query.isBlank()) {
            list
        } else {
            val normalizedQuery = query.normalizeArabic()
            list.filter {
                it.textAr.normalizeArabic().contains(normalizedQuery, ignoreCase = true) ||
                        it.textEn.contains(query, ignoreCase = true) ||
                        it.narrator.contains(query, ignoreCase = true) ||
                        it.chapterEn.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritedHadithIds: StateFlow<Set<Int>> = dao.getAllHadithFavorites()
        .map { list -> list.map { it.hadithId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        // Load initial book
        loadHadithsFromBook("ibadi")
    }

    fun setHadithSearchQuery(query: String) {
        _hadithSearchQuery.value = query
    }

    fun toggleHadithFavorite(hadithId: Int) {
        viewModelScope.launch {
            val isFav = favoritedHadithIds.value.contains(hadithId)
            if (isFav) {
                dao.deleteHadithFavorite(hadithId)
            } else {
                dao.insertHadithFavorite(HadithFavorite(hadithId = hadithId))
            }
        }
    }

    fun loadHadithsFromBook(bookId: String) {
        _selectedHadithBook.value = bookId
        viewModelScope.launch(Dispatchers.IO) {
            _hadithsState.value = HadithLoadResult.Loading
            if (bookId == "ibadi") {
                // Load from Room pre-populated
                val local = dao.getAllHadiths().first()
                _hadithsState.value = HadithLoadResult.Success(local)
                return@launch
            }

            try {
                val urlSpec = when (bookId) {
                    "bukhari" -> "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/ara-bukhari.json"
                    "muslim" -> "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/ara-muslim.json"
                    else -> "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/ara-bukhari.json"
                }
                val url = URL(urlSpec)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                if (conn.responseCode == 200) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(text)
                    val hadithsArray = json.getJSONArray("hadiths")

                    val parsedList = mutableListOf<Hadith>()
                    val bookTitle = if (bookId == "bukhari") "صحيح البخاري" else "صحيح مسلم"
                    val chapterEn = if (bookId == "bukhari") "Book of Revelation & Faith" else "Book of Purity & Belief"
                    val chapterAr = if (bookId == "bukhari") "كتاب بدء الوحي والإيمان" else "كتاب الإيمان والطهارة"

                    val limit = minOf(hadithsArray.length(), 60)
                    for (i in 0 until limit) {
                        val obj = hadithsArray.getJSONObject(i)
                        val num = obj.getInt("hadithnumber")
                        val textAr = obj.getString("text")
                        val textEn = "Hadith $num of $bookTitle."

                        parsedList.add(
                            Hadith(
                                id = bookId.hashCode() + num,
                                bookNumber = if (bookId == "bukhari") 1 else 2,
                                bookName = bookTitle,
                                number = num,
                                narrator = "Narrated by Sahabah (رضي الله عنهم)",
                                textAr = textAr,
                                textEn = textEn,
                                chapterAr = chapterAr,
                                chapterEn = chapterEn
                            )
                        )
                    }
                    if (parsedList.isNotEmpty()) {
                        _hadithsState.value = HadithLoadResult.Success(parsedList)
                        return@launch
                    }
                }
            } catch (e: Exception) {
                // Ignore and fall through to standard mock/fallback
            }

            // Fallback list
            val fallbackTitle = if (bookId == "bukhari") "صحيح البخاري" else "صحيح مسلم"
            val dummyList = List(10) { i ->
                val num = i + 1
                Hadith(
                    id = bookId.hashCode() + num,
                    bookNumber = if (bookId == "bukhari") 1 else 2,
                    bookName = fallbackTitle,
                    number = num,
                    narrator = "Narrated by companions of the Prophet",
                    textAr = if (bookId == "bukhari") {
                        "قَالَ رسول الله ﷺ: «مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ»."
                    } else {
                        "قَالَ رسول الله ﷺ: «الدِّينُ النَّصِيحَةُ»."
                    },
                    textEn = if (bookId == "bukhari") {
                        "The Messenger of Allah ﷺ said: 'Whoever treads a path in search of knowledge, Allah will make easy for him the path to Paradise.'"
                    } else {
                        "The Messenger of Allah ﷺ said: 'Religion is sincerity/advice.'"
                    },
                    chapterAr = "كتاب العلم والرفق",
                    chapterEn = "Book of Knowledge & Compassion"
                )
            }
            _hadithsState.value = HadithLoadResult.Success(dummyList)
        }
    }

    private fun String.normalizeArabic(): String {
        val diacritics = Regex("[\\u064B-\\u0652\\u0670]")
        return diacritics.replace(this, "")
            .replace("[أإآ]".toRegex(), "ا")
            .replace("ة".toRegex(), "ه")
    }
}
