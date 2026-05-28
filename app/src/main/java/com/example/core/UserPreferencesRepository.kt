package com.example.core.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("al_ibadi_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_LATITUDE = "pref_lat"
        const val KEY_LONGITUDE = "pref_lon"
        const val KEY_LOC_NAME = "pref_loc_name"
        const val KEY_ASR_SCHOOL = "pref_asr_school" // 1 = Standard (Ibadi/Shafi'i), 2 = Hanafi
        const val KEY_FAJR_ANGLE = "pref_fajr_angle"
        const val KEY_ISHA_ANGLE = "pref_isha_angle"
        const val KEY_AMOLED_MODE = "pref_amoled_mode"
        const val KEY_HAPTICS = "pref_haptics"
        const val KEY_QURAN_FONT_SIZE = "pref_quran_font_size"
        const val KEY_QURAN_LANG = "pref_quran_lang" // "EN", "SW", "NONE"
        const val KEY_HIJRI_OFFSET = "pref_hijri_offset"

        // Offsets in minutes
        const val KEY_OFFSET_FAJR = "pref_offset_fajr"
        const val KEY_OFFSET_SUNRISE = "pref_offset_sunrise"
        const val KEY_OFFSET_DHUHR = "pref_offset_dhuhr"
        const val KEY_OFFSET_ASR = "pref_offset_asr"
        const val KEY_OFFSET_MAGHRIB = "pref_offset_maghrib"
        const val KEY_OFFSET_ISHA = "pref_offset_isha"
    }

    var latitude: Double
        get() {
            return try {
                prefs.getString(KEY_LATITUDE, null)?.toDoubleOrNull() ?: PrayerTimeCalculator.DEFAULT_LAT
            } catch (e: ClassCastException) {
                prefs.getFloat(KEY_LATITUDE, PrayerTimeCalculator.DEFAULT_LAT.toFloat()).toDouble()
            }
        }
        set(value) = prefs.edit().putString(KEY_LATITUDE, value.toString()).apply()

    var longitude: Double
        get() {
            return try {
                prefs.getString(KEY_LONGITUDE, null)?.toDoubleOrNull() ?: PrayerTimeCalculator.DEFAULT_LON
            } catch (e: ClassCastException) {
                prefs.getFloat(KEY_LONGITUDE, PrayerTimeCalculator.DEFAULT_LON.toFloat()).toDouble()
            }
        }
        set(value) = prefs.edit().putString(KEY_LONGITUDE, value.toString()).apply()


    var locationName: String
        get() = prefs.getString(KEY_LOC_NAME, "Al Awabi, Al Batinah, Oman") ?: "Al Awabi, Al Batinah, Oman"
        set(value) = prefs.edit().putString(KEY_LOC_NAME, value).apply()

    var asrSchool: Int
        get() = prefs.getInt(KEY_ASR_SCHOOL, 1) // 1 = Standard/Ibadi default
        set(value) = prefs.edit().putInt(KEY_ASR_SCHOOL, value).apply()

    var fajrAngle: Double
        get() = prefs.getFloat(KEY_FAJR_ANGLE, 18.0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_FAJR_ANGLE, value.toFloat()).apply()

    var ishaAngle: Double
        get() = prefs.getFloat(KEY_ISHA_ANGLE, 18.0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_ISHA_ANGLE, value.toFloat()).apply()

    var amoledMode: Boolean
        get() = prefs.getBoolean(KEY_AMOLED_MODE, true) // Pure dark mode by default
        set(value) = prefs.edit().putBoolean(KEY_AMOLED_MODE, value).apply()

    var themeMode: String
        get() = prefs.getString("pref_theme_mode", "SYSTEM") ?: "SYSTEM" // LIGHT, DARK, SYSTEM
        set(value) = prefs.edit().putString("pref_theme_mode", value).apply()

    var dynamicColorEnabled: Boolean
        get() = prefs.getBoolean("pref_dynamic_color", true) // Default true for Material You theme accents
        set(value) = prefs.edit().putBoolean("pref_dynamic_color", value).apply()

    var simpleMode: Boolean
        get() = prefs.getBoolean("pref_simple_mode", false) // Elderly/Simple mode off by default
        set(value) = prefs.edit().putBoolean("pref_simple_mode", value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS, value).apply()

    var quranFontSize: Float
        get() = prefs.getFloat(KEY_QURAN_FONT_SIZE, 24f)
        set(value) = prefs.edit().putFloat(KEY_QURAN_FONT_SIZE, value).apply()

    var quranLanguage: String
        get() = prefs.getString(KEY_QURAN_LANG, "NONE") ?: "NONE"
        set(value) = prefs.edit().putString(KEY_QURAN_LANG, value).apply()

    var hijriOffset: Int
        get() = prefs.getInt(KEY_HIJRI_OFFSET, 0)
        set(value) = prefs.edit().putInt(KEY_HIJRI_OFFSET, value).apply()

    var useOnlinePrayerTimes: Boolean
        get() = prefs.getBoolean("pref_use_online_prayer_times", false)
        set(value) = prefs.edit().putBoolean("pref_use_online_prayer_times", value).apply()

    var quranBookMode: Boolean
        get() = prefs.getBoolean("pref_quran_book_mode", false)
        set(value) = prefs.edit().putBoolean("pref_quran_book_mode", value).apply()

    var notificationSound: Boolean
        get() = prefs.getBoolean("pref_notification_sound", true)
        set(value) = prefs.edit().putBoolean("pref_notification_sound", value).apply()

    var onlineCachedTimes: String?
        get() = prefs.getString("pref_online_cached_times", null)
        set(value) = prefs.edit().putString("pref_online_cached_times", value).apply()

    var lastReadSurah: Int
        get() = prefs.getInt("pref_last_read_surah", -1)
        set(value) = prefs.edit().putInt("pref_last_read_surah", value).apply()

    var lastReadPage: Int
        get() = prefs.getInt("pref_last_read_page", 0)
        set(value) = prefs.edit().putInt("pref_last_read_page", value).apply()

    var iqamaFajr: Int
        get() = prefs.getInt("pref_iqama_fajr", 20)
        set(value) = prefs.edit().putInt("pref_iqama_fajr", value).apply()

    var iqamaDhuhr: Int
        get() = prefs.getInt("pref_iqama_dhuhr", 15)
        set(value) = prefs.edit().putInt("pref_iqama_dhuhr", value).apply()

    var iqamaAsr: Int
        get() = prefs.getInt("pref_iqama_asr", 15)
        set(value) = prefs.edit().putInt("pref_iqama_asr", value).apply()

    var iqamaMaghrib: Int
        get() = prefs.getInt("pref_iqama_maghrib", 10)
        set(value) = prefs.edit().putInt("pref_iqama_maghrib", value).apply()

    var iqamaIsha: Int
        get() = prefs.getInt("pref_iqama_isha", 15)
        set(value) = prefs.edit().putInt("pref_iqama_isha", value).apply()

    // Custom offset setters
    fun getOffset(key: String): Int = prefs.getInt(key, 0)
    fun setOffset(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
}
