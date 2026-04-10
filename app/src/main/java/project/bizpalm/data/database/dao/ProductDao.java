package project.bizpalm.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import project.bizpalm.data.entities.Product;

@Dao
public interface ProductDao {
    @Insert
    long insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    @Query("SELECT * FROM products WHERE id = :id")
    Product getProductById(int id);

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    Product getProductByBarcode(String barcode);

    @Query("SELECT * FROM products ORDER BY product_name ASC")
    LiveData<List<Product>> getAllProducts();

    @Query("SELECT * FROM products ORDER BY product_name ASC")
    List<Product> getAllProductsSync();

    @Query("SELECT * FROM products WHERE quantity <= 10 AND quantity > 0")
    LiveData<List<Product>> getLowStockProducts();

    @Query("SELECT * FROM products WHERE quantity = 0")
    LiveData<List<Product>> getOutOfStockProducts();

    @Query("SELECT * FROM products WHERE is_popular = 1")
    LiveData<List<Product>> getPopularProducts();

    @Query("SELECT * FROM products WHERE product_name LIKE '%' || :search || '%' OR barcode LIKE '%' || :search || '%' ORDER BY product_name ASC")
    LiveData<List<Product>> searchProducts(String search);

    @Query("SELECT COUNT(*) FROM products WHERE quantity <= 10 AND quantity > 0")
    LiveData<Integer> getLowStockCount();
}
