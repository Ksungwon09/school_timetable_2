package com.example.timetable

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = Prefs(this)

        val seekTextSize = findViewById<SeekBar>(R.id.seekTextSize)
        val switchTextBold = findViewById<Switch>(R.id.switchTextBold)
        val btnSaveSettings = findViewById<Button>(R.id.btnSaveSettings)

        // Text size progress ranges from 0 to 10 (representing min 10sp, max 20sp, default 14sp is progress 4)
        val currentSize = prefs.widgetTextSize
        seekTextSize.progress = currentSize - 10

        switchTextBold.isChecked = prefs.widgetTextBold

        btnSaveSettings.setOnClickListener {
            prefs.widgetTextSize = seekTextSize.progress + 10
            prefs.widgetTextBold = switchTextBold.isChecked

            // Notify widget to update
            val intent = Intent(this, TimetableWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
                ComponentName(application, TimetableWidgetProvider::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            sendBroadcast(intent)

            finish()
        }
    }
}
