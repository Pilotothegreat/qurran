package com.alibadi.quran.di

import com.alibadi.quran.core.data.AppDatabase
import com.alibadi.quran.core.data.AppPreferencesDataStore
import com.alibadi.quran.feature.adhkar.AdhkarViewModel
import com.alibadi.quran.feature.calendar.CalendarViewModel
import com.alibadi.quran.feature.hadith.HadithViewModel
import com.alibadi.quran.feature.prayer.PrayerViewModel
import com.alibadi.quran.feature.qibla.QiblaViewModel
import com.alibadi.quran.feature.quran.QuranViewModel
import com.alibadi.quran.feature.settings.SettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppPreferencesDataStore(androidApplication()) }
    single { AppDatabase.getDatabase(androidApplication()) }
    single { get<AppDatabase>().appDao() }

    viewModel { PrayerViewModel(androidApplication(), get(), get()) }
    viewModel { QuranViewModel(get(), get()) }
    viewModel { AdhkarViewModel(get(), get()) }
    viewModel { HadithViewModel(get()) }
    viewModel { SettingsViewModel(androidApplication(), get()) }
    viewModel { QiblaViewModel(androidApplication(), get()) }
    viewModel { CalendarViewModel(get()) }
}
