package com.alibadi.quran.feature.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.alibadi.quran.core.data.PrayerTimeCalculator
import java.text.SimpleDateFormat
import java.util.*

class AdhanScheduler(private val context: Context) {

    fun schedulePrayerAlarms(prayerTimes: PrayerTimeCalculator.PrayerTimes, soundEnabled: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())

        val prayers = listOf(
            prayerTimes.fajr to "FAJR",
            prayerTimes.dhuhr to "DHUHR",
            prayerTimes.asr to "ASR",
            prayerTimes.maghrib to "MAGHRIB",
            prayerTimes.isha to "ISHA"
        )

        prayers.forEach { (timeStr, name) ->
            try {
                val parsedTime = format.parse(timeStr) ?: return@forEach
                val alarmTime = Calendar.getInstance().apply {
                    val timeCal = Calendar.getInstance().apply { time = parsedTime }
                    set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // If scheduled time has already passed today, schedule for tomorrow
                if (alarmTime.before(Calendar.getInstance())) {
                    alarmTime.add(Calendar.DAY_OF_YEAR, 1)
                }

                val intent = Intent(context, AdhanReceiver::class.java).apply {
                    putExtra("PRAYER_NAME", name)
                    putExtra("SOUND_ENABLED", soundEnabled)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    name.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            alarmTime.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            alarmTime.timeInMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
