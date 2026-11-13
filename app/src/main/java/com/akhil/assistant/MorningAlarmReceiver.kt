
package com.akhil.assistant

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

class MorningAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val notificationManager =
            context.getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "akhil_morning",
                "Morning greeting",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = PendingIntent.getActivity(
            context,
            81,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.Notification.Builder(context, "akhil_morning")
        } else {
            @Suppress("DEPRECATION")
            android.app.Notification.Builder(context)
        }

        val notification = builder
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Good morning!")
            .setContentText(
                "Good morning. What would you like AKHIL Assistant to open?"
            )
            .setContentIntent(openAppIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(801, notification)
        schedule(context)
    }

    companion object {
        fun schedule(context: Context) {
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, MorningAlarmReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                81,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val nextAlarm = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                nextAlarm.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }
}
