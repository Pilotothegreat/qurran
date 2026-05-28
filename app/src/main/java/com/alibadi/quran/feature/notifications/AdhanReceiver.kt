package com.alibadi.quran.feature.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.alibadi.quran.R

class AdhanReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "PRAYER"
        val soundEnabled = intent.getBooleanExtra("SOUND_ENABLED", true)

        val channelId = "adhan_notifications"
        val channelName = "Adhan Alerts"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily alarms for Islamic prayer times"
            }
            notificationManager?.createNotificationChannel(channel)
        }

        // Map English key to localized Arabic/English prayer name
        val localizedName = when (prayerName.uppercase()) {
            "FAJR" -> "الفجر"
            "DHUHR" -> "الظهر"
            "ASR" -> "العصر"
            "MAGHRIB" -> "المغرب"
            "ISHA" -> "العشاء"
            else -> prayerName
        }

        val notificationTitle = "نداء الصلاة"
        val notificationText = "حان الآن موعد أذان صلاة $localizedName"

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (soundEnabled) {
            builder.setSound(defaultSoundUri)
        }

        notificationManager?.notify(prayerName.hashCode(), builder.build())
    }
}
