package com.example.timetable

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("timetable_prefs", Context.MODE_PRIVATE)

    var ofcdcCode: String?
        get() = prefs.getString("ofcdcCode", null)
        set(value) = prefs.edit().putString("ofcdcCode", value).apply()

    var schoolCode: String?
        get() = prefs.getString("schoolCode", null)
        set(value) = prefs.edit().putString("schoolCode", value).apply()

    var schoolName: String?
        get() = prefs.getString("schoolName", null)
        set(value) = prefs.edit().putString("schoolName", value).apply()

    var dddepNm: String?
        get() = prefs.getString("dddepNm", null)
        set(value) = prefs.edit().putString("dddepNm", value).apply()

    var grade: String?
        get() = prefs.getString("grade", null)
        set(value) = prefs.edit().putString("grade", value).apply()

    var classNm: String?
        get() = prefs.getString("classNm", null)
        set(value) = prefs.edit().putString("classNm", value).apply()

    var widgetTextSize: Int
        get() = prefs.getInt("widgetTextSize", 14)
        set(value) = prefs.edit().putInt("widgetTextSize", value).apply()

    var widgetTextBold: Boolean
        get() = prefs.getBoolean("widgetTextBold", false)
        set(value) = prefs.edit().putBoolean("widgetTextBold", value).apply()

    fun isSetupComplete(): Boolean {
        return ofcdcCode != null && schoolCode != null && grade != null && classNm != null
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
