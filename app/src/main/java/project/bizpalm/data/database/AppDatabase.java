package project.bizpalm.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import project.bizpalm.data.database.dao.InventoryLogDao;
import project.bizpalm.data.database.dao.NotificationDao;
import project.bizpalm.data.database.dao.ProductDao;
import project.bizpalm.data.database.dao.TransactionDao;
import project.bizpalm.data.database.dao.TransactionItemDao;
import project.bizpalm.data.database.dao.UserDao;
import project.bizpalm.data.entities.InventoryLog;
import project.bizpalm.data.entities.Notification;
import project.bizpalm.data.entities.Product;
import project.bizpalm.data.entities.Transaction;
import project.bizpalm.data.entities.TransactionItem;
import project.bizpalm.data.entities.User;

@Database(entities = {Product.class, Transaction.class, TransactionItem.class, InventoryLog.class, Notification.class, User.class}, version = 11, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ProductDao productDao();
    public abstract TransactionDao transactionDao();
    public abstract TransactionItemDao transactionItemDao();
    public abstract InventoryLogDao inventoryLogDao();
    public abstract NotificationDao notificationDao();
    public abstract UserDao userDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "bizpalm_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
