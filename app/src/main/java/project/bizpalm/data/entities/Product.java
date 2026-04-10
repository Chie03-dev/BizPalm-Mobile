package project.bizpalm.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "products")
public class Product implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "barcode")
    public String barcode;

    @ColumnInfo(name = "product_name")
    public String productName;

    @ColumnInfo(name = "price")
    public double price; // This is the Retail/Unit Price

    @ColumnInfo(name = "unit_cost")
    public double unitCost; // This is the Cost Price (COGS)

    @ColumnInfo(name = "quantity")
    public int quantity;

    @ColumnInfo(name = "is_popular")
    public boolean isPopular;

    @ColumnInfo(name = "expiry_date")
    public Long expiryDate;

    @ColumnInfo(name = "image_uri")
    public String imageUri;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "purpose")
    public String purpose;

    @ColumnInfo(name = "type")
    public String type;

    @Ignore
    public String statusLabel; // Temporary field for display insights (e.g., "Low Stock")

    public Product(String barcode, String productName, double price, double unitCost, int quantity, boolean isPopular, Long expiryDate) {
        this.barcode = barcode;
        this.productName = productName;
        this.price = price;
        this.unitCost = unitCost;
        this.quantity = quantity;
        this.isPopular = isPopular;
        this.expiryDate = expiryDate;
    }

    @Ignore
    public double getMarkup() {
        return price - unitCost;
    }

    @Ignore
    public double getProfitMarginPercentage() {
        if (price == 0) return 0;
        return ((price - unitCost) / price) * 100;
    }
}
