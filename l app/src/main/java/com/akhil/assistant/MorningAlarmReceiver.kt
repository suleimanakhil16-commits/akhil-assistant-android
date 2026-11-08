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
val nm = context.getSystemService(NotificationManager::class.java)
if (Build.VERSION.SDK_INT >= 26) {
nm.createNotificationChannel(NotificationChannel("akhil_morning", "Morning greeting", NotificationManager.IMPORTANCE_HIGH))
}
val open = PendingIntent.getActivity(context, 81, Intent(context, MainActivity::class.java),
PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
val b = if (Build.VERSION.SDK_INT >= 26) android.app.Notification.Builder(context, "akhil_morning")
else android.app.Notification.Builder(context)
nm.notify(801, b.setSmallIcon(android.R.drawable.ic_dialog_info)
.setContentTitle("Good morning!")
.setContentText("Good morning. What would you like AKHIL Assistant to open?")
.setContentIntent(open)
.setAutoCancel(true)
.build())
schedule(context)
}

companion object {  
    fun schedule(context: Context) {  
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager  
        val intent = Intent(context, MorningAlarmReceiver::class.java)  
        val pending = PendingIntent.getBroadcast(context, 81, intent,  
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)  
        val next = Calendar.getInstance().apply {  
            set(Calendar.HOUR_OF_DAY, 8)  
            set(Calendar.MINUTE, 0)  
            set(Calendar.SECOND, 0)  
            set(Calendar.MILLISECOND, 0)  
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)  
        }  
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.timeInMillis,  
            AlarmManager.INTERVAL_DAY, pending)  
    }  
}

}
