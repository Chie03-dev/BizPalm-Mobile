package project.bizpalm.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import project.bizpalm.data.database.AppDatabase;
import project.bizpalm.data.database.dao.ProductDao;
import project.bizpalm.data.entities.Product;

public class ProductRepository {
    private final ProductDao productDao;
    private final ExecutorService executorService;

    public ProductRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        productDao = db.productDao();
        executorService = Executors.newFixedThreadPool(2);
    }

    public void insert(Product product) {
        executorService.execute(() -> productDao.insert(product));
    }

    public void update(Product product) {
        executorService.execute(() -> productDao.update(product));
    }

    public void delete(Product product) {
        executorService.execute(() -> productDao.delete(product));
    }

    public LiveData<List<Product>> getAllProducts() {
        return productDao.getAllProducts();
    }

    public LiveData<List<Product>> searchProducts(String search) {
        return productDao.searchProducts(search);
    }

    public LiveData<Integer> getLowStockCount() {
        return productDao.getLowStockCount();
    }
}
