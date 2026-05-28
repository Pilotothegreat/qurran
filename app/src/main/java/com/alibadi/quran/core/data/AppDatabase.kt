package com.alibadi.quran.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alibadi.quran.core.data.dao.AppDao
import com.alibadi.quran.core.data.entities.*

@Database(
    entities = [
        QuranHistory::class,
        QuranBookmark::class,
        HadithFavorite::class,
        PrayerLog::class,
        TasbihCounter::class,
        CachedVerse::class,
        Surah::class,
        Hadith::class,
        AdhkarItem::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "al_ibadi_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
