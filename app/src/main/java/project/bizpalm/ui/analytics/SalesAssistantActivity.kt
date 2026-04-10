package project.bizpalm.ui.analytics

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import io.noties.markwon.Markwon
import project.bizpalm.R
import project.bizpalm.data.entities.Product
import project.bizpalm.ui.inventory.InventoryActivity
import project.bizpalm.ui.products.ProductAdapter
import project.bizpalm.ui.transactions.TransactionHistoryActivity
import java.util.Locale

class SalesAssistantActivity : AppCompatActivity() {

    private lateinit var viewModel: SalesAnalyticsViewModel
    private lateinit var tvTotalProfit: TextView
    private lateinit var tvRevenue: TextView
    private lateinit var tvAvgMargin: TextView
    private lateinit var tvTotalOrders: TextView
    private lateinit var tvAvgSales: TextView
    private lateinit var tvPredictedSales: TextView
    private lateinit var tvSmartAdvice: TextView
    private lateinit var tvEmptyRestock: TextView
    
    private lateinit var cardSalesSummary: MaterialCardView
    private lateinit var cardAdvice: MaterialCardView
    private lateinit var cardForecasting: MaterialCardView
    private lateinit var cardRestockRecommendations: MaterialCardView
    private lateinit var cardInventoryAction: MaterialCardView
    private lateinit var cardHistoryAction: MaterialCardView
    private lateinit var btnDetailedAnalytics: android.widget.Button
    private lateinit var ivHelp: android.widget.ImageView
    
    private lateinit var rvRestock: RecyclerView
    private lateinit var restockAdapter: RestockAdapter
    
