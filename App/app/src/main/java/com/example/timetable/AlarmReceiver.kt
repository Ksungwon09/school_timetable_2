package com.example.timetable

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_UPDATE_WIDGET) {
            // Update timetable widget
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val timetableComponentName = ComponentName(context, TimetableWidgetProvider::class.java)
            val timetableAppWidgetIds = appWidgetManager.getAppWidgetIds(timetableComponentName)

            for (id in timetableAppWidgetIds) {
                TimetableWidgetProvider.updateAppWidget(context, appWidgetManager, id)
            }

            // Update meal widget
            val mealComponentName = ComponentName(context, MealWidgetProvider::class.java)
            val mealAppWidgetIds = appWidgetManager.getAppWidgetIds(mealComponentName)

            for (id in mealAppWidgetIds) {
                MealWidgetProvider.updateAppWidget(context, appWidgetManager, id)
            }

            // Schedule the next one
            scheduleAlarm(context)
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.timetable.UPDATE_WIDGET"
        const val INTERVAL_MILLIS = 30 * 60 * 1000L // 30 minutes

        fun scheduleAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // To bypass Doze mode / power management to update the widget frequently
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + INTERVAL_MILLIS,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + INTERVAL_MILLIS,
                    pendingIntent
                )
            }
        }

        fun cancelAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
