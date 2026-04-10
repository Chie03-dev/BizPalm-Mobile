package project.bizpalm.ui.analytics

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import project.bizpalm.R
import project.bizpalm.data.entities.Product
import project.bizpalm.ui.inventory.AddProductActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        @Suppress("DEPRECATION")
        val product = intent.getSerializableExtra("PRODUCT") as? Product
        if (product == null) {
            finish()
            return
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.title = product.productName

        val ivProductImage = findViewById<ImageView>(R.id.ivProductImage)
        val tvProductName = findViewById<TextView>(R.id.tvProductName)
        val tvProductBarcode = findViewById<TextView>(R.id.tvProductBarcode)
        val tvProductPrice = findViewById<TextView>(R.id.tvProductPrice)
        val tvProductCost = findViewById<TextView>(R.id.tvProductCost)
        val tvProductMargin = findViewById<TextView>(R.id.tvProductMargin)
        val tvProductStock = findViewById<TextView>(R.id.tvProductStock)
        val tvProductExpiry = findViewById<TextView>(R.id.tvProductExpiry)
        val btnEditProduct = findViewById<MaterialButton>(R.id.btnEditProduct)

        tvProductName.text = product.productName
        tvProductBarcode.text = String.format(Locale.getDefault(), "Barcode: %s", product.barcode)
        tvProductPrice.text = String.format(Locale.getDefault(), "₱%.2f", product.price)
        tvProductCost.text = String.format(Locale.getDefault(), "₱%.2f", product.unitCost)
        tvProductMargin.text = String.format(Locale.getDefault(), "%.1f%%", product.getProfitMarginPercentage())
        tvProductStock.text = product.quantity.toString()

        if (product.expiryDate != null && product.expiryDate > 0) {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            tvProductExpiry.text = String.format(Locale.getDefault(), "Expiry: %s", sdf.format(Date(product.expiryDate)))
            tvProductExpiry.visibility = View.VISIBLE
        }

        product.imageUri?.let { uri ->
            try {
                val bitmap = BitmapFactory.decodeFile(uri)
                if (bitmap != null) {
                    ivProductImage.setImageBitmap(bitmap)
                    ivProductImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    ivProductImage.imageTintList = null
                }
            } catch (e: Exception) {
                // Keep default icon
            }
        }

        btnEditProduct.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            intent.putExtra("PRODUCT_ID", product.id)
            startActivity(intent)
            finish()
        }
    }
}
