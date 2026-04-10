package project.bizpalm.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "transaction_items",
        foreignKeys = @ForeignKey(entity = Transaction.class,
                parentColumns = "id",
                childColumns = "transaction_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("transaction_id")})
public class TransactionItem {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "transaction_id")
    public int transactionId;

    @ColumnInfo(name = "amount_due")
    public double amountDue; // This is (retailPrice * quantitySold)

    @ColumnInfo(name = "unit_cost")
    public double unitCost; // Cost at time of sale

    @ColumnInfo(name = "quantity_sold")
    public int quantitySold;

    @ColumnInfo(name = "product_id")
    public int productId;

    public TransactionItem(int transactionId, double amountDue, double unitCost, int quantitySold, int productId) {
        this.transactionId = transactionId;
        this.amountDue = amountDue;
        this.unitCost = unitCost;
        this.quantitySold = quantitySold;
        this.productId = productId;
    }

    public double getProfit() {
        return amountDue - (unitCost * quantitySold);
    }
}
