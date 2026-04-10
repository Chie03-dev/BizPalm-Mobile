package project.bizpalm.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.math3.stat.regression.SimpleRegression
import project.bizpalm.data.database.AppDatabase
import project.bizpalm.data.entities.Product
import project.bizpalm.data.models.BestSeller
import project.bizpalm.data.models.DailySales
import project.bizpalm.data.models.RestockRecommendation
import project.bizpalm.data.database.dao.TransactionDao
import project.bizpalm.data.database.dao.TransactionItemDao
import project.bizpalm.data.models.AttentionalItem
import java.util.*

class SalesAnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TREND_THRESHOLD = 0.5
        private const val SPIKE_THRESHOLD = 1.5
        private const val MIN_SAMPLES_FOR_PREDICTION = 3
        private const val LOW_STOCK_THRESHOLD = 5 
        private const val DEFAULT_RESTOCK_WINDOW_DAYS = 14
        private const val MIN_BASKETS_FOR_MBA = 5
        private const val MBA_MIN_SUPPORT_COUNT = 2
    }

    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()
    private val productDao = db.productDao()
    private val transactionItemDao = db.transactionItemDao()

    private var lastDailySales: List<DailySales>? = null
    private var lastProducts: List<Product>? = null
    private var lastSlowSellers: List<BestSeller>? = null

    private val _analyticsResult = MediatorLiveData<AnalyticsResult>()
    val analyticsResult: LiveData<AnalyticsResult> = _analyticsResult

    private val _restockRecommendations = MutableLiveData<List<RestockRecommendation>>()
    val restockRecommendations: LiveData<List<RestockRecommendation>> = _restockRecommendations

    init {
        setupMediators()
    }

    private fun setupMediators() {
        _analyticsResult.addSource(transactionDao.getDailySales(30)) { sales ->
            lastDailySales = sales
            runAnalytics()
            calculateRestockRecommendations()
        }
        _analyticsResult.addSource(productDao.allProducts) { products ->
            lastProducts = products
            runAnalytics()
            calculateRestockRecommendations()
        }
        _analyticsResult.addSource(transactionDao.slowSellers) { slowSellers ->
            lastSlowSellers = slowSellers
            runAnalytics()
        }
    }

    private fun runAnalytics() {
        val salesData = lastDailySales ?: return
        val productData = lastProducts
        val slowSellers = lastSlowSellers

        if (salesData.isEmpty()) {
            _analyticsResult.postValue(AnalyticsResult("### **Welcome to BizPalm AI**\n\nAnalyzing your business... Add more transactions to see smart tips!", 0.0, 0.0, 0.0, 0.0, 0.0, emptyList()))
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            val stats = DescriptiveStatistics()
            salesData.forEach { day -> stats.addValue(day.totalAmount) }
            val avgSales = stats.mean
            val stdDev = stats.standardDeviation
            val totalRev = salesData.sumOf { it.totalAmount }
            val totalProfit = salesData.sumOf { it.totalProfit }
            val lastDaySales = if (salesData.isNotEmpty()) salesData.last().totalAmount else 0.0
            val growthPercent = if (avgSales > 0) ((lastDaySales - avgSales) / avgSales) * 100 else 0.0
            val currentMargin = if (totalRev > 0) (totalProfit / totalRev) * 100 else 0.0

            var prediction = avgSales
            if (salesData.size >= MIN_SAMPLES_FOR_PREDICTION) {
                try {
                    val regression = SimpleRegression()
                    salesData.forEachIndexed { index, day ->
                        regression.addData(index.toDouble(), day.totalAmount)
                    }
                    prediction = regression.predict(salesData.size.toDouble())
                } catch (e: Exception) {
                    prediction = avgSales
                }
            }

            val advice = StringBuilder()
            advice.append("### **Business Health Summary**\n\n")

            val diff = prediction - avgSales
            when {
                diff > (stdDev * SPIKE_THRESHOLD) -> advice.append("🚀 **Spike Detected:** Your demand is surging! Consider increasing your stock levels immediately to prevent lost sales.\n\n")
                diff > (stdDev * TREND_THRESHOLD) -> advice.append("📈 **Growth Trend:** You are seeing steady upward momentum. This is a great time to introduce new related products.\n\n")
                diff < -(stdDev * SPIKE_THRESHOLD) -> advice.append("📉 **Drop Alert:** Demand is cooling rapidly. Review your pricing or check for new competitors.\n\n")
                else -> advice.append("✅ **Stability:** Your operations are running smoothly with consistent volume.\n\n")
            }

            advice.append("---\n### **AI Recommendations**\n")
            val relatedItems = mutableListOf<AttentionalItem>()
            
            if (currentMargin < 20) {
                advice.append("⚠️ **Margin Alert:** Your average margin is lower than 20%. Try bundling low-cost items with high-price ones to boost profitability.\n\n")
            }

            findBundleOpportunities(productData)?.let { bundle ->
                advice.append("🤝 **Growth Hack:** Customers often pair **${bundle.first.productName}** with **${bundle.second.productName}**. Create a 'Perfect Match' bundle for a 5% discount!\n\n")
                relatedItems.add(AttentionalItem(bundle.first, "Bundle Opportunity", "Often purchased with ${bundle.second.productName}. Identifying these links helps you design effective promotions.", "Create a 'Pair Discount'"))
                relatedItems.add(AttentionalItem(bundle.second, "Bundle Opportunity", "Often purchased with ${bundle.first.productName}. This connection represents a natural customer preference.", "Bundle with ${bundle.first.productName}"))
            }

            productData?.let { products ->
                val criticalLow = products.filter { it.quantity <= 2 }
                if (criticalLow.isNotEmpty()) {
                    advice.append("🚨 **Restock Required:** ${criticalLow.size} items are almost out of stock. Immediate action is recommended to avoid service interruption.\n\n")
                    criticalLow.forEach { 
                        relatedItems.add(AttentionalItem(it, "Critical Stock", "Only ${it.quantity} left. Running out of stock means losing customers to competitors who have it.", "Restock immediately"))
                    }
                }

                val stars = products.filter { it.getProfitMarginPercentage() > 35 && it.quantity > 5 }
                if (stars.isNotEmpty()) {
                    val topStar = stars.maxByOrNull { it.getProfitMarginPercentage() }
                    topStar?.let {
                        advice.append("💎 **Profit Driver:** '${it.productName}' has excellent margins. Consider promoting this item to maximize your returns.\n\n")
                        relatedItems.add(AttentionalItem(it, "Profit Driver", "Excellent margin of ${String.format("%.1f%%", it.getProfitMarginPercentage())}. This item earns you the most per sale.", "Display prominently"))
                    }
                }
            }

            if (!slowSellers.isNullOrEmpty()) {
                advice.append("💡 **Efficiency Tip:** Aging inventory detected. Consider a flash sale for slow-moving items to free up cash flow.\n")
                val slowIds = slowSellers.map { it.productId }
                productData?.filter { it.id in slowIds }?.forEach { 
                    relatedItems.add(AttentionalItem(it, "Slow Mover", "This item has had very low sales recently. It is tying up your cash and shelf space.", "Discount to clear stock"))
                }
            }

            _analyticsResult.postValue(AnalyticsResult(
                advice.toString(), 
                prediction, 
                avgSales, 
                growthPercent, 
                totalRev, 
                totalProfit,
                relatedItems.distinctBy { it.product.id }
            ))
        }
    }

    private fun findBundleOpportunities(products: List<Product>?): Pair<Product, Product>? {
        if (products == null) return null
        try {
            val allItems = transactionItemDao.allTransactionProductIds
            val basketsMap = mutableMapOf<Int, MutableSet<Int>>()
            for (item in allItems) {
                basketsMap.getOrPut(item.transaction_id) { mutableSetOf() }.add(item.product_id)
            }
            if (basketsMap.size < MIN_BASKETS_FOR_MBA) return null
            val pairCounts = mutableMapOf<Pair<Int, Int>, Int>()
            for (basket in basketsMap.values) {
                val items = basket.toList().sorted()
                for (i in items.indices) {
                    for (j in i + 1 until items.size) {
                        val pair = Pair(items[i], items[j])
                        pairCounts[pair] = (pairCounts[pair] ?: 0) + 1
                    }
                }
            }
            val bestPairEntry = pairCounts.entries.filter { it.value >= MBA_MIN_SUPPORT_COUNT }.maxByOrNull { it.value }
            if (bestPairEntry != null) {
                val p1 = products.find { it.id == bestPairEntry.key.first }
                val p2 = products.find { it.id == bestPairEntry.key.second }
                if (p1 != null && p2 != null) return Pair(p1, p2)
            }
        } catch (ignored: Exception) {}
        return null
    }

    private fun calculateRestockRecommendations() {
        val products = lastProducts ?: return
        
        viewModelScope.launch(Dispatchers.Default) {
            val startTime = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            val salesVelocity = transactionDao.getSalesVelocity(startTime)
            val velocityMap = salesVelocity.associateBy({ it.product_id }, { it.totalQty / 30.0 })

            val recommendations = mutableListOf<RestockRecommendation>()

            for (product in products) {
                val dailyVelocity = velocityMap[product.id] ?: 0.0
                if (dailyVelocity > 0) {
                    val daysRemaining = (product.quantity / dailyVelocity).toInt()
                    if (daysRemaining <= DEFAULT_RESTOCK_WINDOW_DAYS) {
                        val targetStock = (dailyVelocity * DEFAULT_RESTOCK_WINDOW_DAYS * 1.5).toInt() 
                        val suggestedQty = Math.max(5, targetStock - product.quantity)
                        recommendations.add(RestockRecommendation(product, daysRemaining, suggestedQty))
                    }
                } else if (product.quantity <= LOW_STOCK_THRESHOLD) {
                    recommendations.add(RestockRecommendation(product, 0, 10))
                }
            }
            recommendations.sortBy { it.daysLeft }
            _restockRecommendations.postValue(recommendations)
        }
    }

    fun getProjectedSales(durationDays: Int, callback: (Double) -> Unit) {
        val salesData = lastDailySales
        if (salesData == null || salesData.size < MIN_SAMPLES_FOR_PREDICTION) {
            callback(0.0)
            return
        }
        viewModelScope.launch(Dispatchers.Default) {
            val regression = SimpleRegression()
            salesData.forEachIndexed { index, day ->
                regression.addData(index.toDouble(), day.totalAmount)
            }
            var totalProjected = 0.0
            val startIndex = salesData.size
            for (i in 0 until durationDays) {
                val dayPrediction = regression.predict((startIndex + i).toDouble())
                totalProjected += Math.max(0.0, dayPrediction)
            }
            callback(totalProjected)
        }
    }

    fun getTotalRevenue(): LiveData<Double> = transactionDao.totalRevenue
    fun getTotalProfit(): LiveData<Double> = transactionDao.totalProfit
    fun getTransactionCount(): LiveData<Int> = transactionDao.transactionCount
    fun getBestSellers(limit: Int): LiveData<List<BestSeller>> = transactionDao.getBestSellers(limit)
    
    fun getBestSellersByMonth(month: Int, year: Int, limit: Int): LiveData<List<BestSeller>> {
        val m = String.format(Locale.getDefault(), "%02d", month)
        val y = year.toString()
        return transactionDao.getBestSellersByMonth(m, y, limit)
    }

    fun getDailySales(days: Int): LiveData<List<DailySales>> = transactionDao.getDailySales(days)
    fun getHourlySales(): LiveData<List<TransactionDao.HourlySales>> = transactionDao.hourlySales
    fun getCategoryRevenue(): LiveData<List<TransactionDao.CategoryRevenue>> = transactionDao.categoryRevenue

    data class AnalyticsResult(
        val advice: String,
        val predictedNextSales: Double,
        val averageSales: Double,
        val growthPercent: Double,
        val totalRevenue: Double,
        val totalProfit: Double,
        val relatedItems: List<AttentionalItem>
    )
}
