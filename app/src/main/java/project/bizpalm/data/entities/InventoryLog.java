package project.bizpalm.data.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventory_logs")
public class InventoryLog {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public long productId;
    public String actionType;
    public int quantityChange;
    public int previousQuantity;
    public int newQuantity;
    public long logDate;

    public InventoryLog() {
    }

    @Ignore
    public InventoryLog(long productId, String actionType, int quantityChange, int previousQuantity, int newQuantity, long logDate) {
        this.productId = productId;
        this.actionType = actionType;
        this.quantityChange = quantityChange;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.logDate = logDate;
    }
}
