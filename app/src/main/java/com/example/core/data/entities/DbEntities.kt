package com.example.core.data.entities

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
    val highlightColorHex: String? = null, // Optional hex color for visual highlight tag
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

