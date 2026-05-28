package com.alibadi.quran.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alibadi.quran.core.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Last read position
    @Query("SELECT * FROM quran_history WHERE id = :id LIMIT 1")
    fun getQuranHistory(id: Int = 1): Flow<QuranHistory?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuranHistory(history: QuranHistory)

    // Quran Bookmarks
    @Query("SELECT * FROM quran_bookmarks ORDER BY timestamp DESC")
    fun getAllQuranBookmarks(): Flow<List<QuranBookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuranBookmark(bookmark: QuranBookmark)

    @Query("DELETE FROM quran_bookmarks WHERE id = :id")
    suspend fun deleteQuranBookmarkById(id: Int)

    @Query("DELETE FROM quran_bookmarks WHERE surahNumber = :surahNum AND ayahNumber = :ayahNum")
    suspend fun deleteQuranBookmarkEx(surahNum: Int, ayahNum: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM quran_bookmarks WHERE surahNumber = :surahNum AND ayahNumber = :ayahNum)")
    fun isQuranBookmarked(surahNum: Int, ayahNum: Int): Flow<Boolean>

    // Hadith Favorites
    @Query("SELECT * FROM hadith_favorites ORDER BY timestamp DESC")
    fun getAllHadithFavorites(): Flow<List<HadithFavorite>>

    @Query("SELECT EXISTS(SELECT 1 FROM hadith_favorites WHERE hadithId = :hadithId)")
    fun isHadithFavorited(hadithId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHadithFavorite(favorite: HadithFavorite)

    @Query("DELETE FROM hadith_favorites WHERE hadithId = :hadithId")
    suspend fun deleteHadithFavorite(hadithId: Int)

    // Prayer logs (daily track)
    @Query("SELECT * FROM prayer_logs WHERE dateKey = :dateKey LIMIT 1")
    fun getPrayerLogForDate(dateKey: String): Flow<PrayerLog?>

    @Query("SELECT * FROM prayer_logs ORDER BY dateKey DESC LIMIT 7")
    fun getRecentPrayerLogs(): Flow<List<PrayerLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePrayerLog(log: PrayerLog)

    // Tasbih Counters
    @Query("SELECT * FROM tasbih_counters")
    fun getAllTasbihCounters(): Flow<List<TasbihCounter>>

    @Query("SELECT * FROM tasbih_counters WHERE dhikrKey = :dhikrKey LIMIT 1")
    suspend fun getTasbihCounter(dhikrKey: String): TasbihCounter?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTasbihCounter(counter: TasbihCounter)

    @Query("UPDATE tasbih_counters SET count = 0 WHERE dhikrKey LIKE 'adhkar_%'")
    suspend fun resetAllAdhkarCounts()

    // Quran Video/Cache Sync Engine
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedVerses(verses: List<CachedVerse>)

    @Query("SELECT * FROM cached_verses WHERE surahNumber = :surahNum ORDER BY ayahNumber ASC")
    fun getCachedVersesForSurah(surahNum: Int): Flow<List<CachedVerse>>

    @Query("SELECT * FROM cached_verses WHERE surahNumber = :surahNum ORDER BY ayahNumber ASC")
    suspend fun getCachedVersesForSurahDirect(surahNum: Int): List<CachedVerse>

    @Query("DELETE FROM cached_verses WHERE surahNumber = :surahNum")
    suspend fun deleteCachedVersesForSurah(surahNum: Int)

    @Query("SELECT * FROM cached_verses WHERE textAr LIKE '%' || :query || '%' OR textEn LIKE '%' || :query || '%'")
    fun searchCachedQuran(query: String): Flow<List<CachedVerse>>

    // Pre-populated Tables
    @Query("SELECT * FROM surahs ORDER BY number ASC")
    fun getAllSurahs(): Flow<List<Surah>>

    @Query("SELECT * FROM surahs WHERE number = :number LIMIT 1")
    fun getSurahByNumber(number: Int): Flow<Surah?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<Surah>)

    @Query("SELECT * FROM hadiths ORDER BY id ASC")
    fun getAllHadiths(): Flow<List<Hadith>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHadiths(hadiths: List<Hadith>)

    @Query("SELECT * FROM adhkar_items WHERE category = :category ORDER BY id ASC")
    fun getAdhkarByCategory(category: String): Flow<List<AdhkarItem>>

    @Query("SELECT * FROM adhkar_items ORDER BY id ASC")
    fun getAllAdhkarItems(): Flow<List<AdhkarItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdhkarItems(items: List<AdhkarItem>)
}
