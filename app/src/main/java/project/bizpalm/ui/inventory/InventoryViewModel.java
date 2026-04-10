package project.bizpalm.ui.inventory;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import project.bizpalm.data.database.AppDatabase;
import project.bizpalm.data.entities.Product;

public class InventoryViewModel extends AndroidViewModel {
    private final AppDatabase db;

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
    }

    public LiveData<List<Product>> getAllProducts() {
        return db.productDao().getAllProducts();
    }

    public LiveData<List<Product>> getLowStockProducts() {
        return db.productDao().getLowStockProducts();
    }

    public LiveData<List<Product>> getOutOfStockProducts() {
        return db.productDao().getOutOfStockProducts();
    }
}
