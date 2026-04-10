package project.bizpalm.ui.analytics

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.ViewPortHandler
import com.google.android.material.button.MaterialButton
import project.bizpalm.R
import project.bizpalm.data.models.BestSeller
import java.text.SimpleDateFormat
import java.util.*

class MonthlyPopularityActivity : AppCompatActivity() {

    private lateinit var viewModel: SalesAnalyticsViewModel
    private lateinit var monthlyChart: HorizontalBarChart
    private lateinit var tvSelectedMonth: TextView
    private lateinit var btnPickMonth: MaterialButton
    
    private var selectedCalendar = Calendar.getInstance()
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_popularity)

        viewModel = ViewModelProvider(this)[SalesAnalyticsViewModel::class.java]
        
        initViews()
        setupChart()
        loadMonthlyData()
    }

    private fun initViews() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        monthlyChart = findViewById(R.id.monthlyChart)
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth)
        btnPickMonth = findViewById(R.id.btnPickMonth)

        updateMonthLabel()

        btnPickMonth.setOnClickListener {
            showMonthYearPicker()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupChart() {
        monthlyChart.description.isEnabled = false
        monthlyChart.setDrawBarShadow(false)
        monthlyChart.setDrawValueAboveBar(true) 
        
        monthlyChart.setTouchEnabled(true)
        monthlyChart.isDragEnabled = true
        monthlyChart.setScaleEnabled(false)
        monthlyChart.setPinchZoom(false)
        monthlyChart.setDoubleTapToZoomEnabled(false)
        
        monthlyChart.setExtraOffsets(0f, 0f, 25f, 0f)

        monthlyChart.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.parent.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.parent.requestDisallowInterceptTouchEvent(false)
            }
            v.onTouchEvent(event)
            true
        }

        monthlyChart.xAxis.isEnabled = false 
        monthlyChart.axisLeft.setDrawGridLines(false)
        monthlyChart.axisLeft.axisMinimum = 0f
        monthlyChart.axisLeft.setDrawLabels(false)
        monthlyChart.axisRight.isEnabled = false
        monthlyChart.legend.isEnabled = false
    }

    private fun showMonthYearPicker() {
        val dialog = DatePickerDialog(this, { _, year, month, _ ->
            selectedCalendar.set(Calendar.YEAR, year)
            selectedCalendar.set(Calendar.MONTH, month)
            updateMonthLabel()
            loadMonthlyData()
        }, selectedCalendar.get(Calendar.YEAR), selectedCalendar.get(Calendar.MONTH), selectedCalendar.get(Calendar.DAY_OF_MONTH))
        
        dialog.setTitle("Select Month and Year")
        dialog.show()
    }

    private fun updateMonthLabel() {
        tvSelectedMonth.text = monthYearFormat.format(selectedCalendar.time)
    }

    private fun loadMonthlyData() {
        val month = selectedCalendar.get(Calendar.MONTH) + 1 
        val year = selectedCalendar.get(Calendar.YEAR)
        
        viewModel.getBestSellersByMonth(month, year, 25).observe(this) { sellers ->
            updateChart(sellers)
        }
    }

    private fun updateChart(bestSellers: List<BestSeller>?) {
        if (bestSellers.isNullOrEmpty()) {
            monthlyChart.clear()
            monthlyChart.setNoDataText("No data available for this month.")
            monthlyChart.invalidate()
            return
        }

        // Auto-sort: Highest demand at the top.
        val sortedSellers = bestSellers.reversed()

        val entries = sortedSellers.mapIndexed { index, seller ->
            BarEntry(index.toFloat(), seller.totalQuantity.toFloat())
        }
        
        val dataSet = BarDataSet(entries, "Popularity")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.valueTextSize = 11f
        dataSet.valueTextColor = Color.parseColor("#333333")
        
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return ""
            }

            override fun getBarLabel(barEntry: BarEntry?): String {
                if (barEntry == null) return ""
                val index = barEntry.x.toInt()
                return if (index >= 0 && index < sortedSellers.size) {
                    "${sortedSellers[index].productName} (${String.format(Locale.getDefault(), "%.0f", barEntry.y)})"
                } else {
                    ""
                }
            }
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.7f 
        monthlyChart.data = barData
        
        val maxVal = entries.maxOfOrNull { it.y } ?: 10f
        monthlyChart.axisLeft.axisMaximum = maxVal * 1.8f 
        
        val visibleItems = 6f
        if (sortedSellers.size > visibleItems) {
            monthlyChart.setVisibleXRangeMaximum(visibleItems)
            monthlyChart.moveViewToX(sortedSellers.size.toFloat())
        } else {
            monthlyChart.setVisibleXRangeMaximum(sortedSellers.size.toFloat())
        }
        
        monthlyChart.invalidate()
        monthlyChart.animateY(800)
    }
}
