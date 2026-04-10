package project.bizpalm.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "total_amount")
    public double totalAmount;

    @ColumnInfo(name = "cash_received")
    public double cashReceived;

    @ColumnInfo(name = "change_amount")
    public double changeAmount;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "is_online_payment")
    public boolean isOnlinePayment;

    @ColumnInfo(name = "is_loaned")
    public boolean isLoaned;

    @ColumnInfo(name = "loaner_name")
    public String loanerName;

    @ColumnInfo(name = "loaner_signature")
    public String loanerSignature; // Base64 string of the signature bitmap

    public Transaction(double totalAmount, double cashReceived, double changeAmount, long timestamp, boolean isOnlinePayment) {
        this.totalAmount = totalAmount;
        this.cashReceived = cashReceived;
        this.changeAmount = changeAmount;
        this.timestamp = timestamp;
        this.isOnlinePayment = isOnlinePayment;
        this.isLoaned = false;
    }
}
