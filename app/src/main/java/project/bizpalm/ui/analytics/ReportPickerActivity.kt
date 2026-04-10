package project.bizpalm.ui.analytics

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.MaterialDatePicker
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
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
import project.bizpalm.data.database.dao.TransactionItemDao
import project.bizpalm.data.entities.Transaction
import project.bizpalm.data.models.BestSeller
import project.bizpalm.data.models.DailySales
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ReportPickerActivity : AppCompatActivity() {

    private lateinit var viewModel: SalesAnalyticsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_picker)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        viewModel = ViewModelProvider(this)[SalesAnalyticsViewModel::class.java]

        findViewById<MaterialCardView>(R.id.cardTodayReport).setOnClickListener {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            generateReportForDate(todayDate)
        }

        findViewById<MaterialCardView>(R.id.cardCustomReport).setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Report Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            generateReportForDate(formattedDate)
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun generateReportForDate(dateString: String) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@ReportPickerActivity)
            
            val dailySummary = withContext(Dispatchers.IO) {
                db.transactionDao().getDailySalesForDateSync(dateString)
            }
            
            val bestSellers = withContext(Dispatchers.IO) {
                db.transactionDao().getBestSellersSync(10)
            }

            val receipts = withContext(Dispatchers.IO) {
                db.transactionDao().getTransactionsByDateSync(dateString)
            }

            val receiptItemsMap = withContext(Dispatchers.IO) {
                val map = mutableMapOf<Int, List<TransactionItemDao.TransactionItemWithProduct>>()
                receipts?.forEach { receipt ->
                    map[receipt.id] = db.transactionItemDao().getItemsWithProductByTransactionIdSync(receipt.id)
                }
                map
            }

            if (dailySummary == null) {
                Toast.makeText(this@ReportPickerActivity, "No transactions found for $dateString", Toast.LENGTH_SHORT).show()
                return@launch
            }

            createReportPdf(dailySummary, bestSellers, receipts, receiptItemsMap)
        }
    }

    private fun createReportPdf(
        summary: DailySales, 
        bestSellers: List<BestSeller>?, 
        receipts: List<Transaction>?,
        itemsMap: Map<Int, List<TransactionItemDao.TransactionItemWithProduct>>
    ) {
        val fileName = "BizPalm_Detailed_Report_${summary.date}.pdf"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/BizPalm_Reports")
            }
        }

        val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        if (uri == null) {
            Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val writer = PdfWriter(outputStream)
                val pdf = PdfDocument(writer)
                val document = Document(pdf)

                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

                // 1. Header & Title
                document.add(Paragraph("BIZPALM BUSINESS INTELLIGENCE")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold().setFontSize(22f).setFontColor(ColorConstants.DARK_GRAY))
                
                document.add(Paragraph("DETAILED DAILY OPERATIONS REPORT")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14f).setMarginBottom(20f))

                document.add(Paragraph("Generated: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())}")
                    .setTextAlignment(TextAlignment.RIGHT).setFontSize(10f))
                document.add(Paragraph("Report for: ${summary.date}")
                    .setTextAlignment(TextAlignment.RIGHT).setFontSize(10f).setMarginBottom(20f))

                // 2. Financial Pulse
                document.add(Paragraph("1. FINANCIAL PERFORMANCE")
                    .setBold().setFontSize(14f).setFontColor(ColorConstants.BLUE))
                
                val financeTable = Table(UnitValue.createPointArray(floatArrayOf(200f, 200f)))
                financeTable.setWidth(UnitValue.createPercentValue(100f))
                
                financeTable.addCell(Cell().add(Paragraph("METRIC").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(ColorConstants.GRAY))
                financeTable.addCell(Cell().add(Paragraph("VALUE").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(ColorConstants.GRAY))
                
                financeTable.addCell(Cell().add(Paragraph("Total Gross Revenue")))
                financeTable.addCell(Cell().add(Paragraph(String.format("₱%,.2f", summary.totalAmount)).setBold().setFontColor(ColorConstants.GREEN)))
                
                financeTable.addCell(Cell().add(Paragraph("Estimated Gross Profit")))
                financeTable.addCell(Cell().add(Paragraph(String.format("₱%,.2f", summary.totalProfit)).setBold().setFontColor(ColorConstants.BLUE)))
                
                val margin = if (summary.totalAmount > 0) (summary.totalProfit / summary.totalAmount) * 100 else 0.0
                financeTable.addCell(Cell().add(Paragraph("Average Profit Margin")))
                financeTable.addCell(Cell().add(Paragraph(String.format("%.1f%%", margin)).setBold()))
                
                financeTable.addCell(Cell().add(Paragraph("Total Transaction Count")))
                financeTable.addCell(Cell().add(Paragraph(receipts?.size?.toString() ?: "0").setBold()))

                document.add(financeTable)

                // 3. Receipt Journal with Item Breakdown
                if (!receipts.isNullOrEmpty()) {
                    document.add(Paragraph("\n2. TRANSACTION JOURNAL & ITEM BREAKDOWN")
                        .setBold().setFontSize(14f).setFontColor(ColorConstants.BLUE).setMarginTop(20f))
                    
                    receipts.forEach { receipt ->
                        val typeStr = when {
                            receipt.isLoaned -> "UTANG"
                            receipt.isOnlinePayment -> "GCash"
                            else -> "CASH"
                        }

                        // Receipt Header
                        document.add(Paragraph("Receipt #TX-${String.format("%05d", receipt.id)} | Time: ${timeFormat.format(Date(receipt.timestamp))} | Mode: $typeStr")
                            .setBold().setFontSize(11f).setMarginTop(10f).setFontColor(ColorConstants.DARK_GRAY))
                        
                        // Item Table for this receipt
                        val itemTable = Table(UnitValue.createPointArray(floatArrayOf(200f, 60f, 140f)))
                        itemTable.setWidth(UnitValue.createPercentValue(100f))
                        
                        itemTable.addCell(Cell().add(Paragraph("Item Name").setFontSize(9f).setBold()))
                        itemTable.addCell(Cell().add(Paragraph("Qty").setFontSize(9f).setBold()))
                        itemTable.addCell(Cell().add(Paragraph("Subtotal").setFontSize(9f).setBold()))
                        
                        val items = itemsMap[receipt.id]
                        items?.forEach { wrap ->
                            itemTable.addCell(Cell().add(Paragraph(wrap.productName).setFontSize(9f)))
                            itemTable.addCell(Cell().add(Paragraph(wrap.item.quantitySold.toString()).setFontSize(9f).setTextAlignment(TextAlignment.CENTER)))
                            itemTable.addCell(Cell().add(Paragraph(String.format("₱%,.2f", wrap.item.amountDue)).setFontSize(9f)))
                        }
                        
                        // Total for this receipt
                        itemTable.addCell(Cell(1, 2).add(Paragraph("Receipt Total:").setBold().setFontSize(9f).setTextAlignment(TextAlignment.RIGHT)))
                        itemTable.addCell(Cell().add(Paragraph(String.format("₱%,.2f", receipt.totalAmount)).setBold().setFontSize(9f).setFontColor(ColorConstants.GREEN)))
                        
                        document.add(itemTable)
                    }
                }

                // 4. Inventory Insights
                if (!bestSellers.isNullOrEmpty()) {
                    document.add(Paragraph("\n3. TOP PERFORMING PRODUCTS")
                        .setBold().setFontSize(14f).setFontColor(ColorConstants.BLUE).setMarginTop(20f))
                    
                    val productsTable = Table(UnitValue.createPointArray(floatArrayOf(250f, 150f)))
                    productsTable.setWidth(UnitValue.createPercentValue(100f))
                    
                    productsTable.addCell(Cell().add(Paragraph("Product Item").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(ColorConstants.GRAY))
                    productsTable.addCell(Cell().add(Paragraph("Units Dispatched").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(ColorConstants.GRAY))
                    
                    bestSellers.take(5).forEach {
                        productsTable.addCell(Cell().add(Paragraph(it.productName)))
                        productsTable.addCell(Cell().add(Paragraph(it.totalQuantity.toString()).setTextAlignment(TextAlignment.CENTER)))
                    }
                    document.add(productsTable)
                }

                // 5. Strategic Recommendations
                document.add(Paragraph("\n4. STRATEGIC AI RECOMMENDATIONS")
                    .setBold().setFontSize(14f).setFontColor(ColorConstants.BLUE).setMarginTop(20f))
                
                if (!bestSellers.isNullOrEmpty()) {
                    val topProduct = bestSellers[0].productName
                    document.add(Paragraph("• STOCK OPTIMIZATION: \"$topProduct\" is your top mover. Suggest increasing safety stock by 15% to meet demand.")
                        .setFontSize(11f).setItalic())
                }
                
                if (margin < 20) {
                    document.add(Paragraph("• PRICING STRATEGY: Margins are tight (<20%). Consider high-margin product bundles.")
                        .setFontSize(11f).setItalic())
                }
                
                document.add(Paragraph("• EFFICIENCY: Based on today's transaction spread, ensure your fastest moving items are placed near the checkout area for quicker processing.")
                    .setFontSize(11f).setItalic())

                // 6. Footer
                document.add(Paragraph("\n\n--- End of Detailed Report ---")
                    .setTextAlignment(TextAlignment.CENTER).setFontSize(9f).setFontColor(ColorConstants.LIGHT_GRAY))
                
                document.close()
                Toast.makeText(this, "Detailed Report with Items Generated", Toast.LENGTH_LONG).show()
                shareFile(uri)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error generating detailed report", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareFile(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Share Detailed Report"))
    }
}
