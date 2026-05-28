package com.alibadi.quran.core.data

import java.util.Calendar
import kotlin.math.*

object HijriCalendarHelper {
    data class HijriDate(
        val year: Int,
        val month: Int,
        val monthNameAr: String,
        val monthNameEn: String,
        val day: Int
    )

    val MONTHS_AR = listOf(
        "المحرّم", "صفر", "ربيع الأوّل", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوّال", "ذو القعدة", "ذو الحجة"
    )

    val MONTHS_EN = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qa'dah", "Dhu al-Hijjah"
    )

    fun getHijriDate(calendar: Calendar, dayOffset: Int = 0): HijriDate {
        val cal = calendar.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, dayOffset)

        var year = cal.get(Calendar.YEAR)
        var month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        if (month < 3) {
            year -= 1
            month += 12
        }

        val a = floor(year / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (year + 4716)) + floor(30.6001 * (month + 1)) + day + b - 1524.5

        val epoch = 1948439.5
        val cycle = 10631.0
        val ljd = jd - epoch

        val n = floor(ljd / cycle)
        val ljdCycle = ljd - n * cycle

        val cy = floor((ljdCycle - 0.1124) / 354.36667)
        val ljdYear = ljdCycle - cy * 354.36667 - floor(cy * 11 / 30.0)

        val hYear = (n * 30 + cy + 1).toInt()
        var hMonth = floor((ljdYear + 0.5) / 29.5001).toInt() + 1
        var hDay = (ljdYear + 1.5 - floor(hMonth * 29.5001)).toInt()

        if (hMonth > 12) hMonth = 12
        if (hMonth < 1) hMonth = 1
        if (hDay > 30) hDay = 30
        if (hDay < 1) hDay = 1

        return HijriDate(
            year = hYear,
            month = hMonth,
            monthNameAr = MONTHS_AR[hMonth - 1],
            monthNameEn = MONTHS_EN[hMonth - 1],
            day = hDay
        )
    }

    fun getDaysToRamadan(calendar: Calendar, dayOffset: Int = 0): Int {
        val hijri = getHijriDate(calendar, dayOffset)
        if (hijri.month == 9) return 0 // It is currently Ramadan!

        val yrDiff = if (hijri.month > 9) 1 else 0
        val targetHijriYear = hijri.year + yrDiff

        // Approximate Julian Date of 1 Ramadan
        // Formula reference for starting Julian Date for Hijri year:
        // JD ≈ epoch + (H - 1) * 354.367 + 240 (which is 8 months of lunar year ≈ 236 days) + 1 (first day)
        val ramadanJd = epochForHijriYear(targetHijriYear) + 236.0 // 8 months elapsed before Ramadan
        
        // Julian date of current calendar
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH) + 1
        val d = calendar.get(Calendar.DAY_OF_MONTH)
        var tempY = y
        var tempM = m
        if (tempM <= 2) {
            tempY -= 1
            tempM += 12
        }
        val ca = floor(tempY / 100.0)
        val cb = 2 - ca + floor(ca / 4.0)
        val currentJd = floor(365.25 * (tempY + 4716)) + floor(30.6001 * (tempM + 1)) + d + cb - 1524.5

        val diff = ceil(ramadanJd - currentJd).toInt()
        return if (diff < 0) 0 else diff
    }

    private fun epochForHijriYear(hYear: Int): Double {
        val yLimit = hYear - 1
        val cycles = floor(yLimit / 30.0)
        val yearsInCycle = yLimit % 30
        val epoch = 1948439.5
        val daysInCycle = cycles * 10631.0
        val daysInYears = yearsInCycle * 354.0 + floor((yearsInCycle * 11 + 14) / 30.0)
        return epoch + daysInCycle + daysInYears
    }
}
