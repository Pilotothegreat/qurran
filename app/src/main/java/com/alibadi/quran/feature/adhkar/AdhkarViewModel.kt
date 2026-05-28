package com.alibadi.quran.feature.adhkar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alibadi.quran.core.data.dao.AppDao
import com.alibadi.quran.core.data.AppPreferencesDataStore
import com.alibadi.quran.core.data.entities.AdhkarItem
import com.alibadi.quran.core.data.entities.TasbihCounter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class AdhkarViewModel(
    private val dao: AppDao,
    private val prefs: AppPreferencesDataStore
) : ViewModel() {

    private val _selectedAdhkarCategory = MutableStateFlow("Post-Prayer")
    val selectedAdhkarCategory = _selectedAdhkarCategory.asStateFlow()

    private val _adhkarCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val adhkarCounts = _adhkarCounts.asStateFlow()

    // Standalone Tasbih counter logic
    private val _standaloneTasbihCount = MutableStateFlow(0)
    val standaloneTasbihCount = _standaloneTasbihCount.asStateFlow()

    private val _standaloneDhikrNameIdx = MutableStateFlow(0)
    val standaloneDhikrNameIdx = _standaloneDhikrNameIdx.asStateFlow()

    val standaloneDhikrPhrases = listOf(
        Pair("سُبْحَانَ اللَّهِ", "SubhanAllah"),
        Pair("الْحَمْدُ لِلَّهِ", "Alhamdulillah"),
        Pair("لَا إِلَهَ إِلَّا اللَّهُ", "La ilaha illallah"),
        Pair("اللَّهُ أَكْبَرُ", "Allahu Akbar"),
        Pair("أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ", "Astaghfirullah Al'Azheem")
    )

    // Dynamic Adhkar Items Flow
    val allAdhkarItems: StateFlow<List<AdhkarItem>> = dao.getAllAdhkarItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredAdhkarItems: StateFlow<List<AdhkarItem>> = combine(allAdhkarItems, _selectedAdhkarCategory) { items, category ->
        items.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Load initial counts from DB
        viewModelScope.launch {
            allAdhkarItems.collect { items ->
                val counts = mutableMapOf<Int, Int>()
                items.forEach { item ->
                    val counter = dao.getTasbihCounter("adhkar_${item.id}")
                    counts[item.id] = counter?.count ?: 0
                }
                _adhkarCounts.value = counts
            }
        }

        // Feature 3 — Midnight Auto-Reset
        viewModelScope.launch {
            var lastDayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            while (true) {
                delay(60_000)
                val today = Calendar.getInstance()
                val currentDayOfYear = today.get(Calendar.DAY_OF_YEAR)
                if (currentDayOfYear != lastDayOfYear) {
                    dao.resetAllAdhkarCounts()
                    // Re-sync local state counts to 0
                    val resetCounts = _adhkarCounts.value.mapValues { 0 }
                    _adhkarCounts.value = resetCounts
                    lastDayOfYear = currentDayOfYear
                }
            }
        }
    }

    fun setAdhkarCategory(category: String) {
        _selectedAdhkarCategory.value = category
    }

    fun incrementAdhkar(item: AdhkarItem) {
        val currentCount = _adhkarCounts.value[item.id] ?: 0
        val nextCount = if (currentCount >= item.countTarget) 0 else currentCount + 1

        _adhkarCounts.value = _adhkarCounts.value.toMutableMap().apply {
            put(item.id, nextCount)
        }

        viewModelScope.launch {
            dao.insertOrUpdateTasbihCounter(
                TasbihCounter(dhikrKey = "adhkar_${item.id}", count = nextCount, target = item.countTarget)
            )
        }
    }

    fun resetAdhkar(id: Int) {
        _adhkarCounts.value = _adhkarCounts.value.toMutableMap().apply {
            put(id, 0)
        }
        viewModelScope.launch {
            dao.insertOrUpdateTasbihCounter(
                TasbihCounter(dhikrKey = "adhkar_$id", count = 0)
            )
        }
    }

    fun incrementStandaloneTasbih() {
        _standaloneTasbihCount.value = _standaloneTasbihCount.value + 1
    }

    fun resetStandaloneTasbih() {
        _standaloneTasbihCount.value = 0
    }

    fun cycleStandaloneDhikr() {
        val nextIdx = (_standaloneDhikrNameIdx.value + 1) % standaloneDhikrPhrases.size
        _standaloneDhikrNameIdx.value = nextIdx
        _standaloneTasbihCount.value = 0
    }
}
