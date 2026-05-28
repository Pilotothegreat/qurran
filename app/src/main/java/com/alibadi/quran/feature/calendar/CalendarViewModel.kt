package com.alibadi.quran.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alibadi.quran.core.data.AppPreferencesDataStore
import com.alibadi.quran.core.data.HijriCalendarHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

data class IslamicEvent(
    val title: String,
    val date: String,
    val description: String,
    val tag: String,
    val color: String
)

class CalendarViewModel(
    private val prefs: AppPreferencesDataStore
) : ViewModel() {

    private val _dynamicEvents = MutableStateFlow<List<IslamicEvent>>(emptyList())
    val islamicEvents = _dynamicEvents.asStateFlow()

    // Hijri date flow derived reactively from current Calendar and offset
    val hijriDate: StateFlow<HijriCalendarHelper.HijriDate?> = prefs.hijriOffset
        .map { offset ->
            HijriCalendarHelper.getHijriDate(Calendar.getInstance(), offset)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val daysToRamadan: StateFlow<Int> = prefs.hijriOffset
        .map { offset ->
            HijriCalendarHelper.getDaysToRamadan(Calendar.getInstance(), offset)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        fetchDynamicEvents()
    }

    fun fetchDynamicEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            val defaults = listOf(
                IslamicEvent(
                    title = "مناصرة فلسطين الحبيبة 🇵🇸",
                    date = "فوري ودائم",
                    description = "الدعاء المتواصل لإخواننا في قطاع غزة والضفة الغربية والقدس الشريف بالثبات والفتح المبين والمساهمة في حملات الإغاثة الإنسانية والمساندة المستمرة والمقاطعة الواعية.",
                    tag = "القضية الفلسطينية",
                    color = "#E53935"
                ),
                IslamicEvent(
                    title = "يوم الجمعة المبارك 🕌",
                    date = "كل أسبوع",
                    description = "سنن وآداب يوم الجمعة: الاستحمام، والتطيب، ولبس أحسن الثياب، وقراءة سورة الكهف الشريفة، والتبكير إلى المصلى، والإكثار من الصلاة والسلام على خير الأنام.",
                    tag = "سنن مأثورة",
                    color = "#129676"
                ),
                IslamicEvent(
                    title = "فضل صيام النوافل 🌙",
                    date = "التقويم الهجري",
                    description = "احرص على إحياء سنة صيام الأيام البيض (١٣، ١٤، ١٥ من كل شهر هجري) وصيام الاثنين والخميس تقرباً وتزكية للنفس والبدن والمحافظة على اللياقة الروحية.",
                    tag = "التربية الذاتية",
                    color = "#D4AF37"
                ),
                IslamicEvent(
                    title = "صدقة جارية ميسرة 💧",
                    date = "تذكير يومي",
                    description = "المساهمة في بناء سقيا ماء أو دعم طالب علم أو كفالة يتيم من أعظم الأعمال بركة وأبقاها أثراً في الدنيا والآخرة.",
                    tag = "الخير والبركة",
                    color = "#1E88E5"
                )
            )

            try {
                val urlSpec = "https://raw.githubusercontent.com/al-labeeb/announcements-api/main/bulletin.json"
                val url = URL(urlSpec)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                if (conn.responseCode == 200) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    val array = org.json.JSONArray(text)
                    val fetched = mutableListOf<IslamicEvent>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        fetched.add(
                            IslamicEvent(
                                title = obj.getString("title"),
                                date = obj.getString("date"),
                                description = obj.getString("description"),
                                tag = obj.getString("tag"),
                                color = obj.optString("color", "#D4AF37")
                            )
                        )
                    }
                    if (fetched.isNotEmpty()) {
                        _dynamicEvents.value = fetched
                        return@launch
                    }
                }
            } catch (e: Exception) {
                // Silently fallback
            }
            _dynamicEvents.value = defaults
        }
    }
}
