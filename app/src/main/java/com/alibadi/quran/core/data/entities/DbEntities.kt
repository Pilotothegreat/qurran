package com.alibadi.quran.core.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quran_history")
data class QuranHistory(
    @PrimaryKey val id: Int = 1, // Single entry to remember last read position
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quran_bookmarks")
data class QuranBookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val verseText: String,
    val note: String? = null,
    val highlightColorHex: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "hadith_favorites")
data class HadithFavorite(
    @PrimaryKey val hadithId: Int, // Number matches Jami' al-Sahih Hadith index
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "prayer_logs")
data class PrayerLog(
    @PrimaryKey val dateKey: String, // e.g., "2026-05-27"
    val fajrCompleted: Boolean = false,
    val dhuhrCompleted: Boolean = false,
    val asrCompleted: Boolean = false,
    val maghribCompleted: Boolean = false,
    val ishaCompleted: Boolean = false
)

@Entity(tableName = "tasbih_counters")
data class TasbihCounter(
    @PrimaryKey val dhikrKey: String, // name of dhikr, e.g., "SubhanAllah", "Alhamdulillah", etc.
    val count: Int,
    val target: Int = 33,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_verses", primaryKeys = ["surahNumber", "ayahNumber"])
data class CachedVerse(
    val surahNumber: Int,
    val ayahNumber: Int,
    val textAr: String,
    val textEn: String,
    val textSw: String
)

@Entity(tableName = "surahs")
data class Surah(
    @PrimaryKey val number: Int,
    val nameEn: String,
    val nameAr: String,
    val versesCount: Int,
    val type: String, // "Meccan" or "Medinan"
    val englishMeaning: String
)

@Entity(tableName = "hadiths")
data class Hadith(
    @PrimaryKey val id: Int,
    val bookNumber: Int,
    val bookName: String,
    val number: Int,
    val narrator: String,
    val textAr: String,
    val textEn: String,
    val chapterAr: String,
    val chapterEn: String
)

@Entity(tableName = "adhkar_items")
data class AdhkarItem(
    @PrimaryKey val id: Int,
    val category: String, // "Morning", "Evening", "Post-Prayer"
    val textAr: String,
    val textEn: String,
    val countTarget: Int,
    val virtueAr: String,
    val virtueEn: String
)
