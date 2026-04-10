package project.bizpalm.ui.products;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import project.bizpalm.data.entities.Product;
import project.bizpalm.data.repository.ProductRepository;

public class ProductListViewModel extends AndroidViewModel {
    private final ProductRepository repository;

    public ProductListViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
    }

    public LiveData<List<Product>> getAllProducts() {
        return repository.getAllProducts();
    }

    public LiveData<List<Product>> searchProducts(String query) {
        return repository.searchProducts(query);
    }

    public void delete(Product product) {
        repository.delete(product);
    }
}
