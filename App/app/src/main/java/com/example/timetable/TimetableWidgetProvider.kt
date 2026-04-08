package com.example.timetable

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TimetableWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        // Schedule alarm for updates
        AlarmReceiver.scheduleAlarm(context)
    }

    override fun onEnabled(context: Context) {
        AlarmReceiver.scheduleAlarm(context)
    }

    override fun onDisabled(context: Context) {
        AlarmReceiver.cancelAlarm(context)
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = Prefs(context)
            val views = RemoteViews(context.packageName, R.layout.widget_timetable)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)

            val refreshIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_UPDATE_WIDGET
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnRefreshWidget, refreshPendingIntent)

            if (!prefs.isSetupComplete()) {
                views.setTextViewText(R.id.tvWidgetTitle, "시간표")
                views.setViewVisibility(R.id.tvWidgetEmpty, android.view.View.VISIBLE)
                views.setTextViewText(R.id.tvWidgetEmpty, "앱에서 학교와 반을 먼저 설정하세요.")
                views.removeAllViews(R.id.layoutWidgetTimetable)
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val todayStr = dateFormat.format(Calendar.getInstance().time)
            val displayFormat = SimpleDateFormat("MM.dd (E)", Locale.KOREA)
            val displayToday = displayFormat.format(Calendar.getInstance().time)

            views.setTextViewText(R.id.tvWidgetTitle, "${prefs.schoolName} $displayToday")
            views.setViewVisibility(R.id.tvWidgetEmpty, android.view.View.GONE)

            val database = AppDatabase.getDatabase(context)

            CoroutineScope(Dispatchers.IO).launch {
                var localData = database.timetableDao().getTimetableByDate(todayStr)

                // If no local data, we attempt to fetch it right away, but widgets shouldn't block.
                if (localData.isEmpty()) {
                    fetchDataSync(context, prefs, database, todayStr)
                    localData = database.timetableDao().getTimetableByDate(todayStr)
                }

                withContext(Dispatchers.Main) {
                    views.removeAllViews(R.id.layoutWidgetTimetable)

                    if (localData.isEmpty()) {
                        views.setViewVisibility(R.id.tvWidgetEmpty, android.view.View.VISIBLE)
                        views.setTextViewText(R.id.tvWidgetEmpty, "오늘의 수업이 없습니다.")
                    } else {
                        views.setViewVisibility(R.id.tvWidgetEmpty, android.view.View.GONE)
                        for (item in localData) {
                            val rowView = RemoteViews(context.packageName, R.layout.item_widget_row)
                            rowView.setTextViewText(R.id.tvPeriod, "${item.period}교시")
                            rowView.setTextViewText(R.id.tvSubject, item.subject)
                            views.addView(R.id.layoutWidgetTimetable, rowView)
                        }
                    }
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        private suspend fun fetchDataSync(context: Context, prefs: Prefs, database: AppDatabase, todayStr: String) {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://open.neis.go.kr/hub/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val neisApi = retrofit.create(NeisApiService::class.java)

                val cal = Calendar.getInstance()
                val yearStr = cal.get(Calendar.YEAR).toString()
                val month = cal.get(Calendar.MONTH) + 1
                val semStr = if (month <= 7) "1" else "2"

                val response = neisApi.getHisTimetable(
                    apiKey = "b1631ef776724f03a6925ba7b6daea99",
                    ofcdcCode = prefs.ofcdcCode!!,
                    schoolCode = prefs.schoolCode!!,
                    ay = yearStr,
                    sem = semStr,
                    dddepNm = prefs.dddepNm,
                    grade = prefs.grade!!,
                    classNm = prefs.classNm!!,
                    tiFromYmd = todayStr,
                    tiToYmd = todayStr
                )

                val rows = response.hisTimetable?.find { it.row != null }?.row
                if (!rows.isNullOrEmpty()) {
                    val entities = rows.map {
                        TimetableEntity(
                            date = it.ALL_TI_YMD ?: "",
                            period = it.PERIO ?: "",
                            subject = it.ITRT_CNTNT ?: ""
                        )
                    }
                    database.timetableDao().deleteByDateRange(todayStr, todayStr)
                    database.timetableDao().insertAll(entities)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
