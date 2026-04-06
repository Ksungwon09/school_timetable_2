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

class MealWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
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
            val views = RemoteViews(context.packageName, R.layout.widget_meal)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)

            if (!prefs.isSetupComplete()) {
                views.setTextViewText(R.id.tvWidgetTitle, "급식")
                views.setViewVisibility(R.id.tvWidgetEmpty, android.view.View.VISIBLE)
                views.setTextViewText(R.id.tvWidgetEmpty, "앱에서 학교를 먼저 설정하세요.")
                views.setViewVisibility(R.id.tvMealInfo, android.view.View.GONE)
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val todayStr = dateFormat.format(Calendar.getInstance().time)
            val displayFormat = SimpleDateFormat("MM.dd (E)", Locale.KOREA)
            val displayToday = displayFormat.format(Calendar.getInstance().time)

            views.setTextViewText(R.id.tvWidgetTitle, "${prefs.schoolName} $displayToday 급식")
            views.setViewVisibility(R.id.tvWidgetEmpty, android.view.View.GONE)

            val database = AppDatabase.getDatabase(context)

            CoroutineScope(Dispatchers.IO).launch {
                var localData = database.mealDao().getMealByDate(todayStr)

                if (localData == null) {
                    fetchDataSync(context, prefs, database, todayStr)
                    localData = database.mealDao().getMealByDate(todayStr)
                }

                withContext(Dispatchers.Main) {
                    if (localData == null) {
                        views.setViewVisibility(R.id.tvWidgetEmpty, android.view.View.VISIBLE)
                        views.setTextViewText(R.id.tvWidgetEmpty, "오늘의 급식 정보가 없습니다.")
                        views.setViewVisibility(R.id.tvMealInfo, android.view.View.GONE)
                    } else {
                        views.setViewVisibility(R.id.tvWidgetEmpty, android.view.View.GONE)
                        views.setViewVisibility(R.id.tvMealInfo, android.view.View.VISIBLE)
                        views.setTextViewText(R.id.tvMealInfo, localData.mealName)
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

                val response = neisApi.getMealInfo(
                    apiKey = "b1631ef776724f03a6925ba7b6daea99",
                    ofcdcCode = prefs.ofcdcCode!!,
                    schoolCode = prefs.schoolCode!!,
                    mlsvYmd = todayStr
                )

                val rows = response.mealServiceDietInfo?.find { it.row != null }?.row
                if (!rows.isNullOrEmpty()) {
                    val rawMeal = rows[0].DDISH_NM ?: ""
                    // Remove <br/> and format
                    val cleanedMeal = rawMeal.replace("<br/>", "\n")

                    val entity = MealEntity(
                        date = todayStr,
                        mealName = cleanedMeal
                    )
                    database.mealDao().deleteByDate(todayStr)
                    database.mealDao().insert(entity)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