    private lateinit var markwon: Markwon
    private lateinit var attentionalAdapter: AttentionalItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_assistant)

        markwon = Markwon.create(this)
        initViews()
        setupViewModel()
        setupClickListeners()
    }

    private fun initViews() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        tvTotalProfit = findViewById(R.id.tvTotalProfit)
        tvRevenue = findViewById(R.id.tvRevenue)
        tvAvgMargin = findViewById(R.id.tvAvgMargin)
        tvTotalOrders = findViewById(R.id.tvTotalOrders)
        tvAvgSales = findViewById(R.id.tvAvgSales)
        tvPredictedSales = findViewById(R.id.tvPredictedSales)
        tvSmartAdvice = findViewById(R.id.tvSmartAdvice)
        tvEmptyRestock = findViewById(R.id.tvEmptyRestock)
        
        cardSalesSummary = findViewById(R.id.cardSalesSummary)
        cardAdvice = findViewById(R.id.cardAdvice)
        cardForecasting = findViewById(R.id.cardForecasting)
        cardRestockRecommendations = findViewById(R.id.cardRestockRecommendations)
        cardInventoryAction = findViewById(R.id.cardInventoryAction)
        cardHistoryAction = findViewById(R.id.cardHistoryAction)
        btnDetailedAnalytics = findViewById(R.id.btnDetailedAnalytics)
        ivHelp = findViewById(R.id.ivHelp)

        rvRestock = findViewById(R.id.rvRestockRecommendations)
        rvRestock.layoutManager = LinearLayoutManager(this)
        restockAdapter = RestockAdapter()
        rvRestock.adapter = restockAdapter
        
        attentionalAdapter = AttentionalItemAdapter()
        
        restockAdapter.setOnItemClickListener { recommendation ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("PRODUCT", recommendation.product)
            startActivity(intent)
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[SalesAnalyticsViewModel::class.java]

        viewModel.analyticsResult.observe(this) { result ->
            result?.let {
                tvAvgSales.text = String.format(Locale.getDefault(), "₱%.2f", it.averageSales)
                tvPredictedSales.text = String.format(Locale.getDefault(), "₱%.2f", it.predictedNextSales)
                
                markwon.setMarkdown(tvSmartAdvice, it.advice)
                
                tvRevenue.text = String.format(Locale.getDefault(), "Revenue: ₱%.2f", it.totalRevenue)
                tvTotalProfit.text = String.format(Locale.getDefault(), "₱%.2f", it.totalProfit)
                
                if (it.totalRevenue > 0) {
                    val margin = (it.totalProfit / it.totalRevenue) * 100
                    tvAvgMargin.text = String.format(Locale.getDefault(), "%.1f%%", margin)
                } else {
                    tvAvgMargin.text = "0%"
                }
                
                // Set items to the specialized attentional adapter
                attentionalAdapter.setItems(it.relatedItems)
                
                val hasItems = it.relatedItems.isNotEmpty()
                cardAdvice.isClickable = hasItems
                cardAdvice.isFocusable = hasItems
                cardAdvice.foreground = if (hasItems) {
                    val attrs = intArrayOf(android.R.attr.selectableItemBackground)
                    val typedArray = obtainStyledAttributes(attrs)
                    val drawable = typedArray.getDrawable(0)
                    typedArray.recycle()
                    drawable
                } else null
            }
        }

        viewModel.getTransactionCount().observe(this) { count ->
            tvTotalOrders.text = String.format(Locale.getDefault(), "Orders: %d", count ?: 0)
        }

        viewModel.restockRecommendations.observe(this) { recommendations ->
            if (recommendations.isNullOrEmpty()) {
                rvRestock.visibility = View.GONE
                tvEmptyRestock.visibility = View.VISIBLE
            } else {
                rvRestock.visibility = View.VISIBLE
                tvEmptyRestock.visibility = View.GONE
                restockAdapter.updateData(recommendations)
            }
        }
    }

    private fun setupClickListeners() {
        ivHelp.setOnClickListener {
            showHelpDialog()
        }

        cardAdvice.setOnClickListener {
            showAttentionalItemsDialog()
        }

        val historyIntent = Intent(this, TransactionHistoryActivity::class.java)
        cardSalesSummary.setOnClickListener { startActivity(historyIntent) }
        cardHistoryAction.setOnClickListener { startActivity(historyIntent) }

        val analyticsIntent = Intent(this, AnalyticsActivity::class.java)
        cardForecasting.setOnClickListener { startActivity(analyticsIntent) }
        btnDetailedAnalytics.setOnClickListener { startActivity(analyticsIntent) }

        cardInventoryAction.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }
    }

    private fun showAttentionalItemsDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_related_products, null)
        bottomSheetDialog.setContentView(view)

        val rv = view.findViewById<RecyclerView>(R.id.rvRelatedProducts)
        val btnClose = view.findViewById<MaterialButton>(R.id.btnCloseDialog)
        val tvTitle = view.findViewById<TextView>(R.id.tvDialogTitle)

        tvTitle.text = "Business Attention List"

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = attentionalAdapter
        
        attentionalAdapter.setOnItemClickListener { attentionalItem ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("PRODUCT", attentionalItem.product)
            startActivity(intent)
            bottomSheetDialog.dismiss()
        }

        btnClose.setOnClickListener { bottomSheetDialog.dismiss() }
        bottomSheetDialog.show()
    }

    private fun showHelpDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_analytics_help, null)
        bottomSheetDialog.setContentView(view)

        val switchAdvanced = view.findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.switchAdvanced)
        val tvFinancialPulseAdvanced = view.findViewById<TextView>(R.id.tvFinancialPulseAdvanced)
        val tvAiConsultantAdvanced = view.findViewById<TextView>(R.id.tvAiConsultantAdvanced)
        val tvStrategicInsightsAdvanced = view.findViewById<TextView>(R.id.tvStrategicInsightsAdvanced)
        val tvRestockAdvanced = view.findViewById<TextView>(R.id.tvRestockAdvanced)

        switchAdvanced.setOnCheckedChangeListener { _, isChecked ->
            val visibility = if (isChecked) View.VISIBLE else View.GONE
            tvFinancialPulseAdvanced.visibility = visibility
            tvAiConsultantAdvanced.visibility = visibility
            tvStrategicInsightsAdvanced.visibility = visibility
            tvRestockAdvanced.visibility = visibility
        }

        val btnClose = view.findViewById<MaterialButton>(R.id.btnCloseHelp)
        btnClose.setOnClickListener { bottomSheetDialog.dismiss() }

        bottomSheetDialog.show()
    }
}
