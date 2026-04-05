package com.example.timetable

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.timetable.databinding.ActivityMainBinding
import android.widget.TableRow
import android.widget.TextView
import android.util.TypedValue
import android.graphics.Color
import android.view.Gravity
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val API_KEY = "b1631ef776724f03a6925ba7b6daea99"
    private lateinit var neisApi: NeisApiService
    private lateinit var prefs: Prefs
    private lateinit var database: AppDatabase

    private val schools = mutableListOf<SchoolRow>()
    private val classes = mutableListOf<ClassRow>()
    private var currentWeekStart = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = Prefs(this)
        database = AppDatabase.getDatabase(this)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://open.neis.go.kr/hub/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        neisApi = retrofit.create(NeisApiService::class.java)

        // Set to this week Monday. If today is Sat or Sun, set to next week Monday.
        val today = Calendar.getInstance()
        if (today.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1)
        }
        currentWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        setupUI()

        if (prefs.isSetupComplete()) {
            showTimetableScreen()
        } else {
            showSearchScreen()
        }
    }

    private fun setupUI() {
        binding.btnSearchSchool.setOnClickListener {
            val query = binding.etSearchSchool.text.toString().trim()
            if (query.isEmpty()) {
                Toast.makeText(this, "학교 이름을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            searchSchool(query)
        }

        binding.lvSchools.setOnItemClickListener { _, _, position, _ ->
            val school = schools[position]
            prefs.ofcdcCode = school.ATPT_OFCDC_SC_CODE
            prefs.schoolCode = school.SD_SCHUL_CODE
            prefs.schoolName = school.SCHUL_NM
            showClassSelectionScreen()
        }

        binding.btnBackToSearch.setOnClickListener {
            prefs.clear()
            showSearchScreen()
        }

        binding.btnReset.setOnClickListener {
            prefs.clear()
            showSearchScreen()
        }

        binding.lvClasses.setOnItemClickListener { _, _, position, _ ->
            val cls = classes[position]
            prefs.dddepNm = cls.DDDEP_NM
            prefs.grade = cls.GRADE
            prefs.classNm = cls.CLASS_NM
            showTimetableScreen()
        }

        binding.btnPrevWeek.setOnClickListener {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1)
            loadTimetable()
        }

        binding.btnNextWeek.setOnClickListener {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1)
            loadTimetable()
        }
    }

    private fun searchSchool(query: String) {
        lifecycleScope.launch {
            try {
                val response = neisApi.getSchoolInfo(apiKey = API_KEY, schoolName = query)
                val row = response.schoolInfo?.find { it.row != null }?.row
                if (row.isNullOrEmpty()) {
                    Toast.makeText(this@MainActivity, "결과가 없습니다. 다시 입력하세요.", Toast.LENGTH_SHORT).show()
                } else {
                    schools.clear()
                    schools.addAll(row)
                    val adapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_list_item_2,
                        android.R.id.text1,
                        schools.map { "${it.SCHUL_NM}\n${it.ORG_RDNMA ?: ""}" }
                    )
                    binding.lvSchools.adapter = adapter
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "검색 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun showSearchScreen() {
        binding.layoutSearch.visibility = View.VISIBLE
        binding.layoutClassSelection.visibility = View.GONE
        binding.layoutTimetable.visibility = View.GONE
    }

    fun showClassSelectionScreen() {
        binding.layoutSearch.visibility = View.GONE
        binding.layoutClassSelection.visibility = View.VISIBLE
        binding.layoutTimetable.visibility = View.GONE
        binding.tvSelectedSchool.text = prefs.schoolName ?: "선택된 학교"
        loadClasses()
    }

    private fun loadClasses() {
        val ofcdcCode = prefs.ofcdcCode ?: return
        val schoolCode = prefs.schoolCode ?: return

        lifecycleScope.launch {
            try {
                val response = neisApi.getClassInfo(
                    apiKey = API_KEY,
                    ofcdcCode = ofcdcCode,
                    schoolCode = schoolCode
                )
                val row = response.classInfo?.find { it.row != null }?.row
                if (row.isNullOrEmpty()) {
                    Toast.makeText(this@MainActivity, "반 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    classes.clear()
                    classes.addAll(row)
                    val adapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_list_item_1,
                        classes.map { "${it.DDDEP_NM} ${it.GRADE}학년 ${it.CLASS_NM}반" }
                    )
                    binding.lvClasses.adapter = adapter
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "반 정보 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun showTimetableScreen() {
        binding.layoutSearch.visibility = View.GONE
        binding.layoutClassSelection.visibility = View.GONE
        binding.layoutTimetable.visibility = View.VISIBLE

        binding.tvTimetableTitle.text = "${prefs.schoolName} ${prefs.grade}학년 ${prefs.classNm}반"
        loadTimetable()
    }

    private fun loadTimetable() {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("MM.dd", Locale.getDefault())

        val dates = mutableListOf<String>()
        val cal = currentWeekStart.clone() as Calendar
        val startDateStr = dateFormat.format(cal.time)
        val startDisplay = displayFormat.format(cal.time)

        for (i in 0 until 5) {
            dates.add(dateFormat.format(cal.time))
            if (i < 4) cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        val endDateStr = dateFormat.format(cal.time)
        val endDisplay = displayFormat.format(cal.time)

        binding.tvCurrentWeek.text = "$startDisplay - $endDisplay"

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE

            // Try to load from cache first
            var localData = withContext(Dispatchers.IO) {
                database.timetableDao().getTimetableByDateRange(startDateStr, endDateStr)
            }

            if (localData.isNotEmpty()) {
                renderTimetable(localData, dates)
            }

            // Fetch from API
            try {
                val yearStr = currentWeekStart.get(Calendar.YEAR).toString()
                // Approximating semester: 1-7 is Sem 1, 8-12 is Sem 2
                val month = currentWeekStart.get(Calendar.MONTH) + 1
                val semStr = if (month <= 7) "1" else "2"

                val response = neisApi.getHisTimetable(
                    apiKey = API_KEY,
                    ofcdcCode = prefs.ofcdcCode!!,
                    schoolCode = prefs.schoolCode!!,
                    ay = yearStr,
                    sem = semStr,
                    dddepNm = prefs.dddepNm,
                    grade = prefs.grade!!,
                    classNm = prefs.classNm!!,
                    tiFromYmd = startDateStr,
                    tiToYmd = endDateStr
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

                    withContext(Dispatchers.IO) {
                        database.timetableDao().deleteByDateRange(startDateStr, endDateStr)
                        database.timetableDao().insertAll(entities)
                    }

                    localData = withContext(Dispatchers.IO) {
                        database.timetableDao().getTimetableByDateRange(startDateStr, endDateStr)
                    }
                    renderTimetable(localData, dates)
                } else if (localData.isEmpty()) {
                    Toast.makeText(this@MainActivity, "해당 주간의 시간표 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
                    renderTimetable(emptyList(), dates)
                }
            } catch (e: Exception) {
                if (localData.isEmpty()) {
                    Toast.makeText(this@MainActivity, "시간표 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun renderTimetable(data: List<TimetableEntity>, weekDates: List<String>) {
        binding.tableTimetable.removeAllViews()

        // Header Row
        val headerRow = TableRow(this)
        val headers = listOf("교시", "월", "화", "수", "목", "금")
        for (h in headers) {
            val tv = TextView(this)
            tv.text = h
            tv.setPadding(16, 16, 16, 16)
            tv.gravity = Gravity.CENTER
            tv.setBackgroundColor(Color.LTGRAY)
            headerRow.addView(tv)
        }
        binding.tableTimetable.addView(headerRow)

        val maxPeriod = data.maxOfOrNull { it.period.toIntOrNull() ?: 0 } ?: 7

        for (p in 1..maxPeriod) {
            val row = TableRow(this)

            // Period Number
            val tvPeriod = TextView(this)
            tvPeriod.text = p.toString()
            tvPeriod.setPadding(16, 16, 16, 16)
            tvPeriod.gravity = Gravity.CENTER
            tvPeriod.setBackgroundColor(Color.LTGRAY)
            row.addView(tvPeriod)

            // Days
            for (date in weekDates) {
                val item = data.find { it.date == date && it.period == p.toString() }
                val tvSubject = TextView(this)
                tvSubject.text = item?.subject ?: ""
                tvSubject.setPadding(8, 16, 8, 16)
                tvSubject.gravity = Gravity.CENTER
                tvSubject.setBackgroundResource(android.R.drawable.btn_default)
                row.addView(tvSubject)
            }

            binding.tableTimetable.addView(row)
        }
    }
}
