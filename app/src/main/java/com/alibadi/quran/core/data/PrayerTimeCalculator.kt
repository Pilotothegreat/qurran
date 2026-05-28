package com.alibadi.quran.core.data

import java.util.Calendar
import kotlin.math.*

object PrayerTimeCalculator {
    data class PrayerTimes(
        val dateString: String,
        val fajr: String,
        val sunrise: String,
        val dhuhr: String,
        val asr: String,
        val maghrib: String,
        val isha: String
    )

    // Default coordinates for Al Awabi, Al Batinah, Oman
    const val DEFAULT_LAT = 23.4074
    const val DEFAULT_LON = 57.5108
    const val DEFAULT_TIMEZONE = 4.0 // Oman GMT+4

    fun calculateTimes(
        calendar: Calendar,
        latitude: Double = DEFAULT_LAT,
        longitude: Double = DEFAULT_LON,
        timezone: Double = DEFAULT_TIMEZONE,
        fajrAngle: Double = 18.0,
        ishaAngle: Double = 18.0,
        asrSchool: Int = 1 // 1 = Standard (Ibadi/Shafi'i), 2 = Hanafi
    ): PrayerTimes {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Julian date
        val d = julianDate(year, month, day) - 2451545.0

        // Keplerian Elements for the Sun
        val g = normalizeAngle(357.529 + 0.98560028 * d)
        val q = normalizeAngle(280.459 + 0.98564736 * d)
        val L = normalizeAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        val R = 1.00014 - 0.01671 * cos(Math.toRadians(g)) - 0.00014 * cos(Math.toRadians(2 * g))
        val e = 23.439 - 0.00000036 * d

        // Declination
        val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(L))))
        
        // Equation of time
        val RA = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(L)), cos(Math.toRadians(L)))) / 15.0
        val raNormalized = if (RA < 0) RA + 24 else RA
        val equationOfTime = (q / 15.0) - raNormalized

        // Transit / Mid-day (Dhuhr)
        var dhuhrTransit = 12.0 + timezone - (longitude / 15.0) - equationOfTime
        if (dhuhrTransit < 0) dhuhrTransit += 24.0
        if (dhuhrTransit > 24) dhuhrTransit -= 24.0

        // Sunset/Sunrise
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)

        // Sunrise/Sunset hour angles (Alt = -0.833 degrees for atmospheric refraction)
        val sunAlt = -0.833
        val sunriseHourAngleRad = run {
            val num = sin(Math.toRadians(sunAlt)) - sin(latRad) * sin(decRad)
            val den = cos(latRad) * cos(decRad)
            val ratio = num / den
            if (ratio in -1.0..1.0) acos(ratio) else Math.toRadians(90.0)
        }
        val sunriseHourAngle = Math.toDegrees(sunriseHourAngleRad)

        // Times in hours
        val sunriseHours = dhuhrTransit - (sunriseHourAngle / 15.0)
        val sunsetHours = dhuhrTransit + (sunriseHourAngle / 15.0)

        // Fajr Hour Angle (Def angle: 18.0)
        val fajrAlt = -fajrAngle
        val fajrHourAngleRad = run {
            val num = sin(Math.toRadians(fajrAlt)) - sin(latRad) * sin(decRad)
            val den = cos(latRad) * cos(decRad)
            val ratio = num / den
            if (ratio in -1.0..1.0) acos(ratio) else Math.toRadians(108.0)
        }
        val fajrHours = dhuhrTransit - (Math.toDegrees(fajrHourAngleRad) / 15.0)

        // Isha Hour Angle (Def angle: 18.0)
        val ishaAlt = -ishaAngle
        val ishaHourAngleRad = run {
            val num = sin(Math.toRadians(ishaAlt)) - sin(latRad) * sin(decRad)
            val den = cos(latRad) * cos(decRad)
            val ratio = num / den
            if (ratio in -1.0..1.0) acos(ratio) else Math.toRadians(108.0)
        }
        val ishaHours = dhuhrTransit + (Math.toDegrees(ishaHourAngleRad) / 15.0)

        // Asr Hour Angle
        // Shadow factor: 1 for Standard, 2 for Hanafi
        val shadowFactor = asrSchool.toDouble()
        val asrAltRad = atan(1.0 / (shadowFactor + tan(abs(latRad - decRad))))
        val asrHourAngleRad = run {
            val num = sin(asrAltRad) - sin(latRad) * sin(decRad)
            val den = cos(latRad) * cos(decRad)
            val ratio = num / den
            if (ratio in -1.0..1.0) acos(ratio) else Math.toRadians(50.0)
        }
        val asrHours = dhuhrTransit + (Math.toDegrees(asrHourAngleRad) / 15.0)

        val dateStr = String.format("%04d-%02d-%02d", year, month, day)

        // Adjust minutes to include standard small safety cushions
        return PrayerTimes(
            dateString = dateStr,
            fajr = formatTime(fajrHours),
            sunrise = formatTime(sunriseHours),
            dhuhr = formatTime(dhuhrTransit),
            asr = formatTime(asrHours),
            maghrib = formatTime(sunsetHours + (2.0 / 60.0)), // Add 2 mins safety buffer for Maghrib sunset tracking
            isha = formatTime(ishaHours)
        )
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun normalizeAngle(angle: Double): Double {
        var a = angle % 360.0
        if (a < 0) a += 360.0
        return a
    }

    private fun formatTime(hours: Double): String {
        var h = hours
        if (h.isNaN()) return "00:00"
        while (h < 0) h += 24.0
        while (h >= 24) h -= 24.0
        val totalMinutes = round(h * 60.0).toInt()
        val m = totalMinutes % 60
        val hr = (totalMinutes / 60) % 24
        return String.format("%02d:%02d", hr, m)
    }
}
