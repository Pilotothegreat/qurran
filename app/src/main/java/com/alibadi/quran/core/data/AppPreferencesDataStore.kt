package com.alibadi.quran.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class AppPreferencesDataStore(private val context: Context) {

    companion object {
        val KEY_LATITUDE = doublePreferencesKey("latitude")
        val KEY_LONGITUDE = doublePreferencesKey("longitude")
        val KEY_LOCATION_NAME = stringPreferencesKey("location_name")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")       // "LIGHT", "DARK", "SYSTEM"
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val KEY_AMOLED = booleanPreferencesKey("amoled")
        val KEY_SIMPLE_MODE = booleanPreferencesKey("simple_mode")
        val KEY_HAPTICS = booleanPreferencesKey("haptics")
        val KEY_ASR_SCHOOL = intPreferencesKey("asr_school")
        val KEY_HIJRI_OFFSET = intPreferencesKey("hijri_offset")
        val KEY_QURAN_LANGUAGE = stringPreferencesKey("quran_language")
        val KEY_LAST_READ_SURAH = intPreferencesKey("last_read_surah")
        val KEY_LAST_READ_PAGE = intPreferencesKey("last_read_page")
        val KEY_NOTIFICATION_SOUND = booleanPreferencesKey("notification_sound")
        val KEY_USE_ONLINE_PRAYER = booleanPreferencesKey("use_online_prayer")
        val KEY_FONT_SCALE = stringPreferencesKey("font_scale")       // "NORMAL", "LARGE", "XLARGE"
        val KEY_SHOW_PALESTINE_UPDATES = booleanPreferencesKey("show_palestine_updates")
        
        // Prayer time manual offsets
        val KEY_OFFSET_FAJR = intPreferencesKey("offset_fajr")
        val KEY_OFFSET_SUNRISE = intPreferencesKey("offset_sunrise")
        val KEY_OFFSET_DHUHR = intPreferencesKey("offset_dhuhr")
        val KEY_OFFSET_ASR = intPreferencesKey("offset_asr")
        val KEY_OFFSET_MAGHRIB = intPreferencesKey("offset_maghrib")
        val KEY_OFFSET_ISHA = intPreferencesKey("offset_isha")

        // Defaults
        const val DEFAULT_LATITUDE = 23.5880  // Muscat
        const val DEFAULT_LONGITUDE = 58.3829
        const val DEFAULT_LOCATION_NAME = "مسقط، عُمان"
    }

    val latitude: Flow<Double> = context.dataStore.data.map { it[KEY_LATITUDE] ?: DEFAULT_LATITUDE }
    val longitude: Flow<Double> = context.dataStore.data.map { it[KEY_LONGITUDE] ?: DEFAULT_LONGITUDE }
    val locationName: Flow<String> = context.dataStore.data.map { it[KEY_LOCATION_NAME] ?: DEFAULT_LOCATION_NAME }
    val themeMode: Flow<String> = context.dataStore.data.map { it[KEY_THEME_MODE] ?: "SYSTEM" }
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { it[KEY_DYNAMIC_COLOR] ?: true }
    val amoled: Flow<Boolean> = context.dataStore.data.map { it[KEY_AMOLED] ?: false }
    val simpleMode: Flow<Boolean> = context.dataStore.data.map { it[KEY_SIMPLE_MODE] ?: false }
    val haptics: Flow<Boolean> = context.dataStore.data.map { it[KEY_HAPTICS] ?: true }
    val asrSchool: Flow<Int> = context.dataStore.data.map { it[KEY_ASR_SCHOOL] ?: 1 }
    val hijriOffset: Flow<Int> = context.dataStore.data.map { it[KEY_HIJRI_OFFSET] ?: 0 }
    val quranLanguage: Flow<String> = context.dataStore.data.map { it[KEY_QURAN_LANGUAGE] ?: "EN" }
    val lastReadSurah: Flow<Int> = context.dataStore.data.map { it[KEY_LAST_READ_SURAH] ?: 1 }
    val lastReadPage: Flow<Int> = context.dataStore.data.map { it[KEY_LAST_READ_PAGE] ?: 0 }
    val notificationSound: Flow<Boolean> = context.dataStore.data.map { it[KEY_NOTIFICATION_SOUND] ?: true }
    val useOnlinePrayer: Flow<Boolean> = context.dataStore.data.map { it[KEY_USE_ONLINE_PRAYER] ?: false }
    val fontScale: Flow<String> = context.dataStore.data.map { it[KEY_FONT_SCALE] ?: "NORMAL" }
    val showPalestineUpdates: Flow<Boolean> = context.dataStore.data.map { it[KEY_SHOW_PALESTINE_UPDATES] ?: false }

    val offsetFajr: Flow<Int> = context.dataStore.data.map { it[KEY_OFFSET_FAJR] ?: 0 }
    val offsetSunrise: Flow<Int> = context.dataStore.data.map { it[KEY_OFFSET_SUNRISE] ?: 0 }
    val offsetDhuhr: Flow<Int> = context.dataStore.data.map { it[KEY_OFFSET_DHUHR] ?: 0 }
    val offsetAsr: Flow<Int> = context.dataStore.data.map { it[KEY_OFFSET_ASR] ?: 0 }
    val offsetMaghrib: Flow<Int> = context.dataStore.data.map { it[KEY_OFFSET_MAGHRIB] ?: 0 }
    val offsetIsha: Flow<Int> = context.dataStore.data.map { it[KEY_OFFSET_ISHA] ?: 0 }

    suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }
}
