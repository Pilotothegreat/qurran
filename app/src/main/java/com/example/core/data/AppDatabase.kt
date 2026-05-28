package com.example.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.core.data.dao.AppDao
import com.example.core.data.entities.*

@Database(
    entities = [
        QuranHistory::class,
        QuranBookmark::class,
        HadithFavorite::class,
        PrayerLog::class,
        TasbihCounter::class,
        CachedVerse::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `cached_verses`")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `cached_verses` (" +
                    "`surahNumber` INTEGER NOT NULL, " +
                    "`ayahNumber` INTEGER NOT NULL, " +
                    "`textAr` TEXT NOT NULL, " +
                    "`textEn` TEXT NOT NULL, " +
                    "`textSw` TEXT NOT NULL, " +
                    "PRIMARY KEY(`surahNumber`, `ayahNumber`))"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "al_ibadi_database"
                )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

