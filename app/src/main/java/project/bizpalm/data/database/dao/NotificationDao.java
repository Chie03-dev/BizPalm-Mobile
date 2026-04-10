package project.bizpalm.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

import project.bizpalm.data.entities.Notification;

@Dao
public interface NotificationDao {
    @Insert
    void insert(Notification notification);

    @Update
    void update(Notification notification);

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    LiveData<List<Notification>> getAllNotifications();

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    LiveData<Integer> getUnreadNotificationCount();

    @Query("UPDATE notifications SET isRead = 1 WHERE isRead = 0")
    void markAllAsRead();

    @Query("SELECT COUNT(*) > 0 FROM notifications WHERE type = 'LOW_STOCK' AND relatedProductId = :productId")
    boolean hasLowStockNotification(int productId);

    @Query("DELETE FROM notifications WHERE id IN (:ids)")
    void deleteByIds(List<Integer> ids);
}
