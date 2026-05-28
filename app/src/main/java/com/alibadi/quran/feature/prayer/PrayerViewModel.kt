package com.alibadi.quran.feature.prayer

import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alibadi.quran.core.data.dao.AppDao
import com.alibadi.quran.core.data.AppPreferencesDataStore
import com.alibadi.quran.core.data.PrayerTimeCalculator
import com.alibadi.quran.core.data.entities.PrayerLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class NewsItem(
    val title: String,
    val source: String,
    val pubDate: String,
    val description: String,
    val link: String
)

class PrayerViewModel(
    private val application: Application,
    private val prefs: AppPreferencesDataStore,
    private val dao: AppDao
) : AndroidViewModel(application) {

    private val _prayerTimesState = MutableStateFlow<PrayerTimeCalculator.PrayerTimes?>(null)
    val prayerTimesState = _prayerTimesState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Live clock for countdowns
    val liveTimeFlow: StateFlow<Calendar> = flow {
        while (true) {
            emit(Calendar.getInstance())
            delay(1000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Calendar.getInstance())

    private val _todayDateKey = MutableStateFlow(getTodayDateString())
    val todayDateKey: StateFlow<String> = _todayDateKey.asStateFlow()

    val todayPrayerLog: StateFlow<PrayerLog> = _todayDateKey.flatMapLatest { date ->
        dao.getPrayerLogForDate(date).map { log ->
            log ?: PrayerLog(dateKey = date)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrayerLog(dateKey = getTodayDateString()))

    val recentPrayerLogs: StateFlow<List<PrayerLog>> = dao.getRecentPrayerLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Palestine Live Bulletins and Ummah News Tickers
    private val _palestineUpdates = MutableStateFlow("اللهم انصر غزة وفلسطين. جاري جلب آخر تطورات القضية المباركة من الأبواق والمصادر المباشرة...")
    val palestineUpdates = _palestineUpdates.asStateFlow()

    private val _liveMuslimNews = MutableStateFlow<List<NewsItem>>(emptyList())
    val liveMuslimNews = _liveMuslimNews.asStateFlow()

    // Active offset cache variables updated on preference collect
    private var cachedOffsetFajr = 0
    private var cachedOffsetSunrise = 0
    private var cachedOffsetDhuhr = 0
    private var cachedOffsetAsr = 0
    private var cachedOffsetMaghrib = 0
    private var cachedOffsetIsha = 0

    init {
        // Collect offsets and recalculate
        viewModelScope.launch {
            combine(
                prefs.offsetFajr, prefs.offsetSunrise, prefs.offsetDhuhr,
                prefs.offsetAsr, prefs.offsetMaghrib, prefs.offsetIsha
            ) { offsets ->
                cachedOffsetFajr = offsets[0]
                cachedOffsetSunrise = offsets[1]
                cachedOffsetDhuhr = offsets[2]
                cachedOffsetAsr = offsets[3]
                cachedOffsetMaghrib = offsets[4]
                cachedOffsetIsha = offsets[5]
            }.collect {
                recalculateTimes()
            }
        }

        // Collect coordinate / settings updates and recalculate
        viewModelScope.launch {
            combine(
                prefs.latitude, prefs.longitude, prefs.asrSchool, prefs.useOnlinePrayer
            ) { lat, lon, school, online ->
                // Trigger recalculation
            }.collect {
                recalculateTimes()
            }
        }

        observeLiveTimeChanges()
        fetchPalestineLiveUpdates()
        fetchLiveMuslimNews()
    }

    fun recalculateTimes() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val tz = TimeZone.getDefault()
            val tzOffsetHours = tz.getOffset(cal.timeInMillis) / 3600000.0
            
            val lat = prefs.latitude.first()
            val lon = prefs.longitude.first()
            val school = prefs.asrSchool.first()
            val online = prefs.useOnlinePrayer.first()

            val offlineTimes = PrayerTimeCalculator.calculateTimes(
                calendar = cal,
                latitude = lat,
                longitude = lon,
                timezone = tzOffsetHours,
                fajrAngle = 18.0,
                ishaAngle = 18.0,
                asrSchool = school
            )

            if (online) {
                // Since cache reading is async from DataStore or db, we'll try to use network if we can,
                // otherwise use offlinetimes as fallback
                _prayerTimesState.value = adjustTimesWithOffsets(offlineTimes)
                // Trigger background update
                syncOnlinePrayerTimes { _, _ -> }
            } else {
                _prayerTimesState.value = adjustTimesWithOffsets(offlineTimes)
            }
        }
    }

    fun syncOnlinePrayerTimes(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshing.value = true
            try {
                val online = prefs.useOnlinePrayer.first()
                if (!online) {
                    recalculateTimes()
                    _isRefreshing.value = false
                    onResult(true, "Switched to high-precision offline model")
                    return@launch
                }

                val todayDdmmyyyy = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
                val lat = prefs.latitude.first()
                val lon = prefs.longitude.first()

                val urlSpec = "https://api.aladhan.com/v1/timings/$todayDdmmyyyy?latitude=$lat&longitude=$lon&method=99&methodSettings=18,null,18"
                val url = java.net.URL(urlSpec)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode == 200) {
                    val text = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = org.json.JSONObject(text)
                    if (json.getInt("code") == 200) {
                        val timings = json.getJSONObject("data").getJSONObject("timings")

                        val onlineTimes = PrayerTimeCalculator.PrayerTimes(
                            dateString = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                            fajr = timings.getString("Fajr"),
                            sunrise = timings.getString("Sunrise"),
                            dhuhr = timings.getString("Dhuhr"),
                            asr = timings.getString("Asr"),
                            maghrib = timings.getString("Maghrib"),
                            isha = timings.getString("Isha")
                        )

                        // Save cached times response in preferences if needed, or simply apply
                        _prayerTimesState.value = adjustTimesWithOffsets(onlineTimes)
                        _isRefreshing.value = false
                        onResult(true, "Synchronized with Aladhan server successfully!")
                    } else {
                        _isRefreshing.value = false
                        onResult(false, "Server returned error status")
                    }
                } else {
                    _isRefreshing.value = false
                    onResult(false, "API server returned HTTP error ${connection.responseCode}")
                }
            } catch (e: Exception) {
                _isRefreshing.value = false
                onResult(false, "Network connection error. Fell back to offline calculations.")
            }
        }
    }

    fun detectLocationOfflineOrOnline(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = java.net.URL("https://ip-api.com/json")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode == 200) {
                    val text = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = org.json.JSONObject(text)
                    if (json.getString("status") == "success") {
                        val lat = json.getDouble("lat")
                        val lon = json.getDouble("lon")
                        val city = json.optString("city", "Unknown City")
                        val country = json.optString("country", "Unknown Country")

                        prefs.set(AppPreferencesDataStore.KEY_LATITUDE, lat)
                        prefs.set(AppPreferencesDataStore.KEY_LONGITUDE, lon)
                        prefs.set(AppPreferencesDataStore.KEY_LOCATION_NAME, "$city, $country")

                        recalculateTimes()
                        onResult(true, "Detected Location: $city, $country")
                    } else {
                        onResult(false, "Location service rejected the request")
                    }
                } else {
                    onResult(false, "Server returned response code ${connection.responseCode}")
                }
            } catch (e: Exception) {
                onResult(false, "Network error: Make sure you are connected to the internet.")
            }
        }
    }

    fun detectGPSLocation(onResult: (Boolean, String) -> Unit) {
        val finePerm = ContextCompat.checkSelfPermission(application, android.Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePerm = ContextCompat.checkSelfPermission(application, android.Manifest.permission.ACCESS_COARSE_LOCATION)

        if (finePerm != android.content.pm.PackageManager.PERMISSION_GRANTED && coarsePerm != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            onResult(false, "Permission Denied: Please authorize location permissions.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                var bestLocation: android.location.Location? = null

                try {
                    val netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (netLoc != null) {
                        bestLocation = netLoc
                    }
                } catch (e: SecurityException) { /* no-op */ }

                try {
                    val gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (gpsLoc != null) {
                        if (bestLocation == null || gpsLoc.accuracy < bestLocation.accuracy) {
                            bestLocation = gpsLoc
                        }
                    }
                } catch (e: SecurityException) { /* no-op */ }

                if (bestLocation != null) {
                    val lat = bestLocation.latitude
                    val lon = bestLocation.longitude

                    prefs.set(AppPreferencesDataStore.KEY_LATITUDE, lat)
                    prefs.set(AppPreferencesDataStore.KEY_LONGITUDE, lon)

                    var resolvedName = ""
                    try {
                        val geocoder = Geocoder(application, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val city = address.locality ?: address.subAdminArea ?: address.adminArea ?: "Oman"
                            val country = address.countryName ?: "Oman"
                            resolvedName = "$city, $country"
                        } else {
                            resolvedName = "Coords: ${String.format(Locale.US, "%.4f, %.4f", lat, lon)}"
                        }
                    } catch (e: Exception) {
                        resolvedName = "Coords: ${String.format(Locale.US, "%.4f, %.4f", lat, lon)}"
                    }

                    prefs.set(AppPreferencesDataStore.KEY_LOCATION_NAME, resolvedName)
                    recalculateTimes()
                    onResult(true, "GPS Success: Located at $resolvedName")
                } else {
                    detectLocationOfflineOrOnline { success, msg ->
                        if (success) {
                            onResult(true, msg)
                        } else {
                            onResult(false, "Could not obtain GPS or Network lock. Fell back to default coordinates.")
                        }
                    }
                }
            } catch (e: Exception) {
                onResult(false, "Location hardware error: ${e.localizedMessage}")
            }
        }
    }

    private fun adjustTimesWithOffsets(orig: PrayerTimeCalculator.PrayerTimes): PrayerTimeCalculator.PrayerTimes {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        fun addMinutes(timeStr: String, offsetMins: Int): String {
            if (offsetMins == 0) return timeStr
            return try {
                val date = format.parse(timeStr) ?: return timeStr
                val cal = Calendar.getInstance().apply {
                    time = date
                    add(Calendar.MINUTE, offsetMins)
                }
                format.format(cal.time)
            } catch (e: Exception) {
                timeStr
            }
        }

        return orig.copy(
            fajr = addMinutes(orig.fajr, cachedOffsetFajr),
            sunrise = addMinutes(orig.sunrise, cachedOffsetSunrise),
            dhuhr = addMinutes(orig.dhuhr, cachedOffsetDhuhr),
            asr = addMinutes(orig.asr, cachedOffsetAsr),
            maghrib = addMinutes(orig.maghrib, cachedOffsetMaghrib),
            isha = addMinutes(orig.isha, cachedOffsetIsha)
        )
    }

    fun togglePrayerCompleted(prayerName: String) {
        viewModelScope.launch {
            val currentLog = todayPrayerLog.value
            val updated = when (prayerName.lowercase()) {
                "fajr" -> currentLog.copy(fajrCompleted = !currentLog.fajrCompleted)
                "dhuhr" -> currentLog.copy(dhuhrCompleted = !currentLog.dhuhrCompleted)
                "asr" -> currentLog.copy(asrCompleted = !currentLog.asrCompleted)
                "maghrib" -> currentLog.copy(maghribCompleted = !currentLog.maghribCompleted)
                "isha" -> currentLog.copy(ishaCompleted = !currentLog.ishaCompleted)
                else -> currentLog
            }
            dao.insertOrUpdatePrayerLog(updated)
        }
    }

    fun fetchPalestineLiveUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = java.net.URL("https://raw.githubusercontent.com/al-labeeb/announcements-api/main/palestine.json")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                if (conn.responseCode == 200) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    val obj = org.json.JSONObject(text)
                    val updateContent = obj.optString("update", "")
                    if (updateContent.isNotBlank()) {
                        _palestineUpdates.value = updateContent
                        return@launch
                    }
                }
            } catch (e: Exception) {
                // Squelch
            }
            _palestineUpdates.value = "صمود أسطوري لأهلنا المرابطين في غزة وعموم قطاعاتها والمسجد الأقصى المبارك. تستمر حملات الإغاثة الدولية بتوريد المياه والمؤن الطبية عبر المعابر، والتبرع والمقاطعة مستمران من كل محب للعدل في العالم."
        }
    }

    fun fetchLiveMuslimNews() {
        viewModelScope.launch(Dispatchers.IO) {
            val defaults = listOf(
                NewsItem(
                    title = "انطلاق المؤتمر الإسلامي العالمي لتدارس التحديات الراهنة للأمة بالمدينة المنورة",
                    source = "أم القرى الإخبارية",
                    pubDate = "اليوم",
                    description = "تجمع واسع للعلماء والمفكرين لمناقشة سبل تماسك الأمة ودعم الأسر الفقيرة وتعزيز التعليم الشرعي الصحيح المتوازن.",
                    link = "https://www.aljazeera.net"
                ),
                NewsItem(
                    title = "إعمار وصيانة المساجد الأثرية القديمة بولايات عُمان لحمايتها وتأهيلها للعبادة",
                    source = "شؤون الأوقاف العمانية",
                    pubDate = "أمس",
                    description = "كشفت وزارة الأوقاف والشؤون الدينية عن خطط شاملة لإعادة ترميم المخطوطات الأثرية وصيانة الجدران الحجرية المتلاشية في المساجد التاريخية القديمة.",
                    link = "https://www.aljazeera.net"
                )
            )

            try {
                val url = java.net.URL("https://raw.githubusercontent.com/al-labeeb/announcements-api/main/muslim-news.json")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                if (conn.responseCode == 200) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    val array = org.json.JSONArray(text)
                    val list = mutableListOf<NewsItem>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        list.add(
                            NewsItem(
                                title = obj.getString("title"),
                                source = obj.optString("source", "إعلام مباشر"),
                                pubDate = obj.optString("pubDate", "مباشر"),
                                description = obj.getString("description"),
                                link = obj.optString("link", "https://www.aljazeera.net")
                            )
                        )
                    }
                    if (list.isNotEmpty()) {
                        _liveMuslimNews.value = list
                        return@launch
                    }
                }
            } catch (e: Exception) {
                try {
                    val url = java.net.URL("https://mubasher.aljazeera.net/rss")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000
                    if (conn.responseCode == 200) {
                        val text = conn.inputStream.bufferedReader().use { it.readText() }
                        val items = mutableListOf<NewsItem>()
                        val parts = text.split("<item>")
                        var index = 0
                        for (i in 1 until parts.size) {
                            if (index >= 5) break
                            val itemContent = parts[i].substringBefore("</item>")

                            fun getTagContent(block: String, startTag: String, endTag: String): String {
                                val sIdx = block.indexOf(startTag)
                                if (sIdx == -1) return ""
                                val startContent = sIdx + startTag.length
                                val eIdx = block.indexOf(endTag, startContent)
                                if (eIdx == -1) return ""
                                return block.substring(startContent, eIdx)
                                    .replace("<![CDATA[", "")
                                    .replace("]]>", "")
                                    .trim()
                            }

                            val titleRaw = getTagContent(itemContent, "<title>", "</title>")
                            val title = if (titleRaw.isNotBlank()) titleRaw else "خبر عاجل"

                            val descRaw = getTagContent(itemContent, "<description>", "</description>")
                            val desc = if (descRaw.isNotBlank()) descRaw.take(180) else "تابع التطورات مباشرة من المراسلين في مختلف العواصم والبلدان الإسلامية."

                            val dateRaw = getTagContent(itemContent, "<pubDate>", "</pubDate>")
                            val date = if (dateRaw.isNotBlank()) dateRaw.take(16) else "اليوم"

                            items.add(
                                NewsItem(
                                    title = title,
                                    source = "الجزيرة عاجل",
                                    pubDate = date,
                                    description = desc,
                                    link = "https://mubasher.aljazeera.net"
                                )
                            )
                            index++
                        }
                        if (items.isNotEmpty()) {
                            _liveMuslimNews.value = items
                            return@launch
                        }
                    }
                } catch (ex: Exception) { /* no-op */ }
            }
            _liveMuslimNews.value = defaults
        }
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun observeLiveTimeChanges() {
        viewModelScope.launch {
            liveTimeFlow.collect { cal ->
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
                if (todayStr != _todayDateKey.value) {
                    _todayDateKey.value = todayStr
                    recalculateTimes()
                }
            }
        }
    }
}
