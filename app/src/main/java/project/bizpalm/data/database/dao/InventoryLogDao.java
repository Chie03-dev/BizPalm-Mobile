package project.bizpalm.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import project.bizpalm.data.entities.InventoryLog;

@Dao
public interface InventoryLogDao {
    @Insert
    void insert(InventoryLog log);

    @Query("SELECT * FROM inventory_logs ORDER BY logDate DESC")
    LiveData<List<InventoryLog>> getAllLogs();

    @Query("SELECT * FROM inventory_logs WHERE productId = :productId ORDER BY logDate DESC")
    LiveData<List<InventoryLog>> getLogsForProduct(long productId);
}
