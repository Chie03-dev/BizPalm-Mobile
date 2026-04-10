package project.bizpalm.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class Notification {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String type; // e.g., "LOW_STOCK", "SALE_ALERT"
    public String title;
    public String message;
    public long timestamp;
    public boolean isRead;

    // Optional: for low stock alerts
    public long relatedProductId;

    public Notification(String type, String title, String message, long timestamp, boolean isRead, long relatedProductId) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.relatedProductId = relatedProductId;
    }
}
