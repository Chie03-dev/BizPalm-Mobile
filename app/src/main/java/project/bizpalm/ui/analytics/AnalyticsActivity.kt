package project.bizpalm.ui.analytics

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import project.bizpalm.R
import project.bizpalm.data.database.AppDatabase
import project.bizpalm.data.database.dao.TransactionDao
import project.bizpalm.data.models.BestSeller
import project.bizpalm.data.models.DailySales
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var hourlyChart: LineChart
    private lateinit var distributionChart: HorizontalBarChart
    private lateinit var forecastChart: com.github.mikephil.charting.charts.BarChart
    private lateinit var viewModel: SalesAnalyticsViewModel
    private lateinit var etTrendDays: TextInputEditText
    private lateinit var etBestsellerLimit: TextInputEditText
    private lateinit var btnUpdateTrend: Button
    private lateinit var btnUpdateBestsellers: Button
    private lateinit var toggleForecastRange: MaterialButtonToggleGroup
    private lateinit var tvForecastDisclaimer: TextView
    private lateinit var btnMonthlyHistory: MaterialButton
    private lateinit var btnPrintSummaryReport: MaterialButton
    private lateinit var ivHelp: android.widget.ImageView

    private var currentTrendLiveData: LiveData<List<DailySales>>? = null
    private var currentBestsellersLiveData: LiveData<List<BestSeller>>? = null
    private var isMonthlyForecast = false

    companion object {
        private const val PREFS_NAME = "AnalyticsPrefs"
        private const val KEY_TREND_DAYS = "trend_days"
        private const val KEY_BESTSELLER_LIMIT = "bestseller_limit"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        initViews()
        viewModel = ViewModelProvider(this)[SalesAnalyticsViewModel::class.java]
        
        setupCharts()
        setupClickListeners()
        setupObservers()
        
        loadPreferencesAndRefresh()
    }

    private fun initViews() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        lineChart = findViewById(R.id.lineChart)
        hourlyChart = findViewById(R.id.hourlyChart)
        distributionChart = findViewById(R.id.distributionChart)
        forecastChart = findViewById(R.id.forecastChart)
        etTrendDays = findViewById(R.id.etTrendDays)
        etBestsellerLimit = findViewById(R.id.etBestsellerLimit)
        btnUpdateTrend = findViewById(R.id.btnUpdateTrend)
        btnUpdateBestsellers = findViewById(R.id.btnUpdateBestsellers)
        toggleForecastRange = findViewById(R.id.toggleForecastRange)
        tvForecastDisclaimer = findViewById(R.id.tvForecastDisclaimer)
        btnMonthlyHistory = findViewById(R.id.btnMonthlyHistory)
        btnPrintSummaryReport = findViewById(R.id.btnPrintDailyReport)
        ivHelp = findViewById(R.id.ivHelp)
        
        // Update button text to match requirement
        btnPrintSummaryReport.text = "Summary Reports"
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupCharts() {
        setupLineChartStyle(lineChart)
        lineChart.setExtraOffsets(5f, 10f, 5f, 15f)
        val l = lineChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.isEnabled = true

        setupLineChartStyle(hourlyChart)
        hourlyChart.description.text = "Average sales per hour of day"
        hourlyChart.legend.isEnabled = false
        
        setupHorizontalBarChartStyle(distributionChart)
        setupBarChartStyle(forecastChart)
    }

    private fun setupBarChartStyle(chart: com.github.mikephil.charting.charts.BarChart) {
        chart.description.isEnabled = false
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.setPinchZoom(false)
        chart.setDrawGridBackground(false)
        
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.setDrawLabels(true)
        
        chart.axisRight.isEnabled = false
        chart.axisLeft.setDrawGridLines(false)
        chart.axisLeft.axisMinimum = 0f
        chart.legend.isEnabled = false
    }

    private fun setupLineChartStyle(chart: LineChart) {
        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.setPinchZoom(false)
        
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.setAvoidFirstLastClipping(true)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupHorizontalBarChartStyle(chart: HorizontalBarChart) {
        chart.description.isEnabled = false
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true) 
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)
        chart.setDoubleTapToZoomEnabled(false)
        chart.setExtraOffsets(0f, 0f, 30f, 0f)

        chart.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> v.parent.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.parent.requestDisallowInterceptTouchEvent(false)
            }
            v.onTouchEvent(event)
            true
        }

        chart.xAxis.isEnabled = false 
        chart.axisLeft.setDrawGridLines(false)
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.setDrawLabels(false)
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false
    }

    private fun setupObservers() {
        viewModel.getHourlySales().observe(this) { updateHourlyChart(it) }
    }

    private fun loadPreferencesAndRefresh() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedDays = prefs.getInt(KEY_TREND_DAYS, 7)
        val savedLimit = prefs.getInt(KEY_BESTSELLER_LIMIT, 5)

        etTrendDays.setText(savedDays.toString())
        etBestsellerLimit.setText(savedLimit.toString())

        refreshData(savedDays, savedLimit)
    }

    private fun setupClickListeners() {
        ivHelp.setOnClickListener {
            showHelpDialog()
        }

        btnUpdateTrend.setOnClickListener {
            val input = etTrendDays.text.toString()
            if (input.isNotEmpty()) {
                try {
                    val days = input.toInt()
                    if (days in 1..9999) {
                        savePreference(KEY_TREND_DAYS, days)
                        refreshTrend(days)
                    } else {
                        Toast.makeText(this, "Please enter a value between 1 and 9999", Toast.LENGTH_SHORT).show()
                    }
                } catch (ignored: NumberFormatException) {}
            }
        }

        btnUpdateBestsellers.setOnClickListener {
            val input = etBestsellerLimit.text.toString()
            if (input.isNotEmpty()) {
                try {
                    val limit = input.toInt()
                    if (limit in 1..9999) {
                        savePreference(KEY_BESTSELLER_LIMIT, limit)
                        refreshBestsellers(limit)
                    } else {
                        Toast.makeText(this, "Please enter a value between 1 and 9999", Toast.LENGTH_SHORT).show()
                    }
                } catch (ignored: NumberFormatException) {}
            }
        }

        toggleForecastRange.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isMonthlyForecast = (checkedId == R.id.btnMonthly)
                tvForecastDisclaimer.text = if (isMonthlyForecast) 
                    "Projected sales volume for the next month." 
                else "Projected sales volume for the next week."
                currentBestsellersLiveData?.value?.let { updateForecastChart(it) }
            }
        }

        btnMonthlyHistory.setOnClickListener {
            startActivity(Intent(this, MonthlyPopularityActivity::class.java))
        }

        btnPrintSummaryReport.setOnClickListener {
            startActivity(Intent(this, ReportPickerActivity::class.java))
        }
    }

    private fun showHelpDialog() {
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_strategic_insights_help, null)
        bottomSheetDialog.setContentView(view)

        val switchAdvanced = view.findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.switchAdvanced)
        val tvPerformanceAdvanced = view.findViewById<TextView>(R.id.tvPerformanceAdvanced)
        val tvPeakHoursAdvanced = view.findViewById<TextView>(R.id.tvPeakHoursAdvanced)
        val tvDemandForecastAdvanced = view.findViewById<TextView>(R.id.tvDemandForecastAdvanced)
        val tvVolumeDistributionAdvanced = view.findViewById<TextView>(R.id.tvVolumeDistributionAdvanced)

        switchAdvanced.setOnCheckedChangeListener { _, isChecked ->
            val visibility = if (isChecked) View.VISIBLE else View.GONE
            tvPerformanceAdvanced.visibility = visibility
            tvPeakHoursAdvanced.visibility = visibility
            tvDemandForecastAdvanced.visibility = visibility
            tvVolumeDistributionAdvanced.visibility = visibility
        }

        val btnClose = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCloseHelp)
        btnClose.setOnClickListener { bottomSheetDialog.dismiss() }

        bottomSheetDialog.show()
    }

    private fun savePreference(key: String, value: Int) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putInt(key, value).apply()
    }

    private fun refreshData(days: Int, limit: Int) {
        refreshTrend(days)
        refreshBestsellers(limit)
    }

    private fun refreshTrend(days: Int) {
        currentTrendLiveData?.removeObservers(this)
        currentTrendLiveData = viewModel.getDailySales(days)
        currentTrendLiveData?.observe(this) { updateLineChart(it) }
    }

    private fun refreshBestsellers(limit: Int) {
        currentBestsellersLiveData?.removeObservers(this)
        currentBestsellersLiveData = viewModel.getBestSellers(limit)
        currentBestsellersLiveData?.observe(this) { sellers ->
            updateDistributionChart(sellers)
            updateForecastChart(sellers)
        }
    }

    private fun updateLineChart(sales: List<DailySales>?) {
        if (sales.isNullOrEmpty()) {
            lineChart.clear()
            return
        }

        val revenueEntries = ArrayList<Entry>()
        val profitEntries = ArrayList<Entry>()
        sales.forEachIndexed { index, day ->
            revenueEntries.add(Entry(index.toFloat(), day.totalAmount.toFloat()))
            profitEntries.add(Entry(index.toFloat(), day.totalProfit.toFloat()))
        }
        
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val dates = sales.map { 
            try {
                val date = inputFormat.parse(it.date)
                if (date != null) outputFormat.format(date) else it.date
            } catch (e: Exception) { it.date }
        }

        val revDataSet = LineDataSet(revenueEntries, "Revenue (₱)")
        revDataSet.color = Color.parseColor("#4CAF50")
        revDataSet.setDrawFilled(true)
        revDataSet.fillColor = Color.parseColor("#4CAF50")
        revDataSet.fillAlpha = 40
        revDataSet.lineWidth = 2.5f
        revDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        revDataSet.setDrawValues(false)

        val profitDataSet = LineDataSet(profitEntries, "Gross Profit (₱)")
        profitDataSet.color = Color.parseColor("#2196F3")
        profitDataSet.setDrawFilled(true)
        profitDataSet.fillColor = Color.parseColor("#2196F3")
        profitDataSet.fillAlpha = 40
        profitDataSet.lineWidth = 2.5f
        profitDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        profitDataSet.setDrawValues(false)

        lineChart.data = LineData(revDataSet, profitDataSet)
        lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < dates.size) dates[index] else ""
            }
        }
        lineChart.xAxis.labelCount = if (dates.size > 5) 5 else dates.size
        lineChart.xAxis.setLabelRotationAngle(-30f)
        lineChart.invalidate()
        lineChart.animateX(800)
    }

    private fun updateHourlyChart(hourlySales: List<TransactionDao.HourlySales>?) {
        if (hourlySales.isNullOrEmpty()) {
            hourlyChart.clear()
            return
        }
        val fullHours = mutableMapOf<Int, Double>()
        for (i in 0..23) fullHours[i] = 0.0
        hourlySales.forEach { fullHours[it.hour] = it.totalAmount }
        val entries = ArrayList<Entry>()
        fullHours.toSortedMap().forEach { (hour, amount) -> entries.add(Entry(hour.toFloat(), amount.toFloat())) }
        val dataSet = LineDataSet(entries, "Revenue")
        dataSet.color = Color.parseColor("#FF9800")
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#FF9800")
        dataSet.fillAlpha = 60
        dataSet.lineWidth = 2f
        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSet.setDrawValues(false)
        hourlyChart.data = LineData(dataSet)
        hourlyChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val hour = value.toInt()
                return when {
                    hour == 0 -> "12 AM"
                    hour < 12 -> "$hour AM"
                    hour == 12 -> "12 PM"
                    else -> "${hour - 12} PM"
                }
            }
        }
        hourlyChart.xAxis.labelCount = 6
        hourlyChart.invalidate()
        hourlyChart.animateX(1000)
    }

    private fun updateDistributionChart(bestSellers: List<BestSeller>?) {
        if (bestSellers.isNullOrEmpty()) {
            distributionChart.clear()
            return
        }
        val sortedSellers = bestSellers.reversed()
        val entries = ArrayList<BarEntry>()
        sortedSellers.forEachIndexed { index, seller -> entries.add(BarEntry(index.toFloat(), seller.totalQuantity.toFloat())) }
        val dataSet = BarDataSet(entries, "Sales Units")
        val colorList = ArrayList<Int>()
        for (c in ColorTemplate.COLORFUL_COLORS) colorList.add(c)
        dataSet.colors = colorList
        dataSet.valueTextSize = 11f
        dataSet.valueTextColor = Color.parseColor("#333333")
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = ""
            override fun getBarLabel(barEntry: BarEntry?): String {
                if (barEntry == null) return ""
                val index = barEntry.x.toInt()
                return if (index >= 0 && index < sortedSellers.size) {
                    "${sortedSellers[index].productName} (${String.format(Locale.getDefault(), "%.0f", barEntry.y)})"
                } else ""
            }
        }
        distributionChart.data = BarData(dataSet)
        distributionChart.data.barWidth = 0.6f
        
        // Prevent bars from becoming too thick when there are few items
        distributionChart.xAxis.axisMinimum = -0.5f
        distributionChart.xAxis.axisMaximum = if (sortedSellers.size < 6) 5.5f else (sortedSellers.size - 0.5).toFloat()
        
        val maxVal = entries.maxOfOrNull { it.y } ?: 10f
        distributionChart.axisLeft.axisMaximum = maxVal * 1.8f
        distributionChart.setVisibleXRangeMaximum(6f)
        distributionChart.invalidate()
        distributionChart.animateY(800)
    }

    private fun updateForecastChart(bestSellers: List<BestSeller>?) {
        if (bestSellers.isNullOrEmpty()) {
            forecastChart.clear()
            return
        }
        val multiplier = if (isMonthlyForecast) 4.5f else 1.2f
        val label = if (isMonthlyForecast) "Next Month" else "Next Week"
        val sortedSellers = bestSellers.reversed()
        val entries = ArrayList<BarEntry>()
        sortedSellers.forEachIndexed { index, seller -> entries.add(BarEntry(index.toFloat(), (seller.totalQuantity * multiplier).toFloat())) }
        val dataSet = BarDataSet(entries, label)
        val colorList = ArrayList<Int>()
        for (c in ColorTemplate.MATERIAL_COLORS) colorList.add(c)
        dataSet.colors = colorList
        dataSet.valueTextSize = 10f
        dataSet.valueTextColor = Color.parseColor("#333333")
        
        forecastChart.data = BarData(dataSet)
        forecastChart.data.barWidth = 0.5f
        
        // Prevent bars from becoming too wide when there are few items
        forecastChart.xAxis.axisMinimum = -0.5f
        forecastChart.xAxis.axisMaximum = if (sortedSellers.size < 6) 5.5f else (sortedSellers.size - 0.5).toFloat()
        
        forecastChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < sortedSellers.size) {
                    val name = sortedSellers[index].productName
                    if (name.length > 8) name.substring(0, 6) + ".." else name
                } else ""
            }
        }
        
        forecastChart.invalidate()
        forecastChart.animateY(1000)
    }
}
