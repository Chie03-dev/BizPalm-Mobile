package project.bizpalm.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import project.bizpalm.data.entities.Transaction;
import project.bizpalm.data.models.BestSeller;
import project.bizpalm.data.models.DailySales;

@Dao
public interface TransactionDao {
    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    LiveData<List<Transaction>> getAllTransactions();

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    List<Transaction> getAllTransactionsSync();

    @Query("SELECT * FROM transactions WHERE id = :id")
    Transaction getTransactionById(int id);

    @Query("SELECT * FROM transactions WHERE date(timestamp / 1000, 'unixepoch', 'localtime') = :date ORDER BY timestamp ASC")
    List<Transaction> getTransactionsByDateSync(String date);

    @Query("SELECT SUM(total_amount) FROM transactions")
    LiveData<Double> getTotalRevenue();

    @Query("SELECT SUM(amount_due - (unit_cost * quantity_sold)) FROM transaction_items")
    LiveData<Double> getTotalProfit();

    @Query("SELECT COUNT(*) FROM transactions")
    LiveData<Integer> getTransactionCount();

    @Query("SELECT p.product_name as productName, SUM(ti.quantity_sold) as totalQuantity, p.id as productId " +
           "FROM transaction_items ti " +
           "JOIN products p ON ti.product_id = p.id " +
           "GROUP BY ti.product_id " +
           "ORDER BY totalQuantity DESC LIMIT :limit")
    LiveData<List<BestSeller>> getBestSellers(int limit);

    @Query("SELECT p.product_name as productName, SUM(ti.quantity_sold) as totalQuantity, p.id as productId " +
           "FROM transaction_items ti " +
           "JOIN products p ON ti.product_id = p.id " +
           "GROUP BY ti.product_id " +
           "ORDER BY totalQuantity DESC LIMIT :limit")
    List<BestSeller> getBestSellersSync(int limit);

    @Query("SELECT p.product_name as productName, SUM(ti.quantity_sold) as totalQuantity, p.id as productId " +
           "FROM transaction_items ti " +
           "JOIN products p ON ti.product_id = p.id " +
           "JOIN transactions t ON ti.transaction_id = t.id " +
           "WHERE strftime('%m', t.timestamp / 1000, 'unixepoch', 'localtime') = :month " +
           "AND strftime('%Y', t.timestamp / 1000, 'unixepoch', 'localtime') = :year " +
           "GROUP BY ti.product_id " +
           "ORDER BY totalQuantity DESC LIMIT :limit")
    LiveData<List<BestSeller>> getBestSellersByMonth(String month, String year, int limit);

    @Query("SELECT p.product_name as productName, COALESCE(SUM(ti.quantity_sold), 0) as totalQuantity, p.id as productId " +
           "FROM products p " +
           "LEFT JOIN transaction_items ti ON p.id = ti.product_id " +
           "GROUP BY p.id " +
           "ORDER BY totalQuantity ASC LIMIT 5")
    LiveData<List<BestSeller>> getSlowSellers();

    @Query("SELECT date(t1.timestamp / 1000, 'unixepoch', 'localtime') as date, " +
           "SUM(t1.total_amount) as totalAmount, " +
           "(SELECT SUM(ti.amount_due - (ti.unit_cost * ti.quantity_sold)) " +
           " FROM transaction_items ti JOIN transactions t2 ON ti.transaction_id = t2.id " +
           " WHERE date(t2.timestamp / 1000, 'unixepoch', 'localtime') = date(t1.timestamp / 1000, 'unixepoch', 'localtime')) as totalProfit " +
           "FROM transactions t1 " +
           "WHERE date(t1.timestamp / 1000, 'unixepoch', 'localtime') >= date('now', 'localtime', '-' || (:days - 1) || ' days') " +
           "GROUP BY date " +
           "ORDER BY date ASC")
    LiveData<List<DailySales>> getDailySales(int days);

    @Query("SELECT date(t1.timestamp / 1000, 'unixepoch', 'localtime') as date, " +
           "SUM(t1.total_amount) as totalAmount, " +
           "(SELECT SUM(ti.amount_due - (ti.unit_cost * ti.quantity_sold)) " +
           " FROM transaction_items ti JOIN transactions t2 ON ti.transaction_id = t2.id " +
           " WHERE date(t2.timestamp / 1000, 'unixepoch', 'localtime') = date(t1.timestamp / 1000, 'unixepoch', 'localtime')) as totalProfit " +
           "FROM transactions t1 " +
           "WHERE date(t1.timestamp / 1000, 'unixepoch', 'localtime') >= date('now', 'localtime', '-' || (:days - 1) || ' days') " +
           "GROUP BY date " +
           "ORDER BY date ASC")
    List<DailySales> getDailySalesSync(int days);

    @Query("SELECT date(t1.timestamp / 1000, 'unixepoch', 'localtime') as date, " +
           "SUM(t1.total_amount) as totalAmount, " +
           "(SELECT SUM(ti.amount_due - (ti.unit_cost * ti.quantity_sold)) " +
           " FROM transaction_items ti JOIN transactions t2 ON ti.transaction_id = t2.id " +
           " WHERE date(t2.timestamp / 1000, 'unixepoch', 'localtime') = date(t1.timestamp / 1000, 'unixepoch', 'localtime')) as totalProfit " +
           "FROM transactions t1 " +
           "WHERE date(t1.timestamp / 1000, 'unixepoch', 'localtime') = :date")
    DailySales getDailySalesForDateSync(String date);

    @Query("SELECT CAST(strftime('%H', timestamp / 1000, 'unixepoch', 'localtime') AS INTEGER) as hour, " +
           "SUM(total_amount) / CAST(MAX(1, (SELECT COUNT(DISTINCT date(timestamp / 1000, 'unixepoch', 'localtime')) FROM transactions)) AS REAL) as totalAmount " +
           "FROM transactions " +
           "GROUP BY hour " +
           "ORDER BY hour ASC")
    LiveData<List<HourlySales>> getHourlySales();

    @Query("SELECT p.product_name as category, SUM(ti.amount_due) as revenue " +
           "FROM transaction_items ti " +
           "JOIN products p ON ti.product_id = p.id " +
           "GROUP BY p.product_name " +
           "ORDER BY revenue DESC")
    LiveData<List<CategoryRevenue>> getCategoryRevenue();

    @Query("SELECT product_id, SUM(quantity_sold) as totalQty FROM transaction_items ti " +
           "JOIN transactions t ON ti.transaction_id = t.id " +
           "WHERE t.timestamp >= :startTime " +
           "GROUP BY product_id")
    List<ProductSalesVelocity> getSalesVelocity(long startTime);

    class HourlySales {
        public int hour;
        public double totalAmount;
    }

    class CategoryRevenue {
        public String category;
        public double revenue;
    }

    class ProductSalesVelocity {
        public int product_id;
        public int totalQty;
    }
}
