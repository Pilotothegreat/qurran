package com.alibadi.quran

import android.app.Application
import com.alibadi.quran.core.data.AppDatabase
import com.alibadi.quran.core.data.entities.Surah
import com.alibadi.quran.core.data.entities.Hadith
import com.alibadi.quran.core.data.entities.AdhkarItem
import com.alibadi.quran.di.appModule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AlIbadiApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@AlIbadiApplication)
            modules(appModule)
        }

        // Populate database if empty
        val db = AppDatabase.getDatabase(this)
        val dao = db.appDao()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val surahsList = dao.getAllSurahs().first()
                if (surahsList.isEmpty()) {
                    val gson = Gson()
                    
                    // 1. Populate Surahs
                    val quranJson = assets.open("quran.json").bufferedReader().use { it.readText() }
                    val surahListType = object : TypeToken<List<Surah>>() {}.type
                    val surahs: List<Surah> = gson.fromJson(quranJson, surahListType)
                    dao.insertSurahs(surahs)

                    // 2. Populate Hadiths
                    val hadithJson = assets.open("hadith.json").bufferedReader().use { it.readText() }
                    val hadithListType = object : TypeToken<List<Hadith>>() {}.type
                    val hadiths: List<Hadith> = gson.fromJson(hadithJson, hadithListType)
                    dao.insertHadiths(hadiths)

                    // 3. Populate Adhkar
                    val adhkarJson = assets.open("adhkar.json").bufferedReader().use { it.readText() }
                    val adhkarListType = object : TypeToken<List<AdhkarItem>>() {}.type
                    val adhkarItems: List<AdhkarItem> = gson.fromJson(adhkarJson, adhkarListType)
                    dao.insertAdhkarItems(adhkarItems)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
