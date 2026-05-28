package com.alibadi.quran.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alibadi.quran.BuildConfig
import com.alibadi.quran.core.data.AppPreferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed interface UpdateState {
    object Idle : UpdateState
    object Checking : UpdateState
    data class UpdateAvailable(
        val latestVersion: String,
        val changelog: String,
        val htmlUrl: String,
        val downloadUrl: String
    ) : UpdateState
    object NoUpdate : UpdateState
    data class Error(val message: String) : UpdateState
}

class SettingsViewModel(
    private val application: Application,
    private val prefs: AppPreferencesDataStore
) : AndroidViewModel(application) {

    // Preference Flows
    val themeMode: StateFlow<String> = prefs.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val dynamicColor: StateFlow<Boolean> = prefs.dynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val amoled: StateFlow<Boolean> = prefs.amoled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val simpleMode: StateFlow<Boolean> = prefs.simpleMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val haptics: StateFlow<Boolean> = prefs.haptics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val asrSchool: StateFlow<Int> = prefs.asrSchool
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val hijriOffset: StateFlow<Int> = prefs.hijriOffset
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val quranLanguage: StateFlow<String> = prefs.quranLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "EN")

    val notificationSound: StateFlow<Boolean> = prefs.notificationSound
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val useOnlinePrayer: StateFlow<Boolean> = prefs.useOnlinePrayer
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val fontScale: StateFlow<String> = prefs.fontScale
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "NORMAL")

    val showPalestineUpdates: StateFlow<Boolean> = prefs.showPalestineUpdates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val latitude = prefs.latitude
    val longitude = prefs.longitude
    val locationName = prefs.locationName

    val offsetFajr = prefs.offsetFajr
    val offsetSunrise = prefs.offsetSunrise
    val offsetDhuhr = prefs.offsetDhuhr
    val offsetAsr = prefs.offsetAsr
    val offsetMaghrib = prefs.offsetMaghrib
    val offsetIsha = prefs.offsetIsha

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    init {
        checkForUpdates()
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_THEME_MODE, mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_DYNAMIC_COLOR, enabled)
        }
    }

    fun setAmoled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_AMOLED, enabled)
        }
    }

    fun setSimpleMode(enabled: Boolean) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_SIMPLE_MODE, enabled)
        }
    }

    fun setHaptics(enabled: Boolean) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_HAPTICS, enabled)
        }
    }

    fun setAsrSchool(school: Int) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_ASR_SCHOOL, school)
        }
    }

    fun setHijriOffset(offset: Int) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_HIJRI_OFFSET, offset)
        }
    }

    fun setQuranLanguage(lang: String) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_QURAN_LANGUAGE, lang)
        }
    }

    fun setNotificationSound(enabled: Boolean) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_NOTIFICATION_SOUND, enabled)
        }
    }

    fun setUseOnlinePrayer(enabled: Boolean) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_USE_ONLINE_PRAYER, enabled)
        }
    }

    fun setFontScale(scale: String) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_FONT_SCALE, scale)
        }
    }

    fun setShowPalestineUpdates(enabled: Boolean) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_SHOW_PALESTINE_UPDATES, enabled)
        }
    }

    fun setOffsetFajr(offset: Int) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_OFFSET_FAJR, offset)
        }
    }

    fun setOffsetSunrise(offset: Int) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_OFFSET_SUNRISE, offset)
        }
    }

    fun setOffsetDhuhr(offset: Int) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_OFFSET_DHUHR, offset)
        }
    }

    fun setOffsetAsr(offset: Int) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_OFFSET_ASR, offset)
        }
    }

    fun setOffsetMaghrib(offset: Int) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_OFFSET_MAGHRIB, offset)
        }
    }

    fun setOffsetIsha(offset: Int) {
        viewModelScope.launch {
            prefs.set(AppPreferencesDataStore.KEY_OFFSET_ISHA, offset)
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            _updateState.value = UpdateState.Checking
            try {
                val url = URL("https://api.github.com/repos/Pilotothegreat/qurran/releases/latest")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android)")
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json")

                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(text)
                    val tagName = json.getString("tag_name")
                    val body = json.optString("body", "مزايا وتحسينات مخصصة واجهة المستخدم وتجربة الماتيريال - متوافقة تماماً 🌴")
                    val htmlUrl = json.getString("html_url")

                    var apkUrl: String? = null
                    if (json.has("assets")) {
                        val assets = json.getJSONArray("assets")
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.getString("name")
                            if (name.endsWith(".apk")) {
                                apkUrl = asset.getString("browser_download_url")
                                break
                            }
                        }
                    }

                    val currentVersion = BuildConfig.VERSION_NAME
                    if (isNewerVersion(currentVersion, tagName)) {
                        _updateState.value = UpdateState.UpdateAvailable(
                            latestVersion = tagName,
                            changelog = body,
                            htmlUrl = htmlUrl,
                            downloadUrl = apkUrl ?: htmlUrl
                        )
                    } else {
                        _updateState.value = UpdateState.NoUpdate
                    }
                } else {
                    _updateState.value = UpdateState.NoUpdate
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("تعذر الاتصال بخادم التحديثات: ${e.localizedMessage}")
            }
        }
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        val cleanCurrent = current.removePrefix("v").trim()
        val cleanLatest = latest.removePrefix("v").trim()
        val currentParts = cleanCurrent.split(".").map { it.toIntOrNull() ?: 0 }
        val latestParts = cleanLatest.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLen) {
            val currVal = currentParts.getOrNull(i) ?: 0
            val latVal = latestParts.getOrNull(i) ?: 0
            if (latVal > currVal) return true
            if (latVal < currVal) return false
        }
        return false
    }
}
